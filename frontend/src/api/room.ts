import request from '@/utils/request'
import type { PageResult, StudyRoom, Seat } from '@/types'

export interface RoomQuery {
  page?: number
  size?: number
  keyword?: string
  building?: string
  floor?: number
  status?: number
}

export const roomApi = {
  // 获取自习室列表
  getRoomList: (params: RoomQuery) => {
    return request.get<PageResult<StudyRoom>>('/room/rooms', { params })
  },

  // 获取自习室详情
  getRoomDetail: (id: number) => {
    return request.get<StudyRoom>(`/room/rooms/${id}`)
  },

  // 获取自习室座位
  getRoomSeats: (roomId: number, date?: string, startTime?: string, endTime?: string) => {
    return request.get<Seat[]>(`/room/rooms/${roomId}/seats`, { params: { date, startTime, endTime } })
  },

  // 获取所有教学楼
  getBuildings: () => {
    return request.get<string[]>('/room/buildings')
  }
}
