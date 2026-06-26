import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/rooms',
    name: 'Rooms',
    component: () => import('@/views/rooms/RoomList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/rooms/:id',
    name: 'RoomDetail',
    component: () => import('@/views/rooms/RoomDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reservations',
    name: 'Reservations',
    component: () => import('@/views/reservations/ReservationList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/attendance',
    name: 'Attendance',
    component: () => import('@/views/attendance/AttendanceList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/ai/recommend',
    name: 'AIRecommend',
    component: () => import('@/views/ai/AIRecommend.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/ai/chat',
    name: 'AIChat',
    component: () => import('@/views/ai/AIChat.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'rooms',
        name: 'AdminRooms',
        component: () => import('@/views/admin/rooms/RoomManage.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/users/UserManage.vue')
      },
      {
        path: 'statistics',
        name: 'AdminStatistics',
        component: () => import('@/views/admin/statistics/Statistics.vue')
      }
    ]
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  // 未登录用户访问需要认证的页面，重定向到登录
  if (to.meta.requiresAuth && !userStore.isAuthenticated()) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 已登录用户访问登录/注册页，跳转到首页
  if ((to.path === '/login' || to.path === '/register') && userStore.isAuthenticated()) {
    next('/rooms')
    return
  }

  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin && !userStore.isAdmin()) {
    next('/rooms')
    return
  }

  next()
})

export default router