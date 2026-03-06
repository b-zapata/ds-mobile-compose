# Doomscrolling Intervention Study --- System Architecture

This document defines the full architecture of the Doomscrolling
Intervention Study system.

The system consists of:

1.  Android mobile research application
2.  AWS ingestion API
3.  PostgreSQL research database
4.  Analytics dashboard

The purpose of the system is to measure and reduce habitual social media
usage using short behavioral interventions triggered during app
sessions.

------------------------------------------------------------------------

# 1. Study Overview

Participants install an Android research application that monitors
social media usage sessions.

When a monitored app is used for extended periods, the app triggers
brief interventions.

The application records behavioral metadata and uploads anonymized logs
to a cloud database.

The study evaluates four intervention conditions:

-   Identity-based prompts
-   Mindfulness prompts
-   Friction tasks
-   Control condition

The study duration per participant is approximately one week.

------------------------------------------------------------------------

# 2. Study Flow

The participant experience follows this sequence:

1.  Install research app
2.  Accept consent form
3.  Eligibility screening
4.  Onboarding questionnaire
5.  Intervention week
6.  Daily data uploads
7.  Study completion

------------------------------------------------------------------------

# 3. Consent Process

The app displays an implied consent form at first launch.

Participants must press **Accept** to continue.

Participants may stop participation at any time.

The application collects only summary usage metadata and does not
collect message content or media.

------------------------------------------------------------------------

# 4. Eligibility Screening

Eligibility screening occurs automatically inside the mobile
application.

The app retrieves the participant's last 7 days of device usage data.

Eligibility criteria include:

-   Ownership of an Android smartphone
-   Ability to install and run the research app
-   Minimum usage of monitored social media apps

If the participant does not meet criteria, the study ends immediately
and no data is retained.

------------------------------------------------------------------------

# 5. Onboarding Questionnaires

Participants are randomly assigned to one of four study arms.

## Arm A --- Identity

Participants answer questions about: - character traits - personal
goals - life roles

These values are used to personalize prompts.

## Arm B --- Mindfulness

Participants identify sensory anchors including: - stress points in the
body - breathing location - calming places - visualization cues

## Arm C --- Friction

Participants complete calibration tasks including: - typing tests -
tapping tests - tracing tests

These tasks measure interaction speed for later task calibration.

## Arm D --- Control

Participants answer neutral descriptive questions about phone habits.

------------------------------------------------------------------------

# 6. Prompt Library

Prompts are stored locally in the mobile application.

Location:

app/src/main/assets/prompts.json

Prompts are grouped by: - intervention arm - milestone time

Milestones include:

-   0 minutes
-   10 minutes
-   15 minutes
-   20 minutes

The intervention engine randomly selects a prompt from the appropriate
group and inserts personalization tokens.

------------------------------------------------------------------------

# 7. Mobile Application Architecture

The mobile app is implemented using:

-   Kotlin
-   Jetpack Compose
-   MVVM architecture
-   Room database
-   WorkManager background jobs

The app continuously monitors foreground application usage.

------------------------------------------------------------------------

# 8. Usage Tracking

The system uses Android's UsageStatsManager API.

Foreground usage is reconstructed by pairing:

-   ACTIVITY_RESUMED
-   ACTIVITY_PAUSED
-   ACTIVITY_STOPPED

This allows accurate reconstruction of app sessions.

A new session begins when a monitored app becomes the foreground
application.

------------------------------------------------------------------------

# 9. Monitored Applications

The system tracks usage of major social media applications.

Example packages include:

-   com.instagram.android
-   com.zhiliaoapp.musically
-   com.reddit.frontpage
-   com.google.android.youtube
-   com.facebook.katana
-   com.twitter.android

------------------------------------------------------------------------

# 10. Session Detection

A session is defined as a continuous period where a monitored app is in
the foreground.

Session lifecycle:

App enters foreground → session_start\
App leaves foreground → session_end

Session records include:

-   session ID
-   device ID
-   app package
-   start timestamp
-   end timestamp
-   duration

------------------------------------------------------------------------

# 11. Intervention Engine

The intervention engine schedules prompts during active sessions.

Milestones occur at:

-   0 minutes
-   10 minutes
-   15 minutes
-   20 minutes

When a milestone occurs, the system verifies that the same monitored app
is still active.

If the app is still active, an intervention prompt is displayed.

Interventions last approximately 12 seconds.

------------------------------------------------------------------------

# 12. Intervention Outcomes

After the intervention completes, the system checks whether the user
continues using the monitored app.

Two outcomes are recorded:

-   continued_session
-   closed_app

------------------------------------------------------------------------

# 13. Local Data Storage

The mobile application stores logs locally using a Room database.

### devices

device_id UUID\
study_arm TEXT\
app_version TEXT\
enrolled_at TIMESTAMP

### sessions

session_id UUID\
device_id UUID\
app_package_name TEXT\
session_start_ts TIMESTAMP\
session_end_ts TIMESTAMP\
duration_seconds INTEGER\
created_at TIMESTAMP

### interventions

intervention_id UUID\
device_id UUID\
session_id UUID\
intervention_arm TEXT\
milestone_minutes INTEGER\
prompt_variant INTEGER\
intervention_start_ts TIMESTAMP\
intervention_end_ts TIMESTAMP\
user_action TEXT\
created_at TIMESTAMP

------------------------------------------------------------------------

# 14. Data Upload Pipeline

The mobile app uploads logs once per day.

Upload time: \~03:00 AM local time.

The upload worker sends:

-   sessions\[\]
-   interventions\[\]

to the ingestion API.

------------------------------------------------------------------------

# 15. Server Architecture

The backend uses AWS serverless infrastructure.

Data flow:

Mobile App → AWS API Gateway → AWS Lambda → PostgreSQL (Amazon RDS)

The database resides inside a private VPC and is not publicly
accessible.

------------------------------------------------------------------------

# 16. Security Controls

Security protections include:

-   HTTPS encryption
-   AWS VPC network isolation
-   IAM access control
-   database encryption at rest
-   anonymized device identifiers

No personal identifiers are stored.

------------------------------------------------------------------------

# 17. Analytics

Research dashboards connect directly to the PostgreSQL database.

The dataset is small (\~50 participants for one week), so no data lake
infrastructure is required.

Tools such as Tableau may be used for analysis.

------------------------------------------------------------------------

# 18. System Components

The complete system consists of:

### Mobile Application

Responsible for: - session detection - intervention delivery - local
data storage - nightly uploads

### Ingestion API

Responsible for: - validating upload payloads - inserting records into
the database

### Research Database

Stores: - devices - sessions - interventions

### Analytics Dashboard

Used for monitoring study progress.

------------------------------------------------------------------------

# 19. Implementation Guidelines

Developers implementing this system should follow these principles:

-   Keep the architecture simple and deterministic
-   Store prompts locally in the app
-   Avoid collecting any personal or message content
-   Ensure the mobile app works offline
-   Upload data in batches to reduce network overhead
-   Keep the database schema consistent between mobile and server

------------------------------------------------------------------------

# 20. Expected System Behavior

During the study week the system should:

1.  detect sessions in monitored apps
2.  trigger interventions at milestone times
3.  record intervention outcomes
4.  store logs locally
5.  upload logs daily to the server
6.  allow researchers to analyze results in the database
