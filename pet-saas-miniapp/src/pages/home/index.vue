<template>
  <view class="home-container">
    <!-- 顶部门店信息 -->
    <view class="header-section" @tap="handleContactService">
      <view class="shop-info">
        <image class="shop-logo" :src="homeStore.shopInfo?.shopLogo || defaultLogo" mode="aspectFill" />
        <view class="shop-name">{{ homeStore.shopInfo?.shopName || '宠物门店' }}</view>
      </view>
      <view class="contact-btn">
        <text>📞</text>
        <text>联系客服</text>
        <text>›</text>
      </view>
    </view>

    <!-- Banner 轮播 -->
    <view class="banner-section" v-if="homeStore.bannerList.length > 0">
      <swiper class="banner-swiper" :indicator-dots="true" :autoplay="true" :interval="3000" :circular="true">
        <swiper-item v-for="banner in homeStore.bannerList" :key="banner.id" @tap="handleBannerTap(banner)">
          <image class="banner-image" :src="banner.imageUrl" mode="aspectFill" />
        </swiper-item>
      </swiper>
    </view>

    <!-- 快捷入口 -->
    <view class="nav-section">
      <view class="nav-grid">
        <view class="nav-item" v-for="nav in quickNavList" :key="nav.id" @tap="handleNavTap(nav)">
          <text class="nav-icon">{{ nav.icon }}</text>
          <text class="nav-name">{{ nav.name }}</text>
        </view>
      </view>
    </view>

    <!-- 限时秒杀 -->
    <view class="section" v-if="homeStore.flashSaleList.length > 0">
      <view class="section-header">
        <view class="section-title">
          <text class="title-icon">⏰</text>
          <text>限时秒杀</text>
        </view>
        <view class="section-more" @tap="navigateToActivity('flash')">更多 ›</view>
      </view>
      <scroll-view class="activity-scroll" scroll-x="true" show-scrollbar="false">
        <view class="activity-list-horizontal">
          <view
            class="activity-card-horizontal"
            v-for="activity in homeStore.flashSaleList"
            :key="activity.id"
            @tap="navigateToActivityDetail(activity.id)"
          >
            <image class="activity-cover" :src="activity.coverImage" mode="aspectFill" />
            <view class="activity-info">
              <view class="activity-title">{{ activity.title }}</view>
              <view class="activity-price">
                <text class="price">¥{{ activity.activityPrice }}</text>
                <text class="original-price">¥{{ activity.originalPrice }}</text>
              </view>
              <view class="flash-countdown" v-if="activity.flashInfo">
                倒计时: {{ formatCountdown(activity.flashInfo.remainingSeconds) }}
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 热门拼团 -->
    <view class="section" v-if="homeStore.groupBuyList.length > 0">
      <view class="section-header">
        <view class="section-title">
          <text class="title-icon">🎊</text>
          <text>热门拼团</text>
        </view>
        <view class="section-more" @tap="navigateToActivity('group')">更多 ›</view>
      </view>
      <view class="activity-list">
        <view
          class="activity-card"
          v-for="activity in homeStore.groupBuyList"
          :key="activity.id"
          @tap="navigateToActivityDetail(activity.id)"
        >
          <image class="activity-cover" :src="activity.coverImage" mode="aspectFill" />
          <view class="activity-info">
            <view class="activity-title">{{ activity.title }}</view>
            <view class="activity-price">
              <text class="price">¥{{ activity.activityPrice }}</text>
              <text class="original-price">¥{{ activity.originalPrice }}</text>
            </view>
            <view class="group-info" v-if="activity.groupInfo">
              <view class="group-progress">
                [{{ activity.groupInfo.currentCount }}/{{ activity.groupInfo.targetCount }}人团]
              </view>
              <view class="invite-btn">邀请好友</view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 全部活动 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">
          <text class="title-icon">📋</text>
          <text>全部活动</text>
        </view>
      </view>
      <view class="activity-tabs">
        <view
          class="tab-item"
          :class="{ active: homeStore.currentTab === tab }"
          v-for="tab in activityTabs"
          :key="tab"
          @tap="switchTab(tab)"
        >
          {{ tab === 'all' ? '全部' : tab === 'group' ? '拼团' : '秒杀' }}
        </view>
      </view>
      <view class="activity-list" v-if="displayActivityList.length > 0">
        <view
          class="activity-card"
          v-for="activity in displayActivityList"
          :key="activity.id"
          @tap="navigateToActivityDetail(activity.id)"
        >
          <image class="activity-cover" :src="activity.coverImage" mode="aspectFill" />
          <view class="activity-info">
            <view class="activity-tag" :class="activity.type">{{ activity.type === 'group' ? '拼团' : '秒杀' }}</view>
            <view class="activity-title">{{ activity.title }}</view>
            <view class="activity-price">
              <text class="price">¥{{ activity.activityPrice }}</text>
              <text class="original-price">¥{{ activity.originalPrice }}</text>
            </view>
            <view class="activity-meta">
              <text class="stock-text">仅剩 {{ activity.stock }} 份</text>
              <text class="sold-text">已售 {{ activity.soldCount }}</text>
            </view>
          </view>
        </view>
      </view>
      <view class="empty-text" v-else>暂无活动</view>
    </view>

    <!-- 底部占位 -->
    <view class="bottom-placeholder"></view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onLoad, onShow, onMounted } from 'vue'
