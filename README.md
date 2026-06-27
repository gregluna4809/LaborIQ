# LaborIQ

LaborIQ is a labor market intelligence platform for exploring occupations, wages, employment trends, skills, education requirements, and workforce insights using public datasets.

## Architecture

LaborIQ is a modular full-stack application:

- `backend/`: Spring Boot 3 API service using Java 21, Maven, Spring Web, Spring Data JPA, PostgreSQL, Validation, Actuator, and Lombok.
- `frontend/`: React, Vite, and TypeScript user interface.
- `data-pipeline/`: Python workspace for future public dataset ingestion, transformation, and loading workflows.
- `docs/`: Supporting project documentation.
- `docker-compose.yml`: Local PostgreSQL, backend, and frontend services for development.

## Technology Stack

- Backend: Spring Boot 3, Java 21, Maven
- Database: PostgreSQL
- Frontend: React, Vite, TypeScript
- Data pipeline: Python
- Local infrastructure: Docker Compose

## Development Roadmap

1. Establish project foundation and local development workflow.
2. Identify public labor market datasets and define ingestion strategy.
3. Design the initial database model.
4. Add backend APIs for occupations, wages, employment trends, skills, and education insights.
5. Build frontend exploration and search experiences.
6. Add analytics, data refresh automation, and production deployment hardening.

## Getting Started

### Backend

```powershell
cd .\backend
mvn clean package
```

### Frontend

```powershell
cd .\frontend
npm install
npm run dev
```

### Database

```powershell
docker compose up -d postgres
```

### Docker Development Environment

The local Docker workflow uses the official OEWS ZIP already present in the repository at
`data/raw/oews/oesm25all.zip`. Docker Compose mounts `data/raw/oews` into the backend
container at `/app/data/raw/oews` and sets:

```text
BLS_OEWS_LOCAL_FILE_PATH=/app/data/raw/oews/oesm25all.zip
BLS_OEWS_DATASET_URL=
```

This keeps local Docker development from downloading OEWS data from BLS, which may block
automated container requests. The application defaults in `backend/src/main/resources/application.yml`
are unchanged, so running the backend outside Docker can still download from
`BLS_OEWS_DATASET_URL` when `BLS_OEWS_LOCAL_FILE_PATH` is not set.

Start the local stack:

```powershell
docker compose up -d
```

Trigger OEWS ingestion from the mounted ZIP:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8081/api/admin/etl/oews `
  -Headers @{ "X-API-Key" = "local-dev-admin-key" }
```

The endpoint returns an accepted job. Use the returned `id` to check completion:

```powershell
Invoke-RestMethod `
  -Uri http://localhost:8081/api/admin/etl/runs/<id> `
  -Headers @{ "X-API-Key" = "local-dev-admin-key" }
```

### BLS Sample Persistence Spike

The backend includes a small opt-in BLS integration spike that can retrieve one public BLS wage series, map the latest annual data point to the existing occupation domain model, persist one occupation and one wage record, and record the attempt in `etl_runs`.

Persistence is disabled by default. To run it locally, start PostgreSQL and set:

```powershell
$env:BLS_PERSIST_SAMPLE_ENABLED="true"
cd .\backend
mvn spring-boot:run
```

This is only a connectivity and mapping proof. It does not create REST endpoints, scheduled jobs, frontend changes, or bulk ingestion.
