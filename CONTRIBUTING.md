# Contribuir a Subcafae - HSJ

Este documento describe el flujo de trabajo del repositorio. Toda contribución debe respetar estas reglas.

---

## Flujo de ramas

El proyecto tiene dos ramas principales y ramas de trabajo:

- **`main`** — rama de release. Contiene el código publicado a los usuarios. Solo recibe merges desde `master`.
- **`master`** — rama de integración. Recibe los merges de las features antes de pasar a `main`.
- **`feature/*`**, **`fix/*`** — ramas de trabajo. Se ramifican **siempre desde `master`**.

### Regla

```
feature/* → master → main
```

1. Crear rama desde `master`:
   ```bash
   git checkout master
   git pull origin master
   git checkout -b feature/nombre-descriptivo
   ```
2. Trabajar y commitear en la feature.
3. Push de la feature al remoto:
   ```bash
   git push -u origin feature/nombre-descriptivo
   ```
4. Merge a `master` con `--no-ff` para preservar la historia del feature:
   ```bash
   git checkout master
   git merge --no-ff feature/nombre-descriptivo -m "Merge feature/... a master"
   git push origin master
   ```
5. Merge a `main` (fast-forward) cuando se quiera cortar release:
   ```bash
   git checkout main
   git merge --ff-only master
   git push origin main
   ```

**No commitear directo en `main`. No ramificar desde `main`.**

---

## Tags y versionado

Cada release publicado a `main` lleva un tag anotado con el siguiente formato:

```
Subcafae-HSJvMAJOR.MINOR.PATCH
```

Ejemplos: `Subcafae-HSJv0.0.15`, `Subcafae-HSJv0.1.0`, `Subcafae-HSJv1.0.0`.

> Git no admite espacios en nombres de tag, por eso se usa guion. En el cuerpo del mensaje se puede usar la forma legible "Subcafae - HSJv0.0.15".

### Cómo crear y publicar un tag

```bash
git checkout main
git tag -a "Subcafae-HSJv0.0.16" -m "Subcafae - HSJv0.0.16

Cambios incluidos:
- ...
- ..."
git push origin "Subcafae-HSJv0.0.16"
```

### Reglas de versionado

- **PATCH** (`0.0.X`): correcciones, ajustes menores, hotfixes.
- **MINOR** (`0.X.0`): nueva funcionalidad compatible.
- **MAJOR** (`X.0.0`): cambios importantes o incompatibles.

El tag siempre se crea **anotado** (`-a`) con un mensaje que enumera los cambios. No usar tags ligeros — los tags son una vía de comunicación con el equipo y los usuarios.

---

## Mensajes de commit

Usar prefijos convencionales en el subject:

- `feat:` nueva funcionalidad
- `fix:` corrección de bug
- `chore:` tareas de mantenimiento, build, dependencias
- `docs:` cambios solo de documentación
- `refactor:` cambio interno sin alterar comportamiento

Ejemplo:

```
feat: aceptar DNI de extranjeria en gestion de empleados

Permite registrar trabajadores con CE además de DNI peruano.
Ajusta validaciones en EmployeeDao y formularios relacionados.
```

---

## Issues

Antes de empezar trabajo no trivial, abrir un issue en GitHub describiendo el problema o la mejora. Usar las plantillas en `.github/ISSUE_TEMPLATE/`. Asociar el commit final al número de issue cuando aplique (`fix #12`).

---

## Build

Stack principal:

- **Java 22** (runtime objetivo)
- **Maven** para build (`mvn clean package`)
- **MariaDB** como base de datos

El JAR final se empaqueta como `.exe` para distribución a Windows.
