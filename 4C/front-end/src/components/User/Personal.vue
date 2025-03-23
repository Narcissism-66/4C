<script setup>
import { onMounted, reactive, ref } from 'vue'
import { userUserStore } from '@/stores/UserStore.js'
import { get, post } from '@/net/index.js'
import { message } from 'ant-design-vue'
import axios from 'axios'
import router from '@/router/index.js'
import {  baseURL1 } from '@/main.js'

const [messageApi, contextHolder] = message.useMessage();
const userStore = userUserStore()

const activeSection = ref(null)
const isEditing = ref(false)
const options = reactive({
  username: userStore.user.username,
  email: userStore.user.email,
  avatar: userStore.user.avatar,
  report: [],
  logs: []
})

const toggleSection = (section) => {
  activeSection.value = activeSection.value === section ? null : section
}

const getAllReports = () => {
  get('api/report/getAllReport', {}, (message, data) => {
    options.report = data;
  })
}

const restoreReport = (id) => {
  if (confirm('确定要恢复这份报告吗？')) {
    post('api/report/updateReportShow', {
      id: id,
      isshow:'true',
    }, (message) => {
      messageApi.success(message);
      getAllReports();
    })
  }
}

const UploadUser = () => {
  post('api/user/updateUser', {
    avatar: options.avatar,
    username: options.username,
    email: options.email,
  }, (message) => {
    messageApi.success(message);
  })
}

const goReport = (id) => {
  router.push('/viewReport/' + id);
}

const onUploadImg = async (e) => {
  const file = e.target.files[0];
  if (!file) return;

  const form = new FormData();
  form.append('image', file);

  try {
    const response = await axios.post("api/user/uploadImg", form, {
      headers: {
        "Content-Type": "multipart/form-data",
        "Authorization": `${localStorage.getItem("authToken")}`,
      },
    });

    if (response.data.success) {
      options.avatar = baseURL1+response.data.data;
    }
  } catch (error) {
    console.error("上传失败：", error);
  }
};

const getHistoryLogs = () => {
  get('api/log/selectLogsByUserId', {}, (message, data) => {
    options.logs = data;
  })
}

onMounted(()=>{
  getAllReports();
  getHistoryLogs();
})
</script>

