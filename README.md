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

## Pruebas

```bash
cd msa-clientes && ./mvnw test
cd msa-cuentas  && ./mvnw test
```

Las pruebas de integración usan **Testcontainers** (PostgreSQL real en contenedor), por lo que
requieren Docker activo.

## Endpoints

> Se completa en la iteración F1.

## Decisiones y limitaciones

> Se completa al final de la entrega.

## Estructura

```
banco-msa/
├── docker-compose.yml      # una base de datos por microservicio
├── .env.example            # credenciales de ejemplo (el .env real no se versiona)
├── msa-clientes/           # com.bank.clientes
└── msa-cuentas/            # com.bank.cuentas
```
