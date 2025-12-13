  <script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import KieskringKaart from "@/components/KieskringKaart.vue";

  const router = useRouter()

  /** Navigatie handlers */
  const goToDashboard = () => router.push({ name: 'dashboard' })
  const goToPartijen  = () => router.push({ path: 'parties' })

  /**
   * define stat.
   */
  type Stat = {
    title: string
    value: number
    subtitle: string
    showPlus?: boolean
  }

  const stats = ref<Stat[]>([])

  /** Haal data op bij mount en zet direct in de UI. */
  onMounted(async () => {
    const res = await fetch('http://localhost:8081/api/stats')
    const fetched: Stat[] = await res.json()
    stats.value = fetched
  })
  </script>

  <template>
    <main class="page">
      <section class="hero">
        <h1>
          Verkiezingsdata voor de <span class="accent">nieuwe generatie</span>
        </h1>

        <p class="tagline">
          Check hoe politiek Nederland echt verandert met interactieve visualisaties,
          vergelijk verkiezingsresultaten en discussieer over data-gedreven
          inzichten.
        </p>

        <div class="cta-row">
          <button @click="goToDashboard" class="btn btn-white">
            <svg viewBox="0 0 24 24" width="18" height="18">
              <path d="M3 3h18v18H3z" fill="none" />
              <path d="M5 13h3v6H5zm5-4h3v10h-3zm5 2h3v8h-3zM4 5h16v2H4z" />
            </svg>
            Bekijk Dashboard
          </button>

          <button @click="goToPartijen" class="btn btn-white">
            <svg viewBox="0 0 24 24" width="18" height="18">
              <path
                d="M10.5 3a7.5 7.5 0 015.93 12.21l3.68 3.68-1.42 1.42-3.68-3.68A7.5 7.5 0 1110.5 3zm0 2a5.5 5.5 0 100 11 5.5 5.5 0 000-11z"
              />
            </svg>
            Verken Partijen
          </button>
        </div>
      </section>
  <!--    stats-->
      <section class="stats-section" aria-label="Verkiezingsdata in cijfers">
        <div class="stats">
          <article
            v-for="(s, i) in stats"
            :key="i"
            class="stat has-tooltip"
            :data-tooltip="`${s.value}${s.showPlus ? '+' : ''} ${s.title} — ${s.subtitle}`"
            :aria-label="`${s.title}: ${s.value}${s.showPlus ? '+' : ''}. ${s.subtitle}`"
          >
            <div class="stat-number">
              {{ s.value }}<span v-if="s.showPlus">+</span>
            </div>
            <div class="stat-title">{{ s.title }}</div>
            <p class="stat-caption">{{ s.subtitle }}</p>
          </article>
        </div>
      </section>

      <section class="features">
        <h2>Ontdek de mogelijkheden</h2>
        <p class="subtitle">
          Coole tools voor het checken van Nederlandse verkiezingsdata!
        </p>

        <div class="card-grid">
          <article class="card">
            <div class="icon">
              <svg viewBox="0 0 24 24" width="20" height="20">
                <path d="M6 17h12v2H6zm0-6h8v2H6zm0-4h12v2H6z" />
              </svg>
            </div>
            <h3>Dashboard</h3>
            <p>Bekijk verkiezingsresultaten in interactieve grafieken en charts.</p>
          </article>

          <article class="card">
            <div class="icon">
              <svg viewBox="0 0 24 24" width="20" height="20">
                <path d="M12 7a5 5 0 015 5v4h-2v-4a3 3 0 10-6 0v4H7v-4a5 5 0 015-5z" />
              </svg>
            </div>
            <h3>Partijen</h3>
            <p>Ontdek partijen, hun standpunten en provinciale resultaten.</p>
          </article>

          <article class="card">
            <div class="icon">
              <svg viewBox="0 0 24 24" width="20" height="20">
                <path d="M7 7h5v5H7zM12 12h5v5h-5zM7 12h5v5H7zM12 7h5v5h-5z" />
              </svg>
            </div>
            <h3>Vergelijken</h3>
            <p>Vergelijk partijen, kandidaten en verkiezingsresultaten.</p>
          </article>

          <article class="card">
            <div class="icon">
              <svg viewBox="0 0 24 24" width="20" height="20">
                <path
                  d="M6 6h12v8H8l-2 2V6zm2 3h8v2H8z"
                  fill-rule="evenodd"
                  clip-rule="evenodd"
                />
              </svg>
            </div>
            <h3>Forum</h3>
            <p>Discussieer over verkiezingsdata met andere gebruikers.</p>
          </article>
        </div>
      </section>

      <section class="about">
        <h2>Over dit project</h2>
        <div class="about-grid">
          <article class="about-card">
            <div class="about-header">
              <svg viewBox="0 0 24 24" width="20" height="20">
                <path d="M6 17h12v2H6zm0-6h8v2H6zm0-4h12v2H6z" />
              </svg>
              <h3>Voor jonge kiezers</h3>
            </div>
            <p>
              Deze applicatie is speciaal ontwikkeld voor kiezers tussen de 16 en
              30 jaar om Nederlandse politieke data toegankelijker en
              begrijpelijker te maken.
            </p>
            <p>
              Door moderne visualisaties en interactieve tools maken we complexe
              verkiezingsdata inzichtelijk voor een nieuwe generatie politiek
              geïnteresseerden.
            </p>
          </article>

          <article class="about-card">
            <div class="about-header">
              <svg viewBox="0 0 24 24" width="20" height="20">
                <path d="M5 13h3v6H5zm5-4h3v10h-3zm5 2h3v8h-3zM4 5h16v2H4z" />
              </svg>
              <h3>Data-gedreven</h3>
            </div>
            <p>
              Alle gegevens zijn gebaseerd op officiële verkiezingsuitslagen van
              de Kiesraad en andere betrouwbare bronnen.
            </p>
            <p>
              We presenteren feiten zonder politieke kleuring, zodat je zelf je
              eigen conclusies kunt trekken over Nederlandse politieke trends.
            </p>
          </article>
        </div>
        <KieskringKaart />
      </section>
    </main>
  </template>

  <style scoped>
  .page {
    background: #fff;
    color: #0a0a0a;
    padding: 48px 20px 80px;
    font-family: system-ui, sans-serif;
    display: grid;
    place-items: center;
  }
  .hero,
  .features,
  .about {
    width: 100%;
    max-width: 1120px;
    margin-bottom: 64px;
  }
  h1 { font-size: clamp(32px, 5.2vw, 48px); font-weight: 800; margin-bottom: 12px; }
  h2 { text-align: center; font-size: 28px; font-weight: 700; margin-bottom: 8px; }
  h3 { font-weight: 700; font-size: 16px; }
  .accent { color: #1e66f5; }
  .tagline { color: var(--grey); max-width: 840px; margin-bottom: 28px; }
  .subtitle { text-align: center; color: var(--grey); margin-bottom: 28px; }

  .cta-row { display: flex; gap: 18px; flex-wrap: wrap; margin: 8px 0 36px; justify-content: center; }
  .btn {
    display: inline-flex; align-items: center; gap: 10px;
    padding: 12px 20px; border-radius: 999px; font-weight: 600;
    text-decoration: none; border: var(--bordercolor);
    background: #fff; color: #0a0a0a; box-shadow: var(--rgb);
    transition: background 0.2s ease, box-shadow 0.2s ease, transform 0.1s ease;
  }
  .btn:hover { background: #f5f5f5; cursor: pointer; }

  .stats {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 18px;
    max-width: 1100px;
    margin: 0 auto 40px;
    text-align: center;
  }

  .stat-number {
    font-size: 22px;
    font-weight: 800;
    color: #fff;
    width: 90px;
    height: 90px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 8px;
    border-radius: 50%;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    animation: popIn 1s ease forwards;
  }

  .stat:nth-child(1) .stat-number { background-color: #ff4b4b; }
  .stat:nth-child(2) .stat-number { background-color: #1e66f5; }
  .stat:nth-child(3) .stat-number { background-color: #3fa34d; }
  .stat:nth-child(4) .stat-number { background-color: #ffd43b; }

  @keyframes popIn {
    0% {
      transform: scale(0.5);
      opacity: 0;
    }
    100% {
      transform: scale(1);
      opacity: 1;
    }
  }

  .stat-title  { font-weight: 700; }
  .stat-caption{ color: var(--grey); font-size: 14px; }

  .has-tooltip { position: relative; }
  .has-tooltip::after {
    content: attr(data-tooltip);
    position: absolute;
    left: 50%; transform: translateX(-50%) translateY(-6px);
    bottom: calc(100% + 8px);
    pointer-events: none;
    white-space: nowrap;
    background: #0a0a0a; color: #fff;
    font-size: 12px; line-height: 1;
    padding: 8px 10px; border-radius: 8px;
    opacity: 0; visibility: hidden;
    transition: opacity .15s ease, transform .15s ease, visibility .15s;
    box-shadow: 0 6px 18px rgba(2,6,23,.15);
  }
  .has-tooltip:hover::after {
    opacity: 1;
    visibility: visible;
    transform: translateX(-50%) translateY(0%);
  }

  .card:nth-child(1) {
    background-color: #ffe5e5;
  }
  .card:nth-child(2) {
    background-color: #e6efff;
  }
  .card:nth-child(3) {
    background-color: #e7f6e7;
  }
  .card:nth-child(4) {
    background-color: #fff9db;
  }
  .card-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; }
  .card {
    border: var(--bordercolor);
    border-radius: 14px;
    padding: 24px;
    background: #fff;
    transition: 0.2s;
  }
  .card:hover {
    box-shadow: 0 8px 28px
    rgba(2, 6, 23, 0.06);
    cursor: pointer;
  }
  .icon {
    background: #f6f8fb; border-radius: 12px; padding: 10px;
    display: inline-flex; align-items: center; justify-content: center; margin-bottom: 8px;
  }
  .icon svg { fill: #1e66f5; }
  .about-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 18px; margin-top: 28px; }
  .about-card { border: var(--bordercolor); border-radius: 14px; padding: 24px; background: #fff; transition: box-shadow 0.2s; }
  .about-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
  .about-header svg { fill: #0a0a0a; }
  .about-card p { color: var(--grey); font-size: 14px; line-height: 1.6; }

  @media (max-width: 768px) {
    .stats { grid-template-columns: repeat(2, 1fr); }
    .about-grid { grid-template-columns: 1fr; }
  }
  </style>
