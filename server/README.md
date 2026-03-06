# Server

Backend code for the Doomscrolling Intervention Study.

**Phase 11** will implement the ingestion API:

- AWS Lambda function
- Request validation
- Insert records into PostgreSQL

Architecture: Mobile App → API Gateway → Lambda → PostgreSQL (RDS).
