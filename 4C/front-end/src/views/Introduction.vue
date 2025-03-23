<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { get, post } from '@/net/index.js'
import router from '@/router/index.js'
import { message } from 'ant-design-vue'
import BarChart from '../components/ECharts/BarChart/VirtualBarChart.vue';
import LineChart from '../components/ECharts/LineChart/VirtualLineChart.vue';
import PieChart from '../components/ECharts/PieChart/VirtualPieChart.vue';
import ScatterChart from '../components/ECharts/ScatterChart/VirtualScatterChart.vue';
import VirtualAreaChart from '@/components/ECharts/AreaChart/VirtualAreaChart.vue'
import VirtualBoxPlotChart from '@/components/ECharts/BoxPlotChart/VirtualBoxPlotChart.vue'
import VirtualSankeyChart from '@/components/ECharts/SankeyChart/VirtualSankeyChart.vue'
import VirtualRadarChart from '@/components/ECharts/RadarChart/VirtualRadarChart.vue'
import VirtualBubbleChart from '@/components/ECharts/BubbleChart/VirtualBubbleChart.vue'
import VirtualWordCloud from '@/components/ECharts/WordCloud/VirtualWordCloud.vue'
import { userUserStore } from '@/stores/UserStore.js'
const [messageApi, contextHolder] = message.useMessage();


const showAuthModal = ref(false);
const isRegistering = ref(false);
const userStore=userUserStore();

const featureCards = [
  {
    title: '云鉴录',
    desc: '支持Json/xls/CSV多格式上传，基于OCR的铭文识别与简帛文字智能解析系统'
  },
  {
    title: '万象析',
    desc: '集成深度学习的卦象推演模型，支持数据关联挖掘与阴阳五行规律可视化'
  },
  {
    title: '古今衡',
    desc: '跨朝代多维对比分析系统，支持时间轴驱动的数据特征演化研究'
  },
  {
    title: '文渊册',
    desc: 'AI生成交互式三维卷轴报告，支持虚拟现实环境的数据沉浸探查'
  }
];
// 数据生成函数
const generateRandomData = (type) => {
  const random = (min, max) => Math.floor(Math.random() * (max - min) + min)
  const now = new Date()

  return {
    bar: () => Array.from({length: 4}, (_, i) => ({
      name: `类别${String.fromCharCode(65 + i)}`,
      value: random(100, 500)
    })),
    pie: () => {
      const colors = ['#08979c', '#36cfc9', '#87e8de', '#b5f5ec', '#e6fffb']
      return Array.from({length: random(3, 6)}, (_, i) => ({
        name: `类别${i+1}`,
        value: random(50, 300),
        itemStyle: { color: colors[i % colors.length] }
      }))
    },
    scatter: () => Array.from({length: 50}, () => ({
      x: random(0, 20),
      y: random(10, 50),
      symbolSize: random(8, 20)
    }))
  }[type]()
}

// 数据更新逻辑优化
const updateCharts = () => {
  chartData.barChartData = generateRandomData('bar')
  // chartData.lineChartData = generateRandomData('line')
  chartData.pieChartData = generateRandomData('pie')
  chartData.scatterChartData = generateRandomData('scatter')
}
const chartData = reactive({
  barChartData: generateRandomData('bar'),
  // lineChartData: generateRandomData('line'),
  pieChartData: generateRandomData('pie'),
  scatterChartData: generateRandomData('scatter')
});

// 定时更新数据
let updateInterval;
onMounted(() => {
  updateInterval = setInterval(updateCharts, 1000)
})

onBeforeUnmount(() => {
  clearInterval(updateInterval);
});


const options =reactive({
  email: '',
  password: '',
  account: '',
  username: '',
})

const handleLogin = () => {
  get("api/auth/login",{
    password: options.password,
    account: options.account,
  },(message,data) => {
    messageApi.success(message);
    localStorage.setItem("authToken",data);
    router.push("/");
  })
}
const validation = reactive({
  account: '',
  username: '',
  email: '',
  password: '',
})

