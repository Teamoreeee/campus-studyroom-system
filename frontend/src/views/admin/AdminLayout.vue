<template>
  <div class="admin-page">
    <NavBar />
    <div class="admin-container">
      <el-aside width="220px" class="admin-aside">
        <el-menu
          :default-active="activeMenu"
          router
          class="admin-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
        >
          <div class="admin-title">管理后台</div>
          <el-menu-item index="/admin/rooms">
            <el-icon><OfficeBuilding /></el-icon>
            <span>自习室管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/statistics">
            <el-icon><DataLine /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '@/components/NavBar.vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: #f5f7fa;
}
.admin-container {
  display: flex;
  padding-top: 60px;
}
.admin-aside {
  position: fixed;
  left: 0;
  top: 60px;
  bottom: 0;
  background: #304156;
}
.admin-menu {
  border-right: none;
  height: 100%;
}
.admin-title {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #1f2d3d;
}
.admin-main {
  margin-left: 220px;
  flex: 1;
  min-height: calc(100vh - 60px);
}
</style>
