<script setup>
import { useRouter } from 'vue-router'
import { userUserStore } from '@/stores/UserStore.js'
import { ref } from 'vue'
import { post } from '@/net/index.js'

const userStore = userUserStore()
const router = useRouter()

const handleLogout = async () => {
  await logoutLog();
  userStore.logout()
  await router.push('/introduction')
}

const logoutLog=()=>{
  post("api/log/insertLog",{
    type:"退出登陆",
    content:"Id: "+userStore.user.id+" 的用户退出了登陆",
  },(msg) => {
    console.log(msg)
  })
}
const GoPersonal=(id)=>{
  router.push('/personal/'+id);
}
</script>

<template>
  <div class="header">
    <div class="logo" @click="router.push('/')">
      <i class="fas fa-chart-line"></i>
      文渊数鉴
    </div>
    <nav class="main-nav">
      <div @click="router.push('/')">天工开物·文渊数鉴</div>
<!--      <div @click="router.push('/upload')">上传</div>-->
      <div class="visualization-nav" @click="router.push('/single')">
        单卷精读
      </div>
<!--      <div class="visualization-nav" @click="router.push('/merge')">-->
<!--        多源聚合-->
<!--      </div>-->
      <div @click="router.push('/customize')">万象合璧</div>
      <div @click="router.push('/AiChat')">AI畅谈</div>
      <div @click="router.push('/report')">研析阁</div>
      <div @click="router.push('/feedback')">反馈</div>
    </nav>
    <div class="user-info">
      <img @click="userStore.user.role==='管理员'?router.push('/admin'):GoPersonal(userStore.user.id)" v-if="userStore.user.avatar" :src="userStore.user.avatar" class="avatar"
           alt="用户头像" />
      <img v-else
           @click="userStore.user.role==='管理员'?router.push('/admin'):GoPersonal(userStore.user.id)"
           src="https://th.bing.com/th/id/R.37f9ac02be45c44ae7f94a73728d1baa?rik=kFogg7UByzuWew&pid=ImgRaw&r=0"
           class="avatar" alt="用户头像" />
      <span class="text-xl font-kai">{{ userStore.user.username }}</span>
      <button @click="handleLogout" class="font-kai">
        退出
      </button>
    </div>
  </div>

  <main >
    <RouterView />
  </main>
</template>

<style scoped>
/* Header */
.header {
  display: flex;
  align-items: center;
  padding: 0 2rem;
  height: 72px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 1000;
  backdrop-filter: blur(8px);
}

.logo {
  font-size: 1.6rem;
  font-weight: 700;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 0.8rem;
  font-family: 'Segoe UI', system-ui, sans-serif;
  letter-spacing: 0.5px;
}

.logo i {
  color: #4a90e2;
  font-size: 2rem;
  text-shadow: 0 2px 4px rgba(74, 144, 226, 0.2);
}

.main-nav {
  display: flex;
  margin-left: 3rem;
  gap: 2.5rem;
}

.main-nav div {
  color: #6c757d;
  font-weight: 500;
  padding: 1rem 0;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  border-bottom: 3px solid transparent;
  cursor: pointer;
  position: relative;
}

.main-nav div:hover {
  color: #4a90e2;
  transform: translateY(-2px);
}

.main-nav div::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 3px;
  background: #4a90e2;
  transition: all 0.3s ease;
}

.main-nav div:hover::after {
  width: 100%;
  left: 0;
}

.visualization-nav {
  position: relative;
}


.visualization-nav:hover .dropdown {
  opacity: 1;
  transform: translateY(0);
}

.dropdown div {
  padding: 0.8rem 1.5rem;
  color: #495057;
  font-size: 0.95rem;
  transition: all 0.2s;
}

.dropdown div:hover {
  background: #f8f9fa;
  color: #4a90e2;
  padding-left: 1.8rem;
}

.user-info {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 2px solid #e9ecef;
  transition: transform 0.3s ease;
}

.avatar:hover {
  transform: scale(1.05);
}

.user-info span {
  font-size: 1.1rem;
  color: #495057;
  font-weight: 500;
}

.user-info button {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
  color: white;
  padding: 0.6rem 1.5rem;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
  gap: 0.8rem;
  box-shadow: 0 4px 12px rgba(255, 82, 82, 0.25);
}

.user-info button:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(255, 82, 82, 0.35);
}

/* Main Content */
.main-content {
  padding: 2rem;
  //max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}


.card i {
  font-size: 2.5rem;
  color: #3498db;
  margin-bottom: 1.5rem;
}

.card p {
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  font-size: 1.1rem;
}


.stat h3 {
  color: #7f8c8d;
  font-size: 1rem;
  margin-bottom: 0.5rem;
}

.stat p {
  font-size: 1.8rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}


.notifications h3 {
  color: #2c3e50;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.8rem;
  font-size: 1.2rem;
}

.notifications ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.notifications li {
  padding: 1rem;
  margin: 0.5rem 0;
  background: #f8f9fa;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.notifications li::before {
  content: "•";
  color: #3498db;
  font-size: 1.4rem;
}
</style>
