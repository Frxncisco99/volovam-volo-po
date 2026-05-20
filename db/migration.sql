-- Punto unico de migracion para bases existentes de Volovan Volo POS.
-- Ejecutar desde el cliente mysql en la raiz del proyecto:
-- SOURCE db/migration.sql;

SOURCE db/migracion_fiscal_financiera.sql;
SOURCE db/migracion_seguridad_permisos.sql;
SOURCE db/migracion_hardening_operativo.sql;
SOURCE db/migracion_catalogo_web.sql;
