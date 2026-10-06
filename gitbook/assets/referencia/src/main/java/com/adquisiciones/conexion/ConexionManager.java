package com.adquisiciones.conexion;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/** Pools independientes; configuración externa, nunca incluida en el WAR. */
public final class ConexionManager implements AutoCloseable {
    private static ConexionManager instancia;
    private final Properties props;
    private HikariDataSource sqlServer;
    private HikariDataSource postgres;

    private ConexionManager() { props = cargarPropiedades(); }
    public static synchronized ConexionManager getInstancia() {
        if (instancia == null) instancia = new ConexionManager();
        return instancia;
    }
    public synchronized Connection getConexionSqlServer() throws SQLException {
        if (sqlServer == null || sqlServer.isClosed())
            sqlServer = crearPool("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return sqlServer.getConnection();
    }
    public synchronized Connection getConexionPostgres() throws SQLException {
        if (postgres == null || postgres.isClosed())
            postgres = crearPool("postgres", "org.postgresql.Driver");
        return postgres.getConnection();
    }
    private HikariDataSource crearPool(String db, String driver) {
        HikariConfig c = new HikariConfig();
        c.setDriverClassName(driver);
        c.setJdbcUrl(requerido(db + ".url"));
        c.setUsername(requerido(db + ".user"));
        c.setPassword(requerido(db + ".password"));
        c.setPoolName("GuateCompras-" + db);
        c.setMaximumPoolSize(5);
        c.setMinimumIdle(1);
        c.setConnectionTimeout(10000);
        c.setValidationTimeout(3000);
        c.setInitializationFailTimeout(10000);
        return new HikariDataSource(c);
    }
    private String requerido(String key) {
        String env = System.getenv("GUATECOMPRAS_" + key.toUpperCase().replace('.', '_'));
        String value = env == null ? props.getProperty(key) : env;
        if (value == null || value.isBlank())
            throw new IllegalStateException("Falta configuración: " + key);
        return value;
    }
    private static Properties cargarPropiedades() {
        String configured = System.getProperty("guatecompras.config");
        if (configured == null || configured.isBlank()) configured = System.getenv("GUATECOMPRAS_CONFIG");
        Path path = Path.of(configured == null || configured.isBlank()
                ? "config/local-db.properties" : configured);
        Properties p = new Properties();
        if (!Files.exists(path)) return p; // También se permite configurar solo por variables.
        try (Reader input = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            p.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la configuración externa", e);
        }
        return p;
    }
    @Override public synchronized void close() {
        if (sqlServer != null) sqlServer.close();
        if (postgres != null) postgres.close();
    }
}
