<template>
  <div class="chart-container">
    <div ref="chartRef" class="chart"></div>
    <!-- 加载动画 -->
    <div ref="loadingRef" class="loading">
      <div class="loader"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import * as echarts from 'echarts';

const chartRef = ref(null);
const loadingRef = ref(null);
let myChart = null;
let interval = null;

// 随机生成数据
const generateRandomData = () => {
  const dateRange = 10;
  const lines = 2; // 至少两条线
  const dates = Array.from({ length: dateRange }, (_, i) => `2025-03-${i + 1}`);
  const data = [];

  for (let i = 0; i < lines; i++) {
    const lineData = dates.map(date => ({
      date,
      value: Math.round(Math.random() * 100)
    }));
    data.push({ NAME: `Line ${i + 1}`, data: lineData });
  }

  return data;
};

// 定义折线图各系列使用的颜色
const colorList = [
  '#5470c6', '#91cc75', '#fac858', '#ee6666',
  '#73c0de', '#3ba272', '#fc8452', '#9a60b4'
];

// 更新图表配置
const updateChart = (rawData) => {
  if (!rawData || !Array.isArray(rawData) || rawData.length === 0) {
    console.error("Invalid rawData");
    return;
  }

  // 若 rawData 格式为单一数据（含 date 字段），则转换格式
  const formattedData = rawData[0].date ?
    [{ NAME: '数据', data: rawData }] :
    rawData;

  // 提取所有唯一日期
  const allDates = [...new Set(formattedData.flatMap(item => item.data.map(d => d.date)))] ;

  // 构建各系列数据
  const seriesData = formattedData.map((dataset, index) => ({
    name: dataset.NAME,
    type: 'line',
    // smooth: true,
    symbol: 'circle',
    symbolSize: 8,
    lineStyle: {
      width: 3,
      shadowColor: 'rgba(0,0,0,0.2)',
      shadowBlur: 10,
      shadowOffsetY: 8
    },
    itemStyle: {
      color: colorList[index % colorList.length],
      borderWidth: 2
    },
    data: allDates.map(date =>
      dataset.data.find(item => item.date === date)?.value ?? null
    )
  }));

  const option = {
    title: {
      text: '折线图',
      left: 'center',
      textStyle: {
        fontSize: 24,
        color: '#333'
      }
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(50,50,50,0.9)',
      borderWidth: 0,
      padding: [10, 15],
      textStyle: {
        color: '#fff',
        fontSize: 14
      },
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      },
      formatter: (params) => {
        let res = `<div style="margin-bottom:5px">${params[0].axisValue}</div>`;
        params.forEach(item => {
          res += `
            <div style="display:flex;align-items:center;margin:5px 0">
              <span style="display:inline-block;
                width:10px;height:10px;
                background:${item.color};
                border-radius:50%;margin-right:8px">
              </span>
              ${item.seriesName}: ${item.value ?? '无数据'}
            </div>`;
        });
        return res;
      }
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      axisLine: {
        lineStyle: {
          color: '#666'
        }
      },
      axisLabel: {
        formatter: value => echarts.format.formatTime('MM-dd', value)
      },
      data: allDates,
      name: '日期',  // x 轴的名称
      nameTextStyle: {
        fontWeight: 'bold',
        fontSize: 14,
        color: '#333'
      },
    },
    yAxis: {
      type: 'value',
      name: '数值',  // y 轴的名称
      nameLocation: 'end',  // 将 y 轴的名称放在末尾
      nameTextStyle: {
        fontWeight: 'bold',
        fontSize: 14,
        color: '#333'
      },
      axisLine: {
        lineStyle: {
          color: '#666'
        }
      }
    },
    series: seriesData
  };

  if (myChart) {
    myChart.setOption(option);
    loadingRef.value.style.display = 'none';
  } else {
    myChart = echarts.init(chartRef.value);
    myChart.setOption(option);
    loadingRef.value.style.display = 'none';
  }
};

const updateDataPeriodically = () => {
  interval = setInterval(() => {
    const newData = generateRandomData();
    updateChart(newData);
  }, 1000); // 每秒钟更新一次数据
};

onMounted(() => {
  const data = generateRandomData();
  updateChart(data);
  updateDataPeriodically(); // 启动动态数据更新

  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    if (myChart) {
      myChart.resize();
    }
  });
});

onBeforeUnmount(() => {
  clearInterval(interval); // 清除定时器
  window.removeEventListener('resize', () => {
    if (myChart) {
      myChart.resize();
    }
  });
});
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
  height: 400px;
}

.loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: none;
}

.loader {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
