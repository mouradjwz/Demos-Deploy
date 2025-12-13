<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import ForumFilters from '../components/forum/ForumFilters.vue'
import NewPostForm from '../components/forum/NewPostForm.vue'
import PostList from '../components/forum/PostList.vue'
import { useAuthStore } from '../stores/auth'

interface Post {
  id?: number
  title: string
  excerpt: string
  author: string
  authorInitials?: string
  time?: string
  category: string
  region?: string
  party?: string
  election?: string
  tags?: string[]
}

const posts = ref<Post[]>([])

const apiBase: string = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'

// Forum statistics fetched from backend
const stats = ref<{ activePosts: number; reactions: number; likes: number; users: number }>({ activePosts: 0, reactions: 0, likes: 0, users: 0 })

async function fetchStats() {
  try {
    const res = await fetch(`${apiBase}/forum/stats`)
    if (res.ok) {
      const data = await res.json()
      stats.value = {
        activePosts: Number(data.activePosts) || 0,
        reactions: Number(data.reactions) || 0,
        likes: Number(data.likes) || 0,
        users: Number(data.users) || 0
      }
    }
  } catch (e) {
    // ignore errors; keep defaults
  }
}

function formatCompact(n: number): string {
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(n % 1_000_000 === 0 ? 0 : 1) + 'm'
  if (n >= 1_000) return (n / 1_000).toFixed(n % 1_000 === 0 ? 0 : 1) + 'k'
  return String(n)
}

async function fetchPosts(opts?: { electionYear?: string | number | null; party?: string | null; region?: string | null }) {
  try {
    const params = new URLSearchParams()
    if (opts) {
      if (opts.electionYear != null && String(opts.electionYear).trim() !== '') params.set('electionYear', String(opts.electionYear))
      if (opts.party != null && String(opts.party).trim() !== '') params.set('party', String(opts.party))
      if (opts.region != null && String(opts.region).trim() !== '') params.set('region', String(opts.region))
    }
    const url = params.toString() ? `${apiBase}/forum/posts?${params.toString()}` : `${apiBase}/forum/posts`
    const res = await fetch(url)
    if (res.ok) {
      const data = await res.json()
      posts.value = Array.isArray(data) ? data : []
    } else {
      posts.value = []
    }
  } catch (e) {
    posts.value = []
  }
}

onMounted(() => {
  fetchPosts()
  fetchStats()
})

// New Post UI state
const showNewPost = ref(false)
const auth = useAuthStore()

// Handler that receives payload from NewPostForm and performs the POST (keeps original behavior)
async function handleNewPost(payload: Record<string, any>) {
  const title = (payload.title || '').trim()
  const excerpt = (payload.excerpt || '').trim()
  // prefer author from payload (form) but if missing and user is logged in, use auth
  const author = payload.author || auth.username || auth.email || 'Anoniem'
  if (!title || !excerpt) return

  const selectedCat = payload.category ?? ''
  const region = payload.region ?? ''
  const party = payload.party ?? ''
  const election = payload.election ?? payload.electionYear ?? ''

  // Validation similar to original logic
  if (!selectedCat) return
  if (selectedCat === 'Partijen' && !party) return
  if (selectedCat === 'Verkiezingen' && !election) return

  // Check backend availability before attempting POST
  const backendUp = await isBackendUp(apiBase)
  if (!backendUp) {
    alert(`Backend is niet bereikbaar (${apiBase}). Start de backend of stel VITE_API_BASE correct in.`)
    return
  }

  try {
    // attach Authorization header if we have a token (backend requires auth for POST)
    const authStore = useAuthStore()
    const token = authStore.token || localStorage.getItem('auth_token')
    const headers: Record<string,string> = { 'Content-Type': 'application/json' }
    if (token) headers['Authorization'] = `Bearer ${token}`

    // Map election -> electionYear (backend expects electionYear) and include party
    const body: Record<string, any> = {
      title,
      excerpt,
      author,
      category: selectedCat,
      region: selectedCat === 'Provincies' ? region : undefined,
      party: party || undefined,
      electionYear: election ? (isNaN(Number(election)) ? election : Number(election)) : undefined,
    }

    const res = await fetch(`${apiBase}/forum/posts`, {
      method: 'POST',
      headers,
      body: JSON.stringify(body)
    })

    if (!res.ok) {
      const msg = await safeReadMessage(res)
      alert(msg || 'Plaatsen van post is uitgeschakeld op de backend.')
      return
    }

    // After successful post, refresh posts with current filters
    await fetchPosts({ electionYear: selectedYear.value || null, party: selectedParty.value || null, region: selectedRegion.value || null })
  } catch (e) {
    console.warn('Fout bij posten naar backend', e)
    alert('Kon geen verbinding maken met de backend. Plaatsen is uitgeschakeld of backend is niet gestart.')
    return
  } finally {
    showNewPost.value = false
  }
}

