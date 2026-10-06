package com.adquisiciones.util;

import com.adquisiciones.conexion.ConexionManager;
import java.math.BigDecimal;
import java.nio.file.*;
import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;

/** Datos académicos ficticios. Repetible por claves naturales, sin borrar información. */
public final class SemillaDemo {
    private static final String[] ROLES = {"AdminSistema", "GestorCompras", "AdminProveedor", "Auditor"};
    private static final String[] USERS = {"admin", "gestor", "proveedor", "auditor"};
    private static final String[] SCREENS = {"Sucursales", "Departamentos", "Articulos", "Proveedores", "ProveedorArticulos", "Relaciones", "TiposOrden", "Pedidos", "Ordenes", "Ofertas", "Adjudicaciones", "Evaluaciones", "Usuarios", "Roles", "Pantallas", "Permisos", "Dashboard", "Reportes", "Conexion", "Auditoria"};
    private static final String[] ARTICLES = {
        "Resma papel carta", "Resma papel oficio", "Bolígrafo azul", "Bolígrafo negro", "Lápiz grafito", "Marcador permanente", "Marcador pizarra", "Carpeta oficio", "Archivador palanca", "Grapadora",
        "Caja grapas", "Perforadora", "Tijera oficina", "Cinta adhesiva", "Pegamento barra", "Cuaderno universitario", "Sobre manila", "Notas adhesivas", "Regla 30 cm", "Corrector líquido",
        "Computadora escritorio", "Laptop administrativa", "Monitor 24 pulgadas", "Teclado USB", "Mouse óptico", "Impresora láser", "Tóner negro", "Disco SSD 512 GB", "Memoria USB 64 GB", "UPS 1000 VA",
        "Router inalámbrico", "Switch 24 puertos", "Cable red Cat6", "Webcam HD", "Proyector multimedia", "Silla ergonómica", "Escritorio ejecutivo", "Archivador metálico", "Mesa reuniones", "Estantería metálica",
        "Alcohol desinfectante", "Jabón líquido", "Papel higiénico", "Toalla de papel", "Escoba", "Trapeador", "Bolsa basura", "Detergente", "Guantes limpieza", "Extintor ABC"
    };
    private static final String[] SUPPLIERS = {"Papelería del Valle", "Suministros Verapaz", "Tecnología Maya", "Equipos del Norte", "Ofimática Central", "Distribuidora Quetzal", "Muebles Los Pinos", "Servicios del Altiplano", "Comercial Tikal", "Insumos del Sur", "Soluciones Digitales GT", "Redes y Sistemas", "Limpieza Integral", "Papeles y Más", "Distribuidora El Roble", "Equipo Profesional", "Comercial La Ceiba", "Abastecimientos Motagua", "Innovación Regional", "Suministros del Lago"};
    private static final String[] RUBROS = {"Papelería", "Tecnología", "Mobiliario", "Limpieza", "Seguridad"};

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args.length == 0 ? "." : args[0]);
        Properties access = new Properties();
        Path file = root.resolve("config/demo-access.properties");
        if (Files.exists(file)) try (var in = Files.newInputStream(file)) { access.load(in); }
        SecureRandom random = new SecureRandom();
        if (!access.containsKey("_seedDate")) access.setProperty("_seedDate", LocalDate.now().toString());
        for (String user : USERS) if (!access.containsKey(user)) {
            byte[] bytes = new byte[18]; random.nextBytes(bytes);
            access.setProperty(user, Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
        }
        try (var out = Files.newOutputStream(file)) { access.store(out, "Accesos DEMO locales; no versionar"); }
        var manager = ConexionManager.getInstancia();
        try {
            for (String db : List.of("sqlserver", "postgres")) {
                try (Connection c = db.equals("sqlserver") ? manager.getConexionSqlServer() : manager.getConexionPostgres()) {
                    c.setAutoCommit(false);
                    try { seed(c, access); c.commit(); System.out.println(db + ": semilla confirmada; mínimos 5/10/50/20/100"); }
                    catch (Exception e) { c.rollback(); throw e; }
                }
            }
        } finally { manager.close(); }
    }

    private static void seed(Connection c, Properties access) throws SQLException {
        int[] roles = new int[4];
        for (int i=0; i<4; i++) roles[i]=ensure(c,"Rol","id_rol","nombre_rol",ROLES[i],"nombre_rol,descripcion",ROLES[i],"Rol académico "+ROLES[i]);
        for (String screen : SCREENS) {
            int id=ensure(c,"Pantalla","id_pantalla","nombre_pantalla",screen,"nombre_pantalla,descripcion",screen,"Módulo "+screen);
            for(int i=0;i<4;i++) {
                boolean read=i!=2 || Set.of("Articulos","ProveedorArticulos","Ordenes","Pedidos","Ofertas","Dashboard","Reportes").contains(screen);
                boolean write=i==0 || i==1 && Set.of("Pedidos","Ordenes","Ofertas","Adjudicaciones","Evaluaciones").contains(screen)
                    || i==2 && Set.of("ProveedorArticulos","Ofertas").contains(screen);
                if (scalar(c,"SELECT COUNT(*) FROM Permiso WHERE id_rol=? AND id_pantalla=?",roles[i],id)==0)
                    execute(c,"INSERT INTO Permiso(id_rol,id_pantalla,permite_crear,permite_leer,permite_actualizar,permite_borrar) VALUES(?,?,?,?,?,?)",roles[i],id,write,read,write,write);
            }
        }
        int[] branches=new int[5],departments=new int[10],articles=new int[50],providers=new int[20];
        String[] cities={"Guatemala","Salamá","Cobán","Quetzaltenango","Flores"}, regions={"Guatemala","Baja Verapaz","Alta Verapaz","Quetzaltenango","Petén"};
        for(int i=0;i<5;i++) {
            branches[i]=ensure(c,"Sucursal","id_sucursal","codigo_sucursal","GC"+(i+1),"codigo_sucursal,direccion,ciudad,region,telefono","GC"+(i+1),"Zona 1, oficina "+(i+1),cities[i],regions[i],"2200-10"+String.format("%02d",i));
            for(int j=0;j<2;j++) {
                String phone="2200-"+(2000+i*10+j);
                if(scalar(c,"SELECT COUNT(*) FROM SucursalTelefono WHERE id_sucursal=? AND telefono=?",branches[i],phone)==0)execute(c,"INSERT INTO SucursalTelefono(id_sucursal,telefono) VALUES(?,?)",branches[i],phone);
                String dep=j==0?"Administración":"Operaciones";
                Integer id=find(c,"SELECT id_departamento FROM Departamento WHERE id_sucursal=? AND nombre=?",branches[i],dep);
                departments[i*2+j]=id!=null?id:insert(c,"Departamento","id_departamento","id_sucursal,nombre,descripcion",branches[i],dep,dep+" de "+cities[i]);
            }
        }
        for(int i=0;i<50;i++)articles[i]=ensure(c,"Articulo","id_articulo","codigo_articulo",String.format("ART-%03d",i+1),"codigo_articulo,nombre,descripcion",String.format("ART-%03d",i+1),ARTICLES[i],"Unidad de compra institucional");
        for(int i=0;i<20;i++) {
            providers[i]=ensure(c,"Proveedor","id_proveedor","codigo_proveedor",String.format("PRV-%03d",i+1),"codigo_proveedor,nombre_comercial,direccion,telefono,categoria",String.format("PRV-%03d",i+1),SUPPLIERS[i],"Local "+(i+1)+", zona comercial","2300-"+(1000+i),RUBROS[i%5]);
            for(String rubro:List.of(RUBROS[i%5],RUBROS[(i+1)%5]))if(scalar(c,"SELECT COUNT(*) FROM ProveedorRubro WHERE id_proveedor=? AND rubro=?",providers[i],rubro)==0)execute(c,"INSERT INTO ProveedorRubro(id_proveedor,rubro) VALUES(?,?)",providers[i],rubro);
            for(int a=0;a<50;a++)if(scalar(c,"SELECT COUNT(*) FROM ProveedorArticulo WHERE id_proveedor=? AND id_articulo=?",providers[i],articles[a])==0)execute(c,"INSERT INTO ProveedorArticulo(id_proveedor,id_articulo,precio) VALUES(?,?,?)",providers[i],articles[a],price(a,i));
        }
        for(int i=0;i<4;i++)if(find(c,"SELECT id_usuario FROM Usuario WHERE nombre_usuario=?",USERS[i])==null)
            insert(c,"Usuario","id_usuario","nombre_usuario,contrasena_hash,nombre_completo,email,id_rol,id_proveedor,estado",USERS[i],BCrypt.hashpw(access.getProperty(USERS[i]),BCrypt.gensalt(12)),"Demo "+ROLES[i],USERS[i]+"@demo.guatecompras.local",roles[i],i==2?providers[0]:null,"Activo");
        int large=ensure(c,"TipoOrden","id_tipo","nombre","Grande","nombre","Grande"),small=ensure(c,"TipoOrden","id_tipo","nombre","Chica","nombre","Chica");
        int[] types={large,small},subtypes=new int[2];
        for(int i=0;i<2;i++) {
            Integer id=find(c,"SELECT id_subtipo FROM SubtipoOrden WHERE id_tipo=? AND nombre=?",types[i],"Normal");
            subtypes[i]=id==null?insert(c,"SubtipoOrden","id_subtipo","id_tipo,nombre",types[i],"Normal"):id;
            if(find(c,"SELECT id_subtipo FROM SubtipoOrden WHERE id_tipo=? AND nombre=?",types[i],"Urgente")==null)insert(c,"SubtipoOrden","id_subtipo","id_tipo,nombre",types[i],"Urgente");
        }
        LocalDate today=LocalDate.parse(access.getProperty("_seedDate"));
        for(int o=0;o<20;o++) {
            boolean awarded=o<10;LocalDate created=today.minusDays(awarded?45:7),deadline=awarded?created.plusDays(10):today.plusDays(10);
            String description=String.format("DEMO Orden %02d - abastecimiento institucional",o+1);
            int order=ensure(c,"OrdenCompra","id_orden","descripcion",description,"descripcion,fecha_creacion,fecha_limite_oferta,id_tipo,id_subtipo",description,created,deadline,types[o%2],subtypes[o%2]);
            int[] pedidos=new int[3],winning=new int[3];
            for(int j=0;j<3;j++) {
                int art=(o*3+j)%50,dep=(o+j)%10;
                Integer p=find(c,"SELECT id_pedido FROM Pedido WHERE id_orden=? AND id_articulo=? AND id_departamento=?",order,articles[art],departments[dep]);
                pedidos[j]=p==null?insert(c,"Pedido","id_pedido","id_departamento,id_orden,id_articulo,cantidad,fecha_solicitud,fecha_necesaria",departments[dep],order,articles[art],5+j*5,created.minusDays(2),created.plusDays(20)):p;
                for(int f=0;f<3;f++) {
                    int provider=providers[(o+f)%20];LocalDate offered=created.plusDays(1+f);
                    Integer offer=find(c,"SELECT id_oferta FROM Oferta WHERE id_pedido=? AND id_proveedor=? AND fecha_oferta=?",pedidos[j],provider,offered);
                    int offerId=offer==null?insert(c,"Oferta","id_oferta","id_proveedor,id_pedido,precio_unitario,fecha_oferta",provider,pedidos[j],price(art,(o+f)%20),offered):offer;
                    if(f==0)winning[j]=offerId;
                }
            }
            if(awarded) {
                Integer existing=find(c,"SELECT id_adjudicacion FROM Adjudicacion WHERE id_orden=?",order);
                int adjud=existing==null?insert(c,"Adjudicacion","id_adjudicacion","id_orden,fecha_resolucion",order,created.plusDays(12)):existing;
                for(int j=0;j<3;j++)if(find(c,"SELECT id_detalle_adjudicacion FROM DetalleAdjudicacion WHERE id_adjudicacion=? AND id_pedido=?",adjud,pedidos[j])==null)
                    insert(c,"DetalleAdjudicacion","id_detalle_adjudicacion","id_adjudicacion,id_pedido,id_oferta,cantidad_final,precio_acordado",adjud,pedidos[j],winning[j],5+j*5,price((o*3+j)%50,o%20));
            }
        }
        for(int i=0;i<40;i++) {
            LocalDate request=today.minusDays(3+i%3);int dep=departments[i%10],art=articles[i%50];
            if(find(c,"SELECT id_pedido FROM Pedido WHERE id_orden IS NULL AND id_departamento=? AND id_articulo=? AND fecha_solicitud=?",dep,art,request)==null)
                insert(c,"Pedido","id_pedido","id_departamento,id_articulo,cantidad,fecha_solicitud,fecha_necesaria",dep,art,10+i%10,request,today.plusDays(15));
        }
        for(int i=0;i<5;i++)if(find(c,"SELECT id_relacion_comercial FROM RelacionComercial WHERE id_proveedor_a=? AND id_proveedor_b=?",providers[i],providers[i+10])==null)
            insert(c,"RelacionComercial","id_relacion_comercial","id_proveedor_a,id_proveedor_b",providers[i],providers[i+10]);
    }
    private static BigDecimal price(int article,int provider) { return BigDecimal.valueOf((article<20?5+article*2:article<35?250+(article-20)*175:25+(article-35)*30)+(provider%5)*3L).setScale(2); }
    private static int ensure(Connection c,String table,String id,String key,Object value,String columns,Object... values) throws SQLException {
        Integer found=find(c,"SELECT "+id+" FROM "+table+" WHERE "+key+"=?",value);return found==null?insert(c,table,id,columns,values):found;
    }
    private static int insert(Connection c,String table,String id,String columns,Object... values) throws SQLException {
        String markers=String.join(",",Collections.nCopies(values.length,"?"));
        try(var ps=c.prepareStatement("INSERT INTO "+table+"("+columns+") VALUES("+markers+")",new String[]{id})) {
            bind(ps,values);ps.executeUpdate();try(var keys=ps.getGeneratedKeys()){if(!keys.next())throw new SQLException("No se generó clave en "+table);return keys.getInt(1);}
        }
    }
    private static Integer find(Connection c,String sql,Object... values) throws SQLException {try(var ps=c.prepareStatement(sql)){bind(ps,values);try(var r=ps.executeQuery()){return r.next()?r.getInt(1):null;}}}
    private static int scalar(Connection c,String sql,Object... values) throws SQLException {Integer n=find(c,sql,values);return n==null?0:n;}
    private static void execute(Connection c,String sql,Object... values) throws SQLException {try(var ps=c.prepareStatement(sql)){bind(ps,values);ps.executeUpdate();}}
    private static void bind(PreparedStatement ps,Object... values) throws SQLException {for(int i=0;i<values.length;i++){Object v=values[i];if(v instanceof LocalDate d)ps.setDate(i+1,java.sql.Date.valueOf(d));else if(v instanceof Boolean b)ps.setBoolean(i+1,b);else ps.setObject(i+1,v);}}
}
