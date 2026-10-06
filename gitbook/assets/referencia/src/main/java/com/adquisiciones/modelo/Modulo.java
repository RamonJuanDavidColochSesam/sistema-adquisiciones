package com.adquisiciones.modelo;
import java.util.*;

/** Esquemas explícitos: ningún nombre SQL procede del cliente. */
public record Modulo(String nombre,String tabla,List<String> claves,List<Campo> campos) {
 public record Campo(String nombre,String etiqueta,String tipo,boolean requerido,int maximo,String referencia) {}
 private static final Map<String,Modulo> MODULOS=new LinkedHashMap<>();
 private static Campo f(String name,String label,String type,boolean required,int max,String ref){return new Campo(name,label,type,required,max,ref);}
 private static Campo text(String n,String l,boolean r,int max){return f(n,l,"text",r,max,null);}
 private static Campo id(String n,String l,String ref,boolean required){return f(n,l,"number",required,0,ref);}
 private static Campo num(String n,String l){return f(n,l,"number",true,0,null);}
 private static Campo date(String n,String l){return f(n,l,"date",true,0,null);}
 private static Campo decimal(String n,String l){return f(n,l,"decimal",true,0,null);}
 private static Campo status(){return f("estado","Estado","status",true,10,null);}
 private static void add(String name,String table,String keys,Campo... fields){MODULOS.put(name,new Modulo(name,table,List.of(keys.split(",")),List.of(fields)));}
 static {
  add("sucursales","Sucursal","id_sucursal",text("codigo_sucursal","Código",true,10),text("direccion","Dirección",true,150),text("ciudad","Ciudad",true,60),text("region","Departamento geográfico",true,60),text("telefono","Teléfono principal",true,20));
  add("departamentos","Departamento","id_departamento",id("id_sucursal","Sucursal","sucursales",true),text("nombre","Nombre",true,60),text("descripcion","Descripción",false,200));
  add("articulos","Articulo","id_articulo",text("codigo_articulo","Código",true,20),text("nombre","Artículo",true,100),text("descripcion","Descripción",false,300),status());
  add("proveedores","Proveedor","id_proveedor",text("codigo_proveedor","Código",true,20),text("nombre_comercial","Nombre comercial",true,100),text("direccion","Dirección",true,150),text("telefono","Teléfono",true,20),text("categoria","Rubro principal",true,80),status());
  add("proveedorarticulos","ProveedorArticulo","id_proveedor,id_articulo",id("id_proveedor","Proveedor","proveedores",true),id("id_articulo","Artículo","articulos",true),decimal("precio","Precio de catálogo (Q)"));
  add("telefonos","SucursalTelefono","id_sucursal,telefono",id("id_sucursal","Sucursal","sucursales",true),text("telefono","Teléfono",true,20));
  add("rubros","ProveedorRubro","id_proveedor,rubro",id("id_proveedor","Proveedor","proveedores",true),text("rubro","Rubro",true,80));
  add("relaciones","RelacionComercial","id_relacion_comercial",id("id_proveedor_a","Primer proveedor","proveedores",true),id("id_proveedor_b","Segundo proveedor","proveedores",true));
  add("tiposorden","TipoOrden","id_tipo",f("nombre","Tipo de orden","tipoorden",true,20,null));
  add("subtiposorden","SubtipoOrden","id_subtipo",id("id_tipo","Tipo","tiposorden",true),f("nombre","Subtipo","subtipoorden",true,20,null));
  add("roles","Rol","id_rol",text("nombre_rol","Nombre del rol",true,40),text("descripcion","Descripción",false,200));
  add("pantallas","Pantalla","id_pantalla",text("nombre_pantalla","Pantalla",true,60),text("descripcion","Descripción",false,200));
  add("permisos","Permiso","id_rol,id_pantalla",id("id_rol","Rol","roles",true),id("id_pantalla","Pantalla","pantallas",true),f("permite_crear","Crear","boolean",true,0,null),f("permite_leer","Leer","boolean",true,0,null),f("permite_actualizar","Actualizar","boolean",true,0,null),f("permite_borrar","Borrar","boolean",true,0,null));
  add("usuarios","Usuario","id_usuario",text("nombre_usuario","Usuario",true,30),f("contrasena","Contraseña nueva","password",false,72,null),text("nombre_completo","Nombre completo",true,100),f("email","Correo electrónico","email",true,100,null),id("id_rol","Rol","roles",true),id("id_proveedor","Proveedor asociado","proveedores",false),status());
  add("pedidos","Pedido","id_pedido",id("id_departamento","Departamento","departamentos",true),id("id_articulo","Artículo","articulos",true),num("cantidad","Cantidad"),date("fecha_solicitud","Fecha solicitud"),date("fecha_necesaria","Fecha necesaria"));
  add("ofertas","Oferta","id_oferta",id("id_proveedor","Proveedor","proveedores",true),id("id_pedido","Pedido","pedidos",true),decimal("precio_unitario","Precio unitario (Q)"),date("fecha_oferta","Fecha oferta"),text("observaciones","Observaciones",false,300));
  add("evaluaciones","EvaluacionProveedor","id_evaluacion",num("id_detalle_adjudicacion","Detalle adjudicado"),num("calificacion","Calificación (1–5)"),text("comentario","Comentario",false,300),date("fecha","Fecha evaluación"));
 }
 public static Modulo obtener(String name){Modulo result=MODULOS.get(name);if(result==null)throw new IllegalArgumentException("Módulo no válido");return result;}
 public List<String> columnas() {
  var cols=new ArrayList<>(claves);
  for(var f:campos)if(!f.tipo().equals("password")&&!cols.contains(f.nombre()))cols.add(f.nombre());
  if(nombre.equals("pedidos"))cols.add("id_orden");
  return cols;
 }
 public boolean identidad(){return claves.size()==1;}
}
