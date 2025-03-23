<template>
  <div class="chart-container" @contextmenu.prevent="openDialog">
    <div ref="chart" class="chart"></div>
  </div>
  <!-- 配置弹窗 -->
  <div v-if="dialogVisible" class="modal-overlay">
    <div class="modal">
      <div class="modal-header">
        <h3>配置词云图属性</h3>
        <span class="close-btn" @click="closeDialog">×</span>
      </div>
      <div class="modal-content">
        <div class="config-columns">
          <!-- 左侧：标题配置 -->
          <div class="config-left">
            <div class="input-group">
              <label for="chartTitle">图表标题：</label>
              <input id="chartTitle" v-model="chartTitle" placeholder="请输入图表标题" />
            </div>
            <div class="input-group">
              <label for="titlePosition">标题位置：</label>
              <select id="titlePosition" v-model="titlePosition">
                <option value="left">左对齐</option>
                <option value="center">居中</option>
                <option value="right">右对齐</option>
              </select>
            </div>
            <div class="input-group">
              <label for="titleColor">标题颜色：</label>
              <input id="titleColor" v-model="titleColor" type="color" />
            </div>
          </div>
          <!-- 右侧：其他配置 -->
          <div class="config-right">
            <div class="input-group">
              <label for="shape">词云形状：</label>
              <select id="shape" v-model="shape">
                <option value="circle">圆形</option>
                <option value="cardioid">心形</option>
                <option value="diamond">菱形</option>
                <option value="triangle-forward">三角形</option>
                <option value="triangle">等边三角形</option>
                <option value="pentagon">五边形</option>
                <option value="star">星形</option>
              </select>
            </div>
            <div class="input-group">
              <label for="minSize">最小字号：</label>
              <input id="minSize" v-model.number="minSize" type="number" min="1" />
            </div>
            <div class="input-group">
              <label for="maxSize">最大字号：</label>
              <input id="maxSize" v-model.number="maxSize" type="number" min="1" />
            </div>
            <div class="input-group">
              <label for="minRotation">最小旋转角度：</label>
              <input id="minRotation" v-model.number="minRotation" type="number" />
            </div>
            <div class="input-group">
              <label for="maxRotation">最大旋转角度：</label>
              <input id="maxRotation" v-model.number="maxRotation" type="number" />
            </div>
          </div>
        </div>
      </div>
      <button @click="closeDialog">确定</button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch, defineProps } from 'vue'
import * as echarts from 'echarts'
import 'echarts-wordcloud'
import { userUserStore } from '@/stores/UserStore.js'

// 接收父组件传入的数据，格式示例：[{ name: '关键词1', value: 100 }, …]
const props = defineProps({
  rawData: {
    type: Array,
    required: true
  }
})
console.log(props.rawData)

// 配置弹窗显示状态及相关属性
const dialogVisible = ref(false)
const chartTitle = ref('词云图')
const titlePosition = ref('center')
const titleColor = ref('#333333')
const shape = ref('circle')
const minSize = ref(20)
const maxSize = ref(70)
const minRotation = ref(-45)
const maxRotation = ref(45)

// 定义存储 key（如需结合用户信息，可扩展）
const userStore = userUserStore();
const storageKey = `WordCloudChartConfig-${userStore.user.id}`

// 随机生成 RGB 颜色（用于文本颜色）
const randomColor = () => {
  return 'rgb(' + [
    Math.round(Math.random() * 160),
    Math.round(Math.random() * 160),
    Math.round(Math.random() * 160)
  ].join(',') + ')'
}

// 右键打开弹窗
const openDialog = () => {
  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
  updateChart()
  saveWordCloudConfigLocally()
}

// 保存配置到 localStorage
const saveWordCloudConfigLocally = () => {
  const config = {
    chartTitle: chartTitle.value,
    titlePosition: titlePosition.value,
    titleColor: titleColor.value,
    shape: shape.value,
    minSize: minSize.value,
    maxSize: maxSize.value,
    minRotation: minRotation.value,
    maxRotation: maxRotation.value
  }
  localStorage.setItem(storageKey, JSON.stringify(config))
}

// 从 localStorage 加载配置
const loadWordCloudConfigLocally = () => {
  const configStr = localStorage.getItem(storageKey)
  if (configStr) {
    try {
      const config = JSON.parse(configStr)
      chartTitle.value = config.chartTitle || chartTitle.value
      titlePosition.value = config.titlePosition || titlePosition.value
      titleColor.value = config.titleColor || titleColor.value
      shape.value = config.shape || shape.value
      minSize.value = config.minSize || minSize.value
      maxSize.value = config.maxSize || maxSize.value
      minRotation.value = config.minRotation || minRotation.value
      maxRotation.value = config.maxRotation || maxRotation.value
    } catch (error) {
      console.error("加载词云图配置失败", error)
    }
  }
}

// 更新图表配置
const chart = ref(null)
const updateChart = () => {
  const chartInstance = echarts.init(chart.value)
  const option = {
    title: {
      text: chartTitle.value,
      left: titlePosition.value,
      textStyle: {
        color: titleColor.value,
        fontSize: 18,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      show: true,
      formatter: params => `${params.name}: ${params.value}`
    },
    series: [
      {
        type: 'wordCloud',
        shape: shape.value,
        left: 'center',
        top: 'center',
        width: '100%',
        height: '100%',
        sizeRange: [minSize.value, maxSize.value],
        rotationRange: [minRotation.value, maxRotation.value],
        drawOutOfBound: false,
        textStyle: {
          color: () => randomColor()
        },
        data: props.rawData
      }
    ]
  }
  chartInstance.setOption(option)
}

onMounted(() => {
  loadWordCloudConfigLocally()
  updateChart()
})
watch(() => props.rawData, updateChart, { deep: true })
</script>

<style scoped>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
}
.chart-container {
  width: 100%;
  height: 100%;
}
.chart {
  width: 100%;
  height: 100%;
  cursor: pointer;
}
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 30px rgba(0, 0, 0, 0.2);
  padding: 30px;
  width: 600px;
  max-width: 90%;
  animation: fadeInScale 0.3s ease-in-out;
}
.modal-header {
  position: relative;
  margin-bottom: 20px;
  text-align: center;
}
.close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}
.close-btn:hover {
  color: #333;
}
.config-columns {
  display: flex;
  gap: 30px;
  margin: 20px 0;
}
.config-left,
.config-right {
  flex: 1;
}
.input-group {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
}
.input-group label {
  font-weight: bold;
  margin-bottom: 5px;
  color: #555;
}
.input-group input,
.input-group select {
  border: 1px solid #ccc;
  border-radius: 4px;
  padding: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s;
}
.input-group input:focus,
.input-group select:focus {
  border-color: #66afe9;
}
.color-input {
  display: flex;
  align-items: center;
  gap: 10px;
}
.color-preview {
  display: inline-block;
  width: 24px;
  height: 24px;
  border: 1px solid #ccc;
  margin-left: 10px;
  vertical-align: middle;
  text-align: center;
  line-height: 24px;
  font-size: 12px;
  color: #fff;
}
@keyframes fadeInScale {
  0% { opacity: 0; transform: scale(0.8); }
  100% { opacity: 1; transform: scale(1); }
}
button {
  background-color: #5470c6;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 10px;
  width: 100%;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-top: 20px;
}
button:hover {
  background-color: #3d5a9a;
}
</style>
