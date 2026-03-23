import {
  exportInterventionsCsv,
  exportSessionsCsv,
  getCounts,
  getDbConfigFromEnv,
  insertUploadPayload,
} from "./db";
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
      "content-type": "application/json",
    },
    body: JSON.stringify(body),
  };
}

function text(
  statusCode: number,
  body: string,
  contentType: string,
): ApiResult {
  return {
    statusCode,
    headers: {
      "content-type": contentType,
    },
    body,
  };
}

function requireAdmin(event: any): ApiResult | null {
  const token = process.env.ADMIN_TOKEN;
  if (!token) {
    return json(404, { ok: false, error: "Not found" });
  }
  const headerToken = (event.headers?.["x-admin-token"] ??
    event.headers?.["X-Admin-Token"] ??
    event.headers?.["x-admin-token".toUpperCase()]) as string | undefined;
  if (!headerToken || headerToken !== token) {
    return json(401, { ok: false, error: "Unauthorized" });
  }
  return null;
}

export const handler = async (event: any): Promise<ApiResult> => {
  const method =
    event?.requestContext?.http?.method ?? event?.httpMethod ?? "POST";
  const path = event?.rawPath ?? event?.path ?? "";

  if (path.startsWith("/admin")) {
    const authErr = requireAdmin(event);
    if (authErr) return authErr;
    const dbConfig = getDbConfigFromEnv(process.env);
    if (!dbConfig) return json(500, { ok: false, error: "DB not configured" });

    try {
      if (method === "GET" && path === "/admin/counts") {
        const counts = await getCounts(dbConfig);
        return json(200, { ok: true, counts });
      }
      if (method === "GET" && path === "/admin/sessions.csv") {
        const since = event?.queryStringParameters?.since_ms;
        const sinceMs = since ? Number(since) : undefined;
        const csv = await exportSessionsCsv(
          dbConfig,
          Number.isFinite(sinceMs as any) ? sinceMs : undefined,
        );
        return text(200, csv, "text/csv; charset=utf-8");
      }
      if (method === "GET" && path === "/admin/interventions.csv") {
        const since = event?.queryStringParameters?.since_ms;
        const sinceMs = since ? Number(since) : undefined;
        const csv = await exportInterventionsCsv(
          dbConfig,
          Number.isFinite(sinceMs as any) ? sinceMs : undefined,
        );
        return text(200, csv, "text/csv; charset=utf-8");
      }
      return json(404, { ok: false, error: "Not found" });
    } catch (e) {
      console.error("ADMIN_ENDPOINT_FAILED", e);
      return json(500, { ok: false, error: "Internal error" });
    }
  }

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
    enrolled_at: payload.enrolled_at ?? null,
    sessions: payload.sessions.length,
    interventions: payload.interventions.length,
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
