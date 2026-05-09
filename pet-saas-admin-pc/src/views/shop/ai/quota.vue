<template>
  <div class="ai-quota">
    <el-card>
      <template #header>
        <span>额度管理</span>
      </template>
      <el-alert
        :title="`今日剩余：${quota.remaining} 次，今日已用：${quota.used} 次`"
        type="info"
        :description="quota.expireTime ? `有效期至：${quota.expireTime}` : ''"
        show-icon
        class="mb-md"
      />
      <el-progress :percentage="usagePercent" :color="usagePercent > 80 ? '#f56c6c' : '#409eff'" />
    </el-card>

    <el-card class="mt-md">
      <template #header>
        <span>套餐购买</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="8" v-for="pkg in packageList" :key="pkg.id">
          <el-card class="package-card" :class="{ selected: selectedPkgId === pkg.id }" shadow="hover">
            <div class="package-name">{{ pkg.name }}</div>
            <div class="package-count">{{ pkg.count }} 次</div>
            <div class="package-price">
              <span v-if="pkg.originalPrice" class="original-price">¥{{ pkg.originalPrice }}</span>
              <span class="price">¥{{ pkg.price }}</span>
            </div>
            <el-button type="primary" style="width: 100%; margin-top: 16px" @click="handlePurchase(pkg)">
              购买
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getQuotaInfo, getPackageList, purchasePackage, type QuotaInfo, type Package } from '@/api/ai'

const loading = ref(false)
const selectedPkgId = ref<number | null>(null)
const quota = reactive<QuotaInfo>({
  remaining: 0,
  total: 0,
  used: 0
})

const packageList = ref<Package[]>([])

const usagePercent = computed(() => {
  if (quota.total === 0) return 0
  return Math.round((quota.used / quota.total) * 100)
})

async function loadQuota() {
  try {
    const data = await getQuotaInfo()
    Object.assign(quota, data)
  } catch (e) {
    console.error('加载额度失败', e)
  }
}

async function loadPackages() {
  try {
    packageList.value = await getPackageList()
  } catch (e) {
    console.error('加载套餐失败', e)
  }
}

async function handlePurchase(pkg: Package) {
  try {
    await ElMessageBox.confirm(`确定购买「${pkg.name}」套餐吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await purchasePackage(pkg.id)
    ElMessage.success('购买成功')
    loadQuota()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('购买失败')
    }
  }
}

onMounted(() => {
  loadQuota()
  loadPackages()
})
</script>

<style scoped lang="scss">
.ai-quota {
  .mb-md {
    margin-bottom: 20px;
  }

  .mt-md {
    margin-top: 20px;
  }

  .package-card {
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-5px);
    }

    &.selected {
      border: 2px solid #409eff;
    }

    .package-name {
      font-size: 18px;
      font-weight: bold;
      margin-bottom: 10px;
    }

    .package-count {
      font-size: 24px;
      font-weight: bold;
      color: #409eff;
      margin-bottom: 10px;
    }

    .package-price {
      .original-price {
        font-size: 14px;
        color: #999;
        text-decoration: line-through;
        margin-right: 8px;
      }

      .price {
        font-size: 28px;
        font-weight: bold;
        color: #f56c6c;
      }
    }
  }
}
</style>
