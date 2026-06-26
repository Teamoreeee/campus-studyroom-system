<template>
  <div class="page">
    <NavBar />
    <div class="container">
      <h2>个人中心</h2>

      <el-row :gutter="24">
        <el-col :md="8">
          <el-card class="profile-card">
            <div class="avatar-section">
              <el-avatar :size="80" :src="userStore.userInfo?.avatar" >
                {{ userStore.userInfo?.realName?.charAt(0) || userStore.userInfo?.username?.charAt(0) }}
              </el-avatar>
              <h3>{{ userStore.userInfo?.realName }}</h3>
              <p>{{ userStore.userInfo?.username }}</p>
              <el-tag :type="userStore.isAdmin() ? 'danger' : undefined">{{ userStore.isAdmin() ? '管理员' : '学生' }}</el-tag>
            </div>
            <el-divider />
            <div class="info-item">
              <span class="label">邮箱：</span>
              <span>{{ userStore.userInfo?.email }}</span>
            </div>
            <div class="info-item">
              <span class="label">电话：</span>
              <span>{{ userStore.userInfo?.phone || '未绑定' }}</span>
            </div>
          </el-card>
        </el-col>

        <el-col :md="16">
          <el-card title="修改密码">
            <el-form :model="pwdForm" :rules="pwdRules" ref="pwdRef" label-width="120px">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="submitting" @click="changePassword">修改密码</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'

const userStore = useUserStore()
const pwdRef = ref()
const submitting = ref(false)

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (_rule: any, value: string, callback: Function) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }]
}

const changePassword = async () => {
  const valid = await pwdRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await authApi.changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    window.location.href = '/login'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
}
.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}
h2 {
  margin-bottom: 20px;
  color: #303133;
}
.profile-card {
  text-align: center;
}
.avatar-section {
  padding: 16px 0;
}
.avatar-section h3 {
  margin: 12px 0 4px;
  color: #303133;
}
.avatar-section p {
  color: #909399;
  margin-bottom: 8px;
}
.info-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  color: #606266;
}
.info-item .label {
  color: #909399;
}
</style>
