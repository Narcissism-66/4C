<script setup>
import { nextTick, onMounted, reactive, ref, watch } from 'vue'
import { userUserStore } from '@/stores/UserStore.js'
import router from '@/router/index.js'
import { get } from '@/net/index.js'

const userStore = userUserStore()

const options = reactive({
  report: '',
})

const getLastReport = () => {
  get('api/report/getLastReport', {}, (message, data) => {
    options.report = data;
  })
}

const getTotalReportCount = () => {
  get('api/report/getTotalReportCount', {}, (message, data) => {
    uploadedDataCount.value = data;
  })
}

const goReport = (id) => {
  router.push('/viewReport/' + id);
}

const uploadedDataCount = ref(0)



//--------------------------------------------引导--------------------------------------
const currentStep = ref(0);
const highlightStyle = ref({});
const showGuide = ref(false)
const showInitialPrompt = ref(false);
const arrowDirection = ref('bottom')
const guidePosition = ref({})

// 锁定滚动
const lockScroll = (shouldLock) => {
  document.body.style.overflow = shouldLock ? 'hidden' : ''
  document.documentElement.style.overflow = shouldLock ? 'hidden' : ''
}
const steps = [
  {
    selector: '.welcome-section',
    description: '欢迎来到文渊数鉴！点击"司南指路"开始探索',
    position: 'auto', // 特殊定位模式
    offset: 0
  },
  {
    selector: '.feature-card:nth-child(1)',
    description: '若需博采众家，可在此观「诸子百家」之汇通，察典册源流之脉络',
    position: 'auto',
    offset: 20
  },
  {
    selector: '.feature-card:nth-child(2)',
    description: '欲究一典之奥义，当以「丹铅甲乙」之法，穷章句之精微',
    position: 'auto',
    offset: 20
  },
  {
    selector: '.feature-card:nth-child(3)',
    description: '心有疑窦未解，可效「程门立雪」之诚，与往圣先贤对话论道',
    position: 'auto',
    offset: 300
  },
  {
    selector: '.feature-card:nth-child(4)',
    description: '稽古考今之所得，皆藏于「石室金匮」，可随时披览先哲遗珠',
    position: 'auto',
    offset: 300
  },
  {
    selector: '.data-overview',
    description: '数据总览：实时掌握文献分析进度与成果',
    position: 'auto', // 自动选择最佳位置
    offset: 150
  },
]

const calculateHighlight = async () => {
  await nextTick();
  const step = steps[currentStep.value];
  const target = document.querySelector(step.selector);

  if (target) {
    const rect = target.getBoundingClientRect();
    highlightStyle.value = {
      top: `${rect.top}px`,
      left: `${rect.left}px`,
      width: `${rect.width}px`,
      height: `${rect.height}px`,
    };
    calculateGuidePosition(target, step);
  }
};

