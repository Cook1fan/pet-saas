<template>
  <view class="cards-container">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">‹</view>
      <view class="nav-title">我的次卡</view>
      <view class="nav-right" @tap="navigateToPurchase">购买</view>
    </view>

    <!-- 次卡列表 -->
    <view class="cards-list" v-if="cardsList.length > 0">
      <view class="card-item" v-for="card in cardsList" :key="card.id" @tap="navigateToDetail(card.id)">
        <view class="card-left">
          <view class="card-title">{{ card.cardName }}</view>
          <view class="card-count">
            <text class="remain-count">{{ card.remainCount }}</text>
            <text class="total-count">/{{ card.totalCount }}次</text>
          </view>
          <view class="card-expire" v-if="card.expireDate">有效期至 {{ card.expireDate }}</view>
        </view>
        <view class="card-right">
          <view
            class="use-btn"
            :class="{ disabled: card.remainCount <= 0 }"
            @tap.stop="navigateToVerifyCode(card.id)"
          >
            {{ card.remainCount > 0 ? '使用' : '已用完' }}
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else>
      <text class="empty-icon">🎫</text>
      <text class="empty-text">暂无次卡</text>
      <text class="empty-tip">参与门店活动获取次卡吧</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onLoad, onShow } from 'vue'
import { getCardList } from '@/api/index'
import type { MemberCard } from '@/types'

const cardsList = ref<MemberCard[]>([])

onLoad(() => {})

onShow(() => {
  loadCardsList()
})

async function loadCardsList() {
  try {
    const res = await getCardList()
    if (res.code === 200 && res.data) {
      cardsList.value = res.data
    }
  } catch (error) {
    console.error('Load cards list failed:', error)
  }
}

function goBack() {
  uni.navigateBack()
}

function navigateToDetail(id: number) {
  uni.navigateTo({ url: `/pages/member/verify-code?cardId=${id}` })
}

function navigateToVerifyCode(id: number) {
  uni.navigateTo({ url: `/pages/member/verify-code?cardId=${id}` })
}

function navigateToPurchase() {
  uni.navigateTo({ url: '/pages/member/card-purchase' })
}
</script>

<style lang="scss" scoped>
.cards-container {
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
  flex: 1;
  text-align: center;
}

.nav-right {
  font-size: 14px;
  color: #ff4d4f;
  font-weight: 500;
}

.cards-list {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-item {
  background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-left {
  flex: 1;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 8px;
}

.card-count {
  margin-bottom: 8px;
}

.remain-count {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
}

.total-count {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.card-expire {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.card-right {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.use-btn {
  padding: 8px 24px;
  background: #fff;
  color: #ff4d4f;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;

  &.disabled {
    background: #f5f5f5;
    color: #909399;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 120px;
  gap: 12px;
}

.empty-icon {
  font-size: 60px;
}

.empty-text {
  font-size: 14px;
  color: #666;
}

.empty-tip {
  font-size: 12px;
  color: #999;
}
</style>
