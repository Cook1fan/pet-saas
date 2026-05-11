<template>
  <view class="recharge-container">
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">‹</view>
      <view class="nav-title">储值充值</view>
    </view>

    <view class="balance-section" v-if="accountInfo">
      <text class="balance-label">当前余额</text>
      <text class="balance-value">¥{{ accountInfo.balance }}</text>
    </view>

    <view class="recharge-section">
      <view class="section-title">选择套餐</view>
      <view class="recharge-list" v-if="ruleList.length > 0">
        <view
          class="recharge-item"
          :class="{ selected: selectedRuleId === item.id }"
          v-for="item in ruleList"
          :key="item.id"
          @tap="selectRule(item)"
        >
          <view class="recharge-amount">
            <text class="symbol">¥</text>
            <text class="amount">{{ item.rechargeAmount }}</text>
          </view>
          <view class="recharge-bonus" v-if="item.bonusAmount > 0">
            赠 ¥{{ item.bonusAmount }}
          </view>
          <view class="rule-name">{{ item.name }}</view>
          <view class="selected-icon" v-if="selectedRuleId === item.id">
            <text>✓</text>
          </view>
        </view>
      </view>

      <view class="empty-state" v-else-if="!loading">
        <text class="empty-icon">💳</text>
        <text class="empty-text">暂无套餐</text>
      </view>
    </view>

    <view class="tips-section">
      <view class="tips-title">充值说明</view>
      <view class="tips-list">
        <view class="tips-item">1. 储值金额可用于门店消费</view>
        <view class="tips-item">2. 赠送金额与储值金额同效</view>
        <view class="tips-item">3. 储值后不支持退款，请谨慎操作</view>
      </view>
    </view>

    <view class="bottom-bar" v-if="selectedRule">
      <view class="pay-info">
        <text class="pay-label">充值金额</text>
        <text class="pay-amount">¥{{ selectedRule.rechargeAmount }}</text>
        <text class="pay-bonus" v-if="selectedRule.bonusAmount > 0">
          赠¥{{ selectedRule.bonusAmount }}
        </text>
      </view>
      <view class="recharge-btn" :class="{ disabled: paying || loading }" @tap="handleRecharge">
        {{ paying ? '支付中...' : loading ? '加载中...' : '立即充值' }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getRechargeRuleList, purchaseRecharge, getMyRechargeAccount, type RechargeRule, type UserRechargeAccount } from '@/api/index'
import { showToast, showLoading, hideLoading } from '@/utils/interaction'

const loading = ref(false)
const paying = ref(false)
const ruleList = ref<RechargeRule[]>([])
const accountInfo = ref<UserRechargeAccount | null>(null)
const selectedRuleId = ref<number | null>(null)

const selectedRule = computed(() => {
  return ruleList.value.find(r => r.id === selectedRuleId.value) || null
})

async function loadData() {
  loading.value = true
  try {
    const [ruleRes, accountRes] = await Promise.all([
      getRechargeRuleList(),
      getMyRechargeAccount()
    ])

    if (ruleRes.code === 200 && ruleRes.data) {
      ruleList.value = ruleRes.data
      if (ruleList.value.length > 0) {
        selectedRuleId.value = ruleList.value[0].id
      }
    }

    if (accountRes.code === 200 && accountRes.data) {
      accountInfo.value = accountRes.data
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

function selectRule(item: RechargeRule) {
  selectedRuleId.value = item.id
}

async function handleRecharge() {
  if (!selectedRuleId.value || paying.value || loading.value) {
    return
  }

  paying.value = true
  showLoading('支付中...')

  try {
    const res = await purchaseRecharge({ rechargeRuleId: selectedRuleId.value })
    if (res.code === 200 && res.data) {
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
            showToast('充值成功', 'success')
            setTimeout(() => {
              loadData()
              setTimeout(() => {
                goBack()
              }, 500)
            }, 1500)
          },
          fail: (err) => {
            console.error('支付失败:', err)
            showToast('支付失败')
          }
        })
      } else {
        showToast('充值成功', 'success')
        setTimeout(() => {
          loadData()
          setTimeout(() => {
            goBack()
          }, 500)
        }, 1500)
      }
    } else {
      showToast(res.message || '充值失败')
    }
  } catch (error) {
    console.error('充值失败:', error)
    showToast('充值失败')
  } finally {
    hideLoading()
    paying.value = false
  }
}

function goBack() {
  uni.navigateBack()
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.recharge-container {
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
  color: #303133;
  margin-bottom: 16px;
}

.recharge-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.recharge-item {
  position: relative;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  padding: 16px 12px;
  text-align: center;

  &.selected {
    border-color: #ff4d4f;
    background: #fff5f5;
  }
}

.recharge-amount {
  margin-bottom: 4px;
}

.symbol {
  font-size: 14px;
  color: #303133;
}

.amount {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.recharge-bonus {
  font-size: 12px;
  color: #ff4d4f;
  margin-bottom: 4px;
}

.rule-name {
  font-size: 12px;
  color: #909399;
}

.selected-icon {
  position: absolute;
  right: 0;
  top: 0;
  width: 28px;
  height: 28px;
  background: #ff4d4f;
  border-radius: 0 10px 0 10px;
  display: flex;
  align-items: center;
  justify-content: center;

  text {
    color: #fff;
    font-size: 14px;
    font-weight: 700;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
  gap: 12px;
}

.empty-icon {
  font-size: 48px;
}

.empty-text {
  font-size: 14px;
  color: #909399;
}

.tips-section {
  padding: 20px 16px;
}

.tips-title {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 12px;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tips-item {
  font-size: 12px;
  color: #909399;
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
  color: #303133;
}

.pay-amount {
  font-size: 24px;
  font-weight: 700;
  color: #ff4d4f;
}

.pay-bonus {
  font-size: 12px;
  color: #67c23a;
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
