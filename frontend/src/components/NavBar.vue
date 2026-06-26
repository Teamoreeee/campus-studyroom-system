<template>
  <el-header class="navbar">
    <div class="logo" @click="$router.push('/')">
      <el-icon size="24"><Reading /></el-icon>
      <span>校园自习室</span>
    </div>
    <nav class="nav-links">
      <router-link to="/" exact-active-class="active">首页</router-link>
      <router-link to="/rooms" active-class="active">自习室</router-link>
      <router-link to="/ai/recommend" active-class="active">智能推荐</router-link>
      <router-link to="/ai/chat" active-class="active">AI 客服</router-link>
    </nav>
    <div class="user-actions">
      <template v-if="userStore.isAuthenticated()">
        <router-link to="/reservations">我的预约</router-link>
        <router-link to="/attendance">考勤</router-link>
        <el-dropdown v-if="userStore.isAdmin()">
          <span class="admin-link">
            管理<el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item><router-link to="/admin/rooms">自习室管理</router-link></el-dropdown-item>
              <el-dropdown-item><router-link to="/admin/users">用户管理</router-link></el-dropdown-item>
              <el-dropdown-item><router-link to="/admin/statistics">数据统计</router-link></el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-dropdown>
          <span class="user-link">
            {{ userStore.userInfo?.realName || userStore.userInfo?.username }}<el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
      <template v-else>
        <router-link to="/login">登录</router-link>
        <router-link to="/register">注册</router-link>
      </template>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 100;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
  cursor: pointer;
}
.nav-links,
.user-actions {
  display: flex;
  align-items: center;
  gap: 24px;
}
.nav-links a,
.user-actions a {
  text-decoration: none;
  color: #606266;
  font-size: 14px;
  transition: color 0.2s;
}
.nav-links a:hover,
.user-actions a:hover {
  color: #409eff;
}
.nav-links a.active,
.user-actions a.router-link-active {
  color: #409eff;
  font-weight: 500;
}
.user-link,
.admin-link {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
}
.admin-link {
  color: #e6a23c;
}
</style>
