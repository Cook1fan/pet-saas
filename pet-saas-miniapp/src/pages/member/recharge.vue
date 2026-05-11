<template>
  <view class="recharge-container">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">‹</view>
      <view class="nav-title">储值充值</view>
    </view>

    <!-- 当前余额 -->
    <view class="balance-section">
      <text class="balance-label">当前余额</text>
      <text class="balance-value">¥{{ currentBalance }}</text>
    </view>

    <!-- 充值选项 -->
    <view class="recharge-section">
      <view class="section-title">充值套餐</view>
      <view class="recharge-list">
        <view
          class="recharge-item"
          :class="{ selected: selectedAmount === item.amount }"
          v-for="item in rechargeOptions"
          :key="item.amount"
          @tap="selectAmount(item.amount)"
        >
          <view class="recharge-amount">
            <text class="symbol">¥</text>
            <text class="amount">{{ item.amount }}</text>
          </view>
          <view class="recharge-bonus" v-if="item.bonus">赠送 ¥{{ item.bonus }}</view>
          <view class="hot-tag" v-if="item.hot">推荐</view>
        </view>
      </view>
    </view>

    <!-- 自定义金额 -->
    <view class="custom-section">
      <view class="section-title">自定义金额</view>
      <view class="custom-input-wrapper">
        <text class="custom-prefix">¥</text>
        <input class="custom-input" v-model="customAmount" type="digit" placeholder="输入金额" />
      </view>
    </view>

    <!-- 充值说明 -->
    <view class="tips-section">
      <view class="tips-title">充值说明</view>
      <view class="tips-list">
        <view class="tips-item">1. 充值金额可用于门店消费</view>
        <view class="tips-item">2. 赠送金额与充值金额同效</view>
        <view class="tips-item">3. 充值后不支持退款，请谨慎操作</view>
      </view>
    </view>

    <!-- 底部充值按钮 -->
    <view class="bottom-bar">
      <view class="pay-info">
        <text class="pay-label">充值金额</text>
        <text class="pay-amount">¥{{ payAmount }}</text>
      </view>
      <view class="recharge-btn" :class="{ disabled: payAmount <= 0 }" @tap="handleRecharge">
        立即充值
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const currentBalance = ref(100)
const selectedAmount = ref(0)
const customAmount = ref('')

const rechargeOptions = [
  { amount: 100, bonus: 10, hot: false },
  { amount: 200, bonus: 25, hot: true },
  { amount: 500, bonus: 80, hot: false },
  { amount: 1000, bonus: 200, hot: false }
]

const payAmount = computed(() => {
  if (selectedAmount.value > 0) {
    return selectedAmount.value
  }
  return parseFloat(customAmount.value) || 0
})

function selectAmount(amount: number) {
  selectedAmount.value = amount
  customAmount.value = ''
}

function handleRecharge() {
  if (payAmount.value <= 0) {
    uni.showToast({ title: '请选择或输入充值金额', icon: 'none' })
    return
  }

  uni.showLoading({ title: '充值中...' })
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '充值成功', icon: 'success' })
    setTimeout(() => {
      goBack()
    }, 1000)
  }, 1500)
}

function goBack() {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.recharge-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px;
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

.balance-section {
  background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.balance-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 8px;
}

.balance-value {
  font-size: 36px;
  font-weight: 700;
  color: #fff;
}

.recharge-section {
  background: #fff;
  margin-top: 12px;
  padding: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.recharge-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.recharge-item {
  position: relative;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 20px 12px;
  text-align: center;

  &.selected {
    border-color: #ff4d4f;
    background: #fff0f0;
  }
}

.recharge-amount {
  margin-bottom: 4px;
}

.symbol {
  font-size: 14px;
  color: #333;
}

.amount {
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.recharge-bonus {
  font-size: 12px;
  color: #ff4d4f;
}

.hot-tag {
  position: absolute;
  top: -6px;
  right: -6px;
  background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
  color: #fff;
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 10px;
}

.custom-section {
  background: #fff;
  margin-top: 12px;
  padding: 16px;
}

.custom-input-wrapper {
  display: flex;
  align-items: center;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 12px 16px;
}

.custom-prefix {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-right: 8px;
}

.custom-input {
  flex: 1;
  font-size: 20px;
  font-weight: 600;
}

.tips-section {
  padding: 20px 16px;
}

.tips-title {
  font-size: 14px;
  font-weight: 600;
  color: #666;
  margin-bottom: 12px;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tips-item {
  font-size: 12px;
  color: #999;
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
  padding-bottom: calc(12px + constant(safe-area-inset-bottom));
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
}

.pay-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.pay-label {
  font-size: 14px;
  color: #333;
}

.pay-amount {
  font-size: 24px;
  font-weight: 700;
  color: #ff4d4f;
}

.recharge-btn {
  padding: 12px 40px;
  background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
  color: #fff;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;

  &.disabled {
    background: #ddd;
  }
}
</style>
