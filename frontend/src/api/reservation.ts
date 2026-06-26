import request from '@/utils/request'
import type { PageResult, Reservation } from '@/types'

export interface ReservationParams {
  roomId: number
  seatId: number
  slotId: number
  reserveDate: string
  startTime: string
  endTime: string
}

export interface ReservationQuery {
  page?: number
  size?: number
  status?: string
}

export const reservationApi = {
  // 创建预约
  createReservation: (params: ReservationParams) => {
    return request.post<Reservation>('/reservation/reservations', params)
  },

  // 获取我的预约列表
  getMyReservations: (params: ReservationQuery) => {
    return request.get<PageResult<Reservation>>('/reservation/reservations/my', { params })
  },

  // 取消预约
  cancelReservation: (id: number) => {
    return request.put(`/reservation/reservations/${id}/cancel`)
  },

  // 签到
  checkIn: (id: number) => {
    return request.put(`/reservation/reservations/${id}/check-in`)
  },

  // 查询当前用户指定日期已预约的时间段
  getMyReservedTimeSlots: (date: string) => {
    return request.get<string[]>('/reservation/reservations/my/time-slots', { params: { date } })
  },

  // 清空已过期/已取消的预约
  clearExpiredReservations: () => {
    return request.delete<number>('/reservation/reservations/clear-expired')
  }
}
