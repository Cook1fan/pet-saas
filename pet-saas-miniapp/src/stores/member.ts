import { defineStore } from 'pinia'
import type { MemberInfo, ShopInfo } from '@/types'

interface MemberState {
  token: string | null
  memberInfo: MemberInfo | null
  currentShop: ShopInfo | null
  isLoggedIn: boolean
}

export const useMemberStore = defineStore('member', {
  state: (): MemberState => ({
    token: uni.getStorageSync('token') || null,
    memberInfo: null,
    currentShop: uni.getStorageSync('currentShop') || null,
    isLoggedIn: !!uni.getStorageSync('token')
  }),

  getters: {
    hasToken: (state): boolean => !!state.token,
    hasCurrentShop: (state): boolean => !!state.currentShop
  },

  actions: {
    setMemberInfo(info: MemberInfo) {
      this.memberInfo = info
      this.isLoggedIn = true
    },

    setToken(token: string) {
      this.token = token
      this.isLoggedIn = true
      uni.setStorageSync('token', token)
    },

    setCurrentShop(shop: ShopInfo) {
      this.currentShop = shop
      uni.setStorageSync('currentShop', shop)
    },

    logout() {
      this.token = null
      this.memberInfo = null
      this.isLoggedIn = false
      uni.removeStorageSync('token')
      uni.removeStorageSync('currentShop')
    }
  }
})
