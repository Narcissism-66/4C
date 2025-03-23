<template>
  <div ref="chart" class="chart" @contextmenu.prevent="openDialog"></div>
  <!-- 配置弹窗 -->
  <div v-if="dialogVisible" class="modal-overlay">
    <div class="modal">
      <div class="modal-header">
        <h3>配置柱状图属性</h3>
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
            <label for="barColor">柱状图颜色：</label>
            <div class="color-input">
              <input id="barColor" v-model="barColor" type="color" />
              <span class="color-preview" :style="{ backgroundColor: barColor }"></span>
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
              <label for="yAxisLineColor">轴线颜色：</label>
              <div class="color-input">
                <input id="yAxisLineColor" v-model="yAxisLineColor" type="color" />
                <span class="color-preview" :style="{ backgroundColor: yAxisLineColor }"></span>
              </div>
            </div>
            <div class="input-group">
              <label for="yAxisSplitLineColor">分割线颜色：</label>
              <div class="color-input">
                <input id="yAxisSplitLineColor" v-model="yAxisSplitLineColor" type="color" />
                <span class="color-preview" :style="{ backgroundColor: yAxisSplitLineColor }"></span>
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
import { onMounted, ref, watch, defineProps } from 'vue';
import * as echarts from 'echarts';
import { userUserStore } from '@/stores/UserStore.js';

const chart = ref(null);
const chartInstance = ref(null);
const dialogVisible = ref(false);

// 配置项：图表标题及轴相关的配置
const chartTitle = ref('');
const titlePosition = ref('center'); // 可选 left、center、right
const titleColor = ref('#333333');
const xAxisName = ref('');
const yAxisName = ref('');
const xAxisLineColor = ref('#bdc3c7');
const yAxisLineColor = ref('#bdc3c7');
const xAxisLabelColor = ref('#2c3e50');
const yAxisLabelColor = ref('#7f8c8d');
const barColor = ref('#3498db');
const yAxisSplitLineColor = ref('#e0e0e0'); // 默认分割线颜色

const userStore = userUserStore();
// 定义一个唯一的 storageKey，结合当前用户 id，确保配置互不影响
const storageKey = `BarChartConfig-${userStore.user.id}`
// 保存当前图表配置到 LocalStorage
const saveChartConfigLocally = () => {
  const config = {
    chartTitle: chartTitle.value,
    titlePosition: titlePosition.value,
    titleColor: titleColor.value,
    xAxisName: xAxisName.value,
    yAxisName: yAxisName.value,
    xAxisLineColor: xAxisLineColor.value,
    yAxisLineColor: yAxisLineColor.value,
    xAxisLabelColor: xAxisLabelColor.value,
    yAxisLabelColor: yAxisLabelColor.value,
    barColor: barColor.value,
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
      xAxisName.value = config.xAxisName || ''
      yAxisName.value = config.yAxisName || ''
      xAxisLineColor.value = config.xAxisLineColor || '#bdc3c7'
      yAxisLineColor.value = config.yAxisLineColor || '#bdc3c7'
      xAxisLabelColor.value = config.xAxisLabelColor || '#2c3e50'
      yAxisLabelColor.value = config.yAxisLabelColor || '#7f8c8d'
      barColor.value = config.barColor || '#3498db'
      yAxisSplitLineColor.value = config.yAxisSplitLineColor || '#e0e0e0'
    } catch (error) {
      console.error("加载图表配置失败", error)
    }
  }
}

const props = defineProps({
  rawData: {
    type: Array,
    required: true,
    default: () => []
  },
});

// 右键打开弹窗
const openDialog = () => {
  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
  updateChart()
  saveChartConfigLocally();
}

console.log('收到的数据:', props.rawData);

// 图表初始化
onMounted(() => {
  chartInstance.value = echarts.init(chart.value);
  loadChartConfigLocally();
  updateChart();
});

// 更新图表配置
const updateChart = () => {
  if (!props.rawData?.length) return;

  let categories = [];
  let series = [];

  // 单数据集情况：判断第一条数据是否具有 name 属性
  if (props.rawData[0]?.name) {
    categories = props.rawData.map(item => item.name);
    series = [{
      name: '数据集1',
      type: 'bar',
      data: props.rawData.map(item => item.value),
      itemStyle: {
        color: barColor.value,
        shadowBlur: 10,
        shadowColor: 'rgba(52, 152, 219, 0.5)',
        shadowOffsetX: 3,
        shadowOffsetY: 3,
        borderRadius: [12, 12, 0, 0],
      },
      label: {
        show: true,
        position: 'top',
        fontSize: 14,
        color: '#333',
        fontWeight: 'bold',
      },
      barBorderRadius: [10, 10, 0, 0],
    }];
  } else { // 多数据集情况
    categories = [...new Set(props.rawData.flatMap(d => d.data.map(i => i.name)))];
    series = props.rawData.map((d, index) => {
      const values = categories.map(cat => {
        const item = d.data.find(i => i.name === cat);
        return item ? item.value : 0;
      });
      return {
        name: d.NAME,
        type: 'bar',
        data: values,
        itemStyle: {
          color: barColor.value,
          shadowBlur: 10,
          shadowColor: 'rgba(52, 152, 219, 0.5)',
          shadowOffsetX: 3,
          shadowOffsetY: 3,
          borderRadius: [12, 12, 0, 0],
        },
        label: {
          show: true,
          position: 'top',
          fontSize: 14,
          color: '#333',
          fontWeight: 'bold',
        },
        barBorderRadius: [10, 10, 0, 0],
      };
    });
  }

  // ECharts 配置项：增加标题设置，使用弹窗中设置的各项配置
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
      borderRadius: 6
    },
    grid: {
      top: 80,
      left: 10,
      right: 10,
      bottom: 30,
      containLabel: true
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
      data: categories,
      axisLine: {
        lineStyle: {
          color: xAxisLineColor.value,
          width: 2,
        },
      },
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
      axisLabel: {
        color: yAxisLabelColor.value,
        fontSize: 12,
      },
      axisLine: {
        lineStyle: {
          color: yAxisLineColor.value,
          width: 2,
        },
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
    series,
  };

  chartInstance.value.setOption(option);
};

watch(
  () => props.rawData,
  () => {
    if (chartInstance.value) {
      updateChart();
    }
  },
  { deep: true }
);
</script>

<style scoped>
/* 关闭按钮：绝对定位在右上角 */
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
/* 新增样式 */
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

/* 调整原有样式 */
.modal {
  width: 600px; /* 增大弹窗宽度 */
  padding: 25px;
}

button {
  margin-top: 20px;
  width: auto;
  padding: 10px 30px;
  display: block;
  margin-left: auto;
  margin-right: auto;
}

.input-group {
  margin-bottom: 15px;
}

.input-group label {
  min-width: 80px;
  margin-bottom: 8px;
}
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
}
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
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
/* 放大弹窗 */
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
/* 显示颜色预览框 */
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
</style>