// 提交注册表单
const handleRegister = () => {
  // 先清空之前的验证信息
  validation.account = '';
  validation.username = '';
  validation.email = '';
  validation.password = '';

  // 验证每个字段是否为空
  if (!options.account) {
    validation.account = '账号不能为空';
  } else if (options.account.length < 5 || options.account.length > 20) {
    validation.account = '账号长度必须在5到20个字符之间';
  }

  if (!options.username) {
    validation.username = '用户名不能为空';
  }

  if (!options.email) {
    validation.email = '邮箱不能为空';
  }

  if (!options.password) {
    validation.password = '密码不能为空';
  }

  // 如果有任何字段为空或验证不通过，终止提交
  if (validation.account || validation.username || validation.email || validation.password) {
    messageApi.error('请填写完整信息并确保每个字段有效！');
    return;
  }

  // 如果所有验证通过，继续提交注册请求
  post("api/auth/register", {
    email: options.email,
    password: options.password,
    account: options.account,
    username: options.username,
  }, (msg) => {
    messageApi.success(msg);
    userStore.showGuide=true;
    isRegistering.value=false;
    options.account='';
    options.username='';
    options.email='';
    options.password='';
    messageApi.success(msg)
  }, (msg) => {
    options.account=''
    messageApi.error(msg);
  });
}

//--------------------------------日志--------------------------

const addRegisterLog=()=>{
  post("api/log/insertLog",{
    type:"注册操作",
    content:"用户"+options.username+"注册了新账号",
  },(message)=>{
    console.log(message);
  })
}

