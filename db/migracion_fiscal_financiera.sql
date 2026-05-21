-- Migracion fiscal/financiera segura para Volovan Volo POS
-- Ejecutar sobre la base existente. No elimina datos.

CREATE TABLE IF NOT EXISTS impuestos (
    id_impuesto INT NOT NULL AUTO_INCREMENT,
    clave VARCHAR(40) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    tasa DECIMAL(9,6) NOT NULL DEFAULT 0,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    predeterminado TINYINT(1) NOT NULL DEFAULT 0,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id_impuesto),
    UNIQUE KEY uk_impuestos_clave (clave)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

INSERT IGNORE INTO impuestos (clave, nombre, tipo, tasa, activo, predeterminado)
VALUES
    ('SIN_IMPUESTO', 'Sin impuesto', 'SIN_IMPUESTO', 0, 1, 0),
    ('EXENTO', 'Exento', 'EXENTO', 0, 1, 0),
    ('TASA_0', 'Tasa 0', 'TASA_0', 0, 1, 0),
    ('IVA_8', 'IVA frontera', 'IVA', 0.080000, 1, 0),
    ('IVA_16', 'IVA general', 'IVA', 0.160000, 1, 1),
    ('IEPS', 'IEPS configurable', 'IEPS', 0.080000, 1, 0);

