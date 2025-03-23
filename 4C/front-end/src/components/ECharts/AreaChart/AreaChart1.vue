<template>
  <div ref="chart" class="chart" @contextmenu.prevent="openDialog"></div>
  <!-- 配置弹窗 -->
  <div v-if="dialogVisible" class="modal-overlay">
    <div class="modal">
      <div class="modal-header">
        <h3>配置面积图属性</h3>
        <span class="close-btn" @click="closeDialog">×</span>
      </div>
      <div class="config-columns">
        <!-- 左侧配置列 -->
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
          <div class="input-group">
            <label for="lineColor">折线图颜色：</label>
            <div class="color-input">
              <input id="lineColor" v-model="lineColor" type="color" />
              <span class="color-preview" :style="{ backgroundColor: lineColor }"></span>
            </div>
          </div>
          <div class="input-group">
            <label for="areaColorStart">面积图起始颜色：</label>
            <div class="color-input">
              <input id="areaColorStart" v-model="areaColorStart" type="color" />
              <span class="color-preview" :style="{ backgroundColor: areaColorStart }"></span>
            </div>
          </div>
          <div class="input-group">
            <label for="areaColorEnd">面积图结束颜色：</label>
            <div class="color-input">
              <input id="areaColorEnd" v-model="areaColorEnd" type="color" />
              <span class="color-preview" :style="{ backgroundColor: areaColorEnd }"></span>
            </div>
          </div>
        </div>

        <!-- 右侧坐标轴配置列 -->
        <div class="config-right">
          <div class="axis-group">
            <h4>X轴配置</h4>
            <div class="input-group">
              <label for="xAxis">名称：</label>
              <input id="xAxis" v-model="xAxisName" placeholder="X轴名称" />
            </div>
            <div class="input-group">
              <label for="xAxisLineColor">轴线颜色：</label>
              <div class="color-input">
                <input id="xAxisLineColor" v-model="xAxisLineColor" type="color" />
                <span class="color-preview" :style="{ backgroundColor: xAxisLineColor }"></span>
              </div>
            </div>
            <div class="input-group">
              <label for="xAxisLabelColor">标签颜色：</label>
              <div class="color-input">
                <input id="xAxisLabelColor" v-model="xAxisLabelColor" type="color" />
                <span class="color-preview" :style="{ backgroundColor: xAxisLabelColor }"></span>
              </div>
            </div>
          </div>

          <div class="axis-group">
            <h4>Y轴配置</h4>
            <div class="input-group">
              <label for="yAxis">名称：</label>
              <input id="yAxis" v-model="yAxisName" placeholder="Y轴名称" />
            </div>
            <div class="input-group">
              <label for="yAxisSplitLineColor">分割线颜色：</label>
              <div class="color-input">
                <input id="yAxisSplitLineColor" v-model="yAxisSplitLineColor" type="color" />
                <span class="color-preview" :style="{ backgroundColor: yAxisSplitLineColor }"></span>
              </div>
            </div>
            <div class="input-group">
              <label for="yAxisLineColor">轴线颜色：</label>
              <div class="color-input">
                <input id="yAxisLineColor" v-model="yAxisLineColor" type="color" />
                <span class="color-preview" :style="{ backgroundColor: yAxisLineColor }"></span>
              </div>
            </div>
            <div class="input-group">
              <label for="yAxisLabelColor">标签颜色：</label>
              <div class="color-input">
                <input id="yAxisLabelColor" v-model="yAxisLabelColor" type="color" />
                <span class="color-preview" :style="{ backgroundColor: yAxisLabelColor }"></span>
              </div>
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
import { userUserStore } from '@/stores/UserStore.js'

const userStore = userUserStore()

// 定义 props：rawData 为图表数据，chartKey 用于标识不同图表的配置
const props = defineProps({
  rawData: { type: Array, required: true, default: () => [] },
  chartKey: { type: String, required: false, default: 'defaultChartConfig' }
})

// 使用 userStore.user.id 构造 LocalStorage 键，保证不同用户的配置不互相影响
const storageKey = `AreaChartConfig-${userStore.user.id}`

const chart = ref(null)
const chartInstance = ref(null)
const dialogVisible = ref(false)

// 配置项
const chartTitle = ref('')
const titlePosition = ref('center')
const titleColor = ref('#333333')
const lineColor = ref('#e74c3c')
const areaColorStart = ref('#e74c3c')
const areaColorEnd = ref('#ffffff')
const xAxisName = ref('')
const yAxisName = ref('')
const xAxisLineColor = ref('#bdc3c7')
const yAxisLineColor = ref('#bdc3c7')
const xAxisLabelColor = ref('#2c3e50')
const yAxisLabelColor = ref('#7f8c8d')
const yAxisSplitLineColor = ref('#e0e0e0'); // 默认分割线颜色

// 右键打开弹窗
const openDialog = () => {
  dialogVisible.value = true
}

// 关闭弹窗时更新图表并保存配置到 LocalStorage
const closeDialog = () => {
  dialogVisible.value = false
  updateChart()
  saveChartConfigLocally()
}

// 图表初始化：加载图表实例、尝试读取 LocalStorage 配置并更新图表
onMounted(() => {
  chartInstance.value = echarts.init(chart.value)
  loadChartConfigLocally()
  updateChart()
})

