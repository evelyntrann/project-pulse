import api from './axios'

export interface InviteLinkResponse {
  shareableLink: string
  expiresAt: string
}

export interface InvitationInfoResponse {
  role: string
  sectionName: string | null
  expiresAt: string
}

export interface StudentRegisterRequest {
  firstName: string
  middleInitial?: string | null
  lastName: string
  email: string
  password: string
}

export const invitationsApi = {
  generateInviteLink: (sectionId: number) =>
    api.post<{ success: boolean; data: InviteLinkResponse }>('/invitations/students/link', { sectionId }),

  getInvitationInfo: (token: string) =>
    api.get<{ success: boolean; data: InvitationInfoResponse }>(`/invitations/${token}`),

  registerViaToken: (token: string, data: StudentRegisterRequest) =>
    api.post<{ success: boolean; data: null }>(`/invitations/${token}/register`, data),
}
