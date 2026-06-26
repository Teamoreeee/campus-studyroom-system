<template>
  <div class="page">
    <NavBar />
    <div class="container">
      <h2>我的预约</h2>

      <el-card class="filter-card">
        <el-form :inline="true" :model="query">
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
              <el-option label="待确认" value="PENDING" />
              <el-option label="已确认" value="CONFIRMED" />
              <el-option label="已签到" value="CHECKED_IN" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
              <el-option label="已超时" value="EXPIRED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadReservations">查询</el-button>
            <el-button @click="query.status = ''; loadReservations()">重置</el-button>
            <el-button type="danger" @click="handleClearExpired">清空已过期/已取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-table :data="enrichedReservations" v-loading="loading" border stripe
                :row-class-name="tableRowClassName">
        <el-table-column prop="reservationId" label="预约ID" width="90" />
        <el-table-column label="自习室" min-width="120">
          <template #default="{ row }">{{ getRoomName(row.roomId) }}</template>
        </el-table-column>
        <el-table-column label="座位" width="100">
          <template #default="{ row }">{{ getSeatNo(row.seatId) }}</template>
        </el-table-column>
        <el-table-column prop="reserveDate" label="日期" width="120" />
        <el-table-column prop="startTime" label="开始" width="90" />
        <el-table-column prop="endTime" label="结束" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.displayStatus)">{{ statusText(row.displayStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.displayStatus === 'CONFIRMED' && !row.isExpired" type="success" size="small"
                      :loading="row.loading" @click="handleCheckIn(row)">签到</el-button>
            <el-button v-if="row.displayStatus === 'CHECKED_IN' && !row.isExpired" type="warning" size="small"
                      :loading="row.loading" @click="handleCheckOut(row)">签退</el-button>
            <el-button v-if="canCancel(row) && !row.isExpired" type="danger" size="small"
                      @click="handleCancel(row.reservationId)">取消</el-button>
            <el-tag v-if="row.isExpired" type="info" size="small">已超时</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          layout="total, prev, pager, next"
          @change="loadReservations"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import { reservationApi } from '@/api/reservation'
import { roomApi } from '@/api/room'
import { attendanceApi } from '@/api/attendance'
import type { Reservation, Attendance } from '@/types'

interface EnrichedReservation extends Reservation {
  displayStatus: string
  isExpired: boolean
  loading: boolean
}

const loading = ref(false)
const reservations = ref<Reservation[]>([])
const attendances = ref<Attendance[]>([])
const total = ref(0)
const rooms = ref<Record<number, string>>({})
const seats = ref<Record<number, string>>({})

const query = reactive({
  page: 1,
  size: 10,
  status: ''
})

const statusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消',
    CHECKED_IN: '已签到', COMPLETED: '已完成', EXPIRED: '已过期', VIOLATED: '违规'
  }
  return map[status] || status
}

const statusType = (status: string) => {
  const map: Record<string, any> = {
    PENDING: 'warning', CONFIRMED: 'primary', CANCELLED: 'info',
    CHECKED_IN: 'success', COMPLETED: 'success', EXPIRED: 'info', VIOLATED: 'danger'
  }
  return map[status] || 'info'
}

const getRoomName = (roomId: number) => rooms.value[roomId] || `自习室#${roomId}`
const getSeatNo = (seatId: number) => seats.value[seatId] || `座位#${seatId}`

const isExpired = (row: Reservation) => {
  const now = new Date()
  const end = new Date(`${row.reserveDate}T${row.endTime}`)
  return now > end
}

const getAttendanceStatus = (reservationId: number) => {
  const active = attendances.value.find(
    a => String(a.reservationId) === String(reservationId) && a.status === 'NORMAL' && !a.checkOutTime
  )
  if (active) return 'CHECKED_IN'
  const completed = attendances.value.find(
    a => String(a.reservationId) === String(reservationId) && a.checkOutTime
  )
  if (completed) return 'COMPLETED'
  return null
}

const enrichedReservations = computed(() => {
  return reservations.value.map((r: Reservation) => {
    const expired = isExpired(r)
    const attendanceStatus = getAttendanceStatus(r.reservationId)
    let displayStatus = r.status
    if (expired && ['PENDING', 'CONFIRMED'].includes(r.status)) {
      displayStatus = 'EXPIRED'
    } else if (attendanceStatus) {
      displayStatus = attendanceStatus
    }
    return {
      ...r,
      displayStatus,
      isExpired: expired,
      loading: false
    } as EnrichedReservation
  })
})

const tableRowClassName = ({ row }: { row: EnrichedReservation }) => {
  return row.isExpired ? 'expired-row' : ''
}

const canCancel = (row: Reservation) => ['PENDING', 'CONFIRMED'].includes(row.status)

const loadAttendances = async () => {
  try {
    const res = await attendanceApi.getMyAttendance({ page: 1, size: 100 })
    attendances.value = res.list
  } catch (e) {}
}

const loadReservations = async () => {
  loading.value = true
  try {
    await loadAttendances()
    const res = await reservationApi.getMyReservations({
      page: query.page,
      size: query.size,
      status: query.status || undefined
    })
    reservations.value = res.list
    total.value = res.total

    // 加载关联的房间和座位信息
    const roomIds = [...new Set(res.list.map(r => r.roomId))]
    await Promise.all(roomIds.map(async id => {
      try {
        rooms.value[id] = (await roomApi.getRoomDetail(id)).roomName
        const seatList = await roomApi.getRoomSeats(id)
        seatList.forEach((s: any) => {
          if (s.seatId) {
            seats.value[s.seatId] = s.seatNo || `座位 ${s.seatId}`
          }
        })
      } catch (e) {}
    }))
  } finally {
    loading.value = false
  }
}

const handleCancel = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定取消该预约吗？', '提示', { type: 'warning' })
    await reservationApi.cancelReservation(id)
    ElMessage.success('取消成功')
    loadReservations()
  } catch (e) {
    // cancel
  }
}

const handleCheckIn = async (row: EnrichedReservation) => {
  row.loading = true
  try {
    await attendanceApi.checkIn({ reservationId: row.reservationId })
    ElMessage.success('签到成功')
    await loadReservations()
  } catch (e: any) {
    ElMessage.error(e?.message || '签到失败')
  } finally {
    row.loading = false
  }
}

const handleCheckOut = async (row: EnrichedReservation) => {
  row.loading = true
  try {
    await attendanceApi.checkOut(row.reservationId)
    ElMessage.success('签退成功')
    await loadReservations()
  } catch (e: any) {
    ElMessage.error(e?.message || '签退失败')
  } finally {
    row.loading = false
  }
}

const handleClearExpired = async () => {
  try {
    await ElMessageBox.confirm(
      '确定清空所有已过期和已取消的预约记录吗？此操作不可恢复！',
      '警告',
      { confirmButtonText: '确定清空', cancelButtonText: '取消', type: 'warning' }
    )
    const count = await reservationApi.clearExpiredReservations()
    ElMessage.success(`成功清空 ${count} 条预约记录`)
    await loadReservations()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || '清空失败')
    }
  }
}

onMounted(loadReservations)
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
.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
}
:deep(.expired-row) {
  background-color: #f5f7fa !important;
  color: #909399;
}
:deep(.expired-row td) {
  background-color: #f5f7fa !important;
}
</style>
