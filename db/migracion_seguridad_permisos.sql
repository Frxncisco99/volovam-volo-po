-- Migracion segura de seguridad, permisos, pagos mixtos y auditoria avanzada.
-- Ejecutar sobre la base existente. No elimina datos.

CREATE TABLE IF NOT EXISTS permisos (
    id_permiso INT NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(80) NOT NULL,
    nombre VARCHAR(140) NOT NULL,
    modulo VARCHAR(60) NOT NULL,
    descripcion VARCHAR(255) DEFAULT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id_permiso),
    UNIQUE KEY uk_permisos_codigo (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE IF NOT EXISTS usuario_permisos (
    id_usuario INT NOT NULL,
    id_permiso INT NOT NULL,
    otorgado_por INT DEFAULT NULL,
    fecha_asignacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_permiso),
    KEY idx_usuario_permisos_usuario (id_usuario),
    KEY idx_usuario_permisos_permiso (id_permiso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE IF NOT EXISTS rol_permisos (
    id_rol INT NOT NULL,
    id_permiso INT NOT NULL,
    PRIMARY KEY (id_rol, id_permiso),
    KEY idx_rol_permisos_rol (id_rol),
    KEY idx_rol_permisos_permiso (id_permiso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE IF NOT EXISTS detalle_pago (
    id_detalle_pago INT NOT NULL AUTO_INCREMENT,
    id_venta INT NOT NULL,
    metodo_pago VARCHAR(40) NOT NULL,
    monto DECIMAL(12,2) NOT NULL DEFAULT 0,
    referencia VARCHAR(120) DEFAULT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_detalle_pago),
    KEY idx_detalle_pago_venta (id_venta),
    KEY idx_detalle_pago_metodo (metodo_pago),
    KEY idx_detalle_pago_fecha (fecha_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE IF NOT EXISTS auditoria_detalle (
    id_auditoria_detalle INT NOT NULL AUTO_INCREMENT,
    id_usuario INT DEFAULT NULL,
    id_admin_autorizo INT DEFAULT NULL,
    accion VARCHAR(80) NOT NULL,
    modulo VARCHAR(80) DEFAULT NULL,
    tabla_afectada VARCHAR(80) DEFAULT NULL,
    id_registro INT DEFAULT NULL,
    descripcion TEXT DEFAULT NULL,
    datos_antes TEXT DEFAULT NULL,
    datos_despues TEXT DEFAULT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_auditoria_detalle),
    KEY idx_auditoria_detalle_fecha (fecha_hora),
    KEY idx_auditoria_detalle_usuario (id_usuario),
    KEY idx_auditoria_detalle_admin (id_admin_autorizo)
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
CREATE PROCEDURE add_index_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_index_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND INDEX_NAME = p_index_name
    ) THEN
        SET @ddl = CONCAT('CREATE INDEX `', p_index_name, '` ON `', p_table_name, '` ', p_index_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_column_if_missing('usuarios', 'password_hash', 'VARCHAR(255) DEFAULT NULL');
CALL add_column_if_missing('usuarios', 'es_admin_principal', 'TINYINT(1) NOT NULL DEFAULT 0');
CALL add_column_if_missing('usuarios', 'fecha_actualizacion_password', 'DATETIME DEFAULT NULL');
CALL add_column_if_missing('ventas', 'fecha_hora', 'DATETIME DEFAULT NULL');
CALL add_column_if_missing('ventas', 'fecha_cancelacion', 'DATETIME DEFAULT NULL');
CALL add_column_if_missing('ventas', 'motivo_cancelacion', 'VARCHAR(255) DEFAULT NULL');
CALL add_column_if_missing('ventas', 'id_admin_autorizo_cancelacion', 'INT DEFAULT NULL');
CALL add_column_if_missing('corte_caja', 'conteo_denominaciones', 'TEXT DEFAULT NULL');

UPDATE usuarios
SET es_admin_principal = 1
WHERE id_usuario = 1 OR LOWER(usuario) = 'admin';

UPDATE ventas
SET fecha_hora = fecha
WHERE fecha_hora IS NULL;

INSERT IGNORE INTO permisos (codigo, nombre, modulo, descripcion) VALUES
('VENTAS_ACCEDER', 'Acceder a ventas', 'Ventas', 'Permite abrir el modulo de ventas.'),
('VENTAS_CANCELAR', 'Cancelar ventas', 'Ventas', 'Permite cancelar ventas finalizadas.'),
('VENTAS_ELIMINAR_PRODUCTO_CARRITO', 'Eliminar producto del carrito', 'Ventas', 'Permite retirar lineas del carrito.'),
('VENTAS_APLICAR_DESCUENTO', 'Aplicar descuento', 'Ventas', 'Permite aplicar descuentos.'),
('VENTAS_COBRAR', 'Cobrar ventas', 'Ventas', 'Permite cobrar ventas.'),
('VENTAS_DEVOLVER', 'Procesar devoluciones', 'Ventas', 'Permite realizar devoluciones.'),
('PRODUCTOS_ACCEDER', 'Acceder a productos', 'Productos', 'Permite ver productos e inventario.'),
('PRODUCTOS_CREAR', 'Crear productos', 'Productos', 'Permite crear productos.'),
('PRODUCTOS_EDITAR', 'Editar productos', 'Productos', 'Permite modificar productos.'),
('PRODUCTOS_ELIMINAR', 'Eliminar productos', 'Productos', 'Permite desactivar productos.'),
('INVENTARIO_AJUSTAR', 'Ajustar inventario', 'Inventario', 'Permite ajustes manuales de stock.'),
('INVENTARIO_VER_COSTOS', 'Ver costos', 'Inventario', 'Permite consultar costos y margen.'),
('CAJA_ACCEDER', 'Acceder a caja', 'Caja', 'Permite ver caja y corte.'),
('CAJA_ABRIR', 'Abrir caja', 'Caja', 'Permite abrir caja.'),
('CAJA_CERRAR', 'Cerrar caja', 'Caja', 'Permite cerrar caja.'),
('CAJA_HACER_RETIRO', 'Hacer retiro', 'Caja', 'Permite registrar salidas de efectivo.'),
('CAJA_HACER_INGRESO', 'Hacer ingreso', 'Caja', 'Permite registrar ingresos de efectivo.'),
('REPORTES_ACCEDER', 'Acceder a reportes', 'Reportes', 'Permite consultar reportes.'),
('REPORTES_EXPORTAR_PDF', 'Exportar PDF', 'Reportes', 'Permite exportar reportes a PDF.'),
('CLIENTES_ACCEDER', 'Acceder a clientes', 'Clientes', 'Permite consultar clientes.'),
('CLIENTES_CREAR', 'Crear clientes', 'Clientes', 'Permite crear clientes.'),
('CLIENTES_EDITAR', 'Editar clientes', 'Clientes', 'Permite editar clientes.'),
('CLIENTES_CREDITO', 'Gestionar credito', 'Clientes', 'Permite modificar credito y abonos.'),
('USUARIOS_ACCEDER', 'Acceder a usuarios', 'Usuarios', 'Permite consultar usuarios.'),
('USUARIOS_CREAR', 'Crear usuarios', 'Usuarios', 'Permite crear usuarios.'),
('USUARIOS_EDITAR', 'Editar usuarios', 'Usuarios', 'Permite editar usuarios.'),
('USUARIOS_DESACTIVAR', 'Desactivar usuarios', 'Usuarios', 'Permite desactivar usuarios.'),
('PERMISOS_GESTIONAR', 'Gestionar permisos', 'Permisos', 'Permite asignar permisos.'),
('CONFIGURACION_ACCEDER', 'Acceder a configuracion', 'Configuracion', 'Permite abrir configuracion.');

INSERT IGNORE INTO rol_permisos (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM roles r
JOIN permisos p
WHERE LOWER(r.nombre) = 'admin';

INSERT IGNORE INTO rol_permisos (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM roles r
JOIN permisos p ON p.codigo IN (
    'VENTAS_ACCEDER', 'VENTAS_COBRAR', 'VENTAS_DEVOLVER',
    'PRODUCTOS_ACCEDER', 'INVENTARIO_AJUSTAR', 'CAJA_ACCEDER',
    'CAJA_CERRAR', 'REPORTES_ACCEDER', 'CLIENTES_ACCEDER'
)
WHERE LOWER(r.nombre) = 'supervisor';

INSERT IGNORE INTO rol_permisos (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM roles r
JOIN permisos p ON p.codigo IN ('VENTAS_ACCEDER', 'VENTAS_COBRAR', 'CLIENTES_ACCEDER')
WHERE LOWER(r.nombre) = 'cajero';

INSERT IGNORE INTO usuario_permisos (id_usuario, id_permiso, otorgado_por)
SELECT u.id_usuario, p.id_permiso, u.id_usuario
FROM usuarios u
JOIN permisos p
WHERE u.es_admin_principal = 1;

CALL add_index_if_missing('ventas', 'idx_ventas_fecha_hora', '(fecha_hora)');
CALL add_index_if_missing('ventas', 'idx_ventas_estado', '(estado)');
CALL add_index_if_missing('movimientos_inventario', 'idx_mov_inv_producto', '(id_producto)');
CALL add_index_if_missing('auditoria', 'idx_auditoria_fecha', '(fecha)');

DROP PROCEDURE add_index_if_missing;
DROP PROCEDURE add_column_if_missing;
