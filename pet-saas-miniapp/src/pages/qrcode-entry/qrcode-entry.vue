<template>
  <view class="qrcode-entry-page">
    <!-- 加载中 -->
    <view v-if="isLoading" class="loading-section">
      <view class="loading-spinner"></view>
      <text class="loading-text">正在验证二维码...</text>
    </view>

    <!-- 二维码无效 -->
    <view v-else-if="!tenantInfo || !tenantInfo.valid" class="error-section">
      <view class="error-icon">!</view>
      <text class="error-title">二维码无效</text>
      <text class="error-desc">该二维码已过期或不存在</text>
      <button class="btn-back" @tap="goHome">返回首页</button>
    </view>

    <!-- 正常显示 -->
    <view v-else class="content-section">
      <!-- 店铺头部信息 -->
      <view class="shop-header">
        <image
          class="shop-logo"
          :src="tenantInfo.tenantLogo || '/static/images/default-shop.png'"
          mode="aspectFill"
        />
        <view class="shop-info">
          <text class="shop-name">{{ tenantInfo.tenantName }}</text>
          <text class="shop-welcome">欢迎您</text>
        </view>
      </view>

      <!-- 已登录但未绑定该店铺 -->
      <view v-if="hasLogin && !isBound" class="bind-section">
        <view class="bind-tip">
          <text class="tip-icon">🏪</text>
          <text class="tip-text">检测到您已登录，是否绑定到 {{ tenantInfo.tenantName }}？</text>
        </view>
        <button class="btn-bind" type="primary" @tap="handleBind">绑定到该店铺</button>
        <text class="link-text" @tap="goHome">暂不绑定，直接进入</text>
      </view>

      <!-- 未登录 - 登录引导 -->
      <view v-else-if="!hasLogin" class="login-section">
        <view class="welcome-card">
          <text class="welcome-title">欢迎来到 {{ tenantInfo.tenantName }}</text>
          <text class="welcome-desc">登录后即可享受更多服务</text>
        </view>

        <!-- 微信手机号一键登录 -->
        <button class="btn-wechat-phone" type="primary" @tap="handleWechatPhoneLogin">
          <text class="wechat-icon-text">V</text>
          <text>微信手机号一键登录</text>
        </button>

        <!-- 分割线 -->
        <view class="divider">
          <view class="divider-line"></view>
          <text class="divider-text">或</text>
          <view class="divider-line"></view>
        </view>

        <!-- 手机号登录 -->
        <view class="phone-login">
          <view class="phone-input-wrap">
            <input
              type="number"
              class="phone-input"
              placeholder="请输入手机号"
              maxlength="11"
              :value="phone"
              @input="onPhoneInput"
            />
          </view>

          <view class="code-input-wrap">
            <input
              type="number"
              class="code-input"
              placeholder="验证码"
              maxlength="6"
              :value="verifyCode"
              @input="onCodeInput"
            />
            <button
              class="btn-send-code"
              :disabled="codeBtnDisabled"
              @tap="handleSendCode"
            >
              {{ codeBtnText }}
            </button>
          </view>

          <button
            class="btn-login"
            type="primary"
            :disabled="!canPhoneLogin"
            @tap="handlePhoneLogin"
          >
            登录/注册
          </button>
        </view>

        <!-- 游客体验 -->
        <view class="guest-section">
          <text class="guest-text" @tap="handleGuestAccess">暂不登录，游客体验</text>
        </view>
      </view>

      <!-- 已绑定 - 直接进入 -->
      <view v-else class="bound-section">
        <view class="success-icon">✓</view>
        <text class="success-text">已绑定到 {{ tenantInfo.tenantName }}</text>
        <text class="redirect-text">正在跳转店铺首页...</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onLoad } from '@dcloudio/uni-app'
import { verifyQrCode, bindTenant } from '@/api/qrcode'
import { getMemberInfo } from '@/api/user'
import request from '@/utils/request'

interface TenantInfo {
  valid: boolean
  tenantId: number
  tenantName: string
  tenantLogo: string
  isBound: boolean
}

const isLoading = ref(true)
const tenantInfo = ref<TenantInfo | null>(null)
const hasLogin = ref(false)
const isBound = ref(false)

// 手机号登录相关
const phone = ref('')
const verifyCode = ref('')
const codeBtnText = ref('发送验证码')
const codeBtnDisabled = ref(false)
const showCodeInput = ref(false)
const countdown = ref(0)

// 倒计时计时器
let countdownTimer: ReturnType<typeof setInterval> | null = null

const canPhoneLogin = computed(() => {
  return phone.value.length === 11 && verifyCode.value.length === 6
})

onLoad((options: any) => {
  const scene = decodeURIComponent(options.scene || '')
  if (scene && scene.startsWith('tenant_')) {
    const tenantId = scene.split('_')[1]
    verifyQrCodeScene(tenantId)
  } else {
    // 没有 scene 参数，检查 globalData 中的 tenantId
    const app = getApp()
    const currentTenantId = (app as any)?.globalData?.currentTenantId
    if (currentTenantId) {
      verifyQrCodeScene(currentTenantId)
    } else {
      isLoading.value = false
      tenantInfo.value = { valid: false, tenantId: 0, tenantName: '', tenantLogo: '', isBound: false }
    }
  }
})

