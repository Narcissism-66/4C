<template>
  <div class="chart-container">
    <div ref="chart" style="width: 100%; height: 400px;"></div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import * as echarts from 'echarts';

const chart = ref(null);

const props = defineProps({
  rawData: {
    type: Array,
    required: true,
  },
});

onMounted(() => {
  const chartInstance = echarts.init(chart.value);

  // Prepare data for the scatter chart
  const xValues = props.rawData.map((item) => item.x);
  const yValues = props.rawData.map((item) => item.y);

  // Configure chart options
  const option = {
    title: {
      text: '散点图展示',
    },
    tooltip: {
      trigger: 'item',
    },
    xAxis: {
      type: 'value',
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        data: xValues.map((x, index) => [x, yValues[index]]),
        type: 'scatter',
        itemStyle: {
          color: '#ff6347',
        },
      },
    ],
  };

  chartInstance.setOption(option);
});
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 400px;
}
</style>
