import api from './axios'

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

export const instructorsApi = {
  generateInstructorLink: () =>
    api.post<{ success: boolean; data: InviteLinkResponse }>('/invitations/instructors/link'),

  listInstructors: () =>
    api.get<{ success: boolean; data: InstructorSummary[] }>('/instructors'),
}
