export type UploadPayload = {
  device_id: string;
  enrolled_at?: number | null;
  sessions: UploadSession[];
  interventions: UploadIntervention[];
  onboarding_response?: UploadOnboardingResponse | null;
  exit_survey_response?: UploadExitSurveyResponse | null;
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
  intervention_arm: string;
  milestone_minutes: number;
  prompt_variant: number;
  user_action: string | null;
  intervention_start_ts: number;
  intervention_end_ts: number | null;
};

export type UploadOnboardingResponse = {
  onboarding_version: string | null;
  completed_at: number | null;

  trait_1: string | null;
  trait_2: string | null;
  trait_3: string | null;

  goal_1: string | null;
  goal_2: string | null;
  goal_3: string | null;

  role_1: string | null;
  role_2: string | null;
  role_3: string | null;

  automaticity: number | null;
  utility: number | null;
  intention: number | null;

  readiness_reduce_use: number | null;
  willingness_pause_task: number | null;
};

export type UploadExitSurveyResponse = {
  completed_at: number | null;

  interruption_awareness: number | null;
  decision_influence: number | null;
  helpfulness: number | null;
  frustration: number | null;
  pause_reconsider: number | null;
  easier_to_ignore: number | null;
  outside_use_likelihood: number | null;

  biggest_influence_aspect: string | null;
  own_words_effect: string | null;
  suggestions: string | null;
};
