<template>
  <div class="copywriting">
    <el-card>
      <template #header>
        <span>AI 文案生成</span>
      </template>
      <el-alert type="info" title="AI 文案助手" description="输入关键词，AI 将为您生成朋友圈或活动推广文案" show-icon class="mb-md" />
      <el-form :model="form" label-width="100px" style="max-width: 600px">
        <el-form-item label="场景">
          <el-radio-group v-model="form.scene">
            <el-radio value="moments">朋友圈</el-radio>
            <el-radio value="activity">活动推广</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="form.keyword" type="textarea" :rows="3" placeholder="请输入关键词，例如：新店开业、洗澡优惠、猫粮促销" />
        </el-form-item>
        <el-form-item label="语气">
          <el-select v-model="form.tone" placeholder="请选择语气" style="width: 200px">
            <el-option value="friendly" label="亲切友好" />
            <el-option value="formal" label="正式专业" />
            <el-option value="humorous" label="幽默风趣" />
            <el-option value="passionate" label="热情洋溢" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleGenerate" :loading="loading">
            <el-icon><MagicStick /></el-icon>
            生成文案
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="mt-md" v-if="result">
      <template #header>
        <span>生成结果</span>
        <el-button type="primary" link @click="handleCopy">
          <el-icon><DocumentCopy /></el-icon>
          复制
        </el-button>
      </template>
      <div class="result-content">{{ result }}</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, DocumentCopy } from '@element-plus/icons-vue'
import { generateCopywriting, type CopywritingReq } from '@/api/ai'

const loading = ref(false)
const result = ref('')

const form = reactive<CopywritingReq>({
  scene: 'moments',
  keyword: '',
  tone: 'friendly'
})

async function handleGenerate() {
  if (!form.keyword.trim()) {
    ElMessage.warning('请输入关键词')
    return
  }
  loading.value = true
  try {
    const res = await generateCopywriting(form)
    result.value = res.content
    ElMessage.success('生成成功')
  } catch (e) {
    ElMessage.error('生成失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function handleCopy() {
  if (!result.value) return
  navigator.clipboard.writeText(result.value).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}
</script>

<style scoped lang="scss">
.copywriting {
  .mb-md {
    margin-bottom: 20px;
  }

  .mt-md {
    margin-top: 20px;
  }

  .result-content {
    white-space: pre-wrap;
    line-height: 1.8;
    font-size: 14px;
    color: #303133;
  }
}
</style>
