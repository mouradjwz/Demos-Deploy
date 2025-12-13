<template>
  <div class="page">
    <ElectionFilter @update:electionId="(id) => (selectedElectionId = id)"/>

    <main class="content">
      <div class="parties-container">
        <h1>Political Parties</h1>
        <div class="parties-grid">
          <router-link
            v-for="party in parties"
            :key="party.id"
            :to="{ name: 'party-detail', params: { id: party.id }}"
            class="party-card"
            :data-party="party.name"
          >
            <h2>{{ party.name }}</h2>
          </router-link>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import {getAllParties, type Party} from "@/api/partyApi.ts";
import {onMounted, ref, watch} from "vue";
import ElectionFilter from "@/components/filter/ElectionFilter.vue";

const parties = ref<Party[]>([]);
const selectedElectionId = ref<string>(localStorage.getItem("selectedElectionId") || "");

async function fetchParties(electionId: string) {
  try {
    parties.value = await getAllParties(electionId);
  } catch (error) {
    console.error("Failed to fetch parties:", error);
  }
}

onMounted(() => {
  if (selectedElectionId.value) {
    fetchParties(selectedElectionId.value);
  }
});

watch(selectedElectionId, (newId) => {
  localStorage.setItem("selectedElectionId", newId);
  fetchParties(newId);
});
</script>

<style scoped>
.page {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
  padding: 20px;
  box-sizing: border-box;
  overflow: visible;
}

/* Main content becomes independently scrollable if needed */
.content {
  flex: 1;
  min-width: 0;
}

.parties-container {
  padding: 0;
}

.parties-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.party-card {
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  text-decoration: none;
  color: inherit;
}

.party-card:hover {
  background-color: #f5f5f5;
}

/* Responsive: disable sticky behavior on small screens */
@media (max-width: 1000px) {
  .page {
    flex-direction: column;
  }
}
</style>
