<template>
  <!-- 图表区域：整个界面充满图表，并绑定右键打开配置弹窗 -->
  <div ref="chartRef" class="chart" @contextmenu.prevent="openDialog"></div>

  <!-- 配置弹窗 -->
  <div v-if="dialogVisible" class="modal-overlay">
    <div class="modal">
      <div class="modal-header">
        <h3>图表配置</h3>
        <span class="close-btn" @click="closeDialog">×</span>
      </div>

      <div class="config-columns">
        <!-- 左侧：全局设置 & 折线颜色设置 -->
        <div class="config-left">
          <div class="config-section">
            <h4>全局设置</h4>
            <div class="input-group">
              <label>图表标题</label>
              <input v-model="chartOptions.title.text" placeholder="请输入图表标题" />
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
            <h4>折线颜色设置</h4>
            <div class="color-range">
              <div class="color-item" v-for="(color, index) in chartOptions.seriesColors" :key="index">
                <input type="color" v-model="chartOptions.seriesColors[index]" />
                <span class="color-preview" :style="{ backgroundColor: color }"></span>
                <button v-if="chartOptions.seriesColors.length > 1" @click="removeColor(index)" class="btn-remove">×</button>
              </div>
              <button @click="addColor" class="btn-add">+ 添加颜色</button>
            </div>
          </div>
        </div>

        <!-- 右侧：坐标轴设置 -->
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
            <div class="input-group">
              <label>轴名称</label>
              <input v-model="chartOptions.yAxis.name" placeholder="请输入Y轴名称" />
            </div>
            <div class="input-group">
              <label>文字颜色</label>
              <div class="color-picker">
                <input type="color" v-model="chartOptions.yAxis.axisLabel.color" />
                <span class="color-preview" :style="{ backgroundColor: chartOptions.yAxis.axisLabel.color }"></span>
              </div>
            </div>
            <div class="input-group">
              <label for="yAxisSplitLineColor">分割线颜色：</label>
              <div class="color-picker">
                <input id="yAxisSplitLineColor" v-model="yAxisSplitLineColor" type="color" />
                <span class="color-preview" :style="{ backgroundColor: yAxisSplitLineColor }"></span>
              </div>
            </div>
            <div class="input-group">
              <label>轴线颜色</label>
              <div class="color-picker">
                <input type="color" v-model="chartOptions.yAxis.axisLine.lineStyle.color" />
                <span class="color-preview" :style="{ backgroundColor: chartOptions.yAxis.axisLine.lineStyle.color }"></span>
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
import { ref, reactive, onMounted, watch, onBeforeUnmount, defineProps } from 'vue';
import * as echarts from 'echarts';
import { userUserStore } from '@/stores/UserStore.js';

const chartRef = ref(null);
let myChart = null;
const dialogVisible = ref(false);
const yAxisSplitLineColor = ref('#e0e0e0'); // 默认分割线颜色

const props = defineProps({
  rawData: {
    type: Array,
    required: true
  }
});

// 获取用户信息，构造存储 key
const userStore = userUserStore();
const storageKey = `LineChartConfig-${userStore.user.id}`;

// 定义图表配置对象
const chartOptions = reactive({
  title: {
    text: '图表标题',
    textStyle: { color: '#333', fontSize: 18 },
    left: 'left'
  },
  xAxis: {
    name: 'X轴',
    type: 'category',
    axisLabel: { color: '#374151' },
    axisLine: { lineStyle: { color: '#666' } },
    data: []
  },
  yAxis: {
    name: 'Y轴',
    type: 'value',
    axisLabel: { color: '#374151' },
    axisLine: { lineStyle: { color: '#666' } },
    // 初始化分割线配置
    splitLine: {
      show: true,
      lineStyle: {
        color: yAxisSplitLineColor.value,
        width: 1,
        type: 'solid'
      }
    }
  },
  // 折线颜色配置
  seriesColors: [
    '#5470c6'
  ]
});

// 监听分割线颜色变化，更新配置并重新绘制图表
watch(yAxisSplitLineColor, (newColor) => {
  console.log("分割线颜色改变为：", newColor);
  // 确保 splitLine 及其 lineStyle 存在
  if (!chartOptions.yAxis.splitLine || !chartOptions.yAxis.splitLine.lineStyle) {
    chartOptions.yAxis.splitLine = {
      show: true,
      lineStyle: {
        color: newColor,
        width: 1,
        type: 'solid'
      }
    };
  } else {
    chartOptions.yAxis.splitLine.lineStyle.color = newColor;
  }
  updateChart(props.rawData);
});

// 折线颜色操作：添加、删除
const addColor = () => {
  chartOptions.seriesColors.push('#000000');
};
const removeColor = (index) => {
  if (chartOptions.seriesColors.length > 1) {
    chartOptions.seriesColors.splice(index, 1);
  }
};

// 右键打开配置弹窗
const openDialog = () => {
  dialogVisible.value = true;
};

// 保存配置到 localStorage
const saveChartConfigLocally = () => {
  localStorage.setItem(storageKey, JSON.stringify(chartOptions));
};

