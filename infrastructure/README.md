# Infrastructure

Cloud configuration for the Doomscrolling Intervention Study.

**Phase 12** will add:

- PostgreSQL schema migration scripts
- RDS instance configuration
- VPC security groups

## Phase 11: Ingestion API (AWS SAM)

This repo includes a minimal AWS SAM template that deploys:

- HTTP API endpoint: `POST /ingest`
- AWS Lambda: validates payload and optionally inserts into PostgreSQL

Template:

- `infrastructure/template.yaml`

