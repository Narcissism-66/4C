<template>
  <div class="chart-container">
    <div v-if="showAxisSettings" class="header">
      <h3 class="title">数据可视化分析</h3>
      <button class="settings-btn" @click="showDialog = true">
        <svg class="icon" viewBox="0 0 24 24">
          <path d="M19.43 12.98c.04-.32.07-.64.07-.98s-.03-.66-.07-.98l2.11-1.65c.19-.15.24-.42.12-.64l-2-3.46c-.12-.22-.39-.3-.61-.22l-2.49 1c-.52-.4-1.08-.73-1.69-.98l-.38-2.65C14.46 2.18 14.25 2 14 2h-4c-.25 0-.46.18-.49.42l-.38 2.65c-.61.25-1.17.59-1.69.98l-2.49-1c-.23-.09-.49 0-.61.22l-2 3.46c-.13.22-.07.49.12.64l2.11 1.65c-.04.32-.07.65-.07.98s.03.66.07.98l-2.11 1.65c-.19.15-.24.42-.12.64l2 3.46c.12.22.39.3.61.22l2.49-1c.52.4 1.08.73 1.69.98l.38 2.65c.03.24.24.42.49.42h4c.25 0 .46-.18.49-.42l.38-2.65c.61-.25 1.17-.59 1.69-.98l2.49 1c.23.09.49 0 .61-.22l2-3.46c.12-.22.07-.49-.12-.64l-2.11-1.65zM12 15.5c-1.93 0-3.5-1.57-3.5-3.5s1.57-3.5 3.5-3.5 3.5 1.57 3.5 3.5-1.57 3.5-3.5 3.5z"/>
        </svg>
        坐标轴设置
      </button>
    </div>
    <div ref="chart" class="chart"></div>

    <transition name="modal">
      <div v-if="showDialog" class="modal-mask">
        <div class="modal-wrapper">
          <div class="modal-container">
            <h3 class="modal-title">📐 坐标轴设置</h3>
            <div class="form-item">
              <label class="input-label">X轴含义：</label>
              <input v-model="xAxisName" type="text" placeholder="请输入X轴说明" class="styled-input">
            </div>
            <div class="form-item">
              <label class="input-label">Y轴含义：</label>
              <input v-model="yAxisName" type="text" placeholder="请输入Y轴说明" class="styled-input">
            </div>
            <div class="button-group">
              <button class="confirm-btn" @click="applyAxisNames">
                <span class="btn-icon">✅</span> 应用设置
              </button>
              <button class="cancel-btn" @click="showDialog = false">
                <span class="btn-icon">❌</span> 取消
              </button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { onMounted, ref, watch, defineProps, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import 'echarts-gl'

const chart = ref(null)
const chartInstance = ref(null)
const showDialog = ref(false)
const xAxisName = ref('')
const yAxisName = ref('')
const showAxisSettings = true

const props = defineProps({
  rawData: { type: Array, required: true, default: () => [] }
})

const gradientColors = [
  { offset: 0, color: '#7CFFCB' },
  { offset: 0.5, color: '#8E54E9' },
  { offset: 1, color: '#4776E6' }
]

const updateChart = () => {
  if (!props.rawData.length) return

  const seriesData = props.rawData.map(item => [item.x, item.y, item.size])

  const option = {
    title: {
      text: '三维气泡分析图',
      left: 'center',
      textStyle: {
        fontWeight: 'bold',
        fontSize: 22,
        color: '#2c3e50',
        textShadow: '2px 2px 4px rgba(0,0,0,0.1)'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: params => `
        <div class="tooltip-box">
          <div class="tooltip-title">数据点详情</div>
          <div>X: ${params.value[0]}</div>
          <div>Y: ${params.value[1]}</div>
          <div>大小: ${params.value[2]}</div>
        </div>
      `,
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#e0e0e0',
      borderWidth: 1,
      textStyle: {
        color: '#2c3e50',
        fontSize: 14
      },
      extraCssText: 'box-shadow: 0 8px 24px rgba(0,0,0,0.15); border-radius: 12px;'
    },
    xAxis: {
      type: 'value',
      name: xAxisName.value,
      nameLocation: 'center',
      nameGap: 30,
      nameTextStyle: {
        fontWeight: '600',
        fontSize: 16,
        color: '#4a4a4a'
      },
      axisLabel: {
        color: '#7f8c8d',
        fontSize: 12
      },
      axisLine: {
        lineStyle: {
          color: '#dcdde1',
          width: 2
        }
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: '#f5f6fa',
          width: 1
        }
      }
    },
    yAxis: {
      type: 'value',
      name: yAxisName.value,
      nameTextStyle: {
        fontWeight: '600',
        fontSize: 16,
        color: '#4a4a4a'
      },
      axisLabel: {
        color: '#7f8c8d',
        fontSize: 12
      },
      axisLine: {
        lineStyle: {
          color: '#dcdde1',
          width: 2
        }
      },
      splitLine: {
        lineStyle: {
          color: '#f5f6fa',
          width: 1
        }
      }
    },
    series: [{
      name: '数值',
      type: 'scatter',
      data: seriesData,
      symbolSize: data => data[2] * 3,
      itemStyle: {
        color: new echarts.graphic.RadialGradient(0.4, 0.3, 1, gradientColors),
        shadowBlur: 15,
        shadowColor: 'rgba(113, 93, 255, 0.4)',
        opacity: 0.8
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 20,
          shadowColor: 'rgba(113, 93, 255, 0.6)',
          opacity: 1
        }
      },
      animationDuration: 2000,
      animationEasing: 'elasticOut'
    }],
    grid: {
      containLabel: true,
      backgroundColor: '#fff',
      borderColor: '#f0f0f0',
      shadowColor: 'rgba(143, 155, 179, 0.15)',
      shadowBlur: 20
    },
    backgroundColor: '#f8f9fa'
  }

  chartInstance.value.setOption(option)
}

