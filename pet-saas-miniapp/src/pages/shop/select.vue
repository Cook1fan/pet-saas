<template>
  <view class="select-shop-container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input-wrapper">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="searchKeyword"
          placeholder="请输入门店名称搜索"
          @confirm="handleSearch"
        />
      </view>
    </view>

    <!-- 附近的门店 -->
    <view class="section" v-if="nearbyShops.length > 0">
      <view class="section-title">📍 附近的门店</view>
      <view class="shop-list">
        <view class="shop-item" v-for="shop in nearbyShops" :key="shop.tenantId" @tap="handleSelectShop(shop)">
          <image class="shop-logo" :src="shop.shopLogo || defaultLogo" mode="aspectFill" />
          <view class="shop-info">
            <view class="shop-name">{{ shop.shopName }}</view>
            <view class="shop-distance" v-if="shop.distance">
              距您 {{ formatDistance(shop.distance) }}
            </view>
            <view class="shop-address" v-if="shop.address">
              🏠 {{ shop.address }}
            </view>
          </view>
          <view class="select-arrow">›</view>
        </view>
      </view>
    </view>

    <!-- 搜索结果 -->
    <view class="section" v-if="searchKeyword && searchResults.length > 0">
      <view class="section-title">🔍 搜索结果</view>
      <view class="shop-list">
        <view class="shop-item" v-for="shop in searchResults" :key="shop.tenantId" @tap="handleSelectShop(shop)">
          <image class="shop-logo" :src="shop.shopLogo || defaultLogo" mode="aspectFill" />
          <view class="shop-info">
            <view class="shop-name">{{ shop.shopName }}</view>
            <view class="shop-distance" v-if="shop.distance">
              距您 {{ formatDistance(shop.distance) }}
            </view>
            <view class="shop-address" v-if="shop.address">
              🏠 {{ shop.address }}
            </view>
          </view>
          <view class="select-arrow">›</view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="!loading && nearbyShops.length === 0 && (!searchKeyword || searchResults.length === 0)">
      <text class="empty-icon">🐾</text>
      <text class="empty-text">暂无门店</text>
    </view>

    <!-- 加载中 -->
    <view class="loading-state" v-if="loading">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onLoad, onMounted } from 'vue'
import { useMemberStore } from '@/stores/member'
import { getNearbyShops, bindShop } from '@/api/index'
import type { ShopInfo } from '@/types'

const memberStore = useMemberStore()

const searchKeyword = ref('')
const nearbyShops = ref<ShopInfo[]>([])
const searchResults = ref<ShopInfo[]>([])
const loading = ref(false)
const defaultLogo = '/static/default-shop-logo.png'

let currentLocation = { latitude: 0, longitude: 0 }

onLoad(() => {
  loadLocationAndShops()
})

onMounted(() => {
  checkLoginStatus()
})

function checkLoginStatus() {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.redirectTo({ url: '/pages/welcome/index' })
  }
}

async function loadLocationAndShops() {
  try {
    loading.value = true

    // 获取位置
    try {
      const location: any = await new Promise((resolve, reject) => {
        uni.getLocation({
          type: 'wgs84',
          success: resolve,
          fail: reject
        })
      })
      currentLocation.latitude = location.latitude
      currentLocation.longitude = location.longitude
    } catch (locError) {
      console.warn('Get location failed, use default:', locError)
    }

    await loadNearbyShops()
  } catch (error) {
    console.error('Load shops failed:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function loadNearbyShops() {
  try {
    const res = await getNearbyShops({
      latitude: currentLocation.latitude,
      longitude: currentLocation.longitude
    })
    if (res.code === 200 && res.data) {
      nearbyShops.value = res.data
    }
  } catch (error) {
    console.error('Load nearby shops failed:', error)
  }
}

async function handleSearch() {
  if (!searchKeyword.value.trim()) {
    return
  }

  try {
    loading.value = true
    const res = await getNearbyShops({
      latitude: currentLocation.latitude,
      longitude: currentLocation.longitude,
      keyword: searchKeyword.value.trim()
    })
    if (res.code === 200 && res.data) {
      searchResults.value = res.data
    }
  } catch (error) {
    console.error('Search shops failed:', error)
    uni.showToast({ title: '搜索失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function handleSelectShop(shop: ShopInfo) {
  try {
    uni.showLoading({ title: '绑定中...' })
    await bindShop({ tenantId: shop.tenantId })
    memberStore.setCurrentShop(shop)
    uni.hideLoading()
    uni.switchTab({ url: '/pages/home/index' })
  } catch (error) {
    uni.hideLoading()
    console.error('Bind shop failed:', error)
    uni.showToast({ title: '绑定失败', icon: 'none' })
  }
}

function formatDistance(meters: number): string {
  if (meters < 1000) {
    return `${meters}m`
  }
  return `${(meters / 1000).toFixed(1)}km`
}
</script>

<style lang="scss" scoped>
.select-shop-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.search-bar {
  padding: 16px;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 20px;
  padding: 10px 16px;
  gap: 8px;
}

.search-icon {
  font-size: 16px;
}

.search-input {
  flex: 1;
  font-size: 14px;
}

.section {
  margin-top: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  padding: 0 16px 12px;
}

.shop-list {
  padding: 0 16px;
}

.shop-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.shop-logo {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  background: #f0f0f0;
  flex-shrink: 0;
}

.shop-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.shop-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.shop-distance {
  font-size: 13px;
  color: #ff4d4f;
}

.shop-address {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.select-arrow {
  font-size: 24px;
  color: #ccc;
  flex-shrink: 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 100px;
  gap: 16px;
}

.empty-icon {
  font-size: 60px;
}

.empty-text {
  font-size: 14px;
  color: #999;
}

.loading-state {
  display: flex;
  justify-content: center;
  padding-top: 100px;
  font-size: 14px;
  color: #999;
}
</style>
