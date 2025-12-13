<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

/**
 * @author Mohammed Laghmani
 * @about This page includes the routing
 */

const authStore = useAuthStore()
const router = useRouter()
const showProfileModal = ref(false)
const editedUsername = ref('')
const saveMessage = ref('')

function handleLogout() {
  authStore.logout()
  router.push('/')
}

function toggleProfileModal() {
  showProfileModal.value = !showProfileModal.value
  if (showProfileModal.value) {
    editedUsername.value = authStore.username || ''
    saveMessage.value = ''
  }
}

function closeProfileModal() {
  showProfileModal.value = false
  saveMessage.value = ''
}

async function saveProfile() {
  if (!editedUsername.value.trim()) {
    saveMessage.value = 'Gebruikersnaam mag niet leeg zijn'
    return
  }
  
  if (editedUsername.value.trim().length < 4) {
    saveMessage.value = 'Gebruikersnaam moet minimaal 4 tekens zijn'
    return
  }
  
  try {
    await authStore.updateUsername(editedUsername.value.trim())
    saveMessage.value = 'Profiel succesvol opgeslagen!'
    
    setTimeout(() => {
      closeProfileModal()
    }, 1500)
  } catch (error: any) {
    saveMessage.value = error.message || 'Fout bij opslaan van profiel'
  }
}
</script>



<template>
  <header class="header">
    <section class="top-section">
      <div class="logo-title">
        <img
          src="../assets/images/logos/demos_white_logo.png"
          alt="EU Politiek Dashboard Logo"
          class="logo"
        />
        <section class="title-section">
          <h1>EU Politiek Dashboard</h1>
          <p>Tweede Kamer verkiezingsdata voor jonge kiezers</p>
        </section>
      </div>
    </section>

    <nav class="navigation">
      <RouterLink to="/" active-class="active-link" title="Home">Home</RouterLink>
      <RouterLink to="/dashboard" active-class="active-link" title="Dashboard">Dashboard</RouterLink>
      <RouterLink to="/parties" active-class="active-link" title="Partijen">Partijen</RouterLink>
      <RouterLink to="/vergelijken" active-class="active-link" title="Vergelijken">Vergelijken</RouterLink>
      <RouterLink to="/forum" active-class="active-link" title="Forum">Forum</RouterLink>
      <RouterLink to="/political-about" active-class="active-link" title="About">About</RouterLink>
      <RouterLink v-if="authStore.isAdmin" to="/admin" active-class="active-link" title="Admin" class="admin-link">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
          <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z"/>
        </svg>
        Admin
      </RouterLink>

      <div class="auth-section">
        <template v-if="!authStore.isAuthenticated">
          <RouterLink to="/login" active-class="active-link" title="Login" class="auth-link">
            Inloggen
          </RouterLink>
          <RouterLink to="/register" active-class="active-link" title="Register" class="auth-link">
            Registreren
          </RouterLink>
        </template>
        <div v-else class="user-section">
          <button @click="toggleProfileModal" class="user-profile-button" title="Profiel">
            <div class="user-icon">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
              </svg>
            </div>
            <span class="username-text">{{ authStore.username }}</span>
          </button>
          <button @click="handleLogout" class="logout-button" title="Uitloggen">
            Uitloggen
          </button>
        </div>
      </div>
    </nav>

    <!-- Profile Modal -->
    <div v-if="showProfileModal" class="modal-overlay" @click.self="closeProfileModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2 class="modal-title">Profiel Bewerken</h2>
          <button @click="closeProfileModal" class="close-button" aria-label="Sluiten">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="profile-info">
            <div class="profile-avatar">
              <svg viewBox="0 0 24 24" width="64" height="64" fill="currentColor">
                <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
              </svg>
            </div>
            <div class="form-group">
              <label for="email">E-mailadres</label>
              <input
                id="email"
                type="email"
                placeholder="voorbeeld@email.com"
                readonly
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label for="username">Gebruikersnaam</label>
              <input
                id="username"
                type="text"
                v-model="editedUsername"
                class="form-input"
                @keyup.enter="saveProfile"
              />
            </div>
            <div v-if="saveMessage" class="save-message" :class="{ 'error': saveMessage.includes('leeg') }">
              {{ saveMessage }}
            </div>
            <button @click="saveProfile" class="save-button">
              Opslaan
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.header {
  background-color: #154273;
  color: white;
  padding: 1rem 2rem;
  font-family: 'Inter', sans-serif;
  margin: 0;
  width: 100%;
  box-sizing: border-box;
}

.top-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  width: 100%;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.logo-title {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.logo {
  height: 50px;
}

.title-section {
  text-align: left;
}

.title-section h1 {
  margin: 0;
  font-size: 1.75rem;
  font-weight: bold;
}

.title-section p {
  margin: 0;
  font-size: 1rem;
  opacity: 0.95;
}

.navigation {
  display: flex;
  gap: 2rem;
  padding-bottom: 0.5rem;
  border-bottom: none;
  overflow-x: auto;
}

.navigation a {
  text-decoration: none;
  color: white;
  font-weight: normal;
  font-size: 1.25rem;
  padding-bottom: 0.5rem;
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  white-space: nowrap;
}

.admin-link {
  background: rgba(255, 212, 59, 0.2);
  padding: 0.5rem 1rem;
  border-radius: 6px;
  transition: background 0.2s;
}

.admin-link:hover {
  background: rgba(255, 212, 59, 0.3);
}

.admin-link svg {
  fill: #ffd43b;
}

.navigation a.active-link {
  font-weight: bold;
  text-decoration: underline;
  text-underline-offset: 0.5rem;
  text-decoration-thickness: 2px;
}

.auth-section {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.auth-link {
  background-color: rgba(255, 255, 255, 0.1);
  padding: 0.5rem 1rem;
  border-radius: 6px;
  transition: background-color 0.2s;
  font-size: 1rem;
}

.auth-link:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.user-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-email {
  font-size: 0.95rem;
  opacity: 0.9;
  cursor: pointer;          /* maakt duidelijk dat het klikbaar is */
}

.logout-button {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.2s;
}

.logout-button:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.user-profile-button {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.2s;
  font-size: 0.95rem;
}

.user-profile-button:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.user-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
}

.username-text {
  font-weight: 500;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 90%;
  max-width: 500px;
  max-height: 85vh;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 24px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #fff;
}

.modal-title {
  font-size: 24px;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0;
}

.close-button {
  background: transparent;
  border: none;
  color: #6c757d;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.close-button:hover {
  background: #f5f5f5;
  color: #0a0a0a;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  max-height: calc(85vh - 80px);
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-avatar {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.profile-avatar svg {
  fill: #154273;
  background: #e6efff;
  border-radius: 50%;
  padding: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 600;
  font-size: 14px;
  color: #0a0a0a;
}

.form-input,
.form-textarea {
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  transition: all 0.2s;
  background: #fff;
  color: #0a0a0a;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #154273;
  box-shadow: 0 0 0 3px rgba(21, 66, 115, 0.1);
}

.form-input:read-only {
  background: #f8f9fa;
  color: #6c757d;
  cursor: not-allowed;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.save-button {
  background: #154273;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  margin-top: 8px;
}

.save-button:hover {
  background: #1a5491;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(21, 66, 115, 0.3);
}

.save-button:active {
  transform: translateY(0);
}

.save-message {
  padding: 12px;
  border-radius: 8px;
  background: #d4edda;
  color: #155724;
  font-size: 14px;
  text-align: center;
  font-weight: 500;
  animation: slideIn 0.3s ease;
}

.save-message.error {
  background: #f8d7da;
  color: #721c24;
}

</style>
