<template>
  <Teleport to="body">
    <div v-if="visible" class="image-viewer-mask" @click.self="handleClose" @wheel.prevent>
      <div class="image-viewer-header">
        <div class="image-viewer-actions">
          <button class="viewer-btn" @click="zoomIn" title="放大">
            <svg class="icon" viewBox="0 0 1024 1024" width="1em" height="1em">
              <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm192 472c0 4.4-3.6 8-8 8H544v152c0 4.4-3.6 8-8 8h-48c-4.4 0-8-3.6-8-8V544H328c-4.4 0-8-3.6-8-8v-48c0-4.4 3.6-8 8-8h152V328c0-4.4 3.6-8 8-8h48c4.4 0 8 3.6 8 8v152h152c4.4 0 8 3.6 8 8v48z" fill="currentColor"/>
            </svg>
          </button>
          <button class="viewer-btn" @click="zoomOut" title="缩小">
            <svg class="icon" viewBox="0 0 1024 1024" width="1em" height="1em">
              <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm192 472c0 4.4-3.6 8-8 8H328c-4.4 0-8-3.6-8-8v-48c0-4.4 3.6-8 8-8h368c4.4 0 8 3.6 8 8v48z" fill="currentColor"/>
            </svg>
          </button>
          <button class="viewer-btn" @click="reset" title="重置">
            <svg class="icon" viewBox="0 0 1024 1024" width="1em" height="1em">
              <path d="M336 170.666667a42.666667 42.666667 0 0 0-42.666667 42.666666v128a42.666667 42.666667 0 0 0 42.666667 42.666667h128a42.666667 42.666667 0 1 0 0-85.333334h-66.432c32.682667-56.533333 94.549333-93.973333 166.432-93.973333 105.92 0 192 86.08 192 192s-86.08 192-192 192a42.666667 42.666667 0 1 0 0 85.333333c153.002667 0 277.333333-124.330667 277.333333-277.333333S665.002667 170.666667 512 170.666667c-83.584 0-159.189333 40.277333-207.957333 102.976v-62.314667a42.666667 42.666667 0 0 0-42.666667-42.666666z" fill="currentColor"/>
            </svg>
          </button>
          <button class="viewer-btn" @click="rotateLeft" title="向左旋转">
            <svg class="icon rotate-180" viewBox="0 0 1024 1024" width="1em" height="1em">
              <path d="M336 170.666667a42.666667 42.666667 0 0 0-42.666667 42.666666v128a42.666667 42.666667 0 0 0 42.666667 42.666667h128a42.666667 42.666667 0 1 0 0-85.333334h-66.432c32.682667-56.533333 94.549333-93.973333 166.432-93.973333 105.92 0 192 86.08 192 192s-86.08 192-192 192a42.666667 42.666667 0 1 0 0 85.333333c153.002667 0 277.333333-124.330667 277.333333-277.333333S665.002667 170.666667 512 170.666667c-83.584 0-159.189333 40.277333-207.957333 102.976v-62.314667a42.666667 42.666667 0 0 0-42.666667-42.666666z" fill="currentColor"/>
            </svg>
          </button>
          <button class="viewer-btn" @click="rotateRight" title="向右旋转">
            <svg class="icon" viewBox="0 0 1024 1024" width="1em" height="1em">
              <path d="M336 170.666667a42.666667 42.666667 0 0 0-42.666667 42.666666v128a42.666667 42.666667 0 0 0 42.666667 42.666667h128a42.666667 42.666667 0 1 0 0-85.333334h-66.432c32.682667-56.533333 94.549333-93.973333 166.432-93.973333 105.92 0 192 86.08 192 192s-86.08 192-192 192a42.666667 42.666667 0 1 0 0 85.333333c153.002667 0 277.333333-124.330667 277.333333-277.333333S665.002667 170.666667 512 170.666667c-83.584 0-159.189333 40.277333-207.957333 102.976v-62.314667a42.666667 42.666667 0 0 0-42.666667-42.666666z" fill="currentColor"/>
            </svg>
          </button>
          <template v-if="urlList.length > 1">
            <button class="viewer-btn" @click="prev" :disabled="currentIndex === 0" title="上一张">
              <svg class="icon" viewBox="0 0 1024 1024" width="1em" height="1em">
                <path d="M724 218.3V141c0-6.7-7.7-10.4-12.9-6.3L260.3 486.8c-5.3 4.1-5.3 12.4 0 16.5l450.9 352.1c5.3 4.1 12.9.4 12.9-6.3v-77.3c0-4.9-2.3-9.6-6.1-12.6l-360-281 360-281.1c3.8-3 6.1-7.7 6.1-12.6z" fill="currentColor"/>
              </svg>
            </button>
            <button class="viewer-btn" @click="next" :disabled="currentIndex === urlList.length - 1" title="下一张">
              <svg class="icon" viewBox="0 0 1024 1024" width="1em" height="1em">
                <path d="M765.7 486.8L314.9 134.7c-5.3-4.1-12.9-.4-12.9 6.3v77.3c0 4.9 2.3 9.6 6.1 12.6l360 281.1-360 281.1c-3.9 3-6.1 7.7-6.1 12.6V883c0 6.7 7.7 10.4 12.9 6.3l450.8-352.1c5.3-4.2 5.3-12.4 0-16.5z" fill="currentColor"/>
              </svg>
            </button>
          </template>
        </div>
        <button class="viewer-btn close-btn" @click="handleClose" title="关闭">
          <svg class="icon" viewBox="0 0 1024 1024" width="1em" height="1em">
            <path d="M563.8 512l262.5-312.9c4.4-5.2.7-13.1-6.1-13.1h-79.8c-4.7 0-9.2 2.1-12.3 5.7L511.6 449.8 295.1 191.7c-3-3.6-7.5-5.7-12.3-5.7H203c-6.8 0-10.5 7.9-6.1 13.1L459.4 512 196.9 824.9A7.95 7.95 0 0 0 203 838h79.8c4.7 0 9.2-2.1 12.3-5.7l216.5-258.1 216.5 258.1c3 3.6 7.5 5.7 12.3 5.7h79.8c6.8 0 10.5-7.9 6.1-13.1L563.8 512z" fill="currentColor"/>
          </svg>
        </button>
      </div>
      <div
        ref="imageWrapperRef"
        class="image-viewer-body"
        @mousedown="handleMouseDown"
      >
        <img
          ref="imageRef"
          :src="currentUrl"
          class="image-viewer-img"
          :style="imageStyle"
          draggable="false"
          @load="onImageLoad"
          @error="onImageError"
        />
      </div>
      <div v-if="urlList.length > 1" class="image-viewer-footer">
        <span class="image-count">{{ currentIndex + 1 }} / {{ urlList.length }}</span>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps<{
  urlList: string[]
  initialIndex?: number
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'update:visible', value: boolean): void
}>()

