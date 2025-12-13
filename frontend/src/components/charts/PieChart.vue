<script setup lang="ts">
/**
 * @author Dannique Klaver
 */

import {computed} from 'vue'
import {use} from 'echarts/core'
import {PieChart} from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components'
import {LabelLayout} from 'echarts/features'
import {CanvasRenderer} from 'echarts/renderers'
import VChart from 'vue-echarts'

use([
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  PieChart,
  CanvasRenderer,
  LabelLayout
])

interface VoteData {
  party: string
  percentage: number
  color?: string
}

interface ChartProps {
  title?: string;
  data: VoteData[];
}

const props = withDefaults(defineProps<ChartProps>(), {
  title: 'Percentage stemmen per partij'
})

/**
 * Generates the complete ECharts configuration object
 * Updates automatically when data changes
 * @returns {Object} ECharts option object with all chart settings
 */
const chartOption = computed(() => ({
  title: {
    text: props.title,
    left: 'center'
  },
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c}% ({d}%)'
  },
  legend: {
    orient: 'horizontal',
    bottom: 0,
    type: 'scroll'
  },
  series: [
    {
      name: 'Stempercentage',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: props.data.map(item => ({
        value: item.percentage,
        name: item.party,
        itemStyle: item.color ? {color: item.color} : undefined
      }))
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
</style>
