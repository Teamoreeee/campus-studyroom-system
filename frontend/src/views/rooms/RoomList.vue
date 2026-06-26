<template>
  <div class="page">
    <NavBar />
    <div class="container">
      <h2>自习室列表</h2>

      <el-card class="filter-card">
        <el-form :inline="true" :model="query">
          <el-form-item label="关键词">
            <el-input v-model="query.keyword" placeholder="自习室名称/教学楼" clearable />
          </el-form-item>
          <el-form-item label="教学楼">
            <el-select v-model="query.building" placeholder="全部" clearable style="width: 160px">
              <el-option v-for="b in buildings" :key="b" :label="b" :value="b" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadRooms">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-row :gutter="16" class="room-list">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="room in roomList" :key="room.roomId">
          <el-card class="room-card" shadow="hover" @click="goDetail(room.roomId)">
            <div class="room-header">
              <h3>{{ room.roomName }}</h3>
              <el-tag :type="room.status === 1 ? 'success' : 'info'" size="small">
                {{ room.status === 1 ? '开放' : '关闭' }}
              </el-tag>
            </div>
            <div class="room-info">
              <p><el-icon><OfficeBuilding /></el-icon> {{ room.building }} {{ room.floor }}楼</p>
              <p><el-icon><User /></el-icon> 容量：{{ room.capacity }}人</p>
              <p><el-icon><Clock /></el-icon> {{ room.openTime }} - {{ room.closeTime }}</p>
              <p><el-icon><SetUp /></el-icon>
                <span v-if="getFacilityTags(room.facilities).length" class="facility-tags">
                  <el-tag v-for="tag in getFacilityTags(room.facilities)" :key="tag" size="small" type="info" style="margin-right: 6px; margin-bottom: 4px;">{{ tag }}</el-tag>
                </span>
                <span v-else>无特殊设施</span>
              </p>
            </div>
            <div class="seat-status">
              <span class="available">{{ room.availableSeats }}</span> / {{ room.seatCount }} 座位可约
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div class="pagination">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[12, 24, 36]"
          layout="total, sizes, prev, pager, next"
          @change="loadRooms"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import { roomApi } from '@/api/room'
import type { StudyRoom } from '@/types'

const router = useRouter()
const roomList = ref<StudyRoom[]>([])
const total = ref(0)
const buildings = ref<string[]>([])

const query = reactive({
  page: 1,
  size: 12,
  keyword: '',
  building: ''
})

const loadRooms = async () => {
  try {
    const res = await roomApi.getRoomList({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      building: query.building || undefined
    })
    roomList.value = res.list
    total.value = res.total
  } catch (e) {
    ElMessage.error('加载自习室失败')
  }
}

const loadBuildings = async () => {
  try {
    buildings.value = await roomApi.getBuildings()
  } catch (e) {
    // ignore
  }
}

const resetQuery = () => {
  query.page = 1
  query.keyword = ''
  query.building = ''
  loadRooms()
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

const goDetail = (id: number) => {
  router.push(`/rooms/${id}`)
}

onMounted(() => {
  loadRooms()
  loadBuildings()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
}
.container {
  max-width: 1400px;
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
.room-list {
  margin-bottom: 24px;
}
.room-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: transform 0.2s;
}
.room-card:hover {
  transform: translateY(-4px);
}
.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.room-header h3 {
  margin: 0;
  color: #303133;
}
.room-info p {
  margin: 6px 0;
  color: #606266;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.seat-status {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  font-size: 14px;
  color: #606266;
}
.available {
  color: #67c23a;
  font-size: 24px;
  font-weight: 600;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
}
</style>
