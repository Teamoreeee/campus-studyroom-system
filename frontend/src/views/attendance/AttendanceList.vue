<template>
  <div class="page">
    <NavBar />
    <div class="container">
      <h2>考勤记录</h2>

      <el-row :gutter="16" style="margin-bottom: 20px">
        <el-col :span="4">
          <el-statistic title="今日预约" :value="todayStats.totalReservations" />
        </el-col>
        <el-col :span="4">
          <el-statistic title="已签到" :value="todayStats.checkedIn" />
        </el-col>
        <el-col :span="4">
          <el-statistic title="未签到" :value="todayStats.notCheckedIn" />
        </el-col>
        <el-col :span="4">
          <el-statistic title="迟到次数" :value="todayStats.lateCount" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="今日学习时长" :value="todayStudyMinutes">
            <template #suffix>分钟</template>
          </el-statistic>
        </el-col>
      </el-row>

      <el-card class="section-card">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span>我的可签到预约</span>
            <el-button type="danger" size="small" :loading="clearLoading" @click="handleClearExpired">
              清空已过期预约
            </el-button>
          </div>
        </template>
        <el-table :data="reservations" v-loading="reservationLoading" border stripe>
          <el-table-column prop="reservationId" label="预约号" width="90" />
          <el-table-column label="自习室" min-width="120">
            <template #default="{ row }">
              {{ row.roomName || `自习室 ${row.roomId}` }}
            </template>
          </el-table-column>
          <el-table-column label="座位" width="90">
            <template #default="{ row }">
              {{ seats[row.seatId] || `座位 ${row.seatId}` }}
            </template>
          </el-table-column>
          <el-table-column prop="reserveDate" label="日期" width="120" />
          <el-table-column prop="timeRange" label="时间段" width="140" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.computedStatus)">
                {{ statusTagText(row.computedStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button
                v-if="row.computedStatus === 'checkable'"
                type="primary"
                size="small"
                :loading="row.loading"
                @click="handleCheckIn(row)"
              >签到</el-button>
              <el-button
                v-else-if="row.computedStatus === 'active'"
                type="warning"
                size="small"
                :loading="row.loading"
                @click="handleCheckOut(row)"
              >签退</el-button>
              <el-tag v-else-if="row.computedStatus === 'completed'" type="success" size="small">已完成</el-tag>
              <el-tag v-else-if="row.computedStatus === 'expired'" type="info" size="small">已过期</el-tag>
              <el-tag v-else-if="row.computedStatus === 'upcoming'" type="info" size="small">未开始</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!reservationLoading && reservations.length === 0" description="暂无今日可签到预约" />
      </el-card>

      <el-card class="section-card" title="考勤历史">
        <template #header>
          <span>考勤历史</span>
        </template>
        <el-table :data="attendanceList" v-loading="loading" border stripe>
          <el-table-column prop="attendanceId" label="ID" width="80" />
          <el-table-column prop="reservationId" label="预约号" width="90" />
          <el-table-column prop="checkInTime" label="签到时间" width="180" />
          <el-table-column prop="checkOutTime" label="签退时间" width="180" />
          <el-table-column prop="duration" label="学习时长" width="120">
            <template #default="{ row }">
              {{ row.duration }} 分钟
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import { attendanceApi } from '@/api/attendance'
import { reservationApi } from '@/api/reservation'
import { roomApi } from '@/api/room'
import type { Attendance, Reservation } from '@/types'

interface ReservationWithAttendance extends Reservation {
  computedStatus: 'checkable' | 'active' | 'completed' | 'expired' | 'upcoming'
  loading: boolean
  roomName?: string
  seatNo?: string
  timeRange?: string
}

const loading = ref(false)
const reservationLoading = ref(false)
const clearLoading = ref(false)
const attendanceList = ref<Attendance[]>([])
const reservations = ref<ReservationWithAttendance[]>([])
const seats = ref<Record<number, string>>({})

const todayStats = ref({
  totalReservations: 0,
  checkedIn: 0,
  notCheckedIn: 0,
  lateCount: 0
})

const todayStudyMinutes = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  return attendanceList.value
    .filter((a: Attendance) => a.checkInTime && a.checkInTime.startsWith(today) && a.duration > 0)
    .reduce((sum: number, a: Attendance) => sum + (a.duration || 0), 0)
})

const statusText = (status: string) => {
  const map: Record<string, string> = {
    NORMAL: '正常', LATE: '迟到', EARLY_LEAVE: '早退', ABSENT: '缺席'
  }
  return map[status] || status
}

const statusType = (status: string) => {
  const map: Record<string, any> = {
    NORMAL: 'success', LATE: 'warning', EARLY_LEAVE: 'warning', ABSENT: 'danger'
  }
  return map[status] || 'info'
}

const statusTagText = (status: string) => {
  const map: Record<string, string> = {
    none: '未签到', active: '学习中', completed: '已完成',
    checkable: '可签到', expired: '已过期', upcoming: '未开始'
  }
  return map[status] || status
}

const statusTagType = (status: string) => {
  const map: Record<string, any> = {
    none: 'info', active: 'warning', completed: 'success',
    checkable: 'primary', expired: 'info', upcoming: 'info'
  }
  return map[status] || 'info'
}

// 根据预约时间段和考勤记录计算预约状态
const computeReservationStatus = (r: Reservation, attendances: Attendance[]) => {
  const now = new Date()
  const dateStr = r.reserveDate
  const start = new Date(`${dateStr}T${r.startTime}`)
  const end = new Date(`${dateStr}T${r.endTime}`)

  const active = attendances.find(
    (a: Attendance) => String(a.reservationId) === String(r.reservationId) && !a.checkOutTime
  )
  const completed = attendances.find(
    (a: Attendance) => String(a.reservationId) === String(r.reservationId) && a.checkOutTime
  )

  if (active) return 'active'
  if (completed) return 'completed'
  if (end < now) return 'expired'
  if (start > now) return 'upcoming'
  return 'checkable'
}

const loadAttendance = async () => {
  loading.value = true
  try {
    const res = await attendanceApi.getMyAttendance({ page: 1, size: 100 })
    attendanceList.value = res.list
  } catch (e) {
    ElMessage.error('加载考勤记录失败')
  } finally {
    loading.value = false
  }
}

const loadReservations = async () => {
  reservationLoading.value = true
  try {
    const res = await reservationApi.getMyReservations({ page: 1, size: 100 })
    // 只保留今天及以后的预约
    const today = new Date().toISOString().split('T')[0]
    const validReservations = res.list.filter((r: Reservation) => r.reserveDate >= today)

    reservations.value = validReservations.map((r: Reservation) => {
      return {
        ...r,
        loading: false,
        computedStatus: computeReservationStatus(r, attendanceList.value),
        timeRange: `${r.startTime} - ${r.endTime}`
      }
    })
  } catch (e) {
    ElMessage.error('加载预约失败')
  } finally {
    reservationLoading.value = false
  }
}

const loadStats = async () => {
  try {
    todayStats.value = await attendanceApi.getTodayStats()
  } catch (e) {}
}

const loadSeatNumbers = async () => {
  const roomIds = [...new Set(reservations.value.map(r => r.roomId))]
  await Promise.all(roomIds.map(async (roomId: number) => {
    try {
      const seatList = await roomApi.getRoomSeats(roomId)
      seatList.forEach((s: any) => {
        if (s.seatId) {
          seats.value[s.seatId] = s.seatNo || `座位 ${s.seatId}`
        }
      })
    } catch (e) {}
  }))
}

const handleCheckIn = async (row: ReservationWithAttendance) => {
  row.loading = true
  try {
    await attendanceApi.checkIn({ reservationId: row.reservationId })
    ElMessage.success('签到成功')
    await loadAll()
  } catch (e: any) {
    ElMessage.error(e?.message || '签到失败')
  } finally {
    row.loading = false
  }
}

const handleCheckOut = async (row: ReservationWithAttendance) => {
  row.loading = true
  try {
    await attendanceApi.checkOut(row.reservationId)
    ElMessage.success('签退成功')
    await loadAll()
  } catch (e: any) {
    ElMessage.error(e?.message || '签退失败')
  } finally {
    row.loading = false
  }
}

const handleClearExpired = async () => {
  clearLoading.value = true
  try {
    const count = await reservationApi.clearExpiredReservations()
    ElMessage.success(`已清空 ${count} 条已过期/已取消预约`)
    await loadAll()
  } catch (e) {
    ElMessage.error('清空失败')
  } finally {
    clearLoading.value = false
  }
}

const loadAll = async () => {
  await loadAttendance()
  await loadReservations()
  await loadSeatNumbers()
  await loadStats()
}

onMounted(() => {
  loadAll()
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
.section-card {
  margin-bottom: 20px;
}
.section-card :deep(.el-card__header) {
  font-weight: 600;
  color: #303133;
}
</style>
