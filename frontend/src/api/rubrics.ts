import api from './axios'

// ── Interfaces ───────────────────────────────────────────────────────────────

export interface CriterionRequest {
  name: string
  description: string
  maxScore: number
  orderIndex: number
}

export interface CriterionResponse {
  id: number
  name: string
  description: string
  maxScore: number
  orderIndex: number
}

export interface RubricCreateRequest {
  name: string
  criteria: CriterionRequest[]
}

export interface RubricSummary {
  id: number
  name: string
  criteria: CriterionResponse[]
}

// ── API calls ────────────────────────────────────────────────────────────────

export const rubricsApi = {
  createRubric: (data: RubricCreateRequest) =>
    api.post<{ success: boolean; data: RubricSummary }>('/rubrics', data),

  listRubrics: () =>
    api.get<{ success: boolean; data: RubricSummary[] }>('/rubrics'),

  getRubric: (id: number) =>
    api.get<{ success: boolean; data: RubricSummary }>(`/rubrics/${id}`),
}
