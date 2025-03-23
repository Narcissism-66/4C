<script setup>
import { onMounted, reactive } from 'vue'
import { get, post } from '@/net/index.js'
import { parseDateTime } from '@/time/Time.js'
import router from '@/router/index.js'
import { message } from 'ant-design-vue'

const [messageApi, contextHolder] = message.useMessage();
const option = reactive({
  report: []
})

const getAllReportAndShow = () => {
  get('api/report/getAllReportAndShow', {}, (message, data) => {
    option.report = data;
  })
}
const goReport = (id,name) => {
  ViewReportLog(id,name);
  router.push('/viewReport/' + id);
}

const deleteReport = (id,name) => {
  if (confirm('确定要删除这份报告吗？')) {
    post('api/report/updateReportShow', {
      id: id,
      isshow:'false',
    }, (message) => {
      messageApi.success(message);
      getAllReportAndShow();
      DeleteReportLog(id,name);
    })
  }
}
const ViewReportLog=(id,name)=>{
  post('/api/log/insertLog',{
    type:"数据报告",
    content:`查看了数据报告: ${name} (${id}) `,
  },(msg)=>{
    console.log(msg);
  })
}
const DeleteReportLog=(id,name)=>{
  post('/api/log/insertLog',{
    type:"删除报告",
    content:`删除了数据报告: ${name} (${id}) `,
  },(msg)=>{
    console.log(msg);
  })
}
onMounted(() => {
  getAllReportAndShow();
})
</script>

<template>
  <!-- 修改后的模板部分 -->
  <div class="min-h-screen bg-white">
    <div class="max-w-7xl mx-auto px-4 py-12">
      <!-- 标题部分加入传统云纹装饰 -->
      <div class="relative mb-16">
        <div class="absolute left-0 right-0 top-1/2 h-1 bg-gradient-to-r from-transparent via-blue-300 to-transparent"></div>
        <h1 class="relative z-10 font-huakang text-4xl text-center bg-white px-8 inline-block">
          <span class="text-blue-800">文献研析阁</span>
          <span class="text-gray-600 ml-4">—— 格物致知·明理求真</span>
        </h1>
      </div>

      <!-- 卡片容器加入传统边框样式 -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
        <div
          v-for="report in option.report"
          class="relative bg-white border-2 border-blue-50 rounded-lg shadow-lg transition-all duration-300 hover:shadow-xl hover:border-blue-100"
          style="background-image: linear-gradient(to bottom right, #f8fafc 0%, #f0f9ff 100%);"
        >
          <!-- 卡片顶部装饰线 -->
          <div class="h-2 bg-gradient-to-r from-blue-100 to-cyan-100 rounded-t-lg"></div>

          <div class="p-6">
            <!-- 标题部分加入古籍样式 -->
            <div class="flex items-center mb-4">
              <div class="mr-3 text-blue-600">
                <svg class="w-6 h-6" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
                </svg>
              </div>
              <h2 class="text-xl font-semibold text-blue-800 font-fangsong truncate">
                《{{ report.filename }}》
              </h2>
            </div>

            <!-- 时间显示样式 -->
            <div class="flex items-center text-sm text-gray-600 mb-6 border-l-4 border-blue-100 pl-3">
              <svg class="w-4 h-4 mr-1 text-blue-400" fill="none" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
              </svg>
              <span class="font-mono">{{ parseDateTime(report.time) }}</span>
            </div>

            <!-- 操作按钮组 -->
            <div class="flex justify-between items-center space-x-4">
              <button @click="goReport(report.id,report.filename)"
                      class="flex-1 flex items-center justify-center px-4 py-2 bg-blue-50 border border-blue-200 text-blue-700 rounded-md hover:bg-blue-100 transition-colors">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                </svg>
                阅览
              </button>
              <button @click="deleteReport(report.id,report.filename)"
                      class="px-3 py-2 text-red-600 hover:text-red-700 transition-colors">
                <svg class="w-5 h-5" fill="none" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                </svg>
              </button>
              <span class="text-xs px-2 py-1 rounded-full bg-cyan-100 text-cyan-800 border border-cyan-200">
                {{ report.analyse === "未使用AI分析" ? "原始考据" : "智析" }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
/* 引入在线书法字体 */
@import url('https://fonts.googleapis.com/css2?family=Ma+Shan+Zheng&family=ZCOOL+XiaoWei&display=swap');

.font-huakang {
  font-family: 'Ma Shan Zheng', cursive;
}

.font-fangsong {
  font-family: 'ZCOOL XiaoWei', serif;
}

/* 传统纹样背景 */
.bg-pattern::before {
  content: "";
  position: absolute;
  inset: 0;
  opacity: 0.03;
  background-image: url("data:image/svg+xml,%3Csvg width='52' height='26' viewBox='0 0 52 26' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M10 10c0-2.21-1.79-4-4-4-3.314 0-6-2.686-6-6h2c0 2.21 1.79 4 4 4 3.314 0 6 2.686 6 6 0 2.21 1.79 4 4 4 3.314 0 6 2.686 6 6 0 2.21 1.79 4 4 4v2c-3.314 0-6-2.686-6-6 0-2.21-1.79-4-4-4-3.314 0-6-2.686-6-6zm25.464-1.95l8.486 8.486-1.414 1.414-8.486-8.486 1.414-1.414z' fill='%23007bff' fill-opacity='0.1' fill-rule='evenodd'/%3E%3C/svg%3E");
}

/* 卡片入场动画 */
@keyframes cardEntrance {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.relative {
  animation: cardEntrance 0.6s ease-out forwards;
  //animation-delay: calc(var(--index) * 0.1s);
}
</style>
