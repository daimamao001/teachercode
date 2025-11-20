import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '仪表板', icon: 'DataLine' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'assessments',
        name: 'Assessments',
        component: () => import('@/views/assessment/AssessmentList.vue'),
        meta: { title: '测评管理', icon: 'Document' }
      },
      {
        path: 'content',
        name: 'Content',
        component: () => import('@/views/content/ContentList.vue'),
        meta: { title: '内容管理', icon: 'Files' }
      },
      {
        path: 'ai-config',
        name: 'AIConfig',
        component: () => import('@/views/ai/AIConfig.vue'),
        meta: { title: 'AI配置', icon: 'Setting' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 心理陪伴管理后台` : '心理陪伴管理后台'

  // 登录页面直接放行
  if (to.path === '/login') {
    next()
    return
  }

  // 检查登录状态
  const adminStore = useAdminStore()
  if (!adminStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  next()
})

export default router

