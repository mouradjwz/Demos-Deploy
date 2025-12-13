<script setup lang="ts">
import { ref, computed} from 'vue';

interface Party {
  id: number;
  name: string;
  fullName: string;
  seats: number;
  percentage: number;
  votes: string;
  color?: string;
}

const props = defineProps<{
  title?: string;
  parties: Party[];
}>();

const currentPage = ref(0);
const partiesPerPage = 5;

// Bereken totale aantal pagina's
const totalPages = computed(() => {
  return Math.ceil(props.parties.length / partiesPerPage);
});


// Haal partijen op voor huidige pagina
const currentParties = computed(() => {
  const startIndex = currentPage.value * partiesPerPage;
  const endIndex = startIndex + partiesPerPage;
  const parties = props.parties.slice(startIndex, endIndex).map((party, index) => ({
    ...party,
    displayRank: startIndex + index + 1
  }));

  console.log('Current parties:', parties);
  return parties;
});

// Ga naar volgende pagina
const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++;
    console.log('Next page:', currentPage.value);
  }
};

// Ga naar vorige pagina
const prevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value--;
    console.log('Previous page:', currentPage.value);
  }
};

// Ga naar specifieke pagina
const goToPage = (page: number) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page;
    console.log('Go to page:', page);
  }
};

// Gebruik de partijkleur voor het bolletje
const getRankColor = (party: Party) => {
  // Gebruik de partijkleur als die beschikbaar is, anders fallback naar grijstinten
  return party.color || '#e0e0e0';
};

// Fallback kleuren voor als er geen partijkleur is (op basis van rang)
const getFallbackColor = (rank: number) => {
  const fallbackColors = {
    1: '#ffeeb3',
    2: '#e0e0e0',
    3: '#d7e9c8',
    4: '#ffd7d7',
    5: '#d7f0e3'
  };
  return fallbackColors[rank as keyof typeof fallbackColors] || '#e0e0e0';
};

// Paginatie knoppen
const paginationButtons = computed(() => {
  if (totalPages.value <= 1) return [];

  const buttons = [];
  const maxVisibleButtons = 5;

  let startPage = Math.max(0, currentPage.value - Math.floor(maxVisibleButtons / 2));
  let endPage = Math.min(totalPages.value - 1, startPage + maxVisibleButtons - 1);

  // Adjust start page if we're at the end
  if (endPage - startPage + 1 < maxVisibleButtons) {
    startPage = Math.max(0, endPage - maxVisibleButtons + 1);
  }

  for (let i = startPage; i <= endPage; i++) {
    buttons.push(i);
  }

  return buttons;
});

// Toon paginatie alleen als er meer dan 1 pagina is
const showPagination = computed(() => {
  return totalPages.value > 1;
});
</script>

<template>
  <div class="rank-list-container">
    <div class="header">
      <h2 class="rank-title">{{ props.title || 'Top Partijen' }}</h2>
      <div class="pagination-info" v-if="showPagination">
        {{
          currentPage * partiesPerPage + 1
        }}-{{ Math.min((currentPage + 1) * partiesPerPage, props.parties.length) }} van
        {{ props.parties.length }}
      </div>
    </div>

    <div class="rank-items">
      <div class="rank-item" v-for="party in currentParties" :key="party.id">
        <div
          class="rank-number"
          :style="{
            backgroundColor: getRankColor(party),
            border: party.color ? `2px solid ${party.color}` : 'none'
          }"
        >
          {{ party.displayRank }}
        </div>
        <div class="party-info">
          <div class="party-name">{{ party.name }}</div>
          <div class="party-full-name">{{ party.fullName }}</div>
        </div>
        <div class="party-stats">
          <div class="seats-count">{{ party.seats }} zetels</div>
          <div class="vote-info">{{ party.percentage }}% • {{ party.votes }} stemmen</div>
        </div>
      </div>
    </div>

    <!-- Paginatie controls - ALTIJD zichtbaar voor testing -->
    <div class="pagination-controls" v-if="showPagination">
      <button
        class="pagination-btn prev-btn"
        @click="prevPage"
        :disabled="currentPage === 0"
        :class="{ disabled: currentPage === 0 }"
      >
        ← Vorige
      </button>

      <div class="page-numbers" v-if="paginationButtons.length > 0">
        <button
          v-for="page in paginationButtons"
          :key="page"
          class="page-btn"
          :class="{ active: page === currentPage }"
          @click="goToPage(page)"
        >
          {{ page + 1 }}
        </button>
      </div>

      <button
        class="pagination-btn next-btn"
        @click="nextPage"
        :disabled="currentPage === totalPages - 1"
        :class="{ disabled: currentPage === totalPages - 1 }"
      >
        Volgende →
      </button>
    </div>


  </div>
</template>

<style scoped>
.rank-list-container {
  background-color: white;
  border-radius: 15px;
  padding: 16px;
  border: var(--bordercolor);
  box-shadow: var(--rgb);
  width: 100%;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.rank-title {
  font-weight: 600;
  color: #333;
  font-size: 1.2rem;
  margin: 0;
}

.pagination-info {
  font-size: 0.9rem;
  color: #666;
}

.rank-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.rank-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.rank-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 12px;
  font-size: 0.9rem;
  color: #333;
  /* Witte tekst voor betere leesbaarheid op donkere achtergronden */
  text-shadow: 0px 0px 1px rgba(255, 255, 255, 0.7);
  /* Subtiele schaduw voor diepte */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.party-info {
  flex: 1;
}

.party-name {
  font-weight: 600;
  color: #333;
}

.party-full-name {
  font-size: 0.85rem;
  color: #666;
}

.party-stats {
  text-align: right;
}

.seats-count {
  font-weight: bold;
  color: #333;
}

.vote-info {
  font-size: 0.85rem;
  color: #666;
}

/* Paginatie styles */
.pagination-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  padding-top: 16px;
  border-top: 1px solid #e0e0e0;
}

.pagination-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s ease;
  min-width: 80px;
}

.pagination-btn:hover:not(.disabled) {
  background-color: #f5f5f5;
  border-color: #999;
}

.pagination-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background-color: #f9f9f9;
}

.page-numbers {
  display: flex;
  gap: 4px;
}

.page-btn {
  padding: 8px 12px;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  min-width: 40px;
  transition: all 0.2s ease;
}

.page-btn:hover {
  background-color: #f5f5f5;
}

.page-btn.active {
  background-color: #1A4788;
  color: white;
  border-color: #1A4788;
}

/* Responsive design */
@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }

  .pagination-info {
    align-self: flex-end;
  }

  .pagination-controls {
    flex-direction: column;
    gap: 12px;
  }

  .page-numbers {
    order: -1;
  }

  .rank-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
  }

  .rank-item:last-child {
    border-bottom: none;
  }

  .party-stats {
    text-align: left;
    width: 100%;
    display: flex;
    justify-content: space-between;
  }
}
</style>
