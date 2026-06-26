import request from '@/utils/request'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  accessToken: string
  refreshToken: string
  userInfo: {
    userId: number
    username: string
    realName: string
    email: string
    phone?: string
    avatar?: string
    role: 'student' | 'admin' | 'super_admin'
    status: number
  }
}

export const authApi = {
  // 登录
  login: (params: LoginParams) => {
    return request.post<LoginResult>('/auth/login', params)
  },

  // 注册
  register: (params: {
    username: string
    password: string
    realName: string
    email: string
    studentNo: string
  }) => {
    return request.post('/auth/register', params)
  },

  // 刷新token
  refreshToken: (params: { refreshToken: string }) => {
    return request.post('/auth/refresh', params)
  },

  // 获取用户信息
  getUserInfo: () => {
    return request.get('/auth/profile')
  },

  // 修改密码
  changePassword: (params: {
    oldPassword: string
    newPassword: string
  }) => {
    return request.put('/auth/password', params)
  }
}