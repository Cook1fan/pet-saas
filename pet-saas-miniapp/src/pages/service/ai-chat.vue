<template>
  <view class="chat-container">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">‹</view>
      <view class="nav-content">
        <view class="nav-title">智能客服</view>
        <view class="nav-subtitle">AI 在线为您服务</view>
      </view>
      <view class="nav-service" @tap="callService">📞</view>
    </view>

    <!-- 消息列表 -->
    <scroll-view
      class="messages-list"
      scroll-y
      :scroll-into-view="scrollIntoView"
      :scroll-with-animation="true"
    >
      <view class="message-item" v-for="msg in messages" :key="msg.id" :id="'msg-' + msg.id">
        <!-- AI 消息 -->
        <view class="message-ai" v-if="msg.type === 'ai'">
          <view class="message-avatar">🤖</view>
          <view class="message-bubble">
            <text class="message-text">{{ msg.content }}</text>
          </view>
        </view>
        <!-- 用户消息 -->
        <view class="message-user" v-else>
          <view class="message-bubble">
            <text class="message-text">{{ msg.content }}</text>
          </view>
          <view class="message-avatar">
            <image :src="userAvatar || '/static/default-avatar.png'" mode="aspectFill" />
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 快捷问题 -->
    <view class="quick-questions" v-if="messages.length <= 3">
      <scroll-view class="quick-scroll" scroll-x :show-scrollbar="false">
        <view class="quick-list">
          <view class="quick-item" v-for="q in quickQuestions" :key="q" @tap="sendQuickQuestion(q)">
            {{ q }}
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 底部输入区域 -->
    <view class="input-section">
      <view class="input-wrapper">
        <input
          class="message-input"
          v-model="inputText"
          placeholder="请输入您的问题..."
          @confirm="handleSend"
          confirm-type="send"
        />
      </view>
      <view class="send-btn" :class="{ disabled: !inputText.trim() || loading }" @tap="handleSend">
        {{ loading ? '...' : '发送' }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onLoad } from 'vue'
import { sendChatMessage, getChatHistory } from '@/api/index'
import type { ChatMessage } from '@/types'

const messages = ref<ChatMessage[]>([
  {
    id: 1,
    type: 'ai',
    content: '您好！我是宠物门店的智能客服，很高兴为您服务。您可以询问关于服务、活动、预约等问题。',
    timestamp: Date.now()
  }
])
const inputText = ref('')
const loading = ref(false)
const userAvatar = ref('')
const scrollIntoView = ref('')

const quickQuestions = [
  '你们营业时间是？',
  '洗澡多少钱？',
  '有什么活动？',
  '如何预约？',
  '需要带什么？'
]

onLoad(() => {
  loadChatHistory()
})

async function loadChatHistory() {
  try {
    const res = await getChatHistory()
    if (res.code === 200 && res.data && res.data.length > 0) {
      messages.value = res.data
    }
  } catch (error) {
    console.error('Load chat history failed:', error)
  }
}

function sendQuickQuestion(question: string) {
  inputText.value = question
  handleSend()
}

async function handleSend() {
  if (!inputText.value.trim() || loading.value) {
    return
  }

  const content = inputText.value.trim()
  inputText.value = ''

  // 添加用户消息
  const userMsgId = Date.now()
  messages.value.push({
    id: userMsgId,
    type: 'user',
    content,
    timestamp: userMsgId
  })

  scrollToBottom(userMsgId)

  loading.value = true
  try {
    const res = await sendChatMessage(content)
    if (res.code === 200 && res.data) {
      const aiMsgId = Date.now() + 1
      messages.value.push({
        id: aiMsgId,
        type: 'ai',
        content: res.data.reply,
        timestamp: aiMsgId
      })
      scrollToBottom(aiMsgId)

      if (res.data.isTransferManual) {
        setTimeout(() => {
          const transferMsgId = Date.now() + 2
          messages.value.push({
            id: transferMsgId,
            type: 'ai',
            content: '正在为您转接人工客服，请稍等...',
            timestamp: transferMsgId
          })
          scrollToBottom(transferMsgId)
        }, 500)
      }
    }
  } catch (error) {
    console.error('Send message failed:', error)
    uni.showToast({ title: '发送失败，请重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function scrollToBottom(msgId: number) {
  setTimeout(() => {
    scrollIntoView.value = 'msg-' + msgId
  }, 50)
}

function goBack() {
  uni.navigateBack()
}

function callService() {
  uni.showModal({
    title: '联系客服',
    content: '是否拨打电话：138-xxxx-xxxx',
    success: (res) => {
      if (res.confirm) {
        uni.makePhoneCall({ phoneNumber: '13800000000' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.chat-container {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
}

.nav-back {
  font-size: 24px;
  font-weight: 600;
  width: 30px;
}

.nav-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.nav-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.nav-subtitle {
  font-size: 12px;
  color: #999;
}

.nav-service {
  font-size: 24px;
  width: 30px;
  text-align: right;
}

.messages-list {
  flex: 1;
  padding: 16px;
}

.message-item {
  margin-bottom: 16px;
}

.message-ai,
.message-user {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.message-user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;

  image {
    width: 100%;
    height: 100%;
    border-radius: 50%;
  }
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
}

.message-ai .message-bubble {
  background: #fff;
  color: #333;
  border-top-left-radius: 4px;
}

.message-user .message-bubble {
  background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
  color: #fff;
  border-top-right-radius: 4px;
}

.message-text {
  word-wrap: break-word;
}

.quick-questions {
  background: #fff;
  padding: 12px 16px;
}

.quick-scroll {
  white-space: nowrap;
}

.quick-list {
  display: flex;
  gap: 12px;
}

.quick-item {
  padding: 8px 16px;
  background: #f5f5f5;
  border-radius: 16px;
  font-size: 13px;
  color: #333;
  flex-shrink: 0;
}

.input-section {
  background: #fff;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: calc(12px + constant(safe-area-inset-bottom));
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
}

.input-wrapper {
  flex: 1;
}

.message-input {
  width: 100%;
  height: 40px;
  padding: 0 16px;
  background: #f5f5f5;
  border-radius: 20px;
  font-size: 14px;
}

.send-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
  color: #fff;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;

  &.disabled {
    background: #ddd;
  }
}
</style>
