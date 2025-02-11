<script setup>

import { userUserStore } from '@/stores/UserStore.js'
import router from '@/router/index.js'

const UserStore=userUserStore();


const notifications = [
  { id: 1, message: "数据上传已完成！" },
  { id: 2, message: "您的分析报告已生成。" }
];

const uploadedDataCount = 120;
const recentAnalysisData = "数据集A";
const recentReport = "报告1";

// 模拟跳转到不同页面
const goToUploadData = () => {
  // 跳转到上传数据页面
  console.log("跳转到上传数据页面");
};
const goToVisualization = () => {
  // 跳转到数据可视化页面
  console.log("跳转到数据可视化页面");
};
const goToAnalysis = () => {
  // 跳转到数据分析页面
  console.log("跳转到数据分析页面");
};
const goToReports = () => {
  // 跳转到查看报告页面
  console.log("跳转到查看报告页面");
};
const handleLogout = () => {
  // 退出登录
  console.log("退出登录");
};
</script>

<template>
  <div class="dashboard-container">
    <!-- 头部导航栏 -->
    <header class="header">
      <div class="logo">智能数据可视化平台</div>
      <div class="user-info">
        <img  v-if="UserStore.user.avatar!==null" class="avatar" :src="UserStore.user.avatar" alt="User Avatar" />
        <img  v-else class="avatar" src="https://www.keaitupian.cn/cjpic/frombd/1/253/2565880906/2862557700.jpg" alt="User Avatar" />
        <span>{{ UserStore.user.username }}</span>
        <button @click="handleLogout">退出</button>
      </div>
    </header>

    <!-- 欢迎信息 -->
    <section class="welcome">
      <h2>欢迎使用智能数据可视化与分析平台</h2>
      <p>探索数据，发现洞察，做出明智的决策。</p>
    </section>

    <!-- 快捷入口卡片 -->
    <section class="quick-access">
      <div class="card" @click="router.push('/upload')">
        <i class="icon-upload"></i>
        <p>上传数据</p>
      </div>
      <div class="card" @click="router.push('/echarts')">
        <i class="icon-visualization"></i>
        <p>数据可视化</p>
      </div>
      <div class="card" @click="goToAnalysis">
        <i class="icon-analysis"></i>
        <p>智能分析</p>
      </div>
      <div class="card" @click="goToReports">
        <i class="icon-reports"></i>
        <p>查看报告</p>
      </div>
    </section>

    <!-- 数据概览 -->
    <section class="overview">
      <div class="stat">
        <h3>已上传数据</h3>
        <p>{{ uploadedDataCount }} 条</p>
      </div>
      <div class="stat">
        <h3>最近分析数据集</h3>
        <p>{{ recentAnalysisData }}</p>
      </div>
      <div class="stat">
        <h3>最近生成报告</h3>
        <p>{{ recentReport }}</p>
      </div>
    </section>

    <!-- 通知区域 -->
    <section class="notifications">
      <h3>系统通知</h3>
      <ul>
        <li v-for="notification in notifications" :key="notification.id">{{ notification.message }}</li>
      </ul>
    </section>

    <!-- 底部 -->
    <footer class="footer">
      <p>&copy; 2025 智能数据可视化与分析平台 | <a href="#">隐私政策</a> | <a href="#">使用条款</a></p>
    </footer>
  </div>
</template>

<style scoped>
.dashboard-container {
  padding: 20px;
  font-family: 'Arial', sans-serif;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f0f4f8;
  padding: 10px;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: #007bff;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 10px;
}

.user-info button {
  background-color: #ff6f61;
  color: white;
  padding: 5px 10px;
  border-radius: 5px;
  border: none;
  cursor: pointer;
}

.welcome {
  text-align: center;
  margin-top: 20px;
}

.quick-access {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-top: 30px;
}

.card {
  background-color: #fff;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
  cursor: pointer;
}

.card:hover {
  background-color: #f1f1f1;
}

.card i {
  font-size: 30px;
  margin-bottom: 10px;
  color: #007bff;
}

.overview {
  display: flex;
  gap: 20px;
  margin-top: 30px;
}

.stat {
  background-color: #fff;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  width: 200px;
  text-align: center;
}

.notifications {
  margin-top: 30px;
}

.notifications h3 {
  font-size: 20px;
  font-weight: 600;
}

.notifications ul {
  list-style: none;
  padding: 0;
}

.notifications li {
  background-color: #f9f9f9;
  margin: 5px 0;
  padding: 10px;
  border-radius: 5px;
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.05);
}

.footer {
  text-align: center;
  margin-top: 40px;
  font-size: 14px;
  color: #666;
}
</style>
