# Gestión de Empleados — Autenticación + CRUD

Aplicación web monolítica en Java + Spring Boot que implementa:

- Login con validación contra PostgreSQL y contraseñas hasheadas con **BCrypt** (Spring Security).
- CRUD completo de empleados protegido por sesión, con control de acceso por rol (`admin` / `usuario`).
- Despliegue con **Docker / docker-compose** (app + PostgreSQL con un solo comando).

## Stack

- Java 21 + Spring Boot 4.1.1
- Spring Web (MVC), Spring Data JPA, Spring Security 7
- Thymeleaf + Bootstrap 5
- PostgreSQL 16
- Maven (con wrapper `mvnw`)
- Docker / docker-compose

## Estructura del proyecto

```text
empleados/
├── src/
│   ├── main/
│   │   ├── java/pe/edu/empresa/empleados/
│   │   │   ├── EmpleadosApplication.java
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java         # filtro de seguridad, BCrypt, login/logout
│   │   │   ├── controller/
│   │   │   │   ├── LoginController.java        # GET /login (el POST lo procesa Spring Security)
│   │   │   │   └── EmpleadoController.java     # CRUD /admin/empleados
│   │   │   ├── model/
│   │   │   │   ├── Empleado.java
│   │   │   │   ├── Rol.java                    # admin | usuario
│   │   │   │   └── Estado.java                 # ACTIVO | INACTIVO
│   │   │   ├── repository/
│   │   │   │   └── EmpleadoRepository.java
│   │   │   └── service/
│   │   │       ├── EmpleadoService.java
│   │   │       ├── EmpleadoServiceImpl.java     # validaciones + hash de password
│   │   │       └── EmpleadoUserDetailsService.java  # puente Spring Security ↔ BD
│   │   └── resources/
│   │       ├── templates/
│   │       │   ├── login.html
│   │       │   └── empleados/{lista,formulario}.html
│   │       ├── static/css/styles.css
│   │       ├── application.properties
│   │       └── data.sql                        # seed: admin inicial
│   └── test/java/pe/edu/empresa/empleados/
│       ├── service/EmpleadoServiceTest.java             # unitarios (Mockito)
│       └── integration/
│           ├── LoginIntegrationTest.java                # login OK / fallido
│           └── EmpleadoCrudIntegrationTest.java         # crear/listar/actualizar/eliminar
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Modelo de datos — `empleados`

| Campo            | Tipo                     | Notas                                   |
|------------------|--------------------------|------------------------------------------|
| id               | Long                     | autogenerado                            |
| nombre           | String                   | obligatorio                             |
| email            | String                   | obligatorio, único (es el usuario de login) |
| rol              | enum: `admin` / `usuario`| obligatorio                             |
| password         | String                   | hash BCrypt, nunca texto plano          |
| estado           | enum: `ACTIVO` / `INACTIVO` | un usuario `INACTIVO` no puede loguear |
| fecha_registro   | LocalDateTime            | se asigna automáticamente al crear      |

## Ejecución con Docker (recomendado)

Requisitos: Docker y Docker Compose.

```bash
git clone https://github.com/walterdiazz/lab3-empleados.git
cd lab3-empleados
docker compose up --build
```

Otros comandos útiles:

```bash
docker compose up --build -d   # en segundo plano
docker compose logs -f         # ver logs si corrió con -d
docker compose down            # detener los contenedores
docker compose down -v         # detener y borrar también los datos
```

Esto levanta dos contenedores:

- `empleados-db`: PostgreSQL 16, base `empleados_db` (puerto host `5433`, para no chocar con un PostgreSQL local).
- `empleados-app`: la aplicación Spring Boot (puerto host `8082`).

Al iniciar, la app crea el esquema automáticamente (`ddl-auto=update`) y ejecuta `data.sql`, que inserta un usuario administrador si no existe.

**URL**: http://localhost:8082

**Credenciales de prueba (seed)**:

| Email               | Password   | Rol   |
|---------------------|------------|-------|
| admin@empresa.com   | admin123   | admin |

Para detener todo: `docker compose down` (agregar `-v` si además quieres borrar el volumen de datos).

## Ejecución local sin Docker (alternativa)

Requisitos: JDK 21, Maven (o usar `mvnw`), PostgreSQL corriendo localmente.

```bash
# crear la base de datos
psql -U postgres -c "CREATE DATABASE empleados_db;"

