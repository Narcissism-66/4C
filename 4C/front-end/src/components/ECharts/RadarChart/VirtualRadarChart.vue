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

// 随机生成动态数据
const generateRandomData = () => {
  const categories = ['A', 'B']
  const indicators = ['Speed', 'Accuracy', 'Strength', 'Agility', 'Flexibility', 'Endurance']
  const data = categories.map(category => ({
    category,
    values: indicators.map(indicator => ({
      indicator,
      value: Math.floor(Math.random() * 100) + 50 // 随机生成50到150之间的数值
    }))
  }))
  return data.flatMap(item => item.values.map(value => ({
    category: item.category,
    indicator: value.indicator,
    value: value.value
  })))
}

const updateChart = () => {
  const rawData = generateRandomData()
  console.log('Updated Data:', rawData) // 查看是否每次数据更新

  // 根据 { category, indicator, value } 分组
  const grouped = {}
  const indicatorsSet = new Set()
  rawData.forEach(item => {
    if (!grouped[item.category]) grouped[item.category] = {}
    grouped[item.category][item.indicator] = item.value
    indicatorsSet.add(item.indicator)
  })
  const indicators = Array.from(indicatorsSet)

  // 求每个指标的最大值
  const maxValues = {}
  indicators.forEach(ind => {
    let maxVal = 0
    rawData.forEach(item => {
      if (item.indicator === ind && item.value > maxVal) {
        maxVal = item.value
      }
    })
    maxValues[ind] = maxVal
  })

  const radarIndicators = indicators.map(ind => ({
    name: ind,
    max: maxValues[ind] * 1.1
  }))
  const seriesData = Object.entries(grouped).map(([category, valuesObj]) => ({
    name: category,
    value: indicators.map(ind => valuesObj[ind] || 0),
    itemStyle: {
      color: category === 'A' ? '#3498db' : '#e74c3c', // 用不同的颜色展示 A 和 B
      borderColor: category === 'A' ? '#2980b9' : '#c0392b',
      borderWidth: 3
    },
    areaStyle: {
      color: category === 'A' ? 'rgba(52, 152, 219, 0.4)' : 'rgba(231, 76, 60, 0.4)',
      opacity: 0.6
    }
  }))

  const option = {
    title: {
      text: '雷达图',
      left: 'center',
      textStyle: {
        fontWeight: 'bold',
        fontSize: 24,
        color: '#2c3e50'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}',
      backgroundColor: 'rgba(255, 255, 255, 0.8)',
      borderColor: '#3498db',
      borderWidth: 2,
      textStyle: {
        color: '#3498db'
      }
    },
    radar: {
      indicator: radarIndicators,
      center: ['50%', '60%'],
      radius: '65%',
      axisLine: {
        lineStyle: {
          color: '#2980b9',
          width: 1
        }
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(44, 62, 80, 0.3)'
        }
      },
      axisLabel: {
        textStyle: {
          color: '#2980b9',
          fontSize: 14
        }
      }
    },
    series: seriesData.map(item => ({
      name: item.name,
      type: 'radar',
      data: [item], // 每个类别的数据单独作为一项
      symbol: 'circle',
      symbolSize: 5,
      lineStyle: {
        normal: {
          color: item.itemStyle.color,
          width: 3,
          type: 'solid'
        }
      },
      itemStyle: item.itemStyle,
      areaStyle: item.areaStyle,
      animationDuration: 2000,
      animationEasing: 'cubicOut'
    }))
  }

  chartInstance.value.setOption(option, true) // 强制更新
}

onMounted(() => {
  chartInstance.value = echarts.init(chart.value)
  updateChart()

  // 每1秒更新一次图表数据
  setInterval(() => {
    console.log('Updating Chart...')
    updateChart()
  }, 1000)
})
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
  font-size: 1.8rem;
  font-weight: 600;
}
</style>
