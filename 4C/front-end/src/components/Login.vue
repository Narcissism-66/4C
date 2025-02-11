<script setup>

import { reactive } from 'vue'
import { get } from '@/net/index.js'
import { message } from 'ant-design-vue';
import router from '@/router/index.js'
const [messageApi, contextHolder] = message.useMessage();

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
</script>


<template>
  <contextHolder/>
  <div class="flex justify-center items-center bg-gradient-to-r min-h-screen bg-gray-100">
    <div class="w-full max-w-md p-6 bg-white rounded-lg shadow-lg">
      <h2 class="text-2xl font-semibold text-center mb-6">登录</h2>

      <form @submit.prevent="handleLogin">
        <div class="mb-4">
          <label for="email" class="block text-sm font-medium text-gray-700">账号</label>
          <input
            type="text"
            id="text"
            v-model="options.account"
            class="w-full mt-2 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="请输入您的账号"
            required
          />
        </div>

        <div class="mb-6">
          <label for="password" class="block text-sm font-medium text-gray-700">密码</label>
          <input
            type="password"
            id="password"
            v-model="options.password"
            class="w-full mt-2 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="请输入您的密码"
            required
          />
        </div>

        <button
          @click="handleLogin"
          class="w-full py-2 px-4 bg-blue-500 text-white rounded-lg hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          登录
        </button>
        <p class="text-center text-sm text-gray-500 mt-4">
          还没有账户？ <a @click="router.push('/register')" class="cursor-pointer text-blue-500 hover:text-blue-600">注册</a>
        </p>
      </form>
    </div>
  </div>
</template>

<style scoped>
/* 背景渐变 */
.bg-gradient-to-r {
  background: linear-gradient(45deg, #4C6A92, #2CC6C8);
}
/* 可以根据需要自定义样式 */
</style>
