# PROBLEMS.md — Registro de Problemas y Soluciones
## DCIM - Data Center Infrastructure Management

---

## 2026-05-19 — LazyInitializationException en Producción

**Síntoma:** La página `/temperaturas` y otras páginas de detalle se renderizaban incompletas en Render. La tabla mostraba celdas vacías. Localmente funcionaba bien.

**Causa Raíz:** Todas las entidades JPA tenían `@ManyToOne(fetch = FetchType.LAZY)`. En producción, `application-production.properties` tiene `spring.jpa.open-in-view=false`, lo que cierra la sesión Hibernate antes de que Thymeleaf renderice las asociaciones. Al acceder a propiedades lazy en el template, Hibernate lanza `LazyInitializationException`.

**Error Log:**
```
org.hibernate.LazyInitializationException: could not initialize proxy - no Session
```

**Archivos Afectados:**
- `GestionAcceso.java`
- `IngresoAP.java`
- `Inventario.java`
- `MedicionTemperatura.java`
- `PlanoSala.java`
- `PlanoSalaElemento.java`
- `PuntoMedicion.java`
- `Sala.java`

**Solución:** Cambiar `FetchType.LAZY` → `FetchType.EAGER` en todos los `@ManyToOne` de las 8 entidades.

---

## 2026-05-19 — Docker Build Cache Corruption

**Síntoma:** `docker build` fallaba con error de snapshot.

**Error:**
```
failed to get parent snapshot: not found
```

**Causa:** Corrupción en la caché local de Docker.

**Solución:** Agregar flag `--no-cache` al comando build:
```
docker build --no-cache -t achaconrios43ipss/dcim-app:latest .
```

---

## 2026-05-18 — UTF-8 Encoding Garbled en Render

**Síntoma:** Caracteres en español (tildes, ñ) y emojis mostraban como caracteres ilegibles (e.g., `â€™`, `Ã±`) en la interfaz web desplegada en Render.

**Causa:** La JVM en el contenedor Docker usaba charset por defecto del sistema operativo (no UTF-8). Spring Boot / Thymeleaf generaba respuestas HTTP con encoding incorrecto.

**Solución:** Agregar flags JVM explícitas en el `Dockerfile`:
```dockerfile
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8"
```

---

## 2026-05-18 — Login Form No Renderizaba en Render

**Síntoma:** La página de login (`/login`) no se mostraba correctamente en el entorno de producción Render.

**Causa:** Error en el template `login.html` que causaba que Thymeleaf fallara al procesar el formulario.

**Solución:** Corrección del template `login.html`.

---

## 2026-05-18 — Página `temperaturas.html` con Contenido Duplicado

**Síntoma:** La segunda parte de la página de temperaturas aparecía duplicada.

**Causa:** El template `temperaturas.html` tenía un bloque HTML duplicado.

**Solución:** Eliminación del bloque duplicado en `temperaturas.html`.

---

## 2026-05-14 — Usuario Docker Hub Incorrecto

**Síntoma:** `docker push` fallaba con "access denied" o subía a un repositorio incorrecto.

**Causa:** Se usaba un nombre de usuario de Docker Hub equivocado en los comandos de build y push.

**Solución:** Corregir a `achaconrios43ipss` en todos los comandos:
```
docker build -t achaconrios43ipss/dcim-app:latest .
docker push achaconrios43ipss/dcim-app:latest
```

---

## 2026-05-13 — UnsupportedClassVersionError (Java Version Mismatch)

**Síntoma:** El JAR no ejecutaba en el contenedor. Error al iniciar Spring Boot.

**Error:**
```
java.lang.UnsupportedSupportedClassVersionError: ... has been compiled by a 
more recent version of the Java Runtime (class file version 69.0)
```

**Causa:** El proyecto fue compilado con JDK 25 (class version 69) pero se intentaba ejecutar con JDK 21 (soporta hasta class version 65).

**Regla:** **Siempre usar JDK 25** (`JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-25.0.0.36-hotspot`). NUNCA usar JDK 21 para este proyecto.

**Solución:** 
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.0.36-hotspot"
$env:SPRING_PROFILES_ACTIVE = "web"
.\mvnw.cmd spring-boot:run
```

---

## 2026-05-13 — `hibernate.dialect` Deprecado

**Síntoma:** Warnings en logs de Spring Boot sobre propiedad deprecada.

**Causa:** `spring.jpa.properties.hibernate.dialect=...` ya no es necesario en versiones recientes de Hibernate. Hibernate detecta el dialecto automáticamente.

**Solución:** Eliminar la propiedad `hibernate.dialect` de los archivos `application*.properties`.

---

## 2025-12-18 — Múltiples Fallos de Despliegue en Koyeb

**Síntoma:** Despliegues fallaban repetidamente en Koyeb con errores de conexión a base de datos y health check timeouts.

**Problemas encontrados:**
- Pool de conexiones agotado (`HikariPool` timeouts)
- Health check timeouts (Koyeb cerraba el servicio antes que Spring Boot terminara de inicializar)
- Incompatibilidades con las variables de entorno de Koyeb

**Solución:** Migración completa de Koyeb a **Render**. Se eliminó `koyeb-deployment-guide.md` como documentación obsoleta.

---

## Notas Generales

- **Entorno de producción:** Render (Docker image de Docker Hub `achaconrios43ipss/dcim-app:latest`)
- **Base de datos producción:** PostgreSQL en Render (internal URL)
- **Base de datos local:** MySQL en `127.0.0.1:3307/dcimdb`
- **Perfil local:** `web` | **Perfil producción:** `production`
- **JDK requerido:** JDK 25 (Eclipse Adoptium) — NUNCA JDK 21
