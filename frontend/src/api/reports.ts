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

// ── API calls ─────────────────────────────────────────────────────────────────

export const reportsApi = {
  getInstructorSections: () =>
    api.get<{ success: boolean; data: InstructorSection[] }>('/reports/instructor/sections'),

  getSectionAvailableWeeks: (sectionId: number) =>
    api.get<{ success: boolean; data: string[] }>(`/reports/peer-evaluations/sections/${sectionId}/available-weeks`),

  getPeerEvalSectionReport: (sectionId: number, weekStartDate: string) =>
    api.get<{ success: boolean; data: PeerEvalSectionReport }>(
      `/reports/peer-evaluations/sections/${sectionId}`,
      { params: { weekStartDate } }
    ),
}
