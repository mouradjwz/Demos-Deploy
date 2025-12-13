<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { use } from 'echarts/core'
import { ScatterChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, TitleComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'


use([ScatterChart, GridComponent, TooltipComponent, TitleComponent, CanvasRenderer])


</script>


<template>
  <main>
    <div id="container">
      <h1>Het Nederlandse Politieke Systeem</h1>
      <p id="subtitle">
        Een visuele gids voor jonge kiezers om de Nederlandse democratie te begrijpen.
        Van verkiezingen tot kabinetsformatie – hier vind je alles wat je moet weten.
      </p>

      <!-- Het Nederlandse Parlement -->
      <section class="card">
        <h2>Het Nederlandse Parlement</h2>
        <div class="grid-2">
          <div class="parlement-box green">
            <h3>Tweede Kamer</h3>
            <p>150 zetels – rechtstreeks gekozen</p>
            <p>Controleert de regering</p>
            <p>Maakt samen met de Eerste Kamer wetten</p>
            <div class="dots">
              <span v-for="n in 150" :key="n" class="dot green-dot"></span>
            </div>
          </div>
          <div class="parlement-box purple">
            <h3>Eerste Kamer</h3>
            <p>75 zetels – indirect gekozen</p>
            <p>Controleert de regering</p>
            <p>Kan wetten goedkeuren of verwerpen</p>
            <div class="dots">
              <span v-for="n in 75" :key="n" class="dot purple-dot"></span>
            </div>
          </div>
        </div>
      </section>

<!--      <section class="card">-->
<!--        <h2>Politiek Spectrum </h2>-->
<!--        <div class="chart-flex-container">-->
<!--          <v-chart :option="chartOptions" autoresize class="chart" />-->
<!--          <div class="chart-labels">-->
<!--            <span class="left">← Links</span>-->
<!--            <span class="right">Rechts →</span>-->
<!--            <span class="top">↑ Progressief</span>-->
<!--            <span class="bottom">Conservatief ↓</span>-->
<!--          </div>-->
<!--        </div>-->
<!--      </section>-->

      <!-- Bestuursniveaus -->
      <section class="card">
        <h2>Bestuursniveaus</h2>
        <ul class="levels">
          <li><span class="dot blue"></span> Europees Parlement – elke 5 jaar</li>
          <li><span class="dot green-dot"></span> Tweede Kamer – landelijke verkiezingen</li>
          <li><span class="dot purple-dot"></span> Eerste Kamer – indirect via Provinciale Staten</li>
          <li><span class="dot red"></span> Provinciale Staten – regionale verkiezingen</li>
          <li><span class="dot grey"></span> Gemeenteraden – lokale verkiezingen</li>
        </ul>
      </section>

      <!-- Coalitievorming -->
      <section class="card">
        <h2>Coalitievorming</h2>
        <div class="steps">
          <div class="step"><strong>1.</strong> Verkiezingen → zetelverdeling</div>
          <div class="step"><strong>2.</strong> Formatie → partijen onderhandelen</div>
          <div class="step"><strong>3.</strong> Regeerakkoord → afspraken over beleid</div>
          <div class="step"><strong>4.</strong> Kabinet → ministers benoemd</div>
        </div>
      </section>

      <!-- Verkiezingskalender -->
      <section class="card">
        <h2>Verkiezingskalender</h2>
        <ul class="calendar">
          <li><span class="year grey">2021</span> Tweede Kamer verkiezingen</li>
          <li><span class="year grey">2022</span> Gemeenteraadsverkiezingen</li>
          <li><span class="year grey">2023</span> Provinciale Staten verkiezingen</li>
          <li><span class="year grey">2024</span> Europees Parlement verkiezingen</li>
          <li><span class="year green">2025</span> Tweede Kamer verkiezingen</li>
        </ul>
      </section>

      <!-- Belangrijke feiten -->
      <section class="card">
        <h2>Belangrijke Feiten</h2>
        <ul>
          <li>Kiesrecht vanaf 18 jaar</li>
          <li>Evenredige vertegenwoordiging</li>
          <li>Meer dan 15 partijen in de Kamer</li>
          <li>Coalities zijn bijna altijd nodig</li>
        </ul>
      </section>
    </div>
  </main>
</template>

<style scoped>
/* Layout */
#container {
  max-width: 1100px;
  margin: 50px auto;
  padding: 0 20px;
}

h1 {
  font-size: 3em;
  font-weight: bold;
  text-align: center;
}

#subtitle {
  color: var(--grey);
  font-weight: 500;
  font-size: 1.3em;
  text-align: center;
  max-width: 800px;
  margin: 0 auto 30px auto;
}

/* Card styling */
.card {
  border: var(--bordercolor);
  border-radius: 10px;
  padding: 20px;
  margin: 20px 0;
  background: #fff;
  box-shadow: var(--rgb);
}

.card h2 {
  margin-bottom: 10px;
  font-size: 1.5em;
  color: #333;
}

/* Chart styling */
.chart-flex-container {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  width: 100%;
  min-height: 350px; /* chart nooit te klein */
  height: auto;      /* groeit met content */
}

.chart-flex-container .chart {
  flex: 1 1 100%;
  min-height: 300px;
  max-height: 600px;
}

.chart-labels {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.chart-labels .left {
  position: absolute;
  left: 5px;
  top: 50%;
  transform: translateY(-50%);
}
.chart-labels .right {
  position: absolute;
  right: 5px;
  top: 50%;
  transform: translateY(-50%);
}
.chart-labels .top {
  position: absolute;
  top: 5px;
  left: 50%;
  transform: translateX(-50%);
}
.chart-labels .bottom {
  position: absolute;
  bottom: 5px;
  left: 50%;
  transform: translateX(-50%);
}


.blue { background: #2980b9; }
.green { background: #27ae60; }
.red { background: #e74c3c; }


/* Parlement */
.grid-2 {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}
.parlement-box {
  flex: 1;
  border-radius: 12px;
  padding: 15px;
  color: white;
}
.parlement-box.green { background: #6ccf8d; }
.parlement-box.purple { background: #a88ed9; }
.dots {
  display: flex;
  flex-wrap: wrap;
  margin-top: 10px;
  gap: 10px;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.green-dot { background: #388e3c; }
.purple-dot { background: #6a1b9a; }
.blue { background: #1976d2; }
.red { background: #d32f2f; }
.grey { background: #616161; }


/* Bestuursniveaus */
.levels {
  list-style: none;
  padding: 0;
}
.levels li {
  margin: 12px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}
.levels img {
  width: 24px;
  height: 24px;
}

/* Steps */
.steps {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.step {
  background: #f5f5f5;
  padding: 10px 15px;
  border-radius: 8px;
}

/* Calendar */
.calendar {
  list-style: none;
  padding: 0;
}
.calendar li {
  margin: 8px 0;
}
.year {
  padding: 4px 10px;
  border-radius: 8px;
  font-weight: 600;
  margin-right: 10px;
  color: white;
}
.year.grey { background:var(--grey) ; }
.year.green { background: #27ae60; }
</style>
