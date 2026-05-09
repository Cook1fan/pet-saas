<template>
  <div class="qr-code-manage">
    <el-card>
      <template #header>
        <span>店铺二维码管理</span>
      </template>

      <el-row :gutter="20" class="main-content">
        <el-col :span="8">
          <QrCodePreview :qr-url="qrCodeInfo.qrUrl" :qr-name="qrCodeInfo.qrName" />
        </el-col>
        <el-col :span="16">
          <QrCodeDownload
            :qr-code-id="qrCodeInfo.qrCodeId"
            :qr-name="qrCodeInfo.qrName"
            :qr-type="qrCodeInfo.qrType"
            :scan-count="qrCodeInfo.scanCount"
            :create-time="qrCodeInfo.createTime"
            @refresh="loadData"
          />
        </el-col>
      </el-row>
    </el-card>

    <QrCodeStats class="mt-md" :stats="qrCodeStats" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import QrCodePreview from './components/QrCodePreview.vue'
import QrCodeStats from './components/QrCodeStats.vue'
import QrCodeDownload from './components/QrCodeDownload.vue'
import { getQrCode, getQrCodeStats, type QrCodeInfo, type QrCodeStatsData } from '@/api/qrcode'

const loading = ref(false)
const qrCodeInfo = reactive<QrCodeInfo>({
  qrCodeId: 0,
  qrName: '',
  qrUrl: '',
  qrType: 1,
  scanCount: 0,
  createTime: ''
})

const qrCodeStats = reactive<QrCodeStatsData>({
  totalScan: 0,
  newUser: 0,
  oldUser: 0,
  guestUser: 0,
  dailyStats: []
})

async function loadData() {
  loading.value = true
  try {
    const [info, stats] = await Promise.all([
      getQrCode(),
      getQrCodeStats()
    ])
    Object.assign(qrCodeInfo, info)
    Object.assign(qrCodeStats, stats)
  } catch (e) {
    console.error('加载数据失败', e)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.qr-code-manage {
  .main-content {
    display: flex;
    align-items: flex-start;
  }

  .mt-md {
    margin-top: 20px;
  }
}
</style>
