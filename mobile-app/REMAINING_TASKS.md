## Final Study Implementation Checklist

### 1. Consent + Permissions
- **Display full consent form**
  - Implement actual consent text (IRB-approved) in the consent screen.
  - Require explicit **Accept** before proceeding.
- **Request required Android permissions immediately after consent**
  - **Usage access** (`UsageStatsManager`) permission flow.
  - **Display over other apps** permission for intervention overlay.
  - Clear UI explaining why each permission is needed and that they can’t proceed without granting them.

### 2. Eligibility (Baseline retrieval + screening)
- **Trigger baseline retrieval on “Check eligibility”**
  - When user taps **Check eligibility**, run the 7‑day baseline import from `UsageStatsManager` into the local `sessions` table.
  - Use the same Mindful-style reconstruction and merge rules already implemented.
- **Eligibility criteria**
  - Determine if user is an “active user” of any **target apps** based on the imported baseline (e.g., total time or session count thresholds for monitored packages).
- **Eligibility result handling**
  - **Eligible**:
    - Show confirmation in the UI.
    - Proceed to **server baseline upload** (see next section).
  - **Ineligible**:
    - Display a clear message: user is **ineligible** because they are not active users of any target apps.
    - End the flow gracefully (no further onboarding or interventions).

### 3. Baseline upload to server (eligibility success path)
- **Upload baseline data once eligibility passes**
  - After eligibility is confirmed, send the baseline sessions to the ingestion API.
  - Expect a confirmation response from the server that the baseline was stored successfully.
- **Error handling**
  - If upload fails (network/5xx/etc.), surface a clear error to the participant and provide:
    - Retry option.
    - “Contact study team” guidance if retries fail.

### 4. Study arm assignment (server-driven)
- **Server-side balanced assignment**
  - Add an API endpoint that:
    - Receives a new device registration/baseline payload.
    - Assigns a **study arm** in a **sequential round‑robin** order:
      - Example rotation: `identity → mindfulness → friction → control → repeat`.
    - Returns the assigned arm to the client.
- **Client behavior**
  - When eligibility + baseline upload succeed, call the arm‑assignment API.
  - Store the returned arm in the local `devices` table and in any in‑memory study state.
  - Remove any remaining client-side randomization for production (keep debug override behind `BuildConfig.DEBUG` only).

### 5. Onboarding questionnaire (per arm)
- **Display arm-specific onboarding**
  - After arm assignment, present the appropriate onboarding questionnaire:
    - Identity: traits, goals, roles.
    - Mindfulness: anchors, stress points, breathing/visualization cues.
    - Friction: calibration tasks, etc.
    - Control: neutral phone‑habits questions.
- **Persist responses**
  - Store onboarding answers locally (Room or structured JSON) in a way that is accessible to the intervention engine.
  - Ensure the questionnaire cannot be skipped; require completion before proceeding to the dashboard.

### 6. Intervention engine personalization
- **Replace placeholders with real tokens**
  - Wire the intervention engine + prompt manager so that:
    - Prompts pull personalization tokens from the onboarding responses (e.g., traits, goals, anchors).
    - No prompts in production show placeholder text.
- **Verify integration**
  - For each arm/milestone (0, 10, 15, 20 minutes):
    - Confirm the selected prompt uses the correct arm.
    - Confirm that onboarding‑derived tokens are present and rendered correctly in the overlay.

### 7. Completion + exit survey messaging
- **Post-onboarding completion screen**
  - After onboarding questionnaire is submitted:
    - Show a “You’re done!” screen that explains:
      - The study will run for **one week** from enrollment.
      - At the end of the week, they should **return to the app** to complete the **exit survey**.
      - All interventions will **stop** one week after enrollment.
      - They will receive their **4 SONA credits** after the exit survey submission is received.
- **Enforcement (optional but recommended)**
  - Track enrollment timestamp.
  - Automatically stop new interventions after the 7‑day window.

### 8. Exit survey
- **Design and implement exit survey**
  - Content:
    - Experience with interventions.
    - Perceived usefulness/annoyance.
    - Self‑reported changes in behavior.
    - Any study‑specific debrief questions.
  - Implementation:
    - New exit survey screen(s) in the app.
    - Accessible after the 1‑week window (or at any time for pilot testing via a debug-only entry point).
  - Data handling:
    - Store responses locally and upload them to the server (new ingestion shape or dedicated endpoint).
    - Ensure exit survey completion is recorded so credits can be granted.

### 9. Intervention engine polishing & testing
- **Behavioral correctness**
  - Manually test interventions across all arms (identity, mindfulness, friction, control) and milestones (0/10/15/20 minutes).
  - Verify:
    - Scheduled milestones fire at expected times.
    - Overlays block interaction for ~12 seconds and then dismiss cleanly.
    - `interventions` records contain correct `intervention_arm`, `milestone_minutes`, `prompt_variant`, `user_action`.
- **Stress testing**
  - Rapidly switch apps, lock/unlock phone, and ensure:
    - No duplicate or “stuck” interventions.
    - Session merging + minimum duration logic still behaves correctly.
- **Debugging & Bug Fixes**
  - [DONE] Fix: After closing the app once through intervention, subsequent opens show no interventions.
  - Implement: Friction prompts (require interaction instead of just waiting).
  - Optimize: Improve intervention trigger latency (reduce "slow to show up" delay).
- **Notification Vignette**
  - [DONE] Implement a "heads up" notification that slides from the top before an intervention.
  - Timing strategy: 10 seconds before the milestone.
- **Audio Management**
  - Implement muting of all audio/target apps during the 12-second intervention overlay to maximize prompt impact.

### 10. Upload worker testing (nightly + manual)
- **Manual trigger (debug)**
  - Use the **Upload Now (Debug)** button to:
    - Confirm `UPLOAD_WORKER_STARTED` → `UPLOAD_SUCCESS` in Logcat.
    - Confirm new rows appear in Postgres (`sessions`, `interventions`).
- **Scheduled run at ~3 AM**
  - Verify WorkManager’s periodic job:
    - Is enqueued with the correct constraints (network connected).
    - Actually runs overnight on a test device (check logs the next morning).
  - Confirm multiple nightly uploads do not create duplicates beyond what ingestion logic already handles.

### 11. Optional: in-app bug reporting
- **Simple debug-only bug report feature (time permitting)**
  - Add a button (e.g., in a debug section or settings) that:
    - Opens a small form for participants to describe issues.
    - Includes optional metadata (app version, device model, timestamps).
  - Sending:
    - Either send bug reports to the server via a small ingestion endpoint, or
    - Use an email intent to pre-fill a report to the study team.
  - Ensure this feature is clearly labeled and does not interfere with the core study flow.
