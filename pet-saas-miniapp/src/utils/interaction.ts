export function showToast(title: string, icon: 'success' | 'error' | 'loading' | 'none' = 'none', duration = 2000) {
  uni.showToast({
    title,
    icon,
    duration
  })
}

export function showLoading(title = '加载中...', mask = true) {
  uni.showLoading({
    title,
    mask
  })
}

export function hideLoading() {
  uni.hideLoading()
}

export function showModal(options: {
  title?: string
  content: string
  showCancel?: boolean
  cancelText?: string
  confirmText?: string
  success?: (res: { confirm: boolean; cancel: boolean }) => void
}) {
  uni.showModal({
    title: options.title || '提示',
    content: options.content,
    showCancel: options.showCancel !== false,
    cancelText: options.cancelText || '取消',
    confirmText: options.confirmText || '确定',
    success: (res) => {
      options.success?.({
        confirm: res.confirm,
        cancel: res.cancel
      })
    }
  })
}