<template>
  <contextHolder />
  <div class="container mx-auto px-4 py-8 max-w-4xl">
    <!-- 个人资料卡片 -->
    <div class="bg-white shadow-xl rounded-2xl p-8 mb-8 transition-all duration-300 hover:shadow-2xl">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center">
        <div class="flex items-center mb-4 md:mb-0">
          <div class="relative group">
            <img
              :src="userStore.user.avatar || 'https://th.bing.com/th/id/R.37f9ac02be45c44ae7f94a73728d1baa?rik=kFogg7UByzuWew&pid=ImgRaw&r=0'"
              class="w-32 h-32 rounded-full object-cover border-4 border-blue-50 shadow-md transition-transform duration-300 group-hover:scale-105"
            />
          </div>
          <div class="ml-6">
            <h2 class="text-2xl font-bold text-gray-800">昵称：{{ userStore.user.username }}</h2>
            <p class="text-gray-500 text-sm mb-1">账号：{{ userStore.user.account }}</p>
            <p class="text-gray-500 text-sm">邮箱：{{ userStore.user.email }}</p>
          </div>
        </div>
        <button
          @click="isEditing = !isEditing"
          class="bg-gradient-to-r from-blue-500 to-blue-600 text-white px-6 py-3 rounded-xl shadow-md hover:shadow-lg transition-all duration-300 flex items-center"
        >
          <span class="icon-[heroicons--pencil-square-solid] w-5 h-5 mr-2"></span>
          <span>编辑资料</span>
        </button>
      </div>
    </div>

    <!-- 编辑资料 -->
    <transition name="slide-fade">
      <div v-if="isEditing" class="bg-white shadow-xl rounded-2xl p-8 mb-8">
        <h3 class="text-xl font-semibold text-gray-800 mb-6 border-l-4 border-blue-500 pl-3">编辑资料</h3>
        <div class="space-y-6">
          <div class="relative">
            <label class="block text-sm font-medium text-gray-700 mb-2">用户名</label>
            <input
              v-model="options.username"
              type="text"
              class="w-full px-4 py-3 rounded-lg border border-gray-200 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
            />
          </div>
          <div class="relative">
            <label class="block text-sm font-medium text-gray-700 mb-2">邮箱</label>
            <input
              v-model="options.email"
              type="email"
              class="w-full px-4 py-3 rounded-lg border border-gray-200 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
            />
          </div>
          <div class="relative">
            <label class="block text-sm font-medium text-gray-700 mb-2">头像上传</label>
            <label class="cursor-pointer flex flex-col items-center">
              <div class="w-24 h-24 rounded-full bg-gray-100 border-2 border-dashed border-gray-300 hover:border-blue-500 transition-colors duration-300 flex items-center justify-center overflow-hidden">
                <img v-if="options.avatar" :src="options.avatar" class="w-full h-full object-cover"/>
                <span v-else class="text-gray-400 icon-[heroicons--photo-solid] w-8 h-8"></span>
              </div>
              <input type="file" class="hidden" @change="onUploadImg" accept="image/*"/>
            </label>
          </div>
          <div class="flex justify-end space-x-4">
            <button @click="isEditing = false" class="px-6 py-2 text-gray-600 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors duration-300">
              取消
            </button>
            <button @click="UploadUser" class="px-6 py-2 bg-blue-500 text-white rounded-lg shadow-md hover:bg-blue-600 transition-all duration-300">
              保存更改
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 功能卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
      <!-- 报告管理 -->
      <div
        @click="toggleSection('report')"
        class="bg-white p-6 rounded-2xl shadow-md cursor-pointer transition-all duration-300 hover:shadow-lg hover:-translate-y-1"
      >
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center mr-4">
              <span class="text-blue-600 icon-[heroicons--document-text-solid] w-6 h-6"></span>
            </div>
            <div>
              <h4 class="text-lg font-semibold text-gray-800">报告管理</h4>
              <p class="text-sm text-gray-400 mt-1">共 {{options.report.length}} 份分析报告</p>
            </div>
          </div>
          <span :class="[
            'icon transition-transform duration-300',
            activeSection === 'report' ? 'rotate-180' : ''
          ]">
            <span class="icon-[heroicons--chevron-down-solid] w-5 h-5 text-gray-400"></span>
          </span>
        </div>
      </div>

      <!-- 操作日志 -->
      <div
        @click="toggleSection('log')"
        class="bg-white p-6 rounded-2xl shadow-md cursor-pointer transition-all duration-300 hover:shadow-lg hover:-translate-y-1"
      >
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center mr-4">
              <span class="text-blue-600 icon-[heroicons--clock-solid] w-6 h-6"></span>
            </div>
            <div>
              <h4 class="text-lg font-semibold text-gray-800">操作日志</h4>
              <p class="text-sm text-gray-400 mt-1">最近 {{options.logs.length}} 条操作记录</p>
            </div>
          </div>
          <span :class="[
            'icon transition-transform duration-300',
            activeSection === 'log' ? 'rotate-180' : ''
          ]">
            <span class="icon-[heroicons--chevron-down-solid] w-5 h-5 text-gray-400"></span>
          </span>
        </div>
      </div>
    </div>

    <!-- 报告展示卡片 -->
    <transition name="expand">
      <div v-if="activeSection === 'report'" class="bg-white rounded-2xl shadow-md mb-6">
        <div class="p-6">
          <h4 class="text-lg font-semibold text-gray-800 mb-4 flex items-center">
            <span class="icon-[heroicons--document-chart-bar-solid] w-5 h-5 mr-2 text-blue-500"></span>
            分析报告列表
          </h4>
          <div class="max-h-96 overflow-y-auto pr-4">
            <template v-if="options.report.length > 0">
              <ul class="space-y-3">
                <li
                  v-for="report in options.report"
                  :key="report.id"
                  class="p-4 bg-gray-50 rounded-xl hover:bg-blue-50 transition-colors duration-200"
                >
                  <div class="flex items-center justify-between">
                    <div class="flex-1 min-w-0">
                      <h5 class="font-medium text-gray-800 truncate">{{ report.filename }}</h5>
                      <div class="flex items-center space-x-2 mt-1">
                        <span class="text-xs text-gray-500">
                          {{ new Date(report.time).toLocaleDateString() }}
                        </span>
                        <span
                          v-if="report.isshow === 'false'"
                          class="px-2 py-0.5 text-xs bg-red-100 text-red-600 rounded-full"
                        >
                          已删除
                        </span>
                      </div>
                    </div>
                    <div class="flex space-x-2 ml-4">
                      <button
                        v-if="report.isshow === 'false'"
                        @click.stop="restoreReport(report.id)"
                        class="px-3 py-1 text-sm bg-amber-100 text-amber-600 rounded-lg hover:bg-amber-200 transition-colors"
                      >
                        恢复
                      </button>
                      <button
                        v-if="report.isshow === 'true'"
                        @click.stop="goReport(report.id)"
                        class="px-3 py-1 text-sm bg-blue-100 text-blue-600 rounded-lg hover:bg-blue-200 transition-colors flex items-center"
                      >
                        <span class="icon-[heroicons--eye-solid] w-4 h-4 mr-1"></span>
                        查看
                      </button>
                    </div>
                  </div>
                </li>
              </ul>
            </template>
            <div v-else class="text-center py-8">
              <span class="icon-[heroicons--document-magnifying-glass-solid] w-12 h-12 text-gray-300 mx-auto"></span>
              <p class="mt-4 text-gray-400">暂无分析报告</p>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- 日志展示卡片 -->
    <transition name="expand">
      <div v-if="activeSection === 'log'" class="bg-white rounded-2xl shadow-md mb-6">
        <div class="p-6">
          <h4 class="text-lg font-semibold text-gray-800 mb-4 flex items-center">
            <span class="icon-[heroicons--clipboard-document-list-solid] w-5 h-5 mr-2 text-blue-500"></span>
            操作记录列表
          </h4>
          <div class="max-h-96 overflow-y-auto pr-4">
            <template v-if="options.logs.length > 0">
              <div class="flow-root">
                <ul role="list" class="-mb-8">
                  <li v-for="(log, index) in options.logs" :key="log.id">
                    <div class="relative pb-8">
                      <span
                        v-if="index !== options.logs.length - 1"
                        class="absolute left-5 top-5 -ml-px h-full w-0.5 bg-gray-200"
                      ></span>
                      <div class="relative flex items-start space-x-4">
                        <div class="relative">
                          <span class="bg-blue-500 text-white h-10 w-10 rounded-full flex items-center justify-center">
                            <span class="icon-[heroicons--command-line-solid] w-5 h-5"></span>
                          </span>
                        </div>
                        <div class="min-w-0 flex-1">
                          <div>
                            <div class="text-sm">
                              <span class="font-medium text-gray-900">{{ log.type }}</span>
                              <span class="ml-2 text-gray-500 text-xs">
                                {{ new Date(log.time).toLocaleString() }}
                              </span>
                            </div>
                            <p class="mt-1 text-sm text-gray-500">
                              {{ log.content }}
                            </p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </li>
                </ul>
              </div>
            </template>
            <div v-else class="text-center py-8">
              <span class="icon-[heroicons--inbox-stack-solid] w-12 h-12 text-gray-300 mx-auto"></span>
              <p class="mt-4 text-gray-400">暂无操作记录</p>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<style>
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
  margin-bottom: 0;
}

.expand-enter-to,
.expand-leave-from {
  max-height: 600px;
  opacity: 1;
  margin-bottom: 1.5rem;
}

/* 自定义滚动条 */
.max-h-96::-webkit-scrollbar {
  width: 6px;
}

.max-h-96::-webkit-scrollbar-track {
  background: #f8fafc;
  border-radius: 4px;
}

.max-h-96::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

.max-h-96::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
