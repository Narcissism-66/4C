<template>
  <div class="chart-container">
    <div ref="chart" class="chart"></div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import 'echarts-wordcloud' // 引入词云扩展

const chart = ref(null)

const props = defineProps({
  // 传入的数据格式示例：[{ name: '关键词1', value: 100 }, { name: '关键词2', value: 80 }, …]
  rawData: {
    type: Array,
    required: true,
  },
})

const randomColor = () => {
  // 随机生成 RGB 颜色
  return 'rgb(' + [
    Math.round(Math.random() * 160),
    Math.round(Math.random() * 160),
    Math.round(Math.random() * 160)
  ].join(',') + ')'
}

const updateChart = () => {
  const chartInstance = echarts.init(chart.value)

  const option = {
    tooltip: {
      show: true,
      formatter: (params) => {
        return `${params.name}: ${params.value}`
      },
    },
    series: [
      {
        type: 'wordCloud',
        shape: 'circle',
        left: 'center',
        top: 'center',
        width: '100%',
        height: '100%',
        sizeRange: [50, 100],
        rotationRange: [-45, 45],
        gridSize: 8,
        drawOutOfBound: false,
        textStyle: {
          color: () => randomColor() // 使用随机颜色
        },
        data: props.rawData
      }
    ]
  }

  chartInstance.setOption(option)
}

onMounted(() => {
  updateChart()
})

watch(
  () => props.rawData,
  () => {
    updateChart()
  },
  { deep: true }
)
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 100%;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.chart {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
}
</style>
