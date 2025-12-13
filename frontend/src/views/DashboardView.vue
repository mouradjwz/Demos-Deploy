<script setup lang="ts">
import BarChart from "@/components/charts/BarChart.vue";
import PieChart from "@/components/charts/PieChart.vue";
import {ref, onMounted, watch, computed} from "vue";
import RankedPartyList from '@/components/display/RankedPartyList.vue';
import {shortenPartyName} from '../components/utils/PartyNames.ts';

interface ElectionData {
  partyId: string;
  partyName: string;
  fullName?: string;
  seats: number;
  percentage: number;
  totalVotes?: number;
  color?: string;
}

// ---- Hardcoded opkomstpercentages per jaar ----
const turnoutData: Record<string, string> = {
  "2017": "81.57%",
  "2021": "78.71%",
  "2023": "77.75%"
};

// ---- API basis URL ----
const API_BASE_URL = 'http://localhost:8081';

// ---- Reactive state ----
const loading = ref(true);
const error = ref<string | null>(null);
const electionData = ref<ElectionData[]>([]);
const selectedYear = ref("2023");
const selectedProvince = ref("nationaal");
const selectedParty = ref("alle");
const hasActiveFilters = ref(false);
const partySeats2023 = ref<{ party: string; zetels: number; color?: string }[]>([]);
const partyVotePercentages = ref<{ party: string; percentage: number; color?: string }[]>([]);

// ALLE partijen (inclusief 0 zetels) voor RankedPartyList
const allParties = computed(() => {
  let parties = electionData.value;

  // Filter op geselecteerde partij als er een is gekozen
  if (selectedParty.value !== "alle") {
    parties = parties.filter(p => p.partyName === selectedParty.value);
  }

  return parties
    .sort((a, b) => b.seats - a.seats)
    .map((p, index) => ({
      id: index + 1,
      name: p.partyName,
      displayName: p.fullName || p.partyName,
      fullName: p.fullName || p.partyName,
      seats: p.seats,
      percentage: p.percentage,
      votes: p.totalVotes ? p.totalVotes.toString() : "-",
      color: p.color
    }));
});

// Alleen partijen met zetels voor charts
const partiesWithSeats = computed(() => {
  let parties = electionData.value.filter(p => p.seats > 0);

  // Filter op geselecteerde partij als er een is gekozen
  if (selectedParty.value !== "alle") {
    parties = parties.filter(p => p.partyName === selectedParty.value);
  }

  return parties;
});

// Partijen voor filter dropdown (alleen van geselecteerd jaar)
const partiesForFilter = computed(() => {
  return electionData.value
    .map(p => p.partyName)
    .filter((name, index, array) => array.indexOf(name) === index) // Remove duplicates
    .sort();
});

const totalVotes = computed(() => {
  let parties = electionData.value;

  // Filter op geselecteerde partij als er een is gekozen
  if (selectedParty.value !== "alle") {
    parties = parties.filter(p => p.partyName === selectedParty.value);
  }

  return parties.reduce((acc, p) => acc + (p.totalVotes || 0), 0);
});

const totalSeats = computed(() => {
  let parties = electionData.value;

  // Filter op geselecteerde partij als er een is gekozen
  if (selectedParty.value !== "alle") {
    parties = parties.filter(p => p.partyName === selectedParty.value);
  }

  return parties.reduce((acc, p) => acc + p.seats, 0);
});

// ---- Helper functie om opkomstpercentage op te halen ----
function getTurnoutPercentage(year: string): string {
  return turnoutData[year] || "-";
}

// ---- Helper functie om jaar om te zetten naar election ID ----
function getElectionId(year: string): string {
  return `TK${year}`;
}

// ---- Reset filters ----
function resetFilters() {
  selectedProvince.value = "nationaal";
  selectedParty.value = "alle";
  hasActiveFilters.value = false;
}

// ---- Data ophalen ----
async function fetchElectionData(year: string) {
  try {
    loading.value = true;
    error.value = null;
    const electionId = getElectionId(year);
    console.log(`Fetching election data for ${electionId} from backend...`);

    const res = await fetch(`${API_BASE_URL}/api/dashboard/dto/${electionId}`);
    console.log("Raw API response:", res);

    if (!res.ok) {
      console.error(`API call failed with status ${res.status}: ${res.statusText}`);
      throw new Error(`HTTP ${res.status}: ${res.statusText}`);
    }

    const data = await res.json();
    console.log("Parsed API response:", data);

    if (!data || !Array.isArray(data) || data.length === 0) {
      console.error("Invalid or empty data received from the server:", data);
      throw new Error("Geen data ontvangen van de server");
    }

    // Sorteer op zetels, voeg kleur toe, rond percentage af, en verkort naam
    electionData.value = data
      .sort((a, b) => b.seats - a.seats)
      .map((p) => ({
        ...p,
        color: p.color,
        percentage: Math.round(p.percentage * 100) / 100,
        fullName: p.partyName,
        partyName: shortenPartyName(p.partyName),
      }));

    // Reset filters bij het laden van nieuw jaar
    resetFilters();

  } catch (err: any) {
    console.error("Fout bij het ophalen van verkiezingsdata:", err);
    error.value = err.message || "Er ging iets mis bij het laden van de data.";
  } finally {
    loading.value = false;
  }
}

