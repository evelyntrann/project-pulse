import api from './axios'

// ── Interfaces ───────────────────────────────────────────────────────────────

export interface StudentSearchParams {
  firstName?: string
  lastName?: string
  email?: string
  sectionName?: string
  teamName?: string
  sectionId?: number
  teamId?: number
}

export interface StudentSearchResult {
  id: number
  firstName: string
  lastName: string
  email: string
  sectionName: string | null
  teamName: string | null
}

// ── API calls ────────────────────────────────────────────────────────────────

export const studentsApi = {
  searchStudents: (params: StudentSearchParams) =>
    api.get<{ success: boolean; data: StudentSearchResult[] }>('/students', { params }),
}