async function safeReadMessage(res: Response): Promise<string | null> {
  try {
    const data = await res.json()
    return (data && (data.message || data.error || data.detail)) ?? null
  } catch {
    try {
      return await res.text()
    } catch {
      return null
    }
  }
}

async function isBackendUp(apiBase: string): Promise<boolean> {
  try {
    const res = await fetch(`${apiBase}/forum/health`, { method: "GET" })
    return res.ok
  } catch {
    return false
  }
}

// Filters (Partijen, Regio's, Verkiezingen)
const years = [2023, 2021, 2017, 2012, 2010, 2006]
const parties = ['VVD', 'D66', 'PVV', 'PvdA', 'GroenLinks', 'CDA', 'SP', 'ChristenUnie', 'PvdD', 'FvD']
const regions = ['Noord', 'Oost', 'Zuid', 'West', 'Midden', 'Randstad']

const selectedYear = ref<string>('')
const selectedRegion = ref<string>('')
const selectedParty = ref<string>('')

function resetFilters() {
  selectedYear.value = ''
  selectedRegion.value = ''
  selectedParty.value = ''
  // fetch all posts from backend
  fetchPosts()
}

function applyFilters() {
  // Request filtered posts from backend; backend filter priority handles electionYear, party and region
  fetchPosts({ electionYear: selectedYear.value || null, party: selectedParty.value || null, region: selectedRegion.value || null })
}

const filteredPosts = computed(() => {
  // Keep a local layer in case additional client-side filtering is desired, but primary filtering is server-side now
  return posts.value
})

</script>

<template>
  <main class="forum">
    <div class="container">
      <div class="layout">
        <!-- Sidebar (Filters) -->
        <aside class="sidebar">
          <ForumFilters
            v-model:year="selectedYear"
            v-model:region="selectedRegion"
            v-model:party="selectedParty"
            :years="years"
            :parties="parties"
            :regions="regions"
            @apply="applyFilters"
            @reset="resetFilters"
          />
        </aside>

        <!-- Main content -->
        <section class="content">
          <!-- Header with forum info and new post button -->
          <div class="header-actions-inline">
            <button class="new-post-btn" type="button" aria-label="Nieuwe post maken" @click="showNewPost = !showNewPost">+ Post</button>
          </div>

          <!-- New Post Form component (keeps same visual markup) -->
          <NewPostForm v-model:visible="showNewPost" :years="years" :parties="parties" :regions="regions" @submit="handleNewPost" />

          <!-- Stats row (kept as-is) -->
          <div class="stats">
            <div class="stat card" aria-label="Actieve posts statistiek">
              <div class="stat-body">
                <div class="stat-icon posts" aria-hidden="true">📝</div>
                <div class="stat-content">
                  <div class="stat-count" aria-label="Aantal actieve posts">{{ formatCompact(stats.activePosts) }}</div>
                  <div class="stat-label">Actieve posts</div>
                </div>
              </div>
            </div>
            <div class="stat card" aria-label="Gebruikers statistiek">
              <div class="stat-body">
                <div class="stat-icon users" aria-hidden="true">👥</div>
                <div class="stat-content">
                  <div class="stat-count" aria-label="Aantal gebruikers">{{ formatCompact(stats.users) }}</div>
                  <div class="stat-label">Gebruikers</div>
                </div>
              </div>
            </div>
            <div class="stat card" aria-label="Reacties statistiek">
              <div class="stat-body">
                <div class="stat-icon comments" aria-hidden="true">💬</div>
                <div class="stat-content">
                  <div class="stat-count" aria-label="Aantal reacties">{{ formatCompact(stats.reactions) }}</div>
                  <div class="stat-label">Reacties</div>
                </div>
              </div>
            </div>
            <div class="stat card" aria-label="Likes statistiek">
              <div class="stat-body">
                <div class="stat-icon likes" aria-hidden="true">❤️</div>
                <div class="stat-content">
                  <div class="stat-count" aria-label="Aantal likes">{{ formatCompact(stats.likes) }}</div>
                  <div class="stat-label">Likes</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Info banner removed per request -->

          <!-- Posts -->
          <div class="empty-state card" v-if="posts.length === 0">
            <div class="empty-title">Nog geen posts</div>
            <div class="empty-text">Wees de eerste! Klik op “+ Post” om een nieuw onderwerp te starten.</div>
          </div>

          <PostList :posts="filteredPosts" v-else @deleted="() => fetchPosts({ electionYear: selectedYear || null, party: selectedParty || null, region: selectedRegion || null })" />

        </section>
      </div>
    </div>
  </main>
</template>

<style scoped>
.forum { padding: 16px; }
.container { max-width: 1200px; margin: 0 auto; }

/* Two-column layout */
.layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 20px;
}

