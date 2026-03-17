# Server

Backend code for the Doomscrolling Intervention Study.

**Phase 11** will implement the ingestion API:

- AWS Lambda function
- Request validation
- Insert records into PostgreSQL

Architecture: Mobile App → API Gateway → Lambda → PostgreSQL (RDS).

## Implemented (Phase 11)

- `server/ingestion/src/handler.ts`: API Gateway-style handler
- `server/ingestion/src/validate.ts`: payload validation (matches mobile keys)
- `server/ingestion/src/db.ts`: optional PostgreSQL inserts via `pg`
- `server/ingestion/schema.sql`: minimal schema

