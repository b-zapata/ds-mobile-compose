-- Phase 11: minimal PostgreSQL schema for ingestion.
-- Matches SYSTEM_ARCHITECTURE.md tables (devices/sessions/interventions).

CREATE TABLE IF NOT EXISTS devices (
  device_id TEXT PRIMARY KEY,
  study_arm TEXT NULL,
  app_version TEXT NULL,
  enrolled_at TIMESTAMPTZ NULL
);

CREATE TABLE IF NOT EXISTS sessions (
  session_id TEXT PRIMARY KEY,
  device_id TEXT NOT NULL REFERENCES devices(device_id) ON DELETE CASCADE,
  app_package_name TEXT NOT NULL,
  session_start_ts TIMESTAMPTZ NOT NULL,
  session_end_ts TIMESTAMPTZ NULL,
  duration_seconds BIGINT NULL,
  created_at TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS idx_sessions_device_id ON sessions(device_id);
CREATE INDEX IF NOT EXISTS idx_sessions_package_name ON sessions(app_package_name);
CREATE INDEX IF NOT EXISTS idx_sessions_start_ts ON sessions(session_start_ts);

CREATE TABLE IF NOT EXISTS interventions (
  intervention_id TEXT PRIMARY KEY,
  device_id TEXT NOT NULL REFERENCES devices(device_id) ON DELETE CASCADE,
  session_id TEXT NOT NULL REFERENCES sessions(session_id) ON DELETE CASCADE,
  intervention_arm TEXT NULL,
  milestone_minutes INTEGER NOT NULL,
  prompt_variant INTEGER NOT NULL,
  intervention_start_ts TIMESTAMPTZ NOT NULL,
  intervention_end_ts TIMESTAMPTZ NULL,
  user_action TEXT NULL,
  created_at TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS idx_interventions_device_id ON interventions(device_id);
CREATE INDEX IF NOT EXISTS idx_interventions_session_id ON interventions(session_id);
CREATE INDEX IF NOT EXISTS idx_interventions_milestone ON interventions(milestone_minutes);