/* Cards */
.card {
  background: #fff;
  border: 1px solid rgba(0,0,0,.08);
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0,0,0,.04);
  padding: 14px;
}
.img-wrap { display:flex; align-items:center; justify-content:center; background:#fafafa; border-radius:8px; overflow:hidden; }
.img-wrap img { max-width:100%; max-height:100%; object-fit:contain; }
.img-wrap.tall { height: 420px; }

/* Sidebar */
.card-title { font-weight: 700; margin-bottom: 10px; }

/* Header */
.header { display:flex; align-items:center; justify-content:space-between; gap: 16px; }
.header .title { font-size: 1.1rem; font-weight: 700; }
.header .subtitle {  color: var(--grey);; font-size:.9rem; }
.new-post-btn {
  background: #2563eb; /* blue */
  color: #ffffff;
  border: none;
  border-radius: 10px;
  padding: 10px 16px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(37,99,235,.3);
  transition: background .15s ease, transform .05s ease;
}
.new-post-btn:hover { background: #1d4ed8; }
.new-post-btn:active { transform: translateY(1px); }

/* Simple inline actions row when header is removed */
.header-actions-inline { display: flex; justify-content: flex-end; margin-bottom: 8px; }

/* Stats */
.stats { display:grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-top: 16px; align-items: stretch; }
.stat { padding: 12px; display: flex; align-items: center; min-height: 86px; }
.stat-body { width: 100%; display: grid; grid-template-columns: 48px 1fr; gap: 12px; align-items: center; }
.stat-icon { width: 48px; height: 48px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 22px; font-weight: 700; line-height: 1; }
.stat-icon.posts { background: #eef5ff; color: #1f3e63; transform: translateY(-1px); }
.stat-icon.users { background: #eefaf3; color: #166534; }
.stat-icon.comments { background: #f5f3ff; color: #5b21b6; }
.stat-icon.likes { background: #fff1f2; color: #be123c; }
.stat-content { display: flex; flex-direction: column; justify-content: center; line-height: 1.15; }
.stat-count { font-size: 1.15rem; font-weight: 800; font-variant-numeric: tabular-nums; }
.stat-label { font-size: .9rem;   color: var(--grey);; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* Info banner */
.info { display:flex; align-items:center; gap:10px; background:#eef5ff; border-color:#d6e6ff; color:#214079; margin-top:16px; }
.info-title { font-weight:700; }
.info-text { opacity:.9; }

/* Posts */
.posts { display:flex; flex-direction:column; gap:16px; margin-top:16px; }
.post-title { margin: 0 0 8px; font-size:1.05rem; }
.post-excerpt { margin: 0 10px 10px 0;   color: var(--grey); }
.post-meta { display:flex; align-items:center; gap:10px; margin-bottom:8px; }
.avatar { width:34px; height:34px; border-radius:50%; background:#eef5ff; color:#1f3e63; display:flex; align-items:center; justify-content:center; font-weight:800; font-size:.9rem; }
.meta-text { line-height:1.1; }
.meta-text .author { font-weight:700; }
.meta-text .time { font-size:.85rem; color:#6b7280; }
.tags { display:flex; gap:8px; flex-wrap:wrap; margin: 6px 0 8px; }
.tag { background:#f3f4f6; color:#374151; padding:4px 8px; border-radius:999px; font-size:.8rem; font-weight:600; }
.post-actions { display:flex; align-items:center; gap:8px; border-top:1px solid #eee; padding-top:8px; color:#4b5563; font-weight:600; }
.post-actions .action { display:inline-flex; align-items:center; gap:6px; }
.post-actions .dot { opacity:.6; }

/* Filters form */
.filters .filter-form { display: flex; flex-direction: column; gap: 12px; }
.filters .field { display: flex; flex-direction: column; gap: 6px; }
.filters label { font-weight: 600; font-size: .9rem; color: #374151; }
.filters input[type="search"], .filters select {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: .95rem;
  background: #fff;
}
.filters .group { border-top: 1px solid #f1f5f9; padding-top: 10px; display: grid; grid-template-columns: 1fr; gap: 6px; }
.filters .group-title { font-weight: 700; color: #1f2937; margin-bottom: 2px; }
.filters .check, .filters .radio { display: flex; align-items: center; gap: 8px; font-weight: 500;   color: var(--grey);}
.filters input[type="checkbox"], .filters input[type="radio"] { accent-color: #2563eb; }
.filters .actions { display: flex; gap: 8px; margin-top: 4px; }
.filters .btn { border: none; border-radius: 10px; padding: 10px 14px; font-weight: 700; cursor: pointer; }
.filters .btn.primary { background: #2563eb; color: #fff; box-shadow: 0 2px 6px rgba(37,99,235,.2); }
.filters .btn.primary:hover { background: #1d4ed8; }
.filters .btn.secondary { background: #eef2f7; color: #1f2937; }


/* Responsive */
@media (max-width: 900px) {
  .layout { grid-template-columns: 1fr; }
  .sidebar { order: 2; }
  .content { order: 1; }
  .stats { grid-template-columns: repeat(2, 1fr); }
}
</style>
