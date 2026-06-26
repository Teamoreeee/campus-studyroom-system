import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/animations.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import request from './utils/request'
import { useUserStore } from './stores/user'

const app = createApp(App)

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

const pinia = createPinia()
app.use(pinia)

// 初始化：从 localStorage 恢复 token
const token = localStorage.getItem('token')
if (token) {
  request.defaults.headers.common['Authorization'] = `Bearer ${token}`
  const userStore = useUserStore()
  userStore.token = token
  userStore.fetchUserInfo().catch(() => {
    // 获取用户信息失败则清除 token，由路由守卫处理跳转
    userStore.logout()
  })
}

app.use(router)
app.use(ElementPlus)

app.mount('#app')