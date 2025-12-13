<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'

/**
 * DTO zoals de backend hem geeft
 */
type BackendPartyDto = {
  partyId: string
  partyName: string
  seats: number
  percentage: number
  totalVotes: number
  color?: string
}

/**
 * DTO zoals de frontend ermee werkt
 * id = genormaliseerde key waarmee we partijen tussen jaren kunnen vergelijken
 */
type PartyDto = {
  id: string
  partyName: string
  seats: number
  percentage: number
  totalVotes: number
  color?: string
}

const API_BASE_URL = 'http://localhost:8081'
const availableYears = [2017, 2021, 2023]

// Geselecteerde jaren links en rechts
const leftYear = ref<number>(2017)
const rightYear = ref<number>(2023)

// Data per jaar
const leftData = ref<PartyDto[]>([])
const rightData = ref<PartyDto[]>([])

const loading = ref(true)
const error = ref<string | null>(null)

// Helpers
function toElectionId(year: number) {
  return `TK${year}`
}

/**
 * Normaliseert partijnamen zodat dezelfde partij in verschillende jaren
 * dezelfde 'key' krijgt (bijv. GROENLINKS vs GROENLINKS / PvdA).
 */
function normalizePartyKey(partyName: string): string {
  const upper = (partyName ?? '').toUpperCase().replace(/\s+/g, ' ').trim()

  if (upper.includes('GROENLINKS')) return 'GROENLINKS'
  if (upper.includes('PARTIJ VAN DE ARBEID') || upper.includes('PVDA')) return 'PVDA'
  if (upper.startsWith('PVV')) return 'PVV'
  if (upper.startsWith('VVD')) return 'VVD'
  if (upper.includes('D66') || upper.includes('DEMOCRATEN 66')) return 'D66'

  // fallback: volledige naam gebruiken
  return upper
}

/**
 * Fallback kleur wanneer backend geen kleur meestuurt.
 */
function getRandomColor() {
  const hue = Math.floor(Math.random() * 360)
  const saturation = 60 + Math.random() * 20
  const lightness = 50 + Math.random() * 10
  return `hsl(${hue}, ${saturation}%, ${lightness}%)`
}

/**
 * Haalt verkiezingsdata op en zet het om naar frontend formaat.
 */
async function fetchElectionData(year: number): Promise<PartyDto[]> {
  const electionId = toElectionId(year)
  const res = await fetch(`${API_BASE_URL}/api/dashboard/dto/${electionId}`)

  if (!res.ok) throw new Error(`HTTP ${res.status}`)

  const json: BackendPartyDto[] = await res.json()

  // Data mappen + sorteren op meeste zetels
  return json
    .map((p) => ({
      id: normalizePartyKey(p.partyName), // merge key
      partyName: p.partyName,
      seats: p.seats,
      percentage: p.percentage,
      totalVotes: p.totalVotes ?? 0,
      color: p.color ?? getRandomColor(),
    }))
    .sort((a, b) => b.seats - a.seats)
}

/**
 * Laadt data voor beide geselecteerde jaren tegelijk.
 */
async function loadAllData() {
  loading.value = true
  error.value = null

  try {
    const [l, r] = await Promise.all([
      fetchElectionData(leftYear.value),
      fetchElectionData(rightYear.value),
    ])

    leftData.value = l
    rightData.value = r
  } catch (e) {
    console.error(e)
    error.value = 'Er ging iets mis bij het ophalen van de data'
  } finally {
    loading.value = false;
  }
}

/**
 * Totale stemmen per jaar (som van totalVotes)
 */
const leftTotalVotes = computed(() =>
  leftData.value.reduce((sum, p) => sum + (p.totalVotes || 0), 0),
)

const rightTotalVotes = computed(() =>
  rightData.value.reduce((sum, p) => sum + (p.totalVotes || 0), 0),
)

/**
 * Helper om grote aantallen stemmen compact weer te geven, bijv. 10.3M
 */
function formatMillions(votes: number): string {
  if (!votes) return '—'
  const millions = votes / 1_000_000
  return `${millions.toFixed(1)}M`
}

/**
 * Combines both years into one compare list:
 * - zelfde partijen worden samengevoegd
 * - partijen met 0 zetels in beide jaren worden weggefilterd
 */
