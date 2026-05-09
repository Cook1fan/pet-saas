<template>
  <div class="tenant-list">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="门店名称">
          <el-input v-model="queryForm.shopName" placeholder="请输入门店名称" clearable />
        </el-form-item>
        <el-form-item label="老板手机号">
          <el-input v-model="queryForm.adminPhone" placeholder="请输入老板手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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
          创建租户
        </el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="shopName" label="门店名称" />
        <el-table-column prop="adminPhone" label="老板手机号" />
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="primary" link @click="handleResetPassword(row)">
              重置密码
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
      title="创建租户"
      width="600px"
      @close="dialogFormRef?.resetFields()"
    >
      <el-form
        ref="dialogFormRef"
        :model="dialogForm"
        :rules="dialogRules"
        label-width="120px"
      >
        <el-form-item label="门店名称" prop="shopName">
          <el-input v-model="dialogForm.shopName" placeholder="请输入门店名称" />
        </el-form-item>
        <el-form-item label="老板手机号" prop="adminPhone">
          <el-input v-model="dialogForm.adminPhone" placeholder="请输入老板手机号" />
        </el-form-item>
        <el-form-item label="门店地址" prop="address">
          <el-input v-model="dialogForm.address" placeholder="请输入门店地址" />
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
  getTenantList,
  createTenant,
  toggleTenantStatus,
  resetTenantPassword,
  type Tenant,
  type TenantQuery,
  type CreateTenantReq
} from '@/api/platform'

const loading = ref(false)
const saveLoading = ref(false)
const tableData = ref<Tenant[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogFormRef = ref<FormInstance>()

const queryForm = reactive<TenantQuery>({
  page: 1,
  pageSize: 10,
  shopName: '',
  adminPhone: '',
  status: undefined
})

const dialogForm = reactive<CreateTenantReq>({
  shopName: '',
  adminPhone: '',
  address: ''
})

// 中国手机号验证正则
const validatePhone = (_rule: any, value: any, callback: any) => {
  const phoneReg = /^1[3-9]\d{9}$/
  if (!value) {
    callback(new Error('请输入老板手机号'))
  } else if (!phoneReg.test(value)) {
    callback(new Error('请输入正确的中国手机号'))
  } else {
    callback()
  }
}

const dialogRules: FormRules = {
  shopName: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
  adminPhone: [{ required: true, validator: validatePhone, trigger: 'blur' }],
  address: [{ required: true, message: '请输入门店地址', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getTenantList(queryForm)
    tableData.value = res.records
    total.value = res.total
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.page = 1
  queryForm.shopName = ''
  queryForm.adminPhone = ''
  queryForm.status = undefined
  loadData()
}

function handleCreate() {
  Object.assign(dialogForm, {
    shopName: '',
    adminPhone: '',
    address: ''
  })
  dialogVisible.value = true
}

async function handleSave() {
  if (!dialogFormRef.value) return
  await dialogFormRef.value.validate(async (valid) => {
    if (valid) {
      saveLoading.value = true
      try {
        await createTenant(dialogForm)
        ElMessage.success('创建成功')
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

async function handleToggleStatus(row: Tenant) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${action}该租户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await toggleTenantStatus(row.tenantId, newStatus)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

async function handleResetPassword(row: Tenant) {
  try {
    await ElMessageBox.confirm('确定要重置该租户的密码吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await resetTenantPassword(row.tenantId)
    ElMessage.success('重置密码成功')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('重置密码失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.tenant-list {
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
