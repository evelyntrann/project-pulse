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

// ── API calls ────────────────────────────────────────────────────────────────

export const instructorsApi = {
  inviteInstructors: (data: InstructorInviteRequest) =>
    api.post<{ success: boolean; data: InstructorInviteResponse }>('/invitations/instructors', data),
}
