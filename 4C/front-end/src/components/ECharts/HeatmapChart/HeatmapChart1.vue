<template>
  <div ref="chart" class="chart" @contextmenu.prevent="openDialog"></div>

  <!-- 配置弹窗 -->
  <div v-if="dialogVisible" class="modal-overlay">
    <div class="modal">
      <div class="modal-header">
        <h3>图表配置</h3>
        <span class="close-btn" @click="closeDialog">×</span>
      </div>

      <div class="config-columns">
        <!-- 左侧配置 -->
        <div class="config-left">
          <div class="config-section">
            <h4>全局设置</h4>
            <div class="input-group">
              <label>图表标题</label>
              <input v-model="chartOptions.title.text" placeholder="请输入标题" />
            </div>
            <div class="input-group">
              <label>标题颜色</label>
              <div class="color-picker">
                <input type="color" v-model="chartOptions.title.textStyle.color" />
                <span class="color-preview" :style="{ backgroundColor: chartOptions.title.textStyle.color }"></span>
              </div>
            </div>
            <div class="input-group">
              <label>标题位置</label>
              <select v-model="chartOptions.title.left">
                <option value="left">左侧</option>
                <option value="center">居中</option>
                <option value="right">右侧</option>
              </select>
            </div>
          </div>

          <div class="config-section">
            <h4>热力色设置</h4>
            <div class="color-range">
              <div class="color-item" v-for="(color, index) in chartOptions.visualMap.inRange.color" :key="index">
                <input type="color" v-model="chartOptions.visualMap.inRange.color[index]" />
                <span class="color-preview" :style="{ backgroundColor: color }"></span>
                <button v-if="index > 0" @click="removeColor(index)" class="btn-remove">×</button>
              </div>
              <button @click="addColor" class="btn-add">+ 添加颜色</button>
            </div>
          </div>
        </div>

        <!-- 右侧配置 -->
        <div class="config-right">
          <div class="config-section">
            <h4>X轴设置</h4>
            <div class="input-group">
              <label>轴名称</label>
              <input v-model="chartOptions.xAxis.name" />
            </div>
            <div class="input-group">
              <label>文字颜色</label>
              <div class="color-picker">
                <input type="color" v-model="chartOptions.xAxis.axisLabel.color" />
                <span class="color-preview" :style="{ backgroundColor: chartOptions.xAxis.axisLabel.color }"></span>
              </div>
            </div>
            <div class="input-group">
              <label>轴线颜色</label>
              <div class="color-picker">
                <input type="color" v-model="chartOptions.xAxis.axisLine.lineStyle.color" />
                <span class="color-preview" :style="{ backgroundColor: chartOptions.xAxis.axisLine.lineStyle.color }"></span>
              </div>
            </div>
          </div>

          <div class="config-section">
            <h4>Y轴设置</h4>
            <div class="input-group">
              <label>轴名称</label>
              <input v-model="chartOptions.yAxis.name" />
            </div>
            <div class="input-group">
              <label>文字颜色</label>
              <div class="color-picker">
                <input type="color" v-model="chartOptions.yAxis.axisLabel.color" />
                <span class="color-preview" :style="{ backgroundColor: chartOptions.yAxis.axisLabel.color }"></span>
              </div>
            </div>
            <div class="input-group">
              <label>轴线颜色</label>
              <div class="color-picker">
                <input type="color" v-model="chartOptions.yAxis.axisLine.lineStyle.color" />
                <span class="color-preview" :style="{ backgroundColor: chartOptions.yAxis.axisLine.lineStyle.color }"></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-save" @click="closeDialog">保存配置</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount, reactive } from 'vue'
import * as echarts from 'echarts'
import { userUserStore } from '@/stores/UserStore.js'

const chart = ref(null)
let chartInstance = null
const dialogVisible = ref(false)

// 获取当前用户信息，并使用 userStore.user.id 构造存储键
const userStore = userUserStore()
const storageKey = `HeatmapChartConfig-${userStore.user.id}`

// 默认配置选项
const chartOptions = reactive({
  title: {
    text: '热力图分析',
    textStyle: {
      color: '#333',
      fontSize: 18
    },
    left: 'center'
  },
  visualMap: {
    inRange: {
      color: ['#fde68a', '#f59e0b', '#ef4444']
    }
  },
  xAxis: {
    name: 'X轴',
    type: 'category',
    axisLabel: {
      color: '#374151'
    },
    axisLine: {
      lineStyle: {
        color: '#6B7280'
      }
    }
  },
  yAxis: {
    name: 'Y轴',
    type: 'category',
    axisLabel: {
      color: '#374151'
    },
    axisLine: {
      lineStyle: {
        color: '#6B7280'
      }
    }
  }
})

