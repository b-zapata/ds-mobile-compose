import { Client } from "pg";
import https from "https";

type CloudFormationEvent = {
  RequestType?: "Create" | "Update" | "Delete";
  ResponseURL?: string;
  StackId?: string;
  RequestId?: string;
  LogicalResourceId?: string;
  PhysicalResourceId?: string;
};

const SCHEMA_SQL = `
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
`;

async function sendResponse(
  responseUrl: string,
  status: "SUCCESS" | "FAILED",
  reason: string,
  data: Record<string, any>,
  event: CloudFormationEvent
) {
  const body = JSON.stringify({
    Status: status,
    Reason: reason,
    PhysicalResourceId: event.PhysicalResourceId ?? "schema-setup",
    StackId: event.StackId,
    RequestId: event.RequestId,
    LogicalResourceId: event.LogicalResourceId,
    NoEcho: false,
    Data: data
  });

  console.log(`[sendResponse] Starting CloudFormation callback to ${responseUrl}`);
  console.log(`[sendResponse] Status: ${status}, Reason: ${reason}`);

  await new Promise<void>((resolve, reject) => {
    const url = new URL(responseUrl);
    const timeoutHandle = setTimeout(() => {
      console.error(`[sendResponse] TIMEOUT after 5s waiting for CloudFormation response`);
      req.destroy();
      reject(new Error("CloudFormation callback timeout (5s)"));
    }, 5000);

    const req = https.request(
      {
        hostname: url.hostname,
        path: `${url.pathname}${url.search}`,
        method: "PUT",
        headers: {
          "content-type": "",
          "content-length": Buffer.byteLength(body)
        }
      },
      (res) => {
        console.log(`[sendResponse] Got response from CloudFormation: status ${res.statusCode}`);
        clearTimeout(timeoutHandle);
        if ((res.statusCode ?? 500) >= 400) {
          reject(new Error(`Failed to send CloudFormation response: ${res.statusCode}`));
          return;
        }
        res.on("data", () => undefined);
        res.on("end", () => {
          console.log(`[sendResponse] Response ended, resolving`);
          resolve();
        });
      }
    );

    req.on("error", (err) => {
      console.error(`[sendResponse] Request error: ${err.message}`);
      clearTimeout(timeoutHandle);
      reject(err);
    });

    console.log(`[sendResponse] Writing body and ending request`);
    req.write(body);
    req.end();
  });

  console.log(`[sendResponse] CloudFormation callback completed successfully`);
}

function getEnvOrThrow(name: string): string {
  const v = process.env[name];
  if (!v) throw new Error(`Missing env var ${name}`);
  return v;
}

export const handler = async (event: CloudFormationEvent): Promise<{ ok: boolean; mode: string } | void> => {
  console.log(`[handler] Starting with event:`, JSON.stringify(event, null, 2));

  const responseUrl = event.ResponseURL;
  const isCloudFormationInvoke = Boolean(responseUrl);
  const requestType = event.RequestType ?? "Update";

  console.log(`[handler] isCloudFormationInvoke=${isCloudFormationInvoke}, requestType=${requestType}`);

  try {
    if (requestType === "Delete") {
      console.log(`[handler] Handling Delete request`);
      if (isCloudFormationInvoke && responseUrl) {
        await sendResponse(responseUrl, "SUCCESS", "Delete: nothing to do", {}, event);
      }
      return;
    }

    const client = new Client({
      host: getEnvOrThrow("DB_HOST"),
      port: Number(getEnvOrThrow("DB_PORT")),
      database: getEnvOrThrow("DB_NAME"),
      user: getEnvOrThrow("DB_USER"),
      password: getEnvOrThrow("DB_PASSWORD"),
      ssl: getEnvOrThrow("DB_SSL") === "true" ? { rejectUnauthorized: false } : undefined
    });

    try {
      console.log(`[handler] Using embedded schema SQL`);
      console.log(`[handler] Schema loaded, ${SCHEMA_SQL.length} bytes`);

      console.log(`[handler] Connecting to database`);
      await client.connect();
      console.log(`[handler] Connected, applying schema`);
      await client.query("BEGIN");
      await client.query(SCHEMA_SQL);
      await client.query("COMMIT");
      console.log(`[handler] Schema applied successfully`);

      if (isCloudFormationInvoke && responseUrl) {
        console.log(`[handler] Sending CloudFormation SUCCESS response`);
        await sendResponse(responseUrl, "SUCCESS", "Schema applied", { ok: true }, event);
        return;
      }
      return { ok: true, mode: "direct" };
    } catch (e: any) {
      console.error(`[handler] Error during schema setup: ${e?.message ?? String(e)}`, e?.stack);
      try {
        await client.query("ROLLBACK");
        console.log(`[handler] Rolled back transaction`);
      } catch {
        // ignore
      }
      if (isCloudFormationInvoke && responseUrl) {
        console.log(`[handler] Sending CloudFormation FAILED response`);
        await sendResponse(
          responseUrl,
          "FAILED",
          `Schema setup failed: ${e?.message ?? String(e)}`,
          { ok: false },
          event
        );
        return;
      }
      throw e;
    } finally {
      console.log(`[handler] Closing database connection`);
      await client.end().catch(() => undefined);
    }
  } catch (e) {
    console.error(`[handler] Unhandled error:`, e);
    throw e;
  }
};

