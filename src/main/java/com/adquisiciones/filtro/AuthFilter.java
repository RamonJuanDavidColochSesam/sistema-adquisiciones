package com.adquisiciones.filtro;
import com.adquisiciones.servicio.SeguridadServicio;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

public final class AuthFilter implements Filter {
 private static final Map<String,String> SCREENS=Map.ofEntries(
  Map.entry("sucursales","Sucursales"),Map.entry("departamentos","Departamentos"),Map.entry("articulos","Articulos"),Map.entry("proveedores","Proveedores"),Map.entry("proveedorarticulos","ProveedorArticulos"),
  Map.entry("relaciones","Relaciones"),Map.entry("tiposorden","TiposOrden"),Map.entry("subtiposorden","TiposOrden"),Map.entry("pedidos","Pedidos"),Map.entry("ordenes","Ordenes"),Map.entry("ofertas","Ofertas"),Map.entry("adjudicaciones","Adjudicaciones"),
  Map.entry("evaluaciones","Evaluaciones"),Map.entry("usuarios","Usuarios"),Map.entry("roles","Roles"),Map.entry("pantallas","Pantallas"),Map.entry("permisos","Permisos"),Map.entry("dashboard","Dashboard"),
  Map.entry("reportes","Reportes"),Map.entry("conexion","Conexion"),Map.entry("auditoria","Auditoria"),Map.entry("telefonos","Sucursales"),Map.entry("rubros","Proveedores"));
 private final SeguridadServicio security=new SeguridadServicio();
 @Override public void doFilter(ServletRequest req,ServletResponse res,FilterChain chain)throws IOException,ServletException {
  var request=(HttpServletRequest)req;var response=(HttpServletResponse)res;
  response.setHeader("X-Content-Type-Options","nosniff");response.setHeader("Cache-Control","no-store");
  response.setHeader("X-Frame-Options","SAMEORIGIN");response.setCharacterEncoding("UTF-8");
  String path=request.getRequestURI().substring(request.getContextPath().length());
  if(path.equals("/api/login")&&request.getMethod().equals("POST")){chain.doFilter(req,res);return;}
  var session=request.getSession(false);
  if(session==null||!(session.getAttribute("idUsuario") instanceof Integer id)){fail(response,401,"No autenticado");return;}
  try {
   var user=security.usuarioActivo(id);
   if(user==null){session.invalidate();fail(response,401,"Sesión no válida");return;}
   request.setAttribute("usuario",user);
   session.setAttribute("idRol",user.get("idRol"));session.setAttribute("rol",user.get("rol"));session.setAttribute("idProveedor",user.get("idProveedor"));
   if(!Set.of("GET","HEAD","OPTIONS").contains(request.getMethod()) && (!(session.getAttribute("csrf") instanceof String token) || !Objects.equals(token,request.getHeader("X-CSRF-Token")))) {fail(response,403,"Token de seguridad no válido");return;}
   if(path.equals("/api/login/me")||path.equals("/api/login/logout")){chain.doFilter(req,res);return;}
   String[] parts=path.split("/");
   String module=parts.length>2?parts[2]:"";
   if(module.equals("gestion")&&parts.length>3)module=parts[3];
   if(module.equals("proveedores")&&parts.length>4&&parts[4].equals("articulos"))module="proveedorarticulos";
   String screen=SCREENS.get(module);
   if(screen==null||!security.permite((Integer)user.get("idRol"),screen,request.getMethod())){fail(response,403,"No tiene permiso para esta operación");return;}
   if("AdminProveedor".equals(user.get("rol"))&&path.startsWith("/api/proveedores/")) {
    if(parts.length<4||!Objects.equals(parts[3],String.valueOf(user.get("idProveedor")))){fail(response,403,"Solo puede gestionar su propio catálogo");return;}
   }
   chain.doFilter(req,res);
  } catch(java.sql.SQLException e){fail(response,503,"No se pudo verificar la autorización");}
 }
 private static void fail(HttpServletResponse response,int code,String message)throws IOException {
  response.setStatus(code);response.setContentType("application/json");response.getWriter().write("{\"error\":\""+message+"\"}");
 }
}
