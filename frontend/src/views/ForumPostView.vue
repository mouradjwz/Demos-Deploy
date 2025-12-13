<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const route = useRoute();
const router = useRouter();
const post = ref<any>(null);
const loading = ref(true);
const error = ref('');

const comments = ref<any[]>([]);
const commentsLoading = ref(false);
const commentError = ref('');
const newComment = ref('');
const newUserId = ref('');
const posting = ref(false);

const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081';
const auth = useAuthStore();
const isAuth = computed(() => !!(auth.token || localStorage.getItem('auth_token')));
const isAdmin = computed(() => auth.isAdmin);

async function fetchPost() {
  loading.value = true;
  error.value = '';
  try {
    const res = await fetch(`${apiBase}/forum/posts/${route.params.id}`);
    if (!res.ok) throw new Error('Post niet gevonden');
    post.value = await res.json();
  } catch (e: any) {
    error.value = e.message || 'Fout bij ophalen post';
  } finally {
    loading.value = false;
  }
}

async function fetchComments() {
  commentsLoading.value = true;
  commentError.value = '';
  try {
    const res = await fetch(`${apiBase}/forum/posts/${route.params.id}/comments`);
    if (!res.ok) throw new Error('Reacties niet gevonden');
    comments.value = await res.json();
  } catch (e: any) {
    commentError.value = e.message || 'Fout bij ophalen reacties';
  } finally {
    commentsLoading.value = false;
  }
}

