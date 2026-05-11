<template>
  <view class="verify-code-container">
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">‹</view>
      <view class="nav-title">核销码</view>
    </view>

    <view class="code-section" v-if="currentCard">
      <view class="code-card">
        <view class="card-header">
          <view class="card-name">{{ currentCard.cardName }}</view>
          <view class="card-remain">{{ currentCard.remainCount }}/{{ currentCard.totalCount }}次</view>
        </view>
        <view class="code-display" v-if="verifyCodeInfo">
          <view class="code-text">{{ formatCode(verifyCodeInfo.verifyCode) }}</view>
          <view class="expire-countdown">
            <text class="countdown">{{ Math.ceil(verifyCodeInfo.remainSeconds / 60) }}</text>
            <text class="unit">分钟后失效</text>
          </view>
        </view>
        <view class="code-empty" v-else>
          <view class="refresh-btn" @tap="handleRefreshCode">
            生成核销码
          </view>
        </view>
      </view>
      <view class="expire-date" v-if="currentCard.expireDate">
        有效期至 {{ currentCard.expireDate }}
      </view>
    </view>

    <view class="tips-section">
      <view class="tips-title">使用说明</view>
      <view class="tips-list">
        <view class="tips-item">1. 向店员出示此核销码</view>
        <view class="tips-item">2. 每次核销扣除1次使用次数</view>
        <view class="tips-item">3. 核销码15分钟内有效</view>
      </view>
    </view>

    <view class="records-section">
      <view class="section-title">核销记录</view>
      <view class="records-list" v-if="recordList.length > 0">
        <view class="record-item" v-for="record in recordList" :key="record.id">
          <view class="record-left">
            <view class="record-type">核销</view>
            <view class="record-time">{{ record.createTime }}</view>
          </view>
          <view class="record-right">-{{ record.times }}次</view>
        </view>
      </view>
      <view class="empty-text" v-else>暂无核销记录</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getCardList, generateVerifyCode, getCardRecordList, type MemberCard, type CardRecord } from '@/api/index'
import { showToast, showLoading, hideLoading } from '@/utils/interaction'

const cardsList = ref<MemberCard[]>([])
const currentCard = ref<MemberCard | null>(null)
const currentCardId = ref<number | null>(null)
const verifyCodeInfo = ref<{ verifyCode: string; expireTime: string; remainSeconds: number } | null>(null)
const recordList = ref<CardRecord[]>([])
let countdownTimer: number | null = null

onLoad((options: any) => {
  const cardId = options.cardId ? parseInt(options.cardId) : undefined
  currentCardId.value = cardId || null
  loadCards(cardId)
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})

async function loadCards(selectCardId?: number) {
  try {
    const res = await getCardList()
    if (res.code === 200 && res.data) {
      cardsList.value = res.data
      if (selectCardId) {
        currentCard.value = res.data.find(c => c.id === selectCardId) || res.data[0] || null
      } else {
        currentCard.value = res.data[0] || null
      }
      if (currentCard.value) {
        loadRecords()
      }
    }
  } catch (error) {
    console.error('加载次卡列表失败:', error)
  }
}

async function loadRecords() {
  if (!currentCardId.value) return
  try {
    const res = await getCardRecordList({ type: 'verify', page: 1, pageSize: 20 })
    if (res.code === 200 && res.data) {
      recordList.value = res.data.list
    }
  } catch (error) {
    console.error('加载核销记录失败:', error)
  }
}

async function handleRefreshCode() {
  if (!currentCard.value || currentCard.value.remainCount <= 0) {
    showToast('次卡次数不足')
    return
  }

  showLoading()
  try {
    const res = await generateVerifyCode(currentCard.value.id)
    if (res.code === 200 && res.data) {
      verifyCodeInfo.value = res.data
      startCountdown()
    } else {
      showToast(res.message || '生成失败')
    }
  } catch (error) {
    console.error('生成核销码失败:', error)
    showToast('生成失败')
  } finally {
    hideLoading()
  }
}

function startCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
  countdownTimer = setInterval(() => {
    if (verifyCodeInfo.value) {
      verifyCodeInfo.value.remainSeconds -= 1
      if (verifyCodeInfo.value.remainSeconds <= 0) {
        verifyCodeInfo.value = null
        clearInterval(countdownTimer!)
      }
    }
  }, 1000)
}

function formatCode(code: string) {
  if (!code) return ''
  if (code.length >= 6) {
    return `${code.slice(0, 3)} ${code.slice(3, 6)}`
  }
  return code
}

function goBack() {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.verify-code-container {
  min-height: 100vh;
  background: #f5f5f5;
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
  margin-right: 16px;
}

.nav-title {
  font-size: 16px;
  font-weight: 600;
}

.code-section {
  padding: 20px 16px;
}

.code-card {
  background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
  border-radius: 16px;
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-name {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.card-remain {
  font-size: 16px;
  color: #fff;
}

.code-display {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
}

.code-text {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  letter-spacing: 8px;
  margin-bottom: 16px;
}

.expire-countdown {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;

  .countdown {
    font-size: 24px;
    font-weight: 600;
    color: #ff4d4f;
  }

  .unit {
    font-size: 14px;
    color: #909399;
  }
}

.code-empty {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  padding: 32px;
  text-align: center;
}

.refresh-btn {
  display: inline-block;
  padding: 12px 32px;
  background: #fff;
  color: #ff4d4f;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;
}

.expire-date {
  text-align: center;
  font-size: 13px;
  color: #909399;
  margin-top: 16px;
}

.tips-section {
  background: #fff;
  margin: 16px;
  padding: 16px;
  border-radius: 12px;
}

.tips-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tips-item {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.records-section {
  background: #fff;
  margin: 0 16px 20px;
  padding: 16px;
  border-radius: 12px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }
}

.record-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.record-type {
  font-size: 14px;
  color: #303133;
}

.record-time {
  font-size: 12px;
  color: #909399;
}

.record-right {
  font-size: 16px;
  color: #ff4d4f;
  font-weight: 600;
}

.empty-text {
  text-align: center;
  padding: 24px 0;
  font-size: 14px;
  color: #909399;
}
</style>
