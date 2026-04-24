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

export interface StudentDetail {
  id: number
  firstName: string
  lastName: string
  email: string
  sectionName: string | null
  teamName: string | null
}

// ── API calls ────────────────────────────────────────────────────────────────

export interface StudentRegisterRequest {
  token: string
  firstName: string
  lastName: string
  email: string
  password: string
}

export const studentsApi = {
  register: (data: StudentRegisterRequest) =>
    api.post<{ success: boolean; data: null }>('/students/register', data),

  searchStudents: (params: StudentSearchParams) =>
    api.get<{ success: boolean; data: StudentSearchResult[] }>('/students', { params }),

  getStudent: (id: number) =>
    api.get<{ success: boolean; data: StudentDetail }>(`/students/${id}`),

  deleteStudent: (id: number) =>
    api.delete<{ success: boolean; data: null }>(`/students/${id}`),
}
