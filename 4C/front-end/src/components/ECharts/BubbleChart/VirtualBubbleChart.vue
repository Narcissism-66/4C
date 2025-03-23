<template>
  <div class="chart-container">
    <div ref="chart" class="chart"></div>
  </div>
</template>

<script setup>
import { onMounted, ref, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'

const chart = ref(null)
const chartInstance = ref(null)
const bubbleData = ref([])

// 🎈 生成初始气泡数据
const generateBubbleData = () => {
  const data = []
  for (let i = 0; i < 40; i++) {
    data.push({
      x: Math.random() * 100,  // 横向随机分布
      y: Math.random() * 10,   // 初始位置较低
      size: Math.random() * 20 + 10, // 气泡大小
      speed: Math.random() * 2 + 0.5, // 上升速度
      opacity: Math.random() * 0.5 + 0.5, // 透明度
      value: Math.round(Math.random() * 100) // 显示的数值
    })
  }
  return data
}

// 初始化数据
bubbleData.value = generateBubbleData()

const updateChart = () => {
  if (!chartInstance.value) return

  // 🎈 更新气泡位置 & 透明度
  bubbleData.value.forEach((bubble) => {
    bubble.y += bubble.speed // 让气泡上升
    bubble.opacity -= 0.02 // 逐渐变透明

    if (bubble.y > 100 || bubble.opacity <= 0) {
      // 气泡消失后重新生成
      bubble.y = 0
      bubble.x = Math.random() * 100
      bubble.size = Math.random() * 20 + 10
      bubble.speed = Math.random() * 2 + 0.5
      bubble.opacity = Math.random() * 0.5 + 0.5
      bubble.value = Math.round(Math.random() * 100)
    }
  })

  // 🌈 渐变色
  const gradientColors = (x, y) =>
    new echarts.graphic.RadialGradient(
      0.4, 0.3, 1,
      [
        { offset: 0, color: `rgba(124, 255, 203, ${y / 100})` },
        { offset: 0.6, color: `rgba(142, 84, 233, ${y / 100})` },
        { offset: 1, color: `rgba(71, 118, 230, ${y / 100})` }
      ]
    )

  // 📊 更新 ECharts 配置
  const option = {
    backgroundColor: 'rgba(240, 248, 255, 0.9)',
    xAxis: { type: 'value', show: false, min: 0, max: 100 },
    yAxis: { type: 'value', show: false, min: 0, max: 100 },
    series: [
      {
        name: '上升气泡',
        type: 'scatter',
        data: bubbleData.value.map(b => [b.x, b.y, b.size, b.opacity, b.value]),
        symbolSize: (data) => data[2] * 1.8, // 气泡大小
        label: {
          show: true,
          position: 'inside',
          formatter: (params) => params.value[4], // 显示数据值
          color: '#fff',
          fontWeight: 'bold',
          fontSize: 14,
          textShadowBlur: 5,
          textShadowColor: 'rgba(0, 0, 0, 0.3)'
        },
        itemStyle: {
          color: (params) => gradientColors(params.value[0], params.value[1]), // 渐变色
          opacity: (params) => params.value[3], // 透明度
          shadowBlur: 15,
          shadowColor: 'rgba(0, 0, 0, 0.1)'
        },
        animationDurationUpdate: 3000,
        animationEasingUpdate: 'linear'
      }
    ]
  }

  chartInstance.value.setOption(option, true)
}

// ⏳ 定时更新，实现动态上升效果
onMounted(() => {
  chartInstance.value = echarts.init(chart.value)
  updateChart()
  setInterval(updateChart, 100) // 100ms 更新一次
})

onBeforeUnmount(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }
})
</script>

<style scoped>
.chart-container {
  position: relative;
  width: 100%;
  height: 100%;
  background: #f8f9fa; /* 白色背景 */
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.chart {
  width: 100%;
  flex-grow: 1;
  transition: all 0.5s ease;
}
</style>
