# Subcafae - HSJ

<p align="center">
  <img src="src/main/resources/IconGeneral/logoIcon.png" alt="Subcafae" width="120" />
</p>

**Subcafae - HSJ** es una aplicación de escritorio para Windows desarrollada por **Subcafae** bajo contrato con el **Hospital San Juan de Dios (HSJ)**. Centraliza la gestión financiera del personal del hospital: préstamos, abonos, avales, refinanciamientos y reportes contables, con conexión a base de datos MariaDB y generación de documentos en PDF/Excel.

> Ejecutable nativo para Windows (`.exe`). No requiere instalar Java.

---

## Resumen del servicio

El sistema permite al área administrativa del hospital:

- **Empleados**: alta, edición y consulta de trabajadores. Soporta DNI peruano y carnet de extranjería.
- **Préstamos**: registro, refinanciamiento, autocompletado de cuotas y reversión de pagos por lote.
- **Abonos / Bonos**: aplicación de pagos a deudas, control de saldos y conceptos.
- **Usuarios y permisos**: login con filtrado de cuentas administrativas en búsquedas.
- **Reportes**: generación de PDF (JasperReports) y Excel para historial de pagos, deuda y abonos.
- **Conexión robusta**: reintentos automáticos contra servidor remoto y fallback a localhost, con configuración externa por archivo de propiedades.

### Stack

| Componente   | Tecnología                       |
|--------------|----------------------------------|
| Lenguaje     | Java 22                          |
| UI           | Swing                            |
| Base de datos| MariaDB                          |
| Reportes     | JasperReports + Apache POI       |
| Build        | Maven                            |
| Distribución | `.exe` empaquetado para Windows  |

### Requisitos

- Windows 7 / 10 / 11
- Acceso de red al servidor MariaDB del hospital (o instancia local en modo fallback)

---

## Descargas

- [Subcafae - HSJ (.exe) – Google Drive](https://drive.google.com/drive/folders/1ydUJZTzbDii47TRMdrtGmIAER1OhEI6R?usp=drive_link)
- [Manual de usuario](https://drive.google.com/file/d/1FQU-qOCM3dV_Tcuq0P0fzvxqizAotvnA/view?usp=drive_link)
- [Manual técnico](https://drive.google.com/file/d/1pl6Wam6__sG9ajKH39QyH3T6nbufDuzn/view?usp=drive_link)

Tamaño aproximado: `~330 MB`.

---

## Versiones

El formato de tag actual del proyecto es `Subcafae-HSJvX.Y.Z`. Las versiones previas usaban el formato corto `vX.Y.Z`.

| Tag                     | Fecha       | Cambios principales                                                                                       |
|-------------------------|-------------|-----------------------------------------------------------------------------------------------------------|
| `Subcafae-HSJv0.0.16`   | 2026-05-04  | CI con GitHub Actions: en cada tag genera JAR + instalador `.exe` automáticamente y publica el Release.  |
| `Subcafae-HSJv0.0.15`   | 2026-05-03  | Aceptación de DNI de extranjería, reconexión automática de BD con reintentos, limpieza del repositorio.   |
| `v0.0.5` / `push`       | 2025-04-28  | Funciones adicionales en abonos y préstamos.                                                              |
| `v0.0.3`                | 2025-04-21  | Versión 0.0.3 — iteración temprana.                                                                       |
| `v0.0.2`                | 2025-04-21  | Versión 0.0.2 — primer release publicado.                                                                 |

### Hitos no taggeados pero relevantes (entre v0.0.5 y v0.0.15)

- Sistema de **reversión de pagos** con soporte para lotes.
- **Autocompletado** y mejoras UX en gestión de bonos y préstamos.
- Filtrado de usuarios admin en búsquedas y corrección de orden de fechas en PDF.
- Agregado de dependencia **JasperReports** y ajustes de compatibilidad de build.
- Manejo robusto de **errores de conexión** (try-catch, fallback).
- **Configuración externa** de base de datos por archivo de propiedades.
- Mejoras de **UI tema claro** y fix del modal de carga.

---

## Estructura del proyecto

```
src/main/java/com/subcafae/finantialtracker/
├── config/        Configuración de la app
├── controller/    Controladores (Main, ManageBond, ManageLoan, ManageWorker, User)
├── data/
│   ├── conexion/  Conexión a MariaDB con reconexión automática
│   ├── dao/       Acceso a datos (Abono, Loan, Employee, Registro, etc.)
│   └── entity/    Entidades del dominio
├── model/         Lógica de negocio (ModelMain y modelos por módulo)
├── report/        Generadores de PDF/Excel (HistoryPayment, ReporteAbono, ReporteDeuda)
├── util/          Utilidades transversales
└── view/          Vistas Swing y componentes
```

---

## Flujo de ramas

- `main` — rama de release. Contiene lo que está publicado.
- `master` — rama de integración. Recibe los merges de las features.
- `feature/*` — ramas de trabajo, ramificadas desde `master`.

Una feature se mergea a `master`, y `master` se mergea a `main` para cortar release. Cada release lleva un tag anotado con el formato `Subcafae-HSJvX.Y.Z` y mensaje descriptivo de los cambios.

> Detalles completos del flujo, convenciones de commit y reglas de tag en [`CONTRIBUTING.md`](CONTRIBUTING.md).

---

## Soporte

¿Encontraste un bug o querés proponer una mejora? Abrí un issue en [GitHub Issues](https://github.com/Jesus123J/subcafe_manager_hsj/issues/new/choose) usando la plantilla correspondiente (bug, feature, release).

---

## Créditos

- **Desarrollado por:** Subcafae
- **Cliente:** Hospital San Juan de Dios (HSJ)
