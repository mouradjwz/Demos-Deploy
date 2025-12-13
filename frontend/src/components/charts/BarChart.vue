<script setup lang="ts">

/**
 * @author Dannique Klaver
 */
import {ref, watch, computed} from 'vue'
import {use} from 'echarts/core'
import {BarChart} from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'
import VChart from 'vue-echarts'

use([
  BarChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  CanvasRenderer
])

interface PartySeats {
  party: string;
  zetels: number;
  color?: string;
}

interface ChartProps {
  title?: string;
  data?: PartySeats[];
}

const props = withDefaults(defineProps<ChartProps>(), {
  title: 'Bar Chart',
  data: () => [
    // will be filled when creating one
  ]
})

/**
 * Generates the complete ECharts configuration object
 * Updates automatically when data changes
 * @returns {Object} ECharts option object with all chart settings
 */
const chartOption = computed(() => ({
  title: {
    text: props.title
  },
  tooltip: {
    trigger: 'axis',
    formatter: '{b}: {c} zetels'
  },
  xAxis: {
    type: 'category',
    data: props.data.map(item => item.party),
    axisLabel: {
      rotate: 45,
      interval: 0
    }
  },
  yAxis: {
    type: 'value',
    name: 'Zetels'
  },
  series: [
    {
      data: props.data.map((item) => ({
        value: item.zetels,
        itemStyle: {
          color: item.color
        }
      })),
      type: 'bar',
      name: 'Zetelverdeling',
      showBackground: true,
      backgroundStyle: {
        color: 'rgba(180, 180, 180, 0.2)'
      }
    }
  ]
}))

</script>

<template>
  <div class="chart-container">
    <v-chart class="chart" :option="chartOption" autoresize/>
  </div>
</template>

<style scoped>

.chart {
  height: 400px;
  width: 100%;
}

select {
  padding: 5px;
  border-radius: 4px;
}
</style>
