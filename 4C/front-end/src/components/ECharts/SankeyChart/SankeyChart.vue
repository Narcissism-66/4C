<template>
  <div class="chart-container">
    <div class="header">
      <h3 class="title">桑基图</h3>
    </div>
    <div ref="chart" class="chart"></div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch, defineProps } from 'vue'
import * as echarts from 'echarts'

const chart = ref(null)
const chartInstance = ref(null)

const props = defineProps({
  rawData: { type: Array, required: true, default: () => [] }
})

const updateChart = () => {
  if (!props.rawData.length) return
  const nodesSet = new Set()
  const links = props.rawData.map(item => {
    nodesSet.add(item.source)
    nodesSet.add(item.target)
    return { source: item.source, target: item.target, value: item.value }
  })
  const nodes = Array.from(nodesSet).map(name => ({ name }))
  const option = {
    title: {
      text: '桑基图',
      left: 'center',
      textStyle: { fontWeight: 'bold', fontSize: 20, color: '#2c3e50' }
    },
    tooltip: { trigger: 'item', triggerOn: 'mousemove' },
    series: {
      type: 'sankey',
      layout: 'none',
      data: nodes,
      links: links,
      focusNodeAdjacency: true,
      itemStyle: { borderWidth: 1, borderColor: '#aaa' },
      lineStyle: { color: 'source', curveness: 0.5 }
    }
  }
  chartInstance.value.setOption(option)
}

onMounted(() => {
  chartInstance.value = echarts.init(chart.value)
  updateChart()
})

watch(() => props.rawData, () => {
  updateChart()
}, { deep: true })
</script>

<style scoped>
.chart-container {
  position: relative;
  width: 100%;
  height: 100%;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
}

.chart {
  width: 100%;
  flex-grow: 1;
}

.header {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 1rem;
}

.title {
  margin: 0;
  color: #2c3e50;
  font-size: 1.4rem;
  font-weight: 600;
}
</style>
