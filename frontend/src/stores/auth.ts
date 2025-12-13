import { defineStore } from 'pinia'

export type AuthState = {
  token: string | null
  email: string | null
  username: string | null
  role: string | null
}

function readPersisted(): AuthState {
  try {
    const token = localStorage.getItem('auth_token')
    const email = localStorage.getItem('auth_email')
    const username = localStorage.getItem('auth_username')
    const role = localStorage.getItem('auth_role')
    return { token, email, username, role }
  } catch {
    return { token: null, email: null, username: null, role: null }
  }
}



function persist(state: AuthState) {
  try {
    if (state.token) localStorage.setItem('auth_token', state.token)
    else localStorage.removeItem('auth_token')

    if (state.email) localStorage.setItem('auth_email', state.email)
    else localStorage.removeItem('auth_email')

    if (state.username) localStorage.setItem('auth_username', state.username)
    else localStorage.removeItem('auth_username')

    if (state.role) localStorage.setItem('auth_role', state.role)
    else localStorage.removeItem('auth_role')
  } catch {}
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    ...readPersisted(),
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => (state.role ? state.role.toUpperCase() === 'ADMIN' : false),
  },
  actions: {
    async login(email: string, password: string) {
      const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      })

      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.error || 'Login failed')
      }

      const data = await response.json()
      this.token = data.token
      this.email = data.email
      this.username = data.username
      this.role = data.role || null
      persist(this)
    },
    async updateUsername(newUsername: string) {
      if (!this.email) {
        throw new Error('No email found in auth state')
      }

      const response = await fetch(`${API_BASE_URL}/api/users/username?email=${encodeURIComponent(this.email)}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.token}`,
        },
        body: JSON.stringify({ username: newUsername }),
      })

      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.error || 'Failed to update username')
      }

      const data = await response.json()
      this.username = data.username
      persist(this)
    },
    setUsername(newUsername: string) {
      this.username = newUsername
      persist(this)
    },
    async register(username: string, email: string, password: string) {
      const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, email, password }),
      })

      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.error || 'Registration failed')
      }

      const data = await response.json()
      this.token = data.token
      this.email = data.email
      this.username = data.username
      this.role = data.role || null
      persist(this)
    },
    logout() {
      this.token = null
      this.email = null
      this.username = null
      this.role = null
      persist(this)
    },
  },
})
