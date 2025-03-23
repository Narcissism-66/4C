<script setup>
import { ref, reactive, onMounted } from 'vue'
import { get, post } from '@/net/index.js'
import { userUserStore } from '@/stores/UserStore.js'
import { message } from 'ant-design-vue'
import { parseDateTime } from '@/time/Time.js'

const [messageApi, contextHolder] = message.useMessage();
const userStore = userUserStore();

const typeLabels = {
  issue: '问题反馈',
  suggestion: '功能建议',
  other: '其他'
}

const newFeedback = reactive({
  title: '',
  content: '',
  type: "issue",
  rating: 5,
})

const option = reactive({
  feedback: [],
})

const addFeedback = () => {
  post('api/feedback/addFeedback', {
    userId: userStore.user.id,
    title: newFeedback.title,
    content: newFeedback.content,
    type: newFeedback.type,
    evaluate: newFeedback.rating,
  }, (message) => {
    messageApi.success(message);
    FeedbackLog(newFeedback.title)
    newFeedback.title = ''
    newFeedback.content = ''
    newFeedback.type = 'issue'
    newFeedback.rating = 5
    getFeedbackByUserId();
  })
}

const FeedbackLog = (title) => {
  post("api/log/insertLog", {
    type: "反馈信息",
    content: `Id:${userStore.user.id}的用户提交了反馈：${title}`
  }, (msg) => {
    console.log(msg)
  })
}

const typeClasses = {
  issue: 'bg-blue-100 text-blue-800 border border-blue-300',
  suggestion: 'bg-blue-50 text-blue-600 border border-blue-200',
  other: 'bg-blue-200 text-blue-900 border border-blue-400'
}

const getFeedbackByUserId = () => {
  get('api/feedback/getFeedbackByUserId', {}, (message, data) => {
    option.feedback = data;
  })
}

onMounted(() => {
  getFeedbackByUserId();
})
</script>

