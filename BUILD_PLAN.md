# BUILD_PLAN.md

Doomscrolling Intervention Study --- Implementation Plan

This document defines the step‑by‑step implementation plan for building
the full system described in SYSTEM_ARCHITECTURE.md.

Developers and AI coding agents (such as Cursor) should implement the
system sequentially following the phases below.

Each phase should be completed and verified before moving to the next.

------------------------------------------------------------------------

# Phase 1 --- Repository Setup

Create the base project structure.

Repository structure:

doomscrolling-study/ │ ├── SYSTEM_ARCHITECTURE.md ├── BUILD_PLAN.md │
├── mobile-app/ ├── server/ ├── infrastructure/ └── prompts/

Tasks:

• Initialize Git repository\
• Create Android project using Kotlin + Jetpack Compose\
• Create server directory for backend code\
• Create infrastructure directory for cloud configuration\
• Add prompts directory for prompt library

Deliverable:

Working project skeleton.

------------------------------------------------------------------------

# Phase 2 --- Android Project Skeleton

Inside mobile-app create the application structure using MVVM.

Required structure:

mobile-app/ │ ├── ui/ ├── viewmodel/ ├── domain/ ├── data/ ├── services/
└── workers/

Tasks:

• Configure Jetpack Compose • Configure Kotlin coroutines • Configure
dependency injection if desired • Setup navigation framework

Deliverable:

App launches successfully with empty screens.

------------------------------------------------------------------------

# Phase 3 --- Usage Tracking Engine

Implement the system that detects foreground app usage.

Tasks:

• Integrate Android UsageStatsManager API • Read UsageEvents from the
system • Reconstruct foreground activity using: ACTIVITY_RESUMED
ACTIVITY_PAUSED ACTIVITY_STOPPED • Detect when monitored apps enter the
foreground

Deliverable:

System logs foreground sessions for monitored apps.

------------------------------------------------------------------------

# Phase 4 --- Session Detection

Implement the session lifecycle.

Tasks:

• Create new session when monitored app becomes foreground • End session
when app leaves foreground • Store session information locally

Session fields:

session_id\
device_id\
app_package_name\
session_start_ts\
session_end_ts\
duration_seconds

Deliverable:

Sessions correctly recorded locally.

------------------------------------------------------------------------

# Phase 5 --- Intervention Engine

Implement milestone detection and intervention triggering.

Milestones:

0 minutes\
10 minutes\
15 minutes\
20 minutes

Tasks:

• Start milestone timers when session begins • Verify monitored app is
still active • Trigger intervention screen

Deliverable:

Intervention triggers during active sessions.

------------------------------------------------------------------------

# Phase 6 --- Intervention UI

Create the intervention overlay.

Tasks:

• Implement full-screen InterventionActivity • Display prompt text or
friction task • Enforce 12-second minimum duration • Disable dismissal
until timer completes

Deliverable:

Working intervention interface.

------------------------------------------------------------------------

# Phase 7 --- Onboarding Module

Implement onboarding questionnaires.

Arms:

Identity\
Mindfulness\
Friction\
Control

Tasks:

• Build questionnaire screens • Store responses locally • Assign study
arm randomly • Save study_arm in local database

Deliverable:

Participants complete onboarding flow.

------------------------------------------------------------------------

# Phase 8 --- Prompt Engine

Implement prompt loading and selection.

Tasks:

• Create prompts.json file • Load prompts from assets directory • Select
prompt based on: study_arm milestone • Insert personalization tokens

Deliverable:

Prompts display correctly.

------------------------------------------------------------------------

# Phase 9 --- Local Database

Implement Room database.

Tables:

devices\
sessions\
interventions

Tasks:

• Create Room entities • Create DAO interfaces • Implement repository
layer

Deliverable:

Local persistence working.

------------------------------------------------------------------------

# Phase 10 --- Nightly Upload Worker

Implement batch uploads.

Tasks:

• Create WorkManager background job • Schedule upload around 3 AM •
Serialize sessions + interventions to JSON • Send to API endpoint

Deliverable:

Mobile app uploads data automatically.

------------------------------------------------------------------------

# Phase 11 --- Backend Ingestion API

Implement server ingestion service.

Architecture:

Mobile App → API Gateway → Lambda → PostgreSQL

Tasks:

• Create AWS Lambda function • Validate request payload • Insert records
into database

Deliverable:

Server accepts upload requests.

------------------------------------------------------------------------

# Phase 12 --- Database Schema

Create PostgreSQL schema.

Tables:

devices sessions interventions

Tasks:

• Create schema migration scripts • Configure RDS instance • Configure
VPC security groups

Deliverable:

Production-ready database.

------------------------------------------------------------------------

# Phase 13 --- End-to-End Integration

Verify entire system pipeline.

Test workflow:

1 Install mobile app 2 Start monitored app session 3 Trigger
intervention 4 Record local data 5 Upload data to server 6 Verify
records in database

Deliverable:

Complete working system.

------------------------------------------------------------------------

# Phase 14 --- Research Testing

Conduct internal testing.

Tasks:

• Simulate multiple users • Verify session accuracy • Validate milestone
triggers • Verify upload reliability

Deliverable:

System ready for participant deployment.

------------------------------------------------------------------------

# Implementation Notes

Follow SYSTEM_ARCHITECTURE.md as the authoritative specification.

When implementing components, ensure:

• Mobile and server schemas match • Prompts load locally from JSON • All
communication uses HTTPS • No personally identifiable information is
stored
