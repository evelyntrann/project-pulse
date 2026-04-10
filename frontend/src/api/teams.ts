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

export const teamsApi = {
  findTeams: (params: { sectionId?: number; name?: string }) =>
    api.get<{ success: boolean; data: TeamSummary[] }>('/teams', { params }),
}
