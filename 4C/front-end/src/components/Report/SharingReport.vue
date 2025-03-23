<script setup>
import { marked } from 'marked'
import { onMounted, reactive, ref, computed } from 'vue'
import { get } from '@/net/index.js'  // 假设你有 get 方法
import { useRoute } from 'vue-router'
import { parseDateTime } from '@/time/Time.js'
import LineChart from '@/components/ECharts/LineChart/LineChart.vue';
import BarChart from '@/components/ECharts/BarChart/BarChart.vue';
import PieChart from '@/components/ECharts/PieChart/PieChart.vue';
import ScatterChart from '@/components/ECharts/ScatterChart/ScatterChart.vue';
import BoxPlotChart from '@/components/ECharts/BoxPlotChart/BoxPlotChart.vue';
import BubbleChart from '@/components/ECharts/BubbleChart/BubbleChart.vue';
import AreaChart from '@/components/ECharts/AreaChart/AreaChart.vue';
import RadarChart from '@/components/ECharts/RadarChart/RadarChart.vue';
import FunnelChart from '@/components/ECharts/FunnelChart/FunnelChart.vue';
import TreemapChart from '@/components/ECharts/TreemapChart/TreemapChart.vue';
import SankeyChart from '@/components/ECharts/SankeyChart/SankeyChart.vue';
import DualAxisChart from '@/components/ECharts/DualAxisChart/DualAxisChart.vue';
import HeatmapChart from '@/components/ECharts/HeatmapChart/HeatmapChart.vue';
import WordCloud from '@/components/ECharts/WordCloud/WordCloud.vue';
import RelationShipChart from '@/components/ECharts/RelationShipChart/RelationShipChart.vue';
import { message } from 'ant-design-vue'

const route = useRoute();
// 假设共享报告的唯一标识通过路由参数传递
const shareId = route.params.id;
const [messageApi, contextHolder] = message.useMessage();

const shareReport = reactive({
  report: {},
  rawData: [],
  shareInfo: {} // 后端返回的分享信息，包含 sharePassword、shareLink、shareId 等
})

// 用于密码校验的状态变量
const enteredPassword = ref('');
const IsShow=ref(false);


// 根据报告类型动态生成数据表格字段（这里可以根据实际需求扩展）
const tableMapping = {
  'LineChart': [
    { key: 'date', label: '日期' },
    { key: 'value', label: '数值' }
  ],
  'AreaChart': [
    { key: 'date', label: '日期' },
    { key: 'value', label: '数值' }
  ],
  'BarChart': [
    { key: 'name', label: '名称' },
    { key: 'value', label: '数值' }
  ],
  'PieChart': [
    { key: 'name', label: '名称' },
    { key: 'value', label: '数值' }
  ],
  'TreemapChart': [
    { key: 'name', label: '名称' },
    { key: 'value', label: '数值' },
    { key: 'parent', label: '父节点' }
  ],
  'ScatterChart': [
    { key: 'x', label: 'x轴' },
    { key: 'y', label: 'y轴' }
  ],
  'BubbleChart': [
    { key: 'x', label: 'x轴' },
    { key: 'y', label: 'y轴' },
    { key: 'size', label: '气泡大小' }
  ],
  'BoxPlotChart': [
    { key: 'name', label: '名称' },
    { key: 'min', label: '最小值' },
    { key: 'q1', label: 'Q1' },
    { key: 'median', label: '中位数' },
    { key: 'q3', label: 'Q3' },
    { key: 'max', label: '最大值' },
    { key: 'outliers', label: '离群值' }
  ],
  'RadarChart': [
    { key: 'category', label: '类别' },
    { key: 'indicator', label: '指标' },
    { key: 'value', label: '数值' }
  ],
  'FunnelChart': [
    { key: 'stage', label: '阶段' },
    { key: 'value', label: '数值' }
  ],
  'SankeyChart': [
    { key: 'source', label: '起始节点' },
    { key: 'target', label: '目标节点' },
    { key: 'value', label: '权重' }
  ],
  'DualAxisChart': [
    { key: 'date', label: '日期' },
    { key: 'value1', label: '数值1' },
    { key: 'value2', label: '数值2' },
    { key: 'yAxis1', label: 'Y轴1' },
    { key: 'yAxis2', label: 'Y轴2' }
  ],
  'HeatmapChart': [
    { key: 'x', label: 'x轴' },
    { key: 'y', label: 'y轴' },
    { key: 'value', label: '数值' }
  ],
  'WordCloud': [
    { key: 'name', label: '名称' },
    { key: 'value', label: '数值' }
  ],
  'RelationShipChart': [
    { key: 'type', label: '类型' },
    { key: 'id', label: '节点ID' },
    { key: 'name', label: '名称' },
    { key: 'symbolsize', label: '节点大小' },
    { key: 'category', label: '类别' },
    { key: 'source', label: '起始点' },
    { key: 'target', label: '目标点' },
    { key: 'value', label: '权重' }
  ]
}

