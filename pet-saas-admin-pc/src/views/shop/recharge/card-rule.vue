<template>
  <div class="card-rule">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="规则名称">
          <el-input v-model="queryForm.name" placeholder="请输入规则名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
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
          新增规则
        </el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="name" label="规则名称" />
        <el-table-column prop="totalTimes" label="次数" />
        <el-table-column prop="price" label="售价">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="validDays" label="有效期(天)">
          <template #default="{ row }">
            {{ row.validDays || '永久' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="warning" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              删除
            </el-button>
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
      :title="isEdit ? '编辑次卡规则' : '新增次卡规则'"
      width="500px"
      @close="dialogFormRef?.resetFields()"
    >
      <el-form
        ref="dialogFormRef"
        :model="dialogForm"
        :rules="dialogRules"
        label-width="100px"
      >
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="dialogForm.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="次数" prop="totalTimes">
          <el-input-number v-model="dialogForm.totalTimes" :min="1" placeholder="请输入次数" style="width: 100%" />
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number v-model="dialogForm.price" :min="0" :precision="2" placeholder="请输入售价" style="width: 100%" />
        </el-form-item>
        <el-form-item label="有效期(天)">
          <el-input-number v-model="dialogForm.validDays" :min="0" placeholder="不填表示永久有效" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saveLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getCardRuleList,
  createCardRule,
  updateCardRule,
  deleteCardRule,
  type CardRule,
  type CardRuleQuery
} from '@/api/recharge-card'

const loading = ref(false)
const saveLoading = ref(false)
const tableData = ref<CardRule[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogFormRef = ref<FormInstance>()

const queryForm = reactive<CardRuleQuery>({
  page: 1,
  pageSize: 10,
  name: '',
  status: undefined
})

const dialogForm = reactive<Partial<CardRule>>({
  name: '',
  totalTimes: 1,
  price: 0,
  validDays: undefined,
  status: 1
})

const dialogRules: FormRules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  totalTimes: [{ required: true, message: '请输入次数', trigger: 'blur' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getCardRuleList(queryForm)
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
    totalTimes: 1,
    price: 0,
    validDays: undefined,
    status: 1
  })
  dialogVisible.value = true
}

function handleEdit(row: CardRule) {
  isEdit.value = true
  dialogForm.id = row.id
  dialogForm.name = row.name
  dialogForm.totalTimes = row.totalTimes
  dialogForm.price = row.price
  dialogForm.validDays = row.validDays
  dialogForm.status = row.status
  dialogVisible.value = true
}

async function handleToggleStatus(row: CardRule) {
  try {
    await ElMessageBox.confirm(`确定要${row.status === 1 ? '禁用' : '启用'}该规则吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateCardRule(row.id, { status: row.status === 1 ? 0 : 1 })
    ElMessage.success('操作成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

async function handleSave() {
  if (!dialogFormRef.value) return
  await dialogFormRef.value.validate(async (valid) => {
    if (valid) {
      saveLoading.value = true
      try {
        if (isEdit.value && dialogForm.id) {
          await updateCardRule(dialogForm.id, dialogForm)
          ElMessage.success('更新成功')
        } else {
          await createCardRule(dialogForm)
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

async function handleDelete(row: CardRule) {
  try {
    await ElMessageBox.confirm('确定要删除该规则吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCardRule(row.id)
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
.card-rule {
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
