<template>
  <div class="card filters">
    <div class="card-title">Filters</div>
    <form class="filter-form" @submit.prevent="onApply">
      <div class="field">
        <label for="verkiezingSelect">Verkiezing</label>
        <select id="verkiezingSelect" :value="year" @change="onYearChange">
          <option value="">Alle verkiezingen</option>
          <option v-for="y in years" :key="String(y)" :value="String(y)">{{ y }}</option>
        </select>
      </div>

      <div class="field">
        <label for="regioSelect">Regio</label>
        <select id="regioSelect" :value="region" @change="onRegionChange">
          <option value="">Alle regio's</option>
          <option v-for="rg in regions" :key="rg" :value="rg">{{ rg }}</option>
        </select>
      </div>

      <div class="field">
        <label for="partijSelect">Partij</label>
        <select id="partijSelect" :value="party" @change="onPartyChange">
          <option value="">Alle partijen</option>
          <option v-for="pa in parties" :key="pa" :value="pa">{{ pa }}</option>
        </select>
      </div>

      <div class="actions">
        <button type="button" class="btn btn-reset" @click.prevent="onReset" aria-label="Reset filters">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
            <path d="M21 12a9 9 0 10-2.6 6.06" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M21 3v6h-6" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>Reset</span>
        </button>
        <button type="submit" class="btn btn-apply" aria-label="Toepassen filters">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
            <path d="M20 6L9 17l-5-5" stroke="white" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>Toepassen</span>
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import type { PropType } from 'vue'

const props = defineProps({
  years: { type: Array as PropType<number[]>, default: () => [] },
  parties: { type: Array as PropType<string[]>, default: () => [] },
  regions: { type: Array as PropType<string[]>, default: () => [] },
  year: { type: String, default: '' },
  region: { type: String, default: '' },
  party: { type: String, default: '' },
})

const emit = defineEmits(['update:year', 'update:region', 'update:party', 'reset', 'apply'])

function onApply() {
  emit('apply')
}

function onYearChange(e: Event) {
  const v = (e.target as HTMLSelectElement)?.value ?? ''
  emit('update:year', v)
}
function onRegionChange(e: Event) {
  const v = (e.target as HTMLSelectElement)?.value ?? ''
  emit('update:region', v)
}
function onPartyChange(e: Event) {
  const v = (e.target as HTMLSelectElement)?.value ?? ''
  emit('update:party', v)
}

function onReset() {
  emit('update:year', '')
  emit('update:region', '')
  emit('update:party', '')
  emit('reset')
}
</script>

<style scoped>
.card { padding: 12px; }
.field { margin-bottom: 8px; display:flex; flex-direction:column; gap:6px; }
.actions { display:flex; gap:8px; justify-content:space-between; align-items:center; margin-top: 6px; }
select { border: 1px solid #e5e7eb; border-radius: 8px; padding: 8px 10px; }

/* New button styles */
.btn { display:inline-flex; align-items:center; gap:8px; padding:8px 12px; border-radius:10px; font-weight:700; cursor:pointer; border: none; transition: transform .06s ease, box-shadow .12s ease, background .12s ease; }
.btn svg { display:block; }

.btn-reset {
  background: transparent;
  color: #374151;
  border: 1px solid #e6e9ee;
  box-shadow: none;
}
.btn-reset:hover { background: rgba(226,232,240,0.6); transform: translateY(-1px); }
.btn-reset:focus { outline: 3px solid rgba(37,99,235,0.14); }

.btn-apply {
  background: linear-gradient(90deg,#2563eb,#1d4ed8);
  color: #ffffff;
  box-shadow: 0 6px 18px rgba(37,99,235,0.18);
}
.btn-apply:hover { transform: translateY(-1px); filter:brightness(.98); }
.btn-apply:active { transform: translateY(0); }
.btn-apply:focus { outline: 3px solid rgba(37,99,235,0.22); }

/* Responsive tweak: buttons stack on narrow screens */
@media (max-width: 520px) {
  .actions { flex-direction: column-reverse; align-items: stretch; }
  .btn { width: 100%; justify-content:center; }
}
</style>
