<template>
  <div class="chart-container">
    <div ref="chart" class="chart" @contextmenu.prevent="openDialog"></div>
  </div>
  <!-- 配置弹窗 -->
  <div v-if="dialogVisible" class="modal-overlay">
    <div class="modal">
      <!-- 弹窗头部 -->
      <div class="modal-header">
        <h3>设置树图配置</h3>
        <span class="close-btn" @click="closeDialog">×</span>
      </div>
      <!-- 弹窗主体：左右两列 -->
      <div class="config-columns">
        <!-- 左侧：整体配置 -->
        <div class="config-left">
          <div class="input-group">
            <label for="chartTitle">图表标题：</label>
            <input id="chartTitle" v-model="chartTitle" placeholder="请输入图表标题" />
          </div>
          <div class="input-group">
            <label for="titlePosition">标题位置：</label>
            <select id="titlePosition" v-model="titlePosition">
              <option value="left">左对齐</option>
              <option value="center">居中</option>
              <option value="right">右对齐</option>
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
            <label for="layout">布局方式：</label>
            <select id="layout" v-model="layout">
              <option value="orthogonal">直角布局</option>
              <option value="radial">径向布局</option>
            </select>
          </div>
          <div class="input-group">
            <label for="initialTreeDepth">初始树层级：</label>
            <input id="initialTreeDepth" v-model.number="initialTreeDepth" type="number" min="0" />
          </div>
        </div>
        <!-- 右侧：样式配置 -->
        <div class="config-right">
          <div class="input-group">
            <label for="symbol">节点形状：</label>
            <select id="symbol" v-model="symbol">
              <option value="circle">圆形</option>
              <option value="rect">矩形</option>
              <option value="roundRect">圆角矩形</option>
              <option value="triangle">三角形</option>
            </select>
          </div>
          <div class="input-group">
            <label for="symbolSize">节点大小：</label>
            <input id="symbolSize" v-model.number="symbolSize" type="number" min="1" />
          </div>
          <div class="input-group">
            <label for="nodeBorderColor">节点边框颜色：</label>
            <div class="color-input">
              <input id="nodeBorderColor" v-model="nodeBorderColor" type="color" />
              <span class="color-preview" :style="{ backgroundColor: nodeBorderColor }"></span>
            </div>
          </div>
          <div class="input-group">
            <label for="lineColor">连线颜色：</label>
            <div class="color-input">
              <input id="lineColor" v-model="lineColor" type="color" />
              <span class="color-preview" :style="{ backgroundColor: lineColor }"></span>
            </div>
          </div>
          <div class="input-group">
            <label for="labelFontSize">标签字体大小：</label>
            <input id="labelFontSize" v-model.number="labelFontSize" type="number" min="10" />
          </div>
          <div class="input-group">
            <label for="labelColor">标签颜色：</label>
            <div class="color-input">
              <input id="labelColor" v-model="labelColor" type="color" />
              <span class="color-preview" :style="{ backgroundColor: labelColor }"></span>
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
import { onMounted, ref, watch, defineProps, nextTick } from 'vue';
import * as echarts from 'echarts';
import { userUserStore } from '@/stores/UserStore.js';

const chart = ref(null);
const chartInstance = ref(null);
const dialogVisible = ref(false);
const userStore = userUserStore();

const props = defineProps({
  // 期望数据格式：[ { name, parent, value }, … ]
  rawData: { type: Array, required: true, default: () => [] }
});

// 存储 key（可结合用户ID）
const storageKey = `TreeChartConfig-${userStore.user.id}`;

// 左侧整体配置
const chartTitle = ref('树图标题');
const titlePosition = ref('center');
const titleColor = ref('#333');
const layout = ref('orthogonal');
const initialTreeDepth = ref(2);

// 右侧样式配置
const symbol = ref('circle');
const symbolSize = ref(15);
const nodeBorderColor = ref('#2c3e50');
const lineColor = ref('#2c3e50');
const lineWidth = ref(2);
const labelFontSize = ref(14);
const labelColor = ref('#2c3e50');

// 右键打开弹窗
const openDialog = () => {
  dialogVisible.value = true;
};

