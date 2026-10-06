using System.Net;
using System.Net.Http;
using System.Net.Http.Json;
using System.Text.Json.Nodes;

namespace GuateCompras.Desktop.Services;
public sealed class ApiClient : IDisposable {
 private readonly HttpClient client;
 public JsonObject? User { get; private set; }
 public string BaseUrl { get; }
 public ApiClient(string baseUrl) {
  var uri=new Uri(baseUrl.TrimEnd('/')+"/");
  if(uri.Scheme is not ("http" or "https"))throw new ArgumentException("Use una dirección HTTP o HTTPS");
  BaseUrl=uri.ToString();
  client=new HttpClient(new HttpClientHandler{CookieContainer=new CookieContainer(),UseCookies=true}){BaseAddress=uri,Timeout=TimeSpan.FromSeconds(25)};
 }
 public async Task<JsonNode> SendAsync(string path,HttpMethod? method=null,JsonNode? body=null) {
  method??=HttpMethod.Get;
  using var request=new HttpRequestMessage(method,path);
  if(body!=null)request.Content=JsonContent.Create(body);
  if(method!=HttpMethod.Get && path!="login" && User?["csrf"] is JsonNode token)request.Headers.Add("X-CSRF-Token",token.GetValue<string>());
  using var response=await client.SendAsync(request);
  if(response.StatusCode==HttpStatusCode.NoContent)return new JsonObject();
  var content=await response.Content.ReadAsStringAsync();
  JsonNode? data;try{data=JsonNode.Parse(content);}catch{throw new InvalidOperationException("El servidor devolvió una respuesta no válida");}
  if(!response.IsSuccessStatusCode) {
   if(response.StatusCode==HttpStatusCode.Unauthorized)User=null;
   throw new InvalidOperationException(data?["error"]?.ToString()??"No se pudo completar la operación ("+(int)response.StatusCode+")");
  }
  return data??new JsonObject();
 }
 public async Task LoginAsync(string username,string password){User=(JsonObject)await SendAsync("login",HttpMethod.Post,new JsonObject{["nombreUsuario"]=username,["contrasena"]=password});}
 public bool Can(string screen,string operation)=>User?["permisos"]?.AsArray().Any(p=>p?["pantalla"]?.ToString()==screen && p?[operation]?.GetValue<bool>()==true)==true;
 public async Task LogoutAsync(){try{await SendAsync("login/logout",HttpMethod.Post,new JsonObject());}finally{User=null;}}
 public void Dispose()=>client.Dispose();
}