const applyAxisNames = () => {
  showDialog.value = false
  updateChart()
}

const handleResize = () => {
  chartInstance.value.resize()
}

onMounted(() => {
  chartInstance.value = echarts.init(chart.value)
  updateChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance.value.dispose()
})

watch(() => props.rawData, () => {
  updateChart()
}, { deep: true })
</script>

<style scoped>
:root {
  --primary-color: #6C5CE7;
  --secondary-color: #A8A4FF;
  --accent-color: #FF7675;
  --background-light: #f8f9fa;
  --text-dark: #2d3436;
  --shadow-light: rgba(149, 157, 165, 0.2);
}

.chart-container {
  position: relative;
  width: 100%;
  height: 100%;
  background: var(--background-light);
  border-radius: 16px;
  box-shadow: 0 12px 24px var(--shadow-light);
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.chart {
  width: 100%;
  height: calc(100% - 60px);
  min-height: 500px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  background: linear-gradient(135deg, var(--primary-color) 0%, #4b3ac9 100%);
}

.title {
  margin: 0;
  color: white;
  font-size: 1.6rem;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.settings-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(4px);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 15px;
  font-weight: 500;
}

.settings-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.icon {
  width: 20px;
  height: 20px;
  filter: drop-shadow(0 1px 2px rgba(0,0,0,0.1));
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
  backdrop-filter: blur(3px);
}

.modal-wrapper {
  transform: scale(0.95);
  transition: all 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.modal-container {
  background: white;
  padding: 30px;
  border-radius: 20px;
  width: 400px;
  box-shadow: 0 16px 40px rgba(0,0,0,0.2);
  transform: translateY(20px);
  opacity: 0;
  transition: all 0.3s ease;
}

.modal-enter-active .modal-container {
  transform: translateY(0);
  opacity: 1;
}

.modal-title {
  margin: 0 0 25px;
  color: var(--text-dark);
  font-size: 1.5rem;
  text-align: center;
}

.form-item {
  margin: 20px 0;
}

.input-label {
  display: block;
  margin-bottom: 10px;
  color: #636e72;
  font-weight: 500;
  font-size: 15px;
}

.styled-input {
  width: 100%;
  padding: 14px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 15px;
  transition: all 0.3s ease;
  background: #f8f9fa;
}

.styled-input:focus {
  border-color: var(--primary-color);
  background: white;
  box-shadow: 0 4px 12px rgba(108, 92, 231, 0.15);
}

.button-group {
  display: flex;
  gap: 15px;
  margin-top: 30px;
}

.confirm-btn, .cancel-btn {
  flex: 1;
  padding: 14px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.confirm-btn {
  background: var(--primary-color);
  color: white;
}

.cancel-btn {
  background: var(--accent-color);
  color: white;
}

.confirm-btn:hover {
  background: #5b4bc4;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(108, 92, 231, 0.3);
}

.cancel-btn:hover {
  background: #e66767;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 118, 117, 0.3);
}

.btn-icon {
  font-size: 16px;
}

.tooltip-box {
  padding: 12px;
  border-radius: 8px;
  background: white;
}

.tooltip-title {
  font-weight: 600;
  color: var(--primary-color);
  margin-bottom: 8px;
  padding-bottom: 4px;
  border-bottom: 1px solid #eee;
}
</style>


