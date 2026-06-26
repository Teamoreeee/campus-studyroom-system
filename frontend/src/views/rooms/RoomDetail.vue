<template>
  <div class="page">
    <NavBar />
    <div class="container" v-loading="loading">
      <el-page-header @back="$router.push('/rooms')" :content="room?.roomName" />

      <el-row :gutter="24" style="margin-top: 20px">
        <el-col :md="16">
          <el-card title="座位分布">
            <div class="seat-map">
              <div
                v-for="seat in seats"
                :key="seat.seatId"
                class="seat"
                :class="getSeatClass(seat)"
                @click="selectSeat(seat)"
              >
                <span>{{ seat.seatNo }}</span>
                <small v-if="getSeatType(seat.seatType) !== '普通'" class="feature-tag">
                  {{ getSeatType(seat.seatType) }}
                </small>
              </div>
            </div>
            <div class="legend">
              <span><i class="dot available"></i> 可选</span>
              <span><i class="dot reserved"></i> 已预约</span>
              <span><i class="dot in-use"></i> 使用中</span>
              <span><i class="dot maintaining"></i> 维护中</span>
            </div>
          </el-card>
        </el-col>

        <el-col :md="8">
          <el-card title="预约信息">
            <el-form label-position="top">
              <el-form-item label="选择日期">
                <el-date-picker v-model="selectedDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" :disabled-date="disabledDate" />
              </el-form-item>
              <el-form-item label="时间段">
                <el-select v-model="selectedTimeSlot" placeholder="选择时间段">
                <el-option
                  v-for="slot in timeSlotOptions"
                  :key="slot.value"
                  :label="slot.label"
                  :value="slot.value"
                  :disabled="reservedTimeSlots.includes(slot.label)"
                />
              </el-select>
              </el-form-item>
              <el-form-item label="已选座位">
                <el-input :model-value="selectedSeat?.seatNo || '未选择'" disabled />
              </el-form-item>
              <el-button type="primary" :disabled="!canReserve" :loading="submitting" @click="handleReserve" style="width: 100%">
                立即预约
              </el-button>
            </el-form>
          </el-card>

          <el-card title="自习室信息" style="margin-top: 16px">
            <p><strong>教学楼：</strong>{{ room?.building }} {{ room?.floor }}楼</p>
            <p><strong>容量：</strong>{{ room?.capacity }}人</p>
            <p><strong>开放时间：</strong>{{ room?.openTime }} - {{ room?.closeTime }}</p>
            <p><strong>设施：</strong>
              <span v-if="getFacilityTags(room?.facilities).length">
                <el-tag v-for="tag in getFacilityTags(room?.facilities)" :key="tag" size="small" type="info" style="margin-right: 6px; margin-bottom: 4px;">{{ tag }}</el-tag>
              </span>
              <span v-else>无</span>
            </p>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import { roomApi } from '@/api/room'
import { reservationApi } from '@/api/reservation'
import type { StudyRoom, Seat } from '@/types'

const route = useRoute()
const router = useRouter()
const roomId = computed(() => Number(route.params.id))

const room = ref<StudyRoom | null>(null)
const seats = ref<Seat[]>([])
const loading = ref(false)
const submitting = ref(false)
const selectedSeat = ref<Seat | null>(null)
const selectedDate = ref('')
const selectedTimeSlot = ref('')
const reservedTimeSlots = ref<string[]>([])

const timeSlotOptions = [
  { label: '08:00 - 10:00', value: '1' },
  { label: '10:00 - 12:00', value: '2' },
  { label: '14:00 - 16:00', value: '3' },
  { label: '16:00 - 18:00', value: '4' },
  { label: '19:00 - 21:00', value: '5' }
]

const canReserve = computed(() => {
  return selectedSeat.value?.status === 'AVAILABLE' && selectedDate.value && selectedTimeSlot.value
})

const getSeatClass = (seat: Seat) => {
  if (selectedSeat.value?.seatId === seat.seatId) return 'selected'
  return {
    AVAILABLE: 'available',
    RESERVED: 'reserved',
    IN_USE: 'in-use',
    MAINTAINING: 'maintaining'
  }[seat.status]
}

const getSeatType = (type: string) => {
  const map: Record<string, string> = {
    NORMAL: '普通',
    WINDOW: '靠窗',
    CORNER: '角落',
    POWER: '电源',
    DISABLED: '无障碍'
  }
  return map[type] || type
}

const facilityMap: Record<string, string> = {
  ac: '空调',
  wifi: 'WiFi',
  power: '电源',
  printer: '打印机',
  whiteboard: '白板',
  projector: '投影仪',
  water: '饮水机',
  locker: '储物柜'
}

