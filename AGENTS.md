# AGENTS.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

Quarkus 3.31.1 microservices project with Java 17. Multi-module Maven structure with two services sharing a common module, deployed on Kubernetes (Minikube for development).

## Build & Development Commands

```bash
# Build common-modules first (required before building services)
mvn -f common-modules/pom.xml clean install

# Build all modules from root
mvn clean install

# Run a specific service in dev mode (requires Postgres running)
mvn -f service-transactions quarkus:dev
mvn -f service-analytics quarkus:dev

# Run tests for a specific module
mvn -f service-transactions test
mvn -f service-transactions test -Dtest=TransactionControllerTest

# Build native image
mvn -f service-transactions package -Pnative
```

## Kubernetes Development

Dev mode runs inside Minikube pods with source mounted from host. See README.md for full setup.

```bash
# Build dev images (run after `eval $(minikube docker-env)`)
docker build -t jcardozo/service-transactions-dev:dev -f service-transactions/src/main/docker/Dockerfile.dev .
docker build -t jcardozo/service-analytics-dev:dev -f service-analytics/src/main/docker/Dockerfile.dev .

# Apply manifests
kubectl apply -f k8s/postgres-db.yaml
kubectl apply -f k8s/app-services.dev.yaml

# Access services
minikube service service-transactions --url
minikube service service-analytics --url
```

## Architecture

### Module Structure
- **common-modules**: Shared entities, datasources, and response types used by both services
- **service-transactions**: CRUD operations for financial transactions with Flyway migrations
- **service-analytics**: Read-only aggregations (balance calculation) using the same PostgreSQL database

### Key Patterns
- **Panache Active Record**: Entities extend `PanacheEntityBase` with public fields (no getters/setters needed for entity fields)
- **Soft Delete**: Transactions use `deleted_at` timestamp; use `listActive()` and `findActiveById()` from `TransactionDatasource`
- **Datasource Layer**: Repository pattern via `PanacheRepository<T>` in `com.sodep.datasources`
- **Service Layer**: Business logic and transactions in `com.sodep.services`

### Database
- PostgreSQL with Flyway migrations in `service-transactions/src/main/resources/db/migration/`
- Migration naming: `V{version}__{description}.sql`
- Config via environment variables: `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`

### Shared Code Registration
Services must register common-modules for CDI scanning in `application.properties`:
```properties
quarkus.arc.scan-dependency.common-modules.group-id=com.sodep
quarkus.arc.scan-dependency.common-modules.artifact-id=common-modules
quarkus.index-dependency.common-modules.group-id=com.sodep
quarkus.index-dependency.common-modules.artifact-id=common-modules
```

## API Endpoints

| Service | Endpoint | Description |
|---------|----------|-------------|
| transactions | `GET/POST /api/transactions` | List all / Create |
| transactions | `GET/PUT/DELETE /api/transactions/{id}` | Single transaction ops |
| analytics | `GET /api/balance` | Calculate current balance |

## Transaction Types
Use `INCOME` or `INCOMING` for credits, `OUTGOING` for debits.
