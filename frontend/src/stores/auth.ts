import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export type Role = 'admin' | 'user'

export interface AuthUser {
  id: number
  name: string
  email: string
  role: Role
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user  = ref<AuthUser | null>(JSON.parse(localStorage.getItem('user') ?? 'null'))

  const isAuthenticated = computed(() => !!token.value)
  const role            = computed(() => user.value?.role ?? null)

  async function login(email: string, password: string): Promise<void> {
    if (!email || !password) throw new Error('Credenziali mancanti')

    const mockUser: AuthUser = email.toLowerCase().includes('admin')
      ? { id: 1, name: 'Admin Turno', email, role: 'admin' }
      : { id: 2, name: 'Mario Rossi',  email, role: 'user'  }

    const mockToken = btoa(`${mockUser.id}:${mockUser.role}:${Date.now()}`)

    token.value = mockToken
    user.value  = mockUser
    localStorage.setItem('token', mockToken)
    localStorage.setItem('user',  JSON.stringify(mockUser))
  }

  function logout(): void {
    token.value = null
    user.value  = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, isAuthenticated, role, login, logout }
})
