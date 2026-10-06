import java.nio.file.*;
import java.sql.*;
import java.util.*;
public class ProbeDb {
 public static void main(String[] args) throws Exception {
  Properties p=new Properties(); try(var in=Files.newInputStream(Path.of(args[0]))){p.load(in);}
  for(String db:List.of("sqlserver","postgres")) {
   long start=System.nanoTime();
   try { Class.forName(db.equals("sqlserver")?"com.microsoft.sqlserver.jdbc.SQLServerDriver":"org.postgresql.Driver");
    Properties c=new Properties(); c.setProperty("user",p.getProperty(db+".user",""));c.setProperty("password",p.getProperty(db+".password",""));
    DriverManager.setLoginTimeout(5);
    try(Connection conn=DriverManager.getConnection(p.getProperty(db+".url"),c);Statement s=conn.createStatement()){
     s.setQueryTimeout(5);try(ResultSet r=s.executeQuery("SELECT 1")){r.next();System.out.println(db+": CONNECTED; SELECT 1="+r.getInt(1)+"; ms="+(System.nanoTime()-start)/1_000_000);}
     if(db.equals("sqlserver")){try(ResultSet r=s.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='BASE TABLE'")){r.next();System.out.println("sqlserver tablas="+r.getInt(1));}
      for(String t:List.of("Sucursal","Departamento","Articulo","Proveedor","Pedido","OrdenCompra","Oferta","Adjudicacion","Usuario","Rol","Permiso")){try(ResultSet r=s.executeQuery("SELECT COUNT(*) FROM dbo."+t)){r.next();System.out.println(t+" registros="+r.getInt(1));}catch(SQLException e){System.out.println(t+": NO_VERIFICADO; SQLState="+e.getSQLState()+"; code="+e.getErrorCode());}}
     }
    }
   }catch(Exception e){System.out.println(db+": FAILED; tipo="+e.getClass().getSimpleName()+"; ms="+(System.nanoTime()-start)/1_000_000); if(e instanceof SQLException x)System.out.println("SQLState="+x.getSQLState()+"; code="+x.getErrorCode());}
  }
 }
}
