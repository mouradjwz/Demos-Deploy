<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const error = ref('')

async function handleRegister() {
  error.value = ''
  
  // Validation
  if (!username.value || !email.value || !password.value || !confirmPassword.value) {
    error.value = 'Vul alle velden in'
    return
  }

  if (password.value.length < 8) {
    error.value = 'Wachtwoord moet minimaal 8 tekens bevatten'
    return
  }

  if (password.value !== confirmPassword.value) {
    error.value = 'Wachtwoorden komen niet overeen'
    return
  }

  loading.value = true
  try {
    await authStore.register(username.value, email.value, password.value)
    router.push('/')
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Registratie mislukt'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-container">
    <div class="register-card">
      <h1>Registreren</h1>
      <p class="subtitle">Maak een account aan om toegang te krijgen tot het forum</p>
      
      <form @submit.prevent="handleRegister" class="register-form">
        <div class="form-group">
          <label for="username">Gebruikersnaam</label>
          <input
            id="username"
            v-model="username"
            type="text"
            placeholder="Jouw naam"
            :disabled="loading"
            required
          />
        </div>

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
            placeholder="Minimaal 8 tekens"
            :disabled="loading"
            required
          />
        </div>

        <div class="form-group">
          <label for="confirmPassword">Bevestig wachtwoord</label>
          <input
            id="confirmPassword"
            v-model="confirmPassword"
            type="password"
            placeholder="Herhaal je wachtwoord"
            :disabled="loading"
            required
          />
        </div>

        <div v-if="error" class="error-message">
          {{ error }}
        </div>

        <button type="submit" class="register-button" :disabled="loading">
          {{ loading ? 'Bezig met registreren...' : 'Registreren' }}
        </button>
      </form>

      <p class="login-link-text">
        Heb je al een account? 
        <router-link to="/login" class="login-link">Inloggen</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
  padding: 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  padding: 3rem;
  max-width: 450px;
  width: 100%;
}

.register-card h1 {
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

.register-form {
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

.register-button {
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

.register-button:hover:not(:disabled) {
  background-color: #0f2f52;
}

.register-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.login-link-text {
  text-align: center;
  margin-top: 1.5rem;
  color: #666;
  font-size: 0.95rem;
}

.login-link {
  color: #154273;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s;
}

.login-link:hover {
  color: #0f2f52;
  text-decoration: underline;
}

@media (max-width: 600px) {
  .register-card {
    padding: 2rem 1.5rem;
  }
  
  .register-card h1 {
    font-size: 1.5rem;
  }
}
</style>
