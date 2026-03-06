# Doomscrolling Study — Mobile App

Android research application for the Doomscrolling Intervention Study.

**Stack:** Kotlin, Jetpack Compose, MVVM (see SYSTEM_ARCHITECTURE.md).

## First-time setup

1. **Open in Android Studio**  
   Open the `mobile-app` folder as the project. Android Studio will sync Gradle and download the correct wrapper and dependencies.

2. **Build**  
   Build → Make Project, or run the app on a device/emulator.

## Command-line build

From the `mobile-app` directory:

- **Windows:** `gradlew.bat assembleDebug`
- **macOS/Linux:** `./gradlew assembleDebug`

If the Gradle wrapper fails, open the project in Android Studio once to sync; it will refresh the wrapper files.

## Project structure (Phase 2+)

Per BUILD_PLAN.md and CURSOR_RULES.md, the app will use:

- `ui/` — Compose screens  
- `viewmodel/` — ViewModels  
- `domain/` — Business logic  
- `data/` — Persistence  
- `services/` — Background logic (e.g. usage tracking)  
- `workers/` — WorkManager jobs  

Phase 1 provides only the skeleton; these directories are added in Phase 2.
