# BridgeForge
### Legacy Modernization & Customer Service

BridgeForge is a **Java 21 + Spring Boot 3.5** service that gradually moves customer records from a legacy HTTP API into PostgreSQL. The service uses a **Strangler pattern**: requests first read the new database, fall back to the legacy system when allowed, and persist converted records along with transactional outbox events.

## Features

- Modern customer REST API with Bean Validation, JPA/Hibernate and optimistic locking.
- Legacy `GET /customers/{id}` adapter, on-demand customer import and deterministic provenance (`X-Data-Origin`).
- PostgreSQL schema migrations with Flyway; unique customer email and legacy identifier constraints.
- Transactional outbox: customer creation/import and event creation commit together.
- Scheduled webhook delivery with retries and bounded attempts (5); optional integration URL.
- MockMvc and JUnit 5 integration tests using H2 and an isolated legacy gateway.
- GitHub Actions for `mvn verify`.

## Run locally

Requirements: Java 21, Maven 3.6.3+, Docker (or PostgreSQL).

```bash
# Set DB_PASSWORD to a strong local value (see .env.template)
docker compose up -d
BRIDGEFORGE_ADMIN_PASSWORD="<strong-local-password>" mvn spring-boot:run
```

Default server: `http://localhost:8082`.

### Create a customer

```bash
curl -u portfolio-admin:"<strong-local-password>" -X POST http://localhost:8082/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{"id":501,"fullName":"Camila Torres","email":"camila@example.org"}'
```

### Retrieve a migrated customer

```bash
curl -u portfolio-admin:"<strong-local-password>" http://localhost:8082/api/v1/customers/501
```

When `LEGACY_ENABLED=true` and `LEGACY_URL` points at an operational legacy service, a missing customer is obtained from `GET {LEGACY_URL}/customers/{id}` and stored. Set `OUTBOX_WEBHOOK_URL` to a trusted receiver to deliver outbox events; without it, events remain queued rather than being discarded.

## Test

```bash
mvn verify
```

## Endpoints

| Method | URL | Purpose |
|---|---|---|
| GET | `/actuator/health` | Health |
| POST | `/api/v1/customers` | Register customer; HTTP 201 |
| GET | `/api/v1/customers/{id}` | Read modern or import legacy customer |
| POST | `/api/v1/migrations/customers/{id}` | Force import/lookup operation |

Migration and reliability assumptions are described in [ARCHITECTURE.md](ARCHITECTURE.md).


## Authorization / performance hardening

See [SECURITY_AND_LOAD.md](docs/SECURITY_AND_LOAD.md) for fail-closed credentials, role-based checks, negative tests and opt-in load testing.