// 更新图表：将当前配置和传入数据应用到 ECharts option 中
const updateChart = () => {
  if (!props.rawData.length) return
  const dates = props.rawData.map(item => item.date)
  const values = props.rawData.map(item => item.value)

  const option = {
    title: {
      text: chartTitle.value,
      left: titlePosition.value,
      textStyle: {
        color: titleColor.value,
        fontSize: 18,
        fontWeight: 'bold',
      },
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(52,152,219,0.9)',
      textStyle: { color: '#fff', fontSize: 14 },
      borderWidth: 0,
      borderRadius: 6,
    },
    xAxis: {
      type: 'category',
      name: xAxisName.value,
      nameLocation: 'center',
      nameGap: 30,
      nameTextStyle: {
        fontWeight: 'bold',
        fontSize: 14,
        color: xAxisLabelColor.value,
      },
      data: dates,
      axisLabel: { color: xAxisLabelColor.value, fontSize: 12 },
      axisLine: { lineStyle: { color: xAxisLineColor.value, width: 2 } },
    },
    yAxis: {
      type: 'value',
      name: yAxisName.value,
      nameLocation: 'end',
      nameTextStyle: {
        fontWeight: 'bold',
        fontSize: 14,
        color: yAxisLabelColor.value,
      },
      axisLabel: { color: yAxisLabelColor.value, fontSize: 12 },
      axisLine: { lineStyle: { color: yAxisLineColor.value, width: 2 } },
      splitLine: {
        show: true,
        lineStyle: {
          color: yAxisSplitLineColor.value, // 应用用户选择的分割线颜色
          width: 1,
          type: 'solid'
        }
      }
    },
    grid: {
      top: 40,
      left: 10,
      right: 10,
      bottom: 30,
      containLabel: true,
    },
    series: [
      {
        name: '数值',
        type: 'line',
        data: values,
        smooth: true,
        lineStyle: {
          color: lineColor.value,
          width: 3,
          shadowBlur: 10,
          shadowColor: 'rgba(231, 76, 60, 0.5)',
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: areaColorStart.value },
            { offset: 1, color: areaColorEnd.value },
          ]),
        },
        itemStyle: {
          color: lineColor.value,
        },
      },
    ],
  }
  chartInstance.value.setOption(option)
}

// 保存当前图表配置到 LocalStorage
const saveChartConfigLocally = () => {
  const config = {
    chartTitle: chartTitle.value,
    titlePosition: titlePosition.value,
    titleColor: titleColor.value,
    lineColor: lineColor.value,
    areaColorStart: areaColorStart.value,
    areaColorEnd: areaColorEnd.value,
    xAxisName: xAxisName.value,
    yAxisName: yAxisName.value,
    xAxisLineColor: xAxisLineColor.value,
    yAxisLineColor: yAxisLineColor.value,
    xAxisLabelColor: xAxisLabelColor.value,
    yAxisLabelColor: yAxisLabelColor.value,
    yAxisSplitLineColor: yAxisSplitLineColor.value,
  }
  localStorage.setItem(storageKey, JSON.stringify(config))
}

// 从 LocalStorage 中加载配置（如果存在则应用到当前配置项）
const loadChartConfigLocally = () => {
  const configStr = localStorage.getItem(storageKey)
  if (configStr) {
    try {
      const config = JSON.parse(configStr)
      chartTitle.value = config.chartTitle || ''
      titlePosition.value = config.titlePosition || 'center'
      titleColor.value = config.titleColor || '#333333'
      lineColor.value = config.lineColor || '#e74c3c'
      areaColorStart.value = config.areaColorStart || '#e74c3c'
      areaColorEnd.value = config.areaColorEnd || '#ffffff'
      xAxisName.value = config.xAxisName || ''
      yAxisName.value = config.yAxisName || ''
      xAxisLineColor.value = config.xAxisLineColor || '#bdc3c7'
      yAxisLineColor.value = config.yAxisLineColor || '#bdc3c7'
      xAxisLabelColor.value = config.xAxisLabelColor || '#2c3e50'
      yAxisLabelColor.value = config.yAxisLabelColor || '#7f8c8d'
      yAxisSplitLineColor.value = config.yAxisSplitLineColor || '#e0e0e0' // 应用用户选择的分割线颜色
    } catch (error) {
      console.error("加载图表配置失败", error)
    }
  }
}

// 当 rawData 发生变化时更新图表
watch(
  () => props.rawData,
  () => {
    updateChart()
  },
  { deep: true }
)
</script>

<style scoped>
.close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}
/* 弹窗头部 */
.modal-header {
  position: relative;
  margin-bottom: 20px;
  text-align: center;
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

.axis-group {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.axis-group h4 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 16px;
}

.color-input {
  display: flex;
  align-items: center;
  gap: 10px;
}

.color-preview {
  width: 24px;
  height: 24px;
  border: 1px solid #ddd;
  border-radius: 4px;
  display: inline-block;
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

.modal h3 {
  margin-top: 0;
  text-align: center;
  color: #333;
  font-weight: 600;
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
}

button:hover {
  background-color: #3d5a9a;
}

.chart {
  width: 100%;
  height: 100%;
  cursor: pointer;
}

html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
}
</style>
