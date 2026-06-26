<template>
  <div class="page">
    <NavBar />
    <div class="container">
      <h2>AI 智能客服</h2>

      <el-card class="chat-card">
        <div class="messages" ref="messageBox">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message"
            :class="msg.role"
          >
            <div class="avatar">
              <el-avatar :icon="msg.role === 'user' ? 'UserFilled' : 'Service'" :style="{ background: msg.role === 'user' ? '#409eff' : '#67c23a' }" />
            </div>
            <div class="bubble">
              <p v-html="renderMarkdown(msg.content)"></p>
              <div v-if="msg.relatedDocs?.length" class="related-docs">
                <small>相关文档：</small>
                <ul>
                  <li v-for="doc in msg.relatedDocs" :key="doc">{{ doc }}</li>
                </ul>
              </div>
            </div>
          </div>

          <!-- AI 思考中 -->
          <div v-if="sending" class="message assistant">
            <div class="avatar">
              <el-avatar icon="Service" :style="{ background: '#67c23a' }" />
            </div>
            <div class="bubble loading-bubble">
              <div class="loading-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
              <p class="loading-text">{{ loadingText }}</p>
            </div>
          </div>
        </div>

        <div class="input-area">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="2"
            placeholder="请输入您的问题，例如：如何预约自习室？"
            @keydown.enter.prevent="sendMessage"
          />
          <el-button type="primary" :loading="sending" @click="sendMessage" style="margin-top: 12px">发送</el-button>
        </div>
      </el-card>

      <el-card class="faq-card" title="常见问题">
        <el-tag
          v-for="q in faqs"
          :key="q"
          class="faq-tag"
          @click="inputMessage = q; sendMessage()"
        >{{ q }}</el-tag>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import { aiApi } from '@/api/ai'
import type { ChatMessage } from '@/api/ai'

const inputMessage = ref('')
const sending = ref(false)
const messageBox = ref<HTMLDivElement>()
const loadingText = ref('')
const loadingTexts = [
  'AI 小助手正在绞尽脑汁想答案...',
  '稍等片刻，让我翻翻自习室小本本 📚',
  '脑子转得快冒烟啦，请稍等 💨',
  '让我想想怎么回答最贴心 💡',
  '正在为您的学习之旅出谋划策 🎯'
]

const messages = ref<ChatMessage[]>([
  {
    role: 'assistant',
    content: '您好！我是校园自习室智能客服，可以帮您解答预约规则、签到签退、违规处理等问题。请问有什么可以帮您？',
    timestamp: new Date().toISOString()
  }
])

const faqs = [
  '如何预约自习室？',
  '预约后如何签到？',
  '迟到会有什么后果？',
  '可以取消预约吗？',
  '自习室开放时间是？'
]

const renderMarkdown = (content: string) => {
  // 简单处理换行
  return content.replace(/\n/g, '<br>')
}

const scrollToBottom = async () => {
  await nextTick()
  if (messageBox.value) {
    messageBox.value.scrollTop = messageBox.value.scrollHeight
  }
}

const sendMessage = async () => {
  const text = inputMessage.value.trim()
  if (!text) return

  messages.value.push({
    role: 'user',
    content: text,
    timestamp: new Date().toISOString()
  })
  inputMessage.value = ''
  sending.value = true
  loadingText.value = loadingTexts[Math.floor(Math.random() * loadingTexts.length)]
  await scrollToBottom()

  try {
    const res = await aiApi.chat(text, messages.value)
    messages.value.push({
      role: 'assistant',
      content: res.reply,
      timestamp: new Date().toISOString()
    })
  } catch (e) {
    ElMessage.error('客服响应失败')
    messages.value.push({
      role: 'assistant',
      content: '抱歉，当前服务繁忙，请稍后再试。',
      timestamp: new Date().toISOString()
    })
  } finally {
    sending.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
}
.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}
h2 {
  margin-bottom: 20px;
  color: #303133;
}
.chat-card {
  margin-bottom: 20px;
}
.messages {
  height: 500px;
  overflow-y: auto;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}
.message {
  display: flex;
  margin-bottom: 16px;
  gap: 12px;
}
.message.user {
  flex-direction: row-reverse;
}
.bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.message.user .bubble {
  background: #409eff;
  color: #fff;
}
.bubble p {
  margin: 0;
  line-height: 1.6;
}
.input-area {
  margin-top: 16px;
}
.faq-card {
  padding: 16px;
}
.faq-tag {
  margin: 6px;
  cursor: pointer;
}
.related-docs {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
}
.related-docs ul {
  margin: 4px 0 0;
  padding-left: 16px;
}

/* AI 加载动画 */
.loading-bubble {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 180px;
}
.loading-dots {
  display: flex;
  gap: 6px;
  align-items: center;
}
.loading-dots span {
  width: 8px;
  height: 8px;
  background: #67c23a;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}
.loading-dots span:nth-child(1) {
  animation-delay: -0.32s;
}
.loading-dots span:nth-child(2) {
  animation-delay: -0.16s;
}
.loading-dots span:nth-child(3) {
  animation-delay: 0s;
}
.loading-text {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
