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

Los pasos 1 y 2 son alternativas: el paso 1 levanta todo con Docker Compose y el paso 2 ejecuta
los microservicios en el host contra las bases en Docker. No ejecutes ambos a la vez.

### 1. Levantar todo con Docker Compose

Prerrequisitos: Docker Desktop/Engine, puertos libres `8081`, `8082`, `5433`, `5434`.

```bash
cp .env.example .env        # en PowerShell: Copy-Item .env.example .env
docker compose up --build   # levanta Postgres + msa-clientes + msa-cuentas
# o en segundo plano
docker compose up -d --build
```

```bash
docker compose ps           # todos los servicios deben quedar "healthy"/"running"
```

Los microservicios se comunican por nombre de servicio dentro de la red de Docker: `http://msa-clientes:8081` y `http://msa-cuentas:8082`. Las bases de datos siguen usando el puerto interno `5432`.

Detener:
```bash
docker compose down         # elimina contenedores y red (mantiene volúmenes)
docker compose down -v      # elimina contenedores, red y volúmenes (borra los datos)
```

### 2. Levantar en local (desarrollo)

```bash
cp .env.example .env        # en PowerShell: Copy-Item .env.example .env
docker compose up -d postgres-clientes postgres-cuentas   # solo bases de datos
```

```bash
cd msa-clientes
./mvnw spring-boot:run      # http://localhost:8081

cd msa-cuentas
./mvnw spring-boot:run      # http://localhost:8082
```

En la segunda terminal, `msa-clientes` alcanza a `msa-cuentas` en `http://localhost:8082`
(configurable con `CUENTAS_BASE_URL`).

No hace falta ningún script de inicialización: las tablas las crea Hibernate (`ddl-auto: update`)
al arrancar cada microservicio. Opcionalmente, `BaseDatos.sql` crea las bases, el esquema y los
registros de ejemplo (ver paso 3).

> Si la base de datos de clientes ya existía con el esquema anterior (una sola tabla `cliente`),
> hay que resetear los volúmenes:
> `docker compose down -v && docker compose up -d postgres-clientes postgres-cuentas`. Con la
> herencia `JOINED` el esquema ahora son dos tablas (`persona` y `cliente`) y `ddl-auto: update`
> no borra las columnas que quedan sin uso.

### 3. Cargar los datos de ejemplo (opcional)

`BaseDatos.sql` es opcional: las tablas ya las crea Hibernate al arrancar. El script crea las
bases/tablas que falten y carga 10 clientes, 20 cuentas y 70 movimientos. Está pensado para
ejecutarse una sola vez sobre una base sin datos y asume los valores por defecto de
`.env.example` (`banco`/`banco`, `clientes_db` y `cuentas_db`). Desde la raíz del repositorio:

```bash
docker compose exec -T -e PGPASSWORD=banco postgres-clientes \
  psql -U banco -d postgres -f - < BaseDatos.sql
```

En PowerShell:

```powershell
Get-Content BaseDatos.sql -Raw | docker compose exec -T -e PGPASSWORD=banco postgres-clientes psql -U banco -d postgres -f -
```

El script se conecta por la red de Compose a `postgres-clientes:5432` (clientes) y
`postgres-cuentas:5432` (cuentas); por eso se ejecuta desde el contenedor de clientes y no
desde el host. Si cambias `DB_USER`, `DB_PASSWORD` o los nombres de las bases en `.env`, ajusta
el comando y los `\connect` del script.

Si necesitas repetir la carga, resetea los volúmenes (`docker compose down -v`) y vuelve a
levantar. Los `INSERT` no son idempotentes: como el script tiene `ON_ERROR_STOP`, se detiene en
el primer duplicado en lugar de dejar datos a medias.

### 4. Verificar

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

Para recorrer la API, importa `banco.postman_collection.json` en Postman: la colección encadena
las peticiones y guarda el `clientId` y el `numeroCuenta` devueltos en las variables.

## Pruebas

### Prueba unitaria (msa-clientes)

```bash
cd msa-clientes
sh mvnw test -Dtest=ClienteTest
```

### Prueba de integración (msa-cuentas)

```bash
cd msa-cuentas
sh mvnw test -Dtest=CuentaMovimientoIntegrationTest
```

### Todas las pruebas

```bash
cd msa-clientes && sh mvnw test
cd msa-cuentas  && sh mvnw test
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
| GET | `/reportes` | Estado de cuenta de un cliente en un rango de fechas (`cliente`, `fechaInicio`, `fechaFin` en formato `YYYY-MM-DD`, inclusive por día completo). Devuelve las cuentas con movimientos en el rango, cada una con su saldo actual, su saldo al inicio del rango y el detalle de movimientos |

Los errores de negocio y de validación se devuelven como `ProblemDetail` (RFC 7807) con el
código de negocio en el campo `code`.

## Estructura

```
banco-msa/
├── docker-compose.yml           # una base de datos por microservicio
├── BaseDatos.sql                # esquema y datos de ejemplo de ambas bases
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
        ├── service/             # AccountService, MovementService, ReporteService
        ├── controller/          # AccountController, MovementController, ReporteController
        └── exception/           # BusinessRuleException, GlobalExceptionHandler
```