</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50/95 to-cyan-50/95 font-serif pb-24 relative overflow-hidden">
    <!-- 新增水墨背景 -->
    <div class="absolute inset-0 z-0 opacity-10 bg-[url('data:image/svg+xml;base64,PHN2Zy...')]"></div>

    <!-- 标题区 -->
    <div class="relative text-center py-16 space-y-8 animate-text-focus">
      <h1 class="text-7xl bg-clip-text text-transparent bg-gradient-to-r from-cyan-700 to-blue-900 font-cursive tracking-wider">
        文渊数鉴
      </h1>
      <p class="text-xl text-cyan-800/85 max-w-3xl mx-auto leading-relaxed tracking-wider font-medium">
        承千年文脉，铸数字鉴章。本平台融合人工智能与古籍智慧，可解析钟鼎甲骨之文，推演阴阳五行之变，
        较古今数据之异，生成三维交互报告。邀君共探文明基因，同解岁月密码。
      </p>
    </div>

    <!-- 主按钮 -->
    <div class="relative flex justify-center my-16">
      <button
        @click="showAuthModal = true"
        class="group relative px-12 py-5 bg-white/90 backdrop-blur-sm rounded-2xl shadow-xl
               hover:shadow-2xl transition-all duration-500 hover:scale-105
               border-2 border-cyan-500/30 hover:border-cyan-600/50">
        <span class="text-2xl bg-gradient-to-r from-cyan-600 to-blue-700 bg-clip-text text-transparent">
          启鉴
        </span>
        <i class="absolute inset-0 rounded-2xl border border-white/50 opacity-0
                    group-hover:opacity-100 transition-opacity duration-300"></i>
        <i class="absolute -inset-2 rounded-2xl bg-cyan-100/30 blur-xl -z-10
                    group-hover:bg-cyan-200/40 transition-all duration-300"></i>
      </button>
    </div>

    <!-- 功能卡片 -->
    <div class="relative grid grid-cols-1 md:grid-cols-4 gap-8 px-8 mb-24">
      <TransitionGroup name="stagger">
        <div
          v-for="(card, index) in featureCards"
          :key="index"
          class="relative bg-white/95 backdrop-blur-lg p-8 rounded-2xl shadow-xl hover:shadow-2xl
                 transition-all duration-300 transform hover:-translate-y-2 cursor-pointer
                 border-2 border-cyan-100/60 hover:border-cyan-200/90 group
                 before:absolute before:inset-0 before:bg-[url('data:image/svg+xml;base64,PHN2Zy...')] before:opacity-5">
          <div class="relative z-10">
            <h3 class="text-2xl font-bold text-cyan-900 mb-4 flex items-center gap-3">
              <span class="w-8 h-8 bg-cyan-100/80 rounded-full flex items-center justify-center
                            text-cyan-700 group-hover:bg-cyan-200 transition-colors">
                {{ index + 1 }}
              </span>
              {{ card.title }}
            </h3>
            <p class="text-cyan-700/90 leading-relaxed tracking-wide font-medium">{{ card.desc }}</p>
          </div>
        </div>
      </TransitionGroup>
    </div>

    <!-- 图表区 -->
    <!-- 修改后的图表区 -->
    <div class="relative grid grid-cols-2 gap-6 px-10 flex-1 pb-24 w-11/12 mx-auto">
      <!-- 每个图表容器添加高度限制 -->
      <div class="bg-white/90 backdrop-blur-sm rounded-2xl shadow-xl p-6 border border-cyan-100/50 h-[450px]">
        <BarChart
          :rawData="chartData.barChartData"
          :settings="{ title: '古籍分类统计', color: ['#08979c'] }"
          class="h-full"
        />
      </div>

      <div class="bg-white/90 backdrop-blur-sm rounded-2xl shadow-xl p-6 border border-cyan-100/50 h-[450px]">
        <LineChart />
      </div>

      <div class="bg-white/90 backdrop-blur-sm rounded-2xl shadow-xl p-6 border border-cyan-100/50 h-[450px]">
        <PieChart
          :rawData="chartData.pieChartData"
          :settings="{ title: '文献类型分布', color: ['#08979c', '#36cfc9', '#87e8de'] }"
          class="h-full"
        />
      </div>

      <div class="bg-white/90 backdrop-blur-sm rounded-2xl shadow-xl p-6 border border-cyan-100/50 h-[450px]">
        <ScatterChart
          :rawData="chartData.scatterChartData"
          :settings="{ title: '数据相关性分析', color: '#08979c', symbolSize: 12 }"
          class="h-full"
        />
      </div>
      <div class="bg-white/90 backdrop-blur-sm rounded-2xl shadow-xl p-6 border border-cyan-100/50 h-[450px]">
        <VirtualAreaChart />
      </div>
      <div class="bg-white/90 backdrop-blur-sm rounded-2xl shadow-xl p-6 border border-cyan-100/50 h-[450px]">
        <VirtualBoxPlotChart />
      </div>
      <div class="bg-white/90 backdrop-blur-sm rounded-2xl shadow-xl p-6 border border-cyan-100/50 h-[450px]">
        <VirtualRadarChart/>
      </div>
      <div class="bg-white/90 backdrop-blur-sm rounded-2xl shadow-xl p-6 border border-cyan-100/50 h-[450px]">
        <VirtualWordCloud/>
      </div>
    </div>


    <!-- 卷轴弹窗 -->
    <Transition name="scroll">
      <div
        v-if="showAuthModal"
        @click.self="showAuthModal = false"
        class="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center">
        <div class="relative w-[480px] bg-white rounded-3xl shadow-2xl overflow-hidden border border-cyan-100 animate-modal-in">
          <div class="p-12 space-y-8">
            <h2 class="text-3xl font-bold text-cyan-800 text-center">
              {{ isRegistering ? '立册文渊阁' : '登鉴天禄阁' }}
            </h2>

            <!-- 注册表单 -->
            <div v-show="isRegistering">
              <div class="space-y-6">
                <div class="mb-4">
                  <label for="account" class="block text-sm font-medium text-gray-700">文牒编号 ( 账号：建议使用手机号且不允许修改 )</label>
                  <input
                    type="text"
                    id="account"
                    v-model="options.account"
                    class="w-full mt-2 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="请赐教贵客文牒编号"
                  />
                  <p v-if="validation.account" class="text-red-500 text-sm mt-1">{{ validation.account }}</p>
                </div>

                <div class="mb-4">
                  <label for="username" class="block text-sm font-medium text-gray-700">丹青雅号（昵称）</label>
                  <input
                    type="text"
                    id="username"
                    v-model="options.username"
                    class="w-full mt-2 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="请题写墨宝雅称"
                  />
                  <p v-if="validation.username" class="text-red-500 text-sm mt-1">{{ validation.username }}</p>
                </div>

                <div class="mb-4">
                  <label for="email" class="block text-sm font-medium text-gray-700">飞鸢传书（邮箱）</label>
                  <input
                    type="email"
                    id="email"
                    v-model="options.email"
                    class="w-full mt-2 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="请留青鸟可达之处"
                  />
                  <p v-if="validation.email" class="text-red-500 text-sm mt-1">{{ validation.email }}</p>
                </div>

                <div class="mb-6">
                  <label for="password" class="block text-sm font-medium text-gray-700">密文符印（密码）</label>
                  <input
                    type="password"
                    id="password"
                    v-model="options.password"
                    class="w-full mt-2 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="请设玄机暗语"
                  />
                  <p v-if="validation.password" class="text-red-500 text-sm mt-1">{{ validation.password }}</p>
                </div>
              </div>
            </div>

            <!-- 登录表单 -->
            <div v-show="!isRegistering" class="space-y-6">
              <label class="classic-label font-bold">文牒验符（账号）</label>
              <input
                type="text"
                v-model="options.account"
                placeholder="请出示文渊阁牒"
                class="w-full px-5 py-3 rounded-xl border-2 border-cyan-100/80 focus:border-cyan-400/50 focus:ring-2 focus:ring-cyan-200/30 transition-all duration-300 placeholder-cyan-500/50 bg-white/50 backdrop-blur-sm">
              <label class="classic-label font-bold">金匮秘钥（密码）</label>
              <input
                type="password"
                v-model="options.password"
                placeholder="请启玄机之钥"
                class="w-full px-5 py-3 rounded-xl border-2 border-cyan-100/80 focus:border-cyan-400/50 focus:ring-2 focus:ring-cyan-200/30 transition-all duration-300 placeholder-cyan-500/50 bg-white/50 backdrop-blur-sm">
            </div>

            <!-- 按钮 -->
            <button v-show="isRegistering"
                    @click="handleRegister"
                    class="w-full py-4 bg-gradient-to-r from-cyan-500 to-blue-600 rounded-xl text-white font-semibold tracking-wide hover:shadow-xl transition-all duration-300 transform hover:scale-[1.02] active:scale-95">
              钤印立册
            </button>
            <button v-show="!isRegistering"
                    @click="handleLogin"
                    class="w-full py-4 bg-gradient-to-r from-cyan-500 to-blue-600 rounded-xl text-white font-semibold tracking-wide hover:shadow-xl transition-all duration-300 transform hover:scale-[1.02] active:scale-95">
              执钥启鉴
            </button>

            <!-- 切换登录与注册 -->
            <p class="text-center text-cyan-700/80">
              {{ isRegistering ? '已有天禄阁文牒？' : '未录册于文渊阁？'}}
              <button
                @click="isRegistering = !isRegistering"
                class=" text-cyan-600 hover:text-cyan-700 font-semibold underline underline-offset-4 decoration-cyan-200/50 hover:decoration-cyan-300">
                {{ isRegistering ? '转道天禄阁' : '移步册府司' }}
              </button>
            </p>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>



