<template>
  <div class="chart-container">
    <div ref="chart" class="chart"></div>
  </div>
</template>

<script setup>
import { onMounted, ref, nextTick } from 'vue'
import * as echarts from 'echarts'

// 先声明 generateDynamicData 函数
const generateDynamicData = () => {
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

const chart = ref(null)
const chartInstance = ref(null)
const chartData = ref(generateDynamicData()) // 初始数据
const chartType = ref('boxplot') // 定义并初始化 chartType

const updateChart = () => {
  const categories = chartData.value.map(item => item.name);
  const boxData = chartData.value.map(item => [item.min, item.q1, item.median, item.q3, item.max]);

  let option = {
    title: {
      text: '箱型图',
      left: 'center',
      textStyle: { fontSize: 22, fontWeight: 'bold', color: '#3498db' } // 蓝色标题
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(0, 0, 0, 0.7)',
      textStyle: { color: '#fff' },
      formatter: (params) => {
        return `
          <div>
            <strong>项: ${params.name}</strong><br>
            最小值: ${params.data[0]}<br>
            第一四分位数: ${params.data[1]}<br>
            中位数: ${params.data[2]}<br>
            第三四分位数: ${params.data[3]}<br>
            最大值: ${params.data[4]}
          </div>
        `;
      }
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLine: { lineStyle: { color: '#3498db' } }, // 蓝色坐标轴
      axisLabel: { color: '#7f8c8d' },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#3498db' } }, // 蓝色坐标轴
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
        color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
          { offset: 0, color: '#8e44ad' }, // 渐变紫色
          { offset: 1, color: '#3498db' }  // 渐变蓝色
        ])
      },
      tooltip: {
        formatter: (params) => {
          return `最小值: ${params.data[0]}<br>第一四分位数: ${params.data[1]}<br>中位数: ${params.data[2]}<br>第三四分位数: ${params.data[3]}<br>最大值: ${params.data[4]}`;
        }
      },
      animationEasing: 'cubicInOut',
      animationDuration: 1500
    });
  } else if (chartType.value === 'line') {
    option.series.push({
      type: 'line',
      data: boxData.map(d => d[2]),
      lineStyle: { color: '#3498db', width: 2, type: 'dashed' }, // 蓝色线条
      smooth: true,
      animationDuration: 1500,
      animationEasing: 'easeInOutBack'
    });
  } else if (chartType.value === 'bar') {
    option.series.push({
      type: 'bar',
      data: boxData.map(d => d[2]),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#3498db' }, // 渐变蓝色
          { offset: 1, color: '#1abc9c' }  // 渐变绿色
        ])
      },
      animationDuration: 1200
    });
  }

  chartInstance.value.setOption(option);
}

// Initialize chart instance
onMounted(() => {
  // Ensure DOM is updated before initializing chart
  nextTick(() => {
    if (chart.value) {
      chartInstance.value = echarts.init(chart.value);
      updateChart(); // Initial render of the chart
    }
  });

  // Update data every second
  setInterval(() => {
    chartData.value = generateDynamicData(); // Update data
    updateChart(); // Update chart
  }, 1000);
})
</script>

<style scoped>
.chart-container {
  position: relative;
  width: 100%;
  height: 100%;
  background: #f8f9fa; /* 白色背景 */
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
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
}

.title {
  margin: 0;
  color: #3498db; /* 蓝色标题 */
  font-size: 1.5rem;
  font-weight: 600;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 20px;
}

.toolbar-btn,
.settings-btn {
  padding: 10px 18px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.toolbar-btn:hover,
.settings-btn:hover {
  background: #2980b9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.toolbar-select {
  padding: 10px;
  border: 1px solid #bdc3c7;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s ease;
}

.toolbar-select:focus {
  border-color: #3498db;
}
</style>
