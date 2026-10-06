using GuateCompras.Desktop.Services;
using GuateCompras.Desktop.ViewModels;
using GuateCompras.Desktop.Views;
using System.Data;
using System.Net.Http;
using System.Text.Json.Nodes;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
namespace GuateCompras.Desktop;
public partial class MainWindow:Window {
 public MainViewModel Model{get;}
 public MainWindow(MainViewModel model){Model=model;DataContext=model;InitializeComponent();}
 private async void Module_Changed(object sender,SelectionChangedEventArgs e){if(IsLoaded)await Model.LoadAsync();}
 private JsonObject? Selected(){
  if(RecordsGrid.SelectedItem is not DataRowView row)return null;
  foreach(var record in Model.Records.OfType<JsonObject>())
   if(record.Where(p=>p.Value is not (JsonArray or JsonObject)&&p.Key!="_key").All(p=>row.Row.Table.Columns.Contains(p.Key)&&(row.Row.IsNull(p.Key)?"—":Convert.ToString(row[p.Key],System.Globalization.CultureInfo.InvariantCulture))==(p.Value?.ToString()??"—")))return record;
  return null;
 }
 private async Task Guard(Func<Task> action){try{await action();}catch(Exception ex){Model.Status=ex.Message;}}
 private async void New_Click(object sender,RoutedEventArgs e)=>await Guard(async()=>{
  if(Model.SelectedModule?.Key=="ordenes"){await Workflow(null,null,false);return;}
  if(Model.SelectedModule?.Key=="adjudicaciones"){Model.Status="Seleccione una orden y use Ver / comparar para adjudicar todos sus pedidos.";return;}
  await Edit(null);
 });
 private async void Edit_Click(object sender,RoutedEventArgs e)=>await Guard(async()=>{
  var record=Selected()??throw new InvalidOperationException("Seleccione un registro");
  string module=Model.SelectedModule!.Key;
  if(module=="ordenes"){await Workflow((JsonObject)await Model.Api.SendAsync("ordenes/"+record["id_orden"]),null,false);return;}
  if(module=="adjudicaciones"){var award=(JsonObject)await Model.Api.SendAsync("adjudicaciones/"+record["id_adjudicacion"]);var order=(JsonObject)await Model.Api.SendAsync("ordenes/"+award["id_orden"]);await Workflow(order,award,true);return;}
  await Edit(record);
 });
 private async Task Edit(JsonObject? record){
  if(Model.Schema==null)return;
  var editor=new EditorViewModel(Model.Api,Model.SelectedModule!.Key,Model.Schema,record);await editor.InitializeAsync();
  var window=new EditorWindow(editor){Owner=this};if(window.ShowDialog()==true)await Model.LoadAsync();
 }
 private async Task Workflow(JsonObject? order,JsonObject? award,bool awarding){var vm=new WorkflowViewModel(Model.Api,order,award,awarding);await vm.InitializeAsync();var window=new WorkflowWindow(vm){Owner=this};if(window.ShowDialog()==true)await Model.LoadAsync();}
 private async void Delete_Click(object sender,RoutedEventArgs e)=>await Guard(async()=>{
  var record=Selected()??throw new InvalidOperationException("Seleccione un registro");if(MessageBox.Show(this,"¿Borrar o revertir este registro? Las referencias pueden impedirlo. La acción queda en el historial.","Confirmar operación",MessageBoxButton.YesNo,MessageBoxImage.Question)!=MessageBoxResult.Yes)return;
  string module=Model.SelectedModule!.Key,path=module=="ordenes"?"ordenes/"+record["id_orden"]:module=="adjudicaciones"?"adjudicaciones/"+record["id_adjudicacion"]:"gestion/"+module+"/"+Uri.EscapeDataString(record["_key"]!.ToString());
  await Model.Api.SendAsync(path,HttpMethod.Delete);await Model.LoadAsync();
 });
 private async void Detail_Click(object sender,RoutedEventArgs e)=>await ShowDetails();
 private async void Grid_DoubleClick(object sender,MouseButtonEventArgs e)=>await ShowDetails();
 private async Task ShowDetails()=>await Guard(async()=>{
  var record=Selected()??throw new InvalidOperationException("Seleccione un registro");
  if(Model.SelectedModule?.Key=="reportes"){new ReportWindow(Model.Api,record){Owner=this}.ShowDialog();return;}
  if(Model.SelectedModule?.Key=="ordenes"){
   var order=(JsonObject)await Model.Api.SendAsync("ordenes/"+record["id_orden"]);
   var panel=new DockPanel{Margin=new Thickness(25)};var header=new StackPanel();
   header.Children.Add(new TextBlock{Text="Orden "+order["id_orden"]+" · "+order["estado"],FontSize=25,Margin=new Thickness(0,0,0,20)});
   if(order["adjudicacion"]==null&&Model.Api.Can("Adjudicaciones","crear")){
    var button=new Button{Content="Adjudicar todos los pedidos",HorizontalAlignment=HorizontalAlignment.Left};
    button.Click+=async(_,_)=>await Guard(async()=>await Workflow(order,null,true));header.Children.Add(button);
   }
   header.Children.Add(new TextBlock{Text="Comparación de ofertas · La columna Ganadora identifica la selección final.",Margin=new Thickness(0,10,0,15)});DockPanel.SetDock(header,Dock.Top);panel.Children.Add(header);
   var grid=new DataGrid{ItemsSource=MainViewModel.ToTable(order["ofertas"]!.AsArray())};grid.AutoGeneratingColumn+=Grid_AutoGeneratingColumn;panel.Children.Add(grid);
   new Window{Title="Comparación de ofertas",Content=panel,Width=1100,Height=680,Owner=this,WindowStartupLocation=WindowStartupLocation.CenterOwner}.ShowDialog();return;
  }
  if(Model.SelectedModule?.Key=="adjudicaciones"){
   var award=await Model.Api.SendAsync("adjudicaciones/"+record["id_adjudicacion"]);var grid=new DataGrid{Margin=new Thickness(25),ItemsSource=MainViewModel.ToTable(award["detalles"]!.AsArray())};grid.AutoGeneratingColumn+=Grid_AutoGeneratingColumn;
   new Window{Title="Detalle adjudicado",Content=grid,Width=1000,Height=650,Owner=this,WindowStartupLocation=WindowStartupLocation.CenterOwner}.ShowDialog();return;
  }
  if(Model.Schema!=null)Model.Status="Seleccione Editar para consultar los campos; su rol determina las operaciones disponibles.";
 });
 private async void Logout_Click(object sender,RoutedEventArgs e)=>await Guard(async()=>{await Model.Api.LogoutAsync();Close();});
 private void Grid_AutoGeneratingColumn(object? sender,DataGridAutoGeneratingColumnEventArgs e){if(e.PropertyName=="_key"){e.Cancel=true;return;}e.Column.Header=Label(e.PropertyName);e.Column.MinWidth=100;e.Column.Width=DataGridLength.Auto;}
 public static string Label(string key)=>key switch{"id_orden"=>"Orden","id_pedido"=>"Pedido","id_oferta"=>"Oferta","id_adjudicacion"=>"Adjudicación","nombre_comercial"=>"Proveedor","fecha_creacion"=>"Creación","fecha_limite_oferta"=>"Límite de ofertas","fecha_resolucion"=>"Resolución","precio_unitario"=>"Precio unitario (Q)","precio_acordado"=>"Precio acordado (Q)","monto_adjudicado"=>"Monto adjudicado (Q)","ganadora"=>"Ganadora",_=>System.Globalization.CultureInfo.GetCultureInfo("es-GT").TextInfo.ToTitleCase(key.Replace('_',' '))};
}
