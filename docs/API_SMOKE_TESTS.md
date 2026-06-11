# LaborIQ API Smoke Tests

This guide covers manual smoke tests for the read-only occupation API.

## Run the Backend Locally

Start PostgreSQL:

```powershell
docker compose up -d postgres
```

Run the backend:

```powershell
cd .\backend
mvn spring-boot:run
```

By default, the backend uses:

```text
http://localhost:8080
jdbc:postgresql://localhost:5432/laboriq
username: laboriq
password: laboriq
```

## Enable BLS Sample Persistence

The BLS sample persistence runner is disabled by default. Enable it only when you want the backend startup to retrieve one BLS sample series, map it to one occupation and one wage record, and record the run in `etl_runs`.

```powershell
$env:BLS_PERSIST_SAMPLE_ENABLED="true"
cd .\backend
mvn spring-boot:run
```

Default sample values:

```text
BLS_SAMPLE_SERIES_ID=WMU00140201020000001300002500
BLS_SAMPLE_SOC_CODE=13-0000
BLS_SAMPLE_OCCUPATION_TITLE=Business and Financial Operations Occupations
```

To disable persistence again in the same PowerShell session:

```powershell
Remove-Item Env:\BLS_PERSIST_SAMPLE_ENABLED
```

## Curl Smoke Tests

The examples below assume the BLS sample persistence runner inserted SOC code `13-0000`.

### Search Occupations

```powershell
curl "http://localhost:8080/api/occupations/search?q=business"
```

Expected response shape:

```json
[
  {
    "id": 1,
    "socCode": "13-0000",
    "title": "Business and Financial Operations Occupations",
    "description": "BLS sample occupation loaded from public API series WMU00140201020000001300002500",
    "sourceMetadata": {
      "sourceSystem": "BLS",
      "seriesId": "WMU00140201020000001300002500"
    }
  }
]
```

### Get Occupation

```powershell
curl "http://localhost:8080/api/occupations/13-0000"
```

Expected response shape:

```json
{
  "id": 1,
  "socCode": "13-0000",
  "title": "Business and Financial Operations Occupations",
  "description": "BLS sample occupation loaded from public API series WMU00140201020000001300002500",
  "sourceMetadata": {
    "sourceSystem": "BLS",
    "seriesId": "WMU00140201020000001300002500",
    "year": "2023",
    "period": "A01",
    "periodName": "Annual",
    "latest": "true",
    "value": "36.41"
  }
}
```

### Get Wage History

```powershell
curl "http://localhost:8080/api/occupations/13-0000/wages"
```

Expected response shape:

```json
[
  {
    "id": 1,
    "socCode": "13-0000",
    "year": 2023,
    "medianWage": null,
    "meanWage": 36.41
  }
]
```

### Get Latest Wage Record

```powershell
curl "http://localhost:8080/api/occupations/13-0000/wages/latest"
```

Expected response shape:

```json
{
  "id": 1,
  "socCode": "13-0000",
  "year": 2023,
  "medianWage": null,
  "meanWage": 36.41
}
```

### Get Employment History

```powershell
curl "http://localhost:8080/api/occupations/13-0000/employment"
```

Expected response shape:

```json
[]
```

The current BLS sample persistence spike only inserts one occupation and one wage record.

### Get Skills

```powershell
curl "http://localhost:8080/api/occupations/13-0000/skills"
```

Expected response shape:

```json
[]
```

### Get Education Data

```powershell
curl "http://localhost:8080/api/occupations/13-0000/education"
```

Expected response shape:

```json
[]
```

### Get Related Occupations

```powershell
curl "http://localhost:8080/api/occupations/13-0000/related"
```

Expected response shape:

```json
[]
```

## Error Response Shape

Missing occupations and missing requested data return a JSON error response:

```json
{
  "timestamp": "2026-06-10T23:04:44.000000-04:00",
  "status": 404,
  "error": "Not Found",
  "message": "Occupation not found for SOC code: 00-0000",
  "path": "/api/occupations/00-0000"
}
```

## Troubleshooting

### Empty Results

If search returns `[]`, confirm that sample persistence was enabled before backend startup:

```powershell
$env:BLS_PERSIST_SAMPLE_ENABLED
```

Then restart the backend with `BLS_PERSIST_SAMPLE_ENABLED=true`. The read endpoints only return data already persisted in PostgreSQL.

### BLS API Failures

If startup logs show BLS API errors, verify internet access and that the public BLS API is reachable:

```powershell
curl "https://api.bls.gov/publicAPI/v2/timeseries/data/WMU00140201020000001300002500?latest=true"
```

The response should include:

```json
{
  "status": "REQUEST_SUCCEEDED"
}
```

If you use a BLS registration key, set it before starting the backend:

```powershell
$env:BLS_API_KEY="your-key"
```

### Database Connection Failures

If the backend cannot connect to PostgreSQL, confirm the container is running:

```powershell
docker compose ps
```

Start it if needed:

```powershell
docker compose up -d postgres
```

The default datasource values must match `docker-compose.yml`:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/laboriq
SPRING_DATASOURCE_USERNAME=laboriq
SPRING_DATASOURCE_PASSWORD=laboriq
```

### Password Authentication Failed for User "laboriq"

If Spring Boot startup logs include:

```text
FATAL: password authentication failed for user "laboriq"
```

the PostgreSQL container is reachable, but PostgreSQL rejected the configured credentials. A common local development cause is a stale Docker volume. PostgreSQL only applies `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` when the data directory is first initialized. If the existing volume was created earlier with different credentials, changing `docker-compose.yml` does not update the database user password inside that existing volume.

You may also see a Hibernate message like:

```text
Unable to determine Dialect without JDBC metadata
```

That Hibernate message is secondary. Hibernate tries to connect to the database to inspect JDBC metadata and infer the dialect. Because PostgreSQL rejects authentication first, Hibernate cannot read metadata and reports the dialect failure afterward.

For a disposable local development database, reset the PostgreSQL volume from the repository root:

```powershell
docker compose down -v
docker compose up -d postgres
docker exec -it laboriq-postgres psql -U laboriq -d laboriq
```

Use `docker compose down -v` only when you are comfortable deleting the local development database volume and all data inside it. This is appropriate for resetting stale local credentials, but not for preserving local test data.

After confirming `psql` connects, exit with:

```sql
\q
```

Then restart the backend:

```powershell
cd .\backend
mvn spring-boot:run
```

### Sample Persistence Disabled

If logs do not show sample persistence activity, this is expected unless the opt-in flag is enabled:

```text
laboriq.bls.persist-sample-enabled=false
```

Enable it with:

```powershell
$env:BLS_PERSIST_SAMPLE_ENABLED="true"
```

Then restart the backend. The runner executes during application startup.
