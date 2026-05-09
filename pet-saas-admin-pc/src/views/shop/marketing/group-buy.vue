<template>
  <div class="group-buy">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>拼团活动管理</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="活动名称">
          <el-input v-model="queryForm.title" placeholder="请输入活动名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option :value="ACTIVITY_STATUS.OFFLINE" label="已下架" />
            <el-option :value="ACTIVITY_STATUS.ONLINE" label="已上架" />
          </el-select>
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

      <!-- 工具栏 -->
      <div class="toolbar">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增拼团活动
        </el-button>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="title" label="活动名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="商品信息" min-width="200">
          <template #default="{ row }">
            <div class="goods-info">
              <el-image v-if="row.goodsImage" :src="row.goodsImage" fit="cover" class="goods-image" />
              <div class="goods-detail">
                <div class="goods-name">{{ row.goodsName }}</div>
                <div v-if="row.skuSpec" class="sku-spec">{{ row.skuSpec }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="160">
          <template #default="{ row }">
            <div class="price-info">
              <div class="origin-price">¥{{ formatPrice(row.originPrice) }}</div>
              <div class="group-price">¥{{ formatPrice(row.price) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="groupCount" label="成团人数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.groupCount }}人</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="140" align="center">
          <template #default="{ row }">
            <div class="stock-info">
              <span class="sold">已售: {{ row.soldCount }}</span>
              <span class="total">库存: {{ row.stock }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="活动时间" width="300">
          <template #default="{ row }">
            <div class="time-range">
              <div><el-icon><Clock /></el-icon> 开始: {{ formatDateTime(row.startTime) }}</div>
              <div><el-icon><Timer /></el-icon> 结束: {{ formatDateTime(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="dark">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewData(row)">
              <el-icon><DataLine /></el-icon>数据
            </el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button v-if="row.status === ACTIVITY_STATUS.OFFLINE" type="success" link size="small" @click="handleToggleStatus(row, ACTIVITY_STATUS.ONLINE)">
              <el-icon><Top /></el-icon>上架
            </el-button>
            <el-button v-if="row.status === ACTIVITY_STATUS.ONLINE" type="warning" link size="small" @click="handleToggleStatus(row, ACTIVITY_STATUS.OFFLINE)">
              <el-icon><Bottom /></el-icon>下架
            </el-button>
            <el-popconfirm
              title="确定要删除该活动吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button type="danger" link size="small">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑拼团活动' : '新增拼团活动'"
      width="700px"
      :close-on-click-modal="false"
      @close="resetDialogForm"
    >
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="120px">
        <el-form-item label="活动名称" prop="title">
          <el-input v-model="dialogForm.title" placeholder="请输入活动名称" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="选择商品" prop="goodsId">
          <el-select
            v-model="dialogForm.goodsId"
            placeholder="请搜索并选择商品"
            filterable
            remote
            reserve-keyword
            style="width: 100%"
            :remote-method="searchGoods"
            :loading="goodsLoading"
            @change="handleGoodsChange"
          >
            <el-option
              v-for="item in goodsOptions"
              :key="item.id"
              :label="item.goodsName"
              :value="item.id"
            >
              <div class="goods-option">
                <el-image v-if="item.mainImage" :src="item.mainImage" fit="cover" class="option-image" />
                <span>{{ item.goodsName }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="选择规格" prop="skuId">
          <el-select
            v-model="dialogForm.skuId"
            placeholder="请选择商品规格"
            style="width: 100%"
            :disabled="!dialogForm.goodsId"
            @change="handleSkuChange"
          >
            <el-option
              v-for="sku in currentSkuList"
              :key="sku.id"
              :label="`${sku.specName}: ${sku.specValue} - ¥${formatPrice(sku.price)}`"
              :value="sku.id"
            />
          </el-select>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="原价" prop="originPrice">
              <el-input-number
                v-model="dialogForm.originPrice"
                :min="0"
                :precision="2"
                :step="1"
                style="width: 100%"
                placeholder="请输入原价"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拼团价" prop="price">
              <el-input-number
                v-model="dialogForm.price"
                :min="0"
                :precision="2"
                :step="1"
                style="width: 100%"
                placeholder="请输入拼团价"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="成团人数" prop="groupCount">
              <el-input-number
                v-model="dialogForm.groupCount"
                :min="2"
                :max="100"
                style="width: 100%"
                placeholder="请输入成团人数"
              />
              <template #tip>
                <div class="el-form-item__tip">需要多少人拼团成功</div>
              </template>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拼团有效期" prop="groupValidHours">
              <el-input-number
                v-model="dialogForm.groupValidHours"
                :min="1"
                :max="720"
                style="width: 100%"
                placeholder="小时"
              />
              <template #tip>
                <div class="el-form-item__tip">拼团发起后多少小时内有效</div>
              </template>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动库存" prop="stock">
              <el-input-number
                v-model="dialogForm.stock"
                :min="0"
                style="width: 100%"
                placeholder="请输入活动库存"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="每人限购" prop="limitNum">
              <el-input-number
                v-model="dialogForm.limitNum"
                :min="1"
                style="width: 100%"
                placeholder="请输入限购数量"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="活动时间" prop="timeRange">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
            :shortcuts="dateShortcuts"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saveLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 数据统计对话框 -->
    <el-dialog
      v-model="dataDialogVisible"
      title="活动数据统计"
      width="600px"
    >
      <el-descriptions :column="2" border v-loading="dataLoading">
        <el-descriptions-item label="活动名称" :span="2">{{ currentActivity?.title }}</el-descriptions-item>
        <el-descriptions-item label="总订单数">
          <span class="highlight">{{ activityData.totalOrderCount || 0 }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="总GMV">
          <span class="highlight amount">¥{{ formatPrice(activityData.totalGmv || 0) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="成功拼团数">
          <span class="highlight success">{{ activityData.successGroupCount || 0 }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="进行中拼团数">
          <span class="highlight warning">{{ activityData.ongoingGroupCount || 0 }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="失败拼团数">
          <span class="highlight danger">{{ activityData.failedGroupCount || 0 }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  Edit,
  Delete,
  Clock,
  Timer,
  DataLine,
  Top,
  Bottom
} from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getActivityList,
  createGroupActivity,
  updateGroupActivity,
  deleteActivity,
  updateActivityStatus,
  getActivityData,
  ACTIVITY_STATUS,
  ACTIVITY_TYPE,
  type ActivityInfoVO,
  type ActivityQuery,
  type CreateGroupActivityReq,
  type ActivityData
} from '@/api/groupActivity'
import { getGoodsList, type Goods, type GoodsSku, getGoods } from '@/api/inventory'

// 状态
const loading = ref(false)
const saveLoading = ref(false)
const goodsLoading = ref(false)
const dataLoading = ref(false)
const tableData = ref<ActivityInfoVO[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dataDialogVisible = ref(false)
const isEdit = ref(false)
const dialogFormRef = ref<FormInstance>()
const goodsOptions = ref<Goods[]>([])
const currentSkuList = ref<GoodsSku[]>([])
const currentActivity = ref<ActivityInfoVO | null>(null)
const activityData = ref<ActivityData>({})

// 查询表单
const queryForm = reactive<ActivityQuery>({
  page: 1,
  pageSize: 10,
  title: '',
  status: undefined,
  type: ACTIVITY_TYPE.GROUP
})

// 对话框表单
const dialogForm = reactive<Partial<CreateGroupActivityReq>>({
  title: '',
  goodsId: undefined,
  skuId: undefined,
  price: 0,
  originPrice: 0,
  groupCount: 2,
  groupValidHours: 24,
  stock: 0,
  limitNum: 1,
  startTime: '',
  endTime: ''
})

// 时间范围
const timeRange = ref<[string, string] | []>([])

// 日期快捷选项
const dateShortcuts = [
  {
    text: '今天',
    value: () => {
      const start = new Date()
      start.setHours(0, 0, 0, 0)
      const end = new Date()
      end.setHours(23, 59, 59, 999)
      return [start, end]
    }
  },
  {
    text: '本周',
    value: () => {
      const start = new Date()
      const day = start.getDay() || 7
      start.setDate(start.getDate() - day + 1)
      start.setHours(0, 0, 0, 0)
      const end = new Date(start)
      end.setDate(end.getDate() + 6)
      end.setHours(23, 59, 59, 999)
      return [start, end]
    }
  },
  {
    text: '本月',
    value: () => {
      const start = new Date()
      start.setDate(1)
      start.setHours(0, 0, 0, 0)
      const end = new Date(start.getFullYear(), start.getMonth() + 1, 0, 23, 59, 59)
      return [start, end]
    }
  }
]

// 表单验证规则
const dialogRules: FormRules = {
  title: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  goodsId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  skuId: [{ required: true, message: '请选择商品规格', trigger: 'change' }],
  originPrice: [
    { required: true, message: '请输入原价', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '原价必须大于0', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入拼团价', trigger: 'blur' },
    {
      type: 'number',
      validator: (rule, value, callback) => {
        if (value && dialogForm.originPrice && value >= dialogForm.originPrice) {
          callback(new Error('拼团价必须小于原价'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  groupCount: [{ required: true, message: '请输入成团人数', trigger: 'blur' }],
  groupValidHours: [{ required: true, message: '请输入拼团有效期', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入活动库存', trigger: 'blur' }],
  limitNum: [{ required: true, message: '请输入限购数量', trigger: 'blur' }],
  timeRange: [
    {
      required: true,
      validator: (rule, value, callback) => {
        if (!timeRange.value || timeRange.value.length !== 2) {
          callback(new Error('请选择活动时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 工具函数
const formatPrice = (price: number | string | undefined): string => {
  if (price === undefined || price === null) return '0.00'
  return Number(price).toFixed(2)
}

const formatDateTime = (dateTime: string | undefined): string => {
  if (!dateTime) return '-'
  return dateTime
}

const getStatusType = (status: number): 'success' | 'info' | 'warning' | 'danger' => {
  return status === ACTIVITY_STATUS.ONLINE ? 'success' : 'info'
}

const getStatusText = (status: number): string => {
  return status === ACTIVITY_STATUS.ONLINE ? '已上架' : '已下架'
}

// 数据加载
async function loadData() {
  loading.value = true
  try {
    const result = await getActivityList(queryForm)
    tableData.value = result.records || []
    total.value = result.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
function resetQuery() {
  queryForm.page = 1
  queryForm.title = ''
  queryForm.status = undefined
  loadData()
}

// 商品搜索
async function searchGoods(query: string) {
  if (!query) {
    goodsOptions.value = []
    return
  }
  goodsLoading.value = true
  try {
    const result = await getGoodsList({ keyword: query, pageNum: 1, pageSize: 20 })
    goodsOptions.value = result.records || []
  } catch (error) {
    console.error('搜索商品失败:', error)
  } finally {
    goodsLoading.value = false
  }
}

// 商品选择变化
async function handleGoodsChange(goodsId: number) {
  dialogForm.skuId = undefined
  dialogForm.originPrice = 0
  dialogForm.price = 0
  dialogForm.stock = 0

  if (!goodsId) {
    currentSkuList.value = []
    return
  }

  try {
    const goods = await getGoods(goodsId)
    currentSkuList.value = goods.skuList || []
  } catch (error) {
    console.error('获取商品详情失败:', error)
  }
}

// SKU选择变化
function handleSkuChange(skuId: number) {
  const sku = currentSkuList.value.find(s => s.id === skuId)
  if (sku) {
    dialogForm.originPrice = sku.price
    dialogForm.price = sku.price
    dialogForm.stock = sku.availableStock || sku.stock || 0
  }
}

// 创建活动
function handleCreate() {
  isEdit.value = false
  resetDialogForm()
  dialogVisible.value = true
}

// 编辑活动
async function handleEdit(row: ActivityInfoVO) {
  isEdit.value = true
  currentActivity.value = row

  // 先获取商品详情，获取SKU列表
  if (row.goodsId) {
    try {
      const goods = await getGoods(row.goodsId)
      currentSkuList.value = goods.skuList || []
    } catch (error) {
      console.error('获取商品详情失败:', error)
    }
  }

  // 填充表单
  Object.assign(dialogForm, {
    title: row.title,
    goodsId: row.goodsId,
    skuId: row.skuId,
    price: row.price,
    originPrice: row.originPrice,
    groupCount: row.groupCount,
    groupValidHours: row.groupValidHours,
    stock: row.stock,
    limitNum: row.limitNum,
    startTime: row.startTime,
    endTime: row.endTime
  })

  timeRange.value = [row.startTime, row.endTime]
  dialogVisible.value = true
}

// 查看数据
async function handleViewData(row: ActivityInfoVO) {
  currentActivity.value = row
  dataDialogVisible.value = true
  dataLoading.value = true
  try {
    const data = await getActivityData(row.id)
    activityData.value = data
  } catch (error) {
    console.error('获取活动数据失败:', error)
    ElMessage.error('获取活动数据失败')
  } finally {
    dataLoading.value = false
  }
}

// 切换状态
async function handleToggleStatus(row: ActivityInfoVO, status: number) {
  try {
    await updateActivityStatus(row.id, status)
    ElMessage.success(status === ACTIVITY_STATUS.ONLINE ? '上架成功' : '下架成功')
    loadData()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 保存活动
async function handleSave() {
  if (!dialogFormRef.value) return

  // 验证时间范围
  if (timeRange.value && timeRange.value.length === 2) {
    dialogForm.startTime = timeRange.value[0]
    dialogForm.endTime = timeRange.value[1]
  }

  await dialogFormRef.value.validate(async (valid) => {
    if (valid) {
      saveLoading.value = true
      try {
        if (isEdit.value && currentActivity.value) {
          await updateGroupActivity(
            currentActivity.value.id,
            dialogForm as CreateGroupActivityReq
          )
          ElMessage.success('更新成功')
        } else {
          await createGroupActivity(dialogForm as CreateGroupActivityReq)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败')
      } finally {
        saveLoading.value = false
      }
    }
  })
}

// 删除活动
async function handleDelete(row: ActivityInfoVO) {
  try {
    await deleteActivity(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

// 重置对话框表单
function resetDialogForm() {
  dialogFormRef.value?.resetFields()
  Object.assign(dialogForm, {
    title: '',
    goodsId: undefined,
    skuId: undefined,
    price: 0,
    originPrice: 0,
    groupCount: 2,
    groupValidHours: 24,
    stock: 0,
    limitNum: 1,
    startTime: '',
    endTime: ''
  })
  timeRange.value = []
  currentSkuList.value = []
  goodsOptions.value = []
  currentActivity.value = null
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.group-buy {
  .card-header {
    font-weight: 600;
    font-size: 16px;
  }

  .search-form {
    margin-bottom: 16px;
  }

  .toolbar {
    margin-bottom: 16px;
  }

  .goods-info {
    display: flex;
    align-items: center;
    gap: 10px;

    .goods-image {
      width: 40px;
      height: 40px;
      border-radius: 4px;
      background-color: #f5f5f5;
    }

    .goods-detail {
      flex: 1;
      overflow: hidden;

      .goods-name {
        font-weight: 500;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .sku-spec {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .goods-option {
    display: flex;
    align-items: center;
    gap: 8px;

    .option-image {
      width: 32px;
      height: 32px;
      border-radius: 4px;
      background-color: #f5f5f5;
    }
  }

  .price-info {
    .origin-price {
      text-decoration: line-through;
      color: #909399;
      font-size: 12px;
    }

    .group-price {
      color: #f56c6c;
      font-weight: bold;
      font-size: 16px;
    }
  }

  .stock-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
    font-size: 12px;

    .sold {
      color: #67c23a;
    }

    .total {
      color: #909399;
    }
  }

  .time-range {
    display: flex;
    flex-direction: column;
    gap: 4px;
    font-size: 12px;
    color: #606266;

    > div {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }

  .el-form-item__tip {
    color: #909399;
    font-size: 12px;
  }

  .highlight {
    font-weight: bold;
    font-size: 16px;

    &.amount {
      color: #f56c6c;
    }

    &.success {
      color: #67c23a;
    }

    &.warning {
      color: #e6a23c;
    }

    &.danger {
      color: #f56c6c;
    }
  }
}
</style>
