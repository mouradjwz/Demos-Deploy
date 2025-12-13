<template>
  <div class="card post-form" v-if="visible">
    <div class="form-header">
      <div class="title">Nieuwe post</div>
    </div>
    <form class="new-post-form" @submit.prevent="onSubmit">
      <div class="field two-cols">
        <div class="col">
          <label for="np-title">Titel</label>
          <input id="np-title" v-model="title" type="text" placeholder="Waar wil je het over hebben?" required />
        </div>
        <div class="col">
          <label for="np-category">Categorie</label>
          <select id="np-category" v-model="category">
            <option value="Verkiezingen">Verkiezingen</option>
            <option value="Provincies">Provincies</option>
            <option value="Partijen">Partijen</option>
            <option value="__custom__">Eigen categorie…</option>
          </select>
        </div>
      </div>

      <div class="field" v-if="category === '__custom__'">
        <label for="np-custom-category">Eigen categorie</label>
        <input id="np-custom-category" v-model="customCategory" type="text" placeholder="Bijv. Gemeenteraad, Onderwijs, Klimaat" />
      </div>

      <div class="field" v-if="category === 'Verkiezingen'">
        <label for="np-election">Verkiezing</label>
        <select id="np-election" v-model="election">
          <option value="">Kies een verkiezing</option>
          <option v-for="y in years" :key="String(y)" :value="String(y)">{{ y }}</option>
        </select>
      </div>

      <div class="field" v-if="category === 'Partijen'">
        <label for="np-party">Partij</label>
        <select id="np-party" v-model="party">
          <option value="">Kies een partij</option>
          <option v-for="pa in parties" :key="pa" :value="pa">{{ pa }}</option>
        </select>
      </div>

      <div class="field" v-if="category === 'Provincies'">
        <label for="np-region">Regio</label>
        <select id="np-region" v-model="region">
          <option value="">Kies een regio</option>
          <option v-for="rg in regions" :key="rg" :value="rg">{{ rg }}</option>
        </select>
      </div>

      <div class="field">
        <label for="np-excerpt">Bericht</label>
        <textarea id="np-excerpt" v-model="excerpt" rows="4" placeholder="Schrijf je post..." required></textarea>
      </div>

      <div class="btn-row">
        <button type="button" class="btn secondary" @click="onCancel">Annuleren</button>
        <button type="submit" class="btn primary">Plaatsen</button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { PropType } from 'vue'
import { useAuthStore } from '../../stores/auth'

const props = defineProps({
  visible: { type: Boolean, default: false },
  years: { type: Array as PropType<number[]>, default: () => [] },
  parties: { type: Array as PropType<string[]>, default: () => [] },
  regions: { type: Array as PropType<string[]>, default: () => [] },
})
// include update:visible so v-model works; keep 'close' and 'submit' for compatibility
const emit = defineEmits(['close', 'submit', 'update:visible'])

const title = ref('')
const excerpt = ref('')
const category = ref('Verkiezingen')
const region = ref('')
const party = ref('')
const election = ref('')
const customCategory = ref('')

const auth = useAuthStore()

function onCancel() {
  emit('close')
  emit('update:visible', false)
}

function onSubmit() {
  const selectedCat = category.value === '__custom__' ? customCategory.value.trim() : String(category.value || '').trim()
  if (!title.value.trim() || !excerpt.value.trim() || !selectedCat) return

  // build payload
  const payload: Record<string, any> = {
    title: title.value.trim(),
    excerpt: excerpt.value.trim(),
    // prefer logged-in username or email if available
    author: auth.username || auth.email || 'Anoniem',
    category: selectedCat,
  }
  if (selectedCat === 'Partijen') payload.party = party.value || ''
  if (selectedCat === 'Verkiezingen') payload.electionYear = election.value || ''
  if (selectedCat === 'Provincies') payload.region = region.value || ''

  emit('submit', payload)
  // reset local form and close
  title.value = ''
  excerpt.value = ''
  category.value = 'Verkiezingen'
  region.value = ''
  party.value = ''
  election.value = ''
  customCategory.value = ''
  emit('close')
  emit('update:visible', false)
}
</script>

<style scoped>
/* Polished form styling for creating a post */
:root { --brand:#154273; --text:#1f2937; --muted:#6b7280; --bg:#ffffff; --border:#e5e7eb; --focus:#3b82f6; }

.post-form { padding: 18px; }

.form-header { display:flex; flex-direction:column; gap:4px; margin-bottom: 8px; }
.form-header .title { font-weight: 800; font-size: 1.05rem; color: var(--text); }
.form-header .hint { color: var(--muted); font-size: .9rem; }

.new-post-form { display: grid; gap: 16px; }

/* Layout for first row */
.field.two-cols { display: grid; grid-template-columns: 1fr 260px; gap: 14px; }
.field.two-cols .col { display: grid; gap: 6px; }

/* Generic field block */
.field { display: grid; gap: 6px; }

label {
  font-size: 0.9rem;
  color: var(--muted);
  font-weight: 600;
}

input[type="text"], select, textarea {
  width: 100%;
  border: 1px solid var(--border);
  background: var(--bg);
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 0.98rem;
  color: var(--text);
  outline: none;
  transition: border-color .15s ease, box-shadow .15s ease, background .2s ease;
}

input::placeholder, textarea::placeholder { color: #9ca3af; }

input:focus, select:focus, textarea:focus {
  border-color: var(--brand);
  box-shadow: 0 0 0 3px rgba(21,66,115,0.12);
}

textarea { resize: vertical; min-height: 100px; }

/* Buttons */
.btn-row { display: flex; gap: 10px; justify-content: flex-end; margin-top: 4px; padding-top: 10px; border-top: 1px dashed #e5e7eb; }

.btn { appearance: none; border: 1px solid transparent; padding: 10px 16px; border-radius: 10px; cursor: pointer; font-weight: 600; font-size: 0.95rem; transition: transform .08s ease, background .2s ease, box-shadow .2s ease, color .2s ease; }
.btn:active { transform: translateY(1px); }

.btn.primary { background: #2563eb; color: #fff; border-color: #2563eb; box-shadow: 0 2px 8px rgba(37,99,235,.25); }
.btn.primary:hover { background: #1d4ed8; }

.btn.secondary { background: #f3f4f6; color: var(--text); border-color: #e5e7eb; }
.btn.secondary:hover { background: #eaeef3; }

/* Select tidy arrow (keeps native but adjusts height) */
select { height: 40px; }

/* Responsive tweaks */
@media (max-width: 720px) {
  .field.two-cols { grid-template-columns: 1fr; }
}
</style>