const imageWrapperRef = ref<HTMLElement>()
const imageRef = ref<HTMLImageElement>()

const currentIndex = ref(props.initialIndex ?? 0)
const scale = ref(1)
const rotate = ref(0)
const translateX = ref(0)
const translateY = ref(0)

// 拖拽相关
const isDragging = ref(false)
const startX = ref(0)
const startY = ref(0)

const currentUrl = computed(() => props.urlList[currentIndex.value] || '')

const imageStyle = computed(() => ({
  transform: `translate(${translateX.value}px, ${translateY.value}px) scale(${scale.value}) rotate(${rotate.value}deg)`,
  transition: isDragging.value ? 'none' : 'transform 0.3s ease'
}))

function zoomIn() {
  scale.value = Math.min(scale.value + 0.25, 10)
}

function zoomOut() {
  scale.value = Math.max(scale.value - 0.25, 0.1)
}

function reset() {
  scale.value = 1
  rotate.value = 0
  translateX.value = 0
  translateY.value = 0
}

function rotateLeft() {
  rotate.value -= 90
}

function rotateRight() {
  rotate.value += 90
}

function prev() {
  if (currentIndex.value > 0) {
    currentIndex.value--
    reset()
  }
}

function next() {
  if (currentIndex.value < props.urlList.length - 1) {
    currentIndex.value++
    reset()
  }
}

function handleClose() {
  emit('update:visible', false)
  emit('close')
  reset()
}

function handleMouseDown(e: MouseEvent) {
  if (e.button !== 0) return
  e.preventDefault()
  e.stopPropagation()
  isDragging.value = true
  startX.value = e.clientX - translateX.value
  startY.value = e.clientY - translateY.value

  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

function handleMouseMove(e: MouseEvent) {
  if (!isDragging.value) return
  translateX.value = e.clientX - startX.value
  translateY.value = e.clientY - startY.value
}

function handleMouseUp() {
  isDragging.value = false
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
}

function handleKeyDown(e: KeyboardEvent) {
  if (!props.visible) return

  switch (e.key) {
    case 'Escape':
      handleClose()
      break
    case 'ArrowLeft':
      prev()
      break
    case 'ArrowRight':
      next()
      break
    case '+':
    case '=':
      zoomIn()
      break
    case '-':
      zoomOut()
      break
  }
}

function handleWheel(e: WheelEvent) {
  if (!props.visible) return
  e.preventDefault()
  if (e.deltaY < 0) {
    zoomIn()
  } else {
    zoomOut()
  }
}

function onImageLoad() {
  // 图片加载完成
}

function onImageError() {
  // 图片加载失败
}

watch(() => props.initialIndex, (val) => {
  currentIndex.value = val ?? 0
})

watch(() => props.visible, (val) => {
  if (val) {
    currentIndex.value = props.initialIndex ?? 0
    document.addEventListener('keydown', handleKeyDown)
    document.addEventListener('wheel', handleWheel, { passive: false })
  } else {
    document.removeEventListener('keydown', handleKeyDown)
    document.removeEventListener('wheel', handleWheel)
  }
})

onMounted(() => {
  if (props.visible) {
    document.addEventListener('keydown', handleKeyDown)
    document.addEventListener('wheel', handleWheel, { passive: false })
  }
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyDown)
  document.removeEventListener('wheel', handleWheel)
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
})
</script>

<style scoped lang="scss">
.image-viewer-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  background-color: rgba(0, 0, 0, 0.9);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  user-select: none;
}

.image-viewer-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  z-index: 10;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.5), transparent);
}

.image-viewer-actions {
  display: flex;
  gap: 12px;
}

.viewer-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background-color: rgba(255, 255, 255, 0.15);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: rgba(255, 255, 255, 0.3);
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }

  .icon {
    width: 18px;
    height: 18px;

    &.rotate-180 {
      transform: rotate(180deg);
    }
  }
}

.close-btn {
  background-color: rgba(255, 255, 255, 0.15);

  &:hover {
    background-color: rgba(255, 255, 255, 0.3);
  }
}

.image-viewer-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: move;
  overflow: hidden;
}

.image-viewer-img {
  max-width: 90vw;
  max-height: 90vh;
  object-fit: contain;
  pointer-events: none;
}

.image-viewer-footer {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  color: #fff;
  font-size: 14px;
  background: rgba(0, 0, 0, 0.5);
  padding: 8px 16px;
  border-radius: 4px;
}
</style>
