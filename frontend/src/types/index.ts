// 用户相关类型
export interface User {
  userId: number
  username: string
  realName: string
  email: string
  phone?: string
  avatar?: string
  role: 'student' | 'admin' | 'super_admin'
  status: number
  createTime: string
}

// 自习室相关类型
export interface StudyRoom {
  roomId: number
  roomName: string
  building: string
  floor: number
  capacity: number
  facilities: string
  openTime: string
  closeTime: string
  status: number
  seatCount: number
  availableSeats: number
}

// 座位相关类型
export interface Seat {
  seatId: number
  roomId: number
  seatNo: string
  seatType: 'NORMAL' | 'WINDOW' | 'CORNER' | 'DISABLED'
  position: string
  hasPower: boolean
  status: 'AVAILABLE' | 'RESERVED' | 'IN_USE' | 'MAINTAINING'
}

// 预约相关类型
export interface Reservation {
  reservationId: number
  userId: number
  roomId: number
  roomName?: string
  seatId: number
  seatNo?: string
  slotId: number
  reserveDate: string
  startTime: string
  endTime: string
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'CHECKED_IN' | 'COMPLETED' | 'EXPIRED' | 'VIOLATED'
  createTime: string
  checkInTime?: string
  checkOutTime?: string
}

// 考勤相关类型
export interface Attendance {
  attendanceId: number
  reservationId: number
  userId: number
  checkInTime: string
  checkOutTime?: string
  duration: number
  status: 'NORMAL' | 'LATE' | 'EARLY_LEAVE' | 'ABSENT'
}

// 违规相关类型
export interface Violation {
  violationId: number
  userId: number
  reservationId: number
  type: 'NO_SHOW' | 'LATE_CHECK_IN' | 'EARLY_LEAVE' | 'DAMAGE'
  description: string
  penaltyDays: number
  createTime: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
}

// AI推荐相关类型
export interface AIRecommendation {
  recommendationId: number
  userId: number
  roomId: number
  seatId: number
  score: number
  reason: string
  strategy: string
  createTime: string
}

// API响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页请求参数
export interface PageParams {
  page: number
  size: number
  keyword?: string
}

// 分页响应数据
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
  pages: number
}