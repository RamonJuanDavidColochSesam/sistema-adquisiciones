package com.adquisiciones.util;

import com.adquisiciones.conexion.ConexionManager;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;

/** Migración explícita: no se ejecuta automáticamente durante el arranque. */
public final class MigrarBases {
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args.length == 0 ? "." : args[0]);
        var manager = ConexionManager.getInstancia();
        try {
            for (String db : List.of("sqlserver", "postgres")) {
                try (Connection c = db.equals("sqlserver") ? manager.getConexionSqlServer() : manager.getConexionPostgres()) {
                    try (Statement s = c.createStatement()) {
                        s.execute(db.equals("sqlserver")
                            ? "IF OBJECT_ID('dbo.SchemaVersion') IS NULL CREATE TABLE dbo.SchemaVersion(version_id VARCHAR(20) PRIMARY KEY,nombre VARCHAR(150) NOT NULL,fecha DATETIME2 DEFAULT SYSUTCDATETIME())"
                            : "CREATE TABLE IF NOT EXISTS SchemaVersion(version_id VARCHAR(20) PRIMARY KEY,nombre VARCHAR(150) NOT NULL,fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                    }
                    Path directory = root.resolve("db/migrations/" + db);
                    try (var paths = Files.list(directory)) {
                        for (Path migration : paths.filter(p -> p.toString().endsWith(".sql")).sorted().toList()) {
                            String name = migration.getFileName().toString();
                            String version = name.substring(0, name.indexOf("__"));
                            try (var q = c.prepareStatement("SELECT version_id FROM SchemaVersion WHERE version_id=?")) {
                                q.setString(1, version);
                                try (var r = q.executeQuery()) {
                                    if (r.next()) { System.out.println(db + " " + version + " ya aplicada"); continue; }
                                }
                            }
                            c.setAutoCommit(false);
                            try {
                                String sql = Files.readString(migration, StandardCharsets.UTF_8);
                                for (String batch : sql.split("(?m)^-- @batch\\s*$")) {
                                    if (!batch.isBlank()) try (var s = c.createStatement()) { s.execute(batch); }
                                }
                                try (var s = c.prepareStatement("INSERT INTO SchemaVersion(version_id,nombre) VALUES(?,?)")) {
                                    s.setString(1, version); s.setString(2, name); s.executeUpdate();
                                }
                                c.commit(); System.out.println(db + " " + version + " aplicada; transacción confirmada");
                            } catch (Exception e) { c.rollback(); throw e; }
                            finally { c.setAutoCommit(true); }
                        }
                    }
                }
            }
        } finally { manager.close(); }
    }
}
