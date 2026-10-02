/*
 * schema.sql - Sistema de Gestión de Adquisiciones y Proveedores (SQL Server)
 *
 * Crea desde cero la estructura de la base de datos GuateCompras:
 * 18 tablas con sus PK, FK, UNIQUE, CHECK y DEFAULT, más 2 triggers de reglas de negocio.
 * Las definiciones provienen del DDL del proyecto (DDL SQL SERVER) y coinciden con el
 * Diccionario de Datos.
 *
 * Uso:
 *   1. Crear la base de datos vacía (una sola vez), por ejemplo:
 *          CREATE DATABASE GuateCompras;
 *   2. Ejecutar este script completo (SSMS o sqlcmd). Debe ejecutarse una sola vez
 *      sobre una base vacía: no borra ni recrea tablas existentes.
 *
 * El nombre de la base debe coincidir con databaseName= en sqlserver.url de db.properties.
 * Este archivo solo contiene estructura: no inserta datos (roles, usuarios, etc.).
 */

USE GuateCompras;
GO

CREATE TABLE Sucursal (
    id_sucursal      INT IDENTITY(1,1) PRIMARY KEY,
    codigo_sucursal  VARCHAR(10)  NOT NULL UNIQUE,
    direccion        VARCHAR(150) NOT NULL,
    ciudad           VARCHAR(60)  NOT NULL,
    region           VARCHAR(60)  NOT NULL,
    telefono         VARCHAR(20)  NOT NULL
);
GO

CREATE TABLE Departamento (
    id_departamento  INT IDENTITY(1,1) PRIMARY KEY,
    id_sucursal      INT          NOT NULL,
    nombre           VARCHAR(60)  NOT NULL,
    descripcion      VARCHAR(200) NULL,
    CONSTRAINT FK_Departamento_Sucursal
        FOREIGN KEY (id_sucursal) REFERENCES Sucursal(id_sucursal),
    CONSTRAINT UQ_Departamento_SucursalNombre
        UNIQUE (id_sucursal, nombre)
);
GO

CREATE TABLE Articulo (
    id_articulo     INT IDENTITY(1,1) PRIMARY KEY,
    codigo_articulo VARCHAR(20)  NOT NULL UNIQUE,
    nombre          VARCHAR(100) NOT NULL,
    descripcion     VARCHAR(300) NULL
);
GO

CREATE TABLE Proveedor (
    id_proveedor     INT IDENTITY(1,1) PRIMARY KEY,
    codigo_proveedor VARCHAR(20)  NOT NULL UNIQUE,
    nombre_comercial VARCHAR(100) NOT NULL,
    direccion        VARCHAR(150) NOT NULL,
    telefono         VARCHAR(20)  NOT NULL,
    categoria        VARCHAR(80)  NOT NULL
);
GO

CREATE TABLE ProveedorArticulo (
    id_proveedor INT           NOT NULL,
    id_articulo  INT           NOT NULL,
    precio       DECIMAL(10,2) NOT NULL,
    CONSTRAINT PK_ProveedorArticulo PRIMARY KEY (id_proveedor, id_articulo),
    CONSTRAINT FK_ProveedorArticulo_Proveedor
        FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor),
    CONSTRAINT FK_ProveedorArticulo_Articulo
        FOREIGN KEY (id_articulo) REFERENCES Articulo(id_articulo),
    CONSTRAINT CK_ProveedorArticulo_Precio CHECK (precio > 0)
);
GO

CREATE TABLE RelacionComercial (
    id_relacion_comercial INT IDENTITY(1,1) PRIMARY KEY,
    id_proveedor_a INT NOT NULL,
    id_proveedor_b INT NOT NULL,
    CONSTRAINT FK_RelacionComercial_ProveedorA
        FOREIGN KEY (id_proveedor_a) REFERENCES Proveedor(id_proveedor),
    CONSTRAINT FK_RelacionComercial_ProveedorB
        FOREIGN KEY (id_proveedor_b) REFERENCES Proveedor(id_proveedor),
    CONSTRAINT CK_RelacionComercial_Distintos CHECK (id_proveedor_a <> id_proveedor_b),
    CONSTRAINT UQ_RelacionComercial_Par UNIQUE (id_proveedor_a, id_proveedor_b)
);
GO

