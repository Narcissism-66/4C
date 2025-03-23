<template>
  <div ref="chart" class="chart" @contextmenu.prevent="openDialog"></div>
  <!-- 配置弹窗 -->
  <div v-if="dialogVisible" class="modal-overlay">
    <div class="modal">
      <!-- 弹窗头部 -->
      <div class="modal-header">
        <h3>设置图表配置</h3>
        <span class="close-btn" @click="closeDialog">×</span>
      </div>
      <!-- 弹窗主体：左侧配置标题，右侧配置半径 -->
      <div class="config-columns">
        <!-- 左侧：标题相关配置 -->
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
        <!-- 右侧：半径配置 -->
        <div class="config-right">
          <div class="input-group">
            <label for="innerRadius">内半径：</label>
            <input id="innerRadius" v-model="innerRadius" placeholder="例如 30%" />
          </div>
          <div class="input-group">
            <label for="outerRadius">外半径：</label>
            <input id="outerRadius" v-model="outerRadius" placeholder="例如 50%" />
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
import { userUserStore } from '@/stores/UserStore.js'

// 图表 DOM 引用及实例
const chart = ref(null)
const chartInstance = ref(null)
// 弹窗显示状态
const dialogVisible = ref(false)

// 配置项：标题相关
const chartTitle = ref('默认标题')
const titlePosition = ref('center')
const titleColor = ref('#333333')
// 配置项：半径相关
const innerRadius = ref('30%')
const outerRadius = ref('50%')

// 定义存储 key（如果需要结合用户信息，可在此处动态生成）
const userStore = userUserStore()
const storageKey = `PieChartConfig-${userStore.user.id}`

// 右键打开弹窗
const openDialog = () => {
  dialogVisible.value = true
}

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
    innerRadius: innerRadius.value,
    outerRadius: outerRadius.value
  }
  localStorage.setItem(storageKey, JSON.stringify(config))
}

// 从 localStorage 加载配置
const loadChartConfigLocally = () => {
  const configStr = localStorage.getItem(storageKey)
  if (configStr) {
    try {
      const config = JSON.parse(configStr)
      chartTitle.value = config.chartTitle || chartTitle.value
      titlePosition.value = config.titlePosition || titlePosition.value
      titleColor.value = config.titleColor || titleColor.value
      innerRadius.value = config.innerRadius || innerRadius.value
      outerRadius.value = config.outerRadius || outerRadius.value
    } catch (error) {
      console.error("加载图表配置失败", error)
    }
  }
}

// 父组件传入的数据（饼图数据示例格式：[{ name: 'A', value: 10 }, ...]）
const props = defineProps({
  rawData: {
    type: Array,
    required: true,
    default: () => []
  }
})

// 图表初始化及更新
onMounted(() => {
  loadChartConfigLocally()
  chartInstance.value = echarts.init(chart.value)
  updateChart()
})

const updateChart = () => {
  if (!props.rawData.length) return

  const pieData = props.rawData.map(item => ({
    name: item.name,
    value: item.value
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
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#ccc',
      borderWidth: 1,
      padding: [10, 15],
      textStyle: {
        fontSize: 14,
        color: '#333'
      },
      formatter: params => `${params.name}: ${params.value} (${params.percent}%)`
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      left: 'center',
      textStyle: {
        fontSize: 12,
        color: '#333'
      }
    },
    series: [
      {
        name: '饼图',
        type: 'pie',
        // 使用配置的内外半径
        radius: [innerRadius.value, outerRadius.value],
        data: pieData,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          position: 'outside',
          formatter: '{b}: {c} ({d}%)',
          color: '#333',
          fontSize: 12
        },
        labelLine: {
          show: true,
          length: 15,
          length2: 8,
          lineStyle: { color: '#333' }
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.3)',
            borderWidth: 4,
            borderColor: '#fff'
          },
          label: {
            fontSize: 14,
            fontWeight: 'bold',
            color: '#fff'
          }
        },
        animationType: 'scale',
        animationEasing: 'cubicOut',
        animationDuration: 2000,
        animationDelay: index => index * 150,
        animationDelayUpdate: index => index * 150,
        color: ['#FF6F61', '#6B5B95', '#88B04B', '#F7CAC9', '#92A8D1']
      }
    ]
  }

  chartInstance.value.setOption(option)
}

// 当原始数据或配置项变化时，更新图表
watch(() => props.rawData, () => {
  updateChart()
}, { deep: true })

watch([chartTitle, titlePosition, titleColor, innerRadius, outerRadius], () => {
  updateChart()
})
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

/* 弹窗主体 */
.modal {
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

/* 关闭按钮 */
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
