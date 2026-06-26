<template>
  <div class="manage-page">
    <h3>数据统计</h3>

    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :md="8" :lg="4">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalUsers }}</div>
          <div class="stat-label">注册用户</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="4">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalRooms }}</div>
          <div class="stat-label">自习室</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="4">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalReservations }}</div>
          <div class="stat-label">累计预约</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="4">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.todayReservations }}</div>
          <div class="stat-label">今日预约</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="4">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.todayCheckIn }}</div>
          <div class="stat-label">今日签到</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="4">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.violationCount }}</div>
          <div class="stat-label">违规记录</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card title="近7天预约趋势" style="margin-top: 20px">
      <div ref="trendChart" style="height: 350px"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { adminApi } from '@/api/admin'

const trendChart = ref<HTMLDivElement>()

const stats = ref({
  totalUsers: 0,
  totalRooms: 0,
  totalReservations: 0,
  todayReservations: 0,
  todayCheckIn: 0,
  violationCount: 0
})

const loadStats = async () => {
  try {
    stats.value = await adminApi.getStatistics()
    const trend = await adminApi.getReservationTrend(7)
    renderTrend(trend)
  } catch (e) {
    ElMessage.error('加载统计失败')
  }
}

const renderTrend = (data: { date: string; count: number }[]) => {
  if (!trendChart.value) return
  const chart = echarts.init(trendChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(d => d.date) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      data: data.map(d => d.count),
      type: 'line',
      smooth: true,
      areaStyle: { color: 'rgba(64, 158, 255, 0.2)' },
      itemStyle: { color: '#409eff' }
    }]
  })
}

onMounted(loadStats)
</script>

<style scoped>
.manage-page {
  padding: 24px;
}
h3 {
  margin-bottom: 20px;
  color: #303133;
}
.stat-card {
  text-align: center;
  margin-bottom: 16px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
}
.stat-label {
  margin-top: 8px;
  color: #606266;
}
</style>
