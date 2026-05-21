# DCIM — Sistema de Gestión de Data Centers

Sistema web desarrollado en Spring Boot para gestionar usuarios, ingresos, inventario de equipos, temperaturas y planos de sala en instalaciones de Data Centers y Mega Centrales Telefónica.

**Producción:** https://dcim-app.onrender.com  
**Docker Hub:** `achaconrios43ipss/dcim-app:latest`  
**GitHub:** https://github.com/achaconrios43/dcim  
**Desarrollado por:** Arturo Chacón

---

## Tecnologías

| Capa | Tecnología |
|------|-----------|
| Runtime | Java 25 LTS (Eclipse Adoptium Temurin) |
| Framework | Spring Boot 3.5.9 |
| ORM | Spring Data JPA / Hibernate |
| Seguridad | Spring Security + BCrypt + CSRF |
| Vistas | Thymeleaf |
| Frontend | TailwindCSS, HTML5, JavaScript |
| Base de datos local | MySQL 8.4 (`localhost:3307/dcimdb`) |
| Base de datos producción | PostgreSQL 16 (Render) |
| Contenedor | Docker multi-stage (Red Hat UBI 9 + OpenJDK 25) |
| Hosting | Render (Web Service + PostgreSQL) |
| Build | Maven Wrapper (`mvnw.cmd`) |

---

## Ejecución local

### Requisitos
- Java 25 LTS instalado en `C:\Program Files\Eclipse Adoptium\jdk-25.0.0.36-hotspot`
- MySQL 8.4 corriendo en `localhost:3307`
- Base de datos `dcimdb` creada

```sql
CREATE DATABASE dcimdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Comandos

```powershell
# Perfil web (navegador, MySQL local, puerto 8082)
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-25.0.0.36-hotspot"
$env:SPRING_PROFILES_ACTIVE="web"
.\mvnw.cmd spring-boot:run

# Compilar sin ejecutar
.\mvnw.cmd compile

# Generar JAR
.\mvnw.cmd package -DskipTests
```

La aplicación inicia en: `http://localhost:8082`

### Perfiles disponibles

| Perfil | Puerto | Base de datos | Uso |
|--------|--------|--------------|-----|
| `default` | 8082 | MySQL local | Desarrollo general |
| `web` | 8082 | MySQL local | Navegador optimizado, CSRF activo |
| `mobile` | 8082 | MySQL local | API móvil, CORS activo |
| `production` | 8082 | PostgreSQL (Render) | Producción en cloud |

---

## Módulos del sistema

| Módulo | URL | Descripción |
|--------|-----|-------------|
| Login | `/login` | Autenticación por email o RUT |
| Dashboard | `/dashboard` | Panel principal con resumen |
| Usuarios | `/user/list` | CRUD de usuarios del sistema |
| Ingreso AP | `/ingresoap` | Registro de ingresos a data centers |
| Inventario | `/inventario` | Gestión de equipos por rack |
| Layout racks | `/inventario/layout-vertical` | Vista visual de racks |
| Gestión accesos | `/gestion` | Control de accesos físicos |
| Temperaturas | `/temperaturas` | Configuración de puntos de medición |
| Registro temp. | `/temperaturas/registro` | Registro diario AM/PM de temperaturas |
| Plano sala | `/plano-sala` | Editor visual de planta de sala |
| Plano plantillas | `/plano-sala/plantillas` | Gestión de plantillas de plano |
| Salas | `/salas` | CRUD de salas de data center |
| Dashboard cliente | `/dashboard-cliente` | Vista de cliente con métricas |

---

## Estructura de Base de Datos

### Tablas principales

| Tabla | Descripción |
|-------|-------------|
| `usuario` | Usuarios del sistema (email, rut, rol) |
| `sitio` | Sitios/Data Centers (DC Apoquindo, DC San Martín, etc.) |
| `sala` | Salas dentro de cada sitio |
| `ingreso_ap` | Registros de ingreso a instalaciones |
| `inventario` | Equipos por rack |
| `gestion_acceso` | Control de accesos físicos |
| `punto_medicion` | Puntos de temperatura configurados por sala |
| `medicion_temperatura` | Mediciones diarias AM/PM por punto |
| `plano_sala` | Planos guardados de sala |
| `plano_sala_elemento` | Elementos individuales del plano |

