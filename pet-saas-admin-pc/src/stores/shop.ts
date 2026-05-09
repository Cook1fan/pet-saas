import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getShopConfig } from '@/api/shop'

interface ShopConfig {
  id?: number
  name?: string
  address?: string
  phone?: string
  logo?: string
}

export const useShopStore = defineStore('shop', () => {
  const shopConfig = ref<ShopConfig | null>(null)

  async function fetchShopConfig() {
    const res = await getShopConfig()
    shopConfig.value = res
  }

  function setShopConfig(config: ShopConfig) {
    shopConfig.value = config
  }

  return {
    shopConfig,
    fetchShopConfig,
    setShopConfig
  }
})
