<template>
  <div class="marketing-data">
    <el-card>
      <template #header>
        <span>活动数据</span>
      </template>
      <el-row :gutter="20" class="data-cards">
        <el-col :span="8">
          <el-statistic title="活动总数" :value="data.totalActivityCount" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="总订单数" :value="data.totalOrderCount" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="总GMV">
            <template #default>
              <span style="color: #f56c6c; font-size: 30px; font-weight: bold">¥{{ data.totalGmv }}</span>
            </template>
          </el-statistic>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="mt-md">
      <template #header>
        <span>各活动数据</span>
      </template>
      <el-table :data="data.activityData" stripe>
        <el-table-column prop="name" label="活动名称" />
        <el-table-column prop="orderCount" label="订单数" width="120" />
        <el-table-column prop="gmv" label="GMV" width="150">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">¥{{ row.gmv }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { getMarketingData, type MarketingData } from '@/api/marketing'

const loading = ref(false)
const data = reactive<MarketingData>({
  totalActivityCount: 0,
  totalOrderCount: 0,
  totalGmv: 0,
  activityData: []
})

async function loadData() {
  loading.value = true
  try {
    const res = await getMarketingData()
    Object.assign(data, res)
  } catch (e) {
    console.error('加载数据失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.marketing-data {
  .data-cards {
    margin-bottom: 20px;
  }

  .mt-md {
    margin-top: 20px;
  }
}
</style>
