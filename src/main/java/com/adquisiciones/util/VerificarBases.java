package com.adquisiciones.util;
import com.adquisiciones.conexion.ConexionManager;
import java.sql.*;
import java.util.*;

/** Pruebas reales de esquema/semilla. Los casos negativos se revierten. */
public final class VerificarBases {
 public static void main(String[] args) throws Exception {
  var manager=ConexionManager.getInstancia();
  try {
   for(String db:List.of("sqlserver","postgres")) {
    try(var c=db.equals("sqlserver")?manager.getConexionSqlServer():manager.getConexionPostgres()) {
     for(var e:Map.of("Sucursal",5,"Departamento",10,"Articulo",50,"Proveedor",20,"Pedido",100,"Rol",4,"Usuario",4).entrySet()) {
      long n=count(c,"SELECT COUNT(*) FROM "+e.getKey());
      require(n>=e.getValue(),db+" mínimo "+e.getKey());System.out.println("PASS "+db+" "+e.getKey()+"="+n);
     }
     // La vista refleja el uso real (fechas vencidas y adjudicaciones), así que se
     // comprueba que coincida con su definición en vez de exigir un conteo fijo.
     long vista=count(c,"SELECT COUNT(*) FROM vw_OrdenesAbiertas");
     long esperada=count(c,"SELECT COUNT(*) FROM OrdenCompra o WHERE NOT EXISTS(SELECT 1 FROM Adjudicacion a WHERE a.id_orden=o.id_orden) AND o.fecha_creacion<=CURRENT_DATE AND o.fecha_limite_oferta>=CURRENT_DATE");
     require(vista==esperada,db+" vista órdenes abiertas");System.out.println("PASS "+db+" vw_OrdenesAbiertas="+vista);
     require(count(c,"SELECT COUNT(*) FROM Oferta f JOIN Pedido p ON p.id_pedido=f.id_pedido WHERE p.id_orden IS NULL")==0,db+" ofertas sólo en pedidos con orden");
     c.setAutoCommit(false);
     try {
      long before=count(c,"SELECT COUNT(*) FROM Oferta");
      int pedido=(int)count(c,"SELECT MIN(id_pedido) FROM Pedido WHERE id_orden IS NOT NULL");
      int provider=(int)count(c,"SELECT MIN(id_proveedor) FROM Proveedor");
      expectFailure(c,"INSERT INTO Oferta(id_proveedor,id_pedido,precio_unitario,fecha_oferta) VALUES("+provider+","+pedido+",-1,'2020-01-01')","precio no positivo");
      expectFailure(c,"INSERT INTO Oferta(id_proveedor,id_pedido,precio_unitario,fecha_oferta) VALUES("+provider+","+pedido+",15,'2020-01-01')","oferta fuera de plazo");
      int unassigned=(int)count(c,"SELECT MIN(id_pedido) FROM Pedido WHERE id_orden IS NULL");
      expectFailure(c,"INSERT INTO Oferta(id_proveedor,id_pedido,precio_unitario,fecha_oferta) VALUES("+provider+","+unassigned+",15,CURRENT_TIMESTAMP)","pedido no asignado");
      int award=(int)count(c,"SELECT MIN(id_adjudicacion) FROM Adjudicacion");
      int offer=(int)count(c,"SELECT MAX(id_oferta) FROM Oferta");
      expectFailure(c,"INSERT INTO DetalleAdjudicacion(id_adjudicacion,id_pedido,id_oferta,cantidad_final,precio_acordado) VALUES("+award+","+unassigned+","+offer+",1,10)","detalle incompatible");
      int winning=(int)count(c,"SELECT MIN(id_oferta) FROM DetalleAdjudicacion");
      expectFailure(c,"UPDATE Oferta SET precio_unitario=precio_unitario+1 WHERE id_oferta="+winning,"oferta ganadora inmutable");
      expectFailure(c,"UPDATE Pedido SET id_orden=NULL WHERE id_pedido="+pedido,"pedido con ofertas no reasignable");
      expectFailure(c,"UPDATE Adjudicacion SET fecha_resolucion='2000-01-01' WHERE id_adjudicacion="+award,"resolución anterior a ofertas");
      expectFailure(c,"UPDATE OrdenCompra SET fecha_limite_oferta='2000-01-01' WHERE id_orden=(SELECT id_orden FROM Pedido WHERE id_pedido="+pedido+")","padre no invalida plazo de ofertas");
      expectFailure(c,"DELETE FROM ProveedorArticulo WHERE id_proveedor=(SELECT id_proveedor FROM Oferta WHERE id_oferta="+winning+") AND id_articulo=(SELECT id_articulo FROM Pedido WHERE id_pedido=(SELECT id_pedido FROM Oferta WHERE id_oferta="+winning+"))","catálogo conserva historial");
      require(before==count(c,"SELECT COUNT(*) FROM Oferta"),"sin efectos persistentes de pruebas negativas");
      c.rollback();
     } finally {c.setAutoCommit(true);}
     System.out.println("PASS "+db+" vista, mínimos, integridad y rollback");
    }
   }
  } finally {manager.close();}
 }
 private static long count(Connection c,String sql)throws SQLException {try(var s=c.createStatement();var r=s.executeQuery(sql)){if(!r.next())throw new SQLException("La consulta de verificación no devolvió filas");return r.getLong(1);}}
 private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException("FAIL "+message);}
 private static void expectFailure(Connection c,String sql,String label)throws SQLException {
  boolean failed=false;
  try(var s=c.createStatement()){s.executeUpdate(sql);}catch(SQLException expected){failed=true;}
  finally {if(!c.getAutoCommit())c.rollback();}
  require(failed,label);System.out.println("PASS rechazo: "+label);
 }
}
