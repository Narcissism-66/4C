<template>
  <div class="chart-container">
    <div v-if="showAxisSettings" class="header">
      <h3 class="title">双轴图</h3>
      <button class="settings-btn" @click="showDialog = true">
        <svg class="icon" viewBox="0 0 24 24">
          <path d="M19.43 12.98c.04-.32.07-.64.07-.98s-.03-.66-.07-.98l2.11-1.65c.19-.15.24-.42.12-.64l-2-3.46c-.12-.22-.39-.3-.61-.22l-2.49 1c-.52-.4-1.08-.73-1.69-.98l-.38-2.65C14.46 2.18 14.25 2 14 2h-4c-.25 0-.46.18-.49.42l-.38 2.65c-.61.25-1.17.59-1.69.98l-2.49-1c-.23-.09-.49 0-.61.22l-2 3.46c-.13.22-.07.49.12.64l2.11 1.65c-.04.32-.07.65-.07.98s.03.66.07.98l-2.11 1.65c-.19.15-.24.42-.12.64l2 3.46c.12.22.39.3.61.22l2.49-1c.52.4 1.08.73 1.69.98l.38 2.65c.03.24.24.42.49.42h4c.25 0 .46-.18.49-.42l.38-2.65c.61-.25 1.17-.59 1.69-.98l2.49 1c.23.09.49 0 .61-.22l2-3.46c.12-.22.07-.49-.12-.64l-2.11-1.65zM12 15.5c-1.93 0-3.5-1.57-3.5-3.5s1.57-3.5 3.5-3.5 3.5 1.57 3.5 3.5-1.57 3.5-3.5 3.5z"/>
        </svg>
        坐标轴设置
      </button>
    </div>
    <div ref="chart" class="chart"></div>
    <div v-if="showDialog" class="modal-mask">
      <div class="modal-wrapper">
        <div class="modal-container">
          <h3>📐 坐标轴设置</h3>
          <div class="form-item">
            <label class="input-label">X轴含义：</label>
            <input v-model="xAxisName" type="text" placeholder="请输入X轴说明">
          </div>
          <div class="form-item">
            <label class="input-label">左Y轴含义：</label>
            <input v-model="leftYAxisName" type="text" placeholder="请输入左Y轴说明">
          </div>
          <div class="form-item">
            <label class="input-label">右Y轴含义：</label>
            <input v-model="rightYAxisName" type="text" placeholder="请输入右Y轴说明">
          </div>
          <div class="button-group">
            <button class="confirm-btn" @click="applyAxisNames">✅ 应用设置</button>
            <button class="cancel-btn" @click="showDialog = false">❌ 取消</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch, defineProps } from 'vue'
import * as echarts from 'echarts'

const chart = ref(null)
const chartInstance = ref(null)
const showDialog = ref(false)
const xAxisName = ref('')
const leftYAxisName = ref('')
const rightYAxisName = ref('')
const showAxisSettings = true

const props = defineProps({
  rawData: { type: Array, required: true, default: () => [] }
})

const updateChart = () => {
  if (!props.rawData.length) return
  const dates = props.rawData.map(item => item.date)
  const value1Data = props.rawData.map(item => item.value1)
  const value2Data = props.rawData.map(item => item.value2)
  const option = {
    title: {
      text: '双轴图',
      left: 'center',
      textStyle: { fontWeight: 'bold', fontSize: 20, color: '#2c3e50' }
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(52,152,219,0.9)',
      textStyle: { color: '#fff', fontSize: 14 },
      borderWidth: 0,
      borderRadius: 6
    },
    legend: {
      data: ['数据1', '数据2'],
      top: 40,
      textStyle: { fontSize: 14, color: '#7f8c8d' }
    },
    xAxis: {
      type: 'category',
      name: xAxisName.value,
      nameLocation: 'center',
      nameGap: 30,
      nameTextStyle: { fontWeight: 'bold', fontSize: 14, color: '#2c3e50' },
      data: dates,
      axisLabel: { color: '#7f8c8d', fontSize: 12, margin: 15 },
      axisLine: { lineStyle: { color: '#bdc3c7', width: 2 } }
    },
    yAxis: [
      {
        type: 'value',
        name: leftYAxisName.value,
        nameTextStyle: { fontWeight: 'bold', fontSize: 14, color: '#2c3e50' },
        axisLabel: { color: '#7f8c8d', fontSize: 12 },
        axisLine: { lineStyle: { color: '#bdc3c7', width: 2 } },
        splitLine: { lineStyle: { color: '#ecf0f1' } }
      },
      {
        type: 'value',
        name: rightYAxisName.value,
        nameTextStyle: { fontWeight: 'bold', fontSize: 14, color: '#2c3e50' },
        axisLabel: { color: '#7f8c8d', fontSize: 12 },
        axisLine: { lineStyle: { color: '#bdc3c7', width: 2 } },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '数据1',
        type: 'bar',
        data: value1Data,
        itemStyle: { color: '#2ecc71' }
      },
      {
        name: '数据2',
        type: 'line',
        yAxisIndex: 1,
        data: value2Data,
        lineStyle: { color: '#e67e22', width: 3 },
        itemStyle: { color: '#e67e22' }
      }
    ]
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
})

watch(() => props.rawData, () => {
  updateChart()
}, { deep: true })
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
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 1rem;
}

.title {
  margin: 0;
  color: #2c3e50;
  font-size: 1.4rem;
  font-weight: 600;
}

.settings-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.settings-btn:hover {
  background: #2980b9;
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(52, 152, 219, 0.3);
}

.icon {
  width: 18px;
  height: 18px;
  fill: currentColor;
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
}

.modal-container {
  background: white;
  padding: 25px;
  border-radius: 12px;
  width: 350px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.form-item {
  margin: 18px 0;
}

.input-label {
  display: block;
  margin-bottom: 8px;
  color: #2c3e50;
  font-weight: 500;
  font-size: 14px;
}

input {
  width: 100%;
  padding: 10px 12px;
  border: 2px solid #bdc3c7;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
}

input:focus {
  border-color: #3498db;
  outline: none;
}

.button-group {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 25px;
}

.confirm-btn, .cancel-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.confirm-btn {
  background: #3498db;
  color: white;
}

.cancel-btn {
  background: #e74c3c;
  color: white;
}

button:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}
</style>
