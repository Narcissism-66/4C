<template>
  <div class="space-y-6">
    <!-- 用户管理标题 -->
    <div class="text-2xl font-bold text-blue-900">用户管理</div>

    <!-- 搜索框 -->
    <div class="flex items-center space-x-4">
      <input
        type="text"
        v-model="searchQuery"
        placeholder="搜索用户"
        class="px-4 py-2 rounded-lg border border-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
      <button @click="searchUser" class="px-6 py-2 rounded-lg bg-blue-600 text-white">搜索</button>
    </div>

    <!-- 用户列表 -->
    <div class="overflow-x-auto shadow-md rounded-lg bg-white">
      <table class="min-w-full table-auto">
        <thead class="bg-blue-100">
        <tr>
          <th class="px-4 py-2 text-left">用户名</th>
          <th class="px-4 py-2 text-left">邮箱</th>
          <th class="px-4 py-2 text-left">身份</th>
          <th class="px-4 py-2 text-left">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="user in filteredUsers" :key="user.id">
          <td @click="GoPersonal(user.id)" class="px-4 py-2 cursor-pointer hover:underline">
            {{ user.username }}
          </td>
          <td class="px-4 py-2">{{ user.email }}</td>
          <td class="px-4 py-2">
            <span>{{ user.role }}</span>
            <button
              @click="confirmToggleRole(user)"
              class="ml-2 text-blue-500 hover:underline"
            >
              {{ user.role === '管理员' ? '转为用户' : '转为管理员' }}
            </button>
          </td>
          <td class="px-4 py-2">
            <button
              @click="confirmDeleteUser(user.id)"
              class="text-red-500 hover:text-red-700"
            >
              删除
            </button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- 删除确认弹框 -->
    <div v-if="isDeleteModalOpen" class="fixed inset-0 bg-gray-800 bg-opacity-50 flex justify-center items-center">
      <div class="bg-white p-6 rounded-lg">
        <p>确定要删除该用户吗？</p>
        <div class="mt-4 flex justify-end space-x-4">
          <button @click="cancelDeleteUser" class="px-4 py-2 bg-gray-300 rounded">取消</button>
          <button @click="performDeleteUser" class="px-4 py-2 bg-red-500 text-white rounded">确定</button>
        </div>
      </div>
    </div>

    <!-- 修改身份确认弹框 -->
    <div v-if="isToggleModalOpen" class="fixed inset-0 bg-gray-800 bg-opacity-50 flex justify-center items-center">
      <div class="bg-white p-6 rounded-lg">
        <p>
          确定将用户身份由
          <span class="font-bold">{{ toggleUser.role }}</span>
          改为
          <span class="font-bold">{{ pendingRole }}</span>
          吗？
        </p>
        <div class="mt-4 flex justify-end space-x-4">
          <button @click="cancelToggle" class="px-4 py-2 bg-gray-300 rounded">取消</button>
          <button @click="performToggleRole" class="px-4 py-2 bg-blue-500 text-white rounded">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { get, post } from '@/net/index.js'
import router from '@/router/index.js'

const options = reactive({
  users: []
})

// 跳转到用户详情页面
const GoPersonal = (id) => {
  router.push('/personal/' + id)
}

// 获取所有用户数据
const getAllUser = () => {
  get("api/user/getAllUsersInfo", {}, (msg, data) => {
    options.users = data
  })
}

const searchQuery = ref("")

// 过滤用户数据
const filteredUsers = computed(() => {
  return options.users.filter(user =>
    user.username.includes(searchQuery.value) || user.email.includes(searchQuery.value)
  )
})

// 删除用户相关逻辑
const isDeleteModalOpen = ref(false)
const deleteUserId = ref(null)
const confirmDeleteUser = (userId) => {
  deleteUserId.value = userId
  isDeleteModalOpen.value = true
}
const performDeleteUser = () => {
  post("api/user/deleteUser", { id: deleteUserId.value }, (message) => {
    console.log(message)
    getAllUser()
    isDeleteModalOpen.value = false
    deleteUserId.value = null
  })
}
const cancelDeleteUser = () => {
  isDeleteModalOpen.value = false
  deleteUserId.value = null
}

// 搜索用户接口
const searchUser = () => {
  get("api/user/searchUsersByName", { username: searchQuery.value }, (msg, data) => {
    console.log(data)
    options.users = data
  })
}

// 修改身份相关逻辑：只能在“管理员”和“用户”间转换
const isToggleModalOpen = ref(false)
const toggleUser = ref(null)
const pendingRole = ref("")
const confirmToggleRole = (user) => {
  pendingRole.value = user.role === '管理员' ? '用户' : '管理员'
  toggleUser.value = user
  isToggleModalOpen.value = true
}
const performToggleRole = () => {
  post("api/user/updateUserRole", { id: toggleUser.value.id, role: pendingRole.value }, (msg) => {
    console.log("身份更新:", msg)
    toggleUser.value.role = pendingRole.value
    isToggleModalOpen.value = false
    toggleUser.value = null
  })
}
const cancelToggle = () => {
  isToggleModalOpen.value = false
  toggleUser.value = null
}

onMounted(() => {
  getAllUser()
})
</script>

<style scoped>
/* 自定义样式，可根据需要调整 */
</style>
