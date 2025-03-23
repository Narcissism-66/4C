<script setup>
import { marked } from 'marked'
import { onMounted, reactive, ref, computed } from 'vue'
import { get, post } from '@/net/index.js'  // 假设你有 post 方法
import { useRoute } from 'vue-router'
import { parseDateTime } from '@/time/Time.js'
import { userUserStore } from '@/stores/UserStore.js'
import BarChart from '@/components/ECharts/BarChart/BarChart.vue';
import LineChart from '@/components/ECharts/LineChart/LineChart.vue';
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
import ShareLink from '@/main.js'
import { message } from 'ant-design-vue'

const route = useRoute();
const userStore = userUserStore()
const pid = route.params.id;
const [messageApi, contextHolder] = message.useMessage();


//随机生成一个包含英文，数字的八位密码
const generatePassword=(length)=> {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let password = '';
  for (let i = 0; i < length; i++) {
    const randomIndex = Math.floor(Math.random() * chars.length);
    password += chars[randomIndex];
  }
  return password;
}

const option = reactive({
  report: {},
  rawData: [],
  password:generatePassword(8),
  currentPassword: '',        // 新增：当前有效密码
  passwordGeneratedTime: null // 新增：密码生成时间戳
})

// 定义各图表类型对应的必需字段和表头名称
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
    {key:'source', label: '起始点'},
    { key: 'target', label: '目标点' },
    { key: 'value', label: '权重' }
    // 注意：如果有边数据(link)需要单独处理
  ]
}

// 根据报告类型动态获取对应表头
const tableColumns = computed(() => {
  const type = option.report.type;
  return tableMapping[type] || [];
})

// 获取报告数据
const getReportById = () => {
  get('api/report/viewReport', {
    id: pid
  }, (message, data) => {
    option.report = data;
    // 解析数据字符串
    if (data.data) {
      try {
        option.rawData = JSON.parse(data.data); // 转换 data 字符串为 JSON
        console.log(JSON.stringify(option.rawData, null, 2))
      } catch (e) {
        console.error('数据解析错误：', e)
      }
    }
  })
}

// 初始化加载报告
onMounted(() => {
  getReportById();
})

// 共享功能相关
const showShareModal = ref(false);
const shareInfo = reactive({
  shareId: 'abc123ef',
  sharePassword: '12345678',
  shareLink: 'http://localhost:5173/viewReport/19'
});


// 新增响应式数据
const timeLeft = ref('')      // 剩余时间显示文本
let timer = null              // 定时器实例

// 计算剩余时间并格式化
const calculateTimeLeft = () => {
  if (!option.passwordGeneratedTime) return '00分00秒'

  const now = Date.now()
  const endTime = option.passwordGeneratedTime + 60 * 60 * 1000 // 有效截止时间
  const remaining = endTime - now

  if (remaining <= 0) {
    clearInterval(timer)
    return '已过期'
  }

  const minutes = Math.floor(remaining / 1000 / 60)
  const seconds = Math.floor((remaining / 1000) % 60)
  return `${minutes.toString().padStart(2, '0')}分${seconds.toString().padStart(2, '0')}秒`
}

// 打开弹窗时启动定时器
const openShareModal = () => {
  showShareModal.value = true
  timeLeft.value = calculateTimeLeft()
  timer = setInterval(() => {
    timeLeft.value = calculateTimeLeft()
    // 自动关闭过期弹窗
    if (timeLeft.value === '已过期') {
      showShareModal.value = false
      clearInterval(timer)
    }
  }, 1000)
}

// 修改生成分享方法
const generateShareReport = () => {
  const now = Date.now()
  const oneHour = 60 * 60 * 1000

  if (option.currentPassword && option.passwordGeneratedTime) {
    if (now - option.passwordGeneratedTime < oneHour) {
      shareInfo.shareUser = userStore.user.username
      shareInfo.sharePassword = option.currentPassword
      shareInfo.shareLink = ShareLink + pid
      openShareModal() // 改为调用新方法
      return
    }
  }

  const newPassword = generatePassword(8)
  option.currentPassword = newPassword
  option.passwordGeneratedTime = now

  post('api/report/MakeShare', {
    reportId: pid,
    password: newPassword,
  }, () => {
    shareInfo.shareUser = userStore.user.username
    shareInfo.sharePassword = newPassword
    shareInfo.shareLink = ShareLink + pid
    openShareModal() // 改为调用新方法
  })
}

