<script setup>
import { ref, onMounted, watch, onBeforeUnmount, defineProps } from 'vue';
import * as echarts from 'echarts';

const chartRef = ref(null); // 图表容器引用
let myChart = null;

// 使用 defineProps 获取传入的属性
const props = defineProps({
  rawData: {
    type: Array,
    required: true
  }
});

const updateChart = (rawData) => {
  console.log("更新图表的数据:", rawData);

  // 确保 rawData 格式正确
  if (!rawData || !Array.isArray(rawData) || rawData.length === 0) {
    console.error("传入的 rawData 格式有问题或为空");
    return;
  }

  let formattedData = [];

  // 如果数据是单一数据集的格式
  if (rawData[0].hasOwnProperty('date') && rawData[0].hasOwnProperty('value')) {
    // 将数据转化为符合多个数据集格式的结构
    formattedData = [
      {
        NAME: '单一数据集',
        data: rawData
      }
    ];
  } else {
    // 如果数据是多个数据集的格式，直接使用
    formattedData = rawData;
  }

  // 获取所有日期的并集，确保 x 轴是统一的
  const allDates = [...new Set(formattedData.flatMap(item => item.data.map(d => d.date)))];
  console.log("所有日期:", allDates);

  // 生成每个产品的折线数据
  const seriesData = formattedData.map(dataset => {
    const data = allDates.map(date => {
      const idx = dataset.data.findIndex(item => item.date === date);
      return idx !== -1 ? dataset.data[idx].value : null;
    });

    return {
      name: dataset.NAME,  // 使用 NAME 作为每个系列的名称
      type: 'line',
      smooth: true,
      data,
      label: {
        show: true,
        position: 'top'
      }
    };
  });

  console.log("折线数据:", seriesData);

  // 配置 ECharts 图表的选项
  const option = {
    title: {
      text: '产品折线图'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: formattedData.map(item => item.NAME)  // 使用 NAME 来生成图例
    },
    xAxis: {
      type: 'category',
      data: allDates,  // 使用所有日期作为 x 轴数据
    },
    yAxis: {
      type: 'value'
    },
    series: seriesData  // 将折线图数据传入
  };

  // 确保 myChart 不为 null 再调用 setOption
  if (myChart) {
    myChart.setOption(option);
  } else {
    console.error("myChart is not initialized");
  }
};

onMounted(() => {
  // 确保 chartRef 已绑定到 DOM 元素
  if (chartRef.value) {
    myChart = echarts.init(chartRef.value);
    updateChart(props.rawData); // 初始化时设置图表
  } else {
    console.error("chartRef is not properly bound to the DOM element");
  }
});

// 监听 rawData 的变化并更新图表
watch(() => props.rawData, (newData) => {
  updateChart(newData); // 数据变化时更新图表
});

// 在组件销毁前销毁图表实例
onBeforeUnmount(() => {
  if (myChart) {
    myChart.dispose();
  }
});
</script>

<template>
  <div ref="chartRef" style="width: 100%; height: 400px;"></div>
</template>
