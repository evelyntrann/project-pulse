import api from './axios'

export interface StudentInviteRequest {
  sectionId: number
  emails: string[]
  customMessage?: string
}

export interface StudentInviteResponse {
  sentCount: number
  emails: string[]
}

export const invitationsApi = {
  inviteStudents: (data: StudentInviteRequest) =>
    api.post<{ success: boolean; data: StudentInviteResponse }>('/invitations/students', data),
}
