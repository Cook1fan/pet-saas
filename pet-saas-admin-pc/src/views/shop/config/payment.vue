<template>
  <div class="payment-config">
    <el-card>
      <template #header>
        <span>支付配置</span>
      </template>
      <el-alert type="info" title="微信支付配置" description="配置微信商户号信息，用于门店收银" show-icon class="mb-md" />
      <el-form :model="form" label-width="140px" style="max-width: 600px">
        <el-form-item label="微信商户号">
          <el-input v-model="form.mchId" placeholder="请输入微信商户号" />
        </el-form-item>
        <el-form-item label="商户API密钥">
          <el-input v-model="form.mchKey" type="password" placeholder="请输入商户API密钥" show-password />
        </el-form-item>
        <el-form-item label="证书路径">
          <el-input v-model="form.certPath" placeholder="请输入证书路径（可选）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="loading">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPaymentConfig, updatePaymentConfig, type PaymentConfig } from '@/api/shop'

const loading = ref(false)

const form = reactive<Partial<PaymentConfig>>({
  mchId: '',
  mchKey: '',
  certPath: ''
})

async function loadData() {
  try {
    const data = await getPaymentConfig()
    Object.assign(form, data)
  } catch (e) {
    console.error('加载支付配置失败', e)
  }
}

async function handleSave() {
  loading.value = true
  try {
    await updatePaymentConfig(form)
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.payment-config {
  .mb-md {
    margin-bottom: 20px;
  }
}
</style>