const tableColumns = computed(() => {
  const type = shareReport.report.type;
  return tableMapping[type] || [];
})

// 获取共享报告数据（包括报告内容与分享密码）
const getReportByIdAndPassword = () => {
  get('api/report/getReportByIdAndPassword', {
    reportId:shareId,
    password:enteredPassword.value
  }, (message, data) => {
    messageApi.success("Successfully");
    shareReport.report = data;
    if (data.data) {
      try {
        shareReport.rawData = JSON.parse(data.data);
        IsShow.value=true;
      } catch (e) {
        console.error('数据解析错误：', e);
      }
    }
  },(message,data)=>{
    messageApi.error("密码错误！");
  })
}



onMounted(() => {

})
</script>

<template>
  <contextHolder/>
  <div class="min-h-screen flex items-center justify-center bg-white  p-4 bg-opacity-90">
    <div class="p-2 bg-white  rounded-xl shadow-md w-1/2  transition-all duration-300">
      <!-- 密码输入界面 -->
      <div v-if="!IsShow" class="space-y-6  ">
        <div class="text-center space-y-2">
          <div class="inline-flex items-center justify-center bg-blue-100 p-4 rounded-full">
            <svg class="w-12 h-12 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"
                 xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
            </svg>
          </div>
          <h2 class="text-2xl font-bold text-gray-800">访问受保护的报告</h2>
          <p class="text-gray-500">请输入访问密码查看此分析报告</p>
        </div>

        <div class="space-y-4">
          <input
            v-model="enteredPassword"
            class="w-full px-4 py-3 border-2 border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
            placeholder="请输入访问密码"
            type="password"
            @keyup.enter="getReportByIdAndPassword"
          >
          <button
            class="w-full bg-blue-600 hover:bg-blue-700 text-white py-3 rounded-lg font-medium transition-all flex items-center justify-center gap-2"
            @click="getReportByIdAndPassword"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"
                 xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
            验证并查看报告
          </button>
        </div>
        <p class="text-center text-sm text-gray-400">
          请向报告创建者获取访问密码
        </p>
      </div>
      <!-- 密码验证通过后显示报告内容 -->
      <div v-else class="bg-white rounded-xl w-full shadow-2xl transform transition-all duration-300 ease-in-out">
        <!-- 报告标题及生成时间 -->
        <div class="mb-8 space-y-2">
          <h2 class="text-4xl font-bold text-gray-800">共享数据分析报告</h2>
          <div class="space-y-1">
            <p class="text-gray-600">
              <span class="font-medium">作者：</span>
              {{ shareReport.report.author || '未知作者' }}
            </p>
            <p class="text-gray-600">
              <span class="font-medium">生成时间：</span>
              {{ parseDateTime(shareReport.report.time) }}
            </p>
          </div>
        </div>
        <!-- 数据表格：根据报告类型动态展示数据 -->
        <section v-if="shareReport.rawData.length" class="mb-8">
          <h3 class="text-2xl font-semibold mb-4">📊 数据展示</h3>
          <div class="overflow-x-auto rounded-lg border max-h-96 shadow-sm">
            <table class="min-w-full table-auto">
              <thead class="bg-gray-50">
              <tr>
                <th
                  v-for="column in tableColumns"
                  :key="column.key"
                  class="px-6 py-3 text-left text-sm font-medium text-gray-700 border-b"
                >
                  {{ column.label }}
                </th>
              </tr>
              </thead>
              <tbody>
              <tr
                v-for="(row, index) in shareReport.rawData"
                :key="index"
                class="hover:bg-gray-50 transition-colors duration-200"
              >
                <td
                  v-for="column in tableColumns"
                  :key="column.key"
                  class="px-6 py-4 text-sm text-gray-600 border-b"
                >
                  {{ row[column.key] }}
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </section>
        <!-- 图表展示 -->
        <div v-if="shareReport.report.type" class="mb-8">
          <h3 class="text-xl font-semibold mb-4">🎨 图表展示</h3>
          <div class="flex items-center justify-center bg-gray-50 rounded-xl" style="height: 500px">
            <component
              :is="{
                'LineChart': LineChart,
                'AreaChart': AreaChart,
                'BarChart': BarChart,
                'PieChart': PieChart,
                'ScatterChart': ScatterChart,
                'BubbleChart': BubbleChart,
                'BoxPlotChart': BoxPlotChart,
                'RadarChart': RadarChart,
                'FunnelChart': FunnelChart,
                'TreemapChart': TreemapChart,
                'SankeyChart': SankeyChart,
                'DualAxisChart': DualAxisChart,
                'HeatmapChart': HeatmapChart,
                'WordCloud': WordCloud,
                'RelationShipChart': RelationShipChart
              }[shareReport.report.type]"
              :rawData="shareReport.rawData"
              class="w-full h-full"
            />
          </div>
        </div>
        <!-- AI分析结果 -->
        <section>
          <h3 class="text-2xl font-semibold mb-4">🤖 AI 分析</h3>
          <div
            class="bg-gray-50 p-6 rounded-lg"
            v-html="marked(shareReport.report.analyse || '暂无分析结果')"
          ></div>
        </section>
      </div>
    </div>
  </div>
