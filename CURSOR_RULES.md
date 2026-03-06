# CURSOR_RULES.md

Guidelines for AI Coding Agents Working on the Doomscrolling Research
App

This document defines rules that AI coding assistants (such as Cursor)
must follow when generating or modifying code in this repository.

These rules ensure the system stays consistent with
SYSTEM_ARCHITECTURE.md and BUILD_PLAN.md.

---

# 1. Source of Truth

The following files define the official architecture of the system:

- SYSTEM_ARCHITECTURE.md
- BUILD_PLAN.md

When writing code, always follow these documents exactly.

If a conflict appears between generated code and these specifications,
the specification documents take precedence.

---

# 2. Project Structure

Do not change the repository structure unless explicitly instructed.

Expected structure:

ds-mobile-compose\

SYSTEM_ARCHITECTURE.md\
BUILD_PLAN.md\
CURSOR_RULES.md

mobile-app/\
server/\
infrastructure/\
prompts/

---

# 3. Android Development Rules

The mobile application must follow these standards:

Language: Kotlin\
UI Framework: Jetpack Compose\
Architecture: MVVM

Required Android components:

- ViewModels for UI state
- Repository pattern for data access
- Room database for local storage
- WorkManager for background jobs
- Foreground Service for usage monitoring

Do not introduce alternative architectures such as MVP or MVC.

---

# 4. Code Organization (Android)

Inside mobile-app the expected structure is:

mobile-app/

ui/\
viewmodel/\
domain/\
data/\
services/\
workers/

Guidelines:

- UI layer contains Compose screens only
- ViewModels manage UI state
- Domain layer contains business logic
- Data layer handles persistence
- Services contain long-running background logic
- Workers contain WorkManager jobs

---

# 5. Session Detection Logic

Sessions must be detected using:

Android UsageStatsManager

Foreground usage reconstruction must use:

ACTIVITY_RESUMED\
ACTIVITY_PAUSED\
ACTIVITY_STOPPED

A session begins when a monitored app enters the foreground and ends
when it leaves the foreground.

---

# 6. Intervention Rules

Interventions must trigger at the following milestones:

0 minutes\
10 minutes\
15 minutes\
20 minutes

Interventions must:

- display a full-screen activity
- block interaction for approximately 12 seconds
- record intervention outcome

Valid outcomes:

continued_session\
closed_app

---

# 7. Prompt System

Prompts must be loaded locally from:

app/src/main/assets/prompts.json

Prompts are selected using:

study_arm + milestone

Do not retrieve prompts from a remote API.

---

# 8. Local Database

The Android app must use Room.

Required tables:

devices\
sessions\
interventions

The schema must match the PostgreSQL schema defined in
SYSTEM_ARCHITECTURE.md.

---

# 9. Backend Architecture

Backend infrastructure must follow this architecture:

Mobile App\
→ AWS API Gateway\
→ AWS Lambda\
→ PostgreSQL (RDS)

Do not introduce additional services unless explicitly required.

Examples of services that should NOT be added without instruction:

- GraphQL
- Firebase
- MongoDB
- Redis
- Kafka

---

# 10. Data Privacy Rules

The system must NEVER collect:

- message content
- images
- videos
- notification text
- personally identifiable information

Only collect:

- app package name
- timestamps
- session durations
- intervention outcomes

All data must be associated with an anonymized device_id.

---

# 11. Networking Rules

All server communication must:

- use HTTPS
- send JSON payloads
- perform batched uploads
- occur via the ingestion API

The mobile app should upload data once per day using WorkManager.

---

# 12. Code Generation Guidelines

When generating code:

- prefer simple and readable implementations
- avoid unnecessary abstractions
- follow Kotlin best practices
- document non-obvious logic

Avoid:

- overengineering
- complex dependency graphs
- unnecessary libraries

---

# 13. Implementation Strategy

Always follow the implementation phases defined in BUILD_PLAN.md.

Implement only the current phase unless instructed otherwise.

Do not skip ahead to later phases.

---

# 14. Testing Expectations

When adding new features:

- ensure the project compiles
- verify Android lifecycle compatibility
- ensure background services do not crash

Prefer small iterative changes over large code dumps.

---

# 15. When Uncertain

If a requirement is unclear:

1.  Check SYSTEM_ARCHITECTURE.md
2.  Check BUILD_PLAN.md
3.  Ask for clarification before inventing architecture
