import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import router from '@/router'

interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface TypedAxiosInstance extends AxiosInstance {
  get<T = any>(url: string, config?: any): Promise<T>
  post<T = any>(url: string, data?: any, config?: any): Promise<T>
  put<T = any>(url: string, data?: any, config?: any): Promise<T>
  delete<T = any>(url: string, config?: any): Promise<T>
}

// 创建axios实例
const instance = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
instance.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data as ApiResponse

    // 成功响应
    if (code === 200) {
      return data
    }

    // 业务错误
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  async (error) => {
    const userStore = useUserStore()

    if (error.response) {
      const { status } = error.response

      switch (status) {
        case 401:
          // Token过期，尝试刷新
          if (userStore.refreshToken) {
            try {
              const success = await userStore.refreshTokenAsync()
              if (success) {
                // 重新发送原始请求
                const config = error.config
                return instance(config)
              }
            } catch (refreshError) {
              // 刷新失败，跳转登录页
              userStore.logout()
              router.push('/login')
              ElMessage.error('登录已过期，请重新登录')
            }
          } else {
            userStore.logout()
            router.push('/login')
            ElMessage.error('请先登录')
          }
          break

        case 403:
          ElMessage.error('没有权限访问')
          break

        case 404:
          ElMessage.error('请求的资源不存在')
          break

        case 500:
          ElMessage.error('服务器内部错误')
          break

        default:
          ElMessage.error(error.response.data.message || '请求失败')
      }
    } else if (error.request) {
      // 请求已发送但无响应
      ElMessage.error('网络连接异常，请检查网络')
    } else {
      // 请求配置错误
      ElMessage.error('请求配置错误')
    }

    return Promise.reject(error)
  }
)

const request = instance as TypedAxiosInstance

export default request
