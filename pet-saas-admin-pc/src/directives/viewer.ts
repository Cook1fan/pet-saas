import type { Directive, DirectiveBinding } from 'vue'
import { useImageViewer } from '@/composables/useImageViewer'

const { openViewer, openViewerList } = useImageViewer()

// 存储图片列表的 map，用于多图查看
const imageListMap = new Map<string, string[]>()

export const viewer: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value, arg } = binding

    // 点击事件处理
    el.style.cursor = 'pointer'

    el.addEventListener('click', (e) => {
      e.stopPropagation()

      if (arg) {
        // 多图模式: v-viewer:listName="index"
        const listName = arg
        const imageList = imageListMap.get(listName)
        if (imageList) {
          const index = typeof value === 'number' ? value : 0
          openViewerList(imageList, index)
        }
      } else {
        // 单图模式: v-viewer="url"
        const url = typeof value === 'string' ? value : el.getAttribute('src')
        if (url) {
          openViewer(url)
        }
      }
    })

    // 存储多图列表（通过 data-image-list 或 arg 方式）
    const imageListAttr = el.getAttribute('data-image-list')
    if (imageListAttr) {
      try {
        const list = JSON.parse(imageListAttr)
        if (Array.isArray(list)) {
          const listName = arg || `list-${Date.now()}`
          imageListMap.set(listName, list)
        }
      } catch (e) {
        console.warn('Failed to parse data-image-list', e)
      }
    }
  },

  unmounted(el: HTMLElement, binding: DirectiveBinding) {
    const { arg } = binding
    if (arg) {
      imageListMap.delete(arg)
    }
  }
}

// 辅助函数：注册图片列表
export function registerImageList(listName: string, imageList: string[]) {
  imageListMap.set(listName, imageList)
}

// 辅助函数：注销图片列表
export function unregisterImageList(listName: string) {
  imageListMap.delete(listName)
}