// 从 localStorage 加载配置，并合并到当前配置
const loadChartConfigLocally = () => {
  const savedConfig = localStorage.getItem(storageKey);
  if (savedConfig) {
    try {
      const parsedConfig = JSON.parse(savedConfig);
      Object.assign(chartOptions, parsedConfig);
      if (parsedConfig.yAxis && parsedConfig.yAxis.splitLine && parsedConfig.yAxis.splitLine.lineStyle) {
        yAxisSplitLineColor.value = parsedConfig.yAxis.splitLine.lineStyle.color;
      }
    } catch (error) {
      console.error("加载图表配置失败", error);
    }
  }
};

const closeDialog = () => {
  dialogVisible.value = false;
  updateChart(props.rawData);
  saveChartConfigLocally();
};

const updateChart = (rawData) => {
  if (!rawData || !Array.isArray(rawData) || rawData.length === 0) {
    console.error("Invalid rawData");
    return;
  }

  // 如果 splitLine 或其 lineStyle 丢失，重新初始化
  if (!chartOptions.yAxis.splitLine || !chartOptions.yAxis.splitLine.lineStyle) {
    chartOptions.yAxis.splitLine = {
      show: true,
      lineStyle: {
        color: yAxisSplitLineColor.value,
        width: 1,
        type: 'solid'
      }
    };
  }
  console.log("更新图表，当前分割线颜色：", chartOptions.yAxis.splitLine.lineStyle.color);

  // 如果 rawData 格式为单一数据（含 date 字段），则转换格式
  const formattedData = rawData[0].date ? [{ NAME: '数据', data: rawData }] : rawData;
  // 提取所有唯一日期
  const allDates = [...new Set(formattedData.flatMap(dataset => dataset.data.map(d => d.date)))];
  // 构建各系列数据，使用配置中的 seriesColors 作为折线颜色
  const seriesData = formattedData.map((dataset, index) => ({
    name: dataset.NAME,
    type: 'line',
    smooth: true,
    symbol: 'circle',
    symbolSize: 8,
    lineStyle: {
      width: 3,
      color: chartOptions.seriesColors[index % chartOptions.seriesColors.length],
      shadowColor: 'rgba(0,0,0,0.2)',
      shadowBlur: 10,
      shadowOffsetY: 8
    },
    itemStyle: {
      color: chartOptions.seriesColors[index % chartOptions.seriesColors.length]
    },
    data: allDates.map(date => {
      const found = dataset.data.find(item => item.date === date);
      return found ? found.value : null;
    })
  }));

  const option = {
    title: chartOptions.title,
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(50,50,50,0.9)',
      borderWidth: 0,
      padding: [10, 15],
      textStyle: { color: '#fff', fontSize: 14 },
      axisPointer: {
        type: 'cross',
        label: { backgroundColor: '#6a7985' }
      },
      formatter: (params) => {
        let res = `<div style="margin-bottom:5px">${params[0].axisValue}</div>`;
        params.forEach(item => {
          res += `<div style="display:flex;align-items:center;margin:5px 0">
                    <span style="display:inline-block;width:10px;height:10px;background:${item.color};border-radius:50%;margin-right:8px"></span>
                    ${item.seriesName}: ${item.value ?? '无数据'}
                  </div>`;
        });
        return res;
      }
    },
    legend: {
      type: 'scroll',
      top: 0,
      icon: 'roundRect',
      itemWidth: 16,
      itemHeight: 8,
      textStyle: {
        rich: { name: { verticalAlign: 'right' } }
      }
    },
    grid: {
      top: 50,
      left: 10,
      right: 40,
      bottom: 30,
      containLabel: true
    },
    xAxis: {
      ...chartOptions.xAxis,
      data: allDates
    },
    yAxis: chartOptions.yAxis,
    animation: true,
    animationDuration: 2000,
    animationEasing: 'cubicInOut',
    series: seriesData
  };

  // 延时更新，确保图表平滑刷新
  setTimeout(() => {
    if (myChart) {
      myChart.clear();
      myChart.setOption(option, true);
    }
  }, 10);
};

let resizeHandler = null;
onMounted(() => {
  // 加载本地保存的配置
  loadChartConfigLocally();
  if (chartRef.value) {
    myChart = echarts.init(chartRef.value, 'light', { renderer: 'canvas' });
    updateChart(props.rawData);
    resizeHandler = () => myChart.resize();
    window.addEventListener('resize', resizeHandler);
  }
});

watch(() => props.rawData, (newVal) => {
  updateChart(newVal);
});

onBeforeUnmount(() => {
  if (myChart) {
    window.removeEventListener('resize', resizeHandler);
    myChart.dispose();
  }
});
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
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  padding: 30px;
  width: 600px;
  max-width: 90%;
  animation: fadeInScale 0.3s ease-in-out;
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

.color-range {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.color-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-add,
.btn-remove {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-add {
  background: #f0f4f8;
  color: #4a5568;
}

.btn-remove {
  background: #fee2e2;
  color: #dc2626;
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