<style>
.classic-label {
  @apply block text-sm font-medium text-cyan-900/80 pl-1;
}

@keyframes chart-in {
  0% { opacity: 0; transform: translateY(20px); }
  100% { opacity: 1; transform: translateY(0); }
}
.margin-bottom-custom {
  margin-bottom: 4rem; /* 例如自定义间距为 4rem */
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(3deg); }
}

@keyframes text-focus {
  0% { opacity: 0; transform: scale(0.95); }
  100% { opacity: 1; transform: scale(1); }
}

@keyframes card-in {
  0% { opacity: 0; transform: translateY(20px) rotateX(-45deg); }
  100% { opacity: 1; transform: translateY(0) rotateX(0); }
}

@keyframes chart-in {
  0% { opacity: 0; transform: scaleX(0); }
  100% { opacity: 1; transform: scaleX(1); }
}

.animate-float {
  animation: float 8s ease-in-out infinite;
}

.animate-text-focus {
  animation: text-focus 1.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.animate-card-in {
  animation: card-in 0.6s ease-out forwards;
}

.animate-chart-in {
  animation: chart-in 0.8s cubic-bezier(0.68, -0.55, 0.27, 1.55) forwards;
}

.scroll-enter-active {
  animation: modal-in 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.scroll-leave-active {
  animation: modal-in 0.4s reverse;
}

@keyframes modal-in {
  0% {
    transform: scale(0.8) rotateX(15deg);
    opacity: 0;
  }
  100% {
    transform: scale(1) rotateX(0);
    opacity: 1;
  }
}
</style>
