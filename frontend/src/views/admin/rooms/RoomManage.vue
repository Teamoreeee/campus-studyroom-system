<template>
  <div class="manage-page">
    <h3>自习室管理</h3>

    <el-card class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="自习室名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRooms">查询</el-button>
          <el-button type="success" @click="openDialog()">新增自习室</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="rooms" v-loading="loading" border stripe
    >
      <el-table-column prop="roomId" label="ID" width="70" />
      <el-table-column prop="roomName" label="名称" />
      <el-table-column prop="building" label="教学楼" width="120" />
      <el-table-column prop="floor" label="楼层" width="80" />
      <el-table-column prop="capacity" label="容量" width="90" />
      <el-table-column prop="seatCount" label="座位数" width="90" />
      <el-table-column prop="openTime" label="开放" width="100" />
      <el-table-column prop="closeTime" label="关闭" width="100" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '开放' : '关闭' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openDialog(row as StudyRoom)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row.roomId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        @change="loadRooms"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑自习室' : '新增自习室'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="roomName">
          <el-input v-model="form.roomName" />
        </el-form-item>
        <el-form-item label="教学楼" prop="building">
          <el-input v-model="form.building" />
        </el-form-item>
        <el-form-item label="楼层" prop="floor">
          <el-input-number v-model="form.floor" :min="1" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" />
        </el-form-item>
        <el-form-item label="座位数" prop="seatCount">
          <el-input-number v-model="form.seatCount" :min="1" />
        </el-form-item>
        <el-form-item label="设施" prop="facilities">
          <el-input v-model="form.facilities" />
        </el-form-item>
        <el-form-item label="开放时间" prop="openTime">
          <el-time-select v-model="form.openTime" start="06:00" step="00:30" end="23:30" />
        </el-form-item>
        <el-form-item label="关闭时间" prop="closeTime">
          <el-time-select v-model="form.closeTime" start="06:00" step="00:30" end="23:30" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">开放</el-radio>
            <el-radio :label="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import type { StudyRoom } from '@/types'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const formRef = ref()
const rooms = ref<StudyRoom[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

const defaultForm = {
  roomName: '',
  building: '',
  floor: 1,
  capacity: 50,
  seatCount: 50,
  facilities: '',
  openTime: '08:00',
  closeTime: '22:00',
  status: 1
}

const form = reactive({ ...defaultForm })

const rules = {
  roomName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  building: [{ required: true, message: '请输入教学楼', trigger: 'blur' }],
  floor: [{ required: true, message: '请输入楼层', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }],
  seatCount: [{ required: true, message: '请输入座位数', trigger: 'blur' }],
  openTime: [{ required: true, message: '请选择开放时间', trigger: 'change' }],
  closeTime: [{ required: true, message: '请选择关闭时间', trigger: 'change' }]
}

const loadRooms = async () => {
  loading.value = true
  try {
    const res = await adminApi.getAllRooms({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined
    })
    rooms.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const openDialog = (row?: StudyRoom) => {
  isEdit.value = !!row
  editId.value = row?.roomId || null
  Object.assign(form, row ? { ...row } : defaultForm)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      await adminApi.updateRoom(editId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await adminApi.createRoom({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadRooms()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除该自习室吗？', '提示', { type: 'warning' })
    await adminApi.deleteRoom(id)
    ElMessage.success('删除成功')
    loadRooms()
  } catch (e) {}
}

loadRooms()
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
