import api from './axios'

// ── Types ─────────────────────────────────────────────────────────────────────

export interface InstructorSection {
  id: number
  name: string
}

export interface EvaluatorEntry {
  evaluatorName: string
  totalScore: number
  publicComment: string | null
  privateComment: string | null
}

export interface StudentPeerEvalEntry {
  studentId: number
  studentName: string
  grade: number
  evaluations: EvaluatorEntry[]
}

export interface PeerEvalSectionReport {
  weekStartDate: string
  sectionName: string
  maxGrade: number
  students: StudentPeerEvalEntry[]
  nonSubmitters: string[]
}

export interface PeerEvalWeekEntry {
  weekStartDate: string
  grade: number
  evaluations: EvaluatorEntry[]
}

export interface PeerEvalStudentReport {
  studentId: number
  studentName: string
  maxGrade: number
  weeks: PeerEvalWeekEntry[]
}

export interface InstructorTeam {
  id: number
  teamName: string
  sectionName: string
}

export interface WARActivityEntry {
  category: string
  plannedActivity: string
  description: string | null
  plannedHours: number | null
  actualHours: number | null
  status: string | null
}

export interface WARStudentEntry {
  studentId: number
  studentName: string
  activities: WARActivityEntry[]
}

export interface WARTeamReport {
  weekStartDate: string
  teamName: string
  students: WARStudentEntry[]
  nonSubmitters: string[]
}

// ── API calls ─────────────────────────────────────────────────────────────────

export const reportsApi = {
  getStudentAvailableWeeks: (studentId: number) =>
    api.get<{ success: boolean; data: string[] }>(`/reports/peer-evaluations/students/${studentId}/available-weeks`),

  getPeerEvalStudentReport: (studentId: number, startWeek: string, endWeek: string) =>
    api.get<{ success: boolean; data: PeerEvalStudentReport }>(
      `/reports/peer-evaluations/students/${studentId}`,
      { params: { startWeek, endWeek } }
    ),

  getInstructorSections: () =>
    api.get<{ success: boolean; data: InstructorSection[] }>('/reports/instructor/sections'),

  getSectionAvailableWeeks: (sectionId: number) =>
    api.get<{ success: boolean; data: string[] }>(`/reports/peer-evaluations/sections/${sectionId}/available-weeks`),

  getPeerEvalSectionReport: (sectionId: number, weekStartDate: string) =>
    api.get<{ success: boolean; data: PeerEvalSectionReport }>(
      `/reports/peer-evaluations/sections/${sectionId}`,
      { params: { weekStartDate } }
    ),

  getInstructorTeams: () =>
    api.get<{ success: boolean; data: InstructorTeam[] }>('/reports/war/instructor/teams'),

  getStudentTeam: () =>
    api.get<{ success: boolean; data: InstructorTeam }>('/reports/war/my-team'),

  getTeamAvailableWeeks: (teamId: number) =>
    api.get<{ success: boolean; data: string[] }>(`/reports/war/teams/${teamId}/available-weeks`),

  getWARTeamReport: (teamId: number, weekStartDate: string) =>
    api.get<{ success: boolean; data: WARTeamReport }>(
      `/reports/war/teams/${teamId}`,
      { params: { weekStartDate } }
    ),
}
