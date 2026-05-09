<template>
  <div class="qr-code-preview">
    <el-card shadow="hover">
      <div class="preview-container">
        <div class="qr-image-wrapper">
          <el-image
            v-if="qrUrl"
            :src="qrUrl"
            fit="contain"
            class="qr-image"
            :preview-src-list="[qrUrl]"
            :initial-index="0"
          >
            <template #placeholder>
              <div class="image-placeholder">
                <el-icon class="is-loading"><Loading /></el-icon>
              </div>
            </template>
          </el-image>
          <div v-else class="no-qrcode">
            <el-icon :size="48"><Picture /></el-icon>
            <span>暂无二维码</span>
          </div>
        </div>
        <div v-if="qrName" class="qr-name">{{ qrName }}</div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { Picture, Loading } from '@element-plus/icons-vue'

interface Props {
  qrUrl?: string
  qrName?: string
}

defineProps<Props>()
</script>

<style scoped lang="scss">
.qr-code-preview {
  .preview-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
  }

  .qr-image-wrapper {
    width: 200px;
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    border-radius: 8px;
    overflow: hidden;
  }

  .qr-image {
    width: 100%;
    height: 100%;
  }

  .image-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    color: #c0c4cc;
  }

  .no-qrcode {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: #909399;
    width: 100%;
    height: 100%;
  }

  .qr-name {
    font-size: 14px;
    color: #606266;
    text-align: center;
  }
}
</style>
