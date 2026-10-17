/*
 * db/ddl/sqlserver.sql - Estructura final de GuateCompras (SQL Server)
 *
 * Un solo archivo con todo lo que el sistema necesita para funcionar:
 *   21 tablas, 1 vista (vw_OrdenesAbiertas), 4 índices, restricciones y
 *   10 triggers de reglas de negocio.
 *
 * Uso (una sola vez, sobre una base vacía):
 *   1. CREATE DATABASE GuateCompras;
 *   2. sqlcmd -S localhost -E -d GuateCompras -i db\ddl\sqlserver.sql
 *      (o ejecutar el archivo completo en SSMS, con GuateCompras seleccionada)
 * El archivo NO fija la base: se ejecuta siempre contra la que elija el llamador.
 * Ese nombre debe coincidir con databaseName= en sqlserver.url
 * de config/local-db.properties.
 *
 * Este archivo solo crea estructura: no inserta datos. El poblado lo hace
 * SemillaDemo.java (scripts/bases.ps1 -Semilla).
 */

/* =========================================================================
 * 1. TABLAS BASE
 * ========================================================================= */

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
    descripcion     VARCHAR(300) NULL,
    estado          VARCHAR(10)  NOT NULL
        CONSTRAINT DF_Articulo_Estado DEFAULT 'Activo',
    CONSTRAINT CK_Articulo_Estado CHECK (estado IN ('Activo','Inactivo'))
);
GO

CREATE TABLE Proveedor (
    id_proveedor     INT IDENTITY(1,1) PRIMARY KEY,
    codigo_proveedor VARCHAR(20)  NOT NULL UNIQUE,
    nombre_comercial VARCHAR(100) NOT NULL,
    direccion        VARCHAR(150) NOT NULL,
    telefono         VARCHAR(20)  NOT NULL,
    categoria        VARCHAR(80)  NOT NULL,
    estado           VARCHAR(10)  NOT NULL
        CONSTRAINT DF_Proveedor_Estado DEFAULT 'Activo',
    CONSTRAINT CK_Proveedor_Estado CHECK (estado IN ('Activo','Inactivo'))
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
    observaciones       VARCHAR(300) NULL,
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
    observaciones   VARCHAR(300) NULL,
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
    observaciones    VARCHAR(300) NULL,
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

/* =========================================================================
 * 2. TABLAS DE APOYO: teléfonos de sucursal, rubros de proveedor y auditoría
 * ========================================================================= */

CREATE TABLE SucursalTelefono (
    id_sucursal INT         NOT NULL,
    telefono    VARCHAR(20) NOT NULL,
    CONSTRAINT PK_SucursalTelefono PRIMARY KEY (id_sucursal, telefono),
    CONSTRAINT FK_SucursalTelefono_Sucursal
        FOREIGN KEY (id_sucursal) REFERENCES Sucursal(id_sucursal)
);
GO

CREATE TABLE ProveedorRubro (
    id_proveedor INT         NOT NULL,
    rubro        VARCHAR(80) NOT NULL,
    CONSTRAINT PK_ProveedorRubro PRIMARY KEY (id_proveedor, rubro),
    CONSTRAINT FK_ProveedorRubro_Proveedor
        FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor)
);
GO

CREATE TABLE Auditoria (
    id_auditoria BIGINT IDENTITY PRIMARY KEY,
    id_usuario   INT NULL,
    accion       VARCHAR(20)  NOT NULL,
    entidad      VARCHAR(60)  NOT NULL,
    referencia   VARCHAR(80)  NULL,
    fecha        DATETIME2    NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Auditoria_Usuario
        FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario)
);
GO

/* =========================================================================
 * 3. RESTRICCIONES COMPUESTAS Y CANÓNICAS
 * ========================================================================= */

ALTER TABLE SubtipoOrden
    ADD CONSTRAINT UQ_SubtipoOrden_TipoNombre UNIQUE (id_tipo, nombre);
ALTER TABLE SubtipoOrden
    ADD CONSTRAINT UQ_SubtipoOrden_TipoId UNIQUE (id_tipo, id_subtipo);
ALTER TABLE OrdenCompra
    ADD CONSTRAINT FK_OrdenCompra_TipoSubtipo
        FOREIGN KEY (id_tipo, id_subtipo) REFERENCES SubtipoOrden (id_tipo, id_subtipo);
