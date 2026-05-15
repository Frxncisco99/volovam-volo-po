# Seguridad y permisos

## Migracion

Ejecuta sobre la base existente:

```sql
SOURCE db/migration.sql;
```

`db/migration.sql` aplica la migracion fiscal/financiera y despues la de seguridad.
Agrega `permisos`, `usuario_permisos`, `rol_permisos`, `detalle_pago`,
`auditoria_detalle`, `password_hash`, proteccion de administrador principal, campos
de fecha/hora para ventas, `conteo_denominaciones` para cortes de caja y campos
fiscales de clientes/productos cuando no existan.

## Flujo de permisos

- `PermisoService.tienePermiso(usuario, permiso)` consulta permisos individuales y de rol.
- `PermisoService.validarPermiso(permiso)` bloquea si el usuario no tiene permiso.
- `PermisoService.requerirPermisoOAutorizacionAdmin(permiso, accion)` permite una accion
  si el usuario tiene permiso o si un administrador autorizado valida con su password.
- Los permisos se gestionan desde Configuracion > Seguridad, boton `Permisos` por usuario.

## Contrasenas

Las nuevas contrasenas se guardan en `password_hash` con BCrypt. Los usuarios antiguos con
`contrasena` en texto plano se migran automaticamente cuando inician sesion correctamente.

## Pruebas recomendadas

1. Cajero sin permiso: iniciar con cajero y abrir un modulo restringido.
2. Autorizacion admin: cancelar venta con cajero; debe pedir admin y ejecutar si autoriza.
3. Admin principal protegido: intentar desactivar o degradar `admin`/id 1.
4. Pago mixto: cobrar venta con efectivo + tarjeta y validar filas en `detalle_pago`.
5. Corte de caja: validar que solo `EFECTIVO`/`DOLARES` de `detalle_pago` sumen al efectivo esperado.
6. Denominaciones: capturar billetes/monedas y confirmar que actualiza `Efectivo contado`.
7. Producto con impuesto: crear/editar producto y asignar IVA 8, IVA 16, tasa 0, exento o IEPS.
8. Cliente fiscal: crear cliente con RFC, razon social, regimen, uso CFDI, CP y correo fiscal.
9. Ticket/reporte: validar que subtotal/impuestos se tomen de configuracion fiscal, no de porcentajes fijos.
10. Salida del sistema: usar `Salir` en login y `Salir del sistema` en dashboard.
11. Migracion password: iniciar con usuario viejo y revisar que `password_hash` quede lleno.
