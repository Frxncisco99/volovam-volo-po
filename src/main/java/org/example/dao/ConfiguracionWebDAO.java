package org.example.dao;

import org.example.modelo.ConfiguracionWeb;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConfiguracionWebDAO {

    public ConfiguracionWebDAO() {
        asegurarEstructura();
    }

    public void asegurarEstructura() {
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS configuracion_web (
                        id TINYINT NOT NULL PRIMARY KEY DEFAULT 1,
                        supabase_url VARCHAR(255) NOT NULL DEFAULT '',
                        supabase_anon_key TEXT NULL,
                        proyecto_ref VARCHAR(120) NOT NULL DEFAULT '',
                        catalogo_activo TINYINT(1) NOT NULL DEFAULT 0,
                        pedidos_web_activos TINYINT(1) NOT NULL DEFAULT 0,
                        mostrar_agotados TINYINT(1) NOT NULL DEFAULT 1,
                        ocultar_sin_stock TINYINT(1) NOT NULL DEFAULT 1,
                        domicilio_activo TINYINT(1) NOT NULL DEFAULT 0,
                        costo_envio DECIMAL(10,2) NOT NULL DEFAULT 50.00,
                        whatsapp VARCHAR(30) NOT NULL DEFAULT '',
                        facebook_url VARCHAR(255) NOT NULL DEFAULT '',
                        ultima_sincronizacion DATETIME NULL,
                        estado_conexion VARCHAR(30) NOT NULL DEFAULT 'SIN_CONEXION',
                        usar_codigo_barras TINYINT(1) NOT NULL DEFAULT 1,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci
                    """)) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("""
                    INSERT IGNORE INTO configuracion_web (id, estado_conexion)
                    VALUES (1, 'SIN_CONEXION')
                    """)) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS web_sync_queue (
                        id_sync BIGINT NOT NULL AUTO_INCREMENT,
                        entidad VARCHAR(50) NOT NULL,
                        operacion ENUM('UPSERT','DELETE') NOT NULL DEFAULT 'UPSERT',
                        id_local INT NOT NULL,
                        codigo_barras VARCHAR(50) NULL,
                        payload LONGTEXT NOT NULL,
                        estado ENUM('PENDIENTE','ENVIANDO','ENVIADO','ERROR') NOT NULL DEFAULT 'PENDIENTE',
                        intentos INT NOT NULL DEFAULT 0,
                        ultimo_error TEXT NULL,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        enviado_at DATETIME NULL,
                        PRIMARY KEY (id_sync),
                        KEY idx_web_sync_estado_created (estado, created_at),
                        KEY idx_web_sync_entidad_local (entidad, id_local)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci
                    """)) {
                ps.executeUpdate();
            }
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }
    }

    public ConfiguracionWeb cargarConfiguracionWeb() {
        asegurarEstructura();
        ConfiguracionWeb config = new ConfiguracionWeb();
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return config;
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM configuracion_web WHERE id = 1");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    config.setId(rs.getInt("id"));
                    config.setSupabaseUrl(rs.getString("supabase_url"));
                    config.setSupabaseAnonKey(rs.getString("supabase_anon_key"));
                    config.setProyectoRef(rs.getString("proyecto_ref"));
                    config.setCatalogoActivo(rs.getBoolean("catalogo_activo"));
                    config.setPedidosWebActivos(rs.getBoolean("pedidos_web_activos"));
                    config.setMostrarAgotados(rs.getBoolean("mostrar_agotados"));
                    config.setOcultarSinStock(rs.getBoolean("ocultar_sin_stock"));
                    config.setDomicilioActivo(rs.getBoolean("domicilio_activo"));
                    config.setCostoEnvio(rs.getBigDecimal("costo_envio"));
                    config.setWhatsapp(rs.getString("whatsapp"));
                    config.setFacebookUrl(rs.getString("facebook_url"));
                    Timestamp ultima = rs.getTimestamp("ultima_sincronizacion");
                    if (ultima != null) config.setUltimaSincronizacion(ultima.toLocalDateTime());
                    config.setEstadoConexion(rs.getString("estado_conexion"));
                    config.setUsarCodigoBarras(rs.getBoolean("usar_codigo_barras"));
                }
            }
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }
        return config;
    }

    public void guardarConfiguracionWeb(ConfiguracionWeb config) {
        asegurarEstructura();
        String sql = """
                INSERT INTO configuracion_web (
                    id, supabase_url, supabase_anon_key, proyecto_ref, catalogo_activo,
                    pedidos_web_activos, mostrar_agotados, ocultar_sin_stock, domicilio_activo,
                    costo_envio, whatsapp, facebook_url, ultima_sincronizacion, estado_conexion, usar_codigo_barras
                ) VALUES (1,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                ON DUPLICATE KEY UPDATE
                    supabase_url = VALUES(supabase_url),
                    supabase_anon_key = VALUES(supabase_anon_key),
                    proyecto_ref = VALUES(proyecto_ref),
                    catalogo_activo = VALUES(catalogo_activo),
                    pedidos_web_activos = VALUES(pedidos_web_activos),
                    mostrar_agotados = VALUES(mostrar_agotados),
                    ocultar_sin_stock = VALUES(ocultar_sin_stock),
                    domicilio_activo = VALUES(domicilio_activo),
                    costo_envio = VALUES(costo_envio),
                    whatsapp = VALUES(whatsapp),
                    facebook_url = VALUES(facebook_url),
                    ultima_sincronizacion = VALUES(ultima_sincronizacion),
                    estado_conexion = VALUES(estado_conexion),
                    usar_codigo_barras = VALUES(usar_codigo_barras)
                """;
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, config.getSupabaseUrl());
                ps.setString(2, config.getSupabaseAnonKey());
                ps.setString(3, config.getProyectoRef());
                ps.setBoolean(4, config.isCatalogoActivo());
                ps.setBoolean(5, config.isPedidosWebActivos());
                ps.setBoolean(6, config.isMostrarAgotados());
                ps.setBoolean(7, config.isOcultarSinStock());
                ps.setBoolean(8, config.isDomicilioActivo());
                ps.setBigDecimal(9, config.getCostoEnvio());
                ps.setString(10, config.getWhatsapp());
                ps.setString(11, config.getFacebookUrl());
                if (config.getUltimaSincronizacion() == null) ps.setNull(12, Types.TIMESTAMP);
                else ps.setTimestamp(12, Timestamp.valueOf(config.getUltimaSincronizacion()));
                ps.setString(13, config.getEstadoConexion());
                ps.setBoolean(14, config.isUsarCodigoBarras());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }
    }

    public void actualizarEstadoConexion(String estado) {
        ejecutarUpdateConfig("UPDATE configuracion_web SET estado_conexion = ? WHERE id = 1", estado);
    }

    public void marcarSincronizacion(String estado) {
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement("""
                     UPDATE configuracion_web
                     SET ultima_sincronizacion = NOW(), estado_conexion = ?
                     WHERE id = 1
                     """)) {
                ps.setString(1, estado);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }
    }

    private void ejecutarUpdateConfig(String sql, String valor) {
        asegurarEstructura();
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, valor);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }
    }

    public int contarProductosSinCodigoBarras() {
        String sql = "SELECT COUNT(*) FROM productos WHERE activo = 1 AND (codigo_barras IS NULL OR TRIM(codigo_barras) = '')";
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return 0;
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }
        return 0;
    }

    public int contarProductosActivos() {
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM productos WHERE activo = 1");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }
        return 0;
    }

    public int contarPendientesSincronizacion() {
        asegurarEstructura();
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM web_sync_queue WHERE estado IN ('PENDIENTE','ERROR')");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }
        return 0;
    }

    public List<CategoriaWeb> listarCategoriasWeb() {
        List<CategoriaWeb> categorias = new ArrayList<>();
        String sql = "SELECT id_categoria, nombre FROM categorias ORDER BY id_categoria";
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return categorias;
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categorias.add(new CategoriaWeb(rs.getInt("id_categoria"), rs.getString("nombre")));
                }
            }
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }
        return categorias;
    }

    public List<ProductoWeb> listarProductosWeb() {
        List<ProductoWeb> productos = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return productos;
            boolean tieneDescripcion = columnaExiste(con, "productos", "descripcion");
            boolean tieneImagen = columnaExiste(con, "productos", "imagen_url");
            boolean tieneUnidad = columnaExiste(con, "productos", "unidad_medida");
            String sql = """
                    SELECT p.id_producto, p.nombre, p.codigo_barras, p.precio, p.costo, p.stock,
                           p.stock_minimo, p.id_categoria, p.activo, c.nombre AS categoria,
                           %s AS descripcion, %s AS imagen_url, %s AS unidad_medida
                    FROM productos p
                    LEFT JOIN categorias c ON c.id_categoria = p.id_categoria
                    WHERE p.activo = 1
                    ORDER BY p.id_producto
                    """.formatted(
                    tieneDescripcion ? "p.descripcion" : "NULL",
                    tieneImagen ? "p.imagen_url" : "NULL",
                    tieneUnidad ? "p.unidad_medida" : "'pieza'"
            );
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(new ProductoWeb(
                            rs.getInt("id_producto"),
                            rs.getString("nombre"),
                            rs.getString("codigo_barras"),
                            rs.getBigDecimal("precio"),
                            rs.getBigDecimal("costo"),
                            rs.getInt("stock"),
                            rs.getInt("stock_minimo"),
                            rs.getInt("id_categoria"),
                            rs.getBoolean("activo"),
                            rs.getString("categoria"),
                            rs.getString("descripcion"),
                            rs.getString("imagen_url"),
                            rs.getString("unidad_medida")
                    ));
                }
            }
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }
        return productos;
    }

    public int encolarInventarioLocal() {
        asegurarEstructura();
        int total = 0;
        for (CategoriaWeb categoria : listarCategoriasWeb()) {
            encolar("categorias", categoria.idLocal(), null, categoria.toJson());
            total++;
        }
        for (ProductoWeb producto : listarProductosWeb()) {
            encolar("productos", producto.idLocal(), producto.codigoBarras(), producto.toJson());
            total++;
        }
        return total;
    }

    public int generarEnlacesAutomaticos() {
        return encolarInventarioLocal();
    }

    private void encolar(String entidad, int idLocal, String codigoBarras, String payload) {
        String sql = "INSERT INTO web_sync_queue (entidad, operacion, id_local, codigo_barras, payload) VALUES (?, 'UPSERT', ?, ?, ?)";
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, entidad);
                ps.setInt(2, idLocal);
                if (codigoBarras == null || codigoBarras.isBlank()) ps.setNull(3, Types.VARCHAR);
                else ps.setString(3, codigoBarras);
                ps.setString(4, payload);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            org.example.servicio.LogService.error("Error no controlado", e);
        }
    }

    private boolean columnaExiste(Connection con, String tabla, String columna) {
        try {
            DatabaseMetaData meta = con.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, tabla, columna)) {
                return rs.next();
            }
        } catch (Exception e) {
            return false;
        }
    }

    public record CategoriaWeb(int idLocal, String nombre) {
        public String slug() {
            String limpio = nombre == null ? "" : nombre
                    .toLowerCase()
                    .replace("á", "a").replace("é", "e").replace("í", "i")
                    .replace("ó", "o").replace("ú", "u").replace("ñ", "n")
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("^-|-$", "");
            return limpio.isBlank() ? "categoria-" + idLocal : limpio;
        }

        public String toJson() {
            return "{\"id_local\":" + idLocal
                    + ",\"nombre\":\"" + json(nombre) + "\""
                    + ",\"slug\":\"" + json(slug()) + "\""
                    + ",\"orden\":" + idLocal
                    + ",\"activo\":true}";
        }
    }

    public record ProductoWeb(
            int idLocal,
            String nombre,
            String codigoBarras,
            BigDecimal precio,
            BigDecimal costo,
            int stock,
            int stockMinimo,
            int idCategoriaLocal,
            boolean activo,
            String categoria,
            String descripcion,
            String imagenUrl,
            String unidadMedida
    ) {
        public String toJson() {
            return "{\"id_local\":" + idLocal
                    + ",\"codigo_barras\":" + jsonOrNull(codigoBarras)
                    + ",\"nombre\":\"" + json(nombre) + "\""
                    + ",\"descripcion\":" + jsonOrNull(descripcion)
                    + ",\"precio\":" + decimal(precio)
                    + ",\"costo\":" + decimal(costo)
                    + ",\"stock\":" + Math.max(0, stock)
                    + ",\"stock_minimo\":" + Math.max(0, stockMinimo)
                    + ",\"id_categoria_local\":" + idCategoriaLocal
                    + ",\"activo\":" + activo
                    + ",\"imagen_url\":" + jsonOrNull(imagenUrl)
                    + ",\"unidad_medida\":\"" + json(unidadMedida == null || unidadMedida.isBlank() ? "pieza" : unidadMedida) + "\"}";
        }
    }

    private static String decimal(BigDecimal value) {
        return value == null ? "0" : value.toPlainString();
    }

    private static String jsonOrNull(String value) {
        return value == null || value.isBlank() ? "null" : "\"" + json(value) + "\"";
    }

    private static String json(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
