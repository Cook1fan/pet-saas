<template>
  <div class="order-detail">
    <el-page-header @back="goBack" content="订单详情" />
    <el-card class="mt-md">
      <template #header>
        <span>订单信息</span>
      </template>
      <el-descriptions :column="2" border v-if="orderInfo">
        <el-descriptions-item label="订单号">{{ orderInfo.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(orderInfo.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">
          <span style="color: #f56c6c; font-weight: bold">¥{{ orderInfo.totalAmount.toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="实付金额">
          <span style="color: #67c23a; font-weight: bold">¥{{ orderInfo.payAmount.toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ getPayTypeText(orderInfo.payType) }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="getPayStatusTagType(orderInfo.payStatus)">{{ getPayStatusText(orderInfo.payStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付时间" v-if="orderInfo.payTime">{{ formatTime(orderInfo.payTime) }}</el-descriptions-item>
        <el-descriptions-item label="微信支付单号" v-if="orderInfo.transactionId">{{ orderInfo.transactionId }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="mt-md">
      <template #header>
        <span>商品明细</span>
      </template>
      <el-table :data="orderItems" stripe>
        <el-table-column prop="goodsName" label="商品/服务" />
        <el-table-column prop="price" label="单价" width="120">
          <template #default="{ row }">¥{{ row.price.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="num" label="数量" width="80" />
        <el-table-column label="小计" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c">¥{{ (row.price * row.num).toFixed(2) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <div class="action-bar mt-md" v-if="orderInfo && orderInfo.payStatus === PayStatusEnum.PAID">
      <el-button type="danger" @click="handleRefund">退款</el-button>
    </div>

    <el-dialog v-model="refundDialogVisible" title="退款" width="500px">
      <el-form :model="refundForm" label-width="100px">
        <el-form-item label="退款金额">
          <el-input-number v-model="refundForm.refundAmount" :min="0" :max="maxRefundAmount" :precision="2" :step="0.01" style="width: 100%" />
        </el-form-item>
        <el-form-item label="退款原因">
          <el-input v-model="refundForm.reason" type="textarea" :rows="4" placeholder="请输入退款原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRefund" :loading="refundLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOrderDetail,
  refundOrder,
  PayTypeEnum,
  PayStatusEnum,
  getPayTypeText,
  getPayStatusText,
  getPayStatusTagType,
  type OrderDetail,
  type RefundOrderRequest
} from '@/api/order'

const route = useRoute()
const router = useRouter()
const orderId = Number(route.params.id)
const orderInfo = ref<OrderDetail['order'] | null>(null)
const orderItems = ref<OrderDetail['items']>([])
const refundDialogVisible = ref(false)
const refundLoading = ref(false)

const refundForm = reactive<RefundOrderRequest>({
  orderId: 0,
  refundAmount: 0,
  reason: ''
})

const maxRefundAmount = computed(() => {
  return orderInfo.value?.payAmount || 0
})

function formatTime(timeStr: string): string {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 19)
}

async function loadData() {
  try {
    const res = await getOrderDetail(orderId)
    orderInfo.value = res.order
    orderItems.value = res.items || []
  } catch (e) {
    ElMessage.error('加载订单详情失败')
  }
}

function goBack() {
  router.back()
}

function handleRefund() {
  if (!orderInfo.value) return
  refundForm.orderId = orderInfo.value.id
  refundForm.refundAmount = orderInfo.value.payAmount
  refundForm.reason = ''
  refundDialogVisible.value = true
}

async function submitRefund() {
  if (!refundForm.orderId) {
    ElMessage.warning('订单ID不存在')
    return
  }
  if (!refundForm.reason) {
    ElMessage.warning('请填写退款原因')
    return
  }
  if (refundForm.refundAmount <= 0) {
    ElMessage.warning('退款金额必须大于0')
    return
  }
  try {
    await ElMessageBox.confirm('确定要退款吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    refundLoading.value = true
    await refundOrder(refundForm)
    ElMessage.success('退款成功')
    refundDialogVisible.value = false
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('退款失败')
    }
  } finally {
    refundLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.order-detail {
  .mt-md {
    margin-top: 20px;
  }

  .action-bar {
    display: flex;
    justify-content: flex-end;
  }
}
</style>
