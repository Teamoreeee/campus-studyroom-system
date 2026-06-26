<template>
  <div class="page">
    <NavBar />
    <div class="container">
      <h2>AI 智能推荐</h2>

      <el-card class="filter-card">
        <el-form :inline="true" :model="params">
          <el-form-item label="日期">
            <el-date-picker v-model="params.date" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
          </el-form-item>
          <el-form-item label="教学楼">
            <el-select v-model="params.building" placeholder="不限" clearable style="width: 160px">
              <el-option v-for="b in buildings" :key="b" :label="b" :value="b" />
            </el-select>
          </el-form-item>
          <el-form-item label="偏好">
            <el-checkbox v-model="params.preferWindow">靠窗</el-checkbox>
            <el-checkbox v-model="params.preferPower">有电源</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="getRecommendations">获取推荐</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-empty v-if="!loading && recommendations.length === 0" description="点击上方按钮获取智能推荐" />

      <el-row :gutter="16" v-loading="loading">
        <el-col :xs="24" :sm="12" :md="8" v-for="rec in recommendations" :key="rec.recommendationId">
          <el-card class="rec-card" shadow="hover">
            <div class="rec-header">
              <h3>{{ rec.reason }}</h3>
              <el-tag type="warning">匹配度 {{ (rec.score * 100).toFixed(0) }}%</el-tag>
            </div>
            <p><strong>推荐策略：</strong>{{ rec.strategy }}</p>
            <p><strong>推荐时间：</strong>{{ rec.createTime }}</p>
            <el-button type="primary" @click="goReserve(rec.roomId, rec.seatId)">去预约</el-button>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import { aiApi } from '@/api/ai'
import { roomApi } from '@/api/room'
import type { AIRecommendation } from '@/types'

const router = useRouter()
const loading = ref(false)
const recommendations = ref<AIRecommendation[]>([])
const buildings = ref<string[]>([])

const params = reactive({
  date: '',
  building: '',
  preferWindow: false,
  preferPower: false
})

const getRecommendations = async () => {
  loading.value = true
  try {
    recommendations.value = await aiApi.getRecommendations({
      date: params.date || undefined,
      building: params.building || undefined,
      preferWindow: params.preferWindow || undefined,
      preferPower: params.preferPower || undefined
    })
  } catch (e) {
    ElMessage.error('获取推荐失败')
  } finally {
    loading.value = false
  }
}

const goReserve = (roomId: number, seatId: number) => {
  router.push({
    path: `/rooms/${roomId}`,
    query: { seatId: String(seatId) }
  })
}

onMounted(() => {
  const today = new Date()
  params.date = today.toISOString().split('T')[0]
  roomApi.getBuildings().then(b => buildings.value = b).catch(() => {})
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}
h2 {
  margin-bottom: 20px;
  color: #303133;
}
.filter-card {
  margin-bottom: 20px;
}
.rec-card {
  margin-bottom: 16px;
}
.rec-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.rec-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
  max-width: 70%;
}
.rec-card p {
  color: #606266;
  font-size: 14px;
  margin: 6px 0;
}
</style>