### Sitios configurados (Producción)

| ID | Nombre |
|----|--------|
| 2 | DC San Martín |
| 3 | DC Apoquindo |

---

## Seguridad

- Autenticación web por formulario (`/login`) con sesión HTTP
- Soporta login por **email** o **RUT** (ej: `15.441.473-8`)
- Password hashing con **BCrypt**
- **CSRF** habilitado en todas las rutas web
- API REST en `/api/**` stateless, sin CSRF
- Roles: `ADMIN`, `USER`, `VIEWER`, `CLIENTE`

| Rol | Acceso |
|-----|--------|
| `ADMIN` | Todo — CRUD completo, gestión de usuarios y salas |
| `USER` | Todos los módulos operativos — lectura y escritura |
| `VIEWER` | Solo lectura — puede ver todas las páginas, **no puede** guardar ni modificar datos. Sesión infinita (pantalla monitoreo 24/7). Bloqueado a nivel de filtro HTTP + botones ocultos en UI. |
| `CLIENTE` | Dashboard cliente solamente |

- Todas las rutas protegidas excepto `/`, `/login`, `/api/**`
- `ViewerReadOnlyFilter`: intercepta POST/PUT/DELETE/PATCH de VIEWER y redirige a `/dashboard?errorViewer=1`

---

## Docker

```bash
# Build imagen (sin caché para asegurar cambios)
docker build --no-cache -t achaconrios43ipss/dcim-app:latest .

# Push a Docker Hub
docker push achaconrios43ipss/dcim-app:latest

# Disparar re-deploy en Render (PowerShell)
Invoke-RestMethod -Uri "https://api.render.com/deploy/srv-d830j9n2gups73c6oui0?key=7KY9rK14JGA" -Method POST
```

---

## Credenciales de prueba (Producción)

| Campo | Valor |
|-------|-------|
| Email | `achaconrios@gmail.com` |
| RUT | `15.441.473-8` |
| Contraseña | `dcim2026` |
| Rol | `ADMIN` |

---

## Historial de Cambios

### 2026-05-21 — Dashboard unificado por sitio + estabilización Render
- **Feat:** Nuevo dashboard unificado por sitio en `/dashboard/sitio` con secciones de Acceso, Temperaturas, Inventario y KPI mensual.
- **Feat:** Filtro por sala (`sala`) con soporte para temperaturas mensuales y alcance de inventario por sitio/sala.
- **Fix:** Correcciones de Thymeleaf en `dashboard-sitio.html` para evitar cortes de respuesta (`ERR_INCOMPLETE_CHUNKED_ENCODING`) por expresiones inválidas.
- **Fix:** Reemplazo de `#arrays.asList(...)` por lista SpEL válida en iteraciones por área (RED, TI, EMPRESA, NOC, OTRO).
- **Fix:** Endurecimiento de métricas KPI con cálculo previo en controlador para evitar lógica frágil en la vista.
- **Deploy:** Build validado con `mvnw compile`, push a GitHub y redeploy manual en Render por webhook.

### 2026-05-20 — Rol VIEWER, Plano Sala UX, Fixes Repositorios
- **Feat:** Rol `VIEWER` implementado completamente — solo lectura en toda la aplicación
  - `ViewerReadOnlyFilter.java`: bloquea POST/PUT/DELETE/PATCH → redirige a `/dashboard?errorViewer=1`
  - `SecurityConfig.java`: VIEWER tiene acceso GET a todos los módulos operativos
  - `dashboard.html`: alerta amber cuando `?errorViewer=1`
  - 9 plantillas Thymeleaf: botones de escritura ocultos con `sec:authorize="hasAnyRole('ADMIN','USER')"`
