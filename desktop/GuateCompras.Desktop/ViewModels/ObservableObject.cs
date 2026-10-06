using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Windows.Input;
namespace GuateCompras.Desktop.ViewModels;
public abstract class ObservableObject:INotifyPropertyChanged {
 public event PropertyChangedEventHandler? PropertyChanged;
 protected void Notify([CallerMemberName]string? name=null)=>PropertyChanged?.Invoke(this,new PropertyChangedEventArgs(name));
 protected bool Set<T>(ref T field,T value,[CallerMemberName]string? name=null){if(EqualityComparer<T>.Default.Equals(field,value))return false;field=value;Notify(name);return true;}
}
public sealed class AsyncCommand(Func<Task> execute,Func<bool>? canExecute=null):ICommand {
 private bool running;
 public event EventHandler? CanExecuteChanged;
 public bool CanExecute(object? parameter)=>!running&&(canExecute?.Invoke()??true);
 public async void Execute(object? parameter){if(!CanExecute(parameter))return;running=true;CanExecuteChanged?.Invoke(this,EventArgs.Empty);try{await execute();}finally{running=false;CanExecuteChanged?.Invoke(this,EventArgs.Empty);}}
}
