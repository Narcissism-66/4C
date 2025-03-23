<template>
  <div class="chart-container">
    <div ref="chart" class="chart"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount } from 'vue';
import * as echarts from 'echarts';

const chart = ref(null);
let chartInstance = null;

const props = defineProps({
  rawData: {
    type: Array,
    required: true,
  }
});

const initChart = () => {
  if (!chart.value) return;

  chartInstance = echarts.init(chart.value);
  window.addEventListener('resize', handleResize);
};

const handleResize = () => {
  chartInstance?.resize();
};

const updateChart = () => {
  if (!chartInstance || props.rawData.length === 0) {
    chartInstance?.clear();
    return;
  }

  // 数据预处理
  const xAxisData = [...new Set(props.rawData.map(item => item.x))].sort();
  const yAxisData = [...new Set(props.rawData.map(item => item.y))].sort();
  const maxValue = Math.max(...props.rawData.map(item => item.value));

  const option = {
    tooltip: {
      position: 'top',
      backgroundColor: 'rgba(0,0,0,0.7)',
      borderColor: 'rgba(255,255,255,0.3)',
      textStyle: {
        color: '#fff',
        fontSize: 12,
        fontWeight: 'normal'
      },
      formatter: params => {
        return `${params.value[0]} × ${params.value[1]}<br/>值: ${params.value[2]}`;
      },
      extraCssText: 'box-shadow: 0 4px 12px rgba(0,0,0,0.15); border-radius: 6px;'
    },
    grid: {
      top: 30,
      bottom: 80,
      left: 60,
      right: 20,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisLine: {
        lineStyle: {
          color: '#6B7280',
          width: 1
        }
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#374151',
        fontSize: 12,
        margin: 8
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: ['rgba(243,244,246,0.3)', 'rgba(229,231,235,0.3)']
        }
      }
    },
    yAxis: {
      type: 'category',
      data: yAxisData,
      axisLine: {
        lineStyle: {
          color: '#6B7280',
          width: 1
        }
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#374151',
        fontSize: 12,
        margin: 8
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: ['rgba(243,244,246,0.3)', 'rgba(229,231,235,0.3)']
        }
      }
    },
    visualMap: {
      min: 0,
      max: maxValue,
      calculable: true,
      orient: 'vertical',
      right: 20,
      top: 'center',
      itemWidth: 12,
      itemHeight: 120,
      textStyle: {
        color: '#374151',
        fontSize: 12
      },
      inRange: {
        color: ['#fde68a', '#f59e0b', '#ef4444'] // 黄-橙-红渐变
      },
      controller: {
        inRange: {
          symbol: 'rect',
          itemSize: 14,
          itemStyle: {
            borderWidth: 1,
            borderColor: '#d1d5db'
          }
        }
      }
    },
    series: [{
      type: 'heatmap',
      data: props.rawData.map(item => [item.x, item.y, item.value]),
      itemStyle: {
        borderColor: '#fff',
        borderWidth: 1,
        borderRadius: 2
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 12,
          shadowColor: 'rgba(0,0,0,0.2)',
          borderWidth: 2,
          borderColor: '#fff'
        }
      },
      progressive: 1000,
      animation: true,
      animationDurationUpdate: 1000
    }],
    backgroundColor: '#f8fafc'
  };

  chartInstance.setOption(option);
};

onMounted(() => {
  initChart();
  updateChart();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  chartInstance?.dispose();
});

watch(
  () => props.rawData,
  () => {
    updateChart();
    handleResize();
  },
  { deep: true }
);
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 600px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 16px;
  box-shadow: 0 8px 24px -4px rgba(0, 0, 0, 0.1),
  0 4px 8px -2px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: box-shadow 0.3s ease;
}

.chart-container:hover {
  box-shadow: 0 12px 32px -4px rgba(0, 0, 0, 0.15),
  0 6px 12px -2px rgba(0, 0, 0, 0.08);
}

.chart {
  width: 100%;
  height: 100%;
  min-height: 400px;
}
</style>
