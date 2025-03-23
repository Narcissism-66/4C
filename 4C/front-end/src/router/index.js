import { createRouter, createWebHistory } from 'vue-router'
import { userUserStore } from '@/stores/UserStore.js'
import { get } from '@/net/index.js'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '导航',
      component: () => import('../views/NavBar.vue'),
      children:[
        {
          path: '/',
          name: '首页',
          component: () => import('../views/HomePage.vue'),
        },
        {
          path: '/admin',
          name: '管理者',
          component: () => import('../components/Manage/Admin.vue'),
        },
        {
          path: '/personal/:id',
          name: '个人空间',
          component: () => import('../components/User/Personal.vue'),
        },
        {
          path: '/viewReport/:id',
          name: '报告',
          component: () => import('../components/Report/ViewReport.vue'),
        },
        {
          path: '/report',
          name: '查看报告',
          component: () => import('../components/Report/Report.vue'),
        },
        {
          path: '/feedback',
          name: '反馈界面',
          component: () => import('../views/FeedBack.vue'),
        },
        {
          path: '/AiChat',
          name: 'AI',
          component: () => import('../components/AI/ChatAI.vue'),
        },
        {
          path: '/single',
          name: '单一可视化',
          component: () => import('../views/SingleVisualization.vue'),
        },
        {
          path: '/merge',
          name: '合并可视化',
          component: () => import('../views/MergeVisualization.vue'),
        },
        {
          path: '/customize',
          name: '可视化大屏',
          component: () => import('../views/Customize.vue'),
        }
      ]
    },
    {
      path: '/introduction',
      name: '介绍',
      component: () => import('../views/Introduction.vue'),
    },
    {
      path: '/shareReport/:id',
      name: '分享',
      component: () => import('../components/Report/SharingReport.vue'),
    },
  ],
})

router.beforeEach((to, from, next)=>
{
  const userStore = userUserStore()
  const publicMap = new Map()
  publicMap.set('/introduction', 2)
  publicMap.set('/share', 3)
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
          next('/introduction')
        }, (message, data) => {
          next('/introduction')
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