ALTER TABLE RelacionComercial
    ADD CONSTRAINT CK_RelacionComercial_Canonica CHECK (id_proveedor_a < id_proveedor_b);
GO

/* =========================================================================
 * 4. ÍNDICES DE APOYO A LAS CONSULTAS
 * ========================================================================= */

CREATE INDEX IX_Pedido_Orden
    ON Pedido (id_orden) INCLUDE (id_departamento, id_articulo, cantidad);
CREATE INDEX IX_Oferta_PedidoProveedor
    ON Oferta (id_pedido, id_proveedor, fecha_oferta) INCLUDE (precio_unitario);
CREATE INDEX IX_Departamento_Sucursal
    ON Departamento (id_sucursal);
CREATE INDEX IX_Adjudicacion_Fecha
    ON Adjudicacion (fecha_resolucion) INCLUDE (id_orden);
GO

/* =========================================================================
 * 5. VISTA DE ÓRDENES ABIERTAS (informe 5 y disponibilidad de ofertas)
 * ========================================================================= */

CREATE OR ALTER VIEW dbo.vw_OrdenesAbiertas AS
SELECT o.id_orden,
       o.descripcion,
       o.fecha_creacion,
       o.fecha_limite_oferta,
       t.nombre AS tipo,
       s.nombre AS subtipo
FROM OrdenCompra o
INNER JOIN TipoOrden t  ON t.id_tipo  = o.id_tipo
LEFT  JOIN SubtipoOrden s ON s.id_subtipo = o.id_subtipo
WHERE NOT EXISTS (SELECT 1 FROM Adjudicacion a WHERE a.id_orden = o.id_orden)
  AND o.fecha_creacion     <= CAST(GETDATE() AS DATE)
  AND o.fecha_limite_oferta >= CAST(GETDATE() AS DATE);
GO

/* =========================================================================
 * 6. TRIGGERS DE REGLAS DE NEGOCIO
 * ========================================================================= */

-- 6.1 Reglas de captura de origen
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

-- 6.2 Integridad de ofertas, detalle de adjudicación y órdenes
CREATE OR ALTER TRIGGER dbo.tr_Oferta_Integridad ON dbo.Oferta AFTER INSERT,UPDATE AS
BEGIN
   SET NOCOUNT ON;
   IF EXISTS(SELECT 1 FROM inserted i JOIN Pedido p ON p.id_pedido=i.id_pedido LEFT JOIN OrdenCompra o ON o.id_orden=p.id_orden WHERE o.id_orden IS NULL OR i.fecha_oferta<o.fecha_creacion OR i.fecha_oferta>o.fecha_limite_oferta)
   THROW 51001,'La oferta requiere pedido asignado y fecha dentro del plazo de la orden.',1;
   IF EXISTS(SELECT 1 FROM inserted i JOIN Pedido p ON p.id_pedido=i.id_pedido WHERE NOT EXISTS(SELECT 1 FROM ProveedorArticulo pa WHERE pa.id_proveedor=i.id_proveedor AND pa.id_articulo=p.id_articulo))
   THROW 51002,'El proveedor debe incluir el artículo en su catálogo.',1;
END;
GO

CREATE OR ALTER TRIGGER dbo.tr_DetalleAdjudicacion_Integridad ON dbo.DetalleAdjudicacion AFTER INSERT,UPDATE AS
BEGIN
   SET NOCOUNT ON;
   IF EXISTS(SELECT 1 FROM inserted i JOIN Adjudicacion a ON a.id_adjudicacion=i.id_adjudicacion JOIN Pedido p ON p.id_pedido=i.id_pedido JOIN Oferta f ON f.id_oferta=i.id_oferta WHERE p.id_orden IS NULL OR p.id_orden<>a.id_orden OR f.id_pedido<>p.id_pedido OR f.fecha_oferta>a.fecha_resolucion OR i.cantidad_final>p.cantidad)
   THROW 51003,'La oferta, pedido y adjudicación deben corresponder a la misma orden y cantidad solicitada.',1;
END;
GO

