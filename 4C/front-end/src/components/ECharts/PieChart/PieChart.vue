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

  // Prepare data for the pie chart
  const pieData = props.rawData.map((item) => ({
    name: item.name,
    value: item.value,
  }));

  // Configure chart options
  const option = {
    title: {
      text: '饼图展示',
      left: 'center',
    },
    tooltip: {
      trigger: 'item',
    },
    series: [
      {
        name: '数据分布',
        type: 'pie',
        radius: '50%',
        data: pieData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
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
