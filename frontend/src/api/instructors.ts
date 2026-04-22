import api from './axios'

export interface InviteLinkResponse {
  shareableLink: string
  expiresAt: string
}

export const instructorsApi = {
  generateInstructorLink: () =>
    api.post<{ success: boolean; data: InviteLinkResponse }>('/invitations/instructors/link'),
}