CREATE TABLE IF NOT EXISTS producto_impuesto (
    id_producto INT NOT NULL,
    id_impuesto INT NOT NULL,
    actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id_producto),
    KEY idx_producto_impuesto_impuesto (id_impuesto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE IF NOT EXISTS configuracion_fiscal (
    id_config INT NOT NULL PRIMARY KEY DEFAULT 1,
    rfc_negocio VARCHAR(13) DEFAULT '',
    razon_social VARCHAR(200) DEFAULT '',
    regimen_fiscal VARCHAR(120) DEFAULT '601 - General de Ley Personas Morales',
    codigo_postal_fiscal VARCHAR(10) DEFAULT '',
    region_fiscal VARCHAR(20) DEFAULT 'GENERAL',
    precio_incluye_impuesto TINYINT(1) NOT NULL DEFAULT 1,
    impuesto_por_producto TINYINT(1) NOT NULL DEFAULT 1,
    mostrar_desglose_ticket TINYINT(1) NOT NULL DEFAULT 1,
    impuesto_predeterminado_clave VARCHAR(40) DEFAULT 'IVA_16',
    serie_factura VARCHAR(10) DEFAULT 'A',
    folio_inicial INT NOT NULL DEFAULT 1,
    modo_facturacion VARCHAR(30) DEFAULT 'PREFACTURA',
    uso_cfdi_default VARCHAR(120) DEFAULT 'G03 - Gastos en general',
    metodo_pago_sat VARCHAR(120) DEFAULT 'PUE - Pago en una sola exhibicion',
    forma_pago_sat VARCHAR(120) DEFAULT '01 - Efectivo',
    actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

INSERT IGNORE INTO configuracion_fiscal (id_config) VALUES (1);

CREATE TABLE IF NOT EXISTS facturas (
    id_factura INT NOT NULL AUTO_INCREMENT,
    id_venta INT NOT NULL,
    serie VARCHAR(10) NOT NULL DEFAULT 'A',
    folio INT NOT NULL,
    folio_interno VARCHAR(40) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    modo VARCHAR(30) NOT NULL DEFAULT 'PREFACTURA',
    uuid VARCHAR(80) DEFAULT NULL,
    rfc_emisor VARCHAR(13) DEFAULT '',
    razon_social_emisor VARCHAR(200) DEFAULT '',
    regimen_fiscal_emisor VARCHAR(120) DEFAULT '',
    codigo_postal_emisor VARCHAR(10) DEFAULT '',
    rfc_receptor VARCHAR(13) DEFAULT 'XAXX010101000',
    razon_social_receptor VARCHAR(200) DEFAULT 'Publico General',
    regimen_fiscal_receptor VARCHAR(120) DEFAULT '',
    codigo_postal_receptor VARCHAR(10) DEFAULT '',
    uso_cfdi VARCHAR(120) DEFAULT '',
    metodo_pago_sat VARCHAR(120) DEFAULT '',
    forma_pago_sat VARCHAR(120) DEFAULT '',
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    descuento DECIMAL(12,2) NOT NULL DEFAULT 0,
    iva DECIMAL(12,2) NOT NULL DEFAULT 0,
    ieps DECIMAL(12,2) NOT NULL DEFAULT 0,
    impuestos DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_gravado DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_exento DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_tasa0 DECIMAL(12,2) NOT NULL DEFAULT 0,
    total DECIMAL(12,2) NOT NULL DEFAULT 0,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cancelacion DATETIME DEFAULT NULL,
    motivo_cancelacion VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id_factura),
    UNIQUE KEY uk_facturas_serie_folio (serie, folio),
    UNIQUE KEY uk_facturas_venta (id_venta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE IF NOT EXISTS factura_detalle (
    id_detalle INT NOT NULL AUTO_INCREMENT,
    id_factura INT NOT NULL,
    id_producto INT DEFAULT NULL,
    descripcion VARCHAR(200) NOT NULL,
    cantidad DECIMAL(12,3) NOT NULL DEFAULT 0,
    precio_unitario DECIMAL(12,2) NOT NULL DEFAULT 0,
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    descuento DECIMAL(12,2) NOT NULL DEFAULT 0,
    impuesto_clave VARCHAR(40) DEFAULT NULL,
    impuesto_tipo VARCHAR(30) DEFAULT NULL,
    impuesto_tasa DECIMAL(9,6) NOT NULL DEFAULT 0,
    impuesto_importe DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_linea DECIMAL(12,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (id_detalle),
    KEY idx_factura_detalle_factura (id_factura)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE IF NOT EXISTS auditoria_fiscal (
    id_auditoria INT NOT NULL AUTO_INCREMENT,
    id_usuario INT DEFAULT NULL,
    accion VARCHAR(60) NOT NULL,
    tabla_afectada VARCHAR(60) DEFAULT NULL,
    id_registro INT DEFAULT NULL,
    detalle TEXT DEFAULT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_auditoria),
    KEY idx_auditoria_fiscal_fecha (fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

DELIMITER $$
CREATE PROCEDURE add_column_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND COLUMN_NAME = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN `', p_column_name, '` ', p_column_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_column_if_missing('clientes', 'rfc', 'VARCHAR(13) DEFAULT NULL');
CALL add_column_if_missing('clientes', 'razon_social', 'VARCHAR(200) DEFAULT NULL');
CALL add_column_if_missing('clientes', 'regimen_fiscal', 'VARCHAR(120) DEFAULT NULL');
CALL add_column_if_missing('clientes', 'uso_cfdi_default', 'VARCHAR(120) DEFAULT NULL');
CALL add_column_if_missing('clientes', 'codigo_postal_fiscal', 'VARCHAR(10) DEFAULT NULL');
CALL add_column_if_missing('clientes', 'correo_facturacion', 'VARCHAR(150) DEFAULT NULL');

CALL add_column_if_missing('ventas', 'subtotal', 'DECIMAL(12,2) NOT NULL DEFAULT 0');
CALL add_column_if_missing('ventas', 'descuento', 'DECIMAL(12,2) NOT NULL DEFAULT 0');
CALL add_column_if_missing('ventas', 'iva', 'DECIMAL(12,2) NOT NULL DEFAULT 0');
CALL add_column_if_missing('ventas', 'ieps', 'DECIMAL(12,2) NOT NULL DEFAULT 0');
CALL add_column_if_missing('ventas', 'impuestos', 'DECIMAL(12,2) NOT NULL DEFAULT 0');
CALL add_column_if_missing('ventas', 'total_gravado', 'DECIMAL(12,2) NOT NULL DEFAULT 0');
CALL add_column_if_missing('ventas', 'total_exento', 'DECIMAL(12,2) NOT NULL DEFAULT 0');
CALL add_column_if_missing('ventas', 'total_tasa0', 'DECIMAL(12,2) NOT NULL DEFAULT 0');
CALL add_column_if_missing('ventas', 'estado_facturacion', 'VARCHAR(20) NOT NULL DEFAULT ''NO_FACTURADA''');
CALL add_column_if_missing('ventas', 'factura_id', 'INT DEFAULT NULL');

CALL add_column_if_missing('detalle_venta', 'impuesto_id', 'INT DEFAULT NULL');
CALL add_column_if_missing('detalle_venta', 'impuesto_clave', 'VARCHAR(40) DEFAULT NULL');
CALL add_column_if_missing('detalle_venta', 'impuesto_nombre', 'VARCHAR(120) DEFAULT NULL');
CALL add_column_if_missing('detalle_venta', 'impuesto_tipo', 'VARCHAR(30) DEFAULT NULL');
CALL add_column_if_missing('detalle_venta', 'impuesto_tasa', 'DECIMAL(9,6) NOT NULL DEFAULT 0');
CALL add_column_if_missing('detalle_venta', 'subtotal_sin_impuesto', 'DECIMAL(12,2) NOT NULL DEFAULT 0');
CALL add_column_if_missing('detalle_venta', 'descuento', 'DECIMAL(12,2) NOT NULL DEFAULT 0');
CALL add_column_if_missing('detalle_venta', 'impuesto_importe', 'DECIMAL(12,2) NOT NULL DEFAULT 0');
CALL add_column_if_missing('detalle_venta', 'total_linea', 'DECIMAL(12,2) NOT NULL DEFAULT 0');

CALL add_column_if_missing('factura_detalle', 'descuento', 'DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER subtotal');

DROP PROCEDURE add_column_if_missing;
