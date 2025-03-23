<script setup>
import { onMounted, ref } from 'vue'
import { marked } from 'marked';
import { get, post } from '@/net/index.js'

const userInput = ref('');
const messages = ref([]);
const isLoading = ref(false);

const askAI = async () => {
  if (!userInput.value.trim()) return;

  messages.value.push({ text: userInput.value, isUser: true });
  const question = userInput.value;
  userInput.value = '';

  isLoading.value = true;

  await post('ai/chat', {
    message: question
  }, (data) => {
    messages.value.push({ text: data, isUser: false });
    updateAI();
    post("api/log/insertLog",{
      type:"AI问答",
      content:`使用了AI问答，提问的是 ${question} `
    },(message)=>{
      console.log(message)
    })
    isLoading.value = false;
  });
};

const updateAI=()=>{
  post("api/function/updateAI",{},(message)=>{
    console.log(message)
  })
}
onMounted(() => {
  messages.value.push({
    text: "**文渊数鉴** 吾承河图洛书之慧，秉青囊云笈之智，愿为君解惑析疑\n（应对依先贤典籍，当慎思明辨）",
    isUser: false
  });
})
</script>

<template>
  <div class="min-h-screen flex flex-col justify-between bg-gradient-to-br from-white to-blue-50 p-6 relative overflow-hidden">

    <!-- 青花云纹背景 -->
    <div class="absolute inset-0 pointer-events-none opacity-10 bg-repeat"
         style="background-image: url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cGF0aCBkPSJNMjAgNDBjLTEwIDAtMTAtMTUgMC0yMCA1IDAgMTUgNSAxNSAxNXMtMTAgMjAtMTUgMjB6TTgwIDYwYzEwIDAgMTAgMTUgMCAyMC01IDAtMTUtNS0xNS0xNXMxMC0yMCAxNS0yMHoiIGZpbGw9IiM2Y2I4ZWMiLz4KPC9zdmc+')">
    </div>

    <!-- 聊天窗口 -->
    <div class="flex-1 overflow-auto p-4 space-y-4 bg-white/95 rounded-xl shadow-lg
               border border-blue-100 relative z-10
               bg-[linear-gradient(to_bottom_right,#ffffff_0%,#f8fbff_100%)]">

      <!-- 历史消息 -->
      <div v-for="(message, index) in messages" :key="index"
           class="flex items-start transition-all duration-300"
           :class="message.isUser ? 'flex-row-reverse' : 'flex-row'">

        <!-- 用户头像（白玉佩） -->
        <div v-if="message.isUser" class="flex-shrink-0 ml-3">
          <div class="w-12 h-12 rounded-xl bg-white flex items-center justify-center
                      shadow-lg border-2 border-blue-200 rotate-3 transform">
            <svg class="w-8 h-8 text-blue-400" viewBox="0 0 24 24">
              <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"/>
              <path fill="currentColor" d="M12 8c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm0 6c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z"/>
            </svg>
          </div>
        </div>

        <!-- AI头像（青花瓷） -->
        <div v-else class="flex-shrink-0 mr-3">
          <div class="w-12 h-12 bg-white rounded-xl flex items-center justify-center
                      shadow-lg border-2 border-blue-200 -rotate-3 transform">
            <svg class="w-8 h-8 text-blue-500" viewBox="0 0 24 24">
              <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"/>
              <path fill="currentColor" d="M16.24 7.76c-1.07-1.07-2.82-1.07-3.89 0L12 8.11l-.35-.35c-1.07-1.07-2.82-1.07-3.89 0s-1.07 2.82 0 3.89l.35.35-.35.35c-1.07 1.07-1.07 2.82 0 3.89s2.82 1.07 3.89 0l.35-.35.35.35c1.07 1.07 2.82 1.07 3.89 0s1.07-2.82 0-3.89l-.35-.35.35-.35c1.07-1.07 1.07-2.82 0-3.89zM12 13.41l-.35.35c-.39.39-1.02.39-1.41 0s-.39-1.02 0-1.41L10.59 12l-.35-.35c-.39-.39-.39-1.02 0-1.41s1.02-.39 1.41 0L12 10.59l.35-.35c.39-.39 1.02-.39 1.41 0s.39 1.02 0 1.41L13.41 12l.35.35c.39.39.39 1.02 0 1.41s-1.02.39-1.41 0L12 13.41z"/>
            </svg>
          </div>
        </div>

        <!-- 消息内容（冰裂纹瓷纹） -->
        <div class="max-w-xl transform origin-center">
          <div class="relative p-4 rounded-lg shadow-sm transition-all duration-300
                      border border-blue-100 before:absolute before:w-2 before:h-2 before:bg-inherit
                      bg-white text-blue-900 font-serif text-lg leading-relaxed
                      hover:shadow-md"
               :class="message.isUser
                 ? 'ml-10 before:-left-1 before:bg-blue-200'
                 : 'mr-10 before:-right-1 before:bg-blue-200'">

            <div v-if="!message.isUser" class="absolute -top-2 -right-2">
              <div class="w-4 h-4 bg-blue-100 rounded-full animate-pulse
                          border border-blue-200"></div>
            </div>

            <div v-html="marked(message.text)"
                 class="markdown-content prose max-w-none
                        prose-headings:text-blue-800 prose-strong:text-blue-500
                        prose-a:text-blue-600 prose-blockquote:border-l-blue-300
                        prose-blockquote:text-blue-700"></div>
          </div>
        </div>
      </div>

      <div v-if="isLoading" class="flex items-start">
        <div class="flex-shrink-0 mr-3">
          <div class="w-12 h-12 bg-white rounded-xl flex items-center justify-center
                shadow-lg border-2 border-blue-200 relative overflow-hidden">
            <!-- 水墨涟漪动画 -->
            <div class="absolute inset-0 flex items-center justify-center">
              <div class="ink-ripple animate-ink-ripple"></div>
              <div class="ink-ripple animate-ink-ripple delay-500"></div>
            </div>
            <!-- 毛笔字"思" -->
            <div class="relative text-2xl font-regular text-blue-500
                  animate-brush-stroke">思</div>
          </div>
        </div>
        <div class="max-w-xl">
          <div class="relative p-4 rounded-lg bg-white border border-blue-100
                shadow-sm text-blue-800 font-serif">
            <div class="flex items-center space-x-2">
              <!-- 流动云纹 -->
              <div class="cloud-flow animate-cloud-flow"></div>
              <span>正在挥毫...</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域（宣纸卷轴） -->
    <div class="mt-6 flex items-center space-x-4 relative z-10">
      <div class="flex-1 relative">
        <div class="absolute -inset-y-1 -inset-x-2 bg-blue-100/50 rounded-xl
                    transform -rotate-1 shadow-lg"></div>
        <input
          v-model="userInput"
          type="text"
          placeholder="🖋 敬问君之所思"
          class="w-full p-4 rounded-xl shadow-sm relative bg-white border-2 border-blue-200
                 focus:outline-none focus:ring-2 focus:ring-blue-300 transition-all
                 placeholder-blue-400/80 text-blue-800 font-serif text-lg
                 hover:bg-blue-50 focus:bg-white"
          @keydown.enter="askAI"
        />
        <button
          @click="askAI"
          class="absolute right-2 top-1/2 -translate-y-1/2 p-2 bg-blue-400/90
                 text-white rounded-lg hover:bg-blue-500 focus:outline-none
                 focus:ring-2 focus:ring-blue-200 transition-all shadow-md
                 border border-blue-500/30 flex items-center justify-center
                 hover:shadow-blue-200/50"
        >
          <svg class="w-6 h-6 animate-float" viewBox="0 0 24 24">
            <path fill="currentColor" d="M3 13h2v-2H3v2zm4 8h2v-2H7v2zm6-18h-2v2h2V3zm6 0v2h2c0-1.1-.9-2-2-2zM5 21v-2H3c0 1.1.9 2 2 2zm-2-4h2v-2H3v2zm8 4h2v-2h-2v2zm8-8h2v-2h-2v2zm0-4h2V7h-2v2zm-4-4h2V3h-2v2zM7 5h2V3H7v2zm4-2h2v2h-2V3zm4 6h2V7h-2v2zm-8 6h2v-2H7v2zm-4 0h2v-2H3v2z"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 水墨涟漪动画 */
@keyframes ink-ripple {
  0% {
    opacity: 1;
    transform: scale(0);
    box-shadow: 0 0 0 0 rgba(66, 153, 225, 0.4);
  }
  100% {
    opacity: 0;
    transform: scale(2);
    box-shadow: 0 0 0 12px rgba(66, 153, 225, 0);
  }
}

.ink-ripple {
  @apply absolute w-8 h-8 rounded-full bg-blue-300/30;
}

.animate-ink-ripple {
  animation: ink-ripple 1.5s ease-out infinite;
}

.delay-500 {
  animation-delay: 500ms;
}

/* 毛笔字动画 */
@keyframes brush-stroke {
  0% {
    clip-path: polygon(0 0, 0 0, 0 100%, 0 100%);
  }
  100% {
    clip-path: polygon(0 0, 100% 0, 100% 100%, 0 100%);
  }
}

.animate-brush-stroke {
  animation: brush-stroke 1.2s ease-in-out infinite alternate;
}

/* 流动云纹 */
.cloud-flow {
  @apply w-16 h-4 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjQiIGhlaWdodD0iMTYiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTQgOEMxLjggOCAwIDkuOCAwIDEyYzAgMi4yIDEuOCA0IDQgNGg1NmMyLjIgMCA0LTEuOCA0LTRzLTEuOC00LTQtNEg0eiIgZmlsbD0iI2IzZDJmMSIvPjwvc3ZnPg==')];
  background-size: auto 100%;
}

@keyframes cloud-flow {
  from { background-position-x: 0; }
  to { background-position-x: 64px; }
}

.animate-cloud-flow {
  animation: cloud-flow 1.6s linear infinite;
}
@keyframes cloud {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-5px); }
  100% { transform: translateY(0px); }
}

@keyframes cloud-dot {
  0% { transform: translateY(0); }
  50% { transform: translateY(3px); }
  100% { transform: translateY(0); }
}

.animate-cloud {
  animation: cloud 3s ease-in-out infinite;
}

.animate-cloud-dot {
  animation: cloud-dot 2.5s ease-in-out infinite;
}

.delay-150 {
  animation-delay: 150ms;
}

.markdown-content :deep(blockquote) {
  @apply pl-4 ml-2 border-l-4 border-blue-200 bg-blue-50/50 py-2 rounded-r;
}

.markdown-content :deep(code) {
  @apply bg-blue-100 text-blue-600 px-2 py-1 rounded;
}

.markdown-content :deep(pre) {
  @apply bg-blue-50 border border-blue-100 p-4 rounded-lg overflow-x-auto;
}

.markdown-content :deep(table) {
  @apply border-collapse w-full my-4;
}

.markdown-content :deep(th) {
  @apply bg-blue-100 text-blue-800 px-4 py-2 border border-blue-200;
}

.markdown-content :deep(td) {
  @apply px-4 py-2 border border-blue-100;
}

.animate-float {
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}
</style>
