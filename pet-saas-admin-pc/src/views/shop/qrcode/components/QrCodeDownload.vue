<template>
  <div class="qr-code-download">
    <div class="info-section">
      <div class="info-item">
        <span class="label">二维码名称：</span>
        <span class="value">{{ qrName || '-' }}</span>
      </div>
      <div class="info-item">
        <span class="label">类型：</span>
        <span class="value">{{ typeName }}</span>
      </div>
      <div class="info-item">
        <span class="label">扫码次数：</span>
        <span class="value">{{ scanCount || 0 }}</span>
      </div>
      <div class="info-item">
        <span class="label">创建时间：</span>
        <span class="value">{{ createTime || '-' }}</span>
      </div>
    </div>

    <div class="action-section">
      <el-button type="primary" :icon="Download" :loading="downloading" @click="handleDownload">
        下载二维码
      </el-button>
      <el-button :icon="Refresh" :loading="refreshing" @click="handleRefresh">
        刷新
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Download, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { downloadQrCode, refreshQrCode, QR_TYPE } from '@/api/qrcode'
import type { QrType } from '@/api/qrcode'

interface Props {
  qrCodeId?: number
  qrName?: string
  qrType?: QrType
  scanCount?: number
  createTime?: string
}

const props = defineProps<Props>()

const downloading = ref(false)
const refreshing = ref(false)

const typeName = computed(() => {
  const typeMap = {
    [QR_TYPE.SHOP]: '店铺码',
    [QR_TYPE.GOODS]: '商品码',
    [QR_TYPE.ACTIVITY]: '活动码'
  }
  return props.qrType ? typeMap[props.qrType] || '未知' : '-'
})

async function handleDownload() {
  if (!props.qrCodeId) {
    ElMessage.warning('暂无二维码信息')
    return
  }

  downloading.value = true
  try {
    const blob = await downloadQrCode(props.qrCodeId)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `qrcode_${props.qrCodeId}.png`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (e) {
    console.error('下载失败', e)
    ElMessage.error('下载失败')
  } finally {
    downloading.value = false
  }
}

async function handleRefresh() {
  if (!props.qrCodeId) {
    ElMessage.warning('暂无二维码信息')
    return
  }

  refreshing.value = true
  try {
    await refreshQrCode(props.qrCodeId)
    ElMessage.success('刷新成功')
    emit('refresh')
  } catch (e) {
    console.error('刷新失败', e)
    ElMessage.error('刷新失败')
  } finally {
    refreshing.value = false
  }
}

const emit = defineEmits<{
  refresh: []
}>()
</script>

<style scoped lang="scss">
.qr-code-download {
  .info-section {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 24px;
  }

  .info-item {
    display: flex;
    align-items: center;

    .label {
      color: #909399;
      font-size: 14px;
      min-width: 80px;
    }

    .value {
      color: #303133;
      font-size: 14px;
    }
  }

  .action-section {
    display: flex;
    gap: 12px;
  }
}
</style>