const calculateGuidePosition = (target, step) => {
  const rect = target.getBoundingClientRect()
  const viewportWidth = window.innerWidth
  const viewportHeight = window.innerHeight

  // 新增：考虑步骤的offset偏移量
  const baseOffset = 20 + (step.offset || 0)

  // 位置配置（新增offset计算）
  const positions = {
    top: {
      y: rect.top - baseOffset,
      x: rect.left + rect.width/2,
      dir: 'bottom'
    },
    bottom: {
      y: rect.bottom + baseOffset,
      x: rect.left + rect.width/2,
      dir: 'top'
    },
    left: {
      y: rect.top + rect.height/2,
      x: rect.left - baseOffset,
      dir: 'right'
    },
    right: {
      y: rect.top + rect.height/2,
      x: rect.right + baseOffset,
      dir: 'left'
    }
  }

  // 优化后的空间判断逻辑
  const spaceThreshold = 200 // 最小保留空间
  let bestPosition = 'bottom'

  // 计算各方向可用空间
  const availableSpace = {
    top: rect.top - spaceThreshold,
    bottom: viewportHeight - rect.bottom - spaceThreshold,
    left: rect.left - spaceThreshold,
    right: viewportWidth - rect.right - spaceThreshold
  }

  // 根据最大可用空间选择位置
  const spacePriority = ['bottom', 'right', 'left', 'top']
  bestPosition = spacePriority.reduce((maxKey, key) =>
    availableSpace[key] > availableSpace[maxKey] ? key : maxKey
  )

  // 新增：边界安全检查
  const cardWidth = 280
  const cardHeight = 120
  let finalPosition = bestPosition
  let adjustX = 0
  let adjustY = 0

  // 水平方向安全检测
  if (positions[finalPosition].x < cardWidth/2) {
    adjustX = cardWidth/2 - positions[finalPosition].x
  } else if (positions[finalPosition].x > viewportWidth - cardWidth/2) {
    adjustX = viewportWidth - cardWidth/2 - positions[finalPosition].x
  }

  // 垂直方向安全检测
  if (positions[finalPosition].y < cardHeight) {
    adjustY = cardHeight - positions[finalPosition].y
  } else if (positions[finalPosition].y > viewportHeight - cardHeight) {
    adjustY = viewportHeight - cardHeight - positions[finalPosition].y
  }

  arrowDirection.value = positions[finalPosition].dir
  guidePosition.value = {
    top: `${positions[finalPosition].y + adjustY}px`,
    left: `${positions[finalPosition].x + adjustX}px`,
    transform: `
      ${finalPosition === 'top' || finalPosition === 'bottom' ? 'translateX(-50%)' : 'translateY(-50%)'}
      ${adjustX ? `translateX(${adjustX}px)` : ''}
    `,
    'transform-origin': 'center center'
  }
}

watch(currentStep, () => {
  lockScroll(true)
  calculateHighlight()
});

const nextStep = () => {
  if (currentStep.value < steps.length - 1) {
    currentStep.value++;
  } else {
    showGuide.value = false;
    lockScroll(false)
  }
};
const startGuide = () => {
  showInitialPrompt.value = false;
  showGuide.value = true;
  nextTick(() => {
    lockScroll(true)
    calculateHighlight();
  })
}
//--------------------------------------------引导--------------------------------------

onMounted(() => {
  getLastReport();
  getTotalReportCount();
  showInitialPrompt.value=userStore.showGuide
})
</script>

