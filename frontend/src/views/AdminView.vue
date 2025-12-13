<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

interface User {
  id: number
  username: string
  email: string
  role: string
}

const users = ref<User[]>([])
const loading = ref(true)
const error = ref('')
const successMessage = ref('')
const selectedUser = ref<User | null>(null)
const showRoleModal = ref(false)
const newRole = ref('')
const showDeleteModal = ref(false)
const userToDelete = ref<User | null>(null)

onMounted(async () => {
  if (!authStore.isAdmin) {
    router.push('/')
    return
  }
  await fetchUsers()
})

async function fetchUsers() {
  loading.value = true
  error.value = ''

  try {
    const response = await fetch('http://localhost:8081/api/users', {
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
      },
    })

    if (!response.ok) {
      throw new Error('Failed to fetch users')
    }

    const data = await response.json()
    // Filter out password from response
    users.value = data.map((user: any) => ({
      id: user.id,
      username: user.username,
      email: user.email,
      role: user.role,
    }))
  } catch (err: any) {
    error.value = err.message || 'Error loading users'
  } finally {
    loading.value = false
  }
}

function openRoleModal(user: User) {
  selectedUser.value = user
  newRole.value = user.role
  showRoleModal.value = true
  successMessage.value = ''
}

function closeRoleModal() {
  showRoleModal.value = false
  selectedUser.value = null
  newRole.value = ''
}

async function updateUserRole() {
  if (!selectedUser.value) return

  try {
    const response = await fetch(`http://localhost:8081/api/users/${selectedUser.value.id}/role`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authStore.token}`,
      },
      body: JSON.stringify({
        role: newRole.value,
      }),
    })

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(errorText || 'Failed to update role')
    }

    successMessage.value = `Rol succesvol gewijzigd naar ${newRole.value}`
    await fetchUsers()
    closeRoleModal()
  } catch (err: any) {
    error.value = err.message || 'Error updating role'
  }
}

function getRoleBadgeClass(role: string) {
  return role === 'ADMIN' ? 'badge-admin' : 'badge-user'
}

function openDeleteModal(user: User) {
  userToDelete.value = user
  showDeleteModal.value = true
  successMessage.value = ''
}

function closeDeleteModal() {
  showDeleteModal.value = false
  userToDelete.value = null
}

async function confirmDeleteUser() {
  if (!userToDelete.value) return

  try {
    const response = await fetch(`http://localhost:8081/api/users/${userToDelete.value.id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
      },
    })

    if (!response.ok) {
      throw new Error('Failed to delete user')
    }

    successMessage.value = `Gebruiker ${userToDelete.value.username} succesvol verwijderd`
    await fetchUsers()
    closeDeleteModal()
  } catch (err: any) {
    error.value = err.message || 'Error deleting user'
    closeDeleteModal()
  }
}
</script>

<template>
  <div class="admin-page">
    <div class="admin-container">
      <div class="admin-header">
        <h1>Admin Dashboard</h1>
        <p class="subtitle">Beheer gebruikers en hun rollen</p>
      </div>

      <div v-if="error" class="error-message">
        {{ error }}
      </div>

      <div v-if="successMessage" class="success-message">
        {{ successMessage }}
      </div>

      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>Gebruikers laden...</p>
      </div>

      <div v-else class="users-table-container">
        <table class="users-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Gebruikersnaam</th>
              <th>Email</th>
              <th>Rol</th>
              <th>Acties</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.id }}</td>
              <td>
                <div class="user-info">
                  <div class="user-avatar">
                    <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                      <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                    </svg>
                  </div>
                  <span class="username">{{ user.username }}</span>
                </div>
              </td>
              <td>{{ user.email }}</td>
              <td>
                <span class="role-badge" :class="getRoleBadgeClass(user.role)">
                  {{ user.role }}
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <button @click="openRoleModal(user)" class="action-button edit-button">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                      <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                    </svg>
                    Wijzig Rol
                  </button>
                  <button @click="openDeleteModal(user)" class="action-button delete-button">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                      <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                    </svg>
                    Verwijder
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Role Modal -->
    <div v-if="showRoleModal" class="modal-overlay" @click.self="closeRoleModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2 class="modal-title">Rol Wijzigen</h2>
          <button @click="closeRoleModal" class="close-button" aria-label="Sluiten">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div v-if="selectedUser" class="user-details">
            <p><strong>Gebruiker:</strong> {{ selectedUser.username }}</p>
            <p><strong>Email:</strong> {{ selectedUser.email }}</p>
            <p><strong>Huidige rol:</strong> <span class="role-badge" :class="getRoleBadgeClass(selectedUser.role)">{{ selectedUser.role }}</span></p>
          </div>

          <div class="form-group">
            <label for="role-select">Nieuwe Rol</label>
            <select id="role-select" v-model="newRole" class="role-select">
              <option value="USER">USER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </div>

          <div class="modal-actions">
            <button @click="closeRoleModal" class="cancel-button">
              Annuleren
            </button>
            <button @click="updateUserRole" class="save-button">
              Opslaan
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="closeDeleteModal">
      <div class="modal-content delete-modal">
        <div class="modal-header">
          <h2 class="modal-title">Gebruiker Verwijderen</h2>
          <button @click="closeDeleteModal" class="close-button" aria-label="Sluiten">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="warning-icon">
            <svg viewBox="0 0 24 24" width="48" height="48" fill="currentColor">
              <path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/>
            </svg>
          </div>
          <p class="warning-text">
            Weet je zeker dat je gebruiker <strong>{{ userToDelete?.username }}</strong> wilt verwijderen?
          </p>
          <p class="warning-subtext">
            Deze actie kan niet ongedaan worden gemaakt.
          </p>

          <div class="modal-actions">
            <button @click="closeDeleteModal" class="cancel-button">
              Annuleren
            </button>
            <button @click="confirmDeleteUser" class="delete-confirm-button">
              Verwijderen
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: #f8f9fa;
  padding: 40px 20px;
}

