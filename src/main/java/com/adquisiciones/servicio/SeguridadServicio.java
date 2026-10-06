package com.adquisiciones.servicio;
import com.adquisiciones.conexion.ConexionManager;
import java.sql.*;
import java.util.*;

public final class SeguridadServicio {
 public Map<String,Object> usuarioActivo(int id) throws SQLException {
  try(var c=ConexionManager.getInstancia().getConexionSqlServer();var p=c.prepareStatement("SELECT u.id_usuario,u.nombre_usuario,u.nombre_completo,u.id_rol,r.nombre_rol,u.id_proveedor FROM Usuario u JOIN Rol r ON r.id_rol=u.id_rol WHERE u.id_usuario=? AND u.estado='Activo'")) {
   p.setInt(1,id);try(var r=p.executeQuery()) {
    if(!r.next())return null;
    Map<String,Object> user=new LinkedHashMap<>();
    user.put("idUsuario",r.getInt(1));user.put("nombreUsuario",r.getString(2));user.put("nombreCompleto",r.getString(3));user.put("idRol",r.getInt(4));user.put("rol",r.getString(5));
    int provider=r.getInt(6);user.put("idProveedor",r.wasNull()?null:provider);return user;
   }
  }
 }
 public boolean permite(int role,String screen,String method) throws SQLException {
  String column=switch(method){case "GET","HEAD"->"permite_leer";case "POST"->"permite_crear";case "PUT","PATCH"->"permite_actualizar";case "DELETE"->"permite_borrar";default->null;};
  if(column==null)return false;
  try(var c=ConexionManager.getInstancia().getConexionSqlServer();var p=c.prepareStatement("SELECT p."+column+" FROM Permiso p JOIN Pantalla s ON s.id_pantalla=p.id_pantalla WHERE p.id_rol=? AND s.nombre_pantalla=?")) {
   p.setInt(1,role);p.setString(2,screen);try(var r=p.executeQuery()){return r.next()&&r.getBoolean(1);}
  }
 }
 public List<Map<String,Object>> permisos(int role)throws SQLException {
  List<Map<String,Object>> list=new ArrayList<>();
  try(var c=ConexionManager.getInstancia().getConexionSqlServer();var p=c.prepareStatement("SELECT s.nombre_pantalla,p.permite_crear,p.permite_leer,p.permite_actualizar,p.permite_borrar FROM Permiso p JOIN Pantalla s ON s.id_pantalla=p.id_pantalla WHERE p.id_rol=? ORDER BY s.nombre_pantalla")) {
   p.setInt(1,role);try(var r=p.executeQuery()){while(r.next())list.add(Map.of("pantalla",r.getString(1),"crear",r.getBoolean(2),"leer",r.getBoolean(3),"actualizar",r.getBoolean(4),"borrar",r.getBoolean(5)));}
  }return list;
 }
}
