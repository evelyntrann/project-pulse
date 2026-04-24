import api from './axios'

// ── Types ─────────────────────────────────────────────────────────────────────

export interface TeamMember {
  id: number
  firstName: string
  lastName: string
}

export interface CriterionDto {
  id: number
  name: string
  description: string
  maxScore: number
}

export interface ScoreSummary {
  criterionId: number
  score: number
}

export interface PeerEvalSummary {
  id: number
  evaluateeId: number
  scores: ScoreSummary[]
  publicComment: string | null
  privateComment: string | null
  submittedAt: string
}

export interface PeerEvalContext {
  teamMembers: TeamMember[]
  criteria: CriterionDto[]
  existingEvaluations: PeerEvalSummary[]
}

export interface ScoreEntry {
  criterionId: number
  score: number
}

export interface EvaluationEntry {
  evaluateeId: number
  scores: ScoreEntry[]
  publicComment: string | null
  privateComment: string | null
}

export interface PeerEvalSubmitRequest {
  weekStartDate: string
  evaluations: EvaluationEntry[]
}

// ── API calls ─────────────────────────────────────────────────────────────────

export const peerEvaluationsApi = {
  getAvailableWeeks: () =>
    api.get<{ success: boolean; data: string[] }>('/peer-evaluations/available-weeks'),

  getMySubmittedWeeks: () =>
    api.get<{ success: boolean; data: string[] }>('/peer-evaluations/my'),

  getContext: (weekStartDate: string) =>
    api.get<{ success: boolean; data: PeerEvalContext }>('/peer-evaluations/context', {
      params: { weekStartDate },
    }),

  submit: (data: PeerEvalSubmitRequest) =>
    api.post<{ success: boolean; data: PeerEvalSummary[] }>('/peer-evaluations', data),
}
