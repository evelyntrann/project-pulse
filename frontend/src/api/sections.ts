import api from './axios'

export interface SectionSummary {
  id: number
  name: string
  startDate: string
  endDate: string
  teamNames: string[]
}

export interface TeamDto {
  id: number
  name: string
  description: string | null
  websiteUrl: string | null
  members: UserDto[]
  instructors: UserDto[]
}

export interface UserDto {
  id: number
  firstName: string
  lastName: string
  email: string
}

export interface SectionDetail {
  id: number
  name: string
  startDate: string
  endDate: string
  rubricId: number | null
  teams: TeamDto[]
  unassignedStudents: UserDto[]
  unassignedInstructors: UserDto[]
}

export const sectionsApi = {
  findSections: (name?: string) =>
    api.get<{ success: boolean; data: SectionSummary[] }>('/sections', {
      params: name ? { name } : {},
    }),

  getSection: (id: number) =>
    api.get<{ success: boolean; data: SectionDetail }>(`/sections/${id}`),
}
