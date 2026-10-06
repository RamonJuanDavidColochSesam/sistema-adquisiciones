using GuateCompras.Desktop.ViewModels;
using System.Windows;
namespace GuateCompras.Desktop.Views;
public partial class WorkflowWindow:Window{
 private readonly WorkflowViewModel vm;
 public WorkflowWindow(WorkflowViewModel model){vm=model;DataContext=model;InitializeComponent();}
 private async void Save_Click(object sender,RoutedEventArgs e){SaveButton.IsEnabled=false;ErrorText.Text="";try{await vm.SaveAsync();DialogResult=true;}catch(Exception ex){ErrorText.Text=ex.Message;}finally{SaveButton.IsEnabled=true;}}
}