<template>
  <div class="dashboard-container">
    <!-- 新手引导 -->
    <!-- 初始提示 -->
    <div v-if="showInitialPrompt" class="initial-prompt">
      <div class="prompt-box">
        <h3>欢迎使用文渊数鉴</h3>
        <p>即将为你开始新手教程</p>
        <button @click="startGuide" class="confirm-button">立即开始</button>
      </div>
    </div>
    <div v-if="showGuide" class="onboarding-guide">
      <div class="highlight-box" :style="highlightStyle"></div>

      <div v-for="(step, index) in steps"
           :key="index"
           v-show="currentStep === index"
           class="guide-card"
           :style="guidePosition">
        <div class="guide-arrow" :class="arrowDirection"></div>
        <p class="guide-text">{{ step.description }}</p>
        <button @click="nextStep" class="next-button">
          {{ index === steps.length - 1 ? '开始探索' : '下一步' }}
        </button>
      </div>
    </div>

    <main class="main-content">
      <!-- 欢迎区域 -->
      <section class="welcome-section animate__animated animate__fadeIn">
        <div class="cloud-pattern"></div>
        <div class="welcome-content">
          <h1 class="calligraphy-font">天工开物·文渊数鉴</h1>
          <p class="subtitle">承古观今 · 数通乾坤</p>
          <div class="deco-line"></div>
        </div>
      </section>

      <!-- 功能入口 -->
      <section class="grid-container w-full">
        <div
          class="feature-card animate__animated animate__fadeInUp"
          v-for="(card, index) in
          [
              {   // 多文件合并分析
                  icon: 'compass',                // 司南指引方向
                  title: '万象合璧',
                  color: '深蓝',
                  path: '/customize',
                  badge: '天工开物',
                  desc: '多位数据齐平展示'
              },
              {   // 单文件可视化
                  icon: 'star-chart',             // 敦煌星图
                  title: '观象绘影',
                  color: '墨绿',
                  path: '/single',
                  badge: '禹贡九州',
                  desc: '单文件深度解析'
              },
              {   // AI对话
                  icon: 'bamboo-slip',           // 简牍文书
                  title: '天工智语',
                  color: '绛紫',
                  path: '/AiChat',
                  badge: '周髀算经',
                  desc: '智能问答交互'
              },
              {   // 报告存储
                  icon: 'ancient-library',      // 藏书楼阁
                  title: '金匮典藏',
                  color: '赭红',
                  path: '/report',
                  badge: 'report',
                  desc: '报告永久存档'
              }
          ]"
          :key="index"
          @click="router.push(card.path)"
        >
          <div class="card-badge" :class="card.color">{{ card.desc }}</div>
          <i class="fas fa-{{ card.icon }}"></i>
          <h3>{{ card.title }}</h3>
          <div class="hover-wave"></div>
        </div>
      </section>

      <!-- 数据概览 -->
      <section class="data-overview">
        <div class="data-card animate__animated animate__fadeIn"
             v-for="(item, index) in [
            { title: '典藏总量', value: uploadedDataCount, unit: '卷', icon: 'database' },
            { title: '近期研习', value: userStore.learn?userStore.learn:'暂无数据',  unit: '',icon: 'chart-line' },
            { title: '最新策论', value: options.report ? options.report.filename : '暂无数据',unit: '', icon: 'file-alt' }
          ]" :key="index">
          <i class="fas fa-{{ item.icon }}"></i>
          <div class="data-content">
            <h4>{{ item.title }}</h4>
            <p @click="index === 2 && options.report && goReport(options.report.id)">
              {{ item.value +item.unit}}
            </p>
          </div>
          <div class="deco-border"></div>
        </div>
      </section>


    </main>

    <footer class="cultural-footer">
      <p>文渊数枢 · 甲辰制 · <a href="#">观星台</a> · <a href="#">墨守规</a></p>
      <div class="seal-mark">
        <span class="seal-text rounded-xl">天工开物·文渊数鉴</span>
        <div class="seal-border"></div>
      </div>
    </footer>
  </div>
</template>



<style scoped>

.initial-prompt {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  z-index: 10000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.prompt-box {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  max-width: 400px;
}

.prompt-box h3 {
  color: #1a237e;
  margin-bottom: 1rem;
}

.prompt-box p {
  margin: 1rem 0;
  color: #666;
}

.confirm-button {
  background: #1976d2;
  color: white;
  border: none;
  padding: 8px 24px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 1rem;
  transition: background 0.3s ease;
}

.confirm-button:hover {
  background: #1565c0;
}
/* 优化层级和定位 */
.onboarding-guide {
  z-index: 9998; /* 降低遮罩层级 */
}

.highlight-box {
  z-index: 9999; /* 高亮区域在遮罩上方 */
}

.guide-card {
  z-index: 10000; /* 引导卡片在最顶层 */
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); /* 平滑动画 */
}

/* 新增：引导卡片动态偏移 */
.guide-card::after {
  content: '';
  position: absolute;
  border: 8px solid transparent;
}

.guide-card .guide-arrow.top {
  bottom: -16px;
  border-top-color: white;
}

.guide-card .guide-arrow.bottom {
  top: -16px;
  border-bottom-color: white;
}

.guide-card .guide-arrow.left {
  right: -16px;
  border-left-color: white;
}

.guide-card .guide-arrow.right {
  left: -16px;
  border-right-color: white;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .guide-card {
    width: 90vw;
    max-width: none;
    left: 50% !important;
    transform: translateX(-50%) !important;
  }

  .guide-card .guide-arrow {
    display: none;
  }
}
/* 增强引导样式 */
.onboarding-guide {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 9999;
  pointer-events: none;
}

