<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarWidth" class="layout-aside">
      <div class="logo">
        <h2 v-if="!appStore.sidebarCollapsed">宠物门店</h2>
        <h2 v-else>宠</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="appStore.sidebarCollapsed"
        :unique-opened="false"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/shop/dashboard">
          <el-icon><DataLine /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-sub-menu index="config">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>门店配置</span>
          </template>
          <el-menu-item index="/shop/config/info">门店信息</el-menu-item>
          <el-menu-item index="/shop/config/staff">员工账号</el-menu-item>
          <el-menu-item index="/shop/config/payment">支付配置</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/shop/member/list">
          <el-icon><User /></el-icon>
          <template #title>会员管理</template>
        </el-menu-item>
        <el-sub-menu index="recharge">
          <template #title>
            <el-icon><Wallet /></el-icon>
            <span>储值次卡</span>
          </template>
          <el-menu-item index="/shop/recharge/rule">储值规则</el-menu-item>
          <el-menu-item index="/shop/recharge/card-rule">次卡规则</el-menu-item>
          <el-menu-item index="/shop/recharge/member-account">会员储值账户</el-menu-item>
          <el-menu-item index="/shop/recharge/recharge-record">储值记录</el-menu-item>
          <el-menu-item index="/shop/recharge/card-record">次卡记录</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/shop/cashier/index">
          <el-icon><CreditCard /></el-icon>
          <template #title>开单收银</template>
        </el-menu-item>
        <el-menu-item index="/shop/order/list">
          <el-icon><Document /></el-icon>
          <template #title>订单管理</template>
        </el-menu-item>
        <el-sub-menu index="goods">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>商品管理</span>
          </template>
          <el-menu-item index="/shop/goods/list">商品列表</el-menu-item>
          <el-menu-item index="/shop/goods/stock-record">库存流水</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="marketing">
          <template #title>
            <el-icon><Present /></el-icon>
            <span>营销活动</span>
          </template>
          <el-menu-item index="/shop/marketing/group-buy">拼团活动</el-menu-item>
          <el-menu-item index="/shop/marketing/seckill">秒杀活动</el-menu-item>
          <el-menu-item index="/shop/marketing/data">活动数据</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="ai">
          <template #title>
            <el-icon><MagicStick /></el-icon>
            <span>AI 助手</span>
          </template>
          <el-menu-item index="/shop/ai/copywriting">文案生成</el-menu-item>
          <el-menu-item index="/shop/ai/quota">额度管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="reconciliation">
          <template #title>
            <el-icon><Tickets /></el-icon>
            <span>对账中心</span>
          </template>
          <el-menu-item index="/shop/reconciliation/flow">流水记录</el-menu-item>
          <el-menu-item index="/shop/reconciliation/detail">对账明细</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/shop/qrcode">
          <el-icon><Link /></el-icon>
          <template #title>店铺二维码</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container class="layout-main">
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-icon" @click="appStore.toggleSidebar">
            <Fold v-if="!appStore.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ userStore.userInfo?.username }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="layout-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Fold,
  Expand,
  DataLine,
  Setting,
  User,
  Wallet,
  CreditCard,
  Document,
  Box,
  Present,
  MagicStick,
  Tickets,
  UserFilled,
  ScaleTo,
  Link
} from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { useShopStore } from '@/stores/shop'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const shopStore = useShopStore()

const activeMenu = computed(() => route.path)
const sidebarWidth = computed(() => (appStore.sidebarCollapsed ? '64px' : '200px'))

async function handleCommand(command: string) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/shop/login')
    } catch (e) {
      // cancel
    }
  }
}

shopStore.fetchShopConfig().catch(() => {})
</script>

<style scoped lang="scss">
.layout-container {
  width: 100%;
  height: 100vh;
}

.layout-aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow-x: hidden;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #2b3a4a;

    h2 {
      color: #fff;
      font-size: 18px;
      white-space: nowrap;
    }
  }

  .el-menu {
    border-right: none;
  }
}

.layout-main {
  display: flex;
  flex-direction: column;
}

.layout-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #dcdfe6;

  .header-left {
    .collapse-icon {
      font-size: 20px;
      cursor: pointer;
      color: #606266;

      &:hover {
        color: #409eff;
      }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      color: #606266;

      &:hover {
        color: #409eff;
      }
    }
  }
}

.layout-content {
  flex: 1;
  padding: 24px;
  background-color: #f5f7fa;
  overflow: auto;
}
</style>
