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

-- Study-level onboarding response captured once per device (identity arm uses these values).
CREATE TABLE IF NOT EXISTS onboarding_responses (
  device_id TEXT PRIMARY KEY REFERENCES devices(device_id) ON DELETE CASCADE,
  onboarding_version TEXT NULL,
  completed_at TIMESTAMPTZ NULL,

  trait_1 TEXT NULL,
  trait_2 TEXT NULL,
  trait_3 TEXT NULL,

  goal_1 TEXT NULL,
  goal_2 TEXT NULL,
  goal_3 TEXT NULL,

  role_1 TEXT NULL,
  role_2 TEXT NULL,
  role_3 TEXT NULL,

  automaticity INTEGER NULL,
  utility INTEGER NULL,
  intention INTEGER NULL
);

CREATE INDEX IF NOT EXISTS idx_onboarding_responses_intention ON onboarding_responses(intention);

-- Study-level exit survey captured once per device.
CREATE TABLE IF NOT EXISTS exit_survey_responses (
  device_id TEXT PRIMARY KEY REFERENCES devices(device_id) ON DELETE CASCADE,
  completed_at TIMESTAMPTZ NULL,

  interruption_awareness INTEGER NULL,
  decision_influence INTEGER NULL,
  helpfulness INTEGER NULL,
  frustration INTEGER NULL,
  pause_reconsider INTEGER NULL,
  easier_to_ignore INTEGER NULL,
  outside_use_likelihood INTEGER NULL,

  biggest_influence_aspect TEXT NULL,
  own_words_effect TEXT NULL,
  suggestions TEXT NULL
);

CREATE INDEX IF NOT EXISTS idx_exit_survey_responses_pause_reconsider ON exit_survey_responses(pause_reconsider);

