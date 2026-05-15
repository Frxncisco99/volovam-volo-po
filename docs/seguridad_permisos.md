# Seguridad y permisos

## Migracion

Ejecuta sobre la base existente:

```sql
SOURCE db/migracion_seguridad_permisos.sql;
```

La migracion agrega `permisos`, `usuario_permisos`, `rol_permisos`, `detalle_pago`,
`auditoria_detalle`, `password_hash`, proteccion de administrador principal, campos
de fecha/hora para ventas y `conteo_denominaciones` para cortes de caja.

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
7. Salida del sistema: usar `Salir` en login y `Salir del sistema` en dashboard.
8. Migracion password: iniciar con usuario viejo y revisar que `password_hash` quede lleno.