import { useMemberStore } from '@/stores/member'
import { useHomeStore } from '@/stores/home'
import { getHomeData } from '@/api/index'
import type { QuickNavItem, BannerItem } from '@/types'

const memberStore = useMemberStore()
const homeStore = useHomeStore()

const defaultLogo = '/static/default-shop-logo.png'
const activityTabs = ['all', 'group', 'flash'] as const

const quickNavList = ref<QuickNavItem[]>([
  { id: 1, icon: '🐱', name: '商城', path: '' },
  { id: 2, icon: '🎁', name: '活动', path: '/pages/activity/list' },
  { id: 3, icon: '💳', name: '余额', path: '/pages/member/recharge' },
  { id: 4, icon: '🐾', name: '档案', path: '/pages/member/pets' },
  { id: 5, icon: '💬', name: '客服', path: '/pages/service/ai-chat' },
  { id: 6, icon: '📋', name: '订单', path: '/pages/order/list' },
  { id: 7, icon: '📍', name: '门店', path: '/pages/shop/manage' },
  { id: 8, icon: '🏠', name: '首页', path: '' }
])

const displayActivityList = computed(() => {
  if (homeStore.currentTab === 'all') {
    return homeStore.allActivityList
  } else if (homeStore.currentTab === 'group') {
    return homeStore.groupBuyList
  } else {
    return homeStore.flashSaleList
  }
})

onLoad(() => {
  checkLoginStatus()
})

onShow(() => {
  loadHomeData()
})

function checkLoginStatus() {
  if (!memberStore.isLoggedIn) {
    uni.reLaunch({ url: '/pages/welcome/index' })
    return
  }
  if (!memberStore.currentShop) {
    uni.redirectTo({ url: '/pages/shop/select' })
    return
  }
}

async function loadHomeData() {
  try {
    homeStore.setLoading(true)
    const res = await getHomeData()
    if (res.code === 200 && res.data) {
      homeStore.setShopInfo(res.data.shopInfo)
      homeStore.setBannerList(res.data.bannerList)
      homeStore.setQuickNavList(res.data.quickNavList)
      homeStore.setFlashSaleList(res.data.flashSaleList)
      homeStore.setGroupBuyList(res.data.groupBuyList)

      // 合并全部活动列表
      const allList = [...res.data.groupBuyList, ...res.data.flashSaleList]
      homeStore.setAllActivityList(allList)

      // 使用后端返回的快捷入口，如果有的话
      if (res.data.quickNavList.length > 0) {
        quickNavList.value = res.data.quickNavList
      }
    }
  } catch (error) {
    console.error('Load home data failed:', error)
  } finally {
    homeStore.setLoading(false)
  }
}