const closeDialog = () => {
  dialogVisible.value = false;
  updateChart();
  saveTreeConfigLocally();
};

// 保存配置到 localStorage
const saveTreeConfigLocally = () => {
  const config = {
    chartTitle: chartTitle.value,
    titlePosition: titlePosition.value,
    titleColor: titleColor.value,
    layout: layout.value,
    initialTreeDepth: initialTreeDepth.value,
    symbol: symbol.value,
    symbolSize: symbolSize.value,
    nodeBorderColor: nodeBorderColor.value,
    lineColor: lineColor.value,
    lineWidth: lineWidth.value,
    labelFontSize: labelFontSize.value,
    labelColor: labelColor.value
  };
  localStorage.setItem(storageKey, JSON.stringify(config));
};

// 从 localStorage 加载配置
const loadTreeConfigLocally = () => {
  const configStr = localStorage.getItem(storageKey);
  if (configStr) {
    try {
      const config = JSON.parse(configStr);
      chartTitle.value = config.chartTitle || chartTitle.value;
      titlePosition.value = config.titlePosition || titlePosition.value;
      titleColor.value = config.titleColor || titleColor.value;
      layout.value = config.layout || layout.value;
      initialTreeDepth.value = config.initialTreeDepth || initialTreeDepth.value;
      symbol.value = config.symbol || symbol.value;
      symbolSize.value = config.symbolSize || symbolSize.value;
      nodeBorderColor.value = config.nodeBorderColor || nodeBorderColor.value;
      lineColor.value = config.lineColor || lineColor.value;
      lineWidth.value = config.lineWidth || lineWidth.value;
      labelFontSize.value = config.labelFontSize || labelFontSize.value;
      labelColor.value = config.labelColor || labelColor.value;
    } catch (error) {
      console.error("加载树图配置失败", error);
    }
  }
};

// 构建树形结构数据
const buildTree = data => {
  if (!data.length) return [];
  const lookup = {};
  data.forEach(item => {
    lookup[item.name] = { name: item.name, value: item.value, children: [] };
  });
  let root = null;
  data.forEach(item => {
    if (item.parent && lookup[item.parent] && item.name !== item.parent) {
      lookup[item.parent].children.push(lookup[item.name]);
    } else if (item.name === item.parent) {
      root = lookup[item.name];
    }
  });
  return root ? [root] : [];
};

const updateChart = () => {
  if (!props.rawData.length || !chartInstance.value) return;
  const treeData = buildTree(props.rawData);
  if (!treeData.length) {
    console.warn("⚠️ 数据为空，无法渲染树图");
    return;
  }
  chartInstance.value.setOption({
    title: {
      text: chartTitle.value,
      left: titlePosition.value,
      textStyle: {
        color: titleColor.value,
        fontSize: 18,
        fontWeight: 'bold'
      }
    },
    tooltip: { trigger: 'item', formatter: "{b}: {c}" },
    series: [{
      type: 'tree',
      data: treeData,
      top: '10%',
      left: '15%',
      bottom: '10%',
      right: '15%',
      layout: layout.value,
      symbol: symbol.value,
      symbolSize: symbolSize.value,
      itemStyle: {
        borderColor: nodeBorderColor.value
      },
      lineStyle: { color: lineColor.value, width: lineWidth.value },
      label: {
        show: true,
        position: 'top',
        color: labelColor.value,
        fontSize: labelFontSize.value
      },
      expandAndCollapse: true,
      initialTreeDepth: initialTreeDepth.value,
      animationDuration: 800,
      animationDurationUpdate: 800
    }]
  });
};

onMounted(() => {
  nextTick(() => {
    loadTreeConfigLocally();
    if (!chart.value) {
      console.error("❌ chart 容器未找到");
      return;
    }
    setTimeout(() => {
      chartInstance.value = echarts.init(chart.value);
      updateChart();
    }, 200);
  });
});

watch(() => props.rawData, updateChart, { deep: true });
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
  text-align: center;
  line-height: 24px;
  font-size: 12px;
  color: #fff;
}
@keyframes fadeInScale {
  0% { opacity: 0; transform: scale(0.8); }
  100% { opacity: 1; transform: scale(1); }
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
