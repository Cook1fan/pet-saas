<template>
  <view class="countdown">
    <view class="countdown-item" v-for="(item, index) in timeParts" :key="index">
      <text class="countdown-number">{{ item.value }}</text>
      <text class="countdown-label">{{ item.label }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'

interface Props {
  remainTime: number // 剩余时间（秒）
}

interface TimePart {
  value: string
  label: string
}

const props = defineProps<Props>()
const emit = defineEmits(['time-up'])

const remainTime = ref(props.remainTime)
const timer = ref<NodeJS.Timeout | null>(null)

const timeParts = computed<TimePart[]>(() => {
  const hours = Math.floor(remainTime.value / 3600)
  const minutes = Math.floor((remainTime.value % 3600) / 60)
  const seconds = remainTime.value % 60

  return [
    { value: String(hours).padStart(2, '0'), label: '时' },
    { value: String(minutes).padStart(2, '0'), label: '分' },
    { value: String(seconds).padStart(2, '0'), label: '秒' }
  ]
})

const startCountdown = () => {
  if (timer.value) {
    clearInterval(timer.value)
  }

  timer.value = setInterval(() => {
    if (remainTime.value > 0) {
      remainTime.value--
    } else {
      clearInterval(timer.value!)
      emit('time-up')
    }
  }, 1000)
}

const stopCountdown = () => {
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }
}

onMounted(() => {
  startCountdown()
})

onUnmounted(() => {
  stopCountdown()
})
</script>

<style scoped lang="scss">
.countdown {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}

.countdown-item {
  display: flex;
  flex-direction: column;
  align-items: center;

  .countdown-number {
    font-size: 48rpx;
    font-weight: bold;
    color: #ff4d4f;
    line-height: 1;
  }

  .countdown-label {
    font-size: 24rpx;
    color: #666;
    margin-top: 8rpx;
  }
}
</style>
