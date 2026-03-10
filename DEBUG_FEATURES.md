# Debug Features

This document tracks all development-only functionality that must be removed,
disabled, or verified before deploying the research study.

Last updated: 2026-03-10

---

## Study Arm Override

Location:
ui/onboarding/OnboardingScreen.kt

Description:
Debug dropdown allowing manual selection of study arm.

Required Action Before Study:
Ensure this UI only appears when BuildConfig.DEBUG == true.

Production Behavior:
Study arm must be randomly assigned.

---

## Reset Study Arm (Debug Only)

Location:
ui/onboarding/OnboardingScreen.kt
viewmodel/OnboardingViewModel.kt
domain/study/StudyArmManager.kt
data/dao/DeviceDao.kt

Description:
Debug-only button allowing developers to clear the current `devices` row and re-run onboarding
to choose a new study arm (via the debug dropdown) without reinstalling the app. Does not delete
sessions or interventions.

Required Action Before Study:
Verify the button is only visible and callable when BuildConfig.DEBUG == true.

Production Behavior:
No UI for resetting study arm. Device row must persist for the duration of the study.

---

## Database Debugging

Tool:
Android Studio Database Inspector

Description:
Used during development to inspect Room database contents.

Required Action Before Study:
None (development-only tool).

---

## Debug Logging

Location:
InterventionEngine
UsageTrackingService

Description:
Verbose logs such as:

SESSION_STARTED
INTERVENTION_TRIGGERED
PROMPT_SELECTED

Required Action Before Study:
Reduce log verbosity or guard with BuildConfig.DEBUG.

---

## Intervention Simulation Tools (if added)

Description:
Tools for simulating 10/15/20 minute milestones instantly.

Required Action Before Study:
Remove or disable.