const getFacilityTags = (facilities: string | undefined) => {
  if (!facilities) return []
  try {
    const obj = JSON.parse(facilities)
    return Object.entries(obj)
      .filter(([, value]) => value === true)
      .map(([key]) => facilityMap[key] || key)
  } catch (e) {
    return facilities ? [facilities] : []
  }
}

const disabledDate = (time: Date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const maxDate = new Date(today)
  maxDate.setDate(maxDate.getDate() + 7)
  return time.getTime() < today.getTime() || time.getTime() > maxDate.getTime()
}

const selectSeat = (seat: Seat) => {
  if (seat.status !== 'AVAILABLE') {
    ElMessage.warning('该座位不可选')
    return
  }
  selectedSeat.value = seat
}

const loadData = async () => {
  loading.value = true
  try {
    room.value = await roomApi.getRoomDetail(roomId.value)
    // 查询当前用户在该日期已预约的时间段
    reservedTimeSlots.value = []
    if (selectedDate.value) {
      try {
        reservedTimeSlots.value = await reservationApi.getMyReservedTimeSlots(selectedDate.value)
      } catch (e) {}
    }
    // 根据已选时间段查询座位占用状态
    let startTime = ''
    let endTime = ''
    if (selectedTimeSlot.value) {
      const slot = timeSlotOptions.find(s => s.value === selectedTimeSlot.value)
      if (slot) {
        [startTime, endTime] = slot.label.split(' - ')
      }
    }
    const list = await roomApi.getRoomSeats(roomId.value, selectedDate.value, startTime, endTime)
    // 按座位号数字排序，避免字符串排序导致 10 排在 2 前面
    list.sort((a: Seat, b: Seat) => {
      const numA = parseInt(String(a.seatNo).replace(/\D/g, '')) || 0
      const numB = parseInt(String(b.seatNo).replace(/\D/g, '')) || 0
      return numA - numB
    })
    seats.value = list
  } finally {
    loading.value = false
  }
}

const handleReserve = async () => {
  if (!selectedSeat.value || !selectedDate.value || !selectedTimeSlot.value) return
  const slot = timeSlotOptions.find(s => s.value === selectedTimeSlot.value)
  if (!slot) return
  const [startTime, endTime] = slot.label.split(' - ')

  submitting.value = true
  try {
    await reservationApi.createReservation({
      roomId: roomId.value,
      seatId: selectedSeat.value.seatId,
      slotId: Number(selectedTimeSlot.value),
      reserveDate: selectedDate.value,
      startTime,
      endTime
    })
    ElMessage.success('预约成功')
    router.push('/reservations')
  } catch (e: any) {
    ElMessage.error(e?.message || '预约失败')
  } finally {
    submitting.value = false
  }
}

watch(selectedDate, () => {
  if (selectedDate.value) loadData()
})

watch(selectedTimeSlot, () => {
  if (selectedDate.value) loadData()
})

onMounted(() => {
  const today = new Date()
  selectedDate.value = today.toISOString().split('T')[0]
  loadData()
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
.seat-map {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
  padding: 16px 0;
}
.seat {
  aspect-ratio: 1;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
  font-size: 14px;
}
.seat small {
  font-size: 12px;
  opacity: 0.8;
}
.seat .feature-tag {
  color: #e6a23c;
  font-weight: 600;
  opacity: 1;
  border: 1px solid #e6a23c;
  background: rgba(230, 162, 60, 0.1);
  border-radius: 4px;
  padding: 0 4px;
  margin-top: 2px;
}
.seat.available {
  background: #f0f9ff;
  color: #409eff;
}
.seat.available:hover {
  background: #409eff;
  color: #fff;
}
.seat.reserved {
  background: #fef0f0;
  color: #f56c6c;
  cursor: not-allowed;
}
.seat.in-use {
  background: #fdf6ec;
  color: #e6a23c;
  cursor: not-allowed;
}
.seat.maintaining {
  background: #f4f4f5;
  color: #909399;
  cursor: not-allowed;
}
.seat.selected {
  background: #67c23a;
  color: #fff;
  border-color: #4e8e2f;
}
.legend {
  display: flex;
  gap: 16px;
  margin-top: 16px;
  font-size: 14px;
}
.legend span {
  display: flex;
  align-items: center;
  gap: 6px;
}
.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}
.dot.available { background: #409eff; }
.dot.reserved { background: #f56c6c; }
.dot.in-use { background: #e6a23c; }
.dot.maintaining { background: #909399; }
</style>
