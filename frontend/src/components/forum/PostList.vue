<template>
  <div class="posts">
    <article class="post card" v-for="post in posts" :key="post.id">
      <div class="post-header">
        <h3 class="post-title"><RouterLink :to="`/forum/${post.id}`">{{ post.title || '(geen titel)' }}</RouterLink></h3>
        <div class="post-meta-row">
          <span class="post-date">{{ formatDate(post.created_at || post.createdAt || post.time) }}</span>
          <span class="dot">•</span>
          <span class="post-user">{{ post.user_id || post.userId || post.author || 'Anoniem' }}</span>
        </div>
      </div>

      <p class="post-excerpt">{{ post.description || post.excerpt || '' }}</p>

      <div class="post-footer">
        <div class="tags" v-if="(post.tags && post.tags.length) || post.category || post.party">
          <span v-if="post.category" class="tag">{{ post.category }}</span>
          <span v-if="post.party" class="tag">{{ post.party }}</span>
          <span v-for="tag in post.tags || []" :key="tag" class="tag">#{{ tag }}</span>
        </div>

        <!-- Reaction actions -->
        <div class="post-actions">
          <button
            class="action like"
            :class="{ active: isUserLiked(post) }"
            @click="toggleLike(post)"
            :title="isAuth ? (isUserLiked(post) ? 'Verwijder like' : 'Like') : 'Log in om te liken'">
            👍
            <span class="count">{{ likeCount(post) }}</span>
          </button>
          <button
            class="action dislike"
            :class="{ active: isUserDisliked(post) }"
            @click="toggleDislike(post)"
            :title="isAuth ? (isUserDisliked(post) ? 'Verwijder dislike' : 'Dislike') : 'Log in om te disliken'">
            👎
            <span class="count">{{ dislikeCount(post) }}</span>
          </button>

          <!-- Admin delete action -->
          <button
            v-if="isAdmin"
            class="action delete"
            @click="onDeletePost(post)"
            title="Verwijder post (admin)">
            🗑️
          </button>
        </div>
      </div>
    </article>
  </div>
</template>

<script setup lang="ts">
import type { PropType } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { computed } from 'vue'

const emit = defineEmits<{ (e: 'deleted', id: number): void }>()

type Post = {
  id?: number
  title?: string
  description?: string
  excerpt?: string
  created_at?: string
  createdAt?: string
  time?: string
  user_id?: string
  userId?: string
  author?: string
  authorInitials?: string
  category?: string
  region?: string
  party?: string
  election?: string
  tags?: string[]
  likedBy?: string[]
  dislikedBy?: string[]
  likeCount?: number
  dislikeCount?: number
}

const props = defineProps({
  posts: { type: Array as PropType<Post[]>, default: () => [] }
})

const auth = useAuthStore()
const API_BASE: string = (import.meta.env.VITE_API_BASE_URL as string) || 'http://localhost:8081'

function formatDate(s?: string | null) {
  if (!s) return ''
  try {
    const d = new Date(s)
    if (isNaN(d.getTime())) return s
    return d.toLocaleString()
  } catch {
    return String(s)
  }
}

function likeCount(post: Post) {
  return (post.likedBy && post.likedBy.length) || (post.likeCount ?? 0) || 0
}
function dislikeCount(post: Post) {
  return (post.dislikedBy && post.dislikedBy.length) || (post.dislikeCount ?? 0) || 0
}

const isAuth = computed(() => !!(auth.token || localStorage.getItem('auth_token')))
const isAdmin = computed(() => auth.isAdmin)

function currentUserId() {
  return auth.username || auth.email || localStorage.getItem('auth_username') || localStorage.getItem('auth_email') || null
}

function isUserLiked(post: Post) {
  const u = currentUserId()
  return !!(u && post.likedBy && post.likedBy.includes(u))
}
function isUserDisliked(post: Post) {
  const u = currentUserId()
  return !!(u && post.dislikedBy && post.dislikedBy.includes(u))
}

