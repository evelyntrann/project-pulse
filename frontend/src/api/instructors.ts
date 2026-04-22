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

export const instructorsApi = {
  generateInstructorLink: () =>
    api.post<{ success: boolean; data: InviteLinkResponse }>('/invitations/instructors/link'),

  listInstructors: () =>
    api.get<{ success: boolean; data: InstructorSummary[] }>('/instructors'),

  searchInstructors: (params: InstructorSearchParams) =>
    api.get<{ success: boolean; data: InstructorSearchResult[] }>('/instructors/search', { params }),
}
