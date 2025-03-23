<script setup>
import { onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import 'echarts-wordcloud'

const WordCloudData = ref({
  datas: [
    { name: "Vue", value: 100 },
    { name: "React", value: 90 },
    { name: "Angular", value: 80 },
    { name: "JavaScript", value: 70 },
    { name: "TypeScript", value: 60 },
    { name: "Node.js", value: 50 },
    { name: "Python", value: 40 },
    { name: "Django", value: 30 },
    { name: "Flask", value: 20 },
    { name: "HTML5", value: 10 }
  ]
})

// 定义值范围
const valueRange = [0, 200];

// 随机生成 RGB 颜色
const randomRGB = () => {
  const r = Math.floor(Math.random() * 255)
  const g = Math.floor(Math.random() * 255)
  const b = Math.floor(Math.random() * 255)
  return `rgb(${r},${g},${b})`
}

const target = ref()

// 更新数据的函数
const updateData = async () => {
  try {
    WordCloudData.value.datas = WordCloudData.value.datas.map((region) => ({
      ...region,
      value: Math.floor(Math.random() * (valueRange[1] - valueRange[0] + 1)) + valueRange[0],
    }));
    renderChart(); // 手动调用 renderChart
  } catch (error) {
    console.error("Error updating data:", error);
  }
};

const renderChart = () => {
  if (target.value) {
    let mChart = echarts.getInstanceByDom(target.value); // 获取已有的 echarts 实例
    if (!mChart) {
      mChart = echarts.init(target.value); // 如果没有实例，则初始化
    }
    const options = {
      series: [
        {
          type: 'wordCloud',
          sizeRange: [40, 100],
          rotationRange: [0, 0],
          gridSize: 0,
          layoutAnimation: true,
          textStyle: {
            color: () => randomRGB() // 使用随机颜色
          },
          emphasis: {
            textStyle: {
              fontWeight: 'bold',
              color: '#000'
            }
          },
          data: WordCloudData.value.datas
        }
      ]
    }
    mChart.setOption(options)
  }
}

onMounted(() => {
  renderChart(); // 初次渲染
  setInterval(updateData, 2000); // 每两秒调用一次 updateData 函数
});

watch(
  () => WordCloudData.value.datas, // 监听数据变化
  () => {
    renderChart(); // 数据变化时重新渲染图表
  },
  { deep: true }
);
</script>

<template>
  <div class="chart-container">
    <div ref="target" class="chart"></div>
  </div>
</template>

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
