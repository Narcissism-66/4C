<template>
  <div ref="chart" class="chart" @contextmenu.prevent="openDialog"></div>
  <!-- 配置弹窗 -->
  <div v-if="dialogVisible" class="modal-overlay">
    <div class="modal">
      <!-- 弹窗头部：标题和右上角的关闭按钮 -->
      <div class="modal-header">
        <h3>设置图表配置</h3>
        <span class="close-btn" @click="closeDialog">×</span>
      </div>
      <!-- 弹窗主体：左右两列 -->
      <div class="config-columns">
        <!-- 左侧：其它配置 -->
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
          <!-- 新增：箱体颜色 -->
          <div class="input-group">
            <label for="boxColor">箱体颜色：</label>
            <div class="color-input">
              <input id="boxColor" v-model="boxColor" type="color" />
              <span class="color-preview" :style="{ backgroundColor: boxColor }"></span>
            </div>
          </div>
          <!-- 新增：边框颜色 -->
          <div class="input-group">
            <label for="borderColor">边框颜色：</label>
            <div class="color-input">
              <input id="borderColor" v-model="borderColor" type="color" />
              <span class="color-preview" :style="{ backgroundColor: borderColor }"></span>
            </div>
          </div>
        </div>
        <!-- 右侧：X、Y轴配置 -->
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
      <!-- 底部确定按钮 -->
      <button @click="closeDialog">确定</button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch, defineProps } from 'vue'
import * as echarts from 'echarts'
import { userUserStore } from '@/stores/UserStore.js'

// 图表 DOM 与实例引用
const chart = ref(null)
const chartInstance = ref(null)
const dialogVisible = ref(false)

// 配置项定义
const chartTitle = ref('')         // 图表标题
const titleColor = ref('#333333')    // 标题颜色
const titlePosition = ref('center')  // 标题位置

// X、Y轴配置
const xAxisName = ref('')                // X轴名称
const yAxisName = ref('')                // Y轴名称
const xAxisLineColor = ref('#2980b9')      // X轴轴线颜色
const xAxisLabelColor = ref('#7f8c8d')     // X轴标签颜色
const yAxisLineColor = ref('#2980b9')      // Y轴轴线颜色
const yAxisLabelColor = ref('#7f8c8d')     // Y轴标签颜色
const yAxisSplitLineColor = ref('#e0e0e0'); // 默认分割线颜色

// 箱体配置（用于 boxplot）
const boxColor = ref('#3498db')          // 箱体颜色
const borderColor = ref('#2980b9')        // 边框颜色

// 接收父组件传入的数据（箱型图数据格式：[{ name, min, q1, median, q3, max }, …]）
const props = defineProps({
  rawData: { type: Array, required: true, default: () => [] }
})

// 定义存储 key（如果需要结合用户信息，可进一步拼接，例如 'BoxPlotChartConfig-用户ID'）
const userStore = userUserStore()
const storageKey = `BoxPlotChartConfig-${userStore.user.id}`

// 右键打开配置弹窗
const openDialog = () => {
  dialogVisible.value = true
}

// 关闭弹窗时更新图表并保存配置
const closeDialog = () => {
  dialogVisible.value = false
  updateChart()
  saveBoxPlotConfigLocally()
}

// 保存配置到 localStorage
const saveBoxPlotConfigLocally = () => {
  const config = {
    chartTitle: chartTitle.value,
    titleColor: titleColor.value,
    titlePosition: titlePosition.value,
    xAxisName: xAxisName.value,
    yAxisName: yAxisName.value,
    xAxisLineColor: xAxisLineColor.value,
    xAxisLabelColor: xAxisLabelColor.value,
    yAxisLineColor: yAxisLineColor.value,
    yAxisLabelColor: yAxisLabelColor.value,
    boxColor: boxColor.value,
    borderColor: borderColor.value,
    yAxisSplitLineColor: yAxisSplitLineColor.value
  }
  localStorage.setItem(storageKey, JSON.stringify(config))
}

// 从 localStorage 加载配置
const loadBoxPlotConfigLocally = () => {
  const configStr = localStorage.getItem(storageKey)
  if (configStr) {
    try {
      const config = JSON.parse(configStr)
      chartTitle.value = config.chartTitle || chartTitle.value
      titleColor.value = config.titleColor || titleColor.value
      titlePosition.value = config.titlePosition || titlePosition.value
      xAxisName.value = config.xAxisName || xAxisName.value
      yAxisName.value = config.yAxisName || yAxisName.value
      xAxisLineColor.value = config.xAxisLineColor || xAxisLineColor.value
      xAxisLabelColor.value = config.xAxisLabelColor || xAxisLabelColor.value
      yAxisLineColor.value = config.yAxisLineColor || yAxisLineColor.value
      yAxisLabelColor.value = config.yAxisLabelColor || yAxisLabelColor.value
      boxColor.value = config.boxColor || boxColor.value
      borderColor.value = config.borderColor || borderColor.value
      yAxisSplitLineColor.value = config.yAxisSplitLineColor || yAxisSplitLineColor.value
    } catch (error) {
      console.error("加载箱型图配置失败", error)
    }
  }
}

// 更新图表配置
const updateChart = () => {
  if (!props.rawData.length) return

  const categories = props.rawData.map(item => item.name)
  const boxData = props.rawData.map(item => [item.min, item.q1, item.median, item.q3, item.max])

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
    tooltip: { trigger: 'item' },
    grid: {
      top: 40,
      left: 10,
      right: 10,
      bottom: 30,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      name: xAxisName.value,
      nameLocation: 'center',
      data: categories,
      axisLine: { lineStyle: { color: xAxisLineColor.value } },
      axisLabel: { color: xAxisLabelColor.value }
    },
    yAxis: {
      type: 'value',
      name: yAxisName.value,
      axisLine: { lineStyle: { color: yAxisLineColor.value } },
      axisLabel: { color: yAxisLabelColor.value },
      splitLine: {
        show: true,
        lineStyle: {
          color: yAxisSplitLineColor.value, // 应用用户选择的分割线颜色
          width: 1,
          type: 'solid'
        }
      }
    },
    series: [
      {
        type: 'boxplot',
        data: boxData,
        itemStyle: {
          color: boxColor.value,
          borderColor: borderColor.value,
          borderWidth: 2
        },
        tooltip: {
          formatter: params =>
            `最小值: ${params.data[0]}<br>第一四分位数: ${params.data[1]}<br>中位数: ${params.data[2]}<br>第三四分位数: ${params.data[3]}<br>最大值: ${params.data[4]}`
        },
        animationEasing: 'cubicInOut',
        animationDuration: 1000
      }
    ]
  }

  chartInstance.value.setOption(option)
}

onMounted(() => {
  loadBoxPlotConfigLocally()
  chartInstance.value = echarts.init(chart.value)
  updateChart()
})

// 监听 rawData 变化
watch(() => props.rawData, () => {
  if (chartInstance.value) {
    updateChart()
  }
}, { deep: true })
</script>

<style scoped>
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

.chart {
  width: 100%;
  height: 100%;
  cursor: pointer;
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
