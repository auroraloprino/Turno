import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export type Role = 'OWNER' | 'ADMIN' | 'USER'

export interface AuthUser {
  id: number
  name: string
  email: string
  role: Role
}

interface LoginResponse {
  token: string
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
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    })

    if (!res.ok) throw new Error('Credenziali non valide')

    const data: LoginResponse = await res.json()

    token.value = data.token
    user.value  = { id: data.id, name: data.name, email: data.email, role: data.role }
    localStorage.setItem('token', data.token)
    localStorage.setItem('user',  JSON.stringify(user.value))
  }

  function logout(): void {
    token.value = null
    user.value  = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, isAuthenticated, role, login, logout }
})