// 关闭弹窗时清理定时器
const closeShareModal = () => {
  showShareModal.value = false
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

// 复制到剪贴板
const copyToClipboard = (text) => {
  navigator.clipboard.writeText(text).then(() => {
    messageApi.success("复制成功！")
  }).catch(() => {
    messageApi.error('复制失败，请手动复制')
  })
}
</script>

<template>
  <contextHolder/>
  <div class="mt-10 inset-0 flex items-center justify-center p-4 z-50 mb-5">
    <div
      class="bg-white rounded-xl w-3/5 shadow-2xl transform transition-all duration-300 ease-in-out">
      <div class="p-8 space-y-8">
        <!-- 标题及分享按钮 -->
        <div class="flex justify-between items-center mb-8">
          <h2 class="text-4xl font-bold text-gray-800">数据分析报告</h2>
          <button
            @click="generateShareReport"
            class="px-4 text-xl py-2 hover:text-blue-700 text-blue-400 rounded-md  transition-all duration-200 flex gap-2"
          >
            <svg class="size-7  text-blue-400 hover:text-blue-700" fill="none" stroke="currentColor" viewBox="0 0 24 24" >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/>
            </svg>
            分享
          </button>
        </div>

        <!-- 用户信息 -->
        <section class="mb-8">
          <h3 class="text-2xl font-semibold mb-4 border-l-4 border-blue-600 pl-3 text-gray-800">👤
            用户信息</h3>
          <div class="bg-gray-50 p-6 rounded-lg shadow-sm">
            <p class="text-gray-700">报告生成用户：{{ userStore.user.username }}</p>
            <p class="text-gray-700">生成时间：{{ parseDateTime(option.report.time) }}</p>
          </div>
        </section>

        <!-- 时间范围 -->
        <section class="mb-8" v-if="option.report.starttime !== '未识别'">
          <h3 class="text-2xl font-semibold mb-4 border-l-4 border-green-600 pl-3 text-gray-800">⏳
            时间范围</h3>
          <div class="bg-gray-50 p-6 rounded-lg shadow-sm">
            <p class="text-gray-700">数据起始时间：{{ option.report.starttime }}</p>
            <p class="text-gray-700">数据结束时间：{{ option.report.endtime }}</p>
          </div>
        </section>

        <!-- 数据表格：动态渲染 -->
        <section v-if="option.rawData.length" class="mb-8">
          <h3 class="text-2xl font-semibold mb-4 border-l-4 border-purple-600 pl-3 text-gray-800">📊
            数据展示</h3>
          <div class="overflow-x-auto rounded-lg border max-h-96 shadow-sm scrollbar-hide">
            <table class="min-w-full table-auto">
              <thead class="bg-gray-50">
              <tr>
                <th v-for="column in tableColumns" :key="column.key"
                    class="px-6 py-3 text-left text-sm font-medium text-gray-700 border-b">
                  {{ column.label }}
                </th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="(row, index) in option.rawData" :key="index"
                  class="hover:bg-gray-50 transition-colors duration-200">
                <td v-for="column in tableColumns" :key="column.key"
                    class="px-6 py-4 text-sm text-gray-600 border-b">
                  {{ row[column.key] }}
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </section>

        <!-- 图表展示 -->
        <div v-if="option.report.type" class="  rounded-xl ">
          <h3 class="text-xl font-semibold mb-4">🎨 图表展示</h3>
          <div class=" flex items-center justify-center bg-gray-50 rounded-xl" style="height: 500px">
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
              }[option.report.type]"
              :rawData="option.rawData"
              class="w-full h-full"
            />
          </div>
        </div>

        <!-- AI分析结果 -->
        <section class="mb-8">
          <h3 class="text-2xl font-semibold mb-4 border-l-4 border-orange-600 pl-3 text-gray-800">🤖
            AI分析</h3>
          <div class="bg-gray-50 p-6 rounded-lg markdown-content shadow-sm"
               v-html="marked(option.report.analyse || '暂无分析结果')"></div>
        </section>
      </div>
    </div>
  </div>

  <!-- 分享信息弹窗 -->
  <div v-if="showShareModal"
       class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
    <div class="bg-white rounded-xl p-6 w-96 relative">
      <h3 class="text-2xl font-bold mb-4 text-center">分享信息</h3>
      <div class="mb-3">
        <label class="block text-gray-700">分享链接：</label>
        <div class="flex items-center">
          <input type="text" readonly :value="shareInfo.shareLink"
                 class="flex-1 border rounded-l-md px-2 py-1" />
          <button @click="copyToClipboard(shareInfo.shareLink)"
                  class="bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded-r-md">
            复制
          </button>
        </div>
      </div>
      <div class="mb-3">
        <label class="block text-gray-700">访问密码：</label>
        <div class="flex items-center">
          <input type="text" readonly :value="shareInfo.sharePassword"
                 class="flex-1 border rounded-l-md px-2 py-1" />
          <button @click="copyToClipboard(shareInfo.sharePassword)"
                  class="bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded-r-md">
            复制
          </button>
        </div>
        <div class="text-sm mt-1"
             :class="timeLeft === '已过期' ? 'text-red-500' : 'text-gray-500'">
          有效期剩余：{{ timeLeft }}
        </div>
      </div>
      <button @click="showShareModal = false"
              class="w-full mt-4 bg-gray-300 hover:bg-gray-400 text-gray-800 px-3 py-2 rounded-md">
        关闭
      </button>
    </div>
  </div>
</template>

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

.markdown-content h1, .markdown-content h2, .markdown-content h3 {
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

.markdown-content ul, .markdown-content ol {
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
