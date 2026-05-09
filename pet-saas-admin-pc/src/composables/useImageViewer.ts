import { ref, createApp, h } from 'vue'
import ImageViewer from '@/components/ImageViewer/index.vue'

let viewerInstance: { close: () => void } | null = null

export function useImageViewer() {
  function openViewer(url: string) {
    openViewerList([url], 0)
  }

  function openViewerList(urlList: string[], initialIndex: number = 0) {
    if (!urlList || urlList.length === 0) return

    // 关闭已有的 viewer
    closeViewer()

    // 创建容器
    const container = document.createElement('div')
    document.body.appendChild(container)

    const visible = ref(true)

    const close = () => {
      visible.value = false
      setTimeout(() => {
        app.unmount()
        if (container.parentNode) {
          container.parentNode.removeChild(container)
        }
        viewerInstance = null
      }, 100)
    }

    const app = createApp({
      render() {
        return h(ImageViewer, {
          urlList,
          initialIndex,
          visible: visible.value,
          'onUpdate:visible': (val: boolean) => {
            if (!val) close()
          },
          onClose: close
        })
      }
    })

    app.mount(container)

    viewerInstance = { close }
  }

  function closeViewer() {
    if (viewerInstance) {
      viewerInstance.close()
      viewerInstance = null
    }
  }

  return {
    openViewer,
    openViewerList,
    closeViewer
  }
}