async function toggleLike(post: Post) {
  if (!isAuth.value) { alert('Log in om te liken'); return }
  const userId = currentUserId()
  const alreadyLiked = isUserLiked(post)
  try {
    const token = auth.token || localStorage.getItem('auth_token')
    if (alreadyLiked) {
      // remove reaction
      const res = await fetch(`${API_BASE}/forum/posts/${post.id}/reaction`, {
        method: 'DELETE',
        headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}) }
      })
      if (!res.ok) throw new Error('Fout bij verwijderen reactie')
      const updated = await res.json()
      Object.assign(post, updated)
      return
    }

    // otherwise add like
    const res = await fetch(`${API_BASE}/forum/posts/${post.id}/like`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      }
    })
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: 'Fout bij liken' }))
      throw new Error(err.message || 'Fout bij liken')
    }
    const updated = await res.json()
    Object.assign(post, updated)
  } catch (e: any) {
    console.warn(e)
    alert(e.message || 'Kon post niet liken')
  }
}

async function toggleDislike(post: Post) {
  if (!isAuth.value) { alert('Log in om te disliken'); return }
  const userId = currentUserId()
  const alreadyDisliked = isUserDisliked(post)
  try {
    const token = auth.token || localStorage.getItem('auth_token')
    if (alreadyDisliked) {
      const res = await fetch(`${API_BASE}/forum/posts/${post.id}/reaction`, {
        method: 'DELETE',
        headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}) }
      })
      if (!res.ok) throw new Error('Fout bij verwijderen reactie')
      const updated = await res.json()
      Object.assign(post, updated)
      return
    }

    const res = await fetch(`${API_BASE}/forum/posts/${post.id}/dislike`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      }
    })
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: 'Fout bij disliken' }))
      throw new Error(err.message || 'Fout bij disliken')
    }
    const updated = await res.json()
    Object.assign(post, updated)
  } catch (e: any) {
    console.warn(e)
    alert(e.message || 'Kon post niet disliken')
  }
}

// Admin delete
async function onDeletePost(post: Post) {
  if (!confirm('Weet je zeker dat je deze post wilt verwijderen?')) return
  try {
    const token = auth.token || localStorage.getItem('auth_token')
    const res = await fetch(`${API_BASE}/forum/posts/${post.id}`, {
      method: 'DELETE',
      headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}) }
    })
    if (res.status === 204) {
      emit('deleted', post.id as number)
      return
    }
    const body = await res.json().catch(() => ({}))
    const msg = (body && (body.message || body.error)) || `Verwijderen mislukt (${res.status})`
    throw new Error(msg)
  } catch (e: any) {
    console.warn('delete post error', e)
    alert(e.message || 'Kon post niet verwijderen')
  }
}
</script>

<style scoped>
.posts { display:flex; flex-direction:column; gap:16px; margin-top:16px; }
.post { padding: 14px; border-radius: 12px; border: 1px solid rgba(0,0,0,0.06); background: #fff; }
.post-header { display:flex; flex-direction:column; gap:6px; margin-bottom:8px; }
.post-title { margin:0; font-size:1.05rem; }
.post-title a { color: inherit; text-decoration: none; }
.post-title a:hover { text-decoration: underline; }
.post-meta-row { color: #6b7280; font-size: 0.85rem; display:flex; align-items:center; gap:8px; }
.post-date { opacity: 0.9 }
.post-user { font-weight:600; }
.post-excerpt { margin: 8px 0 10px 0; color: var(--grey); line-height:1.4 }
.tags { display:flex; gap:8px; flex-wrap:wrap; }
.tag { background:#f3f4f6; color:#374151; padding:4px 8px; border-radius:999px; font-size:.8rem; font-weight:600; }
.dot { opacity: .6 }
.post-actions { display:flex; gap:8px; margin-top:10px; }
.post-actions .action { border: none; background: transparent; cursor: pointer; display:flex; align-items:center; gap:6px; padding:6px 8px; border-radius:8px; }
.post-actions .action.like { color: #ef4444; }
.post-actions .action.dislike { color: #64748b; }
.post-actions .action.active { background: rgba(37,99,235,0.08); box-shadow: inset 0 0 0 1px rgba(37,99,235,0.06); }
.post-actions .action:hover { background: rgba(0,0,0,0.03); }
.post-actions .count { font-weight:700; margin-left:6px; }
.post-actions .action.delete { color: #c62828 }
</style>
