# banco-msa

Solución de la prueba técnica **"Arquitectura Microservicio"**: dos microservicios Spring Boot que
gestionan clientes/personas y cuentas/movimientos, con comunicación asíncrona entre ambos.

| Microservicio | Puerto | Base de datos | Contenido |
|---|---|---|---|
| `msa-clientes` | 8081 | `clientes_db` | Persona, Cliente |
| `msa-cuentas` | 8082 | `cuentas_db` | Cuenta, Movimiento, Reportes |

Cada microservicio es un artefacto independiente, con su propia base de datos (no hay tablas
compartidas ni FK entre servicios) y se comunican por red mediante un contrato explícito.

---

## Requisitos

- JDK 21
- Maven 3.9+
- Docker (Docker Desktop) para las bases de datos

## Configuración

La configuración se resuelve en cascada: **variables de entorno → archivo `.env` → valores por
defecto en `application.yml`**. La fuente de verdad en desarrollo es el `.env` de la raíz del
repositorio, que comparten Docker Compose y los dos microservicios.

| Variable | Para qué sirve |
|---|---|
| `DB_USER` / `DB_PASSWORD` | Credenciales de las dos bases de datos |
| `DB_CLIENTES_HOST` / `_PORT` / `_NAME` | Dónde está la base de datos de `msa-clientes` |
| `DB_CUENTAS_HOST` / `_PORT` / `_NAME` | Dónde está la base de datos de `msa-cuentas` |
| `CLIENTES_PORT` / `CUENTAS_PORT` | Puertos publicados de los microservicios |
| `CUENTAS_BASE_URL` | URL de `msa-cuentas` vista desde `msa-clientes` |
| `CLIENTES_BASE_URL` | URL de `msa-clientes` vista desde `msa-cuentas` |

Los microservicios importan el `.env` con `spring.config.import` (soporte nativo de archivos
`.env` desde Spring Boot 3.4), de forma que cambiar una credencial en el `.env` afecta por igual a
los contenedores y a las aplicaciones que se ejecutan en el host.

## Topología de puertos

Cada base de datos corre en **su propio contenedor**: dos instancias independientes de PostgreSQL,
con volumen y ciclo de vida propios. El puerto **5432** está reservado al interior del contenedor
y en el host se publican en puertos distintos para que no choquen entre sí:

```
host:5433  ──►  postgres-clientes:5432    (base clientes_db)
host:5434  ──►  postgres-cuentas:5432     (base cuentas_db)
```

- Dentro de la red de Docker los servicios se alcanzan **por nombre de servicio y puerto
  interno**: `jdbc:postgresql://postgres-clientes:5432/clientes_db`.
- Desde el host (IDE, DataGrip, DBeaver, pgAdmin, `psql`) se usa el **puerto publicado**:
  `jdbc:postgresql://localhost:5433/clientes_db`.
- El 5432 se deja libre a propósito para un PostgreSQL nativo instalado en la máquina.

Por eso `DB_CLIENTES_PORT` significa "dirección de la base de datos en el host": cuando los
microservicios se ejecutan en Docker, el compose sobrescribe ese valor a `5432` (el puerto
interno) y a `postgres-clientes` (el nombre del servicio).

## Cómo ejecutar

### 1. Levantar las bases de datos

```bash
cp .env.example .env        # en PowerShell: Copy-Item .env.example .env
docker compose up -d
docker compose ps           # ambos Postgres deben quedar "healthy"
```

Para empezar de cero, borrando también los volúmenes y con ello los datos:

```bash
docker compose down -v      # elimina contenedores, red y volúmenes
docker compose up -d        # recrea las bases de datos vacías
```

No hace falta ningún script de inicialización: las tablas las crea Hibernate (`ddl-auto: update`)
al arrancar cada microservicio.

> Si la base de datos de clientes ya existía con el esquema anterior (una sola tabla `cliente`),
> hay que resetear los volúmenes: `docker compose down -v && docker compose up -d`. Con la
> herencia `JOINED` el esquema ahora son dos tablas (`persona` y `cliente`) y `ddl-auto: update`
> no borra las columnas que quedan sin uso.

### 2. Levantar los microservicios

