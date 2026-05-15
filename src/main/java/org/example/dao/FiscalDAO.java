package org.example.dao;

import org.example.modelo.ConfiguracionFiscal;
import org.example.modelo.Impuesto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FiscalDAO {

    public List<Impuesto> obtenerImpuestosActivos() {
        List<Impuesto> impuestos = new ArrayList<>();
        String sql = """
                SELECT id_impuesto, clave, nombre, tipo, tasa, activo, predeterminado
                FROM impuestos
                WHERE activo = 1
                ORDER BY predeterminado DESC, nombre
                """;
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return impuestos;
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    impuestos.add(mapImpuesto(rs));
                }
            }
        } catch (Exception ignored) {
        }
        return impuestos;
    }

    public Optional<Impuesto> obtenerImpuestoPorClave(String clave) {
        String sql = """
                SELECT id_impuesto, clave, nombre, tipo, tasa, activo, predeterminado
                FROM impuestos
                WHERE clave = ? AND activo = 1
                LIMIT 1
                """;
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return Optional.empty();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, clave);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return Optional.of(mapImpuesto(rs));
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    public Impuesto obtenerImpuestoPredeterminado() {
        ConfiguracionFiscal config = obtenerConfiguracionFiscal();
        return obtenerImpuestoPorClave(config.getImpuestoPredeterminadoClave())
                .or(this::obtenerPrimerPredeterminado)
                .orElseGet(Impuesto::sinImpuesto);
    }

    public Optional<Impuesto> obtenerImpuestoProducto(int idProducto) {
        String sql = """
                SELECT i.id_impuesto, i.clave, i.nombre, i.tipo, i.tasa, i.activo, i.predeterminado
                FROM producto_impuesto pi
                JOIN impuestos i ON i.id_impuesto = pi.id_impuesto
                WHERE pi.id_producto = ? AND i.activo = 1
                LIMIT 1
                """;
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return Optional.empty();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idProducto);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return Optional.of(mapImpuesto(rs));
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    public void asignarImpuestoProducto(int idProducto, int idImpuesto) {
        String sql = """
                INSERT INTO producto_impuesto (id_producto, id_impuesto)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE id_impuesto = VALUES(id_impuesto)
                """;
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idProducto);
                ps.setInt(2, idImpuesto);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {
        }
    }

    public ConfiguracionFiscal obtenerConfiguracionFiscal() {
        ConfiguracionFiscal config = new ConfiguracionFiscal();
        String sql = """
                SELECT id_config, rfc_negocio, razon_social, regimen_fiscal, codigo_postal_fiscal,
                       region_fiscal, precio_incluye_impuesto, impuesto_por_producto,
                       mostrar_desglose_ticket, impuesto_predeterminado_clave, serie_factura,
                       folio_inicial, modo_facturacion, uso_cfdi_default, metodo_pago_sat,
                       forma_pago_sat
                FROM configuracion_fiscal
                WHERE id_config = 1
                LIMIT 1
                """;
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return config;
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    config.setIdConfig(rs.getInt("id_config"));
                    config.setRfcNegocio(rs.getString("rfc_negocio"));
                    config.setRazonSocial(rs.getString("razon_social"));
                    config.setRegimenFiscal(rs.getString("regimen_fiscal"));
                    config.setCodigoPostalFiscal(rs.getString("codigo_postal_fiscal"));
                    config.setRegionFiscal(rs.getString("region_fiscal"));
                    config.setPrecioIncluyeImpuesto(rs.getBoolean("precio_incluye_impuesto"));
                    config.setImpuestoPorProducto(rs.getBoolean("impuesto_por_producto"));
                    config.setMostrarDesgloseTicket(rs.getBoolean("mostrar_desglose_ticket"));
                    config.setImpuestoPredeterminadoClave(rs.getString("impuesto_predeterminado_clave"));
                    config.setSerieFactura(rs.getString("serie_factura"));
                    config.setFolioInicial(rs.getInt("folio_inicial"));
                    config.setModoFacturacion(rs.getString("modo_facturacion"));
                    config.setUsoCfdiDefault(rs.getString("uso_cfdi_default"));
                    config.setMetodoPagoSat(rs.getString("metodo_pago_sat"));
                    config.setFormaPagoSat(rs.getString("forma_pago_sat"));
                }
            }
        } catch (Exception ignored) {
        }
        return config;
    }

    public void guardarConfiguracionFiscal(ConfiguracionFiscal config) {
        String sql = """
                INSERT INTO configuracion_fiscal (
                    id_config, rfc_negocio, razon_social, regimen_fiscal, codigo_postal_fiscal,
                    region_fiscal, precio_incluye_impuesto, impuesto_por_producto,
                    mostrar_desglose_ticket, impuesto_predeterminado_clave, serie_factura,
                    folio_inicial, modo_facturacion, uso_cfdi_default, metodo_pago_sat, forma_pago_sat
                ) VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    rfc_negocio = VALUES(rfc_negocio),
                    razon_social = VALUES(razon_social),
                    regimen_fiscal = VALUES(regimen_fiscal),
                    codigo_postal_fiscal = VALUES(codigo_postal_fiscal),
                    region_fiscal = VALUES(region_fiscal),
                    precio_incluye_impuesto = VALUES(precio_incluye_impuesto),
                    impuesto_por_producto = VALUES(impuesto_por_producto),
                    mostrar_desglose_ticket = VALUES(mostrar_desglose_ticket),
                    impuesto_predeterminado_clave = VALUES(impuesto_predeterminado_clave),
                    serie_factura = VALUES(serie_factura),
                    folio_inicial = VALUES(folio_inicial),
                    modo_facturacion = VALUES(modo_facturacion),
                    uso_cfdi_default = VALUES(uso_cfdi_default),
                    metodo_pago_sat = VALUES(metodo_pago_sat),
                    forma_pago_sat = VALUES(forma_pago_sat)
                """;
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, config.getRfcNegocio());
                ps.setString(2, config.getRazonSocial());
                ps.setString(3, config.getRegimenFiscal());
                ps.setString(4, config.getCodigoPostalFiscal());
                ps.setString(5, config.getRegionFiscal());
                ps.setBoolean(6, config.isPrecioIncluyeImpuesto());
                ps.setBoolean(7, config.isImpuestoPorProducto());
                ps.setBoolean(8, config.isMostrarDesgloseTicket());
                ps.setString(9, config.getImpuestoPredeterminadoClave());
                ps.setString(10, config.getSerieFactura());
                ps.setInt(11, config.getFolioInicial());
                ps.setString(12, config.getModoFacturacion());
                ps.setString(13, config.getUsoCfdiDefault());
                ps.setString(14, config.getMetodoPagoSat());
                ps.setString(15, config.getFormaPagoSat());
                ps.executeUpdate();
            }
        } catch (Exception ignored) {
        }
    }

    public void registrarAuditoriaFiscal(String accion, String tabla, int idRegistro, String detalle, int idUsuario) {
        String sql = """
                INSERT INTO auditoria_fiscal (id_usuario, accion, tabla_afectada, id_registro, detalle)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                ps.setString(2, accion);
                ps.setString(3, tabla);
                ps.setInt(4, idRegistro);
                ps.setString(5, detalle);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {
        }
    }

    private Optional<Impuesto> obtenerPrimerPredeterminado() {
        String sql = """
                SELECT id_impuesto, clave, nombre, tipo, tasa, activo, predeterminado
                FROM impuestos
                WHERE activo = 1
                ORDER BY predeterminado DESC, id_impuesto
                LIMIT 1
                """;
        try (Connection con = ConexionDB.getConexion()) {
            if (con == null) return Optional.empty();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapImpuesto(rs));
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    private Impuesto mapImpuesto(ResultSet rs) throws Exception {
        BigDecimal tasa = rs.getBigDecimal("tasa");
        return new Impuesto(
                rs.getInt("id_impuesto"),
                rs.getString("clave"),
                rs.getString("nombre"),
                rs.getString("tipo"),
                tasa == null ? BigDecimal.ZERO : tasa,
                rs.getBoolean("activo"),
                rs.getBoolean("predeterminado")
        );
    }
}
