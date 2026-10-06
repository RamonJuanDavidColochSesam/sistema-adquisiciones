package com.adquisiciones.servicio;
import com.adquisiciones.conexion.ConexionManager;
import com.adquisiciones.dao.SqlDAO;
import jakarta.ws.rs.*;
import java.math.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** Operaciones económicas completas; bloqueo y rollback en una sola conexión. */
public final class AdquisicionServicio {
 @FunctionalInterface private interface Work<T>{T run(Connection c)throws SQLException;}
 private static <T>T transaction(Work<T> work)throws SQLException{
  try(var c=ConexionManager.getInstancia().getConexionSqlServer()){
   c.setAutoCommit(false);c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
   try{T result=work.run(c);c.commit();return result;}catch(Exception e){c.rollback();throw e;}
  }
 }
 public Map<String,Object> ordenes(int page,int size,Map<String,Object> user)throws SQLException{
  page=Math.max(1,page);size=Math.max(1,Math.min(100,size));
  List<Object> params=new ArrayList<>();String where="1=1";
  if("AdminProveedor".equals(user.get("rol"))){where="EXISTS(SELECT 1 FROM Pedido p JOIN ProveedorArticulo pa ON pa.id_articulo=p.id_articulo WHERE p.id_orden=o.id_orden AND pa.id_proveedor=?)";params.add(user.get("idProveedor"));}
  try(var c=ConexionManager.getInstancia().getConexionSqlServer()){
   long total=SqlDAO.scalar(c,"SELECT COUNT(*) FROM OrdenCompra o WHERE "+where,params.toArray());
   params.add((page-1)*size);params.add(size);
   var rows=SqlDAO.query(c,"SELECT o.*,t.nombre AS tipo,s.nombre AS subtipo,CASE WHEN a.id_adjudicacion IS NOT NULL THEN 'Adjudicada' WHEN o.fecha_creacion>CAST(GETDATE() AS DATE) THEN 'Programada' WHEN o.fecha_limite_oferta<CAST(GETDATE() AS DATE) THEN 'Vencida' ELSE 'Abierta' END AS estado,(SELECT COUNT(*) FROM Pedido p WHERE p.id_orden=o.id_orden) AS pedidos FROM OrdenCompra o JOIN TipoOrden t ON t.id_tipo=o.id_tipo LEFT JOIN SubtipoOrden s ON s.id_subtipo=o.id_subtipo LEFT JOIN Adjudicacion a ON a.id_orden=o.id_orden WHERE "+where+" ORDER BY o.id_orden DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",params.toArray());
   return Map.of("items",rows,"total",total,"page",page,"pageSize",size);
  }
 }
 public Map<String,Object> orden(int id,Map<String,Object> user)throws SQLException{try(var c=ConexionManager.getInstancia().getConexionSqlServer()){return detalle(c,id,user);}}
 private static Map<String,Object> base(Connection c,int id,boolean lock)throws SQLException{
  var rows=SqlDAO.query(c,"SELECT * FROM OrdenCompra"+(lock?" WITH(UPDLOCK,HOLDLOCK)":"")+" WHERE id_orden=?",id);
  if(rows.isEmpty())throw new NotFoundException();return rows.get(0);
 }
 private static Map<String,Object> detalle(Connection c,int id,Map<String,Object> user)throws SQLException{
  var order=base(c,id,false);
  List<Object> params=new ArrayList<>(List.of(id));String restriction="";
  if("AdminProveedor".equals(user.get("rol"))){restriction=" AND EXISTS(SELECT 1 FROM ProveedorArticulo pa WHERE pa.id_articulo=p.id_articulo AND pa.id_proveedor=?)";params.add(user.get("idProveedor"));}
  var pedidos=SqlDAO.query(c,"SELECT p.*,a.nombre AS articulo,d.nombre AS departamento,s.codigo_sucursal AS sucursal FROM Pedido p JOIN Articulo a ON a.id_articulo=p.id_articulo JOIN Departamento d ON d.id_departamento=p.id_departamento JOIN Sucursal s ON s.id_sucursal=d.id_sucursal WHERE p.id_orden=?"+restriction+" ORDER BY p.id_pedido",params.toArray());
  if("AdminProveedor".equals(user.get("rol"))&&pedidos.isEmpty())throw new ForbiddenException();
  params=new ArrayList<>(List.of(id));restriction="";
  if("AdminProveedor".equals(user.get("rol"))){restriction=" AND f.id_proveedor=?";params.add(user.get("idProveedor"));}
  var offers=SqlDAO.query(c,"SELECT f.*,v.nombre_comercial AS proveedor,p.id_articulo,a.nombre AS articulo,CASE WHEN d.id_oferta IS NULL THEN 0 ELSE 1 END AS ganadora FROM Oferta f JOIN Pedido p ON p.id_pedido=f.id_pedido JOIN Articulo a ON a.id_articulo=p.id_articulo JOIN Proveedor v ON v.id_proveedor=f.id_proveedor LEFT JOIN DetalleAdjudicacion d ON d.id_oferta=f.id_oferta WHERE p.id_orden=?"+restriction+" ORDER BY f.id_pedido,f.precio_unitario,f.id_oferta",params.toArray());
  for(var offer:offers){
   var same=offers.stream().filter(o->Objects.equals(o.get("id_pedido"),offer.get("id_pedido"))).toList();
   BigDecimal minimum=same.stream().map(o->new BigDecimal(o.get("precio_unitario").toString())).min(BigDecimal::compareTo).orElseThrow();
   BigDecimal price=new BigDecimal(offer.get("precio_unitario").toString());
   offer.put("precio_minimo",minimum);offer.put("diferencia_precio",price.subtract(minimum));offer.put("mejor_precio",price.compareTo(minimum)==0);
   offer.put("empate",price.compareTo(minimum)==0&&same.stream().filter(o->new BigDecimal(o.get("precio_unitario").toString()).compareTo(minimum)==0).count()>1);
   offer.put("cantidad_solicitada",pedidos.stream().filter(p->Objects.equals(p.get("id_pedido"),offer.get("id_pedido"))).findFirst().orElseThrow().get("cantidad"));
  }
  order.put("pedidos",pedidos);order.put("ofertas",offers);
  var awards=SqlDAO.query(c,"SELECT * FROM Adjudicacion WHERE id_orden=?",id);
  order.put("adjudicacion",awards.isEmpty()?null:awards.get(0));
  order.put("estado",!awards.isEmpty()?"Adjudicada":LocalDate.parse(order.get("fecha_creacion").toString()).isAfter(LocalDate.now())?"Programada":LocalDate.parse(order.get("fecha_limite_oferta").toString()).isBefore(LocalDate.now())?"Vencida":"Abierta");
  return order;
 }
 public Map<String,Object> guardarOrden(Integer id,Map<String,Object> body,Map<String,Object> user)throws SQLException{
  if(body==null)throw new IllegalArgumentException("Debe enviar la orden");
  String description=text(body,"descripcion",200),notes=optionalText(body,"observaciones",300);LocalDate created=date(body,"fecha_creacion"),deadline=date(body,"fecha_limite_oferta");
  if(deadline.isBefore(created))throw new IllegalArgumentException("El límite de ofertas no puede preceder la creación");
  int type=integer(body.get("id_tipo"));Integer subtype=body.get("id_subtipo")==null?null:integer(body.get("id_subtipo"));
  if(!(body.get("pedidoIds") instanceof List<?> ids)||ids.isEmpty())throw new IllegalArgumentException("Seleccione al menos un pedido");
  Set<Integer> pedidos=new LinkedHashSet<>();for(Object raw:ids)if(!pedidos.add(integer(raw)))throw new IllegalArgumentException("La orden contiene pedidos duplicados");
  return transaction(c->{
   if(id!=null){base(c,id,true);mutable(c,id);}
   if(SqlDAO.scalar(c,"SELECT COUNT(*) FROM TipoOrden WHERE id_tipo=?",type)!=1)throw new IllegalArgumentException("Tipo de orden no válido");
   if(subtype!=null&&SqlDAO.scalar(c,"SELECT COUNT(*) FROM SubtipoOrden WHERE id_subtipo=? AND id_tipo=?",subtype,type)!=1)throw new IllegalArgumentException("El subtipo no pertenece al tipo");
   for(int pedido:pedidos){
    var rows=SqlDAO.query(c,"SELECT p.*,a.estado AS estado_articulo FROM Pedido p WITH(UPDLOCK,HOLDLOCK) JOIN Articulo a ON a.id_articulo=p.id_articulo WHERE p.id_pedido=?",pedido);
    if(rows.isEmpty())throw new IllegalArgumentException("Pedido inexistente: "+pedido);var row=rows.get(0);
    if(row.get("id_orden")!=null&&!Objects.equals(row.get("id_orden"),id))throw new IllegalArgumentException("Un pedido ya pertenece a otra orden");
    if(!"Activo".equals(row.get("estado_articulo")))throw new IllegalArgumentException("La orden contiene un artículo inactivo");
    if(created.isBefore(LocalDate.parse(row.get("fecha_solicitud").toString())))throw new IllegalArgumentException("La orden no puede crearse antes de sus solicitudes");
   }
   var values=new LinkedHashMap<String,Object>();values.put("descripcion",description);values.put("fecha_creacion",created);values.put("fecha_limite_oferta",deadline);values.put("id_tipo",type);values.put("id_subtipo",subtype);values.put("observaciones",notes);
   int order=id==null?SqlDAO.insert(c,"OrdenCompra","id_orden",values):id;
   if(id!=null){
    SqlDAO.execute(c,"UPDATE Pedido SET id_orden=NULL WHERE id_orden=?",id);
    SqlDAO.execute(c,"UPDATE OrdenCompra SET descripcion=?,fecha_creacion=?,fecha_limite_oferta=?,id_tipo=?,id_subtipo=?,observaciones=? WHERE id_orden=?",description,created,deadline,type,subtype,notes,id);
   }
   for(int pedido:pedidos)SqlDAO.execute(c,"UPDATE Pedido SET id_orden=? WHERE id_pedido=?",order,pedido);
   SqlDAO.audit(c,(Integer)user.get("idUsuario"),id==null?"CREAR":"ACTUALIZAR","OrdenCompra",String.valueOf(order));return detalle(c,order,user);
  });
 }
 public void borrarOrden(int id,Map<String,Object> user)throws SQLException{
  transaction(c->{base(c,id,true);mutable(c,id);SqlDAO.execute(c,"UPDATE Pedido SET id_orden=NULL WHERE id_orden=?",id);SqlDAO.execute(c,"DELETE FROM OrdenCompra WHERE id_orden=?",id);SqlDAO.audit(c,(Integer)user.get("idUsuario"),"BORRAR","OrdenCompra",String.valueOf(id));return null;});
 }
 private static void mutable(Connection c,int id)throws SQLException {
  if(SqlDAO.scalar(c,"SELECT COUNT(*) FROM Adjudicacion WHERE id_orden=?",id)>0||SqlDAO.scalar(c,"SELECT COUNT(*) FROM Oferta f JOIN Pedido p ON p.id_pedido=f.id_pedido WHERE p.id_orden=?",id)>0)
   throw new IllegalArgumentException("La orden ya tiene ofertas o adjudicación; conserve su historial");
 }
 public Map<String,Object> adjudicaciones()throws SQLException{
  try(var c=ConexionManager.getInstancia().getConexionSqlServer()){return Map.of("items",SqlDAO.query(c,"SELECT a.*,o.descripcion,SUM(CAST(d.cantidad_final AS DECIMAL(18,2))*d.precio_acordado) AS monto FROM Adjudicacion a JOIN OrdenCompra o ON o.id_orden=a.id_orden JOIN DetalleAdjudicacion d ON d.id_adjudicacion=a.id_adjudicacion GROUP BY a.id_adjudicacion,a.id_orden,a.fecha_resolucion,a.observaciones,o.descripcion ORDER BY a.fecha_resolucion DESC"));}
 }
 public Map<String,Object> adjudicacion(int id)throws SQLException{try(var c=ConexionManager.getInstancia().getConexionSqlServer()){return award(c,id);}}
 private static Map<String,Object> award(Connection c,int id)throws SQLException{
  var rows=SqlDAO.query(c,"SELECT * FROM Adjudicacion WHERE id_adjudicacion=?",id);if(rows.isEmpty())throw new NotFoundException();var result=rows.get(0);
  result.put("detalles",SqlDAO.query(c,"SELECT d.*,f.id_proveedor,v.nombre_comercial AS proveedor,a.nombre AS articulo FROM DetalleAdjudicacion d JOIN Oferta f ON f.id_oferta=d.id_oferta JOIN Proveedor v ON v.id_proveedor=f.id_proveedor JOIN Pedido p ON p.id_pedido=d.id_pedido JOIN Articulo a ON a.id_articulo=p.id_articulo WHERE d.id_adjudicacion=? ORDER BY d.id_pedido",id));return result;
 }
 public Map<String,Object> guardarAdjudicacion(Integer id,Map<String,Object> body,Map<String,Object> user)throws SQLException{
  if(body==null)throw new IllegalArgumentException("Debe enviar la adjudicación");
  String notes=optionalText(body,"observaciones",300);int order=integer(body.get("id_orden"));LocalDate resolution=date(body,"fecha_resolucion");
  if(resolution.isAfter(LocalDate.now()))throw new IllegalArgumentException("La resolución no puede estar en el futuro");
  if(!(body.get("detalles") instanceof List<?> details)||details.isEmpty())throw new IllegalArgumentException("Seleccione una oferta ganadora para cada pedido");
  return transaction(c->{
   var orderRow=base(c,order,true);
   if(resolution.isBefore(LocalDate.parse(orderRow.get("fecha_creacion").toString())))throw new IllegalArgumentException("La resolución no puede preceder la creación");
   var existing=SqlDAO.query(c,"SELECT id_adjudicacion FROM Adjudicacion WHERE id_orden=?",order);
   if(id==null&&!existing.isEmpty())throw new IllegalArgumentException("La orden ya está adjudicada");
   if(id!=null){var old=award(c,id);if(!Objects.equals(old.get("id_orden"),order))throw new IllegalArgumentException("No cambie la orden de una adjudicación");sinEvaluaciones(c,id);}
   Set<Integer> included=new HashSet<>();List<Map<String,Object>> validated=new ArrayList<>();
   for(Object raw:details){
    if(!(raw instanceof Map<?,?> row))throw new IllegalArgumentException("Detalle no válido");
    int pedido=integer(row.get("id_pedido")),offer=integer(row.get("id_oferta")),qty=integer(row.get("cantidad_final"));BigDecimal price=money(row.get("precio_acordado"));
    if(!included.add(pedido))throw new IllegalArgumentException("Pedido duplicado en la adjudicación");
    var match=SqlDAO.query(c,"SELECT p.cantidad,f.fecha_oferta,f.precio_unitario,v.estado FROM Pedido p JOIN Oferta f ON f.id_pedido=p.id_pedido JOIN Proveedor v ON v.id_proveedor=f.id_proveedor WHERE p.id_pedido=? AND p.id_orden=? AND f.id_oferta=?",pedido,order,offer);
    if(match.isEmpty())throw new IllegalArgumentException("La oferta elegida no corresponde al pedido de esta orden");
    var winning=match.get(0);
    if(price.compareTo(new BigDecimal(winning.get("precio_unitario").toString()))!=0)throw new IllegalArgumentException("El precio acordado debe coincidir con la oferta seleccionada");
    if(qty>((Number)winning.get("cantidad")).intValue())throw new IllegalArgumentException("La cantidad adjudicada excede la solicitada");
    if(resolution.isBefore(LocalDate.parse(winning.get("fecha_oferta").toString())))throw new IllegalArgumentException("La resolución no puede preceder la oferta ganadora");
    if(!"Activo".equals(winning.get("estado")))throw new IllegalArgumentException("El proveedor ganador no está activo");
    validated.add(Map.of("id_pedido",pedido,"id_oferta",offer,"cantidad_final",qty,"precio_acordado",price));
   }
   if(included.size()!=SqlDAO.scalar(c,"SELECT COUNT(*) FROM Pedido WHERE id_orden=?",order))throw new IllegalArgumentException("Debe adjudicar todos los pedidos de la orden");
   int awardId;
   var awardValues=new LinkedHashMap<String,Object>();awardValues.put("id_orden",order);awardValues.put("fecha_resolucion",resolution);awardValues.put("observaciones",notes);
   if(id==null)awardId=SqlDAO.insert(c,"Adjudicacion","id_adjudicacion",awardValues);
   else {awardId=id;SqlDAO.execute(c,"DELETE FROM DetalleAdjudicacion WHERE id_adjudicacion=?",id);SqlDAO.execute(c,"UPDATE Adjudicacion SET fecha_resolucion=?,observaciones=? WHERE id_adjudicacion=?",resolution,notes,id);}
   for(var detail:validated)SqlDAO.execute(c,"INSERT INTO DetalleAdjudicacion(id_adjudicacion,id_pedido,id_oferta,cantidad_final,precio_acordado) VALUES(?,?,?,?,?)",awardId,detail.get("id_pedido"),detail.get("id_oferta"),detail.get("cantidad_final"),detail.get("precio_acordado"));
   SqlDAO.audit(c,(Integer)user.get("idUsuario"),id==null?"ADJUDICAR":"ACTUALIZAR","Adjudicacion",String.valueOf(awardId));return award(c,awardId);
  });
 }
 public void borrarAdjudicacion(int id,Map<String,Object> user)throws SQLException{
  transaction(c->{var a=award(c,id);base(c,((Number)a.get("id_orden")).intValue(),true);sinEvaluaciones(c,id);SqlDAO.execute(c,"DELETE FROM DetalleAdjudicacion WHERE id_adjudicacion=?",id);SqlDAO.execute(c,"DELETE FROM Adjudicacion WHERE id_adjudicacion=?",id);SqlDAO.audit(c,(Integer)user.get("idUsuario"),"REVERTIR","Adjudicacion",String.valueOf(id));return null;});
 }
 private static void sinEvaluaciones(Connection c,int id)throws SQLException {if(SqlDAO.scalar(c,"SELECT COUNT(*) FROM EvaluacionProveedor e JOIN DetalleAdjudicacion d ON d.id_detalle_adjudicacion=e.id_detalle_adjudicacion WHERE d.id_adjudicacion=?",id)>0)throw new IllegalArgumentException("La adjudicación tiene evaluaciones; conserve su historial");}
 private static String optionalText(Map<String,Object> b,String key,int max){Object value=b.get(key);if(value==null)return null;if(!(value instanceof String s)||s.length()>max)throw new IllegalArgumentException("Campo no válido: "+key);return s.isBlank()?null:s.trim();}
 private static int integer(Object v){try{int n=new BigDecimal(String.valueOf(v)).intValueExact();if(n<=0)throw new NumberFormatException();return n;}catch(RuntimeException e){throw new IllegalArgumentException("Identificador o cantidad no válido");}}
 private static LocalDate date(Map<String,Object> b,String key){try{return LocalDate.parse(String.valueOf(b.get(key)));}catch(RuntimeException e){throw new IllegalArgumentException("Fecha no válida: "+key);}}
 private static String text(Map<String,Object> b,String key,int max){Object v=b.get(key);if(!(v instanceof String s)||s.isBlank()||s.trim().length()>max)throw new IllegalArgumentException("Campo no válido: "+key);return s.trim();}
 private static BigDecimal money(Object v){try{BigDecimal n=new BigDecimal(String.valueOf(v)).setScale(2,RoundingMode.UNNECESSARY);if(n.signum()<=0||n.compareTo(new BigDecimal("99999999.99"))>0)throw new NumberFormatException();return n;}catch(RuntimeException e){throw new IllegalArgumentException("Precio acordado no válido");}}
}
