<template>
  <div class="flash-sale">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="活动名称">
          <el-input v-model="queryForm.name" placeholder="请输入活动名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option :value="0" label="未开始" />
            <el-option :value="1" label="进行中" />
            <el-option :value="2" label="已结束" />
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
      <div class="toolbar">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增活动
        </el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="name" label="活动名称" />
        <el-table-column prop="goodsName" label="商品" />
        <el-table-column prop="originalPrice" label="原价" width="100">
          <template #default="{ row }"><span style="text-decoration: line-through">¥{{ row.originalPrice }}</span></template>
        </el-table-column>
        <el-table-column prop="salePrice" label="秒杀价" width="100">
          <template #default="{ row }"><span style="color: #f56c6c; font-weight: bold">¥{{ row.salePrice }}</span></template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="soldCount" label="已售" width="80" />
        <el-table-column label="活动时间" width="320">
          <template #default="{ row }">
            <div>{{ row.startTime }}</div>
            <div style="color: #999">至</div>
            <div>{{ row.endTime }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleQrcode(row)">小程序码</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑秒杀活动' : '新增秒杀活动'"
      width="600px"
      @close="dialogFormRef?.resetFields()"
    >
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="120px">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="dialogForm.name" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="商品" prop="goodsId">
          <el-select v-model="dialogForm.goodsId" placeholder="请选择商品" style="width: 100%" filterable>
            <el-option label="示例商品A" :value="1" />
            <el-option label="示例商品B" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number v-model="dialogForm.originalPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="秒杀价" prop="salePrice">
          <el-input-number v-model="dialogForm.salePrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="dialogForm.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="活动时间" prop="timeRange">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saveLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getFlashSaleList, createFlashSale, updateFlashSale, deleteFlashSale, type FlashSaleActivity, type FlashSaleQuery } from '@/api/marketing'

const loading = ref(false)
const saveLoading = ref(false)
const tableData = ref<FlashSaleActivity[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogFormRef = ref<FormInstance>()
const timeRange = ref<string[]>([])

const queryForm = reactive<FlashSaleQuery>({
  page: 1,
  pageSize: 10,
  name: '',
  status: undefined
})

const dialogForm = reactive<Partial<FlashSaleActivity>>({
  name: '',
  goodsId: undefined,
  goodsName: '',
  originalPrice: 0,
  salePrice: 0,
  stock: 0,
  startTime: '',
  endTime: '',
  status: 0
})

const dialogRules: FormRules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  goodsId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  originalPrice: [{ required: true, message: '请输入原价', trigger: 'blur' }],
  salePrice: [{ required: true, message: '请输入秒杀价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

function getStatusType(status: number) {
  const map: Record<number, any> = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

function getStatusText(status: number) {
  const map: Record<number, string> = { 0: '未开始', 1: '进行中', 2: '已结束' }
  return map[status] || '未知'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getFlashSaleList(queryForm)
    tableData.value = res.list
    total.value = res.total
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.page = 1
  queryForm.name = ''
  queryForm.status = undefined
  loadData()
}

function handleCreate() {
  isEdit.value = false
  Object.assign(dialogForm, {
    name: '',
    goodsId: undefined,
    goodsName: '',
    originalPrice: 0,
    salePrice: 0,
    stock: 0,
    startTime: '',
    endTime: '',
    status: 0
  })
  timeRange.value = []
  dialogVisible.value = true
}

function handleEdit(row: FlashSaleActivity) {
  isEdit.value = true
  Object.assign(dialogForm, { ...row })
  timeRange.value = [row.startTime, row.endTime]
  dialogVisible.value = true
}

function handleQrcode(row: FlashSaleActivity) {
  ElMessage.info('小程序码功能待实现')
}

async function handleSave() {
  if (!dialogFormRef.value) return
  if (timeRange.value && timeRange.value.length === 2) {
    dialogForm.startTime = timeRange.value[0]
    dialogForm.endTime = timeRange.value[1]
  }
  await dialogFormRef.value.validate(async (valid) => {
    if (valid) {
      saveLoading.value = true
      try {
        if (isEdit.value && dialogForm.id) {
          await updateFlashSale(dialogForm.id, dialogForm)
          ElMessage.success('更新成功')
        } else {
          await createFlashSale(dialogForm)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (e) {
        ElMessage.error('保存失败')
      } finally {
        saveLoading.value = false
      }
    }
  })
}

async function handleDelete(row: FlashSaleActivity) {
  try {
    await ElMessageBox.confirm('确定要删除该活动吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteFlashSale(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.flash-sale {
  .search-form {
    margin-bottom: 16px;
  }

  .toolbar {
    margin-bottom: 16px;
  }

  .el-pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