CREATE TABLE TipoOrden (
    id_tipo INT IDENTITY(1,1) PRIMARY KEY,
    nombre  VARCHAR(20) NOT NULL UNIQUE,
    CONSTRAINT CK_TipoOrden_Nombre CHECK (nombre IN ('Grande','Chica'))
);
GO

CREATE TABLE SubtipoOrden (
    id_subtipo INT IDENTITY(1,1) PRIMARY KEY,
    id_tipo    INT         NOT NULL,
    nombre     VARCHAR(20) NOT NULL,
    CONSTRAINT FK_SubtipoOrden_TipoOrden
        FOREIGN KEY (id_tipo) REFERENCES TipoOrden(id_tipo),
    CONSTRAINT CK_SubtipoOrden_Nombre CHECK (nombre IN ('Urgente','Normal'))
);
GO

CREATE TABLE OrdenCompra (
    id_orden            INT IDENTITY(1,1) PRIMARY KEY,
    descripcion         VARCHAR(200) NOT NULL,
    fecha_creacion      DATE NOT NULL,
    fecha_limite_oferta DATE NOT NULL,
    id_tipo             INT  NOT NULL,
    id_subtipo          INT  NULL,
    CONSTRAINT FK_OrdenCompra_TipoOrden
        FOREIGN KEY (id_tipo) REFERENCES TipoOrden(id_tipo),
    CONSTRAINT FK_OrdenCompra_SubtipoOrden
        FOREIGN KEY (id_subtipo) REFERENCES SubtipoOrden(id_subtipo),
    CONSTRAINT CK_OrdenCompra_Fechas CHECK (fecha_limite_oferta >= fecha_creacion)
);
GO

CREATE TABLE Pedido (
    id_pedido       INT IDENTITY(1,1) PRIMARY KEY,
    id_departamento INT  NOT NULL,
    id_orden        INT  NULL,
    id_articulo     INT  NOT NULL,
    cantidad        INT  NOT NULL,
    fecha_solicitud DATE NOT NULL,
    fecha_necesaria DATE NOT NULL,
    CONSTRAINT FK_Pedido_Departamento
        FOREIGN KEY (id_departamento) REFERENCES Departamento(id_departamento),
    CONSTRAINT FK_Pedido_OrdenCompra
        FOREIGN KEY (id_orden) REFERENCES OrdenCompra(id_orden),
    CONSTRAINT FK_Pedido_Articulo
        FOREIGN KEY (id_articulo) REFERENCES Articulo(id_articulo),
    CONSTRAINT CK_Pedido_Cantidad CHECK (cantidad > 0),
    CONSTRAINT CK_Pedido_Fechas CHECK (fecha_necesaria >= fecha_solicitud)
);
GO

CREATE TABLE Oferta (
    id_oferta       INT IDENTITY(1,1) PRIMARY KEY,
    id_proveedor    INT NOT NULL,
    id_pedido       INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    fecha_oferta    DATE NOT NULL,
    CONSTRAINT FK_Oferta_Proveedor
        FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor),
    CONSTRAINT FK_Oferta_Pedido
        FOREIGN KEY (id_pedido) REFERENCES Pedido(id_pedido),
    CONSTRAINT CK_Oferta_Precio CHECK (precio_unitario > 0)
);
GO

CREATE TABLE Adjudicacion (
    id_adjudicacion  INT IDENTITY(1,1) PRIMARY KEY,
    id_orden         INT  NOT NULL UNIQUE,
    fecha_resolucion DATE NOT NULL,
    CONSTRAINT FK_Adjudicacion_OrdenCompra
        FOREIGN KEY (id_orden) REFERENCES OrdenCompra(id_orden)
);
GO

