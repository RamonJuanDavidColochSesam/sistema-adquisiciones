using GuateCompras.Desktop.Services;
using System.Windows;
namespace GuateCompras.Desktop.Views;
public partial class LoginWindow:Window {
 public ApiClient? Api{get;private set;}
 public LoginWindow(){InitializeComponent();ServerUrl.Text=Environment.GetEnvironmentVariable("GUATECOMPRAS_API_URL")??"http://127.0.0.1:18080/sistema-adquisiciones/api/";}
 private async void Login_Click(object sender,RoutedEventArgs e){
  LoginButton.IsEnabled=false;ErrorText.Text="";
  try{
   if(string.IsNullOrWhiteSpace(Username.Text)||string.IsNullOrEmpty(Password.Password))throw new InvalidOperationException("Ingrese usuario y contraseña");
   Api?.Dispose();Api=new ApiClient(ServerUrl.Text);await Api.LoginAsync(Username.Text.Trim(),Password.Password);Password.Clear();DialogResult=true;
  }catch(Exception ex){ErrorText.Text=ex.Message;Api?.Dispose();Api=null;}
  finally{LoginButton.IsEnabled=true;}
 }
}
