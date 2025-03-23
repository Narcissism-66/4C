<template>
  <div class="chart-container">
    <div ref="chart" class="chart" @contextmenu.prevent="openDialog"></div>
  </div>
  <!-- 配置弹窗 -->
  <div v-if="dialogVisible" class="modal-overlay">
    <div class="modal">
      <!-- 弹窗头部：标题和关闭按钮 -->
      <div class="modal-header">
        <h3>设置散点图配置</h3>
        <span class="close-btn" @click="closeDialog">×</span>
      </div>
      <!-- 弹窗主体：左右两列 -->
      <div class="config-columns">
        <!-- 左侧：图表标题配置 -->
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
        <!-- 右侧：轴与散点系列配置 -->
        <div class="config-right">
          <div class="input-group">
            <label for="xAxisName">X轴名称：</label>
            <input id="xAxisName" v-model="xAxisName" placeholder="请输入X轴名称" />
          </div>
          <div class="input-group">
            <label for="xAxisSplitLineColor">X轴分割线颜色：</label>
            <div class="color-input">
              <input id="xAxisSplitLineColor" v-model="xAxisSplitLineColor" type="color" />
              <span class="color-preview" :style="{ backgroundColor: xAxisSplitLineColor }"></span>
            </div>
          </div>
          <div class="input-group">
            <label for="yAxisName">Y轴名称：</label>
            <input id="yAxisName" v-model="yAxisName" placeholder="请输入Y轴名称" />
          </div>
          <div class="input-group">
            <label for="yAxisSplitLineColor">Y轴分割线颜色：</label>
            <div class="color-input">
              <input id="yAxisSplitLineColor" v-model="yAxisSplitLineColor" type="color" />
              <span class="color-preview" :style="{ backgroundColor: yAxisSplitLineColor }"></span>
            </div>
          </div>
          <div class="input-group">
            <label for="symbolSize">散点大小：</label>
            <input id="symbolSize" v-model.number="symbolSize" type="number" min="1" />
          </div>
          <div class="input-group">
            <label for="scatterColorStart">散点颜色起始：</label>
            <div class="color-input">
              <input id="scatterColorStart" v-model="scatterColorStart" type="color" />
              <span class="color-preview" :style="{ backgroundColor: scatterColorStart }"></span>
            </div>
          </div>
          <div class="input-group">
            <label for="scatterColorEnd">散点颜色结束：</label>
            <div class="color-input">
              <input id="scatterColorEnd" v-model="scatterColorEnd" type="color" />
              <span class="color-preview" :style="{ backgroundColor: scatterColorEnd }"></span>
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
import { ref, onMounted, watch, defineProps } from 'vue';
import * as echarts from 'echarts';
import { userUserStore } from '@/stores/UserStore.js'

// 接收父组件传入的散点数据（格式：[{ x, y }, …]）
const props = defineProps({
  rawData: {
    type: Array,
    required: true,
    default: () => []
  }
});

// 图表与弹窗相关变量
const chart = ref(null);
const chartInstance = ref(null);
const dialogVisible = ref(false);

// 配置属性
const chartTitle = ref('散点图标题');
const titleColor = ref('#333333');
const titlePosition = ref('center');

const xAxisName = ref('X轴');
const yAxisName = ref('Y轴');

const symbolSize = ref(10);
const scatterColorStart = ref('#ff6347');
const scatterColorEnd = ref('#ff1493');
const yAxisSplitLineColor = ref('#e0e0e0'); // 默认分割线颜色
const xAxisSplitLineColor = ref('#e0e0e0'); // 默认分割线颜色

// 定义存储 key（可根据需要结合用户信息扩展，此处直接使用固定 key）
const userStore = userUserStore();
const storageKey = `ScatterChartConfig-${userStore.user.id}`;

// 右键打开弹窗
const openDialog = () => {
  dialogVisible.value = true;
}

// 关闭弹窗时更新图表并保存配置
const closeDialog = () => {
  dialogVisible.value = false;
  updateChart();
  saveScatterConfigLocally();
}

// 保存配置到 localStorage
const saveScatterConfigLocally = () => {
  const config = {
    chartTitle: chartTitle.value,
    titleColor: titleColor.value,
    titlePosition: titlePosition.value,
    xAxisName: xAxisName.value,
    yAxisName: yAxisName.value,
    symbolSize: symbolSize.value,
    scatterColorStart: scatterColorStart.value,
    scatterColorEnd: scatterColorEnd.value,
    yAxisSplitLineColor: yAxisSplitLineColor.value,
    xAxisSplitLineColor: xAxisSplitLineColor.value,
  };
  localStorage.setItem(storageKey, JSON.stringify(config));
}

// 从 localStorage 加载配置
const loadScatterConfigLocally = () => {
  const configStr = localStorage.getItem(storageKey);
  if (configStr) {
    try {
      const config = JSON.parse(configStr);
      chartTitle.value = config.chartTitle || chartTitle.value;
      titleColor.value = config.titleColor || titleColor.value;
      titlePosition.value = config.titlePosition || titlePosition.value;
      xAxisName.value = config.xAxisName || xAxisName.value;
      yAxisName.value = config.yAxisName || yAxisName.value;
      symbolSize.value = config.symbolSize || symbolSize.value;
      scatterColorStart.value = config.scatterColorStart || scatterColorStart.value;
      scatterColorEnd.value = config.scatterColorEnd || scatterColorEnd.value;
      yAxisSplitLineColor.value = config.yAxisSplitLineColor || yAxisSplitLineColor.value;
      xAxisSplitLineColor.value = config.xAxisSplitLineColor || xAxisSplitLineColor.value;
    } catch (error) {
      console.error("加载散点图配置失败", error);
    }
  }
}

// 更新图表配置
const updateChart = () => {
  // 将 rawData 转换为二维数组：[[x, y], ...]
  const data = props.rawData.map(item => [item.x, item.y]);
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
      formatter: params => `X: ${params.value[0]}<br/>Y: ${params.value[1]}`
    },
    grid: {
      top: 40,
      left: 10,
      right: 20,
      bottom: 30,
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: xAxisName.value,
      nameLocation: 'center',
      nameGap: 30,
      nameTextStyle: {
        fontWeight: 'bold',
        fontSize: 14,
        color: '#2c3e50'
      },
      axisLabel: {
        fontSize: 12,
        color: '#2f4f4f'
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: xAxisSplitLineColor.value, // 应用用户选择的分割线颜色
          width: 1,
          type: 'solid'
        }
      }
    },
    yAxis: {
      type: 'value',
      name: yAxisName.value,
      nameLocation: 'end',
      nameTextStyle: {
        fontWeight: 'bold',
        fontSize: 14,
        color: '#2c3e50'
      },
      axisLabel: {
        fontSize: 12,
        color: '#2f4f4f'
      },
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
        data: data,
        type: 'scatter',
        symbolSize: symbolSize.value,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
            { offset: 0, color: scatterColorStart.value },
            { offset: 1, color: scatterColorEnd.value }
          ]),
          borderColor: '#fff',
          borderWidth: 1
        }
      }
    ]
  };

  if (chartInstance.value) {
    chartInstance.value.setOption(option);
  } else {
    chartInstance.value = echarts.init(chart.value);
    chartInstance.value.setOption(option);
  }
};

onMounted(() => {
  loadScatterConfigLocally();
  updateChart();
});

// 监听 rawData 变化，实时更新图表
watch(() => props.rawData, () => {
  updateChart();
}, { deep: true });
</script>

<style scoped>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
}

.chart-container {
  width: 100%;
  height: 100%;
}

/* 图表样式 */
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

/* 配置区域 */
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