const props = defineProps({
  rawData: {
    type: Array,
    required: true
  }
})

// 右键打开弹窗
const openDialog = () => {
  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
  updateChart()
  saveConfig()
}

// 颜色操作
const addColor = () => {
  chartOptions.visualMap.inRange.color.push('#000000')
}
const removeColor = (index) => {
  chartOptions.visualMap.inRange.color.splice(index, 1)
}

// 初始化图表
const initChart = () => {
  if (!chart.value) return
  chartInstance = echarts.init(chart.value)
  window.addEventListener('resize', handleResize)
}

// 更新图表配置
const updateChart = () => {
  if (!chartInstance || props.rawData.length === 0) return

  const xAxisData = [...new Set(props.rawData.map(item => item.x))].sort()
  const yAxisData = [...new Set(props.rawData.map(item => item.y))].sort()
  const maxValue = Math.max(...props.rawData.map(item => item.value))

  const baseOption = {
    tooltip: {
      position: 'top',
      formatter: params => `${params.value[0]} × ${params.value[1]}<br/>值: ${params.value[2]}`
    },
    grid: {
      top: 40,
      left: 10,
      right: 50,
      bottom: 30,
      containLabel: true
    },
    series: [{
      type: 'heatmap',
      data: props.rawData.map(item => [item.x, item.y, item.value]),
      itemStyle: {
        borderRadius: 2
      }
    }]
  }

  chartInstance.setOption({
    ...baseOption,
    ...chartOptions,
    visualMap: {
      ...chartOptions.visualMap,
      min: 0,
      max: maxValue,
      calculable: true,
      orient: 'vertical',
      right: 20,
      top: 'center'
    },
    xAxis: {
      ...chartOptions.xAxis,
      data: xAxisData
    },
    yAxis: {
      ...chartOptions.yAxis,
      data: yAxisData
    }
  })
}

const handleResize = () => chartInstance?.resize()

const saveConfig = () => {
  localStorage.setItem(storageKey, JSON.stringify(chartOptions))
}

const loadConfig = () => {
  const configStr = localStorage.getItem(storageKey)
  if (configStr) {
    try {
      const config = JSON.parse(configStr)
      Object.assign(chartOptions, config)
    } catch (error) {
      console.error("加载图表配置失败", error)
    }
  }
}


onMounted(() => {
  initChart()
  loadConfig()
  updateChart()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})

watch(() => props.rawData, updateChart, { deep: true })
</script>

<style scoped>
.chart {
  width: 100%;
  height: 100%;
  cursor: pointer;
}

/* 弹窗遮罩 */
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

/* 弹窗主体（调整为第二个模板的大小） */
.modal {
  position: relative;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 30px rgba(0, 0, 0, 0.2);
  padding: 30px;
  width: 600px;
  max-width: 90%;
  animation: fadeInScale 0.3s ease-in-out;
}

/* 弹窗头部 */
.modal-header {
  position: relative;
  margin-bottom: 20px;
  text-align: center;
}

/* 关闭按钮：绝对定位在右上角 */
.close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

/* 配置两列 */
.config-columns {
  display: flex;
  gap: 30px;
  margin: 20px 0;
}
.config-left, .config-right {
  flex: 1;
}
.config-section {
  margin-bottom: 24px;
}
.config-section h4 {
  margin-bottom: 16px;
  color: #2c3e50;
  font-size: 16px;
}
.input-group {
  margin-bottom: 16px;
}
.input-group label {
  display: block;
  margin-bottom: 6px;
  color: #4a5568;
  font-size: 14px;
}
.input-group input,
.input-group select {
  width: 100%;
  padding: 8px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 14px;
}
.color-picker {
  display: flex;
  align-items: center;
  gap: 8px;
}
.color-preview {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  border: 1px solid #ddd;
}
.color-range {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.color-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.btn-add, .btn-remove {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.btn-add {
  background: #f0f4f8;
  color: #4a5568;
}
.btn-remove {
  background: #fee2e2;
  color: #dc2626;
}
.modal-footer {
  text-align: right;
  margin-top: 20px;
}
.btn-save {
  background: #3b82f6;
  color: white;
  padding: 10px 24px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}
.btn-save:hover {
  background: #2563eb;
}

@keyframes fadeInScale {
  0% {
    opacity: 0;
    transform: scale(0.8);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
