<template>
  <view class="order-confirm-container">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">‹</view>
      <view class="nav-title">确认订单</view>
    </view>

    <!-- 选择宠物 -->
    <view class="section">
      <view class="section-header" @tap="showPetPicker">
        <view class="section-title">🐱 选择宠物</view>
        <view class="section-arrow">
          <text v-if="selectedPet">{{ selectedPet.name }}</text>
          <text v-else class="placeholder-text">请选择宠物</text>
          <text>›</text>
        </view>
      </view>
    </view>

    <!-- 订单商品 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">📦 订单商品</view>
      </view>
      <view class="product-item" v-if="activityDetail">
        <image class="product-cover" :src="activityDetail.coverImages?.[0] || ''" mode="aspectFill" />
        <view class="product-info">
          <view class="product-title">{{ activityDetail.title }}</view>
          <view class="product-sku" v-if="selectedSku">{{ selectedSku.specName }}</view>
          <view class="product-price">¥{{ selectedSku?.price || activityDetail.activityPrice }}</view>
        </view>
      </view>
    </view>

    <!-- 预约备注 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">📝 预约备注</view>
      </view>
      <view class="remark-input-wrapper">
        <textarea
          class="remark-input"
          v-model="remark"
          placeholder="请输入预约时间或其他备注信息（选填）"
          :maxlength="200"
        />
        <view class="char-count">{{ remark.length }}/200</view>
      </view>
    </view>

    <!-- 支付方式 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">💳 支付方式</view>
      </view>
      <view class="pay-method-list">
        <view
          class="pay-method-item"
          :class="{ selected: payMethod === 'wechat' }"
          @tap="selectPayMethod('wechat')"
        >
          <view class="pay-method-info">
            <text class="pay-icon">💚</text>
            <text class="pay-name">微信支付</text>
          </view>
          <view class="pay-check" :class="{ checked: payMethod === 'wechat' }">
            {{ payMethod === 'wechat' ? '✓' : '' }}
          </view>
        </view>
        <view
          class="pay-method-item"
          :class="{ selected: payMethod === 'balance' }"
          @tap="selectPayMethod('balance')"
        >
          <view class="pay-method-info">
            <text class="pay-icon">💰</text>
            <text class="pay-name">余额支付</text>
            <text class="balance-text">(可用余额: ¥{{ memberBalance }})</text>
          </view>
          <view class="pay-check" :class="{ checked: payMethod === 'balance' }">
            {{ payMethod === 'balance' ? '✓' : '' }}
          </view>
        </view>
      </view>
    </view>

    <!-- 费用明细 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">📋 费用明细</view>
      </view>
      <view class="fee-list">
        <view class="fee-item">
          <text class="fee-label">商品金额</text>
          <text class="fee-value">¥{{ orderAmount }}</text>
        </view>
        <view class="fee-item">
          <text class="fee-label">优惠</text>
          <text class="fee-value discount">-¥0</text>
        </view>
      </view>
    </view>

    <!-- 拼团信息（如果是参团） -->
    <view class="section" v-if="groupId">
      <view class="section-header">
        <view class="section-title">🎊 拼团信息</view>
      </view>
      <view class="group-info-card">
        <text>您正在参加他人的拼团</text>
      </view>
    </view>

    <!-- 底部支付栏 -->
    <view class="bottom-bar">
      <view class="amount-info">
        <text class="amount-label">实付金额</text>
        <text class="amount-value">¥{{ orderAmount }}</text>
      </view>
      <view class="submit-btn" :class="{ disabled: !canSubmit }" @tap="handleSubmitOrder">
        确认支付
      </view>
    </view>

    <!-- 宠物选择器 -->
    <view class="picker-modal" v-if="showPetModal" @tap="showPetModal = false">
      <view class="picker-content" @tap.stop>
        <view class="picker-header">
          <text class="picker-title">选择宠物</text>
          <text class="picker-close" @tap="showPetModal = false">✕</text>
        </view>
        <view class="picker-list">
          <view
            class="picker-item"
            :class="{ selected: selectedPet?.id === pet.id }"
            v-for="pet in petList"
            :key="pet.id"
            @tap="handleSelectPet(pet)"
          >
            <image class="pet-avatar" :src="pet.avatar || '/static/default-pet.png'" mode="aspectFill" />
            <view class="pet-info">
              <view class="pet-name">{{ pet.name }}</view>
              <view class="pet-breed">{{ pet.breed }}</view>
            </view>
            <view class="pet-check" v-if="selectedPet?.id === pet.id">✓</view>
          </view>
          <view class="picker-empty" v-if="petList.length === 0">
            <text class="empty-icon">🐾</text>
            <text class="empty-text">暂未添加宠物</text>
            <view class="add-pet-btn" @tap="navigateToAddPet">去添加</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onLoad } from 'vue'
