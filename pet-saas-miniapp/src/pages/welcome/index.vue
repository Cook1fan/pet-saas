<template>
  <view class="welcome-container">
    <!-- 状态栏占位 -->
    <view class="status-bar-placeholder"></view>

    <!-- 门店信息 -->
    <view class="shop-section">
      <image class="shop-logo" :src="shopInfo?.shopLogo || defaultLogo" mode="aspectFill" />
      <view class="shop-name">{{ shopInfo?.shopName || '欢迎来到宠物店' }}</view>
      <view class="welcome-text">欢迎您的到来~</view>
    </view>

    <!-- 门店简介 -->
    <view class="shop-desc" v-if="shopInfo">
      <view class="desc-title">门店简介</view>
      <view class="desc-text">{{ shopDesc }}</view>
    </view>

    <!-- 门店数据 -->
    <view class="shop-stats" v-if="shopInfo">
      <view class="stat-item">
        <text class="stat-icon">🐱</text>
        <text class="stat-text">已服务 500+ 宠友</text>
      </view>
      <view class="stat-item">
        <text class="stat-icon">🎁</text>
        <text class="stat-text">100+ 活动已举办</text>
      </view>
    </view>

    <!-- 分割线 -->
    <view class="divider"></view>

    <!-- 协议 -->
    <view class="agreement-section">
      <view class="agreement-checkbox" @tap="toggleAgreement">
        <text class="checkbox" :class="{ checked: agreeProtocol }">
          {{ agreeProtocol ? '✓' : '' }}
        </text>
        <text class="agreement-text">
          我已阅读并同意
          <text class="link" @tap.stop="navigateToProtocol('user')">《用户协议》</text>
          和
          <text class="link" @tap.stop="navigateToProtocol('privacy')">《隐私政策》</text>
        </text>
      </view>
    </view>

    <!-- 登录按钮 -->
    <view class="login-btn-section">
      <button class="login-btn" :class="{ disabled: !agreeProtocol }" @tap="handleLogin" :disabled="!agreeProtocol">
        一键微信登录
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onLoad, onMounted } from 'vue'
import { useMemberStore } from '@/stores/member'
import { wxLogin } from '@/api/user'
import { getHomeData } from '@/api/index'
import type { ShopInfo } from '@/types'

const memberStore = useMemberStore()

const agreeProtocol = ref(false)
const shopInfo = ref<ShopInfo | null>(null)
const defaultLogo = '/static/default-shop-logo.png'
const shopDesc = ref('专业宠物洗护、寄养，品质商品、贴心服务')

let tenantIdFromQr: string | null = null

onLoad((options) => {
  if (options.tenantId) {
    tenantIdFromQr = options.tenantId
    loadShopInfo(tenantIdFromQr)
  } else {
    checkLoginStatus()
  }
})

onMounted(() => {
  checkLoginStatus()
})

function toggleAgreement() {
  agreeProtocol.value = !agreeProtocol.value
}

function navigateToProtocol(type: 'user' | 'privacy') {
  console.log('Navigate to protocol:', type)
  uni.showToast({ title: '协议开发中', icon: 'none' })
}

async function checkLoginStatus() {
  const token = uni.getStorageSync('token')
  if (token) {
    const currentShop = uni.getStorageSync('currentShop')
    if (currentShop) {
      uni.switchTab({ url: '/pages/home/index' })
    } else {
      uni.redirectTo({ url: '/pages/shop/select' })
    }
  }
}

async function loadShopInfo(tenantId: string) {
  try {
    const res = await getHomeData()
    if (res.code === 200 && res.data) {
      shopInfo.value = res.data.shopInfo
    }
  } catch (error) {
    console.error('Load shop info failed:', error)
  }
}

async function handleLogin() {
  if (!agreeProtocol.value) {
    uni.showToast({ title: '请先阅读并同意协议', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '登录中...' })

    // 获取用户信息
    const profileRes: any = await new Promise((resolve, reject) => {
      uni.getUserProfile({
        desc: '用于完善会员资料',
        success: resolve,
        fail: reject
      })
    })

    // 获取登录 code
    const loginRes: any = await new Promise((resolve, reject) => {
      uni.login({
        provider: 'weixin',
        success: resolve,
        fail: reject
      })
    })

    // 调用后端登录接口
    const loginData: any = {
      code: loginRes.code,
      encryptedData: profileRes.encryptedData,
      iv: profileRes.iv
    }

    if (tenantIdFromQr) {
      loginData.tenantId = tenantIdFromQr
    }

    const res = await wxLogin(loginData)

    if (res.code === 200 && res.data) {
      // 保存 token 和用户信息
      memberStore.setToken(res.data.token)
      memberStore.setMemberInfo(res.data.member)

      uni.hideLoading()

      // 判断是否需要绑定门店
      if (tenantIdFromQr || res.data.member.shopBound) {
        uni.switchTab({ url: '/pages/home/index' })
      } else {
        uni.redirectTo({ url: '/pages/shop/select' })
      }
    }
  } catch (error: any) {
    uni.hideLoading()
    console.error('Login failed:', error)
    uni.showToast({
      title: error.message || '登录失败，请重试',
      icon: 'none'
    })
  }
}
</script>

<style lang="scss" scoped>
.welcome-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #fff5f5 0%, #ffffff 100%);
  padding: 0 32px;
  box-sizing: border-box;
}

.status-bar-placeholder {
  height: var(--status-bar-height, 44px);
}

.shop-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 40px;
  padding-bottom: 30px;
}

.shop-logo {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: #f0f0f0;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.shop-name {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.welcome-text {
  font-size: 16px;
  color: #ff4d4f;
}

.shop-desc {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.desc-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.desc-text {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.shop-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 30px;
}

.stat-item {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.stat-icon {
  font-size: 20px;
}

.stat-text {
  font-size: 14px;
  color: #333;
}

.divider {
  height: 1px;
  background: #f0f0f0;
  margin: 30px 0;
}

.agreement-section {
  margin-bottom: 30px;
}

.agreement-checkbox {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.checkbox {
  width: 20px;
  height: 20px;
  border: 2px solid #ddd;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #fff;
  flex-shrink: 0;
  margin-top: 2px;

  &.checked {
    background: #ff4d4f;
    border-color: #ff4d4f;
  }
}

.agreement-text {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}

.link {
  color: #ff4d4f;
}

.login-btn-section {
  padding-bottom: 40px;
}

.login-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff6b6b 100%);
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);

  &.disabled {
    background: #ddd;
    box-shadow: none;
  }
}
</style>
