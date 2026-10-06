package com.adquisiciones.servicio;
import com.adquisiciones.conexion.ConexionManager;
import com.adquisiciones.dao.SqlDAO;
import jakarta.ws.rs.ForbiddenException;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public final class ReportesServicio {
 public static final List<Map<String,Object>> CATALOGO=List.of(
  Map.of("id",1,"nombre","Historial de compra de artículos","parametros",List.of("articulo","desde","hasta")),
  Map.of("id",2,"nombre","Top 5 proveedores por monto adjudicado","parametros",List.of("desde","hasta")),
  Map.of("id",3,"nombre","Comparación de ofertas y ganadora por orden","parametros",List.of("orden")),
  Map.of("id",4,"nombre","Pedidos sin asignación","parametros",List.of()),
  Map.of("id",5,"nombre","Órdenes abiertas para ofertas (VIEW)","parametros",List.of()),
  Map.of("id",6,"nombre","Gasto por departamento, sucursal y año","parametros",List.of("sucursal","anio")),
  Map.of("id",7,"nombre","Promedio de días entre creación y adjudicación","parametros",List.of("desde","hasta")),
  Map.of("id",8,"nombre","Evolución de precios ofrecidos por artículo y proveedor","parametros",List.of("articulo","proveedor","desde","hasta"))
 );
 public static final Map<Integer,String> SQL=Map.of(
  1,"SELECT ar.codigo_articulo,ar.nombre AS articulo,v.nombre_comercial AS proveedor,a.fecha_resolucion,d.cantidad_final,d.precio_acordado,CAST(d.cantidad_final*d.precio_acordado AS DECIMAL(18,2)) AS monto FROM DetalleAdjudicacion d JOIN Adjudicacion a ON a.id_adjudicacion=d.id_adjudicacion JOIN Pedido p ON p.id_pedido=d.id_pedido JOIN Articulo ar ON ar.id_articulo=p.id_articulo JOIN Oferta f ON f.id_oferta=d.id_oferta JOIN Proveedor v ON v.id_proveedor=f.id_proveedor WHERE (?=0 OR ar.id_articulo=?) AND a.fecha_resolucion BETWEEN ? AND ? %SCOPE% ORDER BY ar.nombre,a.fecha_resolucion,v.nombre_comercial",
  2,"SELECT TOP(5) v.codigo_proveedor,v.nombre_comercial AS proveedor,SUM(CAST(d.cantidad_final*d.precio_acordado AS DECIMAL(18,2))) AS monto_adjudicado,COUNT(DISTINCT a.id_orden) AS ordenes FROM DetalleAdjudicacion d JOIN Adjudicacion a ON a.id_adjudicacion=d.id_adjudicacion JOIN Oferta f ON f.id_oferta=d.id_oferta JOIN Proveedor v ON v.id_proveedor=f.id_proveedor WHERE a.fecha_resolucion BETWEEN ? AND ? %SCOPE% GROUP BY v.codigo_proveedor,v.nombre_comercial ORDER BY monto_adjudicado DESC,v.codigo_proveedor",
  3,"SELECT p.id_pedido,ar.nombre AS articulo,p.cantidad,v.nombre_comercial AS proveedor,f.precio_unitario,f.fecha_oferta,CAST(CASE WHEN d.id_oferta IS NULL THEN 0 ELSE 1 END AS BIT) AS ganadora,d.cantidad_final,d.precio_acordado FROM Oferta f JOIN Pedido p ON p.id_pedido=f.id_pedido JOIN Articulo ar ON ar.id_articulo=p.id_articulo JOIN Proveedor v ON v.id_proveedor=f.id_proveedor LEFT JOIN DetalleAdjudicacion d ON d.id_oferta=f.id_oferta WHERE p.id_orden=? %SCOPE% ORDER BY p.id_pedido,f.precio_unitario,f.id_oferta",
  4,"SELECT p.id_pedido,s.codigo_sucursal,d.nombre AS departamento,a.nombre AS articulo,p.cantidad,p.fecha_solicitud,p.fecha_necesaria FROM Pedido p JOIN Departamento d ON d.id_departamento=p.id_departamento JOIN Sucursal s ON s.id_sucursal=d.id_sucursal JOIN Articulo a ON a.id_articulo=p.id_articulo WHERE p.id_orden IS NULL %SCOPE% ORDER BY p.fecha_necesaria,p.id_pedido",
  5,"SELECT o.* FROM vw_OrdenesAbiertas o WHERE 1=1 %SCOPE% ORDER BY o.fecha_limite_oferta,o.id_orden",
  6,"SELECT s.codigo_sucursal,s.ciudad,de.nombre AS departamento,SUM(CAST(d.cantidad_final*d.precio_acordado AS DECIMAL(18,2))) AS gasto FROM DetalleAdjudicacion d JOIN Adjudicacion a ON a.id_adjudicacion=d.id_adjudicacion JOIN Pedido p ON p.id_pedido=d.id_pedido JOIN Departamento de ON de.id_departamento=p.id_departamento JOIN Sucursal s ON s.id_sucursal=de.id_sucursal WHERE (?=0 OR s.id_sucursal=?) AND a.fecha_resolucion>=? AND a.fecha_resolucion<? GROUP BY s.codigo_sucursal,s.ciudad,de.nombre ORDER BY s.codigo_sucursal,de.nombre",
  7,"SELECT COUNT(*) AS ordenes_adjudicadas,AVG(CAST(DATEDIFF(DAY,o.fecha_creacion,a.fecha_resolucion) AS DECIMAL(10,2))) AS promedio_dias FROM Adjudicacion a JOIN OrdenCompra o ON o.id_orden=a.id_orden WHERE a.fecha_resolucion BETWEEN ? AND ?",
  8,"SELECT ar.codigo_articulo,ar.nombre AS articulo,v.nombre_comercial AS proveedor,f.fecha_oferta,f.precio_unitario,p.id_orden FROM Oferta f JOIN Pedido p ON p.id_pedido=f.id_pedido JOIN Articulo ar ON ar.id_articulo=p.id_articulo JOIN Proveedor v ON v.id_proveedor=f.id_proveedor WHERE (?=0 OR ar.id_articulo=?) AND (?=0 OR f.id_proveedor=?) AND f.fecha_oferta BETWEEN ? AND ? %SCOPE% ORDER BY ar.nombre,v.nombre_comercial,f.fecha_oferta,f.id_oferta"
 );
 public Map<String,Object> ejecutar(int report,int article,int supplier,int order,int branch,int year,LocalDate from,LocalDate to,Map<String,Object> user)throws SQLException {
  if(!SQL.containsKey(report))throw new IllegalArgumentException("Informe no válido");
  if(article<0||supplier<0||branch<0||order<0||year<1900||year>2100)throw new IllegalArgumentException("Filtro no válido");
  if(to.isBefore(from))throw new IllegalArgumentException("El rango de fechas no es válido");
  if(report==3&&order==0)throw new IllegalArgumentException("Seleccione una orden");
  boolean own="AdminProveedor".equals(user.get("rol"));
  if(own&&Set.of(4,6,7).contains(report))throw new ForbiddenException();
  List<Object> params=new ArrayList<>();
  switch(report){
   case 1->Collections.addAll(params,article,article,from,to);
   case 2,7->Collections.addAll(params,from,to);
   case 3->params.add(order);
   case 6->Collections.addAll(params,branch,branch,LocalDate.of(year,1,1),LocalDate.of(year+1,1,1));
   case 8->Collections.addAll(params,article,article,supplier,supplier,from,to);
   default->{}
  }
  String scope="";
  if(own){params.add(user.get("idProveedor"));scope=report==5?" AND EXISTS(SELECT 1 FROM Pedido p JOIN ProveedorArticulo pa ON pa.id_articulo=p.id_articulo WHERE p.id_orden=o.id_orden AND pa.id_proveedor=?)":" AND f.id_proveedor=?";}
  String query=SQL.get(report).replace("%SCOPE%",scope);
  try(var c=ConexionManager.getInstancia().getConexionSqlServer()){
   var rows=SqlDAO.query(c,query,params.toArray());
   return Map.of("id",report,"nombre",CATALOGO.get(report-1).get("nombre"),"items",rows,"total",rows.size(),"moneda","GTQ");
  }
 }
 public Map<String,Object> dashboard(Map<String,Object> user)throws SQLException {
  boolean own="AdminProveedor".equals(user.get("rol"));Object provider=user.get("idProveedor");
  try(var c=ConexionManager.getInstancia().getConexionSqlServer()){
   var result=new LinkedHashMap<String,Object>();
   result.put("articulos",SqlDAO.scalar(c,own?"SELECT COUNT(*) FROM ProveedorArticulo WHERE id_proveedor=?":"SELECT COUNT(*) FROM Articulo",own?new Object[]{provider}:new Object[]{}));
   result.put("proveedores",own?1:SqlDAO.scalar(c,"SELECT COUNT(*) FROM Proveedor"));
   result.put("pedidos",SqlDAO.scalar(c,own?"SELECT COUNT(*) FROM Pedido p JOIN ProveedorArticulo pa ON pa.id_articulo=p.id_articulo WHERE pa.id_proveedor=?":"SELECT COUNT(*) FROM Pedido",own?new Object[]{provider}:new Object[]{}));
   result.put("ordenesAbiertas",SqlDAO.scalar(c,own?"SELECT COUNT(*) FROM vw_OrdenesAbiertas o WHERE EXISTS(SELECT 1 FROM Pedido p JOIN ProveedorArticulo pa ON pa.id_articulo=p.id_articulo WHERE p.id_orden=o.id_orden AND pa.id_proveedor=?)":"SELECT COUNT(*) FROM vw_OrdenesAbiertas",own?new Object[]{provider}:new Object[]{}));
   result.put("ofertas",SqlDAO.scalar(c,own?"SELECT COUNT(*) FROM Oferta WHERE id_proveedor=?":"SELECT COUNT(*) FROM Oferta",own?new Object[]{provider}:new Object[]{}));
   result.put("adjudicaciones",SqlDAO.scalar(c,own?"SELECT COUNT(DISTINCT d.id_adjudicacion) FROM DetalleAdjudicacion d JOIN Oferta f ON f.id_oferta=d.id_oferta WHERE f.id_proveedor=?":"SELECT COUNT(*) FROM Adjudicacion",own?new Object[]{provider}:new Object[]{}));
   result.put("montoAdjudicado",SqlDAO.query(c,"SELECT COALESCE(SUM(CAST(d.cantidad_final*d.precio_acordado AS DECIMAL(18,2))),0) AS monto FROM DetalleAdjudicacion d JOIN Oferta f ON f.id_oferta=d.id_oferta"+(own?" WHERE f.id_proveedor=?":""),own?new Object[]{provider}:new Object[]{}).get(0).get("monto"));
   var topParams=new ArrayList<Object>(List.of(LocalDate.now().minusYears(1),LocalDate.now()));if(own)topParams.add(provider);
   result.put("topProveedores",SqlDAO.query(c,SQL.get(2).replace("%SCOPE%",own?" AND f.id_proveedor=?":""),topParams.toArray()));
   result.put("moneda","GTQ");result.put("alcance",own?"Su proveedor":"Institucional");return result;
  }
 }
}
