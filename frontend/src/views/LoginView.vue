<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  error.value = ''
  
  if (!email.value || !password.value) {
    error.value = 'Vul alle velden in'
    return
  }

  loading.value = true
  try {
    await authStore.login(email.value, password.value)
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Login mislukt'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <h1>Inloggen</h1>
      <p class="subtitle">Log in om toegang te krijgen tot het forum</p>
      
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="email">E-mailadres</label>
          <input
            id="email"
            v-model="email"
            type="email"
            placeholder="voorbeeld@email.nl"
            :disabled="loading"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">Wachtwoord</label>
          <input
            id="password"
            v-model="password"
            type="password"
            placeholder="Minimaal 4 tekens"
            :disabled="loading"
            required
          />
        </div>

        <div v-if="error" class="error-message">
          {{ error }}
        </div>

        <button type="submit" class="login-button" :disabled="loading">
          {{ loading ? 'Bezig met inloggen...' : 'Inloggen' }}
        </button>
      </form>

      <p class="register-link-text">
        Nog geen account? 
        <router-link to="/register" class="register-link">Registreren</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
  padding: 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  padding: 3rem;
  max-width: 450px;
  width: 100%;
}

.login-card h1 {
  margin: 0 0 0.5rem 0;
  font-size: 2rem;
  color: #154273;
  text-align: center;
}

.subtitle {
  text-align: center;
  color: #666;
  margin-bottom: 2rem;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 600;
  color: #333;
  font-size: 0.95rem;
}

.form-group input {
  padding: 0.875rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #154273;
}

.form-group input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.error-message {
  background-color: #fee;
  color: #c33;
  padding: 0.875rem;
  border-radius: 8px;
  border-left: 4px solid #c33;
  font-size: 0.95rem;
}

.login-button {
  background-color: #154273;
  color: white;
  padding: 1rem;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.login-button:hover:not(:disabled) {
  background-color: #0f2f52;
}

.login-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.mock-info {
  margin-top: 1.5rem;
  padding: 1rem;
  background-color: #f0f7ff;
  border-left: 4px solid #154273;
  border-radius: 4px;
  font-size: 0.9rem;
  color: #333;
}

.mock-info strong {
  color: #154273;
}

.register-link-text {
  text-align: center;
  margin-top: 1.5rem;
  color: #666;
  font-size: 0.95rem;
}

.register-link {
  color: #154273;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s;
}

.register-link:hover {
  color: #0f2f52;
  text-decoration: underline;
}

@media (max-width: 600px) {
  .login-card {
    padding: 2rem 1.5rem;
  }
  
  .login-card h1 {
    font-size: 1.5rem;
  }
}
</style>
