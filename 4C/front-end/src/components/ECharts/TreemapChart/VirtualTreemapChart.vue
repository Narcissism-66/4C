<template>
  <div class="chart-container">
    <div class="header">
      <h3 class="title">🌟 动态数据图表</h3>
      <div class="toolbar">
        <button class="toolbar-btn" @click="downloadChart">📥 下载图表</button>
        <button class="toolbar-btn" @click="resetZoom">🔄 重置缩放</button>
        <select class="toolbar-select" v-model="chartType" @change="updateChart">
          <option value="boxplot">箱型图</option>
          <option value="line">折线图</option>
          <option value="bar">柱状图</option>
        </select>
        <button class="settings-btn" @click="showDialog = true">⚙️ 坐标轴设置</button>
      </div>
    </div>
    <div ref="chart" class="chart"></div>

    <!-- Coordinate axis settings modal -->
    <div v-if="showDialog" class="modal-mask">
      <div class="modal-wrapper">
        <div class="modal-container">
          <h3>📐 坐标轴设置</h3>
          <div class="form-item">
            <label class="input-label">X轴含义：</label>
            <input v-model="xAxisName" type="text" placeholder="请输入X轴说明">
          </div>
          <div class="form-item">
            <label class="input-label">Y轴含义：</label>
            <input v-model="yAxisName" type="text" placeholder="请输入Y轴说明">
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
const xAxisName = ref('X 轴')
const yAxisName = ref('Y 轴')
const chartType = ref('boxplot')

const props = defineProps({
  rawData: { type: Array, required: true, default: () => [] }
})

const generateDynamicData = () => {
  // 生成一些动态数据，模拟波动趋势
  const categories = ['数据1', '数据2', '数据3', '数据4', '数据5'];
  const data = categories.map(() => {
    return {
      name: 'Item',
      min: Math.random() * 50,
      q1: Math.random() * 60,
      median: Math.random() * 70,
      q3: Math.random() * 80,
      max: Math.random() * 100
    }
  });

  return data;
}

const updateChart = () => {
  if (!props.rawData.length) return;
  const categories = props.rawData.map(item => item.name);
  const boxData = props.rawData.map(item => [item.min, item.q1, item.median, item.q3, item.max]);

  let option = {
    title: {
      text: '🌟 数据分析',
      left: 'center',
      textStyle: { fontSize: 20, fontWeight: 'bold', color: '#2c3e50' }
    },
    tooltip: { trigger: 'item' },
    xAxis: {
      type: 'category',
      name: xAxisName.value,
      data: categories,
      axisLine: { lineStyle: { color: '#2980b9' } },
      axisLabel: { color: '#7f8c8d' }
    },
    yAxis: {
      type: 'value',
      name: yAxisName.value,
      axisLine: { lineStyle: { color: '#2980b9' } },
      axisLabel: { color: '#7f8c8d' }
    },
    series: []
  };

  // Boxplot
  if (chartType.value === 'boxplot') {
    option.series.push({
      type: 'boxplot',
      data: boxData,
      itemStyle: {
        color: '#3498db',
        borderColor: '#2980b9'
      },
      tooltip: {
        formatter: (params) => {
          return `最小值: ${params.data[0]}<br>第一四分位数: ${params.data[1]}<br>中位数: ${params.data[2]}<br>第三四分位数: ${params.data[3]}<br>最大值: ${params.data[4]}`;
        }
      },
      animationEasing: 'cubicInOut',
      animationDuration: 1500,
      animationDurationUpdate: 1000
    });
  } else if (chartType.value === 'line') {
    option.series.push({
      type: 'line',
      data: boxData.map(d => d[2]),
      lineStyle: { color: '#e74c3c', width: 3 },
      smooth: true,
      animationDuration: 2000,
      animationEasing: 'elasticOut',
      itemStyle: { color: '#e74c3c' }
    });
  } else if (chartType.value === 'bar') {
    option.series.push({
      type: 'bar',
      data: boxData.map(d => d[2]),
      itemStyle: {
        color: '#1abc9c',
        borderColor: '#16a085',
        borderWidth: 1
      },
      animationDuration: 2000,
      animationEasing: 'bounceOut',
      animationDurationUpdate: 1500
    });
  }

  chartInstance.value.setOption(option);
}

// Initialize chart instance
onMounted(() => {
  chartInstance.value = echarts.init(chart.value);
  updateChart();
})

// Update chart on raw data change
watch(
  () => props.rawData,
  () => {
    if (chartInstance.value) {
      updateChart();
    }
  },
  { deep: true }
);

// Apply axis names
const applyAxisNames = () => {
  showDialog.value = false;
  updateChart();
}
</script>

<style scoped>
.chart-container {
  position: relative;
  width: 100%;
  height: 100%;
  background: linear-gradient(45deg, #f0f8ff, #d1ecf1);
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: fadeIn 2s ease-out;
}

@keyframes fadeIn {
  0% { opacity: 0; }
  100% { opacity: 1; }
}

.chart {
  width: 100%;
  flex-grow: 1;
  transition: all 0.5s ease;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
}

.title {
  margin: 0;
  color: #2c3e50;
  font-size: 1.8rem;
  font-weight: 600;
  font-family: 'Arial', sans-serif;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 20px;
}

.toolbar-btn,
.settings-btn {
  padding: 12px 18px;
  background: linear-gradient(145deg, #3498db, #2980b9);
  color: white;
  border: none;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.toolbar-btn:hover,
.settings-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 10px 15px rgba(52, 152, 219, 0.5);
}

.toolbar-select {
  padding: 10px;
  border: 2px solid #2980b9;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s ease;
}

.toolbar-select:focus {
  border-color: #e74c3c;
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-wrapper {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.2);
}

.modal-container {
  width: 400px;
}

.input-label {
  font-size: 14px;
  margin-bottom: 8px;
}

.input {
  width: 100%;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #bdc3c7;
}

.form-item {
  margin-bottom: 20px;
}

.button-group {
  display: flex;
  justify-content: space-between;
}

.confirm-btn,
.cancel-btn {
  padding: 12px 18px;
  background: #2ecc71;
  color: white;
  border: none;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.confirm-btn:hover {
  background: #27ae60;
}

.cancel-btn {
  background: #e74c3c;
}

.cancel-btn:hover {
  background: #c0392b;
}
</style>
