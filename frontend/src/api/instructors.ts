import api from './axios'

// ── Interfaces ───────────────────────────────────────────────────────────────

export interface InstructorInviteRequest {
  emails: string[]
  customMessage?: string
}

export interface InstructorInviteResponse {
  count: number
  emails: string[]
}

export interface InviteLinkResponse {
  shareableLink: string
  expiresAt: string
}

export interface InstructorSummary {
  id: number
  firstName: string
  lastName: string
  email: string
}

export interface InstructorSearchResult {
  id: number
  firstName: string
  lastName: string
  email: string
  active: boolean
  teamNames: string | null
}

export interface InstructorSearchParams {
  firstName?: string
  lastName?: string
  teamName?: string
  active?: boolean
}

export interface InstructorDetail {
  id: number
  firstName: string
  lastName: string
  email: string
  active: boolean
  supervisedTeams: Array<{
    sectionId: number
    sectionName: string
    teams: Array<{ id: number; name: string }>
  }>
}

// ── API calls ────────────────────────────────────────────────────────────────

export const instructorsApi = {
  inviteInstructors: (data: InstructorInviteRequest) =>
    api.post<{ success: boolean; data: InstructorInviteResponse }>('/invitations/instructors', data),

  generateInstructorLink: () =>
    api.post<{ success: boolean; data: InviteLinkResponse }>('/invitations/instructors/link'),

  listInstructors: () =>
    api.get<{ success: boolean; data: InstructorSummary[] }>('/instructors'),

  searchInstructors: (params: InstructorSearchParams) =>
    api.get<{ success: boolean; data: InstructorSearchResult[] }>('/instructors/search', { params }),

  getInstructor: (id: number) =>
    api.get<{ success: boolean; data: InstructorDetail }>(`/instructors/${id}`),

  deactivateInstructor: (id: number, reason: string) =>
    api.patch<{ success: boolean; data: null }>(`/instructors/${id}/deactivate`, { reason }),
}
