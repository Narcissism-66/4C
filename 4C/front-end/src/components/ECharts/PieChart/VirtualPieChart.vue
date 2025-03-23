<template>
  <div class="chart-container">
    <div ref="chart" style="width: 100%; height: 400px;"></div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts';

const chart = ref(null);

const props = defineProps({
  rawData: {
    type: Array,
    required: true,
  },
});

const updateChart=()=>{
  const chartInstance = echarts.init(chart.value);

  // Prepare data for the pie chart
  const pieData = props.rawData.map((item) => ({
    name: item.name,
    value: item.value,
  }));

  // Configure chart options with smoother animation
  const option = {
    title: {
      text: '饼图',
      left: 'center',
      textStyle: {
        fontSize: 18,
        fontWeight: 'bold',
        color: '#333',
      },
      subtextStyle: {
        fontSize: 14,
        color: '#777',
      },
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#ccc',
      borderWidth: 1,
      padding: [10, 15],
      textStyle: {
        fontSize: 14,
        color: '#333',
      },
      formatter: (params) => {
        return `${params.name}: ${params.value} (${params.percent}%)`;
      },
    },
    legend: {
      orient: 'horizontal',
      top: 'bottom',
      left: 'center',
      textStyle: {
        fontSize: 12,
        color: '#333',
      },
    },
    series: [
      {
        name: '饼图',
        type: 'pie',
        radius: ['30%', '50%'],
        data: pieData,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: true,
          position: 'outside',  // Make label outside the pie
          formatter: '{b}: {c} ({d}%)',
          color: '#333',
          fontSize: 12,
        },
        labelLine: {
          show: true,
          length: 15,
          length2: 8,
          lineStyle: {
            color: '#333',
          },
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.3)',
            borderWidth: 4,
            borderColor: '#fff',
          },
          label: {
            fontSize: 14,
            fontWeight: 'bold',
            color: '#fff',
          },
        },
        animationType: 'scale',
        animationEasing: 'cubicOut',
        animationDuration: 2000,
        animationDelay: (index) => index * 150,
        animationDelayUpdate: (index) => index * 150,
        color: ['#FF6F61', '#6B5B95', '#88B04B', '#F7CAC9', '#92A8D1'],  // You can customize the colors here
      },
    ],
  };


  chartInstance.setOption(option);
}

onMounted(() => {
  updateChart();
})
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
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}
</style>
