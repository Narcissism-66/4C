<template>
  <div class="chart-container">
    <div v-if="userStore.user!=null" class="header">
      <h3 class="title"></h3>
      <button class="settings-btn" @click="showDialog = true">
        <svg class="icon" viewBox="0 0 24 24">
          <path d="M19.43 12.98c.04-.32.07-.64.07-.98s-.03-.66-.07-.98l2.11-1.65c.19-.15.24-.42.12-.64l-2-3.46c-.12-.22-.39-.3-.61-.22l-2.49 1c-.52-.4-1.08-.73-1.69-.98l-.38-2.65C14.46 2.18 14.25 2 14 2h-4c-.25 0-.46.18-.49.42l-.38 2.65c-.61.25-1.17.59-1.69.98l-2.49-1c-.23-.09-.49 0-.61.22l-2 3.46c-.13.22-.07.49.12.64l2.11 1.65c-.04.32-.07.65-.07.98s.03.66.07.98l-2.11 1.65c-.19.15-.24.42-.12.64l2 3.46c.12.22.39.3.61.22l2.49-1c.52.4 1.08.73 1.69.98l.38 2.65c.03.24.24.42.49.42h4c.25 0 .46-.18.49-.42l.38-2.65c.61-.25 1.17-.59 1.69-.98l2.49 1c.23.09.49 0 .61-.22l2-3.46c.12-.22.07-.49-.12-.64l-2.11-1.65zM12 15.5c-1.93 0-3.5-1.57-3.5-3.5s1.57-3.5 3.5-3.5 3.5 1.57 3.5 3.5-1.57 3.5-3.5 3.5z"/>
        </svg>
        坐标轴设置
      </button>
    </div>
    <div ref="chart" class="chart"></div>

    <!-- 设置弹窗 -->
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
import { onMounted, ref, watch, defineProps } from 'vue';
import * as echarts from 'echarts';
import { userUserStore } from '@/stores/UserStore.js'

const chart = ref(null);
const chartInstance = ref(null);
const showDialog = ref(false);
const xAxisName = ref('');
const yAxisName = ref('');
const userStore=userUserStore();

const props = defineProps({
  rawData: {
    type: Array,
    required: true,
    default: () => []
  },
});

// 图表初始化
onMounted(() => {
  chartInstance.value = echarts.init(chart.value);
  updateChart();
});

// 更新图表配置
const updateChart = () => {
  if (!props.rawData?.length) return;

  const colorList = ['#3498db', '#1abc9c', '#9b59b6', '#e74c3c', '#f39c12', '#2ecc71'];
  let categories = [];
  let series = [];

  // 处理数据逻辑
  if (props.rawData[0]?.name) { // 单数据集
    categories = props.rawData.map(item => item.name);
    series = [{
      name: '数据集1',
      type: 'bar',
      data: props.rawData.map(item => item.value),
      itemStyle: {
        color: colorList[0],
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
  } else { // 多数据集
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
          color: colorList[index % colorList.length],
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

  // ECharts配置项
  const option = {
    title: {
      text: '柱状图',
      left: 'center',
      textStyle: {
        fontWeight: 'bold',
        fontSize: 20,
        color: '#2c3e50',
      },
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(52, 152, 219, 0.9)',
      textStyle: {
        color: '#fff',
        fontSize: 14,
      },
      borderWidth: 0,
      borderRadius: 6,
    },
    legend: {
      data: props.rawData[0]?.NAME ? props.rawData.map(d => d.NAME) : ['数据1'],
      top: 40,
      textStyle: {
        fontSize: 14,
        color: '#7f8c8d',
      },
      itemGap: 20,
    },
    xAxis: {
      type: 'category',
      name: xAxisName.value,
      nameLocation: 'center',
      nameGap: 30,
      nameTextStyle: {
        fontWeight: 'bold',
        fontSize: 14,
        color: '#2c3e50',
      },
      data: categories,
      axisLabel: {
        rotate: 45,
        color: '#7f8c8d',
        fontSize: 12,
        margin: 15,
      },
      axisLine: {
        lineStyle: {
          color: '#bdc3c7',
          width: 2,
        },
      },
    },
    yAxis: {
      type: 'value',
      name: yAxisName.value,
      nameTextStyle: {
        fontWeight: 'bold',
        fontSize: 14,
        color: '#2c3e50',
      },
      axisLabel: {
        color: '#7f8c8d',
        fontSize: 12,
      },
      axisLine: {
        lineStyle: {
          color: '#bdc3c7',
          width: 2,
        },
      },
      splitLine: {
        lineStyle: {
          color: '#ecf0f1',
        }
      },
    },
    series,
  };

  chartInstance.value.setOption(option);
};

const applyAxisNames = () => {
  showDialog.value = false;
  updateChart();
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
  flex-grow: 1; /* 让它填充剩余空间 */
}


.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title {
  margin: 0;
  color: #2c3e50;
  font-size: 1.4rem;
  font-weight: 600;
}

.settings-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.settings-btn:hover {
  background: #2980b9;
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(52, 152, 219, 0.3);
}

.icon {
  width: 18px;
  height: 18px;
  fill: currentColor;
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-container {
  background: white;
  padding: 25px;
  border-radius: 12px;
  width: 350px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

h3 {
  margin: 0 0 25px;
  color: #2c3e50;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-item {
  margin: 18px 0;
}

.input-label {
  display: block;
  margin-bottom: 8px;
  color: #2c3e50;
  font-weight: 500;
  font-size: 14px;
}

input {
  width: 100%;
  padding: 10px 12px;
  border: 2px solid #bdc3c7;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
}

input:focus {
  border-color: #3498db;
  outline: none;
}

.button-group {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 25px;
}

.confirm-btn, .cancel-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.confirm-btn {
  background: #3498db;
  color: white;
}

.cancel-btn {
  background: #e74c3c;
  color: white;
}

button:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}


</style>
