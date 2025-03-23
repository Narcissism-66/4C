<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
    <!-- 折线图（用户使用时长） -->
    <div class="chart-container">
      <h2 class="text-lg font-bold mb-4 text-center text-blue-600">⏳ 用户使用时长（前五位）</h2>
      <div ref="lineChart" class="chart"></div>
    </div>

    <!-- 柱状图（功能使用次数） -->
    <div class="chart-container">
      <h2 class="text-lg font-bold mb-4 text-center text-green-600">📊 功能使用次数</h2>
      <div ref="barChart" class="chart"></div>
    </div>

    <!-- 饼图（用户类型占比） -->
    <div class="chart-container md:col-span-2">
      <h2 class="text-lg font-bold mb-4 text-center text-purple-600">🍕 反馈类型占比</h2>
      <div ref="pieChart" class="chart"></div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import * as echarts from "echarts";
import { get } from '@/net/index.js'

const options = reactive({
  user: [],
  functionCount: [],
  FeedbackType: [],
})

const lineChart = ref(null);
const barChart = ref(null);
const pieChart = ref(null);

const getAllUser = () => {
  get('api/user/getAllUsers', {}, (message, data) => {
    options.user = data;
    nextTick(() => {
      initLineChart();
    });
  });
};

const getFunctionCountTotal = () => {
  get('api/function/getFunctionCountTotal', {}, (message, data) => {
    options.functionCount = data;
    nextTick(() => {
      initBarChart();
    });
  });
};

const getFeedbackCountByType = () => {
  get("api/feedback/getFeedbackCountByType", {}, (message, data) => {
    options.FeedbackType = data.map(item => {
      // 根据英文类型替换为中文
      if (item.type === 'other') {
        item.type = '其他';
      } else if (item.type === 'suggestion') {
        item.type = '建议';
      } else if (item.type === 'issue') {
        item.type = '问题';
      }
      return item;
    });
    nextTick(() => {
      initPieChart();
    });
  });
};


// ⏳ 折线图（用户使用时长）
const initLineChart = () => {
  if (!options.user.length) return;

  const chart = echarts.init(lineChart.value);

  const usernames = options.user.map(u => u.username);
  const usageHours = options.user.map(u => (u.usageTime / 3600).toFixed(2));
  const usageData = options.user.map(u => {
    const hours = Math.floor(u.usageTime / 3600);
    const minutes = Math.floor((u.usageTime % 3600) / 60);
    return `${hours}小时${minutes}分钟`;
  });

  chart.setOption({
    tooltip: {
      trigger: "axis",
      formatter: function(params) {
        let index = params[0].dataIndex;
        return `${params[0].marker} ${usernames[index]}: ${usageData[index]}`;
      }
    },
    xAxis: {
      type: "category",
      data: usernames,
      axisLine: {
        lineStyle: {
          color: '#4A90E2', // 改变轴线颜色
          width: 2
        }
      },
      axisLabel: {
        color: '#4A90E2', // 改变字体颜色
        fontSize: 12
      }
    },
    yAxis: {
      type: "value",
      name: "时长（小时）",
      axisLine: {
        lineStyle: {
          color: '#4A90E2',
          width: 2
        }
      },
      axisLabel: {
        color: '#4A90E2',
        fontSize: 12
      }
    },
    series: [
      {
        name: "使用时长",
        type: "line",
        data: usageHours,
        smooth: true,
        lineStyle: {
          color: '#FF7F50',
          width: 3
        },
        itemStyle: {
          color: '#FF7F50'
        },
        areaStyle: {
          color: 'rgba(255, 127, 80, 0.3)' // 背景渐变
        }
      },
    ],
    animationDuration: 1000, // 添加动画效果
  });
};

// 📊 柱状图（功能使用次数）
const initBarChart = () => {
  const chart = echarts.init(barChart.value);
  chart.setOption({
    tooltip: {
      trigger: "axis"
    },
    xAxis: {
      type: "category",
      data: ["单一数据", "合并数据", "报告生成", "AI分析"],
      axisLine: {
        lineStyle: {
          color: '#4A90E2',
          width: 2
        }
      },
      axisLabel: {
        color: '#4A90E2',
        fontSize: 14
      }
    },
    yAxis: {
      type: "value",
      name: "使用次数",
      axisLine: {
        lineStyle: {
          color: '#4A90E2',
          width: 2
        }
      },
      axisLabel: {
        color: '#4A90E2',
        fontSize: 14
      }
    },
    series: [
      {
        name: "使用次数",
        type: "bar",
        data: [
          options.functionCount.singleTotal,
          options.functionCount.mergeTotal,
          options.functionCount.reportTotal,
          options.functionCount.aitotal
        ],
        itemStyle: {
          color: '#ADD8E6' // 淡蓝色
        }
      }
    ],
    animationDuration: 1000,
  });
};

// 🍕 饼图（用户类型占比）
const initPieChart = () => {
  if (!options.FeedbackType.length) return;

  const pieData = options.FeedbackType.map(item => ({
    value: item.count,
    name: item.type
  }));

  const colors = {
    '问题': '#FF6347', // 其他 - 番茄红
    '其它': '#1E90FF', // 建议 - 道奇蓝
    '建议': '#32CD32', // 问题 - 石绿
  };

  const chart = echarts.init(pieChart.value);
  chart.setOption({
    tooltip: {
      trigger: "item"
    },
    series: [
      {
        type: "pie",
        radius: "50%",
        data: pieData,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2,
          color: (params) => {
            // 根据 type 设置颜色
            return colors[params.name] || '#FFD700'; // 默认金色
          }
        },
        label: {
          color: '#333',
          fontSize: 14
        },
        labelLine: {
          lineStyle: {
            color: '#4A90E2'
          }
        }
      }
    ],
    animationDuration: 1000,
  });
};



onMounted(async () => {
  getAllUser();
  getFunctionCountTotal();
  getFeedbackCountByType();
});
</script>

<style scoped>
.chart-container {
  background: rgba(255, 255, 255, 0.8);
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.chart {
  width: 100%;
  height: 300px;
}
</style>
