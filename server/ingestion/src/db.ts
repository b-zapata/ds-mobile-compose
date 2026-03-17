import { Client } from "pg";
import { UploadIntervention, UploadPayload, UploadSession } from "./types";

type DbConfig = {
  host: string;
  port: number;
  database: string;
  user: string;
  password: string;
  ssl: boolean;
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
  } catch (e) {
    await client.query("ROLLBACK");
    throw e;
  } finally {
    await client.end();
  }
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

