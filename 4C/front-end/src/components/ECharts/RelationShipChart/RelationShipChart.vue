<template>
  <div class="chart-container">
    <!-- 将双击事件绑定到图表上 -->
    <div ref="chart" class="chart" @contextmenu.prevent="openDialog"></div>
  </div>
  <!-- 配置弹窗 -->
  <div v-if="dialogVisible" class="modal-overlay">
    <div class="modal">
      <!-- 弹窗头部 -->
      <div class="modal-header">
        <h3>设置关系图配置</h3>
        <span class="close-btn" @click="closeDialog">×</span>
      </div>
      <!-- 弹窗内容：左右两栏 -->
      <div class="config-columns">
        <!-- 左侧：标题及力导向图整体配置 -->
        <div class="config-left">
          <div class="input-group">
            <label for="graphTitle">图表标题：</label>
            <input id="graphTitle" v-model="graphTitle" placeholder="请输入图表标题" />
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
            <label for="repulsion">节点排斥力：</label>
            <input id="repulsion" v-model.number="repulsion" type="number" min="0" />
          </div>
        </div>
        <!-- 右侧：提示框与连线样式 -->
        <div class="config-right">
          <div class="axis-group">
            <h4>提示框配置</h4>
            <div class="input-group">
              <label for="tooltipBgColor">提示框背景色：</label>
              <div class="color-input">
                <input id="tooltipBgColor" v-model="tooltipBgColor" type="color" />
                <span class="color-preview" :style="{ backgroundColor: tooltipBgColor }"></span>
              </div>
            </div>
            <div class="input-group">
              <label for="tooltipTextColor">提示文字颜色：</label>
              <div class="color-input">
                <input id="tooltipTextColor" v-model="tooltipTextColor" type="color" />
                <span class="color-preview" :style="{ backgroundColor: tooltipTextColor }"></span>
              </div>
            </div>
            <div class="input-group">
              <label for="tooltipFontSize">提示文字大小：</label>
              <input id="tooltipFontSize" v-model.number="tooltipFontSize" type="number" min="10" />
            </div>
          </div>
          <div class="axis-group">
            <h4>连线样式</h4>
            <div class="input-group">
              <label for="linkColor">连线颜色：</label>
              <div class="color-input">
                <input id="linkColor" v-model="linkColor" type="color" />
                <span class="color-preview" :style="{ backgroundColor: linkColor }"></span>
              </div>
            </div>
            <div class="input-group">
              <label for="linkWidth">连线宽度：</label>
              <input id="linkWidth" v-model.number="linkWidth" type="number" min="1" />
            </div>
            <div class="input-group">
              <label for="linkCurve">连线弯曲程度：</label>
              <input id="linkCurve" v-model.number="linkCurve" type="number" step="0.1" min="0" />
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
import { onMounted, ref, watch, defineProps, computed } from 'vue';
import * as echarts from 'echarts';
import { userUserStore } from '@/stores/UserStore.js';

// 接收父组件传入的数据，格式要求：节点与连线数据
const props = defineProps({
  rawData: {
    type: Array,
    required: true,
    default: () => []
  }
});

const chart = ref(null);
const chartInstance = ref(null);
const dialogVisible = ref(false);

// 图表配置属性
const graphTitle = ref('关系图');
const titleColor = ref('#333333');
const titlePosition = ref('left');
const repulsion = ref(100);

// 提示框配置
const tooltipBgColor = ref('#3498db');
const tooltipTextColor = ref('#fff');
const tooltipFontSize = ref(14);

// 连线样式
const linkColor = ref('#3498db');
const linkWidth = ref(2);
const linkCurve = ref(0.3);

// 获取用户相关信息（如有需要）
const userStore = userUserStore();

// 右键打开弹窗
const openDialog = () => {
  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
  updateChart()
}

// 数据转换：构造 nodes、links 及分类信息
const transformedData = computed(() => {
  const nodes = [];
  const links = [];
  const categoriesMap = new Map();

  props.rawData.forEach(item => {
    if (item.type === 'node') {
      const node = {
        id: item.id,
        name: item.name,
        symbolSize: item.symbolsize,
        category: item.category,
      };
      nodes.push(node);
      if (!categoriesMap.has(item.category)) {
        categoriesMap.set(item.category, { name: item.category });
      }
    } else if (item.type === 'link') {
      links.push({
        source: item.source,
        target: item.target,
        value: item.value
      });
    }
  });

  const categories = Array.from(categoriesMap.values());
  nodes.forEach(node => {
    node.category = categories.findIndex(cat => cat.name === node.category);
  });
  return { nodes, links, categories };
});

// 图表初始化
onMounted(() => {
  if (chart.value) {
    chartInstance.value = echarts.init(chart.value);
    updateChart();
  } else {
    console.error('Chart container is not found!');
  }
});

// 更新图表配置
const updateChart = () => {
  const { nodes, links, categories } = transformedData.value;
  if (!nodes?.length) return;

  const option = {
    title: {
      text: graphTitle.value,
      left: titlePosition.value,
      textStyle: {
        color: titleColor.value,
        fontSize: 18,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      formatter: function(params) {
        if (params.dataType === 'node') {
          return `节点：${params.data.name}`;
        } else if (params.dataType === 'edge') {
          return `关系：${params.data.source} - ${params.data.target} <br/> 权重：${params.data.value}`;
        }
      },
      backgroundColor: tooltipBgColor.value,
      textStyle: {
        color: tooltipTextColor.value,
        fontSize: tooltipFontSize.value,
      },
      borderWidth: 0,
      borderRadius: 6,
    },
    series: [
      {
        name: '关系图',
        type: 'graph',
        layout: 'force',
        force: {
          repulsion: repulsion.value,
          edgeLength: [80, 200],
        },
        data: nodes,
        links: links,
        categories: categories,
        roam: true,
        label: {
          show: true,
          position: 'right',
          formatter: '{b}'
        },
        lineStyle: {
          color: linkColor.value,
          width: linkWidth.value,
          type: 'solid',
          opacity: 0.8,
          curve: linkCurve.value,
        },
        emphasis: {
          focus: 'adjacency',
          label: {
            show: true
          }
        }
      }
    ]
  };

  chartInstance.value.setOption(option);
};


// 监听 rawData 变化
watch(() => props.rawData, () => {
  if (chartInstance.value) {
    updateChart();
  }
}, { deep: true });
</script>

<style scoped>
.chart-container {
  position: relative;
  width: 100%;
  height: 100%;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
}

.chart {
  width: 100%;
  flex-grow: 1;
}

button {
  padding: 8px 16px;
  background-color: #3498db;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #2980b9;
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