CREATE OR ALTER TRIGGER dbo.tr_OrdenCompra_Integridad ON dbo.OrdenCompra AFTER INSERT,UPDATE AS
BEGIN
   SET NOCOUNT ON;
   IF EXISTS(SELECT 1 FROM inserted i JOIN Pedido p ON p.id_orden=i.id_orden WHERE i.fecha_creacion<p.fecha_solicitud)
   THROW 51004,'La creación de la orden no puede preceder sus solicitudes.',1;
   IF EXISTS(SELECT 1 FROM inserted i JOIN Adjudicacion a ON a.id_orden=i.id_orden WHERE i.fecha_creacion>a.fecha_resolucion)
   THROW 51005,'La creación de la orden no puede ser posterior a su adjudicación.',1;
   IF EXISTS(SELECT 1 FROM inserted i JOIN SubtipoOrden s ON s.id_subtipo=i.id_subtipo WHERE s.id_tipo<>i.id_tipo)
   THROW 51006,'El subtipo no pertenece al tipo seleccionado.',1;
END;
GO

-- 6.3 Historial: protege lo ya adjudicado o con ofertas
CREATE OR ALTER TRIGGER dbo.tr_Pedido_Historial ON dbo.Pedido AFTER UPDATE AS
BEGIN
   SET NOCOUNT ON;
   IF EXISTS(SELECT 1 FROM inserted i JOIN deleted d ON d.id_pedido=i.id_pedido WHERE (ISNULL(i.id_orden,0)<>ISNULL(d.id_orden,0) OR i.id_articulo<>d.id_articulo) AND EXISTS(SELECT 1 FROM Oferta f WHERE f.id_pedido=i.id_pedido))
   THROW 51007,'No se puede reasignar ni cambiar el artículo de un pedido con ofertas.',1;
   IF EXISTS(SELECT 1 FROM inserted i JOIN DetalleAdjudicacion d ON d.id_pedido=i.id_pedido WHERE i.cantidad<d.cantidad_final)
   THROW 51008,'La cantidad solicitada no puede ser menor que la adjudicada.',1;
END;
GO

CREATE OR ALTER TRIGGER dbo.tr_Adjudicacion_Historial ON dbo.Adjudicacion AFTER UPDATE AS
BEGIN
   SET NOCOUNT ON;
   IF EXISTS(SELECT 1 FROM inserted i JOIN DetalleAdjudicacion d ON d.id_adjudicacion=i.id_adjudicacion JOIN Pedido p ON p.id_pedido=d.id_pedido JOIN Oferta f ON f.id_oferta=d.id_oferta WHERE p.id_orden<>i.id_orden OR f.fecha_oferta>i.fecha_resolucion)
   THROW 51009,'La resolución contradice los pedidos u ofertas ya adjudicados.',1;
END;
GO

CREATE OR ALTER TRIGGER dbo.tr_Oferta_Historial ON dbo.Oferta AFTER UPDATE AS
BEGIN
   SET NOCOUNT ON;
   IF EXISTS(SELECT 1 FROM inserted i JOIN deleted d ON d.id_oferta=i.id_oferta JOIN DetalleAdjudicacion a ON a.id_oferta=i.id_oferta WHERE i.id_proveedor<>d.id_proveedor OR i.id_pedido<>d.id_pedido OR i.precio_unitario<>d.precio_unitario OR i.fecha_oferta<>d.fecha_oferta)
   THROW 51010,'La oferta adjudicada forma parte del historial y no puede modificarse.',1;
END;
GO

CREATE OR ALTER TRIGGER dbo.tr_OrdenCompra_Ofertas ON dbo.OrdenCompra AFTER UPDATE AS
BEGIN
   SET NOCOUNT ON;
   IF EXISTS(SELECT 1 FROM inserted i JOIN Pedido p ON p.id_orden=i.id_orden JOIN Oferta f ON f.id_pedido=p.id_pedido WHERE f.fecha_oferta<i.fecha_creacion OR f.fecha_oferta>i.fecha_limite_oferta)
   THROW 51011,'Las nuevas fechas dejan ofertas existentes fuera del plazo.',1;
END;
GO

CREATE OR ALTER TRIGGER dbo.tr_ProveedorArticulo_Historial ON dbo.ProveedorArticulo AFTER DELETE AS
BEGIN
   SET NOCOUNT ON;
   IF EXISTS(SELECT 1 FROM deleted d JOIN Oferta f ON f.id_proveedor=d.id_proveedor JOIN Pedido p ON p.id_pedido=f.id_pedido AND p.id_articulo=d.id_articulo)
   THROW 51012,'No puede eliminar una relación de catálogo con ofertas históricas.',1;
END;
GO
