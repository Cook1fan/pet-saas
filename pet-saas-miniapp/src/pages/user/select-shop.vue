<template>
  <view class="select-shop-page">
    <view class="page-header">
      <text class="header-title">选择门店</text>
      <text class="header-subtitle">请选择您要访问的宠物门店</text>
    </view>

    <view class="shop-list" v-if="shops.length > 0">
      <view
        class="shop-item"
        v-for="shop in shops"
        :key="shop.tenantId"
        @tap="handleSelectShop(shop)"
      >
        <view class="shop-icon">
          <text class="shop-emoji">🏠</text>
        </view>
        <view class="shop-info">
          <text class="shop-name">{{ shop.shopName }}</text>
          <text class="shop-address" v-if="shop.address">{{ shop.address }}</text>
          <text class="shop-phone" v-if="shop.phone">电话: {{ shop.phone }}</text>
        </view>
        <view class="shop-arrow">
          <text>></text>
        </view>
      </view>
    </view>

    <view class="empty-state" v-else-if="!loading">
      <text class="empty-text">暂无可用门店</text>
    </view>

    <view class="loading-state" v-if="loading">
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAvailableShopList } from '@/api/shop'

interface ShopItem {
  tenantId: number
  shopName: string
  address: string
  phone: string
  isCurrent: boolean
}

const shops = ref<ShopItem[]>([])
const loading = ref(false)
const selecting = ref(false)

onMounted(() => {
  fetchShops()
})

async function fetchShops() {
  loading.value = true
  try {
    const res = await getAvailableShopList()
    if (res.code === 200) {
      shops.value = res.data || []
    } else {
      uni.showToast({
        title: res.message || '获取门店列表失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('获取门店列表失败:', error)
    uni.showToast({
      title: '网络错误',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

async function handleSelectShop(shop: ShopItem) {
  if (selecting.value) return
  selecting.value = true

  try {
    // 获取当前登录状态
    const token = uni.getStorageSync('token')
    if (!token) {
      // 如果没有 token，说明登录态丢失，需要重新登录
      uni.showModal({
        title: '提示',
        content: '登录已过期，请重新登录',
        confirmText: '去登录',
        success: (res) => {
          if (res.confirm) {
            uni.redirectTo({
              url: '/pages/user/login'
            })
          }
        }
      })
      return
    }

    // 调用绑定接口
    const res = await new Promise<any>((resolve, reject) => {
      uni.request({
        url: 'http://localhost:8080/api/user/shop/bind',
        method: 'POST',
        data: { tenantId: shop.tenantId },
        header: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        success: resolve,
        fail: reject
      })
    })

    if (res.data.code === 200) {
      uni.showToast({
        title: '选择成功',
        icon: 'success'
      })

      // 跳转回首页
      setTimeout(() => {
        uni.switchTab({
          url: '/pages/index/index'
        })
      }, 1500)
    } else {
      uni.showToast({
        title: res.data.message || '选择失败',
        icon: 'none'
      })
    }
  } catch (error: any) {
    console.error('选择门店失败:', error)
    uni.showToast({
      title: error.message || '选择失败',
      icon: 'none'
    })
  } finally {
    selecting.value = false
  }
}
</script>

<style lang="scss">
.select-shop-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.page-header {
  background-color: #fff;
  padding: 40rpx 30rpx;
  text-align: center;
}

.header-title {
  display: block;
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.header-subtitle {
  display: block;
  font-size: 28rpx;
  color: #999;
}

.shop-list {
  padding: 20rpx 30rpx;
}

.shop-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.shop-icon {
  width: 100rpx;
  height: 100rpx;
  background-color: #fff7f7;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.shop-emoji {
  font-size: 48rpx;
}

.shop-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.shop-name {
  font-size: 32rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 8rpx;
}

.shop-address {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 4rpx;
}

.shop-phone {
  font-size: 24rpx;
  color: #666;
}

.shop-arrow {
  font-size: 32rpx;
  color: #ccc;
  flex-shrink: 0;
}

.empty-state,
.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 100rpx 0;
}

.empty-text,
.loading-text {
  font-size: 28rpx;
  color: #999;
}
</style>
