<template>
  <div class="chart-container">
    <div ref="chart" class="chart"></div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import * as echarts from 'echarts'

// 动态生成数据
const generateRandomData = () => {
  const sources = ['A', 'B', 'C', 'D', 'E']
  const targets = ['X', 'Y', 'Z', 'W', 'V']
  const data = []

  // 随机生成连接数据
  sources.forEach(source => {
    targets.forEach(target => {
      data.push({
        source,
        target,
        value: Math.floor(Math.random() * 100) + 1
      })
    })
  })

  return data
}

const chart = ref(null)
const chartInstance = ref(null)

const updateChart = () => {
  const rawData = generateRandomData()
  const nodesSet = new Set()
  const links = rawData.map(item => {
    nodesSet.add(item.source)
    nodesSet.add(item.target)
    return { source: item.source, target: item.target, value: item.value }
  })
  const nodes = Array.from(nodesSet).map(name => ({ name }))

  const option = {
    title: {
      text: '动态桑基图',
      left: 'center',
      textStyle: { fontWeight: 'bold', fontSize: 24, color: '#ecf0f1' }
    },
    tooltip: { trigger: 'item', triggerOn: 'mousemove' },
    series: {
      type: 'sankey',
      layout: 'none',
      data: nodes,
      links: links,
      focusNodeAdjacency: true,
      itemStyle: {
        borderWidth: 2,
        borderColor: '#2c3e50',
        color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
          { offset: 0, color: '#2980b9' },
          { offset: 1, color: '#f39c12' }
        ]) // 颜色渐变（蓝到金）
      },
      lineStyle: {
        color: 'source',
        curveness: 0.5,
        opacity: 0.8,
        width: 5,
        type: 'solid',
        shadowColor: 'rgba(0, 0, 0, 0.3)',
        shadowBlur: 10,
        shadowOffsetX: 6,
        shadowOffsetY: 6,
        // 动感渐变线条
        lineStyle: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
          { offset: 0, color: '#e74c3c' },
          { offset: 1, color: '#9b59b6' }
        ])
      },
      emphasis: {
        lineStyle: {
          color: '#e74c3c',
          width: 6
        },
        itemStyle: {
          color: '#f1c40f'  // 强调部分为黄色
        }
      },
      animationDuration: 1200, // 提升动画流畅度
      animationEasing: 'elasticOut',
      animationDelayUpdate: (idx) => idx * 100, // 延迟逐个加载效果
    }
  }
  chartInstance.value.setOption(option)
}

onMounted(() => {
  chartInstance.value = echarts.init(chart.value)
  updateChart()

  // 每3秒生成新的数据并更新图表
  setInterval(() => {
    updateChart()
  }, 3000)
})
</script>

<style scoped>
.chart-container {
  position: relative;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #2980b9, #8e44ad); /* 更动感的背景渐变 */
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
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
  background: linear-gradient(135deg, #2980b9, #f39c12); /* 更加活力的标题背景 */
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

.title {
  margin: 0;
  color: white;
  font-size: 1.8rem; /* 稍微增大字体 */
  font-weight: 700;
}
</style>
