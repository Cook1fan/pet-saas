<template>
  <view id="app"></view>
</template>

<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

// 全局数据
const globalData = {
  currentTenantId: null as string | null,
  tenantName: null as string | null,
  userInfo: null as any
}

// 获取 app 实例并设置全局数据
const app = getApp()
if (app) {
  (app as any).globalData = globalData
}

onLaunch((options) => {
  console.log('App Launch')

  // 解析二维码 scene 参数
  const scene = decodeURIComponent(options.query?.scene || '')
  if (scene && scene.startsWith('tenant_')) {
    const tenantId = scene.split('_')[1]
    const appInstance = getApp()
    if (appInstance) {
      (appInstance as any).globalData.currentTenantId = tenantId
    }
  }
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style lang="scss">
/* 全局样式 */

/* 重置样式 */
page, view {
  box-sizing: border-box;
}

page {
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 通用工具类 */
.text-center {
  text-align: center;
}

.text-left {
  text-align: left;
}

.text-right {
  text-align: right;
}

.flex {
  display: flex;
}

.flex-column {
  flex-direction: column;
}

.flex-center {
  display: flex;
  justify-content: center;
  align-items: center;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.flex-around {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.flex-wrap {
  flex-wrap: wrap;
}

.flex-1 {
  flex: 1;
}
</style>
