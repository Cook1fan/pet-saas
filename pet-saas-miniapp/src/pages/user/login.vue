<template>
  <view class="login-page">
    <view class="login-header">
      <view class="logo-placeholder">
        <text class="logo-emoji">🐾</text>
      </view>
      <text class="app-name">宠物门店SaaS</text>
      <text class="app-desc">让养宠生活更简单</text>
    </view>
    <view class="login-content">
      <button
        class="login-btn"
        type="primary"
        :loading="logging"
        @tap="handleWxLogin"
      >
        <text class="btn-icon">微</text>
        <text>微信授权登录</text>
      </button>
      <text class="login-tip">登录即表示同意《用户协议》和《隐私政策》</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { wxLogin } from '@/api/user'

const logging = ref(false)

async function handleWxLogin() {
  if (logging.value) return

  logging.value = true

  try {
    // 1. 调用微信登录获取 code
    const loginRes = await new Promise<WechatMiniprogram.LoginSuccessCallbackResult>((resolve, reject) => {
      wx.login({
        success: resolve,
        fail: reject
      })
    })

    if (!loginRes.code) {
      throw new Error('获取微信登录code失败')
    }

    // 2. 发送到后端获取 token
    const res = await wxLogin({
      code: loginRes.code
    })

    if (res.code === 200 && res.data) {
      // 3. 保存 token 和用户信息
      if (res.data.token) {
        uni.setStorageSync('token', res.data.token)
        uni.setStorageSync('member', res.data.member)

        uni.showToast({
          title: '登录成功',
          icon: 'success'
        })

        // 4. 跳转回上一页或首页
        setTimeout(() => {
          const pages = getCurrentPages()
          if (pages.length > 1) {
            uni.navigateBack()
          } else {
            uni.switchTab({
              url: '/pages/index/index'
            })
          }
        }, 1500)
      } else {
        // 没有 token，说明是首次访问，需要选择门店
        // 保存 member 信息
        uni.setStorageSync('member', res.data.member)

        uni.showToast({
          title: '请选择门店',
          icon: 'none'
        })

        // 跳转到选择门店页面
        setTimeout(() => {
          uni.redirectTo({
            url: '/pages/user/select-shop'
          })
        }, 1500)
      }
    } else {
      uni.showToast({
        title: res.message || '登录失败',
        icon: 'none'
      })
    }
  } catch (error: any) {
    console.error('微信登录失败:', error)
    uni.showToast({
      title: error.message || '登录失败',
      icon: 'none'
    })
  } finally {
    logging.value = false
  }
}
</script>

<style lang="scss">
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 120rpx 60rpx 100rpx;
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 100rpx;
}

.logo-placeholder {
  width: 160rpx;
  height: 160rpx;
  background-color: #fff;
  border-radius: 32rpx;
  margin-bottom: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-emoji {
  font-size: 80rpx;
}

.app-name {
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 16rpx;
}

.app-desc {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.login-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.login-btn {
  width: 100%;
  height: 96rpx;
  background-color: #fff;
  border-radius: 48rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 32rpx;

  &::after {
    border: none;
  }
}

.btn-icon {
  width: 48rpx;
  height: 48rpx;
  background-color: #07c160;
  color: #fff;
  font-size: 24rpx;
  font-weight: bold;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}

.login-tip {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.6);
  text-align: center;
}
</style>
