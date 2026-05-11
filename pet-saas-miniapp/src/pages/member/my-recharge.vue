<template>
  <view class="my-recharge">
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">‹</view>
      <view class="nav-title">我的储值</view>
    </view>

    <view class="balance-card" v-if="accountInfo">
      <view class="balance-label">当前余额</view>
      <view class="balance-value">¥{{ accountInfo.balance }}</view>
      <view class="balance-stats">
        <view class="stat-item">
          <text class="stat-value">¥{{ accountInfo.totalRecharge }}</text>
          <text class="stat-label">累计储值</text>
        </view>
        <view class="divider"></view>
        <view class="stat-item">
          <text class="stat-value">¥{{ accountInfo.totalConsume || 0 }}</text>
          <text class="stat-label">累计消费</text>
        </view>
      </view>
    </view>

    <view class="action-section">
      <view class="action-btn" @tap="navigateToPurchase">
        <text class="icon">💰</text>
        <text>立即储值</text>
      </view>
    </view>

    <view class="record-section">
      <view class="section-header">
        <text class="section-title">储值记录</text>
        <view class="filter-tabs">
          <text
            class="tab-item"
            :class="{ active: currentTab === 'all' }"
            @tap="switchTab('all')"
          >
            全部
          </text>
          <text
            class="tab-item"
            :class="{ active: currentTab === 'recharge' }"
            @tap="switchTab('recharge')"
          >
            储值
          </text>
          <text
            class="tab-item"
            :class="{ active: currentTab === 'consume' }"
            @tap="switchTab('consume')"
          >
            消费
          </text>
        </view>
      </view>

      <view class="record-list" v-if="recordList.length > 0">
        <view class="record-item" v-for="record in recordList" :key="record.id">
          <view class="record-left">
            <view class="record-type">
              <text
                :class="record.type === 'recharge' ? 'type-recharge' : record.type === 'consume' ? 'type-consume' : 'type-refund'"
              >
                {{ record.type === 'recharge' ? '储值' : record.type === 'consume' ? '消费' : '退款' }}
              </text>
            </view>
            <view class="record-time">{{ record.createTime }}</view>
            <view class="record-remark" v-if="record.remark">{{ record.remark }}</view>
          </view>
          <view class="record-right">
            <text
              class="amount"
              :class="record.amount > 0 ? 'amount-positive' : 'amount-negative'"
            >
              {{ record.amount > 0 ? '+' : '' }}¥{{ record.amount }}
            </text>
            <text class="balance">余额: ¥{{ record.balanceAfter }}</text>
          </view>
        </view>
      </view>

      <view class="empty-state" v-else>
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无记录</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyRechargeAccount, getRechargeRecordList, type UserRechargeAccount, type RechargeRecord } from '@/api/index'
import { showLoading, hideLoading } from '@/utils/interaction'

const accountInfo = ref<UserRechargeAccount | null>(null)
const recordList = ref<RechargeRecord[]>([])
const currentTab = ref<string>('all')

async function loadAccountInfo() {
  try {
    const res = await getMyRechargeAccount()
    if (res.code === 200 && res.data) {
      accountInfo.value = res.data
    }
  } catch (error) {
    console.error('加载账户信息失败:', error)
  }
}

async function loadRecords() {
  try {
    const params = {
      page: 1,
      pageSize: 50,
      type: currentTab.value === 'all' ? undefined : currentTab.value
    }
    const res = await getRechargeRecordList(params)
    if (res.code === 200 && res.data) {
      recordList.value = res.data.list
    }
  } catch (error) {
    console.error('加载记录失败:', error)
  }
}

function switchTab(tab: string) {
  currentTab.value = tab
  loadRecords()
}

function navigateToPurchase() {
  uni.navigateTo({ url: '/pages/member/recharge-purchase' })
}

function goBack() {
  uni.navigateBack()
}

onMounted(async () => {
  showLoading()
  try {
    await Promise.all([loadAccountInfo(), loadRecords()])
  } finally {
    hideLoading()
  }
})
</script>

<style lang="scss" scoped>
.my-recharge {
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

.balance-card {
  background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
  margin: 16px;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
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
  margin-bottom: 20px;
}

.balance-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.divider {
  width: 1px;
  height: 32px;
  background: rgba(255, 255, 255, 0.3);
}

.action-section {
  padding: 0 16px;
  margin-bottom: 16px;
}

.action-btn {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 500;
  color: #ff4d4f;
}

.action-btn .icon {
  font-size: 20px;
}

.record-section {
  background: #fff;
  margin: 0 16px;
  border-radius: 12px;
  overflow: hidden;
}

.section-header {
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  display: block;
  margin-bottom: 12px;
}

.filter-tabs {
  display: flex;
  gap: 24px;
}

.tab-item {
  font-size: 14px;
  color: #606266;
  padding: 4px 0;
  position: relative;

  &.active {
    color: #ff4d4f;
    font-weight: 500;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 2px;
      background: #ff4d4f;
      border-radius: 1px;
    }
  }
}

.record-list {
  padding: 0 16px;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }
}

.record-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.record-type {
  .type-recharge {
    color: #67c23a;
    font-weight: 500;
  }

  .type-consume {
    color: #ff4d4f;
    font-weight: 500;
  }

  .type-refund {
    color: #e6a23c;
    font-weight: 500;
  }
}

.record-time {
  font-size: 12px;
  color: #909399;
}

.record-remark {
  font-size: 12px;
  color: #c0c4cc;
}

.record-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.amount {
  font-size: 16px;
  font-weight: 600;

  &.amount-positive {
    color: #67c23a;
  }

  &.amount-negative {
    color: #ff4d4f;
  }
}

.balance {
  font-size: 12px;
  color: #909399;
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
</style>