```bash
cd msa-clientes
./mvnw spring-boot:run      # http://localhost:8081

cd msa-cuentas
./mvnw spring-boot:run      # http://localhost:8082
```

En la segunda terminal, `msa-clientes` alcanza a `msa-cuentas` en `http://localhost:8082`
(configurable con `CUENTAS_BASE_URL`).

### 3. Verificar

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

Para recorrer la API, importa `banco.postman_collection.json` en Postman: la colección encadena
las peticiones y guarda el `clientId` y el `numeroCuenta` devueltos en las variables.

## Pruebas

```bash
cd msa-clientes && ./mvnw test
cd msa-cuentas  && ./mvnw test
```

Las pruebas de integración usan **Testcontainers** (PostgreSQL real en contenedor), por lo que
requieren Docker activo.

## Endpoints

### msa-clientes (`http://localhost:8081`)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/clientes` | Crea un cliente. Abre asíncronamente su cuenta corriente en msa-cuentas |
| GET | `/clientes` | Lista paginada de clientes (`page`, `size`) |
| GET | `/clientes/{clientId}` | Consulta un cliente por su identificador único |
| PUT | `/clientes/{clientId}` | Actualiza todos los datos del cliente |
| PATCH | `/clientes/{clientId}` | Actualiza parcialmente los datos del cliente |
| DELETE | `/clientes/{clientId}` | Da de baja al cliente (baja lógica: pasa a `INACTIVO`) y desactiva asíncronamente sus cuentas |

### msa-cuentas (`http://localhost:8082`)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/cuentas` | Crea una cuenta para un cliente. Verifica en msa-clientes que el cliente exista y esté activo. También es el endpoint que consume msa-clientes |
| GET | `/cuentas` | Lista paginada de cuentas. Filtro opcional por `clienteId` |
| GET | `/cuentas/{accountNumber}` | Consulta una cuenta por su número |
| PUT | `/cuentas/{accountNumber}` | Actualiza el tipo y el estado de la cuenta, no el saldo |
| PATCH | `/cuentas/{accountNumber}` | Actualiza parcialmente la cuenta: solo el tipo y/o el estado que lleguen en el cuerpo |
| PATCH | `/cuentas?clienteId=...` | Actualiza en bloque el estado de las cuentas activas de un cliente. Lo consume msa-clientes al dar de baja al cliente |
| POST | `/movimientos` | Registra un movimiento y actualiza el saldo de la cuenta. Solo cuentas en estado `ACTIVA`; `tipoMovimiento` (`INGRESO`/`EGRESO`) debe concordar con el signo del `valor`, que no puede ser cero |
| GET | `/movimientos` | Lista paginada de movimientos. Filtro opcional por `cuenta` |
| GET | `/movimientos/{id}` | Consulta un movimiento |
| PUT | `/movimientos/{id}` | Actualiza la fecha y la referencia del movimiento |

Los errores de negocio y de validación se devuelven como `ProblemDetail` (RFC 7807) con el
código de negocio en el campo `code`.

## Estructura

```
banco-msa/
├── docker-compose.yml           # una base de datos por microservicio
├── .env.example                 # credenciales de ejemplo (el .env real no se versiona)
├── banco.postman_collection.json
├── msa-clientes/                # com.bank.clientes
│   └── src/main/java/com/bank/clientes/
│       ├── model/               # Persona, Cliente, DatosPersona, EstadoCliente
│       ├── repository/          # ClientRepository
│       ├── dto/                 # request y response
│       ├── service/             # ClientService
│       ├── controller/          # ClientController
│       ├── exception/           # BusinessRuleException, GlobalExceptionHandler
│       ├── security/            # PasswordHasher (BCrypt)
│       ├── notification/         # AccountNotifier (cliente HTTP asíncrono)
│       └── event/               # ClientRegisteredEvent, ClientDeactivatedEvent y sus listeners
└── msa-cuentas/                 # com.bank.cuentas
    └── src/main/java/com/bank/cuentas/
        ├── model/               # Cuenta, Movimiento, TipoCuenta, EstadoCuenta
        ├── repository/          # AccountRepository, MovementRepository
        ├── dto/                 # request y response
        ├── service/             # AccountService, MovementService
        ├── controller/          # AccountController, MovementController
        └── exception/           # BusinessRuleException, GlobalExceptionHandler
```
