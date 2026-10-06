using GuateCompras.Desktop.Services;
using GuateCompras.Desktop.ViewModels;
using Microsoft.Win32;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Text.Json.Nodes;
using System.Windows;
using System.Windows.Controls;
namespace GuateCompras.Desktop.Views;
public partial class ReportWindow:Window {
 private readonly ApiClient api;private readonly int report;private readonly JsonObject metadata;
 private JsonArray rows=[];private string ssrsUrl="";
 public ReportWindow(ApiClient client,JsonObject info){api=client;metadata=info;report=info["id"]!.GetValue<int>();InitializeComponent();TitleText.Text=info["nombre"]!.ToString();From.SelectedDate=DateTime.Today.AddYears(-1);To.SelectedDate=DateTime.Today;Year.Text=DateTime.Today.Year.ToString();Loaded+=async(_,_)=>await InitializeAsync();}
 private async Task InitializeAsync(){try{
  var controls=new Dictionary<string,FrameworkElement>{{"articulo",Article},{"proveedor",Provider},{"orden",Order},{"sucursal",Branch},{"anio",Year},{"desde",From},{"hasta",To}};
  foreach(var entry in controls)((FrameworkElement)entry.Value.Parent).Visibility=metadata["parametros"]!.AsArray().Any(p=>p!.ToString()==entry.Key)?Visibility.Visible:Visibility.Collapsed;
var config=await api.SendAsync("reportes");ssrsUrl=config["ssrsUrl"]?.ToString()??"";SsrsButton.IsEnabled=ssrsUrl!=""&&api.User?["rol"]?.ToString()!="AdminProveedor";SsrsStatus.Text=ssrsUrl==""?"Servidor SSRS todavía no configurado":api.User?["rol"]?.ToString()=="AdminProveedor"?"Consultas limitadas a su proveedor":"Enlace SSRS configurado";}catch(Exception ex){StatusText.Text=ex.Message;}}
 private Dictionary<string,string> Parameters(){
  var values=new Dictionary<string,string>{{"articulo",Article.Text},{"proveedor",Provider.Text},{"orden",Order.Text},{"sucursal",Branch.Text},{"anio",Year.Text},{"desde",From.SelectedDate?.ToString("yyyy-MM-dd")??""},{"hasta",To.SelectedDate?.ToString("yyyy-MM-dd")??""}};
  return values.Where(v=>metadata["parametros"]!.AsArray().Any(p=>p!.ToString()==v.Key)).ToDictionary();
 }
 private async void Query_Click(object sender,RoutedEventArgs e){QueryButton.IsEnabled=false;try{string query=string.Join("&",Parameters().Select(p=>p.Key+"="+Uri.EscapeDataString(p.Value)));var data=await api.SendAsync("reportes/"+report+"?"+query);rows=(JsonArray)data["items"]!.DeepClone();ResultsGrid.ItemsSource=MainViewModel.ToTable(rows);StatusText.Text=rows.Count+" registros · Moneda GTQ";}catch(Exception ex){StatusText.Text=ex.Message;}finally{QueryButton.IsEnabled=true;}}
 private void Export_Click(object sender,RoutedEventArgs e){
  if(rows.Count==0){StatusText.Text="Consulte el informe antes de exportar.";return;}
  var dialog=new SaveFileDialog{FileName="GuateCompras-Informe-"+report+".csv",Filter="Archivo CSV|*.csv"};
  if(dialog.ShowDialog()!=true)return;
  try{CsvExporter.Write(dialog.FileName,rows);StatusText.Text="Informe exportado.";}catch(Exception ex){StatusText.Text=ex.Message;}
 }
 private void Ssrs_Click(object sender,RoutedEventArgs e){
  try{
   string[] names=["01_HistorialCompra","02_TopProveedores","03_ComparacionOfertas","04_PedidosSinAsignar","05_OrdenesAbiertas","06_GastoDepartamento","07_PromedioAdjudicacion","08_EvolucionPrecios"];
   var uri=new Uri(ssrsUrl);if(uri.Scheme is not ("http" or "https"))throw new InvalidOperationException("URL SSRS no válida");
   var mapping=new Dictionary<string,string>{{"articulo","Articulo"},{"proveedor","Proveedor"},{"orden","Orden"},{"sucursal","Sucursal"},{"anio","Anio"},{"desde","Desde"},{"hasta","Hasta"}};
   string url=ssrsUrl.TrimEnd('/')+"?/GuateCompras/"+names[report-1]+"&rs:Command=Render";
   foreach(var p in Parameters())url+="&"+mapping[p.Key]+"="+Uri.EscapeDataString(p.Value);
   Process.Start(new ProcessStartInfo(url){UseShellExecute=true});
  }catch(Exception ex){StatusText.Text=ex.Message;}
 }
 private void Grid_AutoGeneratingColumn(object sender,DataGridAutoGeneratingColumnEventArgs e)=>e.Column.Header=MainWindow.Label(e.PropertyName);
}