# ejecutar (ajustando credenciales si el postgres local no usa postgres/postgres)
SPRING_DATASOURCE_PASSWORD=tu_password ./mvnw spring-boot:run
```

La app queda en http://localhost:8080.

## Rutas principales

| Método | URL                                | Acción                                   | Acceso          |
|--------|-------------------------------------|-------------------------------------------|-----------------|
| GET    | `/login`                            | Formulario de login                       | público         |
| POST   | `/login`                            | Procesa autenticación (Spring Security)   | público         |
| POST   | `/logout`                           | Cierra sesión                             | autenticado     |
| GET    | `/admin/empleados`                  | Listar empleados                          | autenticado     |
| GET    | `/admin/empleados/nuevo`            | Formulario de creación                    | autenticado     |
| POST   | `/admin/empleados`                  | Crear empleado                            | autenticado     |
| GET    | `/admin/empleados/{id}/editar`      | Formulario de edición                     | autenticado     |
| POST   | `/admin/empleados/{id}`             | Actualizar empleado                       | autenticado     |
| POST   | `/admin/empleados/{id}/eliminar`    | Eliminar empleado                         | **solo rol admin** |

## Casos de prueba

Implementados como tests de Spring Boot (JUnit 5 + MockMvc + Spring Security Test, base de datos H2 en memoria aislada — ejecutar con `./mvnw test`).

### 1. Login (`LoginIntegrationTest`)

| # | Caso | Entrada | Resultado esperado |
|---|------|---------|---------------------|
| 1.1 | Login con credenciales correctas | `admin@empresa.com` / `admin123` | Queda autenticado y redirige a `/admin/empleados` |
| 1.2 | Login con contraseña incorrecta | `admin@empresa.com` / `incorrecta` | No autentica, redirige a `/login?error` |
| 1.3 | Login con usuario inexistente | `nadie@empresa.com` / `cualquiera` | No autentica, redirige a `/login?error` |

### 2. Acceso protegido

| # | Caso | Entrada | Resultado esperado |
|---|------|---------|---------------------|
| 2.1 | Acceder al panel sin sesión | `GET /admin/empleados` sin login | Redirige a `/login` |

### 3. CRUD (`EmpleadoCrudIntegrationTest`)

| # | Caso | Entrada | Resultado esperado |
|---|------|---------|---------------------|
| 3.1 | Crear empleado | nombre, email, rol=`usuario`, password, autenticado como admin | Se guarda en BD y redirige a la lista |
| 3.2 | Listar empleados | 2 empleados precargados | La tabla HTML muestra ambos emails |
| 3.3 | Actualizar empleado | cambiar nombre y rol a `admin` | Los cambios quedan persistidos |
| 3.4 | Eliminar como admin | `POST /admin/empleados/{id}/eliminar` | El registro desaparece de la BD |
| 3.5 | Eliminar como rol `usuario` | mismo endpoint, usuario sin rol admin | `403 Forbidden`, el registro no se borra |

### 4. Validaciones de negocio (`EmpleadoServiceTest`, unitarios)

| # | Caso | Entrada | Resultado esperado |
|---|------|---------|---------------------|
| 4.1 | Crear con datos válidos | empleado válido | Password se guarda hasheada |
| 4.2 | Crear con email duplicado | email ya existente | `IllegalArgumentException` |
| 4.3 | Crear con nombre vacío | nombre en blanco | `IllegalArgumentException` |
| 4.4 | Actualizar sin nueva contraseña | `nuevaPassword = null` | Conserva el hash original |
| 4.5 | Actualizar con nueva contraseña | `nuevaPassword = "nuevaClave123"` | Se re-encripta |
| 4.6 | Eliminar empleado existente | id válido | Invoca `repository.delete` |
| 4.7 | Obtener por id inexistente | id que no existe | `IllegalArgumentException` |

Ejecutar todos los tests:

```bash
./mvnw test
```

## Notas de seguridad

- Las contraseñas nunca se guardan en texto plano: se codifican con `BCryptPasswordEncoder` antes de persistir.
- El login se valida contra la tabla `empleados` a través de un `UserDetailsService` propio (`EmpleadoUserDetailsService`).
- Un empleado con `estado = INACTIVO` no puede iniciar sesión (cuenta deshabilitada a nivel de Spring Security).
- Las peticiones POST están protegidas con **CSRF** (token oculto en cada formulario).
- Eliminar un empleado requiere rol `ADMIN` (`@PreAuthorize("hasRole('ADMIN')")`); el botón "Eliminar" tampoco se muestra en la vista a usuarios sin ese rol.
