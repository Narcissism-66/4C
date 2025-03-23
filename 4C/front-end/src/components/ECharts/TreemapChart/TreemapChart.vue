<template>
  <div class="chart-container">
    <div class="header">
      <h3 class="title"></h3>
    </div>
    <div ref="chart" class="chart"></div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch, defineProps, nextTick } from 'vue'
import * as echarts from 'echarts'

const chart = ref(null)
const chartInstance = ref(null)

const props = defineProps({
  rawData: { type: Array, required: true, default: () => [] }
})

// ✅ 构建树形结构
const buildTree = data => {
  if (!data.length) return []

  const lookup = {}
  data.forEach(item => {
    lookup[item.name] = { name: item.name, value: item.value, children: [] }
  })

  let root = null
  data.forEach(item => {
    if (item.parent && lookup[item.parent] && item.name !== item.parent) {
      lookup[item.parent].children.push(lookup[item.name])
    } else if (item.name === item.parent) {
      root = lookup[item.name]
    }
  })

  return root ? [root] : []
}

const updateChart = () => {
  if (!props.rawData.length || !chartInstance.value) return

  const treeData = buildTree(props.rawData)
  if (!treeData.length) return console.warn("⚠️ 数据为空，无法渲染树图")

  chartInstance.value.setOption({
    title: {
      text: '树图示例',
      left: 'center',
      textStyle: { fontSize: 20, color: '#2c3e50' }
    },
    tooltip: { trigger: 'item', formatter: "{b}: {c}" },
    series: [{
      type: 'tree',
      data: treeData,
      top: '10%',
      left: '15%',
      bottom: '10%',
      right: '15%',
      layout: 'orthogonal', // 🟢 "orthogonal" 直角布局
      symbol: 'circle', // 🟢 节点样式（圆形）
      symbolSize: 15, // 🟢 调整节点大小
      itemStyle: {
        borderColor: '#2c3e50', // 节点边框色
        borderWidth: 2,
        shadowColor: 'rgba(0, 0, 0, 0.2)',
        shadowBlur: 10
      },
      lineStyle: { color: '#2c3e50', width: 2 }, // 🟢 连线颜色 + 粗细
      label: {
        show: true,
        position: 'top',
        color: '#2c3e50',
        fontSize: 14
      },
      expandAndCollapse: true,
      initialTreeDepth: 2,
      animationDuration: 800,
      animationDurationUpdate: 800
    }]
  })
}

onMounted(() => {
  nextTick(() => {
    if (!chart.value) {
      console.error("❌ chart 容器未找到")
      return
    }

    setTimeout(() => {
      chartInstance.value = echarts.init(chart.value)
      updateChart()
    }, 200)
  })
})

watch(() => props.rawData, updateChart, { deep: true })
</script>

<style scoped>
.chart-container {
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
  height: 500px; /* 🟢 固定高度，确保居中 */
  display: flex;
  justify-content: center;
  align-items: center;
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
