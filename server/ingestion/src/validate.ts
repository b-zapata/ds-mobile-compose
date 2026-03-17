import { UploadIntervention, UploadPayload, UploadSession } from "./types";

type ValidationOk<T> = { ok: true; value: T };
type ValidationErr = { ok: false; message: string };

function isRecord(v: unknown): v is Record<string, unknown> {
  return typeof v === "object" && v !== null && !Array.isArray(v);
}

function isString(v: unknown): v is string {
  return typeof v === "string";
}

function isNumber(v: unknown): v is number {
  return typeof v === "number" && Number.isFinite(v);
}

function isNullableNumber(v: unknown): v is number | null {
  return v === null || isNumber(v);
}

function isNullableString(v: unknown): v is string | null {
  return v === null || isString(v);
}

function validateSession(v: unknown): ValidationOk<UploadSession> | ValidationErr {
  if (!isRecord(v)) return { ok: false, message: "sessions[] must contain objects" };

  const required = [
    "session_id",
    "device_id",
    "app_package_name",
    "session_start_ts",
    "session_end_ts",
    "duration_seconds"
  ] as const;

  for (const key of required) {
    if (!(key in v)) return { ok: false, message: `session missing field: ${key}` };
  }

  if (!isString(v.session_id)) return { ok: false, message: "session.session_id must be string" };
  if (!isString(v.device_id)) return { ok: false, message: "session.device_id must be string" };
  if (!isString(v.app_package_name)) return { ok: false, message: "session.app_package_name must be string" };
  if (!isNumber(v.session_start_ts)) return { ok: false, message: "session.session_start_ts must be number" };
  if (!isNullableNumber(v.session_end_ts)) return { ok: false, message: "session.session_end_ts must be number|null" };
  if (!isNullableNumber(v.duration_seconds)) return { ok: false, message: "session.duration_seconds must be number|null" };

  const session: UploadSession = {
    session_id: v.session_id,
    device_id: v.device_id,
    app_package_name: v.app_package_name,
    session_start_ts: v.session_start_ts,
    session_end_ts: v.session_end_ts,
    duration_seconds: v.duration_seconds
  };

  return { ok: true, value: session };
}

function validateIntervention(v: unknown): ValidationOk<UploadIntervention> | ValidationErr {
  if (!isRecord(v)) return { ok: false, message: "interventions[] must contain objects" };

  const required = [
    "intervention_id",
    "session_id",
    "device_id",
    "milestone_minutes",
    "prompt_variant",
    "user_action",
    "intervention_start_ts",
    "intervention_end_ts"
  ] as const;

  for (const key of required) {
    if (!(key in v)) return { ok: false, message: `intervention missing field: ${key}` };
  }

  if (!isString(v.intervention_id)) return { ok: false, message: "intervention.intervention_id must be string" };
  if (!isString(v.session_id)) return { ok: false, message: "intervention.session_id must be string" };
  if (!isString(v.device_id)) return { ok: false, message: "intervention.device_id must be string" };
  if (!isNumber(v.milestone_minutes)) return { ok: false, message: "intervention.milestone_minutes must be number" };
  if (!isNumber(v.prompt_variant)) return { ok: false, message: "intervention.prompt_variant must be number" };
  if (!isNullableString(v.user_action)) return { ok: false, message: "intervention.user_action must be string|null" };
  if (!isNumber(v.intervention_start_ts)) return { ok: false, message: "intervention.intervention_start_ts must be number" };
  if (!isNullableNumber(v.intervention_end_ts)) return { ok: false, message: "intervention.intervention_end_ts must be number|null" };

  const intervention: UploadIntervention = {
    intervention_id: v.intervention_id,
    session_id: v.session_id,
    device_id: v.device_id,
    milestone_minutes: v.milestone_minutes,
    prompt_variant: v.prompt_variant,
    user_action: v.user_action,
    intervention_start_ts: v.intervention_start_ts,
    intervention_end_ts: v.intervention_end_ts
  };

  return { ok: true, value: intervention };
}

export function validatePayload(body: unknown): ValidationOk<UploadPayload> | ValidationErr {
  if (!isRecord(body)) return { ok: false, message: "body must be an object" };
  if (!("device_id" in body) || !isString(body.device_id)) {
    return { ok: false, message: "device_id must be a string" };
  }
  if (!("sessions" in body) || !Array.isArray(body.sessions)) {
    return { ok: false, message: "sessions must be an array" };
  }
  if (!("interventions" in body) || !Array.isArray(body.interventions)) {
    return { ok: false, message: "interventions must be an array" };
  }

  const sessions: UploadSession[] = [];
  for (const s of body.sessions) {
    const res = validateSession(s);
    if (!res.ok) return res;
    sessions.push(res.value);
  }

  const interventions: UploadIntervention[] = [];
  for (const i of body.interventions) {
    const res = validateIntervention(i);
    if (!res.ok) return res;
    interventions.push(res.value);
  }

  return {
    ok: true,
    value: {
      device_id: body.device_id,
      sessions,
      interventions
    }
  };
}

