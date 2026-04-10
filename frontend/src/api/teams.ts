import api from './axios'

export interface TeamMember {
  id: number
  firstName: string
  lastName: string
  email: string
}

export interface TeamSummary {
  id: number
  name: string
  description: string | null
  websiteUrl: string | null
  sectionId: number
  sectionName: string
  members: TeamMember[]
  instructors: TeamMember[]
}

export interface TeamDetail {
  id: number
  name: string
  description: string | null
  websiteUrl: string | null
  section: { id: number; name: string }
  members: TeamMember[]
  instructors: TeamMember[]
}

export interface TeamCreateRequest {
  name: string
  description: string
  websiteUrl: string
  sectionId: number
}

export const teamsApi = {
  findTeams: (params: { sectionId?: number; name?: string }) =>
    api.get<{ success: boolean; data: TeamSummary[] }>('/teams', { params }),

  getTeam: (id: number) =>
    api.get<{ success: boolean; data: TeamDetail }>(`/teams/${id}`),

  createTeam: (data: TeamCreateRequest) =>
    api.post<{ success: boolean; data: TeamDetail }>('/teams', data),
}