<template>
  <contextHolder/>
  <div class="min-h-screen bg-[#f7fbff] p-8 font-chinese relative overflow-hidden">
    <!-- 青花瓷纹背景 -->
    <div class="absolute inset-0 opacity-10 bg-repeat pointer-events-none"
         style="background-image: url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CiAgPHBhdGggZD0iTTAgMTBjMjAgMCAyMC0xMCA0MC0xMHY0MGMtMjAgMC0yMCAxMC00MCAxMFYxMHoiIGZpbGw9IiMyYzVmOGQiIG9wYWNpdHk9IjAuMSIvPgogIDxwYXRoIGQ9Ik00MCAzMGMtMjAgMC0yMCAxMC00MCAxMFYtMTBjMjAgMCAyMC0xMCA0MC0xMHY0MHoiIGZpbGw9IiMzYTdjYTUiIG9wYWNpdHk9IjAuMSIvPgo8L3N2Zz4=')">
    </div>

    <div class="max-w-4xl mx-auto space-y-8 relative z-10">
      <!-- 卷轴式表单 -->
      <div class="bg-[#f7fbff] p-8 shadow-xl border-8 border-[#e0e7f2]
                  relative scroll-container before:absolute before:left-0 before:right-0
                  before:h-12 before:bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwJSIgaGVpZ2h0PSI0OHB4IiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPgogIDxyZWN0IHdpZHRoPSIxMDAlIiBoZWlnaHQ9IjQ4IiBmaWxsPSIjZTBjZmY1Ii8+CiAgPHBhdGggZD0iTTAgMjRjMTYtOCAzMi04IDQ4LTAgMTYgOCAzMiA4IDQ4IDAgMTYtOCAzMi04IDQ4IDB2MjRIMHYtMjR6IiBmaWxsPSIjYjhkZmY1Ii8+Cjwvc3ZnPg=='))]
                  before:top-0 after:absolute after:left-0 after:right-0 after:h-12
                  after:bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwJSIgaGVpZ2h0PSI0OHB4IiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPgogIDxyZWN0IHdpZHRoPSIxMDAlIiBoZWlnaHQ9IjQ4IiBmaWxsPSIjZTBjZmY1Ii8+CiAgPHBhdGggZD0iTTAgMjRjMTYtOCAzMi04IDQ4LTAgMTYgOCAzMiA4IDQ4IDAgMTYtOCAzMi04IDQ4IDB2MjRIMHYtMjR6IiBmaWxsPSIjYjhkZmY1IiB0cmFuc2Zvcm09InJvdGF0ZSgxODApIiB0cmFuc2Zvcm0tb3JpZ2luPSJjZW50ZXIiLz4KPC9zdmc+'))]
                  after:bottom-0">
        <h2 class="text-4xl font-bold mb-6 text-center text-[#2c5f8d]
                   relative pb-4 scroll-title">
          <span class="bg-clip-text text-transparent bg-gradient-to-r from-[#2c5f8d] to-[#3a7ca5]">
            云章启事
          </span>
          <div class="absolute bottom-0 left-1/2 -translate-x-1/2 w-32 h-1 bg-[#3a7ca5]
                      rounded-full opacity-50"></div>
        </h2>

        <div class="space-y-6">
          <!-- 表单项 -->
          <div class="relative">
            <label class="block text-lg mb-2 font-medium text-[#2c5f8d]
                          flex items-center">
              <span class="icon-container w-8 h-8 bg-[#3a7ca5] rounded-full
                          flex items-center justify-center mr-2">
                <svg class="w-5 h-5 text-[#f3f9ff]" viewBox="0 0 24 24">
                  <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm1-13h-2v6h2V7zm0 8h-2v2h2v-2z"/>
                </svg>
              </span>
              标题
            </label>
            <input
              v-model="newFeedback.title"
              type="text"
              required
              class="w-full p-3 border-2 border-[#c5d9e8] rounded-lg
                     focus:ring-2 focus:ring-[#3a7ca5] focus:border-[#5c9cc5]
                     bg-[#ffffff] shadow-sm transition-all duration-300
                     placeholder-[#8fafc1]"
              placeholder="请输入反馈标题"
            />
          </div>

          <!-- 内容输入 -->
          <div class="relative">
            <label class="block text-lg mb-2 font-medium text-[#2c5f8d] flex items-center">
              <span class="icon-container w-8 h-8 bg-[#3a7ca5] rounded-full
                          flex items-center justify-center mr-2">
                <svg class="w-5 h-5 text-[#f3f9ff]" viewBox="0 0 24 24">
                  <path fill="currentColor" d="M14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm4 18H6V4h7v5h5v11zm-9-5H8v-2h2v2zm0-4H8v-2h2v2zm4 4h-2v-2h2v2zm0-4h-2v-2h2v2z"/>
                </svg>
              </span>
              内容
            </label>
            <textarea
              v-model="newFeedback.content"
              required
              rows="4"
              class="w-full p-3 border-2 border-[#c5d9e8] rounded-lg
                     focus:ring-2 focus:ring-[#3a7ca5] focus:border-[#5c9cc5]
                     bg-[#ffffff] shadow-sm transition-all duration-300
                     placeholder-[#8fafc1]"
              placeholder="请详述您的反馈内容"
            ></textarea>
          </div>

          <!-- 选项组 -->
          <div class="grid grid-cols-2 gap-6">
            <!-- 类型选择 -->
            <div class="relative">
              <label class="block text-lg mb-2 font-medium text-[#2c5f8d] flex items-center">
                <span class="icon-container w-8 h-8 bg-[#3a7ca5] rounded-full
                            flex items-center justify-center mr-2">
                  <svg class="w-5 h-5 text-[#f3f9ff]" viewBox="0 0 24 24">
                    <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"/>
                  </svg>
                </span>
                类型
              </label>
              <select
                v-model="newFeedback.type"
                class="w-full p-3 border-2 border-[#c5d9e8] rounded-lg
                       bg-[#ffffff] focus:ring-2 focus:ring-[#3a7ca5]
                       shadow-sm transition-all duration-300"
              >
                <option value="issue" class="text-[#2c5f8d]">问题反馈</option>
                <option value="suggestion" class="text-[#2c5f8d]">功能建议</option>
                <option value="other" class="text-[#2c5f8d]">其他</option>
              </select>
            </div>

            <!-- 评分 -->
            <div class="relative">
              <label class="block text-lg mb-2 font-medium text-[#2c5f8d] flex items-center">
                <span class="icon-container w-8 h-8 bg-[#3a7ca5] rounded-full
                            flex items-center justify-center mr-2">
                  <svg class="w-5 h-5 text-[#f3f9ff]" viewBox="0 0 24 24">
                    <path fill="currentColor" d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
                  </svg>
                </span>
                评分
              </label>
              <div class="relative">
                <select
                  v-model="newFeedback.rating"
                  class="w-full p-3 border-2 border-[#c5d9e8] rounded-lg
                         bg-[#ffffff] focus:ring-2 focus:ring-[#3a7ca5]
                         shadow-sm appearance-none"
                >
                  <option v-for="n in 5" :key="n" :value="n">{{ n }} 星</option>
                </select>
                <div class="absolute right-3 top-1/2 -translate-y-1/2 text-[#3a7ca5]">
                  ▼
                </div>
              </div>
            </div>
          </div>

          <!-- 提交按钮 -->
          <button
            @click="addFeedback"
            class="w-full py-4 px-6 rounded-xl bg-gradient-to-r from-[#3a7ca5] to-[#2c5f8d]
                   text-[#f3f9ff] font-semibold text-lg shadow-lg hover:shadow-xl
                   transition-all duration-300 hover:from-[#4d8db5] hover:to-[#3a7ca5]
                   flex items-center justify-center space-x-2 relative
                   overflow-hidden group"
          >
            <div class="absolute inset-0 bg-black opacity-0 group-hover:opacity-10
                        transition-opacity duration-300"></div>
            <svg class="w-6 h-6 animate-pulse" viewBox="0 0 24 24">
              <path fill="currentColor" d="M12 2L9 12l-7 3 7 3 3 10 3-10 7-3-7-3-3-10z"/>
            </svg>
            <span>敬呈云章</span>
          </button>
        </div>
      </div>

      <!-- 简牍式反馈列表 -->
      <div>
        <h2 class="text-4xl font-bold mb-6 text-center text-[#2c5f8d]
                   relative pb-4 scroll-title">
          <span class="bg-clip-text text-transparent bg-gradient-to-r from-[#2c5f8d] to-[#3a7ca5]">
            昔日启事
          </span>
          <div class="absolute bottom-0 left-1/2 -translate-x-1/2 w-32 h-1 bg-[#3a7ca5]
                      rounded-full opacity-50"></div>
        </h2>

        <div class="space-y-6">
          <!-- 反馈项 -->
          <div
            v-for="feedback in option.feedback"
            :key="feedback.id"
            class="bg-white p-6 rounded-lg shadow-md border-l-8 border-[#3a7ca5]
                   relative transition-all duration-300 hover:shadow-lg
                   before:absolute before:left-0 before:top-0 before:h-full before:w-1
                   before:bg-gradient-to-b before:from-[#c5d9e8] before:to-[#a8c4d9]"
          >
            <div class="flex justify-between items-start mb-4">
              <h3 class="text-xl font-semibold text-[#2c5f8d]">{{ feedback.title }}</h3>
              <span class="px-3 py-1 rounded-full text-sm font-medium
                          bg-[#e0e7f2] text-[#3a7ca5] shadow-inner">
                {{ typeLabels[feedback.type] }}
              </span>
            </div>

            <p class="text-[#2c5f8d]/90 mb-4 leading-relaxed
                      border-l-2 border-[#c5d9e8] pl-4">
              {{ feedback.content }}
            </p>
            <!-- 展示回复信息及管理员已查收标签 -->
            <div v-if="feedback.response != null" class="mb-2">
              <div class="flex items-center mb-1">
                <span class="text-sm font-semibold text-[#3a7ca5] mr-2">云章阁主</span>
                <span class="text-xs text-[#8fafc1] bg-[#e0f0ff] px-2 py-1 rounded-full mr-2">
                  管理员
                </span>
                <span class="px-3 py-1 rounded-full bg-green-100 text-green-800 text-xs font-semibold shadow">
                  管理员已查收
                </span>
              </div>
              <p class="text-[#2c5f8d] leading-relaxed">
                回复：{{ feedback.response }}
              </p>
            </div>

            <div class="flex justify-between items-center text-sm">
              <div class="flex items-center space-x-2 text-[#3a7ca5]">
                <span class="font-medium">品第：</span>
                <div class="flex text-[#d4af37]">
                  <span v-for="n in feedback.evaluate" :key="n">★</span>
                </div>
              </div>
              <div class="text-right">
                <div class="text-[#2c5f8d] font-medium">{{ feedback.username }}</div>
                <div class="text-[#3a7ca5] text-sm">
                  {{ parseDateTime(feedback.time) }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.font-chinese {
  font-family: 'Noto Serif SC', 'KaiTi', 'SimSun', serif;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.scroll-container {
  background-image: linear-gradient(to bottom,
  rgba(225, 235, 245, 0.2) 0%,
  rgba(255,255,255,0.8) 20%,
  rgba(255,255,255,0.8) 80%,
  rgba(225, 235, 245, 0.2) 100%);
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 8px;
  background: #e8f1f8;
}

::-webkit-scrollbar-thumb {
  background: #c5d9e8;
  border-radius: 4px;
  border: 2px solid #e8f1f8;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8c4d9;
}
</style>
