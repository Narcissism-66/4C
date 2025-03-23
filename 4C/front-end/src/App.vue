<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { post } from '@/net/index.js'
import { userUserStore } from '@/stores/UserStore.js'

const router = useRouter()
const timer = ref(0)  // 用于保存浏览时长的时间
let interval = null  // 用于存储定时器的引用

// 判断用户是否登录
const userStore = userUserStore()

// 计时函数
const startTimer = () => {
  if (userStore.user!=null) {
    setInterval(() => {
      timer.value += 1
    }, 1000)  // 每秒增加一次
  }
}
onMounted(() => {
  // 如果用户已登录，开始计时
  if (userStore.user !== null) {
    startTimer()
  } else {
    // 如果用户未登录，确保不执行计时
    console.log('用户未登录');
  }
  window.addEventListener('beforeunload', stopTimer)
})


// 停止计时
const stopTimer = () => {
  post('api/user/updateUsageTime',{
    usageTime: timer.value
  },(message)=>{
    console.log(message);
  })
}


onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', stopTimer)
  // 离开页面时停止计时
  stopTimer()
  console.log('用户已退出');
})

router.beforeEach((to, from) => {
  // 如果用户离开当前页面，停止计时
  if (interval) {
    console.log('用户已退出');
    stopTimer()
  }
})

watch(
  () => userStore.user, // 监听 userStore.user 的变化
  (newVal, oldVal) => {
    if (oldVal === null && newVal !== null) {
      // 如果原值为 null 且新值不为 null，表示用户已登录
      startTimer();
      console.log('用户已登录');
      // 这里可以进行登录后的逻辑处理
    } else if (oldVal !== null && newVal === null) {
      // 如果原值不为 null 且新值为 null，表示用户已退出
      stopTimer();
      console.log('用户已退出');
      // 这里可以进行退出后的逻辑处理
    }
  }
);


</script>

<template>
  <RouterView />
</template>
