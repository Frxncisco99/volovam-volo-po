-- Configuracion local para Catalogo Web / Supabase.
-- No reemplaza la base local del POS. Solo guarda parametros y cola de sincronizacion.

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

INSERT IGNORE INTO configuracion_web (id, estado_conexion)
VALUES (1, 'SIN_CONEXION');

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;
