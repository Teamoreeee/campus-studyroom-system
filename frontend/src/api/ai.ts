import request from '@/utils/request'
import type { AIRecommendation } from '@/types'

export interface RecommendParams {
  date?: string
  building?: string
  preferWindow?: boolean
  preferPower?: boolean
}

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  timestamp?: string
  relatedDocs?: string[]
}

export const aiApi = {
  // 获取智能推荐
  getRecommendations: (params: RecommendParams) => {
    return request.post<AIRecommendation[]>('/ai/recommendations', params)
  },

  // AI 客服对话
  chat: (message: string, history?: ChatMessage[]) => {
    return request.post<{ reply: string; relatedDocs?: string[] }>('/ai/chat', {
      message,
      history
    })
  },

  // 获取推荐原因解释
  explainRecommendation: (recommendationId: number) => {
    return request.get<{ explanation: string }>(`/ai/recommendations/${recommendationId}/explain`)
  }
}
