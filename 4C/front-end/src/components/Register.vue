<script setup>
import { reactive } from 'vue'
import { post } from '@/net/index.js'
import { message } from 'ant-design-vue';
import router from '@/router/index.js'

const [messageApi, contextHolder] = message.useMessage();

const options = reactive({
  email: '',
  password: '',
  account: '',
  username: '',
})

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
    messageApi.success(msg+"一秒后跳转到登陆界面！");
    setTimeout(function(){
      router.push('/auth');
    },1000)

  }, (msg) => {
    options.account=''
    messageApi.error(msg);
  });
}
</script>

<template>
  <contextHolder />
  <div
    class="flex justify-center items-center min-h-screen bg-gradient-to-r from-blue-500 to-teal-400">
    <div class="w-full max-w-lg p-8 bg-white rounded-xl shadow-lg">
      <h2 class="text-3xl font-semibold text-center mb-6 text-gray-800">注册账户</h2>

      <!-- 账号 -->
      <div class="mb-4">
        <label for="account" class="block text-sm font-medium text-gray-700">账号</label>
        <input
          type="text"
          id="account"
          v-model="options.account"
          class="w-full mt-2 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="请输入您的账号"
          required
        />
        <p v-if="validation.account" class="text-red-500 text-sm mt-1">{{ validation.account }}</p>
      </div>

      <!-- 用户名 -->
      <div class="mb-4">
        <label for="username" class="block text-sm font-medium text-gray-700">用户名</label>
        <input
          type="text"
          id="username"
          v-model="options.username"
          class="w-full mt-2 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="请输入您的用户名"
          required
        />
        <p v-if="validation.username" class="text-red-500 text-sm mt-1">{{ validation.username
          }}</p>
      </div>

      <!-- 邮箱 -->
      <div class="mb-4">
        <label for="email" class="block text-sm font-medium text-gray-700">邮箱</label>
        <input
          type="email"
          id="email"
          v-model="options.email"
          class="w-full mt-2 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="请输入您的邮箱"
          required
        />
        <p v-if="validation.email" class="text-red-500 text-sm mt-1">{{ validation.email }}</p>
      </div>

      <!-- 密码 -->
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
        <p v-if="validation.password" class="text-red-500 text-sm mt-1">{{ validation.password
          }}</p>
      </div>

      <!-- 注册按钮 -->
      <button
        @click="handleRegister"
        class="w-full py-2 px-4 bg-blue-500 text-white rounded-lg hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
      >
        注册
      </button>

      <!-- 登录提示 -->
      <p class="text-center text-sm text-gray-500 mt-4">
        已经有账户？ <a @click="router.push('/auth')" class="cursor-pointer text-blue-500 hover:text-blue-600">登录</a>
      </p>
    </div>
  </div>
</template>

<style scoped>
/* 背景渐变 */
.bg-gradient-to-r {
  background: linear-gradient(45deg, #4C6A92, #2CC6C8);
}

/* 按钮悬停效果 */
button:hover {
  background-color: #3490dc;
}

/* 可自定义的字体和颜色 */
.text-blue-500:hover {
  color: #5a67d8;
}
</style>
