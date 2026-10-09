package com.adquisiciones.servicio;
import com.adquisiciones.conexion.ConexionManager;
import com.adquisiciones.dao.SqlDAO;
import com.adquisiciones.modelo.Modulo;
import jakarta.ws.rs.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;

public final class CatalogoServicio {
 public Map<String,Object> listar(Modulo m,int page,int size,String search,Map<String,Object> user)throws SQLException {
  page=Math.max(1,page);size=Math.min(100,Math.max(1,size));
  List<Object> params=new ArrayList<>();String where=scope(m,user,params);
  if(search!=null&&!search.isBlank()){
   if(search.length()>100)throw new IllegalArgumentException("La búsqueda es demasiado larga");
   var terms=new ArrayList<String>();
   for(var f:m.campos())if(Set.of("text","email","status").contains(f.tipo())){terms.add(f.nombre()+" LIKE ?");params.add("%"+search.trim()+"%");}
   if(!terms.isEmpty())where+=" AND ("+String.join(" OR ",terms)+")";
  }
  try(var c=ConexionManager.getInstancia().getConexionSqlServer()){
   long total=SqlDAO.scalar(c,"SELECT COUNT(*) FROM "+m.tabla()+" WHERE "+where,params.toArray());
   params.add((page-1)*size);params.add(size);
   var items=SqlDAO.query(c,"SELECT "+selectColumns(m)+" FROM "+readSource(m)+" WHERE "+where+" ORDER BY "+String.join(",",m.claves())+" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",params.toArray());
   items.forEach(row->key(m,row));return Map.of("items",items,"total",total,"page",page,"pageSize",size);
  }
 }
 private static String selectColumns(Modulo m){
  String columns=String.join(",",m.columnas());
  if(m.nombre().equals("proveedores"))columns+=",(SELECT AVG(CAST(e.calificacion AS DECIMAL(5,2))) FROM EvaluacionProveedor e JOIN DetalleAdjudicacion d ON d.id_detalle_adjudicacion=e.id_detalle_adjudicacion JOIN Oferta f ON f.id_oferta=d.id_oferta WHERE f.id_proveedor=Proveedor.id_proveedor) AS promedio_evaluacion";
  if(m.nombre().equals("ofertas"))columns+=",CASE WHEN EXISTS(SELECT 1 FROM DetalleAdjudicacion d WHERE d.id_oferta=Oferta.id_oferta) THEN 'Adjudicada' WHEN EXISTS(SELECT 1 FROM Pedido p JOIN OrdenCompra o ON o.id_orden=p.id_orden WHERE p.id_pedido=Oferta.id_pedido AND o.fecha_limite_oferta<CAST(GETDATE() AS DATE)) THEN 'Vencida' ELSE 'Vigente' END AS estado_oferta";
  if(m.nombre().equals("departamentos"))columns+=",codigo_sucursal";
  if(m.nombre().equals("proveedorarticulos"))columns+=",proveedor,articulo";
  if(m.nombre().equals("pedidos"))columns+=",departamento,sucursal,articulo,CASE WHEN EXISTS(SELECT 1 FROM vw_OrdenesAbiertas ab WHERE ab.id_orden=Pedido.id_orden) THEN 'Sí' ELSE 'No' END AS disponible_oferta";
  return columns;
 }
 private static String readSource(Modulo m){
  return switch(m.nombre()){
   case "departamentos"->"(SELECT d.*,s.codigo_sucursal FROM Departamento d JOIN Sucursal s ON s.id_sucursal=d.id_sucursal) Departamento";
   case "proveedorarticulos"->"(SELECT pa.*,v.nombre_comercial AS proveedor,a.nombre AS articulo FROM ProveedorArticulo pa JOIN Proveedor v ON v.id_proveedor=pa.id_proveedor JOIN Articulo a ON a.id_articulo=pa.id_articulo) ProveedorArticulo";
   case "pedidos"->"(SELECT p.*,d.nombre AS departamento,s.codigo_sucursal AS sucursal,a.nombre AS articulo FROM Pedido p JOIN Departamento d ON d.id_departamento=p.id_departamento JOIN Sucursal s ON s.id_sucursal=d.id_sucursal JOIN Articulo a ON a.id_articulo=p.id_articulo) Pedido";
   default->m.tabla();
  };
 }
 public Map<String,Object> obtener(Modulo m,String key,Map<String,Object> user)throws SQLException {
  try(var c=ConexionManager.getInstancia().getConexionSqlServer()){return obtener(c,m,key,user);}
 }
 private Map<String,Object> obtener(Connection c,Modulo m,String key,Map<String,Object> user)throws SQLException {
  List<Object> params=new ArrayList<>();String where=scope(m,user,params)+" AND "+keyWhere(m,key,params);
  var rows=SqlDAO.query(c,"SELECT "+selectColumns(m)+" FROM "+readSource(m)+" WHERE "+where,params.toArray());
  if(rows.isEmpty())throw new NotFoundException();var row=rows.get(0);key(m,row);return row;
 }
 /** Fila guardada e indicador de si se insertó (true) o solo se actualizó (false). */
 public record Guardado(Map<String,Object> fila,boolean creado) {}
 public Guardado guardar(Modulo m,String key,Map<String,Object> body,Map<String,Object> user)throws SQLException {
  boolean creating=key==null;var row=validar(m,body,creating,user);
  try(var c=ConexionManager.getInstancia().getConexionSqlServer()){
   c.setAutoCommit(false);c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
   try {
    Map<String,Object> original=creating?null:obtener(c,m,key,user);
    reglas(c,m,row,original,user);
    if(creating&&m.nombre().equals("ofertas")){
     long previa=ofertaPrevia(c,row); // un proveedor conserva una sola oferta por pedido
     if(previa>0){key=String.valueOf(previa);original=obtener(c,m,key,user);creating=false;} // se actualiza la existente en lugar de duplicarla
    }
    if(creating){
     if(m.identidad()){int id=SqlDAO.insert(c,m.tabla(),m.claves().get(0),row);key=String.valueOf(id);}
     else {SqlDAO.execute(c,"INSERT INTO "+m.tabla()+"("+String.join(",",row.keySet())+") VALUES("+String.join(",",Collections.nCopies(row.size(),"?"))+")",row.values().toArray());key=String.join("~",m.claves().stream().map(k->String.valueOf(row.get(k))).toList());}
    }else{
     for(String k:m.claves())if(row.containsKey(k)&&!String.valueOf(row.get(k)).equals(String.valueOf(original.get(k))))throw new IllegalArgumentException("Las claves del registro no se pueden cambiar");
     m.claves().forEach(row::remove);
     if(row.isEmpty())throw new IllegalArgumentException("No hay campos para actualizar");
     List<Object> params=new ArrayList<>(row.values());
     String where=keyWhere(m,key,params);
     SqlDAO.execute(c,"UPDATE "+m.tabla()+" SET "+String.join(",",row.keySet().stream().map(k->k+"=?").toList())+" WHERE "+where,params.toArray());
    }
    SqlDAO.audit(c,(Integer)user.get("idUsuario"),creating?"CREAR":"ACTUALIZAR",m.tabla(),key);
    var result=obtener(c,m,key,user);c.commit();return new Guardado(result,creating);
   }catch(Exception e){c.rollback();throw e;}
  }
 }
 public void borrar(Modulo m,String key,Map<String,Object> user)throws SQLException {
  try(var c=ConexionManager.getInstancia().getConexionSqlServer()){
   c.setAutoCommit(false);c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
   try{
    var original=obtener(c,m,key,user);
    if(m.nombre().equals("usuarios")){
     if(Objects.equals(original.get("id_usuario"),user.get("idUsuario")))throw new IllegalArgumentException("No puede borrar su propia cuenta");
     protegerAdmin(c,original,null);
    }
    if(m.nombre().equals("roles")&&Set.of("AdminSistema","GestorCompras","AdminProveedor","Auditor").contains(String.valueOf(original.get("nombre_rol"))))throw new IllegalArgumentException("Los cuatro roles académicos deben conservarse");
    if(m.nombre().equals("pedidos")&&original.get("id_orden")!=null)throw new IllegalArgumentException("El pedido ya pertenece a una orden");
    if(m.nombre().equals("ofertas"))ofertaModificable(c,original);
    if(m.nombre().equals("proveedorarticulos")&&SqlDAO.scalar(c,"SELECT COUNT(*) FROM Oferta f JOIN Pedido p ON p.id_pedido=f.id_pedido WHERE f.id_proveedor=? AND p.id_articulo=?",original.get("id_proveedor"),original.get("id_articulo"))>0)throw new IllegalArgumentException("El catálogo ya tiene ofertas históricas; conserve su relación");
    List<Object> params=new ArrayList<>();String where=keyWhere(m,key,params);
    SqlDAO.execute(c,"DELETE FROM "+m.tabla()+" WHERE "+where,params.toArray());
    SqlDAO.audit(c,(Integer)user.get("idUsuario"),"BORRAR",m.tabla(),key);c.commit();
   }catch(Exception e){c.rollback();throw e;}
  }
 }
 private static Map<String,Object> validar(Modulo m,Map<String,Object> body,boolean creating,Map<String,Object> user){
  if(body==null)throw new IllegalArgumentException("Debe enviar los datos del formulario");
  Set<String> allowed=new HashSet<>();m.campos().forEach(f->allowed.add(f.nombre()));
  for(String k:body.keySet())if(!allowed.contains(k)&&!m.claves().contains(k)&&!k.equals("_key"))throw new IllegalArgumentException("Campo no permitido: "+k);
  var row=new LinkedHashMap<String,Object>();
  for(var f:m.campos()){
   Object v=body.get(f.nombre());
   if(f.nombre().equals("id_proveedor")&&"AdminProveedor".equals(user.get("rol"))) {
    if(v!=null&&!String.valueOf(v).equals(String.valueOf(user.get("idProveedor"))))throw new ForbiddenException();
    v=user.get("idProveedor");
   }
   if(v==null||v instanceof String s&&s.isBlank()){
    if(f.requerido())throw new IllegalArgumentException("Falta "+f.etiqueta());
    if(!f.tipo().equals("password"))row.put(f.nombre(),null);
    continue;
   }
   try {
    switch(f.tipo()){
     case "number"->{int n=new BigDecimal(v.toString()).intValueExact();if(n<=0)throw new IllegalArgumentException();row.put(f.nombre(),n);}
     case "decimal"->{BigDecimal n=new BigDecimal(v.toString()).setScale(2,java.math.RoundingMode.UNNECESSARY);if(n.signum()<=0||n.compareTo(new BigDecimal("99999999.99"))>0)throw new IllegalArgumentException();row.put(f.nombre(),n);}
     case "boolean"->{if(!(v instanceof Boolean))throw new IllegalArgumentException();row.put(f.nombre(),v);}
     case "date"->row.put(f.nombre(),LocalDate.parse(v.toString()));
     default->{
      String value=v.toString().trim();if(value.length()>f.maximo()||value.contains("~"))throw new IllegalArgumentException();
      if(f.tipo().equals("status")&&!Set.of("Activo","Inactivo").contains(value))throw new IllegalArgumentException();
      if(f.tipo().equals("tipoorden")&&!Set.of("Grande","Chica").contains(value))throw new IllegalArgumentException();
      if(f.tipo().equals("subtipoorden")&&!Set.of("Urgente","Normal").contains(value))throw new IllegalArgumentException();
      if(f.tipo().equals("email")&&!value.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))throw new IllegalArgumentException();
      if(f.tipo().equals("password")){if(value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length<12||value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new IllegalArgumentException();row.put("contrasena_hash",BCrypt.hashpw(value,BCrypt.gensalt(12)));}
      else row.put(f.nombre(),value);
     }
    }
   }catch(RuntimeException invalid){if(invalid instanceof ForbiddenException)throw invalid;throw new IllegalArgumentException("Valor no válido: "+f.etiqueta());}
  }
  if(m.nombre().equals("usuarios")&&creating&&!row.containsKey("contrasena_hash"))throw new IllegalArgumentException("Debe establecer una contraseña de 12 a 72 bytes");
  return row;
 }
 private static void reglas(Connection c,Modulo m,Map<String,Object> row,Map<String,Object> original,Map<String,Object> user)throws SQLException {
  switch(m.nombre()){
   case "usuarios"->{
    var role=SqlDAO.query(c,"SELECT nombre_rol FROM Rol WHERE id_rol=?",row.get("id_rol"));
    if(role.isEmpty())throw new IllegalArgumentException("Rol no válido");
    boolean adminProveedor="AdminProveedor".equals(role.get(0).get("nombre_rol"));
    if(adminProveedor&&row.get("id_proveedor")==null)throw new IllegalArgumentException("El administrador de proveedor requiere proveedor asociado");
    if(!adminProveedor&&row.get("id_proveedor")!=null)throw new IllegalArgumentException("Sólo el administrador de proveedor puede llevar proveedor asociado");
    if(original!=null){
     if(Objects.equals(original.get("id_usuario"),user.get("idUsuario"))&&(!"Activo".equals(row.get("estado"))||!Objects.equals(original.get("id_rol"),row.get("id_rol"))))throw new IllegalArgumentException("No puede desactivar su cuenta ni cambiar su propio rol");
     protegerAdmin(c,original,row);
    }
   }
   case "roles"->{if(original!=null&&Set.of("AdminSistema","GestorCompras","AdminProveedor","Auditor").contains(String.valueOf(original.get("nombre_rol")))&&!Objects.equals(original.get("nombre_rol"),row.get("nombre_rol")))throw new IllegalArgumentException("Conserve los nombres de los roles académicos");}
   case "relaciones"->{int a=(Integer)row.get("id_proveedor_a"),b=(Integer)row.get("id_proveedor_b");if(a==b)throw new IllegalArgumentException("Seleccione proveedores distintos");row.put("id_proveedor_a",Math.min(a,b));row.put("id_proveedor_b",Math.max(a,b));}
   case "pedidos"->{
    if(original!=null&&original.get("id_orden")!=null)throw new IllegalArgumentException("El pedido asignado se gestiona desde su orden");
    if(((LocalDate)row.get("fecha_necesaria")).isBefore((LocalDate)row.get("fecha_solicitud")))throw new IllegalArgumentException("La fecha necesaria no puede preceder a la solicitud");
    if(SqlDAO.scalar(c,"SELECT COUNT(*) FROM Articulo WHERE id_articulo=? AND estado='Activo'",row.get("id_articulo"))!=1)throw new IllegalArgumentException("El artículo no está activo");
   }
   case "ofertas"->{
    if(original!=null)ofertaModificable(c,original);
    ofertaModificable(c,row);
    if(SqlDAO.scalar(c,"SELECT COUNT(*) FROM Proveedor WHERE id_proveedor=? AND estado='Activo'",row.get("id_proveedor"))!=1)throw new IllegalArgumentException("El proveedor no está activo");
    if(SqlDAO.scalar(c,"SELECT COUNT(*) FROM Pedido p JOIN Articulo a ON a.id_articulo=p.id_articulo WHERE p.id_pedido=? AND a.estado='Activo'",row.get("id_pedido"))!=1)throw new IllegalArgumentException("El artículo no está activo");
   }
   case "evaluaciones"->{int n=(Integer)row.get("calificacion");if(n>5)throw new IllegalArgumentException("La calificación debe estar entre 1 y 5");
    if(SqlDAO.scalar(c,"SELECT COUNT(*) FROM DetalleAdjudicacion d JOIN Adjudicacion a ON a.id_adjudicacion=d.id_adjudicacion WHERE d.id_detalle_adjudicacion=? AND a.fecha_resolucion<=?",row.get("id_detalle_adjudicacion"),row.get("fecha"))!=1)throw new IllegalArgumentException("La evaluación debe referirse a una adjudicación resuelta");}
   case "subtiposorden"->{if(SqlDAO.scalar(c,"SELECT COUNT(*) FROM SubtipoOrden WHERE id_tipo=? AND nombre=?"+(original==null?"":" AND id_subtipo<>?"),original==null?new Object[]{row.get("id_tipo"),row.get("nombre")}:new Object[]{row.get("id_tipo"),row.get("nombre"),original.get("id_subtipo")})>0)throw new IllegalArgumentException("El subtipo ya existe en este tipo");}
   default->{}
  }
 }
 private static void protegerAdmin(Connection c,Map<String,Object> original,Map<String,Object> replacement)throws SQLException {
  if(SqlDAO.scalar(c,"SELECT COUNT(*) FROM Rol WHERE id_rol=? AND nombre_rol='AdminSistema'",original.get("id_rol"))==1&&"Activo".equals(original.get("estado")) &&
     (replacement==null||!"Activo".equals(replacement.get("estado"))||!Objects.equals(original.get("id_rol"),replacement.get("id_rol")))&&
     SqlDAO.scalar(c,"SELECT COUNT(*) FROM Usuario u JOIN Rol r ON r.id_rol=u.id_rol WHERE r.nombre_rol='AdminSistema' AND u.estado='Activo'")<=1)throw new IllegalArgumentException("Debe conservar al menos un administrador activo");
 }
 private static void ofertaModificable(Connection c,Map<String,Object> row)throws SQLException {
  if(SqlDAO.scalar(c,"SELECT COUNT(*) FROM Pedido p JOIN OrdenCompra o ON o.id_orden=p.id_orden WHERE p.id_pedido=? AND o.fecha_limite_oferta>=CAST(GETDATE() AS DATE) AND o.fecha_creacion<=CAST(GETDATE() AS DATE)",row.get("id_pedido"))!=1)throw new IllegalArgumentException("La orden no está abierta para recibir cambios de ofertas");
  if(SqlDAO.scalar(c,"SELECT COUNT(*) FROM Pedido p JOIN Adjudicacion a ON a.id_orden=p.id_orden WHERE p.id_pedido=?",row.get("id_pedido"))>0)throw new IllegalArgumentException("La orden ya está adjudicada; sus ofertas se conservan");
 }
 private static long ofertaPrevia(Connection c,Map<String,Object> row)throws SQLException {
  return SqlDAO.scalar(c,"SELECT ISNULL(MAX(id_oferta),0) FROM Oferta WHERE id_proveedor=? AND id_pedido=?",row.get("id_proveedor"),row.get("id_pedido"));
 }
 private static String scope(Modulo m,Map<String,Object> user,List<Object> params){
  if(!"AdminProveedor".equals(user.get("rol")))return "1=1";
  return switch(m.nombre()){
   case "proveedorarticulos","ofertas"->{params.add(user.get("idProveedor"));yield "id_proveedor=?";}
   case "articulos"->"estado='Activo'";
   case "pedidos"->{params.add(user.get("idProveedor"));yield "id_orden IN(SELECT id_orden FROM vw_OrdenesAbiertas) AND id_articulo IN(SELECT id_articulo FROM ProveedorArticulo WHERE id_proveedor=?)";}
   default->throw new ForbiddenException();
  };
 }
 private static String keyWhere(Modulo m,String key,List<Object> params){
  String[] parts=key.split("~",-1);if(parts.length!=m.claves().size())throw new IllegalArgumentException("Clave no válida");
  for(int i=0;i<parts.length;i++){
   if(m.claves().get(i).startsWith("id_")){try{int id=Integer.parseInt(parts[i]);if(id<=0)throw new NumberFormatException();params.add(id);}catch(NumberFormatException e){throw new IllegalArgumentException("Identificador no válido");}}
   else params.add(parts[i]);
  }
  return String.join(" AND ",m.claves().stream().map(k->k+"=?").toList());
 }
 private static void key(Modulo m,Map<String,Object> row){row.put("_key",String.join("~",m.claves().stream().map(k->String.valueOf(row.get(k))).toList()));}
}
