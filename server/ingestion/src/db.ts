import { Client } from "pg";
import {
  UploadExitSurveyResponse,
  UploadIntervention,
  UploadOnboardingResponse,
  UploadPayload,
  UploadSession
} from "./types";

type DbConfig = {
  host: string;
  port: number;
  database: string;
  user: string;
  password: string;
  ssl: boolean;
};

export type DbCounts = {
  devices: number;
  sessions: number;
  interventions: number;
};

export function getDbConfigFromEnv(env: NodeJS.ProcessEnv): DbConfig | null {
  const host = env.DB_HOST;
  const portStr = env.DB_PORT;
  const database = env.DB_NAME;
  const user = env.DB_USER;
  const password = env.DB_PASSWORD;
  const sslStr = env.DB_SSL;

  if (!host || !portStr || !database || !user || !password) return null;
  const port = Number(portStr);
  if (!Number.isFinite(port)) return null;

  const ssl = sslStr === "true" || sslStr === "1";
  return { host, port, database, user, password, ssl };
}

export async function insertUploadPayload(payload: UploadPayload, config: DbConfig): Promise<void> {
  const client = new Client({
    host: config.host,
    port: config.port,
    database: config.database,
    user: config.user,
    password: config.password,
    ssl: config.ssl ? { rejectUnauthorized: false } : undefined
  });

  await client.connect();
  try {
    await client.query("BEGIN");

    // devices: upsert by device_id. (Other columns may be null until Phase 12 expands.)
    await client.query(
      `
      INSERT INTO devices (device_id)
      VALUES ($1)
      ON CONFLICT (device_id) DO NOTHING
      `,
      [payload.device_id]
    );

    // Optional survey responses, captured once per device.
    if (payload.onboarding_response) {
      await insertOnboardingResponse(client, payload.device_id, payload.onboarding_response);
    }

    if (payload.exit_survey_response) {
      await insertExitSurveyResponse(client, payload.device_id, payload.exit_survey_response);
    }

    if (payload.sessions.length > 0) {
      for (const s of payload.sessions) {
        await insertSession(client, s);
      }
    }

    if (payload.interventions.length > 0) {
      for (const i of payload.interventions) {
        await insertIntervention(client, i);
      }
    }

    await client.query("COMMIT");
  } catch (e: any) {
    await client.query("ROLLBACK");
    // If schema hasn't been applied yet, create it and retry once.
    // Postgres missing table: SQLSTATE 42P01.
    if (e?.code === "42P01") {
      await ensureSchema(client);
      await client.query("BEGIN");
      try {
        await client.query(
          `
          INSERT INTO devices (device_id)
          VALUES ($1)
          ON CONFLICT (device_id) DO NOTHING
          `,
          [payload.device_id]
        );
        for (const s of payload.sessions) await insertSession(client, s);
        for (const i of payload.interventions) await insertIntervention(client, i);

        if (payload.onboarding_response) {
          await insertOnboardingResponse(client, payload.device_id, payload.onboarding_response);
        }
        if (payload.exit_survey_response) {
          await insertExitSurveyResponse(client, payload.device_id, payload.exit_survey_response);
        }

        await client.query("COMMIT");
        return;
      } catch (e2) {
        await client.query("ROLLBACK");
        throw e2;
      }
    }
    throw e;
  } finally {
    await client.end();
  }
}

export async function getCounts(config: DbConfig): Promise<DbCounts> {
  const client = new Client({
    host: config.host,
    port: config.port,
    database: config.database,
    user: config.user,
    password: config.password,
    ssl: config.ssl ? { rejectUnauthorized: false } : undefined
  });

  await client.connect();
  try {
    await ensureSchema(client);

    const devices = await client.query(`SELECT COUNT(*)::bigint AS c FROM devices`);
    const sessions = await client.query(`SELECT COUNT(*)::bigint AS c FROM sessions`);
    const interventions = await client.query(`SELECT COUNT(*)::bigint AS c FROM interventions`);
    return {
      devices: Number(devices.rows[0]?.c ?? 0),
      sessions: Number(sessions.rows[0]?.c ?? 0),
      interventions: Number(interventions.rows[0]?.c ?? 0)
    };
  } finally {
    await client.end();
  }
}

