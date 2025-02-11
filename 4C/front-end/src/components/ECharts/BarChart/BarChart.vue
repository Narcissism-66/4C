<template>
  <div class="chart-container">
    <div ref="chart" style="width: 100%; height: 400px;"></div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch, defineProps } from 'vue';
import * as echarts from 'echarts';

const chart = ref(null);

const props = defineProps({
  rawData: {
    type: Array,
    required: true,
  },
});

onMounted(() => {
  // 确保 rawData 不为空且有数据
  if (!props.rawData || props.rawData.length === 0) {
    console.error('rawData 数据为空');
    return;
  }

  const chartInstance = echarts.init(chart.value);

  // 定义颜色数组，用于不同数据集
  const colorList = ['#2f89fc', '#ff7f50', '#8e44ad', '#e74c3c', '#2ecc71', '#f39c12'];

  // 监听 rawData 更新，重新绘制图表
  watch(
    () => props.rawData,
    (newData) => {
      if (!newData || newData.length === 0) {
        console.error('传入的数据为空');
        return;
      }

      let categories = [];
      let series = [];

      // 判断数据格式并准备柱状图
      if (newData[0].hasOwnProperty('name') && newData[0].hasOwnProperty('value')) {
        // 如果是单一数据集格式
        categories = newData.map(item => item.name); // 使用 name 作为 x 轴的类别
        series = [
          {
            name: '数据集1',
            type: 'bar',
            data: newData.map(item => item.value),
            itemStyle: {
              color: colorList[0], // 使用颜色数组中的第一个颜色
            },
            label: {
              show: true,
              position: 'top',
            },
          },
        ];
      } else {
        // 如果是多个数据集格式，处理每个数据集
        categories = newData[0].data.map(item => item.name); // 假设所有数据集的 categories 相同

        // 为每个数据集创建 series 数据
        series = newData.map((dataset, index) => {
          const values = dataset.data.map(item => item.value);
          return {
            name: dataset.NAME, // 使用 NAME 作为每个数据集的名称
            type: 'bar',
            data: values,
            itemStyle: {
              color: colorList[index % colorList.length], // 根据索引使用颜色数组中的颜色
            },
            label: {
              show: true,
              position: 'top',
            },
          };
        });
      }

      // 配置图表选项
      const option = {
        title: {
          text: '柱状图展示',
        },
        tooltip: {
          trigger: 'axis',
        },
        legend: {
          data: newData[0].hasOwnProperty('NAME')
            ? newData.map(dataset => dataset.NAME) // 使用每个数据集的 NAME 作为图例
            : ['数据集1'], // 如果只有一个数据集
        },
        xAxis: {
          type: 'category',
          data: categories, // x轴数据来自所有数据集的类别（name）
        },
        yAxis: {
          type: 'value',
        },
        series, // 根据数据格式生成不同的 series
      };

      // 清除并重新设置图表
      chartInstance.setOption(option);
    },
    { immediate: true } // 确保初始时也能执行
  );
});
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 400px;
}
</style>