async function postComment() {
  // prefer authenticated username when available
  const userIdToSend = (auth.username || auth.email) ? (auth.username || auth.email) : newUserId.value.trim();
  if (!userIdToSend || !newComment.value.trim()) return;
  posting.value = true;
  try {
    const headers: Record<string,string> = { 'Content-Type': 'application/json' };
    const token = auth.token || localStorage.getItem('auth_token');
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const res = await fetch(`${apiBase}/forum/posts/${route.params.id}/comments`, {
      method: 'POST',
      headers,
      body: JSON.stringify({
        userId: userIdToSend,
        content: newComment.value.trim(),
      })
    });
    if (!res.ok) throw new Error('Reactie plaatsen mislukt');
    newComment.value = '';
    newUserId.value = '';
    await fetchComments();
  } catch (e: any) {
    commentError.value = e.message || 'Fout bij plaatsen reactie';
  } finally {
    posting.value = false;
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('nl-NL', { day: '2-digit', month: 'short', year: 'numeric' }) +
    ' om ' + d.toLocaleTimeString('nl-NL', { hour: '2-digit', minute: '2-digit' });
}

// --- Reactions for post ---
async function likePost() {
  try {
    const token = auth.token || localStorage.getItem('auth_token') || getCookie('auth_token');
    const headers: Record<string,string> = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = `Bearer ${token}`;
    const res = await fetch(`${apiBase}/forum/posts/${route.params.id}/like`, { method: 'POST', headers });
    if (!res.ok) {
      const text = await res.text().catch(() => '');
      console.error('likePost failed', res.status, text);
      throw new Error('Fout bij liken');
    }
    post.value = await res.json();
  } catch (e: any) {
    console.warn(e);
    alert(e.message || 'Kon post niet liken');
  }
}

async function dislikePost() {
  try {
    const token = auth.token || localStorage.getItem('auth_token') || getCookie('auth_token');
    const headers: Record<string,string> = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = `Bearer ${token}`;
    const res = await fetch(`${apiBase}/forum/posts/${route.params.id}/dislike`, { method: 'POST', headers });
    if (!res.ok) {
      const text = await res.text().catch(() => '');
      console.error('dislikePost failed', res.status, text);
      throw new Error('Fout bij disliken');
    }
    post.value = await res.json();
  } catch (e: any) {
    console.warn(e);
    alert(e.message || 'Kon post niet disliken');
  }
}

async function removePostReaction() {
  try {
    const token = auth.token || localStorage.getItem('auth_token') || getCookie('auth_token');
    const headers: Record<string,string> = {};
    if (token) headers['Authorization'] = `Bearer ${token}`;
    const res = await fetch(`${apiBase}/forum/posts/${route.params.id}/reaction`, { method: 'DELETE', headers });
    if (!res.ok) {
      const text = await res.text().catch(() => '');
      console.error('removePostReaction failed', res.status, text);
      throw new Error('Fout bij verwijderen reactie');
    }
    post.value = await res.json();
  } catch (e: any) {
    console.warn(e);
    alert(e.message || 'Kon reactie niet verwijderen');
  }
}

// --- Reactions for comments ---
async function likeComment(commentId: number) {
  // allow anonymous likes: backend will resolve anon id from IP if no token is provided
  try {
    const token = auth.token || localStorage.getItem('auth_token') || getCookie('auth_token');
    const headers: Record<string,string> = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = `Bearer ${token}`;
    const res = await fetch(`${apiBase}/forum/posts/${route.params.id}/comments/${commentId}/like`, { method: 'POST', headers });
    if (!res.ok) {
      const text = await res.text().catch(() => '');
      console.error('likeComment failed', res.status, text);
      throw new Error('Fout bij liken reactie');
    }
    const updated = await res.json();
    const idx = comments.value.findIndex(c => c.id === updated.id);
    if (idx >= 0) comments.value[idx] = updated;
  } catch (e: any) {
    console.warn('likeComment error', e);
    alert(e.message || 'Kon reactie niet liken');
  }
}

async function dislikeComment(commentId: number) {
  // allow anonymous dislikes
  try {
    const token = auth.token || localStorage.getItem('auth_token') || getCookie('auth_token');
    const headers: Record<string,string> = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = `Bearer ${token}`;
    const res = await fetch(`${apiBase}/forum/posts/${route.params.id}/comments/${commentId}/dislike`, { method: 'POST', headers });
    if (!res.ok) { const text = await res.text().catch(() => ''); console.error('dislikeComment failed', res.status, text); throw new Error('Fout bij disliken reactie') }
    const updated = await res.json();
    const idx = comments.value.findIndex(c => c.id === updated.id);
    if (idx >= 0) comments.value[idx] = updated;
  } catch (e: any) {
    console.warn('dislikeComment error', e);
    alert(e.message || 'Kon reactie niet disliken');
  }
}

async function removeCommentReaction(commentId: number) {
  // allow anonymous removal (backend will compute anon id from IP if no token)
  try {
    const token = auth.token || localStorage.getItem('auth_token') || getCookie('auth_token');
    const headers: Record<string,string> = {};
    if (token) headers['Authorization'] = `Bearer ${token}`;
    const res = await fetch(`${apiBase}/forum/posts/${route.params.id}/comments/${commentId}/reaction`, { method: 'DELETE', headers });
    if (!res.ok) { const text = await res.text().catch(() => ''); console.error('removeCommentReaction failed', res.status, text); throw new Error('Fout bij verwijderen reactie') }
    const updated = await res.json();
    const idx = comments.value.findIndex(c => c.id === updated.id);
    if (idx >= 0) comments.value[idx] = updated;
  } catch (e: any) {
    console.warn('removeCommentReaction error', e);
    alert(e.message || 'Kon reactie niet verwijderen');
  }
}

// helper to read cookies (fallback)
function getCookie(name: string) {
  try {
    const v = document.cookie.split('; ').find(row => row.startsWith(name + '='));
    return v ? decodeURIComponent(v.split('=')[1]) : null;
  } catch {
    return null;
  }
}

onMounted(() => {
  fetchPost();
  fetchComments();
});

// --- Admin delete actions ---
async function deletePost() {
  if (!confirm('Weet je zeker dat je deze post wilt verwijderen?')) return;
  try {
    const token = auth.token || localStorage.getItem('auth_token');
    const res = await fetch(`${apiBase}/forum/posts/${route.params.id}`, {
      method: 'DELETE',
      headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}) }
    });
    if (res.status === 204) {
      // navigate back to forum list
      await router.push('/forum');
      return;
    }
    const body = await res.json().catch(() => ({}));
    throw new Error((body && (body.message || body.error)) || `Verwijderen mislukt (${res.status})`);
  } catch (e: any) {
    console.warn('delete post error', e);
    alert(e.message || 'Kon post niet verwijderen');
  }
}

async function deleteComment(commentId: number) {
  if (!confirm('Weet je zeker dat je deze reactie wilt verwijderen?')) return;
  try {
    const token = auth.token || localStorage.getItem('auth_token');
    const res = await fetch(`${apiBase}/forum/posts/${route.params.id}/comments/${commentId}`, {
      method: 'DELETE',
      headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}) }
    });
    if (res.status === 204) {
      await fetchComments();
      return;
    }
    const body = await res.json().catch(() => ({}));
    throw new Error((body && (body.message || body.error)) || `Verwijderen mislukt (${res.status})`);
  } catch (e: any) {
    console.warn('delete comment error', e);
    alert(e.message || 'Kon reactie niet verwijderen');
  }
}
</script>

