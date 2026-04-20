import api from './axios'

// UC-25 Step 2 support — response from the token validation call
export interface InvitationValidation {
  email: string
  role: 'STUDENT' | 'INSTRUCTOR'
  sectionName: string | null
  expired: boolean
  alreadyUsed: boolean
}

// UC-25 POST-1 — what comes back after successful registration
export interface RegisterResponse {
  id: number
  email: string
  firstName: string
  lastName: string
  role: string
}

export const registrationApi = {
  // Called on page load to check the token before showing the form (UC-25 Step 2)
  validateToken: (token: string) =>
    api.get<{ success: boolean; data: InvitationValidation }>('/invitations/validate', {
      params: { token },
    }),

  // UC-25 Steps 3–7: submit the student registration form
  registerStudent: (data: { token: string; firstName: string; lastName: string; password: string }) =>
    api.post<{ success: boolean; data: RegisterResponse }>('/students/register', data),

  // UC-30 (Instructor account setup — Micah owns this too, stubbed for now)
  registerInstructor: (data: { token: string; firstName: string; lastName: string; password: string }) =>
    api.post<{ success: boolean; data: RegisterResponse }>('/instructors/register', data),
}
