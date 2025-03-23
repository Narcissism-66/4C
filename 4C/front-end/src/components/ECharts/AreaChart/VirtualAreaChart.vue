<template>
  <div class="chart-container">
    <div ref="chart" class="chart"></div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import * as echarts from 'echarts'

const chart = ref(null)
const chartInstance = ref(null)
const showDialog = ref(false)
const xAxisName = ref('')
const yAxisName = ref('')
const showAxisSettings = true

// 生成随机数据
const generateRandomData = () => {
  const dates = []
  const values = []
  const currentDate = new Date()
  for (let i = 0; i < 10; i++) {
    const randomDate = new Date(currentDate)
    randomDate.setDate(currentDate.getDate() - i)
    dates.push(randomDate.toISOString().split('T')[0])  // 日期格式化为 YYYY-MM-DD
    values.push(Math.floor(Math.random() * 100) + 1)  // 随机值生成
  }
  return { dates, values }
}

const updateChart = () => {
  const { dates, values } = generateRandomData()

  const option = {
    title: {
      text: '面积图',
      left: 'center',
      textStyle: { fontWeight: 'bold', fontSize: 20, color: '#2c3e50', fontFamily: 'STKaiti' }
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(52,152,219,0.9)',
      textStyle: { color: '#fff', fontSize: 14 },
      borderWidth: 0,
      borderRadius: 6
    },
    xAxis: {
      type: 'category',
      name: xAxisName.value,
      nameTextStyle: { fontWeight: 'bold', fontSize: 14, color: '#34495e' },
      data: dates,
      axisLabel: { color: '#7f8c8d', fontSize: 12 },
      axisLine: { lineStyle: { color: '#2c3e50', width: 2 } }
    },
    yAxis: {
      type: 'value',
      name: yAxisName.value,
      nameTextStyle: { fontWeight: 'bold', fontSize: 14, color: '#34495e' },
      axisLabel: { color: '#7f8c8d', fontSize: 12 },
      axisLine: { lineStyle: { color: '#2c3e50', width: 2 } },
      splitLine: { lineStyle: { color: '#ecf0f1' } }
    },
    series: [{
      name: '数值',
      type: 'line',
      data: values,
      smooth: true,
      lineStyle: { color: '#e74c3c', width: 3, shadowBlur: 10, shadowColor: 'rgba(231,76,60,0.5)' },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(231,76,60,0.8)' },
          { offset: 1, color: 'rgba(231,76,60,0)' }
        ]) },
      itemStyle: { color: '#e74c3c' }
    }]
  }
  chartInstance.value.setOption(option)
}

const applyAxisNames = () => {
  showDialog.value = false
  updateChart()
}

onMounted(() => {
  chartInstance.value = echarts.init(chart.value)
  updateChart()

  // 每隔 3 秒生成新的随机数据
  setInterval(() => {
    updateChart()
  }, 1000)
})
</script>

<style scoped>
.chart-container {
  position: relative;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.chart {
  width: 100%;
  flex-grow: 1;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
}

.title {
  font-size: 1.6rem;
  font-weight: bold;
  color: #2c3e50;
}

.settings-btn {
  padding: 8px 16px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s;
}

.settings-btn:hover {
  background: #2980b9;
  transform: translateY(-2px);
  box-shadow: 0 3px 8px rgba(52, 152, 219, 0.4);
}

.modal-mask {
  position: fixed;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-container {
  background: white;
  padding: 25px;
  border-radius: 12px;
  width: 350px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}
</style>
