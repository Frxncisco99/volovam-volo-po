-- Configuracion general usada por el POS.
-- Debe existir antes de guardar preferencias, email, ticket y reglas de cancelacion.

CREATE TABLE IF NOT EXISTS configuracion (
    clave VARCHAR(100) PRIMARY KEY,
    valor TEXT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

INSERT IGNORE INTO configuracion (clave, valor) VALUES
('negocio_nombre', ''),
('negocio_slogan', ''),
('negocio_telefono', ''),
('negocio_direccion', ''),
('negocio_ciudad', ''),
('negocio_cp', ''),
('negocio_correo', ''),
('negocio_web', ''),
('negocio_rfc', ''),
('ticket_nombre', 'Volovan Volo'),
('ticket_giro', 'Panaderia y Reposteria'),
('ticket_direccion', ''),
('ticket_ciudad', ''),
('ticket_telefono', ''),
('ticket_encabezado', 'Bienvenido!\\nGracias por visitarnos.'),
('ticket_pie', 'Gracias por su compra!\\nVuelva pronto.\\nfacebook.com/VolovanVolo'),
('ticket_aviso', 'Este ticket no es comprobante fiscal'),
('ticket_logo', 'true'),
('ticket_folio', 'true'),
('ticket_desglose', 'true'),
('ticket_qr', 'false'),
('ticket_ancho', '58 mm'),
('cmbImpresora', ''),
('cmbAnchoPapel', '58 mm'),
('cajon_activo', 'false'),
('cajon_puerto', 'Via impresora termica (ESC/POS)'),
('cajon_pulso', 'Pulso 1 (pin 2)'),
('email_activo', 'false'),
('email_smtp', 'Gmail'),
('email_remitente', ''),
('email_password', ''),
('email_host', ''),
('email_puerto', ''),
('email_reporte_diario', 'false');
