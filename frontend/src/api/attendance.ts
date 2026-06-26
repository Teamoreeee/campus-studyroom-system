import request from '@/utils/request'
import type { PageResult, Attendance } from '@/types'

export interface AttendanceQuery {
  page?: number
  size?: number
  status?: string
}

export const attendanceApi = {
  // 签到
  checkIn: (params: { reservationId?: number; qrCode?: string }) => {
    return request.post<Attendance>('/attendance/check-in', params)
  },

  // 签退
  checkOut: (reservationId: number) => {
    return request.post<Attendance>('/attendance/check-out', { reservationId })
  },

  // 获取考勤记录
  getMyAttendance: (params: AttendanceQuery) => {
    return request.get<PageResult<Attendance>>('/attendance/my', { params })
  },

  // 获取今日考勤统计
  getTodayStats: () => {
    return request.get<{
      totalReservations: number
      checkedIn: number
      notCheckedIn: number
      lateCount: number
    }>('/attendance/today-stats')
  }
}
