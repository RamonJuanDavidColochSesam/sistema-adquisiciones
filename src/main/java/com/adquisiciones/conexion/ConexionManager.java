package com.adquisiciones.conexion;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionManager {

    private static ConexionManager instancia;

    private final HikariDataSource poolSqlServer;
    private final HikariDataSource poolPostgres;

    private ConexionManager() {
        Properties props = cargarPropiedades();

        HikariConfig configSqlServer = new HikariConfig();
        configSqlServer.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        configSqlServer.setJdbcUrl(props.getProperty("sqlserver.url"));
        configSqlServer.setJdbcUrl(props.getProperty("sqlserver.url"));
        configSqlServer.setUsername(props.getProperty("sqlserver.user"));
        configSqlServer.setPassword(props.getProperty("sqlserver.password"));
        configSqlServer.setMaximumPoolSize(5);
        this.poolSqlServer = new HikariDataSource(configSqlServer);

        HikariConfig configPostgres = new HikariConfig();
        configPostgres.setDriverClassName("org.postgresql.Driver");
        configPostgres.setJdbcUrl(props.getProperty("postgres.url"));
        configPostgres.setJdbcUrl(props.getProperty("postgres.url"));
        configPostgres.setUsername(props.getProperty("postgres.user"));
        configPostgres.setPassword(props.getProperty("postgres.password"));
        configPostgres.setMaximumPoolSize(5);
        this.poolPostgres = new HikariDataSource(configPostgres);
    }

    public static synchronized ConexionManager getInstancia() {
        if (instancia == null) {
            instancia = new ConexionManager();
        }
        return instancia;
    }

    public Connection getConexionSqlServer() throws SQLException {
        return poolSqlServer.getConnection();
    }

    public Connection getConexionPostgres() throws SQLException {
        return poolPostgres.getConnection();
    }

    private Properties cargarPropiedades() {
        Properties props = new Properties();
        try (InputStream input = ConexionManager.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new RuntimeException("No se encontró db.properties en el classpath");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar db.properties", e);
        }
        return props;
    }
}