CREATE TABLE DetalleAdjudicacion (
    id_detalle_adjudicacion INT IDENTITY(1,1) PRIMARY KEY,
    id_adjudicacion INT NOT NULL,
    id_pedido       INT NOT NULL,
    id_oferta       INT NOT NULL,
    cantidad_final  INT NOT NULL,
    precio_acordado DECIMAL(10,2) NOT NULL,
    CONSTRAINT FK_DetalleAdjudicacion_Adjudicacion
        FOREIGN KEY (id_adjudicacion) REFERENCES Adjudicacion(id_adjudicacion),
    CONSTRAINT FK_DetalleAdjudicacion_Pedido
        FOREIGN KEY (id_pedido) REFERENCES Pedido(id_pedido),
    CONSTRAINT FK_DetalleAdjudicacion_Oferta
        FOREIGN KEY (id_oferta) REFERENCES Oferta(id_oferta),
    CONSTRAINT CK_DetalleAdjudicacion_Cantidad CHECK (cantidad_final > 0),
    CONSTRAINT CK_DetalleAdjudicacion_Precio CHECK (precio_acordado > 0),
    CONSTRAINT UQ_DetalleAdjudicacion_AdjPedido UNIQUE (id_adjudicacion, id_pedido)
);
GO

CREATE TABLE EvaluacionProveedor (
    id_evaluacion           INT IDENTITY(1,1) PRIMARY KEY,
    id_detalle_adjudicacion INT NOT NULL,
    calificacion            TINYINT NOT NULL,
    comentario              VARCHAR(300) NULL,
    fecha                   DATE NOT NULL,
    CONSTRAINT FK_EvaluacionProveedor_Detalle
        FOREIGN KEY (id_detalle_adjudicacion) REFERENCES DetalleAdjudicacion(id_detalle_adjudicacion),
    CONSTRAINT CK_EvaluacionProveedor_Calificacion CHECK (calificacion BETWEEN 1 AND 5)
);
GO

CREATE TABLE Rol (
    id_rol      INT IDENTITY(1,1) PRIMARY KEY,
    nombre_rol  VARCHAR(40) NOT NULL UNIQUE,
    descripcion VARCHAR(200) NULL
);
GO

CREATE TABLE Pantalla (
    id_pantalla     INT IDENTITY(1,1) PRIMARY KEY,
    nombre_pantalla VARCHAR(60) NOT NULL UNIQUE,
    descripcion     VARCHAR(200) NULL
);
GO

CREATE TABLE Permiso (
    id_rol             INT NOT NULL,
    id_pantalla        INT NOT NULL,
    permite_crear      BIT NOT NULL DEFAULT 0,
    permite_leer       BIT NOT NULL DEFAULT 0,
    permite_actualizar BIT NOT NULL DEFAULT 0,
    permite_borrar     BIT NOT NULL DEFAULT 0,
    CONSTRAINT PK_Permiso PRIMARY KEY (id_rol, id_pantalla),
    CONSTRAINT FK_Permiso_Rol
        FOREIGN KEY (id_rol) REFERENCES Rol(id_rol),
    CONSTRAINT FK_Permiso_Pantalla
        FOREIGN KEY (id_pantalla) REFERENCES Pantalla(id_pantalla)
);
GO

CREATE TABLE Usuario (
    id_usuario      INT IDENTITY(1,1) PRIMARY KEY,
    nombre_usuario  VARCHAR(30)  NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    id_rol          INT NOT NULL,
    id_proveedor    INT NULL,
    estado          VARCHAR(10) NOT NULL DEFAULT 'Activo',
    fecha_creacion  DATE NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Usuario_Rol
        FOREIGN KEY (id_rol) REFERENCES Rol(id_rol),
    CONSTRAINT FK_Usuario_Proveedor
        FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor),
    CONSTRAINT CK_Usuario_Estado CHECK (estado IN ('Activo','Inactivo'))
);
GO

CREATE TRIGGER TRG_Pedido_ValidaFechaOrden
ON Pedido
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1
        FROM inserted i
        INNER JOIN OrdenCompra o ON o.id_orden = i.id_orden
        WHERE i.id_orden IS NOT NULL
          AND o.fecha_creacion < i.fecha_solicitud
    )
    BEGIN
        RAISERROR('La fecha de creación de la orden no puede ser anterior a la fecha de solicitud del pedido.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO

CREATE TRIGGER TRG_Adjudicacion_ValidaFecha
ON Adjudicacion
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1
        FROM inserted i
        INNER JOIN OrdenCompra o ON o.id_orden = i.id_orden
        WHERE i.fecha_resolucion < o.fecha_creacion
    )
    BEGIN
        RAISERROR('La fecha de adjudicación no puede ser anterior a la fecha de creación de la orden.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO
