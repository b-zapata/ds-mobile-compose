# Phase 11: Ingestion API (AWS Lambda)

This folder contains the Phase 11 ingestion Lambda handler.

## Endpoint

- `POST /ingest`

Payload shape matches the mobile upload worker JSON:

- `device_id`
- `sessions[]`
- `interventions[]`

## Local testing (SAM)

Prereqs:

- AWS SAM CLI installed

From repo root:

```bash
sam build -t infrastructure/template.yaml
sam local start-api -t infrastructure/template.yaml
```

Then POST a payload:

```bash
curl -X POST "http://127.0.0.1:3000/ingest" \
  -H "Content-Type: application/json" \
  -d @payload.json
```

Expected:

- `200` with `{ "ok": true, "inserted": false }` when DB env vars are not configured.

## DB inserts

If `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` are set (and networking allows access), the handler will attempt to insert rows.

Schema (minimal) is provided in:

- `server/ingestion/schema.sql`

## Deploy (SAM)

```bash
sam deploy --guided -t infrastructure/template.yaml
```

The template outputs the base URL. Your ingest URL will be:

- `<baseUrl>/ingest`