function handleContactService() {
  uni.navigateTo({ url: '/pages/service/ai-chat' })
}

function handleBannerTap(banner: BannerItem) {
  if (banner.linkType === 'activity' && banner.linkValue) {
    uni.navigateTo({ url: `/pages/activity/detail?id=${banner.linkValue}` })
  } else if (banner.linkType === 'page' && banner.linkValue) {
    uni.navigateTo({ url: banner.linkValue })
  }
}

function handleNavTap(nav: QuickNavItem) {
  if (nav.path) {
    if (nav.path.startsWith('/pages/')) {
      uni.navigateTo({ url: nav.path })
    }
  } else {
    uni.showToast({ title: '功能开发中', icon: 'none' })
  }
}

function navigateToActivity(type: 'group' | 'flash') {
  uni.switchTab({ url: '/pages/activity/list' })
}

function navigateToActivityDetail(id: number) {
  uni.navigateTo({ url: `/pages/activity/detail?id=${id}` })
}

function switchTab(tab: 'all' | 'group' | 'flash') {
  homeStore.setCurrentTab(tab)
}

function formatCountdown(seconds: number): string {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
.home-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 20px;
}

.header-section {
  background: #fff;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.shop-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.shop-logo {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f0f0f0;
}

.shop-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.contact-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
}

.banner-section {
  padding: 16px;
}

.banner-swiper {
  height: 160px;
  border-radius: 12px;
  overflow: hidden;
}

.banner-image {
  width: 100%;
  height: 100%;
}

.nav-section {
  background: #fff;
  margin: 0 16px;
  border-radius: 12px;
  padding: 20px 16px;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px 8px;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.nav-icon {
  font-size: 28px;
}

.nav-name {
  font-size: 12px;
  color: #333;
}

.section {
  margin-top: 16px;
  padding: 0 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.title-icon {
  font-size: 18px;
}

.section-more {
  font-size: 13px;
  color: #999;
}

.activity-scroll {
  white-space: nowrap;
}

.activity-list-horizontal {
  display: flex;
  gap: 12px;
  padding-bottom: 8px;
}

.activity-card-horizontal {
  width: 140px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.activity-cover {
  width: 100%;
  height: 100px;
  background: #f0f0f0;
}

.activity-info {
  padding: 10px;
}

.activity-tag {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  margin-bottom: 6px;

  &.group {
    background: #fff0f0;
    color: #ff4d4f;
  }

  &.flash {
    background: #fff7e6;
    color: #fa8c16;
  }
}

.activity-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-price {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 6px;
}

.price {
  font-size: 18px;
  font-weight: 700;
  color: #ff4d4f;
}

.original-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
}

.activity-meta {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #999;
}

.flash-countdown {
  font-size: 11px;
  color: #ff4d4f;
}

.group-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
}

.group-progress {
  font-size: 11px;
  color: #666;
}

.invite-btn {
  font-size: 11px;
  color: #ff4d4f;
  padding: 2px 8px;
  border: 1px solid #ff4d4f;
  border-radius: 10px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-card {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  gap: 12px;
}

.activity-card .activity-cover {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  flex-shrink: 0;
}

.activity-card .activity-info {
  flex: 1;
  padding: 0;
  display: flex;
  flex-direction: column;
}

.activity-tabs {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.tab-item {
  font-size: 14px;
  color: #666;
  padding-bottom: 8px;
  position: relative;

  &.active {
    color: #ff4d4f;
    font-weight: 600;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 24px;
      height: 3px;
      background: #ff4d4f;
      border-radius: 2px;
    }
  }
}

.empty-text {
  text-align: center;
  padding: 40px 0;
  font-size: 14px;
  color: #999;
}

.bottom-placeholder {
  height: 20px;
}
</style>
