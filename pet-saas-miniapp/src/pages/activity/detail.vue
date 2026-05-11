<template>
  <view class="activity-detail-container">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">‹</view>
      <view class="nav-title">活动详情</view>
      <view class="nav-actions">
        <view class="nav-action" @tap="handleShare">分享</view>
        <view class="nav-action" @tap="handleGeneratePoster">海报</view>
      </view>
    </view>

    <!-- 活动图片轮播 -->
    <view class="banner-section" v-if="activityDetail">
      <swiper class="banner-swiper" :indicator-dots="true" :circular="true">
        <swiper-item v-for="(img, index) in activityDetail.coverImages" :key="index">
          <image class="banner-image" :src="img" mode="aspectFill" />
        </swiper-item>
        <swiper-item v-if="activityDetail.coverImages.length === 0">
          <image class="banner-image" src="/static/default-activity.png" mode="aspectFill" />
        </swiper-item>
      </swiper>
    </view>

    <!-- 活动基本信息 -->
    <view class="info-section" v-if="activityDetail">
      <view class="activity-type-badge" :class="activityDetail.type">
        {{ activityDetail.type === 'group' ? '🎊 拼团' : '⏰ 秒杀' }}
      </view>
      <view class="activity-title">{{ activityDetail.title }}</view>
      <view class="activity-sold">🔥 已售 {{ activityDetail.soldCount }} 份</view>

      <view class="price-section">
        <text class="price-label">活动价</text>
        <text class="price">¥{{ activityDetail.activityPrice }}</text>
        <text class="original-price">¥{{ activityDetail.originalPrice }}</text>
      </view>

      <view class="meta-section">
        <view class="meta-item">
          <text class="meta-icon">⏰</text>
          <text>活动截止: {{ formatDate(activityDetail.endTime) }}</text>
        </view>
        <view class="meta-item" v-if="activityDetail.serviceInfo?.address">
          <text class="meta-icon">📍</text>
          <text>服务地址: {{ activityDetail.serviceInfo.address }}</text>
        </view>
      </view>
    </view>

    <!-- 拼团进度（仅限拼团） -->
    <view class="group-progress-section" v-if="activityDetail?.type === 'group'">
      <view class="progress-header">
        <text>拼团进度</text>
      </view>
      <view class="progress-info" v-if="activityDetail.groupInfo">
        <view class="progress-stats">
          <text class="progress-current">{{ activityDetail.groupInfo.groupList[0]?.currentCount || 0 }}</text>
          <text class="progress-divider">/</text>
          <text class="progress-target">{{ activityDetail.groupInfo.targetCount }}</text>
          <text class="progress-unit">人</text>
        </view>
        <view class="progress-bar">
          <view
            class="progress-fill"
            :style="{
              width: `${((activityDetail.groupInfo.groupList[0]?.currentCount || 0) / activityDetail.groupInfo.targetCount) * 100}%`
            }"
          ></view>
        </view>
        <view class="progress-text">还差 {{ activityDetail.groupInfo.targetCount - (activityDetail.groupInfo.groupList[0]?.currentCount || 0) }} 人即可成团！</view>
      </view>
    </view>

    <!-- 可加入的团（仅限拼团） -->
    <view class="joinable-groups-section" v-if="activityDetail?.type === 'group' && activityDetail?.groupInfo?.groupList?.length > 0">
      <view class="section-header">
        <text class="section-title">正在拼团的团</text>
      </view>
      <view class="group-list">
        <view
          class="group-item"
          v-for="group in activityDetail.groupInfo.groupList"
          :key="group.groupId"
        >
          <view class="group-members">
            <view class="member-avatars">
              <image
                class="avatar"
                v-for="(avatar, index) in group.memberAvatars"
                :key="index"
                :src="avatar || '/static/default-avatar.png'"
                mode="aspectFill"
              />
              <view class="avatar-placeholder" v-if="group.memberAvatars.length < group.targetCount">
                +{{ group.targetCount - group.memberAvatars.length }}
              </view>
            </view>
            <view class="group-leader">{{ group.leaderNickname }} 发起的团</view>
            <view class="group-countdown">剩余 {{ formatCountdown(group.remainingSeconds) }}</view>
          </view>
          <view class="join-btn" @tap="handleJoinGroup(group)">去参团</view>
        </view>
      </view>
    </view>

    <!-- 秒杀倒计时（仅限秒杀） -->
    <view class="flash-countdown-section" v-if="activityDetail?.type === 'flash' && activityDetail?.flashInfo">
      <view class="countdown-header">
        <text>🔥 秒杀进行中</text>
      </view>
      <view class="countdown-display">
        <view class="countdown-item">{{ Math.floor(activityDetail.flashInfo.remainingSeconds / 3600).toString().padStart(2, '0') }}</view>
        <text class="countdown-colon">:</text>
        <view class="countdown-item">{{ Math.floor((activityDetail.flashInfo.remainingSeconds % 3600) / 60).toString().padStart(2, '0') }}</view>
        <text class="countdown-colon">:</text>
        <view class="countdown-item">{{ (activityDetail.flashInfo.remainingSeconds % 60).toString().padStart(2, '0') }}</view>
      </view>
      <view class="sold-percentage">
        <view class="sold-bar">
          <view class="sold-fill" :style="{ width: `${activityDetail.flashInfo.soldPercent}%` }"></view>
        </view>
        <text>已抢 {{ activityDetail.flashInfo.soldPercent }}%</text>
      </view>
    </view>

    <!-- 规格选择 -->
    <view class="sku-section" v-if="activityDetail?.skuList?.length > 0">
      <view class="section-header">
        <text class="section-title">选择规格</text>
      </view>
      <view class="sku-list">
        <view
          class="sku-item"
          :class="{ selected: selectedSku?.skuId === sku.skuId, disabled: sku.stock === 0 }"
          v-for="sku in activityDetail.skuList"
          :key="sku.skuId"
          @tap="handleSelectSku(sku)"
        >
          <view class="sku-name">{{ sku.specName }}</view>
          <view class="sku-price">¥{{ sku.price }}</view>
        </view>
      </view>
    </view>

    <!-- 活动详情内容 -->
    <view class="detail-content-section" v-if="activityDetail">
      <view class="section-header">
        <text class="section-title">活动详情</text>
      </view>
      <view class="content-block">
        <view class="content-title">📦 套餐内容</view>
        <view class="content-text">{{ activityDetail.description || '专业宠物服务，让您的爱宠享受星级待遇~' }}</view>
      </view>
      <view class="content-block">
        <view class="content-title">📋 使用说明</view>
        <view class="content-text">
          • 有效期至: {{ formatDate(activityDetail.endTime) }}<br />
          • 节假日通用<br />
          • 需提前1天预约
        </view>
      </view>
      <view class="content-block">
        <view class="content-title">⚠️ 购买须知</view>
        <view class="content-text">
          • 拼团活动支付后24小时内未成团自动退款<br />
          • 未消费可随时退款<br />
          • 不找零不兑现
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar" v-if="activityDetail">
      <view class="left-actions">
        <view class="action-btn" @tap="handleShare">
          <text class="action-icon">📤</text>
          <text>分享</text>
        </view>
        <view class="action-btn" @tap="handleGeneratePoster">
          <text class="action-icon">🖼️</text>
          <text>海报</text>
        </view>
      </view>
      <view class="right-actions">
        <view
          class="primary-btn"
          :class="{ disabled: !selectedSku || selectedSku.stock === 0 }"
          @tap="handlePrimaryAction"
        >
          {{ activityDetail.type === 'group' ? '发起拼团' : '立即秒杀' }}
        </view>
        <view
          class="secondary-btn"
          v-if="activityDetail.type === 'group'"
          :class="{ disabled: !selectedSku || selectedSku.stock === 0 }"
          @tap="handleBuyNow"
        >
          单独购买
        </view>
      </view>
    </view>

    <!-- 海报预览弹窗 -->
    <view class="poster-modal" v-if="showPosterModal" @tap="showPosterModal = false">
      <view class="poster-content" @tap.stop>
        <image class="poster-image" :src="posterUrl" mode="widthFix" />
        <view class="poster-actions">
          <view class="poster-btn cancel" @tap="showPosterModal = false">取消</view>
          <view class="poster-btn save" @tap="handleSavePoster">保存到相册</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onLoad, onShareAppMessage } from 'vue'
