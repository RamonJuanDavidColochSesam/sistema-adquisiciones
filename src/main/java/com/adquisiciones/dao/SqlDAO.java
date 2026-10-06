package com.adquisiciones.dao;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public final class SqlDAO {
 private SqlDAO(){}
 public static void bind(PreparedStatement p,Object... values)throws SQLException {
  for(int i=0;i<values.length;i++){Object v=values[i];if(v instanceof LocalDate d)p.setDate(i+1,java.sql.Date.valueOf(d));else if(v instanceof Boolean b)p.setBoolean(i+1,b);else p.setObject(i+1,v);}
 }
 public static List<Map<String,Object>> query(Connection c,String sql,Object... values)throws SQLException {
  try(var p=c.prepareStatement(sql)){bind(p,values);try(var r=p.executeQuery()){var rows=new ArrayList<Map<String,Object>>();var meta=r.getMetaData();while(r.next()){var row=new LinkedHashMap<String,Object>();for(int i=1;i<=meta.getColumnCount();i++){Object v=r.getObject(i);if(v instanceof java.sql.Date || v instanceof Timestamp)v=v.toString();row.put(meta.getColumnLabel(i),v);}rows.add(row);}return rows;}}
 }
 public static int execute(Connection c,String sql,Object... values)throws SQLException {try(var p=c.prepareStatement(sql)){bind(p,values);return p.executeUpdate();}}
 public static int insert(Connection c,String table,String key,Map<String,Object> row)throws SQLException {
  String sql="INSERT INTO "+table+"("+String.join(",",row.keySet())+") VALUES("+String.join(",",Collections.nCopies(row.size(),"?"))+")";
  try(var p=c.prepareStatement(sql,new String[]{key})){bind(p,row.values().toArray());p.executeUpdate();try(var r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("No se obtuvo la clave creada");return r.getInt(1);}}
 }
 public static long scalar(Connection c,String sql,Object... values)throws SQLException {var rows=query(c,sql,values);return rows.isEmpty()?0:((Number)rows.get(0).values().iterator().next()).longValue();}
 public static void audit(Connection c,int user,String action,String entity,String key)throws SQLException {execute(c,"INSERT INTO Auditoria(id_usuario,accion,entidad,referencia) VALUES(?,?,?,?)",user,action,entity,key);}
}