.highlight-box {
  position: absolute;
  border: 2px solid rgba(255, 212, 0, 0.8);
  border-radius: 8px;
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.5);
  pointer-events: none;
  transition: all 0.3s ease;
}

.guide-card {
  position: absolute;
  background: white;
  border-radius: 12px;
  padding: 20px;
  width: 280px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  pointer-events: auto;
  z-index: 10000;
}

.guide-arrow {
  position: absolute;
  width: 12px;
  height: 12px;
  background: white;
  transform: rotate(45deg);
}

.guide-arrow.top {
  bottom: -6px;
  left: 50%;
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.1);
}
.guide-arrow.bottom {
  top: -6px;
  left: 50%;
}
.guide-arrow.left {
  right: -6px;
  top: 50%;
}
.guide-arrow.right {
  left: -6px;
  top: 50%;
}

.next-button {
  display: block;
  margin-top: 15px;
  padding: 8px 20px;
  background: #1976d2;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  float: right;
}


/* 具体定位策略 */
.guide-card[data-position="top"] {
  bottom: calc(100% + 20px);
  left: 50%;
  transform: translateX(-50%);
}
.guide-card[data-position="bottom"] {
  top: calc(100% + 20px);
  left: 50%;
  transform: translateX(-50%);
}
.guide-card[data-position="left"] {
  right: calc(100% + 20px);
  top: 50%;
  transform: translateY(-50%);
}
.guide-card[data-position="right"] {
  left: calc(100% + 20px);
  top: 50%;
  transform: translateY(-50%);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .guide-card {
    width: 80vw;
    max-width: 300px;
  }

  .guide-card[data-position="left"],
  .guide-card[data-position="right"] {
    left: 50%;
    right: auto;
    top: auto;
    bottom: 20px;
    transform: translateX(-50%);
  }
}
/* 新增引导样式 */
.onboarding-guide {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 9999;
}

.highlight-box {
  position: fixed;
  background: rgba(255, 255, 0, 0.2);
  border: 2px solid rgba(255, 212, 0, 0.8);
  border-radius: 8px;
  box-shadow: 0 0 15px rgba(255, 212, 0, 0.3);
}


/* 调整原有卡片间距 */
.grid-container {
  gap: 2rem;
  padding: 0 2rem;
}

.feature-card {
  position: relative;
  transition: transform 0.3s ease;

  &:hover {
    transform: translateY(-5px) scale(1.02);
  }
}
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@500&display=swap');

.dashboard-container {
  min-height: 100vh;
  background: #f1f1f1;
  color: #2c3e50;
  font-family: 'Noto Serif SC', serif;
}