- **Feat:** Editor plano sala (`plano-sala.html`): botón Guardar muestra banner con links "Ver plano" y "Planos guardados"
- **Fix:** `InventarioRepository` — añadidos `findAllByOrderByIdAsc`, `countBySala`, `findDistinctSitios`, `findDistinctSalasBySitio`, `findDistinctRacksBySitioAndSala`, `findByFiltroLayout`
- **Fix:** `MedicionTemperaturaRepository` — añadidos `findPuntoIdsRegistradosEnFechaYHorario`, `findBySitioIdAndFechaRange`
- **Limpieza:** Eliminadas 4 plantillas huérfanas (`coming-soon.html`, `layout-sala.html`, `plano-sala-view.html`, `create-success.html`)
- **Compilación:** Verificado con JDK 25 (Eclipse Adoptium) — 0 errores

### 2026-05-19 — Fix LazyLoading, Plano Sala, Dashboard
- **Fix crítico:** `FetchType.LAZY → FetchType.EAGER` en 8 entidades JPA (Sala, GestionAcceso, IngresoAP, Inventario, MedicionTemperatura, PlanoSala, PlanoSalaElemento, PuntoMedicion). Resolvía `LazyInitializationException` en producción con `spring.jpa.open-in-view=false`.
- **Mejora:** `plano-sala.html` actualizado
- **Mejora:** `dashboard-cliente-diario.html` actualizado
- Docker `achaconrios43ipss/dcim-app:latest` reconstruido y desplegado en Render

### 2026-05-18 — Módulo Temperaturas, Fix Encoding, Migración a Render
- **Fix:** Codificación UTF-8 en producción (caracteres españoles y emojis garbled). Flags `-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8` en Dockerfile.
- **Fix:** Login form no renderizaba en Render
- **Fix:** `temperaturas.html` — segunda página duplicada eliminada
- **Feat:** Vista por racks en inventario con hover tooltip
- **Feat:** Datos semilla para `punto_medicion` (r4, j6 para sala CPD)
- **Deploy:** Migración de Koyeb a **Render** con PostgreSQL

### 2026-05-14 — Dockerfile Java 25, Docker Hub, Layout Racks
- **Fix:** Dockerfile actualizado a Java 25, removido `hibernate.dialect` deprecado
- **Fix:** Usuario Docker Hub corregido a `achaconrios43ipss`
- **Feat:** Layout vertical de racks con tabla inventario completa
- **Fix:** CSRF cookie repository + soporte completo Java 25

### 2026-05-13 — Upgrade Java 25 + Maven Wrapper
- **Upgrade:** Java 21 → Java 25 LTS
- **Upgrade:** Maven Wrapper actualizado
- **Fix:** Configuración CSRF y seguridad mejorada

### 2026-04-22 — Renombre a DCIM
- **Refactor:** Proyecto renombrado de "clases" a "DCIM"

### 2025-12-29 — Corrección dependencias y controladores
- **Fix:** `pom.xml` completo con todas las dependencias
- **Fix:** Controlador duplicado eliminado

### 2025-12-18~19 — Despliegue Koyeb, API REST Móvil
- **Feat:** Backend API móvil con JWT, autenticación RUT/BCrypt, CRUD IngresoAP con foto y GPS
- **Fix:** `SecurityConfig` separado para API (stateless) y web (formLogin)
- **Fix:** Múltiples correcciones de despliegue en Koyeb

### 2025-12-15 — Containerización + Configuración Producción
- **Feat:** `Dockerfile` + `.dockerignore` para contenedorización
- **Feat:** GitHub Actions workflow para build y push de Docker automático
- **Fix:** `import.sql` deshabilitado en producción
- **Docs:** README.md inicial completo

### 2025-11-25 — Módulo Gestión de Accesos + Dashboard Cliente
- **Feat:** Módulo completo de Gestión de Accesos físicos
- **Feat:** Dashboard Cliente con contador en tiempo real

### 2025-11-17~21 — Sistema completo v1
- **Feat:** Primera versión funcional completa del sistema de gestión de ingresos

### 2025-10-27 — Inicio del proyecto
- **Init:** Spring Boot con gestión básica de usuarios e ingresos

---

**Versión:** 1.1.0 | **Java:** 25 LTS | **Spring Boot:** 3.5.9 | **Última actualización:** 2026-05-19