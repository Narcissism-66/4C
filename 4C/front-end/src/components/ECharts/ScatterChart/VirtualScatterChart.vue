<template>
  <div class="chart-container">
    <div ref="chart" style="width: 100%; height: 100%;"></div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts';

const chart = ref(null);

const props = defineProps({
  rawData: {
    type: Array,
    required: true,
  },
});

let chartInstance = null;

const updateChart = () => {
  // Prepare data for the scatter chart
  const xValues = props.rawData.map((item) => item.x);
  const yValues = props.rawData.map((item) => item.y);

  // Configure chart options
  const option = {
    title: {
      text: '散点图',
      left: 'center',
      textStyle: {
        fontSize: 18,
        fontWeight: 'bold',
        color: '#2f4f4f',
      },
    },
    tooltip: {
      trigger: 'item',
      formatter: 'X: {b0}, Y: {b1}',
    },
    xAxis: {
      type: 'value',
      axisLabel: {
        fontSize: 12,
        color: '#2f4f4f',
      },
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        fontSize: 12,
        color: '#2f4f4f',
      },
    },
    series: [
      {
        data: xValues.map((x, index) => [x, yValues[index]]),
        type: 'scatter',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
            { offset: 0, color: '#ff6347' },
            { offset: 1, color: '#ff1493' },
          ]),
          borderColor: '#fff',
          borderWidth: 1,
        },
        symbolSize: 10, // Increase size for better visibility
      },
    ],
  };

  if (chartInstance) {
    chartInstance.setOption(option);
  } else {
    chartInstance = echarts.init(chart.value);
    chartInstance.setOption(option);
  }
};

onMounted(() => {
  updateChart();
});

// Watch for data changes
watch(
  () => props.rawData,
  () => {
    updateChart();
  },
  { deep: true }
);
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 100%;
  border: 1px solid #dcdcdc;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}
</style>
