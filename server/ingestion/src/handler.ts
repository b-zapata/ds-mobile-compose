import { insertUploadPayload, getDbConfigFromEnv } from "./db";
import { validatePayload } from "./validate";

type ApiResult = {
  statusCode: number;
  headers?: Record<string, string>;
  body: string;
};

function json(statusCode: number, body: unknown): ApiResult {
  return {
    statusCode,
    headers: {
      "content-type": "application/json"
    },
    body: JSON.stringify(body)
  };
}

export const handler = async (event: any): Promise<ApiResult> => {
  if (!event.body) {
    return json(400, { ok: false, error: "Missing body" });
  }

  let parsed: unknown;
  try {
    parsed = JSON.parse(event.body);
  } catch {
    return json(400, { ok: false, error: "Body must be valid JSON" });
  }

  const validated = validatePayload(parsed);
  if (!validated.ok) {
    return json(400, { ok: false, error: validated.message });
  }

  const payload = validated.value;
  console.log("INGESTION_REQUEST", {
    device_id: payload.device_id,
    sessions: payload.sessions.length,
    interventions: payload.interventions.length
  });

  const dbConfig = getDbConfigFromEnv(process.env);
  if (!dbConfig) {
    console.log("DB_NOT_CONFIGURED: skipping insert");
    return json(200, { ok: true, inserted: false });
  }

  try {
    await insertUploadPayload(payload, dbConfig);
    return json(200, { ok: true, inserted: true });
  } catch (e) {
    console.error("DB_INSERT_FAILED", e);
    return json(500, { ok: false, error: "DB insert failed" });
  }
};

