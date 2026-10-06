using GuateCompras.Desktop.Services;
using System.Collections.ObjectModel;
using System.Data;
using System.Text.Json.Nodes;
using System.Windows.Input;

namespace GuateCompras.Desktop.ViewModels;
public record Module(string Key,string Label,string Screen);
public sealed class MainViewModel:ObservableObject {
 public ApiClient Api {get;}
 public ObservableCollection<Module> Modules {get;}=[];
 public static readonly Module[] Catalog=[
  new("dashboard","Resumen institucional","Dashboard"),new("pedidos","Solicitudes de compra","Pedidos"),new("ordenes","Órdenes de compra","Ordenes"),new("ofertas","Ofertas","Ofertas"),new("adjudicaciones","Adjudicaciones","Adjudicaciones"),
  new("articulos","Artículos","Articulos"),new("proveedores","Proveedores","Proveedores"),new("proveedorarticulos","Catálogo de proveedores","ProveedorArticulos"),new("sucursales","Sucursales","Sucursales"),new("departamentos","Departamentos","Departamentos"),new("telefonos","Teléfonos de sucursal","Sucursales"),new("rubros","Rubros de proveedores","Proveedores"),new("relaciones","Relaciones comerciales","Relaciones"),new("tiposorden","Tipos de orden","TiposOrden"),new("subtiposorden","Subtipos de orden","TiposOrden"),new("evaluaciones","Evaluaciones","Evaluaciones"),new("reportes","Informes y SSRS","Reportes"),new("usuarios","Usuarios","Usuarios"),new("roles","Roles","Roles"),new("pantallas","Pantallas","Pantallas"),new("permisos","Matriz de permisos","Permisos"),new("conexion","Conexión de bases","Conexion"),new("auditoria","Historial de cambios","Auditoria")];
 private Module? selectedModule;public Module? SelectedModule {get=>selectedModule;set{if(Set(ref selectedModule,value)){page=1;}}}
 private DataView? rows;public DataView? Rows{get=>rows;private set=>Set(ref rows,value);}
 private string status="";public string Status{get=>status;set=>Set(ref status,value);}
 private string search="";public string Search{get=>search;set=>Set(ref search,value);}
 private bool busy;public bool Busy{get=>busy;private set=>Set(ref busy,value);}
 private int page=1;private long total;
 public string PageLabel=>total+" registros · Página "+page;
 public string Identity=>Api.User?["nombreCompleto"]+" · "+Api.User?["rol"];
 public string Title=>SelectedModule?.Label??"GuateCompras";
 public string MoneyTotal {get;private set;}="Q 0.00";
 public string OpenOrders {get;private set;}="0";
 public string Requests {get;private set;}="0";
 public string Awards {get;private set;}="0";
 public bool IsDashboard=>SelectedModule?.Key=="dashboard";
 public bool CanCreate=>SelectedModule!=null&&Api.Can(SelectedModule.Screen,"crear")&&IsEditable;
 public bool CanEdit=>SelectedModule!=null&&Api.Can(SelectedModule.Screen,"actualizar")&&IsEditable;
 public bool CanDelete=>SelectedModule!=null&&Api.Can(SelectedModule.Screen,"borrar")&&IsEditable;
 public bool IsEditable=>SelectedModule!=null&&!new[]{"dashboard","reportes","conexion","auditoria"}.Contains(SelectedModule.Key);
 public JsonArray Records {get;private set;}=[];
 public JsonObject? Schema {get;private set;}
 public ICommand RefreshCommand{get;}
 public ICommand SearchCommand{get;}
 public ICommand NextCommand{get;}
 public ICommand PreviousCommand{get;}
 public MainViewModel(ApiClient api){
  Api=api;RefreshCommand=new AsyncCommand(LoadAsync);SearchCommand=new AsyncCommand(async()=>{page=1;await LoadAsync();});
  NextCommand=new AsyncCommand(async()=>{if(page*20<total){page++;await LoadAsync();}});
  PreviousCommand=new AsyncCommand(async()=>{if(page>1){page--;await LoadAsync();}});
 }
 public async Task InitializeAsync(){Modules.Clear();foreach(var module in Catalog)if(Api.Can(module.Screen,"leer"))Modules.Add(module);Notify(nameof(Identity));selectedModule=Modules.FirstOrDefault();Notify(nameof(SelectedModule));await LoadAsync();}
 public async Task LoadAsync(){
  if(SelectedModule==null)return;
  Busy=true;Status="Cargando información…";
  try{
   string module=SelectedModule.Key;Schema=null;JsonNode result;
   if(module=="dashboard"){
    result=await Api.SendAsync("dashboard");MoneyTotal=decimal.Parse(result["montoAdjudicado"]!.ToString(),System.Globalization.CultureInfo.InvariantCulture).ToString("C2",System.Globalization.CultureInfo.GetCultureInfo("es-GT"));OpenOrders=result["ordenesAbiertas"]!.ToString();Requests=result["pedidos"]!.ToString();Awards=result["adjudicaciones"]!.ToString();
    foreach(var prop in new[]{nameof(MoneyTotal),nameof(OpenOrders),nameof(Requests),nameof(Awards)})Notify(prop);
    Records=(JsonArray)result["topProveedores"]!.DeepClone();total=Records.Count;
   }else if(module=="conexion"){
    result=await Api.SendAsync("conexion/test");Records=[new JsonObject{["motor"]="SQL Server",["disponible"]=result["sqlServer"]!.DeepClone(),["tiempo_ms"]=result["sqlServerMs"]!.DeepClone()},new JsonObject{["motor"]="PostgreSQL",["disponible"]=result["postgres"]!.DeepClone(),["tiempo_ms"]=result["postgresMs"]!.DeepClone()}];total=2;
   }else if(module=="reportes"){result=await Api.SendAsync("reportes");Records=(JsonArray)result["informes"]!.DeepClone();total=8;}
   else if(module is "ordenes" or "adjudicaciones" or "auditoria"){result=await Api.SendAsync(module+"?page="+page);Records=(JsonArray)result["items"]!.DeepClone();total=result["total"]?.GetValue<long>()??Records.Count;}
   else{Schema=(JsonObject)await Api.SendAsync("gestion/"+module+"/schema");result=await Api.SendAsync("gestion/"+module+"?page="+page+"&q="+Uri.EscapeDataString(Search));Records=(JsonArray)result["items"]!.DeepClone();total=result["total"]!.GetValue<long>();}
   Rows=ToTable(Records);Status=Records.Count==0?"No hay registros para esta consulta.":"Información actualizada.";
  }catch(Exception ex){Status=ex.Message;}
  finally{Busy=false;foreach(var prop in new[]{nameof(Title),nameof(PageLabel),nameof(IsDashboard),nameof(CanCreate),nameof(CanEdit),nameof(CanDelete),nameof(Schema)})Notify(prop);}
 }
 public static DataView ToTable(JsonArray records){
  var table=new DataTable();
  var objects=records.OfType<JsonObject>().ToArray();
  foreach(var record in objects)foreach(var pair in record){
   if(table.Columns.Contains(pair.Key)||pair.Key=="_key"||pair.Value is JsonArray or JsonObject)continue;
   bool numeric=objects.Any(o=>o[pair.Key]?.GetValueKind()==System.Text.Json.JsonValueKind.Number);
   table.Columns.Add(pair.Key,numeric?typeof(decimal):typeof(string));
  }
  foreach(var record in objects){var row=table.NewRow();foreach(DataColumn col in table.Columns){
   var value=record[col.ColumnName];row[col.ColumnName]=col.DataType==typeof(decimal)?value==null?DBNull.Value:decimal.Parse(value.ToString(),System.Globalization.CultureInfo.InvariantCulture):value?.ToString()??"—";
  }table.Rows.Add(row);}
  return table.DefaultView;
 }
}
