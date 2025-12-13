<template>
  <div class="page">
    <ElectionFilter @update:electionId="(id) => (selectedElectionId = id)"/>

    <main class="content">
      <div class="party-detail-container">
        <div v-if="loading" class="loading">Loading party details...</div>
        <div v-else-if="error" class="error">{{ error }}</div>
        <div v-else>
          <div class="party-header">
            <router-link to="/parties" class="back-link">← Back to Parties</router-link>
            <h1>{{ partyName }}</h1>
            <p class="party-id">Party ID: {{ partyId }}</p>
          </div>

          <div class="candidates-section">
            <h2>Candidates ({{ candidates.length }})</h2>
            <div v-if="candidates.length === 0" class="no-candidates">
              No candidates found for this party.
            </div>
            <div v-else class="candidates-grid">
              <div
                v-for="candidate in candidates"
                :key="candidate.id"
                class="candidate-card"
              >
                <h3>{{ candidate.fullName }}</h3>
                <div class="candidate-details">
                  <p v-if="candidate.firstName"><strong>First Name:</strong> {{
                      candidate.firstName
                    }}
                  </p>
                  <p v-if="candidate.lastName"><strong>Last Name:</strong> {{ candidate.lastName }}
                  </p>
                  <p v-if="candidate.initials"><strong>Initials:</strong> {{ candidate.initials }}
                  </p>
                  <p v-if="candidate.gender"><strong>Gender:</strong> {{ candidate.gender }}</p>
                  <p v-if="candidate.locality"><strong>Locality:</strong> {{ candidate.locality }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted, watch} from "vue";
import {useRoute} from "vue-router";
import {getCandidatesByParty, type Candidate} from "@/api/CandidateApi";
import {getAllParties, type Party} from "@/api/partyApi";
import ElectionFilter from "@/components/filter/ElectionFilter.vue";

const route = useRoute();
const candidates = ref<Candidate[]>([]);
const loading = ref(true);
const error = ref<string>("");
const partyName = ref<string>("");
const partyId = ref<string>("");
const selectedElectionId = ref<string>(localStorage.getItem("selectedElectionId") || "");

async function fetchPartyDetails(electionId: string, partyId: string) {
  try {
    loading.value = true;

    // Get party name from the parties list
    const parties = await getAllParties(electionId);
    const party = parties.find((p) => p.id === partyId);
    partyName.value = party?.name || `Party ${partyId}`;

    // Get candidates for this party
    candidates.value = await getCandidatesByParty(electionId, partyId);
  } catch (err) {
    error.value = "Failed to load party details and candidates";
    console.error("Error loading party details:", err);
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  partyId.value = route.params.id as string;
  if (selectedElectionId.value) {
    fetchPartyDetails(selectedElectionId.value, partyId.value);
  }
});

watch(selectedElectionId, (newId) => {
  fetchPartyDetails(newId, partyId.value);
});
</script>

<style scoped>
.page {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
  padding: 20px;
  box-sizing: border-box;
}

/* Main content area */
.content {
  flex: 1;
  min-width: 0;
}

.party-detail-container {
  padding: 0;
  max-width: 1200px;
  margin: 0;
}

.loading,
.error {
  text-align: center;
  padding: 40px;
  font-size: 18px;
}

.error {
  color: #d32f2f;
}

.party-header {
  margin-bottom: 30px;
}

.back-link {
  display: inline-block;
  margin-bottom: 10px;
  color: #1976d2;
  text-decoration: none;
  font-weight: 500;
}

.back-link:hover {
  text-decoration: underline;
}

.party-header h1 {
  margin: 0 0 10px 0;
  color: #333;
}

.party-id {
  color: #666;
  margin: 0;
}

.candidates-section h2 {
  margin-bottom: 20px;
  color: #333;
}

.no-candidates {
  text-align: center;
  padding: 40px;
  color: #666;
  font-style: italic;
}

.candidates-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.candidate-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.candidate-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.candidate-card h3 {
  margin: 0 0 15px 0;
  color: #1976d2;
  font-size: 18px;
}

.candidate-details p {
  margin: 5px 0;
  font-size: 14px;
}

.candidate-details strong {
  color: #333;
}

/* Responsive: disable sticky behavior on small screens */
@media (max-width: 1000px) {
  .page {
    flex-direction: column;
  }
}
</style>