const combinedParties = computed(() => {
  const result: Record<string, any> = {}

  // Data linker verkiezing
  leftData.value.forEach((p) => {
    result[p.id] = {
      id: p.id,
      partyName: p.partyName,
      leftSeats: p.seats,
      rightSeats: 0,
      leftPercentage: p.percentage,
      rightPercentage: 0,
      color: p.color,
    }
  })

  // Data rechter verkiezing
  rightData.value.forEach((p) => {
    if (!result[p.id]) {
      // partij bestond niet links
      result[p.id] = {
        id: p.id,
        partyName: p.partyName,
        leftSeats: 0,
        rightSeats: p.seats,
        leftPercentage: 0,
        rightPercentage: p.percentage,
        color: p.color,
      }
    } else {
      // partij bestond al → waardes rechts aanvullen
      result[p.id].rightSeats = p.seats
      result[p.id].rightPercentage = p.percentage

      // naam updaten naar nieuwste jaar (bijv. GROENLINKS / PvdA)
      result[p.id].partyName = p.partyName
    }
  })

  return Object.values(result)
    .filter(p => (p.leftSeats > 0) || (p.rightSeats > 0)) // partijen zonder zetels -> verbergen
    .sort((a, b) => b.rightSeats - a.rightSeats)           // sorteer op nieuwste jaar
})

// Data laden bij opstart + wanneer een jaar wijzigt
onMounted(loadAllData)
watch([leftYear, rightYear], loadAllData)
</script>

<template>
  <section class="compare-section">
    <div class="header">
      <h2 class="title">Vergelijk verkiezingen</h2>

      <div class="dropdown-group">
        <div class="dropdown-wrapper">
          <span class="dropdown-label">Verkiezing 1</span>
          <select v-model.number="leftYear" class="dropdown">
            <option v-for="y in availableYears" :key="'l' + y" :value="y">
              {{ y }}
            </option>
          </select>
        </div>

        <div class="dropdown-wrapper">
          <span class="dropdown-label">Verkiezing 2</span>
          <select v-model.number="rightYear" class="dropdown">
            <option v-for="y in availableYears" :key="'r' + y" :value="y">
              {{ y }}
            </option>
          </select>
        </div>
      </div>
    </div>

    <!-- Nieuw: kaartje met totaal stemmen -->
    <div class="summary-card">
      <div class="summary-header">
        <span class="summary-title">Totaal stemmen</span>
      </div>

      <!-- Let op: nieuwste jaar eerst, zoals in je voorbeeld (2023 boven 2021) -->
      <div class="summary-row">
        <span class="summary-label">{{ rightYear }}:</span>
        <span class="summary-value">
          {{ formatMillions(rightTotalVotes) }}
        </span>
      </div>
      <div class="summary-row">
        <span class="summary-label">{{ leftYear }}:</span>
        <span class="summary-value">
          {{ formatMillions(leftTotalVotes) }}
        </span>
      </div>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <div v-else-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p class="loading-text">Data wordt geladen...</p>
    </div>

    <div v-else class="party-list">
      <h3 class="subtitle">
        Gedetailleerde veranderingen ({{ leftYear }} → {{ rightYear }})
      </h3>

      <div v-for="party in combinedParties" :key="party.id" class="party-row">
        <div class="left">
          <span class="dot" :style="{ backgroundColor: party.color }"></span>
          <span class="name">{{ party.partyName }}</span>
        </div>

        <div class="right">
          <div class="metric">
            <span class="label">Zetels</span>
            <div class="value">
              {{ party.leftSeats || '—' }} → {{ party.rightSeats || '—' }}
            </div>
          </div>

          <div class="metric">
            <span class="label">Percentage</span>
            <div class="value">
              {{
                party.leftPercentage
                  ? party.leftPercentage.toFixed(1) + '%'
                  : '—'
              }}
              →
              {{
                party.rightPercentage
                  ? party.rightPercentage.toFixed(1) + '%'
                  : '—'
              }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>


<style scoped>
.compare-section {
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.title {
  font-weight: 700;
  font-size: 20px;
}

/* ---- Summary card met totaal stemmen ---- */

.summary-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 10px 16px;
  margin-bottom: 16px;
  background: #ffffff;
}

.summary-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.summary-title {
  font-size: 20px;
  font-weight: 600;

}

.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.summary-label {
  font-weight: 500;
  color: #374151;
}

.summary-value {
  font-weight: 700;
}

/* ---- rest van je bestaande styles ---- */

.subtitle {
  margin-bottom: 10px;
  font-weight: 600;
  color: #374151;
}

.dropdown-group {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.dropdown-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dropdown-label {
  font-size: 12px;
  color: #6b7280;
}

.dropdown {
  padding: 6px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: #f9fafb;
  font-size: 14px;
  cursor: pointer;
}

.dropdown:hover {
  background: #f3f4f6;
}

.error {
  color: #b91c1c;
  font-weight: 600;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #4b5563;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 5px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  margin-top: 10px;
  font-size: 14px;
  color: #6b7280;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.party-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.party-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 8px 25px;
  transition: background-color 0.2s ease;
}

.party-row:hover {
  background: #f9fafb;
}

.left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.name {
  font-weight: 600;
}

.right {
  display: flex;
  align-items: center;
  gap: 30px;
}

.metric {
  text-align: right;
}

.label {
  font-size: 12px;
  color: #6b7280;
}

.value {
  font-size: 18px;
  font-weight: 700;
}
</style>
