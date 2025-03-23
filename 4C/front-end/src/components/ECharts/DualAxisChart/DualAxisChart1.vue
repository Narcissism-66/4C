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
        <!-- 左侧配置：全局设置 & 图表颜色 -->
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
            <h4>图表颜色</h4>
            <div class="input-group">
              <label>数据1颜色</label>
              <div class="color-picker">
                <input type="color" v-model="chartOptions.series[0].itemStyle.color" />
                <span class="color-preview" :style="{ backgroundColor: chartOptions.series[0].itemStyle.color }"></span>
              </div>
            </div>
            <div class="input-group">
              <label>数据2颜色</label>
              <div class="color-picker">
                <input type="color" v-model="chartOptions.series[1].lineStyle.color" />
                <span class="color-preview" :style="{ backgroundColor: chartOptions.series[1].lineStyle.color }"></span>
              </div>
            </div>
          </div>

          <div class="config-section">
            <h4>图例设置</h4>
            <div class="input-group">
              <label>显示图例</label>
              <select v-model="chartOptions.legend.show">
                <option :value="true">显示</option>
                <option :value="false">隐藏</option>
              </select>
            </div>
            <div class="input-group" v-if="chartOptions.legend.show">
              <label>图例位置</label>
              <select v-model="chartOptions.legend.left">
                <option value="left">左侧</option>
                <option value="center">居中</option>
                <option value="right">右侧</option>
              </select>
            </div>
            <div class="input-group" v-if="chartOptions.legend.show">
              <label>图例文字颜色</label>
              <div class="color-picker">
                <input type="color" v-model="chartOptions.legend.textStyle.color" />
                <span class="color-preview" :style="{ backgroundColor: chartOptions.legend.textStyle.color }"></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧配置：坐标轴设置 -->
        <div class="config-right">
          <div class="config-section">
            <h4>X轴设置</h4>
            <div class="input-group">
              <label>轴名称</label>
              <input v-model="chartOptions.xAxis.name" placeholder="请输入X轴名称" />
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
            <!-- 注意这里使用数组方式绑定 yAxis 配置 -->
            <div class="input-group">
              <label>轴名称</label>
              <input v-model="chartOptions.yAxis[0].name" placeholder="请输入Y轴名称" />
            </div>
            <div class="input-group">
              <label>文字颜色</label>
              <div class="color-picker">
                <input type="color" v-model="chartOptions.yAxis[0].axisLabel.color" />
                <span class="color-preview" :style="{ backgroundColor: chartOptions.yAxis[0].axisLabel.color }"></span>
              </div>
            </div>
            <div class="input-group">
              <label>轴线颜色</label>
              <div class="color-picker">
                <input type="color" v-model="chartOptions.yAxis[0].axisLine.lineStyle.color" />
                <span class="color-preview" :style="{ backgroundColor: chartOptions.yAxis[0].axisLine.lineStyle.color }"></span>
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
import { ref, reactive, onMounted, onBeforeUnmount, watch, defineProps } from 'vue'
import * as echarts from 'echarts'
import { userUserStore } from '@/stores/UserStore.js'

// 获取当前用户信息，并使用 userStore.user.id 构造存储键
const userStore = userUserStore()
const storageKey = `DualChartConfig-${userStore.user.id}`

const chart = ref(null)
let chartInstance = null
const dialogVisible = ref(false)

// 默认图表配置（注意：yAxis 采用数组格式，legend 也在此处配置）
const chartOptions = reactive({
  title: {
    text: '双轴图',
    textStyle: {
      color: '#333',
      fontSize: 18
    },
    left: 'left'
  },
  legend: {
    show: true,
    data: ['柱状', '折线'],
    left: 'center',
    textStyle: {
      color: '#333'
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
    },
    data: []
  },
  yAxis: [
    {
      name: 'Y轴',
      type: 'value',
      axisLabel: {
        color: '#374151'
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: '#e0e0e0', // 设置 Y 轴网格线颜色为绿色
          width: 1,
          type: 'solid'
        }
      },
      axisLine: {
        lineStyle: {
          color: '#6B7280'
        }
      },
    }
  ],
  series: [
    {
      name: '柱状',
      type: 'bar',
      data: [],
      itemStyle: {
        color: '#2ecc71'
      }
    },
    {
      name: '折线',
      type: 'line',
      yAxisIndex: 0,
      data: [],
      lineStyle: {
        color: '#e67e22',
        width: 3
      },
      itemStyle: {
        color: '#e67e22'
      }
    }
  ]
})

const props = defineProps({
  rawData: {
    type: Array,
    required: true,
    default: () => []
  }
})

// 打开配置弹窗
const openDialog = () => {
  dialogVisible.value = true
}

// 关闭配置弹窗时更新图表并保存配置到 localStorage
const closeDialog = () => {
  dialogVisible.value = false
  updateChart()
  saveConfig()
}

// 保存当前 chartOptions 到 localStorage
const saveConfig = () => {
  localStorage.setItem(storageKey, JSON.stringify(chartOptions))
}

// 从 localStorage 中加载配置，并合并到 chartOptions
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

// 更新图表：根据 props.rawData 更新 xAxis 数据和 series 数据，然后传入完整配置
const updateChart = () => {
  if (!chartInstance || !props.rawData.length) return

  // 假设 rawData 格式为 [{ date, value1, value2 }, ...]
  const dates = props.rawData.map(item => item.date)
  const value1Data = props.rawData.map(item => item.value1)
  const value2Data = props.rawData.map(item => item.value2)

  chartOptions.xAxis.data = dates
  chartOptions.series[0].data = value1Data
  chartOptions.series[1].data = value2Data

  chartInstance.setOption({
    title: chartOptions.title,
    legend: chartOptions.legend,
    xAxis: chartOptions.xAxis,
    yAxis: chartOptions.yAxis,
    series: chartOptions.series
  })
}

const handleResize = () => chartInstance?.resize()

onMounted(() => {
  chartInstance = echarts.init(chart.value)
  window.addEventListener('resize', handleResize)
  loadConfig()   // 加载之前保存的配置
  updateChart()  // 更新图表显示
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
  position: relative;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 30px rgba(0, 0, 0, 0.2);
  padding: 30px;
  width: 600px;
  max-width: 90%;
  animation: fadeInScale 0.3s ease-in-out;
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

.config-columns {
  display: flex;
  gap: 30px;
  margin: 20px 0;
}

.config-left,
.config-right {
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

html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
}
</style>
