<template>
  <contextHolder/>
  <div class="p-6 bg-white rounded-lg shadow">
    <h1 class="text-2xl font-bold mb-4">反馈信息</h1>

    <!-- 筛选区域 -->
    <div class="mb-4 flex items-center">
      <label for="filter" class="mr-2 font-medium">筛选类型:</label>
      <select id="filter" v-model="selectedType" class="p-2 border rounded">
        <option value="">全部</option>
        <option value="issue">问题</option>
        <option value="suggestion">建议</option>
        <option value="other">其它</option>
      </select>
    </div>

    <!-- 反馈信息列表 -->
    <div class="overflow-x-auto">
      <table class="min-w-full bg-white">
        <thead>
        <tr>
          <th class="py-2 px-4 border-b">序号</th>
          <th class="py-2 px-4 border-b">类型</th>
          <th class="py-2 px-4 border-b">标题</th>
          <th class="py-2 px-4 border-b">时间</th>
          <th class="py-2 px-4 border-b">用户</th>
        </tr>
        </thead>
        <tbody>
        <tr
          v-for="(item, index) in filteredFeedback"
          :key="item.id"
          :class="getTypeClass(item.type)"
        >
          <td class="py-2 px-4 border-b">{{ index + 1 }}</td>
          <td class="py-2 px-4 border-b">{{ typeMap[item.type] || "未知" }}</td>
          <td
            class="py-2 px-4 border-b text-blue-600 cursor-pointer"
            @click="openModal(item)"
          >
            {{ item.title }}
          </td>
          <td class="py-2 px-4 border-b">{{ formatTime(item.time) }}</td>
          <td class="py-2 px-4 border-b">{{ item.username }}</td>
        </tr>
        <tr v-if="filteredFeedback.length === 0">
          <td class="py-2 px-4 text-center" colspan="5">暂无反馈信息</td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- 反馈详情弹窗 -->
    <div
      v-if="showModal"
      class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50"
    >
      <div class="bg-white p-6 rounded-lg shadow-lg w-96 relative">
        <h2 class="text-xl font-bold mb-2">{{ selectedFeedback.title }}</h2>
        <p class="mb-2"><strong>内容:</strong> {{ selectedFeedback.content }}</p>
        <p class="mb-2"><strong>时间:</strong> {{ formatTime(selectedFeedback.time) }}</p>
        <p class="mb-2"><strong>用户:</strong> {{ selectedFeedback.username }}</p>
        <p class="mb-2"><strong>评分:</strong> {{ renderStars(selectedFeedback.evaluate) }}</p>

        <!-- 回复区域 -->
        <div class="mb-2">
          <label class="block font-medium mb-1">回复:</label>
          <textarea
            v-model="replyMessage"
            class="w-full border rounded p-2"
            rows="3"
            placeholder="请输入回复内容"
          ></textarea>
        </div>
        <div class="flex justify-end space-x-4">
          <button @click="closeModal" class="px-4 py-2 bg-gray-300 rounded">取消</button>
          <button @click="submitReply(selectedFeedback.id)" class="px-4 py-2 bg-blue-500 text-white rounded">发送回复</button>
        </div>

        <!-- 关闭按钮 -->
        <button class="absolute top-2 right-2 text-xl" @click="closeModal">&times;</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from "vue";
import { get, post } from "@/net/index.js";
import { message } from 'ant-design-vue'

const [messageApi, contextHolder] = message.useMessage();
const option = reactive({ feedback: [] });

const typeMap = {
  issue: "问题",
  suggestion: "建议",
  other: "其它",
};

const selectedType = ref("");
const showModal = ref(false);
const selectedFeedback = ref({});
const replyMessage = ref("");

// 获取反馈数据
const fetchFeedback = async () => {
  await get("api/feedback/getFeedbackList", {}, (msg, data) => {
    option.feedback = data || [];
  });
};

// 过滤反馈数据（只显示未回复的）
const filteredFeedback = computed(() => {
  return option.feedback
    .filter(item => !selectedType.value || item.type === selectedType.value);
});

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return "未知时间";
  return new Date(timeStr).toLocaleString();
};

// 渲染评分星星
const renderStars = (rating) => {
  return rating ? "⭐".repeat(rating) : "无";
};

// 打开弹窗，显示反馈详情，并初始化回复内容为空
const openModal = (item) => {
  selectedFeedback.value = item;
  replyMessage.value = "";
  showModal.value = true;
};

// 关闭弹窗
const closeModal = () => {
  showModal.value = false;
  selectedFeedback.value = {};
};

// 根据反馈类型返回对应的类名
const getTypeClass = (type) => {
  switch (type) {
    case "issue": return "bg-red-200";
    case "suggestion": return "bg-green-100";
    case "other": return "bg-yellow-100";
    default: return "";
  }
};

// 提交回复
const submitReply = async (id) => {
  if (!replyMessage.value.trim()) {
    alert("请输入回复内容");
    return;
  }
  await post("api/feedback/solveFeedback",
    { id: id, response: replyMessage.value },
    async (msg, data) => {
      messageApi.success("回复成功！");
      closeModal();
      await fetchFeedback();
    }
  );
};

// 组件加载时获取数据
onMounted(fetchFeedback);
</script>

<style scoped>
table {
  border-collapse: collapse;
  width: 100%;
}

th, td {
  text-align: left;
}

button {
  cursor: pointer;
}

.bg-red-200 {
  background-color: #fecaca;
}

.bg-green-100 {
  background-color: #d1fae5;
}

.bg-yellow-100 {
  background-color: #fef9c3;
}
</style>
