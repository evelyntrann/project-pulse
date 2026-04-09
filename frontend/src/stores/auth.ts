import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'

interface User {
  id: number
  email: string
  firstName: string
  lastName: string
  role: 'ADMIN' | 'INSTRUCTOR' | 'STUDENT'
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))

  const isLoggedIn = computed(() => !!token.value)

  async function login(email: string, password: string) {
    const res = await authApi.login(email, password)
    token.value = res.data.data.token
    user.value = res.data.data.user
    localStorage.setItem('token', token.value!)
  }

  async function fetchMe() {
    const res = await authApi.me()
    user.value = res.data.data
  }

  function logout() {
    authApi.logout().catch(() => {})
    token.value = null
    user.value = null
    localStorage.removeItem('token')
  }

  return { user, token, isLoggedIn, login, logout, fetchMe }
})
