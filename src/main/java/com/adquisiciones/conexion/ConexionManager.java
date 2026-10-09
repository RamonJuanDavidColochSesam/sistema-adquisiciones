package com.adquisiciones.conexion;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pools independientes; configuración externa, nunca incluida en el WAR.
 * El archivo de configuración se busca así (en este orden):
 * 1. -Dguatecompras.config=... o la variable de entorno GUATECOMPRAS_CONFIG.
 * 2. config/local-db.properties en el directorio de trabajo y en sus padres.
 * 3. config/local-db.properties en una subcarpeta del directorio de trabajo
 *    (cuando Tomcat arranca desde la raíz del workspace de Eclipse).
 * 4. db.properties en el classpath: solo existe en el despliegue de desarrollo
 *    de Eclipse, ya que pom.xml lo excluye del WAR empaquetado.
 */
public final class ConexionManager implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ConexionManager.class);
    private static final String CONFIGURACION = "config/local-db.properties";
    private static final String RECURSO_CLASPATH = "db.properties";
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
            throw new IllegalStateException("Falta la configuración '" + key + "'. Coloque el archivo "
                    + CONFIGURACION + " junto al proyecto o defina la variable GUATECOMPRAS_CONFIG "
                    + "con la ruta absoluta de ese archivo.");
        return value;
    }

    private static Properties cargarPropiedades() {
        List<Path> candidatas = candidatasConfiguracion();
        for (Path candidata : candidatas) {
            if (Files.isRegularFile(candidata)) {
                Properties p = leer(candidata);
                if (!p.isEmpty()) {
                    LOG.info("Configuración de base de datos: {}", candidata.toAbsolutePath());
                    return p;
                }
            }
        }
        Properties delClasspath = leerClasspath();
        if (!delClasspath.isEmpty()) {
            LOG.info("Configuración de base de datos: classpath:/{}", RECURSO_CLASPATH);
            return delClasspath;
        }
        LOG.warn("No se encontró la configuración externa; se confiará en variables de entorno. Rutas probadas: {}",
                candidatas);
        return new Properties(); // También se permite configurar solo por variables.
    }

    /** Todas las rutas posibles: explícita, relativas al directorio de trabajo y una subcarpeta. */
    private static List<Path> candidatasConfiguracion() {
        List<Path> candidatas = new ArrayList<>();
        String configured = System.getProperty("guatecompras.config");
        if (configured == null || configured.isBlank()) configured = System.getenv("GUATECOMPRAS_CONFIG");
        if (configured != null && !configured.isBlank()) candidatas.add(Path.of(configured));

        Path actual = Path.of("").toAbsolutePath();
        candidatas.add(actual.resolve(CONFIGURACION));
        for (Path padre = actual.getParent(); padre != null && candidatas.size() < 12; padre = padre.getParent()) {
            candidatas.add(padre.resolve(CONFIGURACION));
        }
        // Cuando el servidor arranca fuera del proyecto (p. ej. en la raíz del
        // workspace o en la carpeta de Eclipse), el archivo vive en un hijo.
        try (var subcarpetas = Files.list(actual)) {
            subcarpetas.filter(Files::isDirectory)
                    .map(carpeta -> carpeta.resolve(CONFIGURACION))
                    .filter(Files::isRegularFile)
                    .forEach(candidatas::add);
        } catch (IOException e) {
            LOG.debug("No se pudo explorar {}", actual, e);
        }
        return candidatas;
    }

    private static Properties leer(Path path) {
        Properties p = new Properties();
        try (Reader input = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            p.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la configuración externa: " + path.toAbsolutePath(), e);
        }
        return p;
    }

    /** Solo existe en el despliegue de desarrollo: pom.xml excluye db.properties del WAR. */
    private static Properties leerClasspath() {
        Properties p = new Properties();
        ClassLoader cargador = ConexionManager.class.getClassLoader();
        InputStream entrada = cargador == null ? null : cargador.getResourceAsStream(RECURSO_CLASPATH);
        if (entrada == null) {
            ClassLoader contexto = Thread.currentThread().getContextClassLoader();
            entrada = contexto == null ? null : contexto.getResourceAsStream(RECURSO_CLASPATH);
        }
        if (entrada == null) return p;
        try (InputStream in = entrada) {
            p.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + RECURSO_CLASPATH + " del classpath", e);
        }
        return p;
    }
    @Override public synchronized void close() {
        if (sqlServer != null) sqlServer.close();
        if (postgres != null) postgres.close();
    }
}
