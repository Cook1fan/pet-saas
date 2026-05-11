import { defineStore } from 'pinia'
import type { BannerItem, QuickNavItem, ActivityItem, ShopInfo } from '@/types'

interface HomeState {
  shopInfo: ShopInfo | null
  bannerList: BannerItem[]
  quickNavList: QuickNavItem[]
  flashSaleList: ActivityItem[]
  groupBuyList: ActivityItem[]
  allActivityList: ActivityItem[]
  currentTab: 'all' | 'group' | 'flash'
  loading: boolean
}

export const useHomeStore = defineStore('home', {
  state: (): HomeState => ({
    shopInfo: null,
    bannerList: [],
    quickNavList: [],
    flashSaleList: [],
    groupBuyList: [],
    allActivityList: [],
    currentTab: 'all',
    loading: false
  }),

  actions: {
    setShopInfo(info: ShopInfo) {
      this.shopInfo = info
    },

    setBannerList(list: BannerItem[]) {
      this.bannerList = list
    },

    setQuickNavList(list: QuickNavItem[]) {
      this.quickNavList = list
    },

    setFlashSaleList(list: ActivityItem[]) {
      this.flashSaleList = list
    },

    setGroupBuyList(list: ActivityItem[]) {
      this.groupBuyList = list
    },

    setAllActivityList(list: ActivityItem[]) {
      this.allActivityList = list
    },

    setCurrentTab(tab: 'all' | 'group' | 'flash') {
      this.currentTab = tab
    },

    setLoading(loading: boolean) {
      this.loading = loading
    }
  }
})
