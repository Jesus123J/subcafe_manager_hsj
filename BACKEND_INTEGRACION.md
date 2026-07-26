# Integración con el backend de Gestión Bodega

Este sistema (FinantialTracker, planilla del HSJ) comparte su base de datos
`financialtracker1` con el backend Spring Boot del sistema de bodega:

```
C:\Users\Jesus Gutierrez\Documents\Proyeto_2026\backend
```

La documentación completa de las dos aplicaciones, las dos bases de datos y
el inventario de tablas está en:

```
Proyeto_2026\backend\README_SISTEMAS.md
```

## Resumen de lo que toca este sistema

- Esta app Swing está **en migración hacia el backend**: la lista de
  empleados y las estadísticas de empleado ya se piden por API REST con
  `ApiBackend.java` (login JWT; configurable con `backend.properties` junto
  al JAR). Si el backend no responde, cae automáticamente al JDBC directo
  de `Conexion.java` (remoto 192.168.97.10 con fallback a localhost) — la
  app nunca se queda sin datos. El resto de módulos sigue por JDBC directo
  hasta que el backend exponga sus endpoints (sobre todo escrituras).
- El backend de la bodega tiene un **segundo datasource** hacia
  `financialtracker1` (módulo `modules/integracion/ft`):
  - **Escribe** abonos de "consumo en bodega" (`abono`, `abonodetail`,
    `lote_carga_abono`) cuando la bodega cierra su mes de créditos — los
    mismos INSERT que hace `AbonoDao` aquí, para que la UI los muestre y
    descuente normal.
  - **Lee** empleados, préstamos, abonos, vouchers y estadísticas por
    empleado, expuestos como API REST (`/api/integracion/ft/...`) con la
    misma lógica de este sistema (p.ej. `loan.EmployeeID` = DNI,
    `abono.Employee_id` = id numérico).

## Reglas para no romperse mutuamente

1. **No renombrar ni borrar columnas** de `employees`, `loan`, `abono`,
   `abonodetail`, `service_concept`, `lote_carga_abono` sin actualizar
   también el backend (`FinancialTrackerRepository.java`).
2. Los abonos creados por la bodega llegan con `discount_from = 'BODEGA'` y
   `lote_id` asignado — se pueden revertir por lote desde el backend si aún
   no tienen pagos.
3. El concepto de servicio usado por la bodega se configura en el backend
   (`FT_SERVICE_CONCEPT_ID`); debe existir en `service_concept`.
