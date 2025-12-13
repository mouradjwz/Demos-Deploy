<script setup lang="ts">
import {ref, onMounted} from "vue";
import {getAvailableElections, type Election} from "@/api/ExistingElectionApi";

const availableElections = ref<Election[]>([]);
const selectedElectionId = ref<string>(localStorage.getItem("selectedElectionId") || "");

const emit = defineEmits(["update:electionId"]);

onMounted(async () => {
  try {
    availableElections.value = await getAvailableElections();
    if (!selectedElectionId.value && availableElections.value.length > 0) {
      selectedElectionId.value = availableElections.value[0].electionId;
      emit("update:electionId", selectedElectionId.value);
    }
  } catch (error) {
    console.error("Failed to fetch available elections:", error);
  }
});

function handleSelectionChange(newId: string) {
  selectedElectionId.value = newId;
  localStorage.setItem("selectedElectionId", newId);
  emit("update:electionId", newId);
}
</script>

<template>
  <div class="filters">
    <h2>Filters</h2>
    <div class="filter-group">
      <label>Verkiezing</label>
      <select v-model="selectedElectionId"
              @change="handleSelectionChange(($event.target as HTMLSelectElement).value)">
        <option
          v-for="election in availableElections"
          :key="election.electionId"
          :value="election.electionId"
        >
          {{ election.electionName }}
        </option>
      </select>
    </div>
  </div>
</template>

<style scoped>
.filters {
  width: 250px;
  min-width: 250px;
  background-color: white;
  border: var(--bordercolor);
  border-radius: 15px;
  padding: 20px;
  box-shadow: var(--rgb);
  position: sticky;
  top: 20px;
}

.filters h2 {
  margin-bottom: 15px;
  font-size: 1.5em;
  text-align: center;
}

.filter-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 15px;
}

.filter-group label {
  font-weight: 600;
  margin-bottom: 5px;
}

.filter-group select {
  border: 1px solid #ccc;
  border-radius: 8px;
  padding: 8px;
  font-size: 0.9em;
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
}
</style>
