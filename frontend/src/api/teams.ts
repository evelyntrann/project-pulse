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

export interface TeamUpdateRequest {
  name: string
  description: string
  websiteUrl: string
}

export const teamsApi = {
  findTeams: (params: { sectionId?: number; name?: string }) =>
    api.get<{ success: boolean; data: TeamSummary[] }>('/teams', { params }),

  getTeam: (id: number) =>
    api.get<{ success: boolean; data: TeamDetail }>(`/teams/${id}`),

  createTeam: (data: TeamCreateRequest) =>
    api.post<{ success: boolean; data: TeamDetail }>('/teams', data),

  updateTeam: (id: number, data: TeamUpdateRequest) =>
    api.put<{ success: boolean; data: TeamDetail }>(`/teams/${id}`, data),

  assignStudents: (teamId: number, studentIds: number[]) =>
    api.post<{ success: boolean; data: TeamDetail }>(`/teams/${teamId}/students`, { studentIds }),

  removeStudent: (teamId: number, studentId: number) =>
    api.delete<{ success: boolean; data: TeamDetail }>(`/teams/${teamId}/students/${studentId}`),

  deleteTeam: (id: number) =>
    api.delete<{ success: boolean; data: null }>(`/teams/${id}`),

  assignInstructor: (teamId: number, instructorId: number) =>
    api.put<{ success: boolean; data: null }>(`/teams/${teamId}/instructors/${instructorId}`),

  removeInstructor: (teamId: number, instructorId: number) =>
    api.delete<{ success: boolean; data: null }>(`/teams/${teamId}/instructors/${instructorId}`),

  transferTeam: (teamId: number, sectionId: number) =>
    api.patch<{ success: boolean; data: TeamTransferResponse }>(`/teams/${teamId}/section`, { sectionId }),
}

export interface TeamTransferResponse {
  teamId: number
  teamName: string
  fromSectionId: number
  fromSectionName: string
  toSectionId: number
  toSectionName: string
  studentsTransferred: number
}
