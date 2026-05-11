<template>
  <view class="verify-code-container">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">‹</view>
      <view class="nav-title">核销码</view>
    </view>

    <!-- 选择次卡 -->
    <view class="select-card-section" v-if="cardsList.length > 1">
      <picker :range="cardsList" range-key="cardName" @change="onCardChange">
        <view class="select-card-box">
          <text>{{ currentCard?.cardName || '选择次卡' }}</text>
          <text>›</text>
        </view>
      </picker>
    </view>

    <!-- 核销码区域 -->
    <view class="code-section" v-if="currentCard">
      <view class="code-card">
        <view class="card-header">
          <text class="card-name">{{ currentCard.cardName }}</text>
          <text class="card-remain">{{ currentCard.remainCount }}/{{ currentCard.totalCount }}次</text>
        </view>
        <view class="qr-code-wrapper">
          <image class="qr-code" :src="currentCard.qrCodeUrl || '/static/qr-placeholder.png'" mode="aspectFit" />
        </view>
        <view class="code-text">{{ currentCard.verifyCode || '8888 8888 8888' }}</view>
        <view class="refresh-tip">二维码会自动更新，请勿截图</view>
      </view>
      <view class="expire-text" v-if="currentCard.expireDate">
        有效期至 {{ currentCard.expireDate }}
      </view>
    </view>

    <!-- 使用说明 -->
    <view class="tips-section">
      <view class="tips-title">使用说明</view>
      <view class="tips-list">
        <view class="tips-item">1. 向店员出示此二维码进行核销</view>
        <view class="tips-item">2. 每次核销扣除一次使用次数</view>
        <view class="tips-item">3. 仅限当前门店使用</view>
      </view>
    </view>

    <!-- 使用记录 -->
    <view class="records-section">
      <view class="section-title">使用记录</view>
      <view class="records-list">
        <view class="record-item">
          <view class="record-left">
            <view class="record-service">宠物洗澡</view>
            <view class="record-time">2024-05-10 14:30</view>
          </view>
          <view class="record-right">-1次</view>
        </view>
        <view class="record-item">
          <view class="record-left">
            <view class="record-service">宠物洗澡</view>
            <view class="record-time">2024-04-25 10:15</view>
          </view>
          <view class="record-right">-1次</view>
        </view>
      </view>
      <view class="empty-text" v-if="false">暂无使用记录</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onLoad, onMounted, onUnmounted } from 'vue'
import { getCardList, getCardDetail, refreshVerifyCode } from '@/api/index'
import type { MemberCard } from '@/types'

const cardsList = ref<MemberCard[]>([])
const currentCard = ref<MemberCard | null>(null)
let refreshTimer: number | null = null

onLoad((options) => {
  const cardId = options.cardId ? parseInt(options.cardId) : undefined
  loadCards(cardId)
})

onMounted(() => {
  refreshTimer = setInterval(() => {
    if (currentCard.value) {
      handleRefreshCode()
    }
  }, 5 * 60 * 1000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

async function loadCards(selectCardId?: number) {
  try {
    const res = await getCardList()
    if (res.code === 200 && res.data) {
      cardsList.value = res.data
      if (selectCardId) {
        currentCard.value = res.data.find(c => c.id === selectCardId) || res.data[0]
      } else {
        currentCard.value = res.data[0]
      }
    }
  } catch (error) {
    console.error('Load cards list failed:', error)
  }
}

function onCardChange(e: any) {
  currentCard.value = cardsList.value[e.detail.value]
}

async function handleRefreshCode() {
  if (!currentCard.value) return
  try {
    const res = await refreshVerifyCode(currentCard.value.id)
    if (res.code === 200 && res.data) {
      currentCard.value = res.data
    }
  } catch (error) {
    console.error('Refresh verify code failed:', error)
  }
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

.select-card-section {
  background: #fff;
  padding: 16px;
}

.select-card-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f5f5;
  border-radius: 8px;
  font-size: 14px;
  color: #333;
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
  margin-bottom: 20px;
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

.qr-code-wrapper {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.qr-code {
  width: 200px;
  height: 200px;
}

.code-text {
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 8px;
  letter-spacing: 2px;
}

.refresh-tip {
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.expire-text {
  text-align: center;
  font-size: 13px;
  color: #999;
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
  color: #333;
  margin-bottom: 12px;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tips-item {
  font-size: 13px;
  color: #666;
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
  color: #333;
  margin-bottom: 16px;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.record-item:last-child {
  border-bottom: none;
}

.record-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.record-service {
  font-size: 14px;
  color: #333;
}

.record-time {
  font-size: 12px;
  color: #999;
}

.record-right {
  font-size: 14px;
  color: #ff4d4f;
  font-weight: 600;
}

.empty-text {
  text-align: center;
  padding: 24px 0;
  font-size: 14px;
  color: #999;
}
</style>