/* 欢迎区域 */
.welcome-section {
  position: relative;
  height: 280px;
  background: linear-gradient(135deg, #1976d2 0%, #0d47a1 100%);
  border-radius: 16px;
  overflow: hidden;
  margin: 2rem 0;
  box-shadow: 0 8px 24px rgba(13, 71, 161, 0.15);
  animation: sectionIn 1.5s ease-out;
}

.cloud-pattern {
  position: absolute;
  width: 100%;
  height: 100%;
  background:
    linear-gradient(45deg, transparent 95%, rgba(255,255,255,0.1) 96%),
    linear-gradient(-45deg, transparent 95%, rgba(255,255,255,0.1) 96%);
  background-size: 20px 20px;
  opacity: 0.3;
  animation: cloudMovement 30s linear infinite;
}

.welcome-content {
  position: relative;
  padding: 2rem;
  text-align: center;
  color: white;
}

.calligraphy-font {
  display: inline-block;
  overflow: hidden;
  white-space: nowrap;
  width: 0;
  animation: calligraphyWrite 4s ease-in-out forwards, fadeIn 1s ease-in-out;
  font-size: 2.5rem;
  margin: 1rem 0;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.subtitle {
  font-size: 1.2rem;
  letter-spacing: 2px;
  opacity: 0.9;
}

.deco-line {
  width: 60px;
  height: 2px;
  background: #ffd600;
  margin: 1.5rem auto;
  box-shadow: 0 0 8px rgba(255,214,0,0.3);
  animation: decoLine 1s ease-in-out infinite;
}

/* 功能入口 */
.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.5rem;
  margin: 2rem 0;
}

.feature-card {
  position: relative;
  background: #e3f2fd;
  border-radius: 12px;
  padding: 2rem;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border: 1px solid #e1e4e8;
  animation: bounceInUp 1s ease-in-out;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(13, 71, 161, 0.1);
}

.feature-card i {
  font-size: 2rem;
  color: #1a237e;
  margin-bottom: 1rem;
  animation: bounceIn 1s ease-in-out;
}

.feature-card h3 {
  font-family: 'Noto Serif SC', serif;
  color: #1a237e;
}

.card-badge {
  position: absolute;
  top: -10px;
  right: 0px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: bold;
}

.card-badge.blue { background: #e3f2fd; color: #1a237e; }
.card-badge.green { background: #e8f5e9; color: #2e7d32; }
.card-badge.purple { background: #f3e5f5; color: #6a1b9a; }
.card-badge.red { background: #ffebee; color: #c62828; }

.hover-wave {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 50% 50%, rgba(26,35,126,0.1) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.feature-card:hover .hover-wave {
  opacity: 1;
}

/* 数据概览 */
.data-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin: 2rem 0;
}

.data-card {
  background: #e3f2fd;
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  position: relative;
  border: 1px solid #e1e4e8;
  animation: fadeInUp 1s ease-in-out;
}

.data-card i {
  font-size: 1.8rem;
  color: #1a237e;
  margin-right: 1.5rem;
}

.data-content h4 {
  color: #666;
  margin-bottom: 0.5rem;
}

.data-content p {
  font-size: 1.2rem;
  font-weight: bold;
  color: #1a237e;
}



.deco-border {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, #1a237e 0%, #0d47a1 100%);
}



.panel-header i {
  color: #1a237e;
  margin-right: 1rem;
}

.notification-panel ul {
  padding: 1rem;
}

.notification-panel li {
  padding: 1rem;
  margin: 0.5rem 0;
  background: #f8f9fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  animation: fadeIn 0.5s ease-out;
}

.notification-panel li i {
  color: #2e7d32;
  margin-right: 1rem;
}

/* 页脚 */
.cultural-footer {
  text-align: center;
  padding: 2rem;
  color: #666;
  position: relative;
}

.seal-mark {
  position: relative;
  display: inline-block;
  margin-top: 1rem;
  animation: sealFloat 2s ease-in-out infinite;
}

.seal-text {
  font-family: 'Noto Serif SC', serif;
  color: #1a237e;
  padding: 0 1rem;
}

.seal-border {
  position: absolute;
  top: 50%;
  left: 0;
  width: 100%;
  height: 2px;
  background: #1a237e;
  transform: translateY(-50%);
  z-index: 1;
}

.seal-text {
  position: relative;
  z-index: 2;
  background: #f9fbfd;
}

/* 动画 */
@keyframes calligraphyWrite {
  0% { width: 0; }
  100% { width: 100%; }
}

@keyframes cloudMovement {
  0% { transform: translateX(0); }
  100% { transform: translateX(-100%); }
}

@keyframes sectionIn {
  0% { opacity: 0; transform: translateY(-20px); }
  100% { opacity: 1; transform: translateY(0); }
}

@keyframes bounceInUp {
  0% { transform: translateY(20px); opacity: 0; }
  60% { transform: translateY(-10px); opacity: 1; }
  100% { transform: translateY(0); }
}

@keyframes fadeIn {
  0% { opacity: 0; }
  100% { opacity: 1; }
}

@keyframes decoLine {
  0% { transform: scaleX(0); }
  100% { transform: scaleX(1); }
}

@keyframes fadeInUp {
  0% { opacity: 0; transform: translateY(20px); }
  100% { opacity: 1; transform: translateY(0); }
}

@keyframes sealFloat {
  0% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
  100% { transform: translateY(0); }
}
</style>
