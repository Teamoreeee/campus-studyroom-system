import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'
import { authApi } from '@/api/auth'

export interface UserInfo {
  userId: number
  username: string
  realName: string
  email: string
  phone?: string
  avatar?: string
  role: 'student' | 'admin' | 'super_admin'
  status: number
}

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))

  // 登录
  const login = async (username: string, password: string) => {
    try {
      const result = await authApi.login({ username, password })

      const { accessToken, refreshToken: rt, userInfo: info } = result

      token.value = accessToken
      refreshToken.value = rt
      userInfo.value = info

      // 保存token
      localStorage.setItem('token', accessToken)
      localStorage.setItem('refreshToken', rt)

      // 设置axios默认headers
      request.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`

      return true
    } catch (error) {
      console.error('Login failed:', error)
      return false
    }
  }

  // 登出
  const logout = () => {
    token.value = null
    refreshToken.value = null
    userInfo.value = null

    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    delete request.defaults.headers.common['Authorization']
  }

  // 检查是否已认证
  const isAuthenticated = () => {
    return !!token.value
  }

  // 检查是否是管理员
  const isAdmin = () => {
    return userInfo.value?.role === 'admin' || userInfo.value?.role === 'super_admin'
  }

  // 刷新token
  const refreshTokenAsync = async () => {
    if (!refreshToken.value) return false

    try {
      const result = await authApi.refreshToken({ refreshToken: refreshToken.value })

      const { accessToken, refreshToken: newRefreshToken } = result

      token.value = accessToken
      refreshToken.value = newRefreshToken

      localStorage.setItem('token', accessToken)
      localStorage.setItem('refreshToken', newRefreshToken)

      request.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`

      return true
    } catch (error) {
      console.error('Token refresh failed:', error)
      logout()
      return false
    }
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    try {
      const info = await authApi.getUserInfo()
      userInfo.value = info as UserInfo
      return true
    } catch (error) {
      console.error('Fetch user info failed:', error)
      return false
    }
  }

  return {
    userInfo,
    token,
    refreshToken,
    login,
    logout,
    isAuthenticated,
    isAdmin,
    refreshTokenAsync,
    fetchUserInfo
  }
})