// Update charts wanneer data of filters veranderen
function updateCharts() {
  // Voor BarChart - alleen partijen met zetels
  partySeats2023.value = partiesWithSeats.value.map(p => ({
    party: p.partyName,
    zetels: p.seats,
    color: p.color
  }));

  // Voor PieChart - alleen partijen met zetels
  partyVotePercentages.value = partiesWithSeats.value.map(p => ({
    party: p.partyName,
    percentage: Math.round(p.percentage * 100) / 100,
    color: p.color
  }));
}

// Watch for year changes
watch(selectedYear, (newYear) => {
  fetchElectionData(newYear);
});

// Watch for filter changes
watch([selectedProvince, selectedParty], () => {
  hasActiveFilters.value = selectedProvince.value !== "nationaal" || selectedParty.value !== "alle";
  updateCharts();
});

// Watch for election data changes
watch(electionData, () => {
  updateCharts();
});

onMounted(() => {
  fetchElectionData(selectedYear.value);
});
</script>

<template>
  <main class="dashboard">
    <aside class="filters">
      <div class="side">
        <h2>Filters</h2>
        <span v-if="hasActiveFilters" class="reset-filters" @click="resetFilters">
          X wissen
        </span>
        <span v-else class="reset-filters placeholder">X wissen</span>
      </div>
      <div class="filter-group">
        <label>Verkiezing</label>
        <select v-model="selectedYear">
          <option value="2023">Tweede Kamer 2023</option>
          <option value="2021">Tweede Kamer 2021</option>
          <option value="2017">Tweede Kamer 2017</option>
        </select>
        <label>Provincie</label>
        <select v-model="selectedProvince">
          <option value="nationaal">Nationaal</option>
        </select>
        <label>Partij</label>
        <select v-model="selectedParty">
          <option value="alle">Alle partijen</option>
          <option v-for="party in partiesForFilter" :key="party" :value="party">
            {{ party }}
          </option>
        </select>
      </div>
    </aside>

    <section class="content">
      <div v-if="error" class="error">{{ error }}</div>
      <div v-else-if="loading" class="loading">Gegevens laden...</div>

      <div v-else class="stats-row">
        <div class="stat-card">
          <p>Verkiezing</p>
          <h2>Tweede Kamer {{ selectedYear }}</h2>
        </div>
        <div class="stat-card">
          <p>Opkomst</p>
          <h2>{{ getTurnoutPercentage(selectedYear) }}</h2>
        </div>
        <div class="stat-card">
          <p>Totaal Zetels</p>
          <h2>{{ totalSeats }}</h2>
        </div>
        <div class="stat-card">
          <p>Totaal stemmen</p>
          <h2>{{ totalVotes.toLocaleString() }}</h2>
        </div>
      </div>

      <div class="charts-row" v-if="partiesWithSeats.length">
        <div class="chart-box">
          <BarChart
            :title="selectedParty === 'alle' ? `${selectedYear} Zetelverdeling` : `${selectedParty} Zetels ${selectedYear}`"
            :data="partySeats2023"
          />
        </div>
        <div class="chart-box">
          <PieChart
            :title="selectedParty === 'alle' ? `Stempercentage per partij ${selectedYear}` : `${selectedParty} Stempercentage ${selectedYear}`"
            :data="partyVotePercentages"
          />
        </div>
      </div>

      <div v-else-if="!loading" class="no-data">
        Geen data beschikbaar voor de geselecteerde filters.
      </div>

      <div class="bottom-row" v-if="allParties.length">
        <!-- Gebruik allParties (inclusief 0 zetels) voor RankedPartyList -->
        <RankedPartyList :parties="allParties"/>
      </div>
    </section>
  </main>
</template>

<style scoped>
.side{
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reset-filters {
  color: #007bff;
  cursor: pointer;
  font-size: 0.9em;
  font-weight: 500;
}

.reset-filters:hover {
  text-decoration: underline;
}

.reset-filters.placeholder {
  color: transparent;
  cursor: default;
}

.dashboard {
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  gap: 2rem;
  margin-top: 40px;
  padding: 0 40px;
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}

.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  gap: 20px; /* Consistent gap between all content children */
}

