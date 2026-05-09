import { defineStore } from 'pinia'
import { ref } from 'vue'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 配置 NProgress
NProgress.configure({
  showSpinner: false,
  trickleSpeed: 200,
  minimum: 0.1
})

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const loading = ref(false)
  const pendingRequestCount = ref(0)

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setLoading(val: boolean) {
    loading.value = val
  }

  /**
   * 开始请求 - 增加计数并显示进度条
   */
  function startRequest() {
    pendingRequestCount.value++
    if (pendingRequestCount.value === 1) {
      NProgress.start()
    }
  }

  /**
   * 结束请求 - 减少计数并隐藏进度条
   */
  function finishRequest() {
    pendingRequestCount.value = Math.max(0, pendingRequestCount.value - 1)
    if (pendingRequestCount.value === 0) {
      NProgress.done()
    }
  }

  return {
    sidebarCollapsed,
    loading,
    pendingRequestCount,
    toggleSidebar,
    setLoading,
    startRequest,
    finishRequest
  }
})
