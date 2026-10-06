import java.nio.file.*;
import java.sql.*;
import java.util.*;
public class ProbeContext {
 public static void main(String[] a)throws Exception{
  Properties p=new Properties();try(var in=Files.newInputStream(Path.of(a[0]))){p.load(in);}
  try(Connection c=DriverManager.getConnection(p.getProperty("sqlserver.url"),p.getProperty("sqlserver.user"),p.getProperty("sqlserver.password"));Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT DB_NAME(), HAS_PERMS_BY_NAME(DB_NAME(),'DATABASE','VIEW DEFINITION'), SCHEMA_NAME()")){r.next();System.out.println("database="+r.getString(1)+"; view_definition="+r.getInt(2)+"; esquema="+r.getString(3));}
  try{Class<?> manager=Class.forName("com.adquisiciones.conexion.ConexionManager"); Object instance=manager.getMethod("getInstancia").invoke(null); ((Connection)manager.getMethod("getConexionSqlServer").invoke(instance)).close();System.out.println("HIKARI_MANAGER=CONNECTED");}catch(Exception e){Throwable cause=e; while(cause.getCause()!=null)cause=cause.getCause(); System.out.println("HIKARI_MANAGER=FAILED; tipo="+cause.getClass().getSimpleName()); if(cause instanceof SQLException x)System.out.println("SQLState="+x.getSQLState()+"; code="+x.getErrorCode());}
  System.exit(0);
 }
}


