<template>
  <div class="order-list">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="queryForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="queryForm.payType" placeholder="请选择支付方式" clearable style="width: 150px">
            <el-option :value="PayTypeEnum.WECHAT" :label="getPayTypeText(PayTypeEnum.WECHAT)" />
            <el-option :value="PayTypeEnum.CASH" :label="getPayTypeText(PayTypeEnum.CASH)" />
            <el-option :value="PayTypeEnum.BALANCE" :label="getPayTypeText(PayTypeEnum.BALANCE)" />
            <el-option :value="PayTypeEnum.CARD" :label="getPayTypeText(PayTypeEnum.CARD)" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="queryForm.orderStatus" placeholder="请选择订单状态" clearable style="width: 150px">
            <el-option :value="OrderStatusEnum.UNPAID" :label="getOrderStatusText(OrderStatusEnum.UNPAID)" />
            <el-option :value="OrderStatusEnum.PAID" :label="getOrderStatusText(OrderStatusEnum.PAID)" />
            <el-option :value="OrderStatusEnum.SHIPPED" :label="getOrderStatusText(OrderStatusEnum.SHIPPED)" />
            <el-option :value="OrderStatusEnum.COMPLETED" :label="getOrderStatusText(OrderStatusEnum.COMPLETED)" />
            <el-option :value="OrderStatusEnum.CANCELLED" :label="getOrderStatusText(OrderStatusEnum.CANCELLED)" />
            <el-option :value="OrderStatusEnum.REFUNDED" :label="getOrderStatusText(OrderStatusEnum.REFUNDED)" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" :loading="loading">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="220" fixed="left" />
        <el-table-column label="订单金额" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">¥{{ row.totalAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实付金额" width="120">
          <template #default="{ row }">
            <span style="color: #67c23a; font-weight: bold">¥{{ row.payAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="支付方式" width="100">
          <template #default="{ row }">
            {{ getPayTypeText(row.payType) }}
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusTagType(row.orderStatus)">
              {{ getOrderStatusText(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="180">
          <template #default="{ row }">
            {{ row.payTime ? formatTime(row.payTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="transactionId" label="第三方交易流水号" width="220">
          <template #default="{ row }">
            {{ row.transactionId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">
              详情
            </el-button>
            <el-button type="danger" link @click="handleRefund(row)" v-if="row.payStatus === PayStatusEnum.PAID">
              退款
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="refundDialogVisible" title="退款" width="500px">
      <el-form :model="refundForm" label-width="100px">
        <el-form-item label="退款金额">
          <el-input-number v-model="refundForm.refundAmount" :min="0" :precision="2" :step="0.01" style="width: 100%" />
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import {
  getOrderList,
  refundOrder,
  PayTypeEnum,
  PayStatusEnum,
  OrderStatusEnum,
  getPayTypeText,
  getPayStatusText,
  getPayStatusTagType,
  getOrderStatusText,
  getOrderStatusTagType,
  type OrderListItem,
  type OrderListQuery,
  type RefundOrderRequest
} from '@/api/order'

const router = useRouter()
const loading = ref(false)
const refundLoading = ref(false)
const tableData = ref<OrderListItem[]>([])
const total = ref(0)
const dateRange = ref<string[]>([])
const refundDialogVisible = ref(false)
const currentOrderId = ref<number | null>(null)

const queryForm = reactive<OrderListQuery>({
  pageNum: 1,
  pageSize: 10,
  payType: undefined,
  orderStatus: undefined,
  orderNo: '',
  startTime: '',
  endTime: ''
})

const refundForm = reactive<RefundOrderRequest>({
  orderId: 0,
  refundAmount: 0,
  reason: ''
})

function formatTime(timeStr: string): string {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 19)
}

async function loadData() {
  loading.value = true
  try {
    if (dateRange.value && dateRange.value.length === 2) {
      queryForm.startTime = dateRange.value[0]
      queryForm.endTime = dateRange.value[1]
    } else {
      queryForm.startTime = ''
      queryForm.endTime = ''
    }
    const res = await getOrderList(queryForm)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.pageNum = 1
  queryForm.payType = undefined
  queryForm.orderStatus = undefined
  queryForm.orderNo = ''
  queryForm.startTime = ''
  queryForm.endTime = ''
  dateRange.value = []
  loadData()
}

function handleDetail(row: OrderListItem) {
  router.push(`/shop/order/detail/${row.id}`)
}

function handleRefund(row: OrderListItem) {
  currentOrderId.value = row.id
  refundForm.orderId = row.id
  refundForm.refundAmount = row.payAmount
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
.order-list {
  .search-form {
    margin-bottom: 16px;
  }

  .el-pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
