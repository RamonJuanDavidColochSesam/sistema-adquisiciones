using System.IO;
using System.Text;
using System.Text.Json.Nodes;
namespace GuateCompras.Desktop.Services;
public static class CsvExporter {
 public static string Escape(string value){if(value.Length>0&&"=+@-".Contains(value[0]))value="'"+value;return "\""+value.Replace("\"","\"\"")+"\"";}
 public static void Write(string path,JsonArray rows){
  if(rows.Count==0)throw new InvalidOperationException("No hay datos para exportar");
  var columns=rows[0]!.AsObject().Select(p=>p.Key).ToArray();
  var lines=new List<string>{string.Join(",",columns.Select(Escape))};
  lines.AddRange(rows.Select(r=>string.Join(",",columns.Select(c=>Escape(r![c]?.ToString()??"")))));
  File.WriteAllLines(path,lines,new UTF8Encoding(true));
 }
}
