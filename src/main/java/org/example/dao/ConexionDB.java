package org.example.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionDB {

    private static final String URL_DEFAULT =
            "jdbc:mysql://localhost:3306/pospanaderia?useSSL=false&serverTimezone=America/Monterrey&useLegacyDatetimeCode=false&useJDBCCompliantTimezoneShift=true";
    private static final String USUARIO_DEFAULT = "root";
    private static final String CLAVE_DEFAULT = "";
    private static final Properties LOCAL_PROPERTIES = cargarPropiedadesLocales();

    private static final HikariDataSource DATA_SOURCE = crearDataSource();

    public static Connection getConexion() {
        try {
            return DATA_SOURCE.getConnection();
        } catch (SQLException e) {
            System.err.println("No se pudo obtener conexion a la base de datos: " + e.getMessage());
            return null;
        }
    }

    public static void cerrarPool() {
        if (DATA_SOURCE != null && !DATA_SOURCE.isClosed()) {
            DATA_SOURCE.close();
        }
    }

    private static HikariDataSource crearDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(config("pos.db.url", "POS_DB_URL", URL_DEFAULT));
        config.setUsername(config("pos.db.user", "POS_DB_USER", USUARIO_DEFAULT));
        config.setPassword(config("pos.db.password", "POS_DB_PASSWORD", CLAVE_DEFAULT));
        config.setMaximumPoolSize(entero("pos.db.pool.max", "POS_DB_POOL_MAX", 10));
        config.setMinimumIdle(entero("pos.db.pool.min", "POS_DB_POOL_MIN", 2));
        config.setConnectionTimeout(10_000);
        config.setIdleTimeout(120_000);
        config.setMaxLifetime(1_800_000);
        config.setPoolName("VolovanVoloPosPool");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return new HikariDataSource(config);
    }

    private static String config(String property, String env, String defaultValue) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) value = System.getenv(env);
        if ((value == null || value.isBlank()) && LOCAL_PROPERTIES.containsKey(property)) {
            value = LOCAL_PROPERTIES.getProperty(property);
        }
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static int entero(String property, String env, int defaultValue) {
        try {
            return Integer.parseInt(config(property, env, String.valueOf(defaultValue)));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static Properties cargarPropiedadesLocales() {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream("db.local.properties")) {
            props.load(in);
        } catch (Exception e) {
            // Archivo opcional para desarrollo/produccion local. No se versiona.
        }
        return props;
    }
}
