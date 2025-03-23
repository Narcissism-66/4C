<template>
  <div ref="chart" class="chart" @contextmenu.prevent="openDialog"></div>
  <!-- 配置弹窗 -->
  <div v-if="dialogVisible" class="modal-overlay">
    <div class="modal">
      <!-- 弹窗头部：标题和右上角的关闭按钮 -->
      <div class="modal-header">
        <h3>设置雷达图配置</h3>
        <span class="close-btn" @click="closeDialog">×</span>
      </div>
      <!-- 弹窗主体：左右两列 -->
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
        <!-- 右侧：雷达图相关配置 -->
        <div class="config-right">
          <div class="axis-group">
            <h4>雷达图配置</h4>
            <div class="input-group">
              <label for="indicatorLabelColor">指标文字颜色：</label>
              <div class="color-input">
                <input id="indicatorLabelColor" v-model="indicatorLabelColor" type="color" />
                <span class="color-preview" :style="{ backgroundColor: indicatorLabelColor }"></span>
              </div>
            </div>
            <div class="input-group">
              <label for="radarAxisLineColor">轴线颜色：</label>
              <div class="color-input">
                <input id="radarAxisLineColor" v-model="radarAxisLineColor" type="color" />
                <span class="color-preview" :style="{ backgroundColor: radarAxisLineColor }"></span>
              </div>
            </div>
            <div class="input-group">
              <label for="radarSplitLineColor">分割线颜色：</label>
              <div class="color-input">
                <input id="radarSplitLineColor" v-model="radarSplitLineColor" type="color" />
                <span class="color-preview" :style="{ backgroundColor: radarSplitLineColor }"></span>
              </div>
            </div>
            <div class="input-group">
              <label for="radarSplitAreaColor">覆盖区域颜色：</label>
              <div class="color-input">
                <input id="radarSplitAreaColor" v-model="radarSplitAreaColor" type="color" />
                <span class="color-preview" :style="{ backgroundColor: radarSplitAreaColor }"></span>
              </div>
            </div>
            <div class="input-group">
              <label for="seriesLineColor">系列线颜色：</label>
              <div class="color-input">
                <input id="seriesLineColor" v-model="seriesLineColor" type="color" />
                <span class="color-preview" :style="{ backgroundColor: seriesLineColor }"></span>
              </div>
            </div>
            <div class="input-group">
              <label for="seriesAreaColor">系列填充颜色：</label>
              <div class="color-input">
                <input id="seriesAreaColor" v-model="seriesAreaColor" type="color" />
                <span class="color-preview" :style="{ backgroundColor: seriesAreaColor }"></span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 底部确定按钮 -->
      <button @click="closeDialog">确定</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, defineProps } from 'vue'
import * as echarts from 'echarts'

// 图表容器与实例
const chart = ref(null)
const chartInstance = ref(null)
const dialogVisible = ref(false)

// 标题相关配置
const chartTitle = ref('雷达图标题')
const titleColor = ref('#333333')
const titlePosition = ref('left')

// 雷达图专用配置
const radarAxisLineColor = ref('#ccc')        // 雷达轴线颜色
const radarSplitLineColor = ref('#ccc')         // 分割线颜色
const radarSplitAreaColor = ref('#f0f0f0')        // 覆盖区域颜色（splitArea 使用数组形式）
const indicatorLabelColor = ref('#333333')       // 指标文字颜色

// 系列样式配置
const seriesLineColor = ref('#5470c6')           // 系列线颜色
const seriesAreaColor = ref('rgba(84,112,198,0.3)')// 系列填充颜色

// 接收父组件传入的数据，格式要求：[{ category, indicator, value }, …]
const props = defineProps({
  rawData: { type: Array, required: true, default: () => [] }
})

// 右键打开弹窗
const openDialog = () => {
  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
  updateChart()
}

const updateChart = () => {
  if (!props.rawData.length) return
  // 将数据按 category 与 indicator 分组
  const grouped = {}
  const indicatorsSet = new Set()
  props.rawData.forEach(item => {
    if (!grouped[item.category]) grouped[item.category] = {}
    grouped[item.category][item.indicator] = item.value
    indicatorsSet.add(item.indicator)
  })
  const indicators = Array.from(indicatorsSet)
  // 求每个指标的最大值（乘以 1.1 作为最大刻度）
  const maxValues = {}
  indicators.forEach(ind => {
    let maxVal = 0
    props.rawData.forEach(item => {
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
  // 构造系列数据，每个 category 对应一组数值
  const seriesData = Object.entries(grouped).map(([category, valuesObj]) => ({
    name: category,
    value: indicators.map(ind => valuesObj[ind] || 0)
  }))

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
    tooltip: {},
    legend: {
      data: Object.keys(grouped),
      top: 0,
      textStyle: { fontSize: 14, color: '#7f8c8d' }
    },
    radar: {
      indicator: radarIndicators,
      center: ['50%', '60%'],
      // 配置雷达轴与分割线样式
      axisLine: { lineStyle: { color: radarAxisLineColor.value } },
      splitLine: { lineStyle: { color: radarSplitLineColor.value } },
      splitArea: { areaStyle: { color: [radarSplitAreaColor.value] } },
      // 设置指标文字颜色
      axisName: { color: indicatorLabelColor.value }
    },
    series: [{
      name: '雷达图数据',
      type: 'radar',
      data: seriesData,
      lineStyle: { color: seriesLineColor.value },
      areaStyle: { color: seriesAreaColor.value }
    }]
  }
  chartInstance.value.setOption(option)
}

onMounted(() => {
  chartInstance.value = echarts.init(chart.value)
  updateChart()
})

// 监听数据变化，实时更新图表
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

/* 关闭按钮：绝对定位在右上角 */
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
  0% {
    opacity: 0;
    transform: scale(0.8);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
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
