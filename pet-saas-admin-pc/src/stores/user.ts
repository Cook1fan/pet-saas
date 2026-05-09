import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { shopLogin, platformLogin, getCurrentUser, platformLogout, shopLogout } from '@/api/auth'

export type UserRole = 'platform_admin' | 'shop_admin' | 'shop_staff' | null

interface UserInfo {
  id: number
  username: string
  role: string
  tenantId?: number
  status?: number
}

// 从 localStorage 恢复用户信息
function getStoredUserInfo(): UserInfo | null {
  const stored = localStorage.getItem('userInfo')
  return stored ? JSON.parse(stored) : null
}

function getStoredRole(): string | null {
  return localStorage.getItem('role')
}

function getStoredTenantId(): number | null {
  const stored = localStorage.getItem('tenantId')
  return stored ? parseInt(stored, 10) : null
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<UserInfo | null>(getStoredUserInfo())
  const tenantId = ref<number | null>(getStoredTenantId())
  const role = ref<string | null>(getStoredRole())

  const isLoggedIn = computed(() => !!token.value)
  const isPlatformAdmin = computed(() => role.value === 'platform_admin')
  const isShopAdmin = computed(() => role.value === 'shop_admin')
  const isShopStaff = computed(() => role.value === 'shop_staff')

  // 持久化登录状态
  function persistAuth() {
    if (token.value) {
      localStorage.setItem('token', token.value)
    }
    if (userInfo.value) {
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }
    if (role.value) {
      localStorage.setItem('role', role.value)
    }
    if (tenantId.value) {
      localStorage.setItem('tenantId', String(tenantId.value))
    }
  }

  // 清除持久化数据
  function clearPersistedAuth() {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('role')
    localStorage.removeItem('tenantId')
  }

  async function loginShop(username: string, password: string) {
    const res = await shopLogin({ username, password })
    console.log('登录接口返回:', res)
    // 尝试多种可能的 token 字段名
    token.value = (res as any).token || (res as any).data?.token || (res as any).satoken
    console.log('提取到的 token:', token.value)
    userInfo.value = (res as any).user || (res as any).data?.user as UserInfo
    // 从返回的用户信息中获取 tenantId
    const shopUser = (res as any).user || (res as any).data?.user as { tenantId?: number; role: string }
    if (shopUser?.tenantId) {
      tenantId.value = shopUser.tenantId
    }
    role.value = shopUser?.role || 'shop_admin'
    persistAuth()
  }

  async function loginPlatform(username: string, password: string) {
    const res = await platformLogin({ username, password })
    console.log('登录响应:', res)
    token.value = res.token
    userInfo.value = res.user as UserInfo
    role.value = res.user.role
    console.log('设置 role:', role.value)
    persistAuth()
  }

  async function fetchCurrentUser() {
    const res = await getCurrentUser()
    userInfo.value = res
    role.value = res.role
    if (res.tenantId) {
      tenantId.value = res.tenantId
    }
    persistAuth()
  }

  async function logout() {
    try {
      // 根据角色调用对应的登出接口
      if (role.value === 'platform_admin') {
        await platformLogout()
      } else {
        await shopLogout()
      }
    } catch (e) {
      // 登出接口调用失败时，仍然继续清除本地状态
      console.warn('登出接口调用失败，但仍将清除本地状态:', e)
    } finally {
      // 无论接口调用成功与否，都清除本地状态
      token.value = null
      userInfo.value = null
      tenantId.value = null
      role.value = null
      clearPersistedAuth()
    }
  }

  return {
    token,
    userInfo,
    tenantId,
    role,
    isLoggedIn,
    isPlatformAdmin,
    isShopAdmin,
    isShopStaff,
    loginShop,
    loginPlatform,
    fetchCurrentUser,
    logout
  }
})
