import { createRouter, createWebHistory } from 'vue-router'
import { userUserStore } from '@/stores/UserStore.js'
import { get } from '@/net/index.js'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '首页',
      component: () => import('../views/HomePage.vue'),
    },
    {
      path: '/echarts',
      name: '数据可视化',
      component: () => import('../views/DataVisualization.vue'),
    },
    {
      path: '/upload',
      name: '上传',
      component: () => import('../views/DataUpload.vue'),
    },
    {
      path: '/auth',
      name: '登陆',
      component: () => import('../components/Login.vue'),
    },
    {
      path: '/register',
      name: '注册',
      component: () => import('../components/Register.vue'),
    },
  ],
})

router.beforeEach((to, from, next)=>
{
  const userStore = userUserStore()
  const publicMap = new Map()
  publicMap.set('/auth', 2)
  publicMap.set('/register', 3)
  publicMap.set('/error/401', 4)
  publicMap.set('/error/404', 5)
  if (to.matched.length === 0) next('error/404')

  if (!publicMap.has(to.path)) {
    // 不是访问根路径，检查用户状态
    const user = userStore.user; // 假设你的用户状态保存在Vuex的`user`状态中
    if (user === null) {
      get('api/user/information', {},
        (message, data) => {
          userStore.login(data);
          next();
        }, (message, data) => {
          next('/auth')
        }, (message, data) => {
          next('/auth')
        }
      )
    } else {
      // 用户已登录，允许路由继续
      next();
    }
  } else {
    // 访问的是根路径或其他公开路径，直接放行
    next();
  }
})

export default router