export async function exportSessionsCsv(config: DbConfig, sinceMs?: number): Promise<string> {
  const client = new Client({
    host: config.host,
    port: config.port,
    database: config.database,
    user: config.user,
    password: config.password,
    ssl: config.ssl ? { rejectUnauthorized: false } : undefined
  });

  await client.connect();
  try {
    await ensureSchema(client);
    const params: any[] = [];
    const where = sinceMs ? (params.push(sinceMs), `WHERE session_start_ts >= to_timestamp($1 / 1000.0)`) : "";
    const res = await client.query(
      `
      SELECT
        session_id,
        device_id,
        app_package_name,
        session_start_ts::text AS session_start_ts,
        session_end_ts::text AS session_end_ts,
        duration_seconds
      FROM sessions
      ${where}
      ORDER BY session_start_ts DESC
      `,
      params
    );

    const header = [
      "session_id",
      "device_id",
      "app_package_name",
      "session_start_ts",
      "session_end_ts",
      "duration_seconds"
    ];
    return toCsv(header, res.rows);
  } finally {
    await client.end();
  }
}

export async function exportInterventionsCsv(config: DbConfig, sinceMs?: number): Promise<string> {
  const client = new Client({
    host: config.host,
    port: config.port,
    database: config.database,
    user: config.user,
    password: config.password,
    ssl: config.ssl ? { rejectUnauthorized: false } : undefined
  });

  await client.connect();
  try {
    await ensureSchema(client);
    const params: any[] = [];
    const where = sinceMs
      ? (params.push(sinceMs), `WHERE intervention_start_ts >= to_timestamp($1 / 1000.0)`)
      : "";
    const res = await client.query(
      `
      SELECT
        intervention_id,
        session_id,
        device_id,
        milestone_minutes,
        prompt_variant,
        user_action,
        intervention_start_ts::text AS intervention_start_ts,
        intervention_end_ts::text AS intervention_end_ts
      FROM interventions
      ${where}
      ORDER BY intervention_start_ts DESC
      `,
      params
    );

    const header = [
      "intervention_id",
      "session_id",
      "device_id",
      "milestone_minutes",
      "prompt_variant",
      "user_action",
      "intervention_start_ts",
      "intervention_end_ts"
    ];
    return toCsv(header, res.rows);
  } finally {
    await client.end();
  }
}

async function insertOnboardingResponse(
  client: Client,
  deviceId: string,
  r: UploadOnboardingResponse
): Promise<void> {
  await client.query(
    `
    INSERT INTO onboarding_responses (
      device_id,
      onboarding_version,
      completed_at,
      trait_1, trait_2, trait_3,
      goal_1, goal_2, goal_3,
      role_1, role_2, role_3,
      automaticity, utility, intention
    ) VALUES (
      $1, $2, to_timestamp($3 / 1000.0),
      $4, $5, $6,
      $7, $8, $9,
      $10, $11, $12,
      $13, $14, $15
    )
    ON CONFLICT (device_id) DO UPDATE SET
      onboarding_version = EXCLUDED.onboarding_version,
      completed_at = EXCLUDED.completed_at,
      trait_1 = EXCLUDED.trait_1,
      trait_2 = EXCLUDED.trait_2,
      trait_3 = EXCLUDED.trait_3,
      goal_1 = EXCLUDED.goal_1,
      goal_2 = EXCLUDED.goal_2,
      goal_3 = EXCLUDED.goal_3,
      role_1 = EXCLUDED.role_1,
      role_2 = EXCLUDED.role_2,
      role_3 = EXCLUDED.role_3,
      automaticity = EXCLUDED.automaticity,
      utility = EXCLUDED.utility,
      intention = EXCLUDED.intention
    `,
    [
      deviceId,
      r.onboarding_version ?? null,
      r.completed_at ?? null,
      r.trait_1 ?? null,
      r.trait_2 ?? null,
      r.trait_3 ?? null,
      r.goal_1 ?? null,
      r.goal_2 ?? null,
      r.goal_3 ?? null,
      r.role_1 ?? null,
      r.role_2 ?? null,
      r.role_3 ?? null,
      r.automaticity ?? null,
      r.utility ?? null,
      r.intention ?? null
    ]
  );
}

