# Corte de Caja: arquitectura recomendada

Este modulo esta implementado en JavaFX con JDBC. La estructura queda preparada para crecer hacia una API REST o una capa JPA sin cambiar la pantalla.

## Tablas principales

```sql
CREATE TABLE caja (
  id_caja INT AUTO_INCREMENT PRIMARY KEY,
  fecha_apertura DATETIME NOT NULL,
  fecha_cierre DATETIME NULL,
  monto_inicial DECIMAL(12,2) NOT NULL DEFAULT 0,
  monto_final DECIMAL(12,2) NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'abierta',
  id_usuario INT NOT NULL,
  tipo_cambio_dolar DECIMAL(10,4) NULL,
  INDEX idx_caja_estado (estado),
  INDEX idx_caja_usuario (id_usuario)
);

CREATE TABLE corte_caja (
  id_corte INT AUTO_INCREMENT PRIMARY KEY,
  id_caja INT NOT NULL,
  id_usuario INT NOT NULL,
  fecha_apertura DATETIME NOT NULL,
  fecha_cierre DATETIME NOT NULL,
  fondo_inicial DECIMAL(12,2) NOT NULL DEFAULT 0,
  total_ventas DECIMAL(12,2) NOT NULL DEFAULT 0,
  total_entradas DECIMAL(12,2) NOT NULL DEFAULT 0,
  total_salidas DECIMAL(12,2) NOT NULL DEFAULT 0,
  dinero_esperado DECIMAL(12,2) NOT NULL DEFAULT 0,
  dinero_real DECIMAL(12,2) NOT NULL DEFAULT 0,
  diferencia DECIMAL(12,2) NOT NULL DEFAULT 0,
  observaciones TEXT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'CERRADO',
  INDEX idx_corte_fecha (fecha_cierre),
  INDEX idx_corte_caja (id_caja)
);

CREATE TABLE movimientos_caja (
  id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
  id_caja INT NOT NULL,
  tipo VARCHAR(20) NOT NULL,
  monto DECIMAL(12,2) NOT NULL,
  motivo VARCHAR(255) NOT NULL,
  id_usuario INT NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_mov_caja_fecha (id_caja, fecha)
);
```

## Entidades/modelos

- `Caja`: apertura, cierre, fondo inicial, monto final y estado operativo.
- `CorteCaja`: snapshot financiero cerrado de una caja.
- `MovimientoCaja`: entradas y salidas manuales de efectivo.
- `Venta`, `Pago`, `DetalleVenta`: ventas y desglose por metodo de pago/producto.
- `Cancelacion`, `Devolucion`: ajustes autorizados que impactan el reporte.
- `CorteCajaReporte`: DTO de lectura para la pantalla, PDF y futuros endpoints.

## Relaciones

- `caja 1--N ventas`
- `caja 1--N movimientos_caja`
- `caja 1--1 corte_caja` por ciclo abierto/cerrado
- `usuarios 1--N caja`, `usuarios 1--N corte_caja`
- `ventas 1--N detalle_venta`
- `ventas 1--1 pagos`
- `ventas 1--N cancelaciones/devoluciones`

## Controladores/endpoints sugeridos si migras a REST

```http
GET  /api/cajas/{idCaja}/corte-actual
POST /api/cajas/{idCaja}/corte
GET  /api/cortes?inicio=2026-05-01&fin=2026-05-13
GET  /api/cortes/{idCorte}/pdf
POST /api/cajas/{idCaja}/movimientos
```

En esta app el equivalente esta en:

- `CorteCajaController`: orquesta UI, conteo fisico, filtros y cierre.
- `CorteCajaDAO`: consultas SQL, historial y registro transaccional.
- `ReportePDFService`: exportacion profesional del corte.

## Consultas clave

Ventas por metodo de pago:

```sql
SELECT CASE
         WHEN COALESCE(pg.tipo_pago, v.metodo_pago) IN ('EFECTIVO', 'DOLARES', 'MIXTO', 'MIXTO_USD') THEN 'Efectivo'
         WHEN COALESCE(pg.tipo_pago, v.metodo_pago) = 'TARJETA' THEN 'Tarjeta'
         WHEN COALESCE(pg.tipo_pago, v.metodo_pago) = 'TRANSFERENCIA' THEN 'Transferencia'
         WHEN COALESCE(pg.tipo_pago, v.metodo_pago) = 'QR' THEN 'QR'
         WHEN COALESCE(pg.tipo_pago, v.metodo_pago) IN ('FIADO', 'CREDITO') THEN 'Credito'
         ELSE COALESCE(pg.tipo_pago, v.metodo_pago, 'Otro')
       END AS metodo,
       COUNT(v.id_venta) AS cantidad,
       COALESCE(SUM(v.total), 0) AS total
FROM ventas v
LEFT JOIN pagos pg ON pg.id_venta = v.id_venta
WHERE v.id_caja = ?
  AND COALESCE(v.estado, 'COMPLETADA') NOT IN ('CANCELADA')
GROUP BY metodo;
```

Productos mas vendidos:

```sql
SELECT p.nombre,
       SUM(dv.cantidad) AS cantidad,
       SUM(dv.subtotal) AS ingresos,
       SUM(dv.cantidad * COALESCE(p.costo, 0)) AS costo
FROM detalle_venta dv
JOIN ventas v ON v.id_venta = dv.id_venta
JOIN productos p ON p.id_producto = dv.id_producto
WHERE v.id_caja = ?
  AND COALESCE(v.estado, 'COMPLETADA') NOT IN ('CANCELADA')
GROUP BY p.id_producto, p.nombre
ORDER BY cantidad DESC, ingresos DESC
LIMIT 10;
```

## Buenas practicas para crecer

- Mantener el cierre de caja en una transaccion: actualizar `caja` e insertar `corte_caja` juntos.
- Guardar el corte como snapshot, no recalcular cortes historicos desde ventas cambiantes.
- Indexar fechas, `id_caja`, `id_usuario` y estados.
- Registrar auditoria en cierre, cancelaciones, devoluciones y movimientos de efectivo.
- Separar DTO de reporte (`CorteCajaReporte`) de entidades persistentes.
- Versionar migraciones SQL cuando el proyecto incorpore Flyway o Liquibase.
