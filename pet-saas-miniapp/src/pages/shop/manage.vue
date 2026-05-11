<template>
  <view class="shop-manage-container">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">‹</view>
      <view class="nav-title">我的门店</view>
    </view>

    <!-- 当前门店 -->
    <view class="section" v-if="currentShop">
      <view class="section-header">
        <text class="section-title">当前门店</text>
      </view>
      <view class="current-shop-card">
        <image class="shop-avatar" :src="currentShop.shopLogo || '/static/default-shop.png'" mode="aspectFill" />
        <view class="shop-details">
          <view class="shop-name">{{ currentShop.shopName }}</view>
          <view class="shop-address" v-if="currentShop.address">📍 {{ currentShop.address }}</view>
        </view>
        <view class="shop-status">使用中</view>
      </view>
    </view>

    <!-- 已绑定门店列表 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">已绑定门店</text>
      </view>
      <view class="shop-list">
        <view
          class="shop-item"
          :class="{ active: shop.tenantId === currentShop?.tenantId }"
          v-for="shop in shopList"
          :key="shop.tenantId"
          @tap="handleSwitchShop(shop)"
        >
          <image class="shop-avatar" :src="shop.shopLogo || '/static/default-shop.png'" mode="aspectFill" />
          <view class="shop-details">
            <view class="shop-name">{{ shop.shopName }}</view>
            <view class="shop-address" v-if="shop.address">📍 {{ shop.address }}</view>
          </view>
          <view class="shop-action" v-if="shop.tenantId === currentShop?.tenantId">
            当前使用
          </view>
          <view class="shop-action" v-else>
            切换
          </view>
        </view>
      </view>
      <view class="empty-text" v-if="shopList.length === 0">
        暂无绑定门店
      </view>
    </view>

    <!-- 绑定新门店 -->
    <view class="section">
      <view class="add-shop-btn" @tap="navigateToSelect">
        <text class="add-icon">➕</text>
        <text>绑定新门店</text>
      </view>
    </view>

    <!-- 门店信息说明 -->
    <view class="tips-section">
      <text class="tips-text">提示：您可以同时绑定多家门店，随时切换查看不同门店的活动和服务</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onLoad, onShow } from 'vue'
import { useMemberStore } from '@/stores/member'
import { getMyShops, switchShop } from '@/api/index'
import type { ShopInfo } from '@/types'

const memberStore = useMemberStore()

const currentShop = ref<ShopInfo | null>(null)
const shopList = ref<ShopInfo[]>([])

onLoad(() => {
  currentShop.value = memberStore.currentShop
})

onShow(() => {
  loadShopList()
})

async function loadShopList() {
  try {
    const res = await getMyShops()
    if (res.code === 200 && res.data) {
      shopList.value = res.data
    }
  } catch (error) {
    console.error('Load shop list failed:', error)
  }
}

async function handleSwitchShop(shop: ShopInfo) {
  if (shop.tenantId === currentShop.value?.tenantId) {
    return
  }

  try {
    uni.showLoading({ title: '切换中...' })
    await switchShop({ tenantId: shop.tenantId })
    memberStore.setCurrentShop(shop)
    currentShop.value = shop
    uni.hideLoading()
    uni.showToast({ title: '切换成功', icon: 'success' })
  } catch (error) {
    uni.hideLoading()
    console.error('Switch shop failed:', error)
    uni.showToast({ title: '切换失败', icon: 'none' })
  }
}

function goBack() {
  uni.navigateBack()
}

function navigateToSelect() {
  uni.navigateTo({ url: '/pages/shop/select' })
}
</script>

<style lang="scss" scoped>
.shop-manage-container {
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

.section {
  background: #fff;
  margin: 16px;
  padding: 16px;
  border-radius: 12px;
}

.section-header {
  margin-bottom: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.current-shop-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fff0f0;
  border-radius: 8px;
}

.shop-avatar {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  background: #f0f0f0;
  flex-shrink: 0;
}

.shop-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.shop-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.shop-address {
  font-size: 13px;
  color: #999;
}

.shop-status {
  padding: 4px 12px;
  background: #ff4d4f;
  color: #fff;
  font-size: 12px;
  border-radius: 12px;
}

.shop-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.shop-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid transparent;

  &.active {
    border-color: #ff4d4f;
    background: #fff0f0;
  }
}

.shop-action {
  padding: 6px 16px;
  background: #f0f0f0;
  border-radius: 16px;
  font-size: 12px;
  color: #666;
  flex-shrink: 0;

  &.active {
    background: #ff4d4f;
    color: #fff;
  }
}

.add-shop-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  border: 1px dashed #e8e8e8;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
}

.add-icon {
  font-size: 20px;
}

.empty-text {
  text-align: center;
  padding: 24px 0;
  font-size: 14px;
  color: #999;
}

.tips-section {
  padding: 20px 32px;
}

.tips-text {
  font-size: 12px;
  color: #999;
  line-height: 1.6;
}
</style>
