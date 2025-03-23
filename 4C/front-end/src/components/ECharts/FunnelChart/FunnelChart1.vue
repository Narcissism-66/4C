<template>
  <div ref="chart" class="chart" @contextmenu.prevent="openDialog"></div>
  <!-- 配置弹窗 -->
  <div v-if="dialogVisible" class="modal-overlay">
    <div class="modal">
      <!-- 弹窗头部：标题和右上角关闭按钮 -->
      <div class="modal-header">
        <h3>设置图表配置</h3>
        <span class="close-btn" @click="closeDialog">×</span>
      </div>
      <!-- 弹窗主体：左右两列配置 -->
      <div class="config-columns">
        <!-- 左侧：图表标题相关 -->
        <div class="config-left">
          <div class="input-group">
            <label for="chartTitle">图表标题：</label>
            <input id="chartTitle" v-model="chartTitle" placeholder="请输入图表标题" />
          </div>
          <div class="input-group">
            <label for="titlePosition">标题位置：</label>
            <select id="titlePosition" v-model="titlePosition">
              <option value="left">左侧</option>
              <option value="center">中间</option>
              <option value="right">右侧</option>
            </select>
          </div>
          <div class="input-group">
            <label for="titleColor">标题颜色：</label>
            <div class="color-input">
              <input id="titleColor" v-model="titleColor" type="color" />
              <span class="color-preview" :style="{ backgroundColor: titleColor }"></span>
            </div>
          </div>
        </div>
        <!-- 右侧：funnel系列相关 -->
        <div class="config-right">
          <div class="input-group">
            <label for="funnelName">系列名称：</label>
            <input id="funnelName" v-model="funnelName" placeholder="请输入系列名称" />
          </div>
          <div class="input-group">
            <label for="funnelLeft">左边距：</label>
            <input id="funnelLeft" v-model="funnelLeft" placeholder="例如：10%" />
          </div>
          <div class="input-group">
            <label for="funnelTop">顶部距离：</label>
            <input id="funnelTop" v-model.number="funnelTop" placeholder="例如：60" />
          </div>
          <div class="input-group">
            <label for="funnelBottom">底部距离：</label>
            <input id="funnelBottom" v-model.number="funnelBottom" placeholder="例如：60" />
          </div>
          <div class="input-group">
            <label for="funnelWidth">宽度：</label>
            <input id="funnelWidth" v-model="funnelWidth" placeholder="例如：80%" />
          </div>
          <div class="input-group">
            <label for="borderWidth">边框宽度：</label>
            <input id="borderWidth" v-model.number="borderWidth" placeholder="例如：1" />
          </div>
          <div class="input-group">
            <label for="borderColor">边框颜色：</label>
            <div class="color-input">
              <input id="borderColor" v-model="borderColor" type="color" />
              <span class="color-preview" :style="{ backgroundColor: borderColor }"></span>
            </div>
          </div>
          <div class="input-group">
            <label for="labelFontSize">标签字体大小：</label>
            <input id="labelFontSize" v-model.number="labelFontSize" placeholder="例如：14" />
          </div>
        </div>
      </div>
      <!-- 底部确定按钮 -->
      <button @click="closeDialog">确定</button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch, defineProps } from 'vue'
import * as echarts from 'echarts'
import { userUserStore } from '@/stores/UserStore.js'

const chart = ref(null)
const chartInstance = ref(null)
const dialogVisible = ref(false)
// 获取用户信息，用于生成存储 key
const userStore = userUserStore()

const props = defineProps({
  rawData: { type: Array, required: true, default: () => [] }
})

// 存储 key，结合用户 id 保证各个用户配置互不影响
const storageKey = `FunnelChartConfig-${userStore.user.id}`

// 配置项：图表标题相关
const chartTitle = ref('')
const titlePosition = ref('center')
const titleColor = ref('#333333')

// 配置项：funnel系列相关
const funnelName = ref('转化率')
const funnelLeft = ref('10%')
const funnelTop = ref(60)
const funnelBottom = ref(60)
const funnelWidth = ref('80%')
const borderWidth = ref(1)
const borderColor = ref('#aaa')
const labelFontSize = ref(14)

// 右键打开弹窗
const openDialog = () => {
  dialogVisible.value = true
}

// 关闭弹窗时更新图表并保存配置
const closeDialog = () => {
  dialogVisible.value = false
  updateChart()
  saveChartConfigLocally()
}

// 保存配置到 localStorage
const saveChartConfigLocally = () => {
  const config = {
    chartTitle: chartTitle.value,
    titlePosition: titlePosition.value,
    titleColor: titleColor.value,
    funnelName: funnelName.value,
    funnelLeft: funnelLeft.value,
    funnelTop: funnelTop.value,
    funnelBottom: funnelBottom.value,
    funnelWidth: funnelWidth.value,
    borderWidth: borderWidth.value,
    borderColor: borderColor.value,
    labelFontSize: labelFontSize.value
  }
  localStorage.setItem(storageKey, JSON.stringify(config))
}

// 从 localStorage 加载配置
const loadChartConfigLocally = () => {
  const configStr = localStorage.getItem(storageKey)
  if (configStr) {
    try {
      const config = JSON.parse(configStr)
      chartTitle.value = config.chartTitle || ''
      titlePosition.value = config.titlePosition || 'center'
      titleColor.value = config.titleColor || '#333333'
      funnelName.value = config.funnelName || '转化率'
      funnelLeft.value = config.funnelLeft || '10%'
      funnelTop.value = config.funnelTop || 60
      funnelBottom.value = config.funnelBottom || 60
      funnelWidth.value = config.funnelWidth || '80%'
      borderWidth.value = config.borderWidth || 1
      borderColor.value = config.borderColor || '#aaa'
      labelFontSize.value = config.labelFontSize || 14
    } catch (error) {
      console.error("加载图表配置失败", error)
    }
  }
}

const updateChart = () => {
  if (!props.rawData.length) return
  // 处理传入数据：假设每个数据项包含 stage 与 value 字段
  const data = props.rawData.map(item => ({ name: item.stage, value: item.value }))
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
      trigger: 'item',
      formatter: '{a} <br/>{b} : {c}%'
    },
    series: [{
      name: funnelName.value,
      type: 'funnel',
      left: funnelLeft.value,
      top: funnelTop.value,
      bottom: funnelBottom.value,
      width: funnelWidth.value,
      data: data,
      itemStyle: {
        borderWidth: borderWidth.value,
        borderColor: borderColor.value
      },
      label: {
        fontSize: labelFontSize.value
      }
    }]
  }
  chartInstance.value.setOption(option)
}

onMounted(() => {
  // 加载之前保存的配置
  loadChartConfigLocally()
  chartInstance.value = echarts.init(chart.value)
  updateChart()
})

watch(() => props.rawData, () => {
  updateChart()
}, { deep: true })
</script>

<style scoped>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
}

/* 图表充满整个视口 */
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

/* 弹窗主体 */
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

/* 关闭按钮：右上角绝对定位 */
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

/* 分栏布局 */
.config-columns {
  display: flex;
  gap: 30px;
  margin: 20px 0;
}

.config-left,
.config-right {
  flex: 1;
}

/* 输入组 */
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

/* 颜色输入样式 */
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
}

/* 按钮样式 */
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