import { getActivityDetail, getMemberPets, createOrder } from '@/api/index'
import type { ActivityDetail, PetInfo, ActivitySku } from '@/types'

const activityId = ref<number>(0)
const groupId = ref<number | undefined>()
const skuId = ref<number | undefined>()
const activityType = ref<'group' | 'flash'>('group')

const activityDetail = ref<ActivityDetail | null>(null)
const selectedSku = ref<ActivitySku | null>(null)
const selectedPet = ref<PetInfo | null>(null)
const petList = ref<PetInfo[]>([])
const payMethod = ref<'wechat' | 'balance'>('wechat')
const remark = ref('')
const memberBalance = ref(0)
const showPetModal = ref(false)

const orderAmount = computed(() => {
  return selectedSku.value?.price || activityDetail.value?.activityPrice || 0
})

const canSubmit = computed(() => {
  return orderAmount.value > 0 && payMethod.value
})

onLoad(async (options) => {
  if (options.id) activityId.value = parseInt(options.id)
  if (options.activityId) activityId.value = parseInt(options.activityId)
  if (options.groupId) groupId.value = parseInt(options.groupId)
  if (options.skuId) skuId.value = parseInt(options.skuId)
  if (options.type) activityType.value = options.type as any

  await Promise.all([
    loadActivityDetail(),
    loadPetList()
  ])
})

