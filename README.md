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

## Cómo ejecutar

### 1. Levantar las bases de datos

```bash
cp .env.example .env        # ajusta credenciales si quieres
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
├── msa-cuentas/            # com.bank.cuentas
└── banco.postman_collection.json
```
