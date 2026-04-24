import api from './axios'

// ── Interfaces ───────────────────────────────────────────────────────────────

export interface ActivityData {
  category: string
  plannedActivity: string
  description?: string | null
  plannedHours: number | string
  actualHours?: number | string | null
  status?: string | null
}

export interface ActivityResponse {
  id: number
  category: string
  plannedActivity: string
  description: string | null
  plannedHours: number
  actualHours: number | null
  status: string | null
}

export interface WARSummary {
  id: number
  weekStartDate: string
  status: string
  submittedAt: string | null
}

export interface WARDetail {
  id: number
  weekStartDate: string
  status: string
  submittedAt: string | null
  activities: ActivityResponse[]
}

// ── API calls ────────────────────────────────────────────────────────────────

export const warsApi = {
  getAvailableWeeks: () =>
    api.get<{ success: boolean; data: string[] }>('/wars/available-weeks'),

  getWARs: () =>
    api.get<{ success: boolean; data: WARSummary[] }>('/wars'),

  getWAR: (id: number) =>
    api.get<{ success: boolean; data: WARDetail }>(`/wars/${id}`),

  createWAR: (weekStartDate: string) =>
    api.post<{ success: boolean; data: WARDetail }>('/wars', { weekStartDate }),

  submitWAR: (id: number) =>
    api.patch<{ success: boolean; data: WARDetail }>(`/wars/${id}/submit`),

  addActivity: (warId: number, data: ActivityData) =>
    api.post<{ success: boolean; data: WARDetail }>(`/wars/${warId}/activities`, data),

  updateActivity: (warId: number, activityId: number, data: ActivityData) =>
    api.put<{ success: boolean; data: WARDetail }>(`/wars/${warId}/activities/${activityId}`, data),

  deleteActivity: (warId: number, activityId: number) =>
    api.delete<{ success: boolean; data: WARDetail }>(`/wars/${warId}/activities/${activityId}`),
}