</template>


<style scoped>
/* 设置背景图片 */
.min-h-screen {
  position: relative;
  background: linear-gradient(
    rgba(0, 0, 0, 0.5),
    rgba(0, 0, 0, 0.5)
  ),
  url('https://bpic.588ku.com/back_pic/06/42/63/94647d8c29b3127.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
}


/* 移动端优化 */
@media (max-width: 640px) {
  .min-h-screen {
    background-attachment: scroll;
  }

  .bg-white {
    background-color: rgba(255, 255, 255, 0.95);
  }
}
</style>
<style scoped>
/* 隐藏滚动条 */
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.markdown-content {
  line-height: 1.6;
}
.markdown-content h1,
.markdown-content h2,
.markdown-content h3 {
  margin-top: 1.5em;
  margin-bottom: 0.5em;
  font-weight: bold;
}
.markdown-content h1 {
  font-size: 2em;
}
.markdown-content h2 {
  font-size: 1.75em;
}
.markdown-content h3 {
  font-size: 1.5em;
}
.markdown-content p {
  margin-bottom: 1em;
}
.markdown-content ul,
.markdown-content ol {
  margin-bottom: 1em;
  padding-left: 2em;
}
.markdown-content li {
  margin-bottom: 0.5em;
}
.markdown-content code {
  background-color: #f3f4f6;
  padding: 0.2em 0.4em;
  border-radius: 4px;
  font-family: monospace;
}
.markdown-content pre {
  background-color: #f3f4f6;
  padding: 1em;
  border-radius: 4px;
  overflow-x: auto;
}
.markdown-content pre code {
  background-color: transparent;
  padding: 0;
}
</style>
