using GuateCompras.Desktop.Services;
using GuateCompras.Desktop.ViewModels;
using GuateCompras.Desktop.Views;
using System.IO;
using System.Text.Json.Nodes;
using System.Windows;
using System.Windows.Media;
using System.Windows.Media.Imaging;

namespace GuateCompras.Desktop;
public partial class App:Application {
 private ApiClient? sessionClient;
 protected override void OnExit(ExitEventArgs e){sessionClient?.Dispose();base.OnExit(e);}
 protected override async void OnStartup(StartupEventArgs e){
  base.OnStartup(e);ShutdownMode=ShutdownMode.OnExplicitShutdown;
  try{
   if(e.Args.Length>=3&&e.Args[0]=="--self-test"){
    await SelfTest(e.Args[1],e.Args[2]);Shutdown(0);return;
   }
   var login=new LoginWindow();if(login.ShowDialog()!=true){Shutdown();return;}
   sessionClient=login.Api!;var vm=new MainViewModel(sessionClient);await vm.InitializeAsync();var window=new MainWindow(vm);MainWindow=window;ShutdownMode=ShutdownMode.OnMainWindowClose;window.Show();
  }catch(Exception ex){if(e.Args.Contains("--self-test")){File.WriteAllText(Path.Combine(e.Args[2],"wpf-test-error.txt"),ex.ToString());Shutdown(1);}else{MessageBox.Show(ex.Message,"GuateCompras",MessageBoxButton.OK,MessageBoxImage.Error);Shutdown(1);}}
 }
 private static async Task SelfTest(string accessPath,string evidence){
  Directory.CreateDirectory(evidence);
  var values=File.ReadAllLines(accessPath).Where(l=>l.Contains('=')&&!l.StartsWith('#')).Select(l=>l.Split('=',2)).ToDictionary(p=>p[0],p=>p[1]);
  using var api=new ApiClient(Environment.GetEnvironmentVariable("GUATECOMPRAS_API_URL")??"http://127.0.0.1:18080/sistema-adquisiciones/api/");
  await api.LoginAsync("admin",values["admin"]);
  var vm=new MainViewModel(api);await vm.InitializeAsync();
  if(vm.Modules.Count<20||vm.Records.Count==0)throw new InvalidOperationException("No se cargaron módulos o dashboard reales");
  var log=new List<string>{"PASS login WPF con cookies y CSRF","PASS módulos según permisos","PASS dashboard con datos reales"};
  var articleSchema=(JsonObject)await api.SendAsync("gestion/articulos/schema");
  var createEditor=new EditorViewModel(api,"articulos",articleSchema,null);await createEditor.InitializeAsync();
  foreach(var field in createEditor.Fields)field.Text=field.Name switch{"codigo_articulo"=>"TEST-WPF","nombre"=>"Prueba WPF reversible","descripcion"=>"Verificación MVVM","estado"=>"Activo",_=>field.Text};
  await createEditor.SaveAsync();
  var created=(JsonObject)(await api.SendAsync("gestion/articulos?q=TEST-WPF"))["items"]![0]!;
  var updateEditor=new EditorViewModel(api,"articulos",articleSchema,created);await updateEditor.InitializeAsync();updateEditor.Fields.Single(f=>f.Name=="nombre").Text="Prueba WPF actualizada";await updateEditor.SaveAsync();
  await api.SendAsync("gestion/articulos/"+created["_key"],System.Net.Http.HttpMethod.Delete);log.Add("PASS CRUD reversible mediante EditorViewModel y API protegida");
  var priceSchema=(JsonObject)await api.SendAsync("gestion/proveedorarticulos/schema");
  var priceRecord=(JsonObject)(await api.SendAsync("gestion/proveedorarticulos?pageSize=1"))["items"]![0]!;
  var priceEditor=new EditorViewModel(api,"proveedorarticulos",priceSchema,priceRecord);await priceEditor.InitializeAsync();
  var previousCulture=System.Globalization.CultureInfo.CurrentCulture;
  try{
   System.Globalization.CultureInfo.CurrentCulture=System.Globalization.CultureInfo.GetCultureInfo("es-ES");
   priceEditor.Fields.Single(f=>f.Name=="precio").Text="123.45";
   if(priceEditor.BuildBody()["precio"]!.GetValue<decimal>()!=123.45m)throw new InvalidOperationException("Conversión decimal dependiente del idioma");
   priceEditor.Fields.Single(f=>f.Name=="precio").Text="123,45";bool invalid=false;try{priceEditor.BuildBody();}catch(InvalidOperationException){invalid=true;}
   if(!invalid)throw new InvalidOperationException("Se interpretó una coma como separador de miles");
  }finally{System.Globalization.CultureInfo.CurrentCulture=previousCulture;}
  log.Add("PASS formulario de precio conserva decimales y rechaza formato ambiguo");
  var sorted=MainViewModel.ToTable(JsonNode.Parse("[{\"precio\":30},{\"precio\":5}]")!.AsArray());sorted.Sort="precio ASC";
  if((decimal)sorted[0]["precio"]!=5m)throw new InvalidOperationException("Ordenamiento financiero lexicográfico");log.Add("PASS ordenamiento WPF numérico");
  var orderEditor=new WorkflowViewModel(api,null,null,false);await orderEditor.InitializeAsync();
  foreach(var field in orderEditor.Fields){
   if(field.Name=="descripcion")field.Text="TEST-WPF-ORDEN";
   if(field.Name=="id_tipo")field.Text=field.Choices[0].Value;
   if(field.Name=="id_subtipo")field.Text="";
  }
  foreach(var pedido in orderEditor.Pedidos.Take(2))pedido.Selected=true;
  await orderEditor.SaveAsync();
  var createdOrder=(await api.SendAsync("ordenes?pageSize=100"))["items"]!.AsArray().Single(o=>o!["descripcion"]!.ToString()=="TEST-WPF-ORDEN")!;
  await api.SendAsync("ordenes/"+createdOrder["id_orden"],System.Net.Http.HttpMethod.Delete);log.Add("PASS agrupación y liberación de pedidos mediante WorkflowViewModel");
  foreach(string module in new[]{"articulos","proveedores","ordenes","adjudicaciones","reportes","conexion"}){
   vm.SelectedModule=vm.Modules.Single(m=>m.Key==module);await vm.LoadAsync();if(vm.Records.Count==0)throw new InvalidOperationException("Sin datos en "+module);log.Add("PASS carga MVVM "+module+" registros="+vm.Records.Count);
  }
  var reportOrder=(await api.SendAsync("ordenes?pageSize=100"))["items"]![0]!["id_orden"]!.ToString();
  for(int report=1;report<=8;report++){
   var result=await api.SendAsync("reportes/"+report+"?orden="+reportOrder);
   if(result["items"] is not JsonArray)throw new InvalidOperationException("Informe WPF sin datos estructurados");
   CsvExporter.Write(Path.Combine(evidence,"informe-"+report+".csv"),(JsonArray)result["items"]!);
   log.Add("PASS informe WPF y CSV "+report+" filas="+result["total"]);
  }
  if(!CsvExporter.Escape("=SUM(A1)").StartsWith("\"'="))throw new InvalidOperationException("CSV sin protección de fórmulas");log.Add("PASS CSV neutraliza fórmulas");
  bool rejected=false;try{await api.SendAsync("reportes/3?orden=0");}catch(InvalidOperationException ex){rejected=ex.Message.Contains("Seleccione una orden");}
  if(!rejected)throw new InvalidOperationException("El cliente no gestionó el error de validación");log.Add("PASS mensaje de validación WPF desde API");
  vm.SelectedModule=vm.Modules.Single(m=>m.Key=="dashboard");await vm.LoadAsync();
  var window=new MainWindow(vm);var root=(FrameworkElement)window.Content;root.Measure(new Size(1400,900));root.Arrange(new Rect(0,0,1400,900));root.UpdateLayout();
  var image=new RenderTargetBitmap(1400,900,96,96,PixelFormats.Pbgra32);image.Render(root);var encoder=new PngBitmapEncoder();encoder.Frames.Add(BitmapFrame.Create(image));using(var stream=File.Create(Path.Combine(evidence,"wpf-dashboard.png")))encoder.Save(stream);
  await api.LogoutAsync();log.Add("PASS logout WPF");File.WriteAllLines(Path.Combine(evidence,"wpf-self-test.log"),log);
  foreach(string role in new[]{"gestor","proveedor","auditor"}){
   using var other=new ApiClient(api.BaseUrl);await other.LoginAsync(role,values[role]);var otherModel=new MainViewModel(other);await otherModel.InitializeAsync();
   if(role=="auditor"&&other.Can("Articulos","crear"))throw new InvalidOperationException("Auditor con permiso de escritura");
   if(role=="proveedor"&&other.Can("Usuarios","leer"))throw new InvalidOperationException("Proveedor con acceso administrativo");
   await other.LogoutAsync();log.Add("PASS cliente WPF y permisos "+role);
  }
  File.WriteAllLines(Path.Combine(evidence,"wpf-self-test.log"),log);
 }
}
