using GuateCompras.Desktop.Services;
using System.Collections.ObjectModel;
using System.Globalization;
using System.Net.Http;
using System.Text.Json.Nodes;
namespace GuateCompras.Desktop.ViewModels;
public record Choice(string Value,string Label);
public sealed class EditorField:ObservableObject {
 public string Name{get;init;}="";public string Label{get;init;}="";public string Kind{get;init;}="text";public bool Required{get;init;}public bool Enabled{get;init;}=true;public int MaxLength{get;init;}=1000;
 private string text="";public string Text{get=>text;set=>Set(ref text,value);}
 private bool check;public bool Checked{get=>check;set=>Set(ref check,value);}
 private DateTime? date;public DateTime? Date{get=>date;set=>Set(ref date,value);}
 public ObservableCollection<Choice> Choices{get;}=[];
}
public sealed class EditorViewModel(ApiClient api,string module,JsonObject schema,JsonObject? record):ObservableObject {
 public ObservableCollection<EditorField> Fields{get;}=[];
 public string Title=>record==null?"Nuevo registro":"Editar registro";
 public async Task InitializeAsync(){
  var keys=schema["claves"]!.AsArray().Select(n=>n!.ToString()).ToHashSet();
  foreach(var raw in schema["campos"]!.AsArray()){
   var field=raw!.AsObject();string name=field["nombre"]!.ToString(),kind=field["tipo"]!.ToString();string? reference=field["referencia"]?.ToString();
   bool own=name=="id_proveedor"&&api.User?["rol"]?.ToString()=="AdminProveedor";
   string uiKind=reference!=null&&!own?"select":kind is "status" or "tipoorden" or "subtipoorden"?"select":kind;
   var max=field["maximo"]!.GetValue<int>();
   var f=new EditorField{Name=name,Label=field["etiqueta"]!.ToString(),Kind=uiKind,Required=field["requerido"]!.GetValue<bool>()||(kind=="password"&&record==null),Enabled=!(record!=null&&keys.Contains(name))&&!own,MaxLength=max>0?max:1000};
   f.Text=own?api.User!["idProveedor"]!.ToString():record?[name]?.ToString()??"";
   if(kind=="boolean")f.Checked=record?[name]?.GetValue<bool>()??false;
   if(kind=="date")f.Date=DateTime.TryParse(f.Text,out var date)?date:DateTime.Today;
   if(reference!=null&&!own){
    if(!f.Required)f.Choices.Add(new Choice("","Sin asociación"));
    int page=1;
    while(true){
     var data=await api.SendAsync("gestion/"+reference+"?pageSize=100&page="+page);
     foreach(var row in data["items"]!.AsArray()){
      var obj=row!.AsObject();var id=obj.First(p=>p.Key.StartsWith("id_")).Value!.ToString();
      string label=obj["nombre_comercial"]?.ToString()??obj["nombre_completo"]?.ToString()??obj["nombre_rol"]?.ToString()??obj["nombre_pantalla"]?.ToString()??obj["nombre"]?.ToString()??obj["codigo_sucursal"]?.ToString()??"Pedido "+obj["id_pedido"];
      if(reference=="departamentos")label=obj["codigo_sucursal"]+" · "+label;
      if(reference=="articulos")label=obj["codigo_articulo"]+" · "+label;
      if(reference=="proveedores")label=obj["codigo_proveedor"]+" · "+label;
      if(reference=="subtiposorden"){var type=Fields.FirstOrDefault(f=>f.Name=="id_tipo");label=(type?.Choices.FirstOrDefault(c=>c.Value==obj["id_tipo"]?.ToString())?.Label??"Tipo "+obj["id_tipo"])+" · "+label;}
      if(reference=="pedidos")label="Pedido "+id+" · "+obj["articulo"]+" · "+obj["cantidad"]+" unidades";
      f.Choices.Add(new Choice(id,label));
     }
     if(page*100>=data["total"]!.GetValue<long>())break;page++;if(page>100)throw new InvalidOperationException("Hay demasiadas referencias; reduzca la búsqueda");
    }
   }else if(kind is "status" or "tipoorden" or "subtipoorden"){
    string[] values=kind=="status"?["Activo","Inactivo"]:kind=="tipoorden"?["Grande","Chica"]:["Normal","Urgente"];
    foreach(string v in values)f.Choices.Add(new Choice(v,v));if(f.Text=="")f.Text=values[0];
   }
   Fields.Add(f);
  }
 }
 public JsonObject BuildBody(){
  var body=new JsonObject();
  foreach(var f in Fields){
   if(f.Required&&f.Kind!="boolean"&&f.Kind!="date"&&string.IsNullOrWhiteSpace(f.Text))throw new InvalidOperationException("Falta "+f.Label);
   var metadata=schema["campos"]!.AsArray().First(n=>n!["nombre"]!.ToString()==f.Name)!;string kind=metadata["tipo"]!.ToString();
   if(kind=="boolean")body[f.Name]=f.Checked;
   else if(kind=="date")body[f.Name]=f.Date?.ToString("yyyy-MM-dd")??throw new InvalidOperationException("Fecha no válida: "+f.Label);
   else if(string.IsNullOrWhiteSpace(f.Text))body[f.Name]=null;
   else if(kind is "number" or "decimal"){
    if(!decimal.TryParse(f.Text.Trim(),NumberStyles.AllowLeadingSign|NumberStyles.AllowDecimalPoint,CultureInfo.InvariantCulture,out var n))throw new InvalidOperationException("Número no válido (use punto decimal, sin separador de miles): "+f.Label);
    body[f.Name]=JsonValue.Create(n);
   }else body[f.Name]=f.Text.Trim();
  }
  return body;
 }
 public async Task SaveAsync(){
  string path="gestion/"+module+(record==null?"":"/"+Uri.EscapeDataString(record["_key"]!.ToString()));
  await api.SendAsync(path,record==null?HttpMethod.Post:HttpMethod.Put,BuildBody());
 }
}
