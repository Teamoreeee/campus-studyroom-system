import request from '@/utils/request'
import type { PageResult, StudyRoom, User } from '@/types'

export interface RoomManageParams {
  roomName: string
  building: string
  floor: number
  capacity: number
  seatCount: number
  facilities: string
  openTime: string
  closeTime: string
  status: number
}

export interface UserQuery {
  page?: number
  size?: number
  keyword?: string
  role?: string
  status?: number
}

export const adminApi = {
  // 自习室管理
  getAllRooms: (params: { page?: number; size?: number; keyword?: string }) => {
    return request.get<PageResult<StudyRoom>>('/room/admin/rooms', { params })
  },

  createRoom: (params: RoomManageParams) => {
    return request.post<StudyRoom>('/room/admin/rooms', params)
  },

  updateRoom: (id: number, params: RoomManageParams) => {
    return request.put<StudyRoom>(`/room/admin/rooms/${id}`, params)
  },

  deleteRoom: (id: number) => {
    return request.delete(`/room/admin/rooms/${id}`)
  },

  // 用户管理
  getAllUsers: (params: UserQuery) => {
    return request.get<PageResult<User>>('/auth/admin/users', { params })
  },

  updateUserStatus: (id: number, status: number) => {
    return request.put(`/auth/admin/users/${id}/status`, { status })
  },

  // 统计
  getStatistics: () => {
    return request.get<{
      totalUsers: number
      totalRooms: number
      totalReservations: number
      todayReservations: number
      todayCheckIn: number
      violationCount: number
    }>('/reservation/admin/statistics')
  },

  getReservationTrend: (days: number = 7) => {
    return request.get<{ date: string; count: number }[]>('/reservation/admin/trend', { params: { days } })
  }
}