async function loadActivityDetail() {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await getActivityDetail(activityId.value)
    if (res.code === 200 && res.data) {
      activityDetail.value = res.data
      if (skuId.value) {
        selectedSku.value = res.data.skuList?.find(s => s.skuId === skuId.value)
      }
      if (!selectedSku.value && res.data.skuList?.length > 0) {
        selectedSku.value = res.data.skuList.find(s => s.stock > 0) || res.data.skuList[0]
      }
    }
  } catch (error) {
    console.error('Load activity detail failed:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

async function loadPetList() {
  try {
    const res = await getMemberPets()
    if (res.code === 200 && res.data) {
      petList.value = res.data
      if (res.data.length > 0) {
        selectedPet.value = res.data[0]
      }
    }
  } catch (error) {
    console.error('Load pet list failed:', error)
  }
}

function goBack() {
  uni.navigateBack()
}

function showPetPicker() {
  showPetModal.value = true
}

function handleSelectPet(pet: PetInfo) {
  selectedPet.value = pet
  showPetModal.value = false
}

function selectPayMethod(method: 'wechat' | 'balance') {
  payMethod.value = method
}

function navigateToAddPet() {
  showPetModal.value = false
  uni.navigateTo({ url: '/pages/member/pets?action=add' })
}

async function handleSubmitOrder() {
  if (!canSubmit.value) return

  try {
    uni.showLoading({ title: '创建订单...' })

    const orderData: Parameters<typeof createOrder>[0] = {
      activityId: activityId.value,
      skuId: selectedSku.value!.skuId,
      payMethod: payMethod.value
    }

    if (groupId.value) orderData.groupId = groupId.value
    if (selectedPet.value) orderData.petId = selectedPet.value.id
    if (remark.value) orderData.remark = remark.value

    const res = await createOrder(orderData)

    if (res.code === 200 && res.data) {
      uni.hideLoading()

      if (payMethod.value === 'wechat' && res.data.wechatPayParams) {
        await requestPayment(res.data.wechatPayParams, res.data.orderId)
      } else {
        navigateToResult(res.data.orderId, 'success')
      }
    }
  } catch (error: any) {
    uni.hideLoading()
    console.error('Submit order failed:', error)
    uni.showToast({
      title: error.message || '下单失败',
      icon: 'none'
    })
  }
}

async function requestPayment(payParams: any, orderId: string) {
  try {
    uni.showLoading({ title: '支付中...' })

    await new Promise((resolve, reject) => {
      uni.requestPayment({
        provider: 'wxpay',
        timeStamp: payParams.timeStamp,
        nonceStr: payParams.nonceStr,
        package: payParams.package,
        signType: payParams.signType,
        paySign: payParams.paySign,
        success: resolve,
        fail: reject
      })
    })

    uni.hideLoading()
    navigateToResult(orderId, 'success')
  } catch (error) {
    uni.hideLoading()
    navigateToResult(orderId, 'fail')
  }
}

function navigateToResult(orderId: string, status: 'success' | 'fail') {
  uni.redirectTo({
    url: `/pages/order/pay?orderId=${orderId}&status=${status}`
  })
}
</script>

<style lang="scss" scoped>
.order-confirm-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 100px;
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
  margin-top: 8px;
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.section-arrow {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.placeholder-text {
  color: #999;
}

.product-item {
  display: flex;
  gap: 12px;
}

.product-cover {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  background: #f0f0f0;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.product-sku {
  font-size: 12px;
  color: #999;
}

.product-price {
  font-size: 16px;
  font-weight: 600;
  color: #ff4d4f;
  margin-top: auto;
}

.remark-input-wrapper {
  position: relative;
}

.remark-input {
  width: 100%;
  min-height: 80px;
  background: #f5f5f5;
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
  box-sizing: border-box;
}

.char-count {
  position: absolute;
  right: 8px;
  bottom: 8px;
  font-size: 12px;
  color: #999;
}

.pay-method-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pay-method-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid transparent;

  &.selected {
    border-color: #ff4d4f;
    background: #fff0f0;
  }
}

.pay-method-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pay-icon {
  font-size: 20px;
}

.pay-name {
  font-size: 14px;
  color: #333;
}

.balance-text {
  font-size: 12px;
  color: #666;
}

.pay-check {
  width: 20px;
  height: 20px;
  border: 2px solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #fff;

  &.checked {
    background: #ff4d4f;
    border-color: #ff4d4f;
  }
}

.fee-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.fee-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.fee-label {
  color: #666;
}

.fee-value {
  color: #333;

  &.discount {
    color: #ff4d4f;
  }
}

.group-info-card {
  background: #fff0f0;
  border-radius: 8px;
  padding: 12px;
  font-size: 13px;
  color: #ff4d4f;
  text-align: center;
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

.amount-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.amount-label {
  font-size: 14px;
  color: #666;
}

.amount-value {
  font-size: 22px;
  font-weight: 700;
  color: #ff4d4f;
}

.submit-btn {
  padding: 12px 32px;
  background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
  color: #fff;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;

  &.disabled {
    background: #ddd;
  }
}

.picker-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
}

.picker-content {
  width: 100%;
  background: #fff;
  border-radius: 16px 16px 0 0;
  max-height: 60vh;
  display: flex;
  flex-direction: column;
}

.picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.picker-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.picker-close {
  font-size: 20px;
  color: #999;
}

.picker-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}

.picker-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.picker-item:last-child {
  border-bottom: none;
}

.pet-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: #f0f0f0;
  flex-shrink: 0;
}

.pet-info {
  flex: 1;
}

.pet-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.pet-breed {
  font-size: 13px;
  color: #999;
}

.pet-check {
  color: #ff4d4f;
  font-size: 18px;
}

.picker-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
  gap: 12px;
}

.empty-icon {
  font-size: 48px;
}

.empty-text {
  font-size: 14px;
  color: #999;
}

.add-pet-btn {
  padding: 8px 24px;
  background: #ff4d4f;
  color: #fff;
  border-radius: 16px;
  font-size: 14px;
}
</style>
