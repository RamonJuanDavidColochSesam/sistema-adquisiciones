using GuateCompras.Desktop.ViewModels;
using System.Windows;
using System.Windows.Controls;
namespace GuateCompras.Desktop.Views;
public partial class EditorWindow:Window {
 private readonly EditorViewModel vm;
 public EditorWindow(EditorViewModel model){vm=model;DataContext=model;InitializeComponent();}
 private void Password_Changed(object sender,RoutedEventArgs e){if(sender is PasswordBox box&&box.DataContext is EditorField field)field.Text=box.Password;}
 private async void Save_Click(object sender,RoutedEventArgs e){SaveButton.IsEnabled=false;ErrorText.Text="";try{await vm.SaveAsync();DialogResult=true;}catch(Exception ex){ErrorText.Text=ex.Message;}finally{SaveButton.IsEnabled=true;}}
}
