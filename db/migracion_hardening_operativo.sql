-- Hardening operativo seguro para Volovan Volo POS
-- Ejecutar despues de migracion_seguridad_permisos.sql y migracion_fiscal_financiera.sql.
-- No elimina ventas ni movimientos. Corrige duplicados tecnicos y bloquea cuentas legacy inseguras.

DELIMITER $$

CREATE PROCEDURE drop_index_if_exists(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND INDEX_NAME = p_index_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` DROP INDEX `', p_index_name, '`');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE drop_fk_if_exists(
    IN p_table_name VARCHAR(64),
    IN p_fk_name VARCHAR(64)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND CONSTRAINT_NAME = p_fk_name
          AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` DROP FOREIGN KEY `', p_fk_name, '`');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE add_column_if_missing_hardening(
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

-- Quita duplicados conocidos del dump original.
CALL drop_fk_if_exists('detalle_devolucion', 'detalle_devolucion_ibfk_4');
CALL drop_index_if_exists('movimientos_inventario', 'idx_mov_inv_producto');
CALL drop_index_if_exists('auditoria', 'idx_auditoria_fecha');

-- Limpia contrasena plana cuando ya existe hash.
UPDATE usuarios
SET contrasena = ''
WHERE COALESCE(password_hash, '') <> ''
  AND COALESCE(contrasena, '') <> '';

-- Bloquea cuentas legacy sin hash para que un admin las reactive cambiando clave.
-- Esto evita dejar usuarios productivos con contrasena visible en dump.
UPDATE usuarios
SET activo = 0
WHERE COALESCE(password_hash, '') = ''
  AND COALESCE(contrasena, '') <> '';

-- Marca ventas historicas sin desglose como legacy no fiscal.
-- No inventa IVA; las deja conciliables sin romper totales.
UPDATE ventas
SET subtotal = total,
    descuento = 0,
    iva = 0,
    ieps = 0,
    impuestos = 0,
    total_gravado = 0,
    total_exento = total,
    total_tasa0 = 0
WHERE COALESCE(subtotal, 0) = 0
  AND total > 0;

-- Evita motivos basura en cancelaciones nuevas sin cambiar historico.
ALTER TABLE cancelaciones
    MODIFY motivo VARCHAR(255) NOT NULL;

-- Persistencia de ventas en espera. Antes vivian solo en memoria del controlador.
CREATE TABLE IF NOT EXISTS ventas_en_espera (
    id_espera INT NOT NULL AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    id_caja INT NOT NULL,
    id_cliente INT DEFAULT 1,
    nombre_cliente VARCHAR(180) DEFAULT 'Publico General',
    limite_credito DECIMAL(12,2) NOT NULL DEFAULT 0,
    saldo_cliente DECIMAL(12,2) NOT NULL DEFAULT 0,
    etiqueta VARCHAR(120) NOT NULL,
    carrito_data LONGTEXT NOT NULL,
    total DECIMAL(12,2) NOT NULL DEFAULT 0,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id_espera),
    KEY idx_ventas_espera_usuario_caja (id_usuario, id_caja),
    KEY idx_ventas_espera_fecha (fecha_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- Datos faltantes para productos/clientes sin romper vistas actuales.
CALL add_column_if_missing_hardening('productos', 'descripcion', 'TEXT NULL');
CALL add_column_if_missing_hardening('productos', 'imagen_url', 'VARCHAR(255) DEFAULT NULL');
CALL add_column_if_missing_hardening('productos', 'unidad_medida', 'VARCHAR(30) NOT NULL DEFAULT ''pieza''');
CALL add_column_if_missing_hardening('clientes', 'email', 'VARCHAR(150) DEFAULT NULL');

-- Base profesional para proveedores y compras.
CREATE TABLE IF NOT EXISTS proveedores (
    id_proveedor INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(180) NOT NULL,
    rfc VARCHAR(13) DEFAULT NULL,
    telefono VARCHAR(20) DEFAULT NULL,
    email VARCHAR(150) DEFAULT NULL,
    direccion VARCHAR(255) DEFAULT NULL,
    contacto VARCHAR(120) DEFAULT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id_proveedor),
    KEY idx_proveedores_nombre (nombre),
    KEY idx_proveedores_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE IF NOT EXISTS compras (
    id_compra INT NOT NULL AUTO_INCREMENT,
    id_proveedor INT DEFAULT NULL,
    id_usuario INT DEFAULT NULL,
    folio VARCHAR(40) DEFAULT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    impuestos DECIMAL(12,2) NOT NULL DEFAULT 0,
    total DECIMAL(12,2) NOT NULL DEFAULT 0,
    estado VARCHAR(20) NOT NULL DEFAULT 'REGISTRADA',
    observaciones VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id_compra),
    KEY idx_compras_fecha (fecha_hora),
    KEY idx_compras_proveedor (id_proveedor),
    KEY idx_compras_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE IF NOT EXISTS compra_detalle (
    id_detalle INT NOT NULL AUTO_INCREMENT,
    id_compra INT NOT NULL,
    id_producto INT DEFAULT NULL,
    descripcion VARCHAR(200) NOT NULL,
    cantidad DECIMAL(12,3) NOT NULL DEFAULT 0,
    costo_unitario DECIMAL(12,2) NOT NULL DEFAULT 0,
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (id_detalle),
    KEY idx_compra_detalle_compra (id_compra),
    KEY idx_compra_detalle_producto (id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE IF NOT EXISTS producto_proveedor (
    id_producto INT NOT NULL,
    id_proveedor INT NOT NULL,
    sku_proveedor VARCHAR(80) DEFAULT NULL,
    costo_ultimo DECIMAL(12,2) NOT NULL DEFAULT 0,
    proveedor_principal TINYINT(1) NOT NULL DEFAULT 0,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id_producto, id_proveedor),
    KEY idx_producto_proveedor_proveedor (id_proveedor),
    KEY idx_producto_proveedor_principal (id_producto, proveedor_principal)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- Turnos para cortes parciales por cajero.
CREATE TABLE IF NOT EXISTS turnos (
    id_turno INT NOT NULL AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    id_caja INT DEFAULT NULL,
    fecha_apertura DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre DATETIME DEFAULT NULL,
    fondo_inicial DECIMAL(12,2) NOT NULL DEFAULT 0,
    efectivo_esperado DECIMAL(12,2) NOT NULL DEFAULT 0,
    efectivo_contado DECIMAL(12,2) NOT NULL DEFAULT 0,
    diferencia DECIMAL(12,2) NOT NULL DEFAULT 0,
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTO',
    observaciones VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id_turno),
    KEY idx_turnos_usuario (id_usuario),
    KEY idx_turnos_caja (id_caja),
    KEY idx_turnos_estado (estado),
    KEY idx_turnos_apertura (fecha_apertura)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- Promociones/descuentos controlados. No sustituye el flujo actual, queda listo para reglas.
CREATE TABLE IF NOT EXISTS promociones (
    id_promocion INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    tipo VARCHAR(30) NOT NULL DEFAULT 'PORCENTAJE',
    valor DECIMAL(12,4) NOT NULL DEFAULT 0,
    fecha_inicio DATETIME DEFAULT NULL,
    fecha_fin DATETIME DEFAULT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    requiere_permiso VARCHAR(80) DEFAULT 'VENTAS_APLICAR_DESCUENTO',
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_promocion),
    KEY idx_promociones_activo (activo),
    KEY idx_promociones_vigencia (fecha_inicio, fecha_fin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE IF NOT EXISTS promocion_productos (
    id_promocion INT NOT NULL,
    id_producto INT NOT NULL,
    PRIMARY KEY (id_promocion, id_producto),
    KEY idx_promocion_productos_producto (id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE IF NOT EXISTS motivos_cancelacion (
    id_motivo INT NOT NULL AUTO_INCREMENT,
    clave VARCHAR(40) NOT NULL,
    descripcion VARCHAR(160) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id_motivo),
    UNIQUE KEY uk_motivos_cancelacion_clave (clave)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

INSERT IGNORE INTO motivos_cancelacion (clave, descripcion) VALUES
('ERROR_CAPTURA', 'Error de captura'),
('CLIENTE_CANCELO', 'Cliente cancelo la compra'),
('PRODUCTO_EQUIVOCADO', 'Producto equivocado'),
('FORMA_PAGO_EQUIVOCADA', 'Forma de pago equivocada');

INSERT IGNORE INTO configuracion (clave, valor) VALUES
('cancelacion_limite_horas', '24');

-- Normaliza pagos historicos hacia detalle_pago cuando aun solo existen en pagos.
-- Para MIXTO historico no hay desglose confiable, por eso se conserva como MIXTO_LEGACY.
INSERT INTO detalle_pago (id_venta, metodo_pago, monto, referencia, fecha_hora)
SELECT p.id_venta,
       CASE WHEN p.tipo_pago = 'MIXTO' THEN 'MIXTO_LEGACY' ELSE p.tipo_pago END,
       COALESCE(v.total, p.monto_recibido - p.cambio, p.monto_recibido),
       CASE WHEN p.tipo_pago = 'MIXTO' THEN 'Migrado desde pagos sin desglose original' ELSE 'Migrado desde pagos' END,
       COALESCE(v.fecha_hora, v.fecha, NOW())
FROM pagos p
LEFT JOIN ventas v ON v.id_venta = p.id_venta
WHERE NOT EXISTS (
    SELECT 1
    FROM detalle_pago dp
    WHERE dp.id_venta = p.id_venta
);

INSERT INTO detalle_pago (id_venta, metodo_pago, monto, referencia, fecha_hora)
SELECT v.id_venta,
       'CREDITO',
       v.total,
       'Migrado desde venta a credito',
       COALESCE(v.fecha_hora, v.fecha, NOW())
FROM ventas v
WHERE UPPER(COALESCE(v.metodo_pago, '')) IN ('FIADO', 'CREDITO')
  AND NOT EXISTS (
      SELECT 1
      FROM detalle_pago dp
      WHERE dp.id_venta = v.id_venta
  );

DROP PROCEDURE drop_index_if_exists;
DROP PROCEDURE drop_fk_if_exists;
DROP PROCEDURE add_column_if_missing_hardening;
