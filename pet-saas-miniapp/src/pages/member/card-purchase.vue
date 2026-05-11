<template>
  <view class="card-purchase">
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">‹</view>
      <view class="nav-title">购买次卡</view>
    </view>

    <view class="rule-list">
      <view
        class="rule-item"
        :class="{ selected: selectedRuleId === item.id }"
        v-for="item in ruleList"
        :key="item.id"
        @tap="selectRule(item)"
      >
        <view class="rule-header">
          <view class="rule-name">{{ item.name }}</view>
          <view class="rule-price">
            <text class="symbol">¥</text>
            <text class="amount">{{ item.price }}</text>
          </view>
        </view>
        <view class="rule-info">
          <view class="info-item">
            <text class="label">次数：</text>
            <text class="value">{{ item.totalTimes }}次</text>
          </view>
          <view class="info-item" v-if="item.validDays">
            <text class="label">有效期：</text>
            <text class="value">{{ item.validDays }}天</text>
          </view>
        </view>
        <view class="selected-icon" v-if="selectedRuleId === item.id">
          <text>✓</text>
        </view>
      </view>
    </view>

    <view class="empty-state" v-if="ruleList.length === 0 && !loading">
      <text class="empty-icon">🎫</text>
      <text class="empty-text">暂无可用次卡</text>
    </view>

    <view class="bottom-bar" v-if="selectedRuleId">
      <view class="pay-info">
        <text class="pay-label">应付金额</text>
        <text class="pay-amount">¥{{ selectedRule?.price || 0 }}</text>
      </view>
      <view class="pay-btn" :class="{ disabled: paying }" @tap="handlePurchase">
        {{ paying ? '支付中...' : '立即购买' }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCardRuleList, purchaseCard, type CardRule } from '@/api/index'
import { showToast, showLoading, hideLoading } from '@/utils/interaction'

const loading = ref(false)
const paying = ref(false)
const ruleList = ref<CardRule[]>([])
const selectedRuleId = ref<number | null>(null)

const selectedRule = ref<CardRule | null>(null)

async function loadRuleList() {
  loading.value = true
  try {
    const res = await getCardRuleList()
    if (res.code === 200 && res.data) {
      ruleList.value = res.data
    }
  } catch (error) {
    console.error('加载次卡规则失败:', error)
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

function selectRule(item: CardRule) {
  selectedRuleId.value = item.id
  selectedRule.value = item
}

async function handlePurchase() {
  if (!selectedRuleId.value || paying.value) {
    return
  }

  paying.value = true
  showLoading('支付中...')

  try {
    const res = await purchaseCard({ cardRuleId: selectedRuleId.value })
    if (res.code === 200 && res.data) {
      // 处理微信支付
      if (res.data.wechatPayParams) {
        const payParams = res.data.wechatPayParams
        uni.requestPayment({
          provider: 'wxpay',
          timeStamp: payParams.timeStamp,
          nonceStr: payParams.nonceStr,
          package: payParams.package,
          signType: payParams.signType,
          paySign: payParams.paySign,
          success: () => {
            showToast('购买成功', 'success')
            setTimeout(() => {
              goBack()
            }, 1500)
          },
          fail: (err) => {
            console.error('支付失败:', err)
            showToast('支付失败')
          }
        })
      } else {
        // 无支付参数时直接提示成功
        showToast('购买成功', 'success')
        setTimeout(() => {
          goBack()
        }, 1500)
      }
    } else {
      showToast(res.message || '购买失败')
    }
  } catch (error) {
    console.error('购买失败:', error)
    showToast('购买失败')
  } finally {
    hideLoading()
    paying.value = false
  }
}

function goBack() {
  uni.navigateBack()
}

onMounted(() => {
  loadRuleList()
})
</script>

<style lang="scss" scoped>
.card-purchase {
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

.rule-list {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rule-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  position: relative;
  border: 2px solid transparent;
  transition: all 0.3s;

  &.selected {
    border-color: #ff4d4f;
    background: #fff5f5;
  }
}

.rule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.rule-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.rule-price {
  .symbol {
    font-size: 14px;
    color: #ff4d4f;
  }

  .amount {
    font-size: 24px;
    font-weight: 700;
    color: #ff4d4f;
  }
}

.rule-info {
  display: flex;
  gap: 24px;
}

.info-item {
  display: flex;
  align-items: center;

  .label {
    font-size: 14px;
    color: #909399;
  }

  .value {
    font-size: 14px;
    color: #606266;
  }
}

.selected-icon {
  position: absolute;
  right: 0;
  top: 0;
  width: 36px;
  height: 36px;
  background: #ff4d4f;
  border-radius: 0 12px 0 12px;
  display: flex;
  align-items: center;
  justify-content: center;

  text {
    color: #fff;
    font-size: 16px;
    font-weight: 700;
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
  color: #909399;
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
  color: #303133;
}

.pay-amount {
  font-size: 24px;
  font-weight: 700;
  color: #ff4d4f;
}

.pay-btn {
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