.admin-container {
  max-width: 1200px;
  margin: 0 auto;
}

.admin-header {
  text-align: center;
  margin-bottom: 40px;
}

.admin-header h1 {
  font-size: 36px;
  font-weight: 700;
  color: #0a0a0a;
  margin-bottom: 8px;
}

.subtitle {
  color: #6c757d;
  font-size: 16px;
}

.error-message,
.success-message {
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
  font-weight: 500;
}

.error-message {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.success-message {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.loading {
  text-align: center;
  padding: 60px 20px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e5e7eb;
  border-top-color: #154273;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.users-table-container {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table thead {
  background: #154273;
  color: white;
}

.users-table th {
  padding: 16px;
  text-align: left;
  font-weight: 600;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.users-table tbody tr {
  border-bottom: 1px solid #e5e7eb;
  transition: background 0.2s;
}

.users-table tbody tr:hover {
  background: #f8f9fa;
}

.users-table td {
  padding: 16px;
  font-size: 14px;
  color: #0a0a0a;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: #e6efff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #154273;
}

.username {
  font-weight: 600;
}

.role-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.badge-admin {
  background: #ffd43b;
  color: #856404;
}

.badge-user {
  background: #e6efff;
  color: #154273;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.edit-button {
  background: #154273;
}

.edit-button:hover {
  background: #1a5491;
  transform: translateY(-1px);
}

.delete-button {
  background: #dc3545;
}

.delete-button:hover {
  background: #c82333;
  transform: translateY(-1px);
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
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 90%;
  max-width: 500px;
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

.save-button:active {
  transform: translateY(0);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 24px 16px;
  border-bottom: 1px solid #e5e7eb;
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
}

.user-details {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.user-details p {
  margin: 8px 0;
  font-size: 14px;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  font-weight: 600;
  font-size: 14px;
  color: #0a0a0a;
  margin-bottom: 8px;
}

.role-select {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  transition: all 0.2s;
  background: #fff;
  color: #0a0a0a;
  cursor: pointer;
}

.role-select:focus {
  outline: none;
  border-color: #154273;
  box-shadow: 0 0 0 3px rgba(21, 66, 115, 0.1);
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.cancel-button,
.save-button {
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.cancel-button {
  background: #f5f5f5;
  color: #0a0a0a;
}

.cancel-button:hover {
  background: #e5e7eb;
}

.save-button {
  background: #154273;
  color: white;
}

.save-button:hover {
  background: #1a5491;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(21, 66, 115, 0.3);
}

/* Delete Modal Styles */
.delete-modal .modal-body {
  text-align: center;
}

.warning-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.warning-icon svg {
  fill: #ffc107;
}

.warning-text {
  font-size: 16px;
  color: #0a0a0a;
  margin-bottom: 8px;
}

.warning-text strong {
  color: #dc3545;
}

.warning-subtext {
  font-size: 14px;
  color: #6c757d;
  margin-bottom: 24px;
}

.delete-confirm-button {
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  background: #dc3545;
  color: white;
}

.delete-confirm-button:hover {
  background: #c82333;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.3);
}

.delete-confirm-button:active {
  transform: translateY(0);
}

@media (max-width: 768px) {
  .users-table-container {
    overflow-x: auto;
  }

  .users-table {
    min-width: 600px;
  }

  .modal-content {
    width: 95%;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-button {
    width: 100%;
  }
}
</style>
