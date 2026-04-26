import api from './axios'

// ── Interfaces ───────────────────────────────────────────────────────────────

export interface UserDto {
  id: number
  firstName: string
  lastName: string
  email: string
}

export interface TeamDto {
  id: number
  name: string
  description: string | null
  websiteUrl: string | null
  members: UserDto[]
  instructors: UserDto[]
}

export interface SectionSummary {
  id: number
  name: string
  startDate: string
  endDate: string
  teamNames: string[]
}

export interface SectionDetail {
  id: number
  name: string
  startDate: string
  endDate: string
  rubricId: number | null
  isActive: boolean
  warWeeklyDueDay: string | null
  warDueTime: string | null
  peerEvaluationWeeklyDueDay: string | null
  peerEvaluationDueTime: string | null
  teams: TeamDto[]
  unassignedStudents: UserDto[]
  unassignedInstructors: UserDto[]
}

export interface SectionCreateRequest {
  name: string
  startDate: string
  endDate: string
  rubricId?: number | null
  isActive?: boolean
  warWeeklyDueDay?: string | null
  warDueTime?: string | null
  peerEvaluationWeeklyDueDay?: string | null
  peerEvaluationDueTime?: string | null
}

export interface ActiveWeek {
  weekStartDate: string
  isActive: boolean
}

export interface EnrolledStudent {
  id: number
  firstName: string
  lastName: string
  email: string
}

export interface InviteOrAddResult {
  added: string[]
  invited: string[]
  alreadyExists: string[]
}

// ── API calls ────────────────────────────────────────────────────────────────

export const sectionsApi = {
  // List / search
  findSections: (name?: string) =>
    api.get<{ success: boolean; data: SectionSummary[] }>('/sections', {
      params: name ? { name } : {},
    }),

  // Get single section with full detail
  getSection: (id: number) =>
    api.get<{ success: boolean; data: SectionDetail }>(`/sections/${id}`),

  // Create / update / delete
  createSection: (data: SectionCreateRequest) =>
    api.post<{ success: boolean; data: SectionDetail }>('/sections', data),

  updateSection: (id: number, data: SectionCreateRequest) =>
    api.put<{ success: boolean; data: SectionDetail }>(`/sections/${id}`, data),

  deleteSection: (id: number) =>
    api.delete<{ success: boolean; data: null }>(`/sections/${id}`),

  // Students
  getEnrolledStudents: (id: number) =>
    api.get<{ success: boolean; data: EnrolledStudent[] }>(`/sections/${id}/students`),

  // Active weeks
  getActiveWeeks: (id: number) =>
    api.get<{ success: boolean; data: ActiveWeek[] }>(`/sections/${id}/active-weeks`),

  setActiveWeeks: (id: number, weeks: ActiveWeek[]) =>
    api.put<{ success: boolean; data: ActiveWeek[] }>(`/sections/${id}/active-weeks`, { weeks }),

  // Rubric assignment
  assignRubric: (sectionId: number, rubricId: number) =>
    api.put<{ success: boolean; data: null }>(`/sections/${sectionId}/rubrics/${rubricId}`),

  // Instructor management
  getInstructors: (sectionId: number) =>
    api.get<{ success: boolean; data: UserDto[] }>(`/sections/${sectionId}/instructors`),

  assignInstructor: (sectionId: number, instructorId: number) =>
    api.put<{ success: boolean; data: null }>(`/sections/${sectionId}/instructors/${instructorId}`),

  removeInstructor: (sectionId: number, instructorId: number) =>
    api.delete<{ success: boolean; data: null }>(`/sections/${sectionId}/instructors/${instructorId}`),

  inviteOrAddInstructors: (sectionId: number, emails: string[]) =>
    api.post<{ success: boolean; data: InviteOrAddResult }>(`/sections/${sectionId}/instructors/invite-or-add`, emails),
}
