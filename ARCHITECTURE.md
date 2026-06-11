# Architecture

LaborIQ is planned as a three-part system: a Spring Boot backend, a React frontend, and a Python data pipeline.

## Backend

The backend will provide the application API and data access layer. It is initialized with Spring Boot 3, Java 21, Maven, Spring Web, Spring Data JPA, PostgreSQL, Validation, Actuator, and Lombok.

Planned responsibilities:

- Serve labor market intelligence APIs.
- Validate incoming requests.
- Persist and query structured labor market data in PostgreSQL.
- Expose health and operational metadata through Spring Boot Actuator.

This foundation intentionally does not include entities, repositories, services, controllers, migrations, or business logic.

## Frontend

The frontend will provide the user experience for exploring occupations, wages, employment trends, skills, education requirements, and workforce insights. It is initialized with React, Vite, and TypeScript.

Planned responsibilities:

- Provide search and exploration workflows.
- Visualize labor market trends and comparisons.
- Present occupation profiles and workforce insights.
- Communicate with the backend API.

## Data Pipeline

The Python data pipeline will support future public dataset ingestion and transformation.

Planned responsibilities:

- Download or import public labor datasets.
- Normalize source data into backend-ready structures.
- Support repeatable refresh workflows.
- Produce quality checks and processing summaries.

## Local Infrastructure

The root Docker Compose file defines a local PostgreSQL service for development. Database schema design and migrations will be added only after the domain model is defined.
