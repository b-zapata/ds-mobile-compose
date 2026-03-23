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

## Fast schema-only iteration

Use these scripts from `infrastructure/` to avoid deleting/recreating the full stack while testing schema setup behavior.

- Full fast deploy (build + deploy built artifacts):
  - `./scripts/deploy-v3-fast.ps1 -DbPassword "<db-password>"`

- Re-run only the schema custom resource on an existing stack:
  - `./scripts/rerun-schema-custom-resource.ps1 -StackName "doomscrolling-ingestion-public-v3"`
  - Optional explicit version: `./scripts/rerun-schema-custom-resource.ps1 -StackName "doomscrolling-ingestion-public-v3" -SchemaVersion "manual-20260320-1"`

- Invoke only `SchemaSetupFunction` directly (isolated test):
  - `./scripts/invoke-schema-setup.ps1 -StackName "doomscrolling-ingestion-public-v3" -RequestType Update`

Note: `template.yaml` now includes `SchemaVersionParam`, and changing it forces only `SchemaSetupCustomResource` to run again.
