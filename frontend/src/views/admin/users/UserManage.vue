<template>
  <div class="manage-page">
    <h3>用户管理</h3>

    <el-card class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="用户名/姓名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.role" placeholder="全部" clearable style="width: 120px">
            <el-option label="学生" value="student" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadUsers">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="users" v-loading="loading" border stripe
    >
      <el-table-column prop="userId" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="真实姓名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="电话" />
      <el-table-column label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="row.role === 'admin' ? 'danger' : undefined">{{ row.role === 'admin' ? '管理员' : '学生' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            :type="row.status === 1 ? 'danger' : 'success'"
            size="small"
            @click="toggleStatus(row as User)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        @change="loadUsers"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'
import type { User } from '@/types'

const loading = ref(false)
const users = ref<User[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  role: ''
})

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await adminApi.getAllUsers({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      role: query.role || undefined
    })
    users.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (row: User) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await adminApi.updateUserStatus(row.userId, newStatus)
    ElMessage.success('操作成功')
    loadUsers()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

loadUsers()
</script>

<style scoped>
.manage-page {
  padding: 24px;
}
h3 {
  margin-bottom: 20px;
  color: #303133;
}
.filter-card {
  margin-bottom: 20px;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
}
</style>
