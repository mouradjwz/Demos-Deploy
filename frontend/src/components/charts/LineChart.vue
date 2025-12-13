<script setup lang="ts">
/**
 * @author Dannique Klaver
 */

import {ref, watch} from 'vue'
import {use} from 'echarts/core'
import {LineChart} from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DataZoomComponent,
  LegendComponent
} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'
import VChart from 'vue-echarts'

use([
  LineChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DataZoomComponent,
  LegendComponent,
  CanvasRenderer
])

interface DataPoint {
  date: string;
  value: number;
}

interface ChartProps {
  title?: string;
  data?: DataPoint[];
}

const props = withDefaults(defineProps<ChartProps>(), {
  title: 'Line Chart',
  data: () => [
    // will be filled when creating one
  ]
})

/**
 * Generates the complete ECharts configuration object
 * Updates automatically when data changes
 * @returns {Object} ECharts option object with all chart settings
 */
const chartOption = ref({
  title: {
    text: props.title
  },
  tooltip: {
    trigger: 'axis',
    formatter: '{b}: {c}'
  },
  xAxis: {
    type: 'category',
    data: props.data.map(item => item.date),
    axisLabel: {
      formatter: (value: string) => {
        const date = new Date(value);
        return `${date.getDate()}/${date.getMonth() + 1}`;
      }
    }
  },
  yAxis: {
    type: 'value'
  },
  dataZoom: [
    {
      type: 'inside',
      start: 0,
      end: 100
    },
    {
      start: 0,
      end: 100
    }
  ],
  series: [
    {
      data: props.data.map(item => item.value),
      type: 'line',
      name: 'Value',
      smooth: true
    }
  ]
})
</script>

<template>
  <div class="chart-container">
    <v-chart class="chart" :option="chartOption" autoresize/>
  </div>
</template>

<style scoped>
.chart-container {
  margin: 1rem 0;
  border: 1px solid #625555;
  border-radius: 15px;
  padding: 15px;
  background-color: white;
}

.chart {
  height: 400px;
  width: 100%;
}
</style>