async function insertExitSurveyResponse(
  client: Client,
  deviceId: string,
  r: UploadExitSurveyResponse
): Promise<void> {
  await client.query(
    `
    INSERT INTO exit_survey_responses (
      device_id,
      completed_at,
      interruption_awareness,
      decision_influence,
      helpfulness,
      frustration,
      pause_reconsider,
      easier_to_ignore,
      outside_use_likelihood,
      biggest_influence_aspect,
      own_words_effect,
      suggestions
    ) VALUES (
      $1, to_timestamp($2 / 1000.0),
      $3, $4, $5, $6,
      $7, $8, $9,
      $10, $11, $12
    )
    ON CONFLICT (device_id) DO UPDATE SET
      completed_at = EXCLUDED.completed_at,
      interruption_awareness = EXCLUDED.interruption_awareness,
      decision_influence = EXCLUDED.decision_influence,
      helpfulness = EXCLUDED.helpfulness,
      frustration = EXCLUDED.frustration,
      pause_reconsider = EXCLUDED.pause_reconsider,
      easier_to_ignore = EXCLUDED.easier_to_ignore,
      outside_use_likelihood = EXCLUDED.outside_use_likelihood,
      biggest_influence_aspect = EXCLUDED.biggest_influence_aspect,
      own_words_effect = EXCLUDED.own_words_effect,
      suggestions = EXCLUDED.suggestions
    `,
    [
      deviceId,
      r.completed_at ?? null,
      r.interruption_awareness ?? null,
      r.decision_influence ?? null,
      r.helpfulness ?? null,
      r.frustration ?? null,
      r.pause_reconsider ?? null,
      r.easier_to_ignore ?? null,
      r.outside_use_likelihood ?? null,
      r.biggest_influence_aspect ?? null,
      r.own_words_effect ?? null,
      r.suggestions ?? null
    ]
  );
}

async function ensureSchema(client: Client): Promise<void> {
  await client.query(`
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
  `);
}

function toCsv(header: string[], rows: Array<Record<string, any>>): string {
  const lines: string[] = [];
  lines.push(header.join(","));
  for (const row of rows) {
    const values = header.map((k) => csvEscape(row[k]));
    lines.push(values.join(","));
  }
  return lines.join("\n") + "\n";
}

function csvEscape(v: any): string {
  if (v === null || v === undefined) return "";
  const s = String(v);
  if (s.includes("\"") || s.includes(",") || s.includes("\n") || s.includes("\r")) {
    return `"${s.replace(/\"/g, "\"\"")}"`;
  }
  return s;
}

async function insertSession(client: Client, s: UploadSession): Promise<void> {
  await client.query(
    `
    INSERT INTO sessions (
      session_id,
      device_id,
      app_package_name,
      session_start_ts,
      session_end_ts,
      duration_seconds,
      created_at
    ) VALUES ($1, $2, $3, to_timestamp($4 / 1000.0), to_timestamp($5 / 1000.0), $6, NOW())
    ON CONFLICT (session_id) DO NOTHING
    `,
    [
      s.session_id,
      s.device_id,
      s.app_package_name,
      s.session_start_ts,
      s.session_end_ts ?? null,
      s.duration_seconds ?? null
    ]
  );
}

async function insertIntervention(client: Client, i: UploadIntervention): Promise<void> {
  await client.query(
    `
    INSERT INTO interventions (
      intervention_id,
      device_id,
      session_id,
      milestone_minutes,
      prompt_variant,
      user_action,
      intervention_start_ts,
      intervention_end_ts,
      created_at
    ) VALUES ($1, $2, $3, $4, $5, $6, to_timestamp($7 / 1000.0), to_timestamp($8 / 1000.0), NOW())
    ON CONFLICT (intervention_id) DO NOTHING
    `,
    [
      i.intervention_id,
      i.device_id,
      i.session_id,
      i.milestone_minutes,
      i.prompt_variant,
      i.user_action ?? null,
      i.intervention_start_ts,
      i.intervention_end_ts ?? null
    ]
  );
}

