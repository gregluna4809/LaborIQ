# OEWS National Data

This directory holds the BLS Occupational Employment and Wage Statistics (OEWS) national dataset used by the LaborIQ ingestion pipeline.

**Data files are not committed to version control.** Download them manually and place them here.

---

## Download

1. Go to the BLS OEWS special requests page:
   https://www.bls.gov/oes/tables.htm

2. Under "National" → "National industry-specific and by ownership" → download the most recent release.
   Direct URL for May 2025 estimates:
   https://www.bls.gov/oes/special.requests/oesm25nat.zip

3. Save the ZIP file to this directory:
   ```
   data/raw/oews/oesm25nat.zip
   ```

The ZIP does not need to be extracted. The ingestion pipeline reads the `.xlsx` inside it directly.

---

## Configure

Set `laboriq.bls.oews.local-file-path` to the absolute path of the ZIP file, or set the
`BLS_OEWS_LOCAL_FILE_PATH` environment variable:

**application.yml (local override)**
```yaml
laboriq:
  bls:
    oews:
      local-file-path: /absolute/path/to/data/raw/oews/oesm24nat.zip
```

**Environment variable**
```
BLS_OEWS_LOCAL_FILE_PATH=C:/path/to/LaborIQ/data/raw/oews/oesm24nat.zip
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
  "recordsProcessed": 830,
  "startTime": "...",
  "endTime": "..."
}
```

---

## Dataset notes

- Survey year in file name: `oesm25nat` → May 2025 estimates
- ~830 detailed occupations + major/minor groups in the national cross-industry file
- Wages are annual (USD). Special codes `#` (not applicable) and `**` (suppressed) are stored as null.
- SOC codes follow the 2018 SOC taxonomy.
