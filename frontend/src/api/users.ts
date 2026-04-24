import api from './axios'

export interface UpdateProfileRequest {
  firstName?: string
  lastName?: string
  email?: string
  password?: string
}

export const usersApi = {
  updateProfile: (id: number, data: UpdateProfileRequest) =>
    api.put<{ success: boolean; data: null }>(`/users/${id}`, data),
}
