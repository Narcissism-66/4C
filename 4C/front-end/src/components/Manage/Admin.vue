<template>
  <div class="flex h-full bg-gradient-to-r from-blue-100 to-blue-300">
    <!-- 侧边栏 -->
    <aside class="w-72 bg-gradient-to-b from-blue-800 to-blue-900 text-white p-6 fixed h-full shadow-lg rounded-tr-2xl rounded-br-2xl">
      <div class="text-2xl font-bold mb-8 flex items-center">
        <span>管理后台</span>
      </div>
      <nav class="space-y-4">
        <button @click="currentView = 'Dashboard'" class="menu-item">📊 数据统计</button>
        <button @click="currentView = 'Users'" class="menu-item">👤 用户管理</button>
        <button @click="currentView = 'Feedback'" class="menu-item">✉️ 反馈信息</button>
        <button @click="currentView = 'Logs'" class="menu-item">🔍 操作日志</button>
      </nav>
    </aside>

    <!-- 主内容 -->
    <div class="flex-1 ml-72 p-6 min-h-screen">
      <!-- 顶部导航 -->
      <header class="bg-white/60 backdrop-blur-md shadow-md px-6 py-4 rounded-lg flex justify-between items-center sticky top-4">
        <div class="text-xl font-semibold">管理员面板</div>
        <div class="flex items-center space-x-4">
          <button class="relative hover:scale-110 transition-transform">🔔</button>
          <div class="w-10 h-10 bg-gray-300 rounded-full border-2 border-gray-500"></div>
        </div>
      </header>

      <!-- 内容区域 -->
      <div class="mt-6">
        <Dashboard v-if="currentView === 'Dashboard'" />
        <Feedback v-if="currentView === 'Feedback'" />
        <Logs v-if="currentView === 'Logs'" />
        <Users v-if="currentView === 'Users'" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import Dashboard from "../Manage/Dashboard.vue";  // 引入仪表盘
import Feedback from "../Manage/Feedback.vue";    // 引入反馈信息组件
import Logs from "../Manage/Logs.vue";            // 引入操作日志组件
import Users from "../Manage/Users.vue";          // 引入用户管理组件

const currentView = ref("Dashboard");
</script>

<style scoped>
.menu-item {
  display: block;
  padding: 12px 16px;
  transition: all 0.3s;
  width: 100%;
  text-align: left;
  border-radius: 8px;
  background: linear-gradient(45deg, rgba(255,255,255,0.1), rgba(255,255,255,0));
}
.menu-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateX(5px);
}
html, body, #app {
  height: 100%;
  margin: 0;
}
</style>
