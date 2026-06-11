# LaborIQ

LaborIQ is a labor market intelligence platform for exploring occupations, wages, employment trends, skills, education requirements, and workforce insights using public datasets.

## Planned Architecture

LaborIQ is planned as a modular full-stack application:

- `backend/`: Spring Boot 3 API service using Java 21, Maven, Spring Web, Spring Data JPA, PostgreSQL, Validation, Actuator, and Lombok.
- `frontend/`: React, Vite, and TypeScript user interface.
- `data-pipeline/`: Python workspace for future public dataset ingestion, transformation, and loading workflows.
- `docs/`: Supporting project documentation.
- `docker-compose.yml`: Local PostgreSQL service for development.

No database entities, Flyway migrations, business logic, or API endpoints are included in this foundation.

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
