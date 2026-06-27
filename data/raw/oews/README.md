# OEWS Data

This directory holds the BLS Occupational Employment and Wage Statistics (OEWS) dataset used by the LaborIQ ingestion pipeline.

The repository includes the official May 2025 all-data ZIP used by local Docker development:

```
data/raw/oews/oesm25all.zip
```

The ZIP does not need to be extracted. The ingestion pipeline reads the `.xlsx` inside it directly and filters the all-data workbook to national cross-industry rows (`AREA=99`, `NAICS=000000`).

---

## Docker development

Docker Compose mounts this directory into the backend container at `/app/data/raw/oews` and sets:

```
BLS_OEWS_LOCAL_FILE_PATH=/app/data/raw/oews/oesm25all.zip
BLS_OEWS_DATASET_URL=
```

This prevents local Docker ingestion from downloading OEWS data from BLS, which may reject automated container requests.

---

## Manual download

1. Go to the BLS OEWS special requests page:
   https://www.bls.gov/oes/tables.htm

2. Download the desired ZIP. The local Docker workflow expects the May 2025 all-data ZIP:
   https://www.bls.gov/oes/special.requests/oesm25all.zip

3. Save the ZIP file to this directory:
   ```
   data/raw/oews/oesm25all.zip
   ```

---

## Configure

Set `laboriq.bls.oews.local-file-path` to the absolute path of the ZIP file, or set the
`BLS_OEWS_LOCAL_FILE_PATH` environment variable:

**application.yml (local override)**
```yaml
laboriq:
  bls:
    oews:
      local-file-path: /absolute/path/to/data/raw/oews/oesm25all.zip
```

**Environment variable**
```
BLS_OEWS_LOCAL_FILE_PATH=C:/path/to/LaborIQ/data/raw/oews/oesm25all.zip
```

When `local-file-path` is blank (the default), the endpoint will attempt to download from
`laboriq.bls.oews.dataset-url`. That URL may return HTTP 403 depending on BLS access controls;
the local file path is the primary development path.

---

## Trigger ingestion

With the application running:

```
POST http://localhost:8081/api/admin/etl/oews
```

Expected response on success:

```json
{
  "id": 1,
  "sourceSystem": "BLS_OEWS",
  "status": "COMPLETED",
  "recordsProcessed": 1401,
  "startTime": "...",
  "endTime": "..."
}
```

---

## Dataset notes

- Survey year in file name: `oesm25all` → May 2025 estimates
- The all-data workbook includes national, area, and industry-specific rows; LaborIQ currently loads national cross-industry rows.
- Wages are annual (USD). Special codes `#` (not applicable) and `**` (suppressed) are stored as null.
- SOC codes follow the 2018 SOC taxonomy.
