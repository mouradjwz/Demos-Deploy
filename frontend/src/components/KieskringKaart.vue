<template>
  <div class="kieskring-container">
    <!-- Header -->
    <div class="map-header">
      <h3 class="map-title">Interactieve Kieskringkaart</h3>
      <p class="map-subtitle">Klik op een kieskring om de top partijen te bekijken</p>
    </div>

    <!-- SVG-kaart -->
    <div class="map-wrapper">
      <div
        v-html="svgContent"
        class="svg-map"
        @click="handleClick"
      ></div>
    </div>

    <!-- Modal -->
    <div
      v-if="selectedKring"
      class="modal-overlay"
      @click.self="closeModal"
    >
      <div class="modal-content">
        <!-- Titel -->
        <div class="modal-header">
          <h2 class="modal-title">
            {{ selectedKring.name }}
          </h2>
          <button @click="closeModal" class="close-button" aria-label="Sluiten">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>

        <!-- Partij tabel -->
        <div class="parties-list">
          <div
            v-for="(partij, index) in selectedKring.partijen"
            :key="index"
            class="party-item"
            :style="{ animationDelay: `${index * 0.05}s` }"
          >
            <div class="party-rank">{{ index + 1 }}</div>
            <div class="party-info">
              <span class="party-name">{{ partij.naam }}</span>
              <span class="party-votes">{{ partij.stemmen.toLocaleString('nl-NL') }} stemmen</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";

const svgContent = ref<string>("");

// API response types (matches backend DTO)
interface PartyVotes {
  party: string;
  votes: number;
}

interface KieskringTopParties {
  kieskring: number;
  topParties: PartyVotes[];
}

// Component data types
interface Partij {
  naam: string;
  stemmen: number;
}

interface KringData {
  id: string;
  name: string;
  partijen: Partij[];
}

const selectedKring = ref<KringData | null>(null);
const apiData = ref<KieskringTopParties[]>([]);
const selectedElection = ref<string>("TK2023");

// Map SVG kring-X IDs to actual kieskring numbers
const kringIdToNumber: Record<string, number> = {
  "kring-1": 1,   // Groningen
  "kring-2": 2,   // Leeuwarden
  "kring-3": 3,   // Assen
  "kring-4": 4,   // Zwolle
  "kring-5": 5,   // Lelystad
  "kring-6": 6,   // Nijmegen
  "kring-7": 7,   // Arnhem
  "kring-8": 8,   // Utrecht
  "kring-9": 9,   // Amsterdam
  "kring-10": 10, // Haarlem
  "kring-11": 11, // Den Helder
  "kring-12": 12, // Leiden
  "kring-13": 13, // 's-Gravenhage
  "kring-14": 14, // Rotterdam
  "kring-15": 15, // Dordrecht
  "kring-16": 16, // Middelburg
  "kring-17": 17, // Tilburg
  "kring-18": 18, // 's-Hertogenbosch
  "kring-19": 19, // Maastricht
  "kring-20": 20, // Bonaire
};

onMounted(async () => {
  // Load SVG
  const svgResponse = await fetch("/Kieskringen_interactief.svg");
  svgContent.value = await svgResponse.text();

  // Fetch kieskring data from API
  try {
    const apiResponse = await fetch(`http://localhost:8081/api/kieskring/top/${selectedElection.value}`);
    apiData.value = await apiResponse.json();
    console.log("Loaded kieskring data:", apiData.value);
  } catch (error) {
    console.error("Failed to load kieskring data:", error);
  }
});

// Get party data for a specific kieskring
function getPartiesForKring(kringId: string): Partij[] {
  const kieskringNumber = kringIdToNumber[kringId];
  if (!kieskringNumber) return [];

  const kieskringData = apiData.value.find(k => k.kieskring === kieskringNumber);
  if (!kieskringData) return [];

  return kieskringData.topParties.map(p => ({
    naam: p.party,  // Use "party" field from backend
    stemmen: p.votes
  }));
}

function handleClick(e: MouseEvent) {
  let el = e.target as HTMLElement | null;
  if (!el) return;

  // Zoek bovenliggende path/g met id="kring-x"
  while (el && (!el.id || !el.id.startsWith("kring-"))) {
    el = el.parentElement;
  }

  if (!el) return;

  const id = el.id;

  // Naam ophalen uit SVG via label attribuut
  const name =
    el.getAttribute("inkscape:label") ||
    el.getAttribute("label") ||
    el.getAttribute("data-label") ||
    id;

  // popup vullen met echte data
  const partijen = getPartiesForKring(id);
  
  selectedKring.value = {
    id,
    name,
    partijen: partijen.length > 0 ? partijen : [
      { naam: "Geen data", stemmen: 0 },
    ],
  };
}

function closeModal() {
  selectedKring.value = null;
}
</script>

<style scoped>
.kieskring-container {
  width: 100%;
  max-width: 1120px;
  margin: 48px auto 0;
}

.map-header {
  text-align: center;
  margin-bottom: 32px;
}

.map-title {
  font-size: 28px;
  font-weight: 700;
  color: #0a0a0a;
  margin-bottom: 8px;
}

.map-subtitle {
  color: #6c757d;
  font-size: 16px;
}

.map-wrapper {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s ease;
}

.map-wrapper:hover {
  box-shadow: 0 8px 28px rgba(2, 6, 23, 0.06);
}

.svg-map {
  width: 100%;
  max-width: 100%;
  user-select: none;
  display: flex;
  justify-content: center;
}

.svg-map :deep(svg) {
  max-width: 100%;
  height: auto;
}

.svg-map :deep([id^="kring-"]) {
  transition: all 0.3s ease;
  cursor: pointer;
  filter: brightness(0.95);
}

.svg-map :deep([id^="kring-"]:hover) {
  fill: #1e66f5 !important;
  filter: brightness(1.1);
  transform: scale(1.02);
  transform-origin: center;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 90%;
  max-width: 480px;
  max-height: 80vh;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 24px 16px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-title {
  font-size: 24px;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0;
}

.close-button {
  background: transparent;
  border: none;
  color: #6c757d;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.close-button:hover {
  background: #f5f5f5;
  color: #0a0a0a;
}

.parties-list {
  padding: 24px;
  max-height: 60vh;
  overflow-y: auto;
}

.party-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 12px;
  transition: all 0.2s ease;
  animation: slideIn 0.3s ease forwards;
  opacity: 0;
}

@keyframes slideIn {
  from {
    transform: translateX(-20px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.party-item:hover {
  background: #e6efff;
  transform: translateX(4px);
}

.party-item:last-child {
  margin-bottom: 0;
}

.party-rank {
  width: 36px;
  height: 36px;
  background: #1e66f5;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
}

.party-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.party-name {
  font-weight: 600;
  font-size: 16px;
  color: #0a0a0a;
}

.party-votes {
  font-size: 14px;
  color: #1e66f5;
  font-weight: 600;
}

/* Responsive */
@media (max-width: 768px) {
  .map-wrapper {
    padding: 20px;
  }

  .modal-content {
    width: 95%;
    max-width: none;
  }

  .modal-title {
    font-size: 20px;
  }

  .party-item {
    padding: 12px;
  }

  .party-rank {
    width: 32px;
    height: 32px;
    font-size: 14px;
  }
}
</style>
