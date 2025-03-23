<template>
  <div class="p-6 bg-white shadow-md rounded-lg">
    <h2 class="text-2xl font-bold mb-4">操作日志(近七天)</h2>

    <!-- 分类下拉框 -->
    <div class="mb-4 flex items-center space-x-2">
      <label for="category" class="font-medium">分类：</label>
      <select id="category" v-model="selectedCategory" class="border p-2 rounded">
        <option value="全部">全部</option>
        <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
      </select>
    </div>

    <!-- 可滚动的表格容器 -->
    <div class="overflow-y-auto scrollbar-hide" style="max-height: 500px;">
      <table class="min-w-full border-collapse">
        <thead>
        <tr class="bg-gray-200">
          <th class="px-4 py-2 text-left">时间</th>
          <th class="px-4 py-2 text-left">操作用户</th>
          <th class="px-4 py-2 text-left">操作类型</th>
          <th class="px-4 py-2 text-left">内容</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="log in filteredLogs" :key="log.id" class="hover:bg-gray-100">
          <td class="px-4 py-2 border-b">{{ formatTime(log.time) }}</td>
          <td @click="GoPersonal(log.userId)" class="px-4 py-2 border-b hover:text-blue-600 hover:underline">{{ log.userId===userStore.user.id? "本用户":log.userId }}</td>
          <td class="px-4 py-2 border-b">{{ log.type }}</td>
          <td class="px-4 py-2 border-b">{{ log.content }}</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { get } from '@/net/index.js'
import { userUserStore } from '@/stores/UserStore.js'
import router from '@/router/index.js'

const userStore=userUserStore();

// 响应式数据：存放日志数据
const options = reactive({
  log: [],
})

// 跳转到用户详情页面
const GoPersonal = (id) => {
  router.push('/personal/' + id)
}

// 当前选中的分类，默认为“全部”
const selectedCategory = ref('全部')

// 根据选中的分类过滤日志
const filteredLogs = computed(() => {
  if (selectedCategory.value === '全部') {
    return options.log
  }
  return options.log.filter(log => log.type === selectedCategory.value)
})

// 计算所有的分类（去重），供下拉框使用
const categories = computed(() => {
  const cats = new Set()
  options.log.forEach(log => cats.add(log.type))
  return Array.from(cats)
})

// 获取日志数据接口
const selectAllLogs = () => {
  get("api/log/selectAllLogs", {}, (message, data) => {
    options.log = data;
  })
}

// 组件挂载后加载数据
onMounted(() => {
  selectAllLogs()
})

// 格式化时间，转换 ISO 格式到本地显示格式
function formatTime(time) {
  return new Date(time).toLocaleString();
}
</script>

<style scoped>
table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

th {
  background-color: #f8f9fa;
  font-weight: bold;
}

tr:hover {
  background-color: #f1f1f1;
}

/* 隐藏滚动条 */
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