.content > * {
  width: 100%;
}

.filters {
  width: 250px;
  min-width: 250px;
  background-color: white;
  border: var(--bordercolor);
  border-radius: 15px;
  padding: 20px;
  box-shadow: var(--rgb);
}

.filters h2 {
  margin-bottom: 15px;
  font-size: 1.5em;
  text-align: center;
}

.filter-group {
  display: flex;
  flex-direction: column;
}

.filter-group label {
  font-weight: 600;
  margin-bottom: 5px;
}

.filter-group select {
  border: 1px solid #ccc;
  border-radius: 8px;
  padding: 8px;
  font-size: 1em;
  margin-bottom: 15px;
}

.description {
  text-align: center;
  color: var(--grey);
  font-weight: bold;
  font-size: 1.3em;
  max-width: 1300px;
  margin: 0 auto;
}

h1 {
  font-size: 3em;
  font-weight: bold;
  margin-bottom: 10px;
  text-align: center;
}

.stats-row {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 20px; /* Same gap as charts-row */
  margin-bottom: 0; /* Remove bottom margin since gap is handled by parent */
}

.stat-card {
  background-color: white;
  border: var(--bordercolor);
  border-radius: 15px;
  padding: 20px 25px; /* Slightly more balanced padding */
  text-align: center;
  flex: 1;
  min-width: 160px;
  max-width: 200px;
  box-shadow: var(--rgb);
  box-sizing: border-box;
}

.stat-card p {
  margin: 0;
  color: #444;
  font-weight: 500;
  font-size: 0.9em;
}

.stat-card h2 {
  margin: 8px 0 0;
  font-size: 1.6em;
  font-weight: bold;
}

.charts-row {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 20px;
  width: 100%;
  margin-bottom: 0; /* Remove bottom margin since gap is handled by parent */
}

.chart-box {
  background-color: white;
  border: var(--bordercolor);
  border-radius: 15px;
  padding: 20px;
  flex: 1;
  min-width: 300px;
  max-width: calc(50% - 10px); /* Account for gap */
  box-shadow: var(--rgb);
  box-sizing: border-box;
}

.bottom-row {
  display: flex;
  justify-content: center;
  width: 100%;
}

.box {
  border: var(--bordercolor);
  padding: 15px;
  background-color: white;
  border-radius: 15px;
}

.wide {
  width: 100%;
}

.error {
  color: red;
  text-align: center;
  padding: 20px;
  background-color: #ffe6e6;
  border-radius: 8px;
}

.loading {
  text-align: center;
  padding: 20px;
  font-size: 1.2em;
}

.no-data {
  text-align: center;
  padding: 40px;
  color: #666;
  font-style: italic;
  background-color: #f9f9f9;
  border-radius: 8px;
}

@media (max-width: 1200px) {
  .chart-box {
    max-width: 100%;
  }
}

@media (max-width: 1000px) {
  .dashboard {
    flex-direction: column;
    padding: 0 20px;
    gap: 20px;
  }

  .filters {
    width: 100%;
    max-width: 100%;
    margin-bottom: 0;
  }

  .content {
    max-width: 100%;
    gap: 15px;
  }

  .charts-row {
    flex-direction: column;
    align-items: center;
    gap: 15px;
  }

  .chart-box {
    width: 100%;
    max-width: 100%;
  }

  .stats-row {
    gap: 15px;
  }

  .stat-card {
    min-width: 140px;
    max-width: 160px;
    padding: 15px 20px;
  }

  .wide {
    max-width: 100%;
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 0 15px;
    margin-top: 20px;
  }

  .content {
    gap: 15px;
  }

  .stats-row {
    gap: 12px;
  }

  .stat-card {
    min-width: calc(50% - 12px);
    max-width: calc(50% - 12px);
    padding: 12px 15px;
  }

  .stat-card h2 {
    font-size: 1.4em;
  }

  .charts-row {
    gap: 15px;
  }

  .chart-box {
    padding: 15px;
  }

  h1 {
    font-size: 2.5em;
  }
}

@media (max-width: 480px) {
  .stats-row {
    gap: 10px;
  }

  .stat-card {
    min-width: calc(50% - 10px);
    max-width: calc(50% - 10px);
    padding: 10px 12px;
  }

  .stat-card h2 {
    font-size: 1.3em;
  }

  .stat-card p {
    font-size: 0.8em;
  }
}

html, body {
  overflow-x: hidden;
  max-width: 100%;
}
</style>
