<template>
  <div class="config-info">
    <el-card>
      <template #header>
        <span>门店信息</span>
      </template>
      <el-alert type="info" title="门店信息配置" description="配置门店基本信息，包括名称、地址、联系方式等" show-icon class="mb-md" />
      <el-form :model="form" label-width="120px" style="max-width: 600px">
        <el-form-item label="门店Logo">
          <el-upload
            class="logo-uploader"
            action="#"
            :show-file-list="false"
            :auto-upload="false"
            accept="image/*"
            :on-change="handleLogoChange"
          >
            <img v-if="form.logo" :src="form.logo" class="logo" />
            <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="门店名称">
          <el-input v-model="form.name" placeholder="请输入门店名称" />
        </el-form-item>
        <el-form-item label="门店地址">
          <el-input v-model="form.address" placeholder="请输入门店地址" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
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
import { Plus } from '@element-plus/icons-vue'
import { getShopConfig, updateShopConfig, type ShopConfig } from '@/api/shop'

const loading = ref(false)

const form = reactive<Partial<ShopConfig>>({
  name: '',
  address: '',
  phone: '',
  logo: ''
})

async function loadData() {
  try {
    const data = await getShopConfig()
    Object.assign(form, data)
  } catch (e) {
    console.error('加载门店信息失败', e)
  }
}

async function handleSave() {
  loading.value = true
  try {
    await updateShopConfig(form)
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

function handleLogoChange(file: any) {
  const reader = new FileReader()
  reader.onload = () => {
    form.logo = reader.result as string
  }
  reader.readAsDataURL(file.raw)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.config-info {
  .mb-md {
    margin-bottom: 20px;
  }

  .logo-uploader {
    :deep(.el-upload) {
      border: 1px dashed var(--el-border-color);
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration-fast);

      &:hover {
        border-color: var(--el-color-primary);
      }
    }

    .logo {
      width: 100px;
      height: 100px;
      display: block;
      object-fit: cover;
    }

    .logo-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 100px;
      height: 100px;
      text-align: center;
      line-height: 100px;
    }
  }
}
</style>
