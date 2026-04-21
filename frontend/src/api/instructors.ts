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

// ── API calls ────────────────────────────────────────────────────────────────

export const instructorsApi = {
  inviteInstructors: (data: InstructorInviteRequest) =>
    api.post<{ success: boolean; data: InstructorInviteResponse }>('/invitations/instructors', data),

  generateInstructorLink: () =>
    api.post<{ success: boolean; data: InviteLinkResponse }>('/invitations/instructors/link'),

  listInstructors: () =>
    api.get<{ success: boolean; data: InstructorSummary[] }>('/instructors'),
}