async function verifyQrCodeScene(tenantId: string) {
  try {
    const res = await verifyQrCode(`tenant_${tenantId}`)
    if (res.code === 200 && res.data) {
      tenantInfo.value = res.data

      // 检查登录状态
      const token = uni.getStorageSync('token')
      hasLogin.value = !!token

      if (hasLogin.value) {
        // 已登录，检查是否已绑定该店铺
        isBound.value = res.data.isBound
        if (isBound.value) {
          // 已绑定，直接跳转首页
          setTimeout(() => {
            goHome()
          }, 1500)
        }
      }
    } else {
      tenantInfo.value = { valid: false, tenantId: 0, tenantName: '', tenantLogo: '', isBound: false }
    }
  } catch (e: any) {
    console.error('验证二维码失败:', e)
    uni.showToast({
      title: e.message || '二维码验证失败',
      icon: 'none'
    })
    tenantInfo.value = { valid: false, tenantId: 0, tenantName: '', tenantLogo: '', isBound: false }
  } finally {
    isLoading.value = false
  }
}

async function handleBind() {
  if (!tenantInfo.value) return

  try {
    const member = uni.getStorageSync('member')
    if (!member || !member.id) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      return
    }

    const res = await bindTenant({
      tenantId: tenantInfo.value.tenantId,
      memberId: member.id
    })

    if (res.code === 200) {
      uni.showToast({
        title: '绑定成功',
        icon: 'success'
      })
      isBound.value = true
      setTimeout(() => {
        goHome()
      }, 1500)
    }
  } catch (e: any) {
    uni.showToast({
      title: e.message || '绑定失败',
      icon: 'none'
    })
  }
}

async function handleWechatPhoneLogin() {
  if (!tenantInfo.value) return

  try {
    // 获取手机号授权
    const res = await new Promise<any>((resolve, reject) => {
      wx.getPhoneNumber({
        getPhoneNumber: async (e: any) => {
          if (e.code) {
            // 调用后端接口用 code 登录
            try {
              const loginRes = await request.post('/api/user/auth/wechat-phone-login', {
                tenantId: tenantInfo.value?.tenantId,
                code: e.code
              })
              resolve(loginRes)
            } catch (err) {
              reject(err)
            }
          } else {
            reject(new Error('获取手机号失败'))
          }
        },
        fail: (err: any) => {
          reject(err)
        }
      })
    })

    if (res.code === 200 && res.data) {
      handleLoginSuccess(res.data)
    }
  } catch (e: any) {
    console.error('微信手机号登录失败:', e)
    uni.showToast({
      title: e.message || '登录失败',
      icon: 'none'
    })
  }
}

function onPhoneInput(e: any) {
  phone.value = e.detail.value
  if (phone.value.length === 11) {
    showCodeInput.value = true
  }
}

function onCodeInput(e: any) {
  verifyCode.value = e.detail.value
}

async function handleSendCode() {
  if (codeBtnDisabled.value || phone.value.length !== 11) return

  try {
    const res = await request.post('/api/user/auth/send-code', {
      phone: phone.value
    })

    if (res.code === 200) {
      uni.showToast({
        title: '验证码已发送',
        icon: 'success'
      })
      startCountdown()
    }
  } catch (e: any) {
    uni.showToast({
      title: e.message || '发送失败',
      icon: 'none'
    })
  }
}

function startCountdown() {
  countdown.value = 60
  codeBtnDisabled.value = true
  codeBtnText.value = '60s'

  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      if (countdownTimer) clearInterval(countdownTimer)
      codeBtnText.value = '发送验证码'
      codeBtnDisabled.value = false
    } else {
      codeBtnText.value = `${countdown.value}s`
    }
  }, 1000)
}

async function handlePhoneLogin() {
  if (!canPhoneLogin.value || !tenantInfo.value) return

  try {
    const res = await request.post('/api/user/auth/quick-login', {
      tenantId: tenantInfo.value.tenantId,
      phone: phone.value,
      code: verifyCode.value
    })

    if (res.code === 200 && res.data) {
      handleLoginSuccess(res.data)
    }
  } catch (e: any) {
    uni.showToast({
      title: e.message || '登录失败',
      icon: 'none'
    })
  }
}

function handleLoginSuccess(data: any) {
  // 保存 token 和用户信息
  if (data.token) {
    uni.setStorageSync('token', data.token)
  }
  if (data.memberInfo) {
    uni.setStorageSync('member', data.memberInfo)
  }

  // 绑定店铺
  if (tenantInfo.value && data.memberInfo) {
    bindTenant({
      tenantId: tenantInfo.value.tenantId,
      memberId: data.memberInfo.id
    }).catch(() => {})
  }

  uni.showToast({
    title: data.isNewUser ? '注册成功' : '登录成功',
    icon: 'success'
  })

  setTimeout(() => {
    goHome()
  }, 1500)
}