<template>
  <main class="forum-post-detail">
    <div class="back-link"><a href="/forum">← Terug naar forum</a></div>
    <div v-if="loading" class="loading">Laden...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="post">
      <div class="post-card">
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-meta-row">
          <span class="post-author-icon">{{ post.userId?.charAt(0)?.toUpperCase() || '?' }}</span>
          <div class="post-meta-info">
            <span class="post-author">{{ post.userId }}</span>
            <span class="post-date">{{ formatDate(post.createdAt) }}</span>
          </div>
        </div>
        <div class="post-description">{{ post.description }}</div>
        <div class="post-actions">
          <button @click="likePost" title="Like">👍 <strong>{{ post.likedBy?.length ?? post.likeCount ?? 0 }}</strong></button>
          <button @click="dislikePost" title="Dislike">👎 <strong>{{ post.dislikedBy?.length ?? post.dislikeCount ?? 0 }}</strong></button>
          <button @click="removePostReaction">❌ Verwijder reactie</button>
          <button v-if="isAdmin" @click="deletePost" title="Verwijder post (admin)">🗑️ Verwijder post</button>
        </div>
      </div>

      <section class="comments">
        <div class="comments-header-row">
          <h2>Reacties ({{ comments.length }})</h2>
        </div>
        <form class="comment-form" @submit.prevent="postComment">
          <!-- logged-in message removed per request -->
          <template v-if="!isAuth">
            <input v-model="newUserId" type="text" placeholder="Jouw naam" required />
          </template>
          <textarea v-model="newComment" placeholder="Jouw reactie..." required rows="2"></textarea>
          <button type="submit" :disabled="posting">Plaatsen</button>
        </form>
        <div v-if="commentsLoading" class="loading">Reacties laden...</div>
        <div v-else-if="commentError" class="error">{{ commentError }}</div>
        <div v-else>
          <div v-if="comments.length === 0" class="no-comments">Nog geen reacties.</div>
          <ul v-else class="comment-list">
            <li v-for="comment in comments" :key="comment.id" class="comment-card">
              <div class="comment-meta-row">
                <span class="comment-user-icon">{{ comment.userId?.charAt(0)?.toUpperCase() || '?' }}</span>
                <div class="comment-meta-info">
                  <span class="comment-user">{{ comment.userId }}</span>
                  <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
                </div>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
              <div class="comment-actions">
                <button @click.prevent="likeComment(comment.id)" :title="'Like reactie'">👍 <strong>{{ comment.likedBy?.length ?? comment.likeCount ?? 0 }}</strong></button>
                <button @click.prevent="dislikeComment(comment.id)" :title="'Dislike reactie'">👎 <strong>{{ comment.dislikedBy?.length ?? comment.dislikeCount ?? 0 }}</strong></button>
                <button @click.prevent="removeCommentReaction(comment.id)">❌</button>
                <button v-if="isAdmin" @click.prevent="deleteComment(comment.id)" title="Verwijder reactie (admin)">🗑️</button>
              </div>
            </li>
          </ul>
        </div>
      </section>
    </div>
  </main>
</template>

<style scoped>
.forum-post-detail {
  max-width: 700px;
  margin: 2rem auto;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.back-link {
  margin-bottom: 1.2rem;
  font-size: 1.05em;
}
.back-link a {
  color: #1976d2;
  text-decoration: none;
  font-weight: 500;
}
.post-card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  padding: 1.5rem 1.5rem 1rem 1.5rem;
  margin-bottom: 2rem;
}
.post-title {
  font-size: 1.4em;
  margin-bottom: 0.7rem;
}
.post-meta-row {
  display: flex;
  align-items: center;
  margin-bottom: 0.7rem;
}
.post-author-icon {
  width: 36px;
  height: 36px;
  background: #e3e6ee;
  color: #1976d2;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 1.2em;
  margin-right: 0.7em;
}
.post-meta-info {
  display: flex;
  flex-direction: column;
}
.post-author {
  font-weight: 500;
  color: #222;
}
.post-date {
  color: #888;
  font-size: 0.97em;
}
.post-description {
  font-size: 1.1em;
  margin-top: 0.5rem;
  margin-bottom: 0.5rem;
}
.post-actions {
  display: flex;
  gap: 1.5em;
  color: #888;
  font-size: 0.98em;
  margin-top: 1.2em;
}
.post-action {
  display: flex;
  align-items: center;
  gap: 0.3em;
}
.comments {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  padding: 1.2rem 1.5rem 1.5rem 1.5rem;
}
.comments-header-row {
  display: flex;
  align-items: center;
  margin-bottom: 1.2em;
}
.comment-form {
  display: flex;
  flex-direction: column;
  gap: 0.8em;
  margin-bottom: 1.5em;
}
.comment-form input,
.comment-form textarea {
  padding: 0.8em;
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 1em;
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
}
.comment-form button {
  width: 140px;
  padding: 0.6em 0.8em;
  border-radius: 8px;
  background: #2563eb;
  color: #fff;
  border: none;
  cursor: pointer;
}
.comment-list { list-style: none; padding: 0; margin: 0; display: grid; gap: 12px; }
.comment-card { padding: 12px; border-radius: 8px; background: #f9fafb; }
.comment-meta-row { display:flex; gap:12px; align-items:center; margin-bottom:8px; }
.comment-user-icon { width:34px; height:34px; border-radius:50%; display:flex; align-items:center; justify-content:center; background:#eef5ff; color:#1f3e63; }
.comment-actions { display:flex; gap:10px; align-items:center; }
</style>