import { getActivityDetail, generateActivityPoster } from '@/api/index'
import type { ActivityDetail, ActivitySku, JoinableGroup } from '@/types'

const activityId = ref<number>(0)
const activityDetail = ref<ActivityDetail | null>(null)
const selectedSku = ref<ActivitySku | null>(null)
const showPosterModal = ref(false)
const posterUrl = ref('')

onLoad((options) => {
  if (options.id) {
    activityId.value = parseInt(options.id)
    loadActivityDetail()
  }
})

async function loadActivityDetail() {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await getActivityDetail(activityId.value)
    if (res.code === 200 && res.data) {
      activityDetail.value = res.data
      // 默认选择第一个规格
      if (res.data.skuList?.length > 0) {
        selectedSku.value = res.data.skuList.find(sku => sku.stock > 0) || res.data.skuList[0]
      }
    }
  } catch (error) {
    console.error('Load activity detail failed:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

function goBack() {
  uni.navigateBack()
}

function handleSelectSku(sku: ActivitySku) {
  if (sku.stock === 0) {
    uni.showToast({ title: '该规格已售罄', icon: 'none' })
    return
  }
  selectedSku.value = sku
}

function handleJoinGroup(group: JoinableGroup) {
  if (!selectedSku.value) {
    uni.showToast({ title: '请先选择规格', icon: 'none' })
    return
  }
  navigateToConfirm(group.groupId)
}

function handlePrimaryAction() {
  if (!selectedSku.value) {
    uni.showToast({ title: '请先选择规格', icon: 'none' })
    return
  }
  if (selectedSku.value.stock === 0) {
    uni.showToast({ title: '该规格已售罄', icon: 'none' })
    return
  }
  navigateToConfirm()
}

function handleBuyNow() {
  if (!selectedSku.value) {
    uni.showToast({ title: '请先选择规格', icon: 'none' })
    return
  }
  navigateToConfirm()
}

function navigateToConfirm(groupId?: number) {
  const params: Record<string, any> = {
    activityId: activityId.value,
    skuId: selectedSku.value!.skuId,
    type: activityDetail.value?.type || 'group'
  }
  if (groupId) {
    params.groupId = groupId
  }

  uni.navigateTo({
    url: `/pages/order/confirm?${Object.entries(params).map(([k, v]) => `${k}=${v}`).join('&')}`
  })
}

function handleShare() {
  uni.showShareMenu({ withShareTicket: true })
}

onShareAppMessage(() => {
  return {
    title: activityDetail.value?.title || '这个活动不错，一起看看~',
    path: `/pages/activity/detail?id=${activityId.value}`,
    imageUrl: activityDetail.value?.coverImages?.[0] || ''
  }
})

async function handleGeneratePoster() {
  try {
    uni.showLoading({ title: '生成中...' })
    const res = await generateActivityPoster(activityId.value)
    if (res.code === 200 && res.data?.posterUrl) {
      posterUrl.value = res.data.posterUrl
      showPosterModal.value = true
    }
  } catch (error) {
    console.error('Generate poster failed:', error)
    uni.showToast({ title: '生成失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

function handleSavePoster() {
  uni.saveImageToPhotosAlbum({
    filePath: posterUrl.value,
    success: () => {
      uni.showToast({ title: '保存成功', icon: 'success' })
      showPosterModal.value = false
    },
    fail: () => {
      uni.showToast({ title: '保存失败', icon: 'none' })
    }
  })
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hour = date.getHours().toString().padStart(2, '0')
  const minute = date.getMinutes().toString().padStart(2, '0')
  return `${month}-${day} ${hour}:${minute}`
}

function formatCountdown(seconds: number): string {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
.activity-detail-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px;
}

.nav-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
}

.nav-back {
  font-size: 24px;
  font-weight: 600;
}

.nav-title {
  font-size: 16px;
  font-weight: 600;
}

.nav-actions {
  display: flex;
  gap: 16px;
}

.nav-action {
  font-size: 14px;
  color: #333;
}

.banner-section {
  background: #fff;
}

.banner-swiper {
  height: 280px;
}

.banner-image {
  width: 100%;
  height: 100%;
}

.info-section {
  background: #fff;
  padding: 16px;
  margin-top: 8px;
}

.activity-type-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  margin-bottom: 8px;

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
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.activity-sold {
  font-size: 13px;
  color: #ff4d4f;
  margin-bottom: 12px;
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 16px;
}

.price-label {
  font-size: 14px;
  color: #666;
}

.price {
  font-size: 24px;
  font-weight: 700;
  color: #ff4d4f;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.meta-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
}

.meta-icon {
  font-size: 16px;
}

.group-progress-section {
  background: #fff;
  padding: 16px;
  margin-top: 8px;
}

.section-header {
  margin-bottom: 12px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.progress-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.progress-stats {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.progress-current {
  font-size: 24px;
  font-weight: 700;
  color: #ff4d4f;
}

.progress-divider {
  font-size: 16px;
  color: #666;
}

.progress-target,
.progress-unit {
  font-size: 14px;
  color: #666;
}

.progress-bar {
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff4d4f, #ff6b6b);
  border-radius: 4px;
  transition: width 0.3s;
}

.progress-text {
  font-size: 13px;
  color: #ff4d4f;
  text-align: center;
}

.joinable-groups-section {
  background: #fff;
  padding: 16px;
  margin-top: 8px;
}

.group-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.group-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #fffaf0;
  border-radius: 8px;
}

.group-members {
  flex: 1;
}

.member-avatars {
  display: flex;
  margin-bottom: 8px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: -8px;
  border: 2px solid #fff;
}

.avatar-placeholder {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #999;
}

.group-leader {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.group-countdown {
  font-size: 12px;
  color: #999;
}

.join-btn {
  padding: 8px 16px;
  background: #ff4d4f;
  color: #fff;
  border-radius: 16px;
  font-size: 13px;
}

.flash-countdown-section {
  background: #fff7e6;
  padding: 16px;
  margin-top: 8px;
}

.countdown-header {
  text-align: center;
  margin-bottom: 12px;
  font-size: 15px;
  font-weight: 600;
  color: #fa8c16;
}

.countdown-display {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.countdown-item {
  width: 48px;
  height: 48px;
  background: #333;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
  color: #fff;
}

.countdown-colon {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.sold-percentage {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

.sold-bar {
  width: 200px;
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.sold-fill {
  height: 100%;
  background: #fa8c16;
  border-radius: 4px;
}

.sku-section {
  background: #fff;
  padding: 16px;
  margin-top: 8px;
}

.sku-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.sku-item {
  padding: 10px 16px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;

  &.selected {
    border-color: #ff4d4f;
    background: #fff0f0;
  }

  &.disabled {
    opacity: 0.5;
  }
}

.sku-name {
  font-size: 14px;
  color: #333;
}

.sku-price {
  font-size: 14px;
  font-weight: 600;
  color: #ff4d4f;
}

.detail-content-section {
  background: #fff;
  padding: 16px;
  margin-top: 8px;
}

.content-block {
  margin-bottom: 20px;
}

.content-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.content-text {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.08);
}

.left-actions {
  display: flex;
  gap: 20px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 11px;
  color: #666;
}

.action-icon {
  font-size: 18px;
}

.right-actions {
  display: flex;
  gap: 12px;
}

.primary-btn,
.secondary-btn {
  padding: 12px 24px;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;
  text-align: center;
  min-width: 100px;
}

.primary-btn {
  background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
  color: #fff;

  &.disabled {
    background: #ddd;
  }
}

.secondary-btn {
  background: #fff0f0;
  color: #ff4d4f;
  border: 1px solid #ff4d4f;

  &.disabled {
    background: #f5f5f5;
    border-color: #ddd;
    color: #999;
  }
}

.poster-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 40px 20px;
}

.poster-content {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  max-width: 100%;
}

.poster-image {
  width: 100%;
  max-width: 300px;
}

.poster-actions {
  display: flex;
  padding: 12px;
  gap: 12px;
}

.poster-btn {
  flex: 1;
  padding: 12px;
  text-align: center;
  border-radius: 8px;
  font-size: 14px;
}

.poster-btn.cancel {
  background: #f5f5f5;
  color: #666;
}

.poster-btn.save {
  background: #ff4d4f;
  color: #fff;
}
</style>
