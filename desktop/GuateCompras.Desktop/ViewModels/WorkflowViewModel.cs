using GuateCompras.Desktop.Services;
using System.Collections.ObjectModel;
using System.Globalization;
using System.Net.Http;
using System.Text.Json.Nodes;

namespace GuateCompras.Desktop.ViewModels;
public sealed class PedidoChoice:ObservableObject {
 public int Id{get;init;}public string Label{get;init;}="";
 private bool selected;public bool Selected{get=>selected;set=>Set(ref selected,value);}
}
public sealed class AwardLine:ObservableObject {
 public int Pedido{get;init;}public string Label{get;init;}="";public int Maximum{get;init;}
 public ObservableCollection<Choice> Offers{get;}=[];public Dictionary<string,string> Prices{get;}=[];
 private string offer="";public string Offer{get=>offer;set{if(Set(ref offer,value)&&Prices.TryGetValue(value,out var price))Price=price;}}
 private string quantity="";public string Quantity{get=>quantity;set=>Set(ref quantity,value);}
 private string price="";public string Price{get=>price;set=>Set(ref price,value);}
}
public sealed class WorkflowViewModel(ApiClient api,JsonObject? order,JsonObject? award,bool awarding):ObservableObject {
 public bool Awarding=>awarding;public bool Ordering=>!awarding;
 public string Title=>awarding?(award==null?"Adjudicar orden":"Editar adjudicación"):(order==null?"Nueva orden":"Editar orden");
 public ObservableCollection<EditorField> Fields{get;}=[];
 public ObservableCollection<PedidoChoice> Pedidos{get;}=[];
 public ObservableCollection<AwardLine> AwardLines{get;}=[];
 public string Observations{get;set;}="";
 public DateTime Resolution{get;set;}=DateTime.Today;
 private EditorViewModel? editor;
 private static JsonObject F(string name,string label,string type,bool required,int max=0,string? reference=null)=>new(){["nombre"]=name,["etiqueta"]=label,["tipo"]=type,["requerido"]=required,["maximo"]=max,["referencia"]=reference};
 public async Task InitializeAsync(){
  if(awarding){
   if(order==null)throw new InvalidOperationException("Seleccione una orden");
   Observations=award?["observaciones"]?.ToString()??"";
   if(award!=null)Resolution=DateTime.Parse(award["fecha_resolucion"]!.ToString());
   foreach(var pedido in order["pedidos"]!.AsArray()){
    int id=pedido!["id_pedido"]!.GetValue<int>();var old=award?["detalles"]?.AsArray().FirstOrDefault(d=>d!["id_pedido"]!.GetValue<int>()==id);
    var line=new AwardLine{Pedido=id,Maximum=pedido["cantidad"]!.GetValue<int>(),Label=pedido["articulo"]+" · Pedido "+id+" · Cantidad solicitada: "+pedido["cantidad"],Quantity=old?["cantidad_final"]?.ToString()??pedido["cantidad"]!.ToString()};
    foreach(var offer in order["ofertas"]!.AsArray().Where(o=>o!["id_pedido"]!.GetValue<int>()==id)){
     string key=offer!["id_oferta"]!.ToString();line.Offers.Add(new Choice(key,offer["proveedor"]+" · Q "+offer["precio_unitario"]));line.Prices[key]=offer["precio_unitario"]!.ToString();
    }
    line.Offer=old?["id_oferta"]?.ToString()??"";line.Price=old?["precio_acordado"]?.ToString()??line.Price;AwardLines.Add(line);
   }
  }else{
   var schema=new JsonObject{["claves"]=new JsonArray(),["campos"]=new JsonArray(F("descripcion","Descripción","text",true,200),F("observaciones","Observaciones","text",false,300),F("fecha_creacion","Fecha creación","date",true),F("fecha_limite_oferta","Límite de ofertas","date",true),F("id_tipo","Tipo","number",true,reference:"tiposorden"),F("id_subtipo","Subtipo","number",false,reference:"subtiposorden"))};
   editor=new EditorViewModel(api,"ordenes",schema,order);await editor.InitializeAsync();foreach(var field in editor.Fields)Fields.Add(field);
   var included=order?["pedidos"]?.AsArray().Select(p=>p!["id_pedido"]!.GetValue<int>()).ToHashSet()??[];
   int page=1;
   while(true){var result=await api.SendAsync("gestion/pedidos?pageSize=100&page="+page);foreach(var p in result["items"]!.AsArray()){int id=p!["id_pedido"]!.GetValue<int>();if(p["id_orden"]==null||included.Contains(id))Pedidos.Add(new PedidoChoice{Id=id,Selected=included.Contains(id),Label="Pedido "+id+" · Artículo "+p["id_articulo"]+" · "+p["cantidad"]+" unidades · "+p["fecha_solicitud"]});}if(page*100>=result["total"]!.GetValue<long>())break;page++;}
  }
 }
 public async Task SaveAsync(){
  if(!awarding){
   var body=editor!.BuildBody();body["pedidoIds"]=new JsonArray(Pedidos.Where(p=>p.Selected).Select(p=>(JsonNode?)JsonValue.Create(p.Id)).ToArray());
   await api.SendAsync("ordenes"+(order==null?"":"/"+order["id_orden"]),order==null?HttpMethod.Post:HttpMethod.Put,body);
  }else{
   var details=new JsonArray();
   foreach(var line in AwardLines){
    if(!int.TryParse(line.Offer,out int offer)||!int.TryParse(line.Quantity,out int qty)||qty<1||qty>line.Maximum||!decimal.TryParse(line.Price,NumberStyles.Number,CultureInfo.InvariantCulture,out decimal price)||price<=0)throw new InvalidOperationException("Complete oferta, cantidad y precio de "+line.Label);
    details.Add(new JsonObject{["id_pedido"]=line.Pedido,["id_oferta"]=offer,["cantidad_final"]=qty,["precio_acordado"]=price});
   }
   await api.SendAsync("adjudicaciones"+(award==null?"":"/"+award["id_adjudicacion"]),award==null?HttpMethod.Post:HttpMethod.Put,new JsonObject{["id_orden"]=order!["id_orden"]!.DeepClone(),["fecha_resolucion"]=Resolution.ToString("yyyy-MM-dd"),["observaciones"]=Observations,["detalles"]=details});
  }
 }
}
