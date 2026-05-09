<template>
  <div class="staff-list">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="姓名/手机号">
          <el-input v-model="queryForm.keyword" placeholder="请输入姓名或手机号" clearable />
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
          新增员工
        </el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="username" label="账号" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            <el-tag :type="row.role === 'shop_admin' ? 'primary' : 'info'">
              {{ row.role === 'shop_admin' ? '门店管理员' : '员工' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
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
      :title="isEdit ? '编辑员工' : '新增员工'"
      width="600px"
      @close="dialogFormRef?.resetFields()"
    >
      <el-form
        ref="dialogFormRef"
        :model="dialogForm"
        :rules="dialogRules"
        label-width="100px"
      >
        <el-form-item label="账号" prop="username">
          <el-input v-model="dialogForm.username" placeholder="请输入账号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="dialogForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="dialogForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="dialogForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="dialogForm.role">
            <el-radio value="shop_admin">门店管理员</el-radio>
            <el-radio value="shop_staff">员工</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dialogForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
  getStaffList,
  createStaff,
  updateStaff,
  deleteStaff,
  type Staff,
  type StaffQuery
} from '@/api/shop'

const loading = ref(false)
const saveLoading = ref(false)
const tableData = ref<Staff[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogFormRef = ref<FormInstance>()

const queryForm = reactive<StaffQuery & { keyword?: string }>({
  page: 1,
  pageSize: 10
})

const dialogForm = reactive<Partial<Staff>>({
  username: '',
  password: '',
  name: '',
  phone: '',
  role: 'shop_staff',
  status: 1
})

const dialogRules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

async function loadData() {
  loading.value = true
  try {
    const params: StaffQuery = {
      page: queryForm.page,
      pageSize: queryForm.pageSize
    }
    if (queryForm.keyword) {
      params.name = queryForm.keyword
    }
    const res = await getStaffList(params)
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
  queryForm.keyword = ''
  loadData()
}

function handleCreate() {
  isEdit.value = false
  Object.assign(dialogForm, {
    username: '',
    password: '',
    name: '',
    phone: '',
    role: 'shop_staff',
    status: 1
  })
  dialogVisible.value = true
}

function handleEdit(row: Staff) {
  isEdit.value = true
  dialogForm.id = row.id
  dialogForm.username = row.username
  dialogForm.name = row.name
  dialogForm.phone = row.phone
  dialogForm.role = row.role
  dialogForm.status = row.status
  dialogVisible.value = true
}

async function handleSave() {
  if (!dialogFormRef.value) return
  await dialogFormRef.value.validate(async (valid) => {
    if (valid) {
      saveLoading.value = true
      try {
        if (isEdit.value && dialogForm.id) {
          await updateStaff(dialogForm.id, dialogForm)
          ElMessage.success('更新成功')
        } else {
          await createStaff(dialogForm)
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

async function handleDelete(row: Staff) {
  try {
    await ElMessageBox.confirm('确定要删除该员工吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteStaff(row.id)
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
.staff-list {
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