function handleGuestAccess() {
  // 保存临时店铺信息到 globalData
  const app = getApp()
  if (tenantInfo.value && app) {
    (app as any).globalData.currentTenantId = tenantInfo.value.tenantId
    ;(app as any).globalData.tenantName = tenantInfo.value.tenantName
  }
  goHome()
}

function goHome() {
  uni.switchTab({
    url: '/pages/index/index'
  })
}
</script>

<style lang="scss" scoped>
.qrcode-entry-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
}

/* 加载中 */
.loading-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
}

.loading-spinner {
  width: 80rpx;
  height: 80rpx;
  border: 4rpx solid #f0f0f0;
  border-top-color: #ff4d4f;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  margin-top: 24rpx;
  font-size: 28rpx;
  color: #999;
}

/* 错误页面 */
.error-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  padding: 60rpx;
}

.error-icon {
  width: 120rpx;
  height: 120rpx;
  background-color: #f5f5f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60rpx;
  color: #999;
  margin-bottom: 32rpx;
}

.error-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.error-desc {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 48rpx;
}

.btn-back {
  width: 320rpx;
  height: 88rpx;
  background-color: #ff4d4f;
  border-radius: 44rpx;
  color: #fff;
  font-size: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 内容区域 */
.content-section {
  padding: 48rpx 32rpx;
}

/* 店铺头部 */
.shop-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 48rpx;
}

.shop-logo {
  width: 160rpx;
  height: 160rpx;
  border-radius: 24rpx;
  background-color: #f5f5f5;
  margin-bottom: 24rpx;
}

.shop-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.shop-name {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.shop-welcome {
  font-size: 28rpx;
  color: #999;
}

/* 绑定区域 */
.bind-section {
  background-color: #fff;
  border-radius: 24rpx;
  padding: 48rpx 32rpx;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.05);
}

.bind-tip {
  display: flex;
  align-items: center;
  background-color: #fff5f5;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 32rpx;
}

.tip-icon {
  font-size: 40rpx;
  margin-right: 16rpx;
}

.tip-text {
  flex: 1;
  font-size: 28rpx;
  color: #666;
  line-height: 1.5;
}

.btn-bind {
  width: 100%;
  height: 96rpx;
  background-color: #ff4d4f;
  border-radius: 48rpx;
  color: #fff;
  font-size: 32rpx;
  margin-bottom: 24rpx;
}

.link-text {
  text-align: center;
  font-size: 26rpx;
  color: #999;
  text-decoration: underline;
}

/* 已绑定区域 */
.bound-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
}

.success-icon {
  width: 120rpx;
  height: 120rpx;
  background-color: #52c41a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60rpx;
  color: #fff;
  margin-bottom: 24rpx;
}

.success-text {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.redirect-text {
  font-size: 28rpx;
  color: #999;
}

/* 登录区域 */
.login-section {
  background-color: #fff;
  border-radius: 24rpx;
  padding: 48rpx 32rpx;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.05);
}

.welcome-card {
  text-align: center;
  margin-bottom: 48rpx;
}

.welcome-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.welcome-desc {
  font-size: 28rpx;
  color: #999;
}

.btn-wechat-phone {
  width: 100%;
  height: 96rpx;
  background-color: #07c160;
  border-radius: 48rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #fff;
  margin-bottom: 32rpx;
}

.wechat-icon-text {
  width: 48rpx;
  height: 48rpx;
  background-color: #fff;
  color: #07c160;
  font-size: 28rpx;
  font-weight: bold;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}

.divider {
  display: flex;
  align-items: center;
  margin-bottom: 32rpx;
}

.divider-line {
  flex: 1;
  height: 1rpx;
  background-color: #f0f0f0;
}

.divider-text {
  padding: 0 24rpx;
  font-size: 26rpx;
  color: #999;
}

/* 手机号登录 */
.phone-login {
  display: flex;
  flex-direction: column;
}

.phone-input-wrap {
  margin-bottom: 24rpx;
}

.phone-input {
  width: 100%;
  height: 96rpx;
  background-color: #f5f5f5;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 30rpx;
}

.code-input-wrap {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.code-input {
  flex: 1;
  height: 96rpx;
  background-color: #f5f5f5;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 30rpx;
  margin-right: 16rpx;
}

.btn-send-code {
  width: 220rpx;
  height: 96rpx;
  background-color: #fff5f5;
  border-radius: 16rpx;
  color: #ff4d4f;
  font-size: 26rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.btn-send-code[disabled] {
  background-color: #f5f5f5;
  color: #999;
}

.btn-login {
  width: 100%;
  height: 96rpx;
  background-color: #ff4d4f;
  border-radius: 48rpx;
  color: #fff;
  font-size: 32rpx;
}

.btn-login[disabled] {
  background-color: #ccc;
}

/* 游客体验 */
.guest-section {
  margin-top: 32rpx;
  text-align: center;
}

.guest-text {
  font-size: 26rpx;
  color: #999;
  text-decoration: underline;
}
</style>