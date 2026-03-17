export type UploadPayload = {
  device_id: string;
  sessions: UploadSession[];
  interventions: UploadIntervention[];
};

export type UploadSession = {
  session_id: string;
  device_id: string;
  app_package_name: string;
  session_start_ts: number;
  session_end_ts: number | null;
  duration_seconds: number | null;
};

export type UploadIntervention = {
  intervention_id: string;
  session_id: string;
  device_id: string;
  milestone_minutes: number;
  prompt_variant: number;
  user_action: string | null;
  intervention_start_ts: number;
  intervention_end_ts: number | null;
};

