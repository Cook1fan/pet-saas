<template>
  <div class="member-list">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="姓名/手机号">
          <el-input v-model="queryForm.keyword" placeholder="请输入姓名或手机号" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="queryForm.tag" placeholder="请选择标签" clearable style="width: 150px">
            <el-option v-for="tag in PRESET_TAGS" :key="tag.value" :label="tag.value" :value="tag.value" />
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
          新增会员
        </el-button>
        <el-button @click="handleImport">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="tags" label="标签">
          <template #default="{ row }">
            <el-tag v-for="tag in getTagList(row.tags)" :key="tag" :type="getTagType(tag) || undefined" size="small" style="margin-right: 4px">
              {{ tag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="宠物概要">
          <template #default="{ row }">
            <span v-if="row.pets && row.pets.length">
              {{ row.pets.length }}只宠物：{{ row.pets.map((p: any) => p.name).join('、') }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">
              查看
            </el-button>
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
        v-model:current-page="queryForm.pageNum"
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
      :title="isEdit ? '编辑会员' : '新增会员'"
      width="1200px"
      top="5vh"
      :close-on-click-modal="false"
      @close="dialogFormRef?.resetFields()"
    >
      <el-form
        ref="dialogFormRef"
        :model="dialogForm"
        :rules="dialogRules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="dialogForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="dialogForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="selectedTags" multiple placeholder="请选择标签" style="width: 100%">
            <el-option v-for="tag in PRESET_TAGS" :key="tag.value" :label="tag.value" :value="tag.value" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">宠物信息（可选）</el-divider>
        <div class="pet-table-wrapper">
          <el-table :data="dialogForm.pets || []" border height="400">
            <el-table-column prop="name" label="名字" width="160">
              <template #default="{ row, $index }">
                <el-input v-model="row.name" placeholder="请输入名字" />
              </template>
            </el-table-column>
            <el-table-column prop="breed" label="品种" min-width="160">
              <template #default="{ row }">
                <el-input v-model="row.breed" placeholder="请输入品种" />
              </template>
            </el-table-column>
            <el-table-column prop="gender" label="性别" width="120">
              <template #default="{ row }">
                <el-radio-group v-model="row.gender">
                  <el-radio :value="0">公</el-radio>
                  <el-radio :value="1">母</el-radio>
                </el-radio-group>
              </template>
            </el-table-column>
            <el-table-column prop="birthday" label="生日" width="180">
              <template #default="{ row }">
                <el-date-picker
                  v-model="row.birthday"
                  type="date"
                  placeholder="请选择生日"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column prop="vaccineTime" label="疫苗时间" width="180">
              <template #default="{ row }">
                <el-date-picker
                  v-model="row.vaccineTime"
                  type="date"
                  placeholder="疫苗时间"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column prop="dewormTime" label="驱虫时间" width="180">
              <template #default="{ row }">
                <el-date-picker
                  v-model="row.dewormTime"
                  type="date"
                  placeholder="驱虫时间"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column prop="washTime" label="洗护时间" width="180">
              <template #default="{ row }">
                <el-date-picker
                  v-model="row.washTime"
                  type="date"
                  placeholder="洗护时间"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ $index }">
                <el-button type="danger" link @click="removePet($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="primary" text style="margin-top: 8px" @click="addPet">
            <el-icon><Plus /></el-icon>
            添加宠物
          </el-button>
        </div>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Refresh, Plus, Upload } from '@element-plus/icons-vue'
import {
  getMemberList,
  createMember,
  updateMember,
  PRESET_TAGS,
  type MemberVO,
  type MemberListParams,
  type CreateMemberReq,
  type UpdateMemberReq,
  type PetInfo
} from '@/api/member'

const router = useRouter()

const loading = ref(false)
const saveLoading = ref(false)
const tableData = ref<MemberVO[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogFormRef = ref<FormInstance>()
const selectedTags = ref<string[]>([])

const queryForm = reactive<MemberListParams>({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  tag: ''
})

const dialogForm = reactive<CreateMemberReq & { id?: number }>({
  phone: '',
  name: '',
  tags: '',
  pets: []
})

const dialogRules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

function getTagList(tags?: string | null): string[] {
  if (!tags) return []
  // 支持中文逗号和英文逗号
  return tags.split(/[，,]/).filter(t => t.trim())
}

function getTagType(tag: string): '' | 'success' | 'warning' | 'info' | 'primary' | 'danger' {
  const found = PRESET_TAGS.find(t => t.value === tag)
  return found?.color || ''
}

async function loadData() {
  loading.value = true
  try {
    const params: MemberListParams = {
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    }
    if (queryForm.keyword) {
      params.keyword = queryForm.keyword
    }
    if (queryForm.tag) {
      params.tag = queryForm.tag
    }
    const res = await getMemberList(params)
    console.log('会员列表接口返回:', res)
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
  queryForm.keyword = ''
  queryForm.tag = ''
  loadData()
}

function handleCreate() {
  isEdit.value = false
  dialogForm.id = undefined
  dialogForm.name = ''
  dialogForm.phone = ''
  dialogForm.tags = ''
  dialogForm.pets = []
  selectedTags.value = []
  dialogVisible.value = true
}

function handleEdit(row: MemberVO) {
  isEdit.value = true
  dialogForm.id = row.id
  dialogForm.name = row.name
  dialogForm.phone = row.phone
  dialogForm.tags = row.tags || ''
  // 深拷贝宠物信息，避免直接修改原数据
  dialogForm.pets = row.pets ? JSON.parse(JSON.stringify(row.pets)) : []
  selectedTags.value = getTagList(row.tags)
  dialogVisible.value = true
}

function handleDetail(row: MemberVO) {
  router.push(`/shop/member/detail/${row.id}`)
}

function handleImport() {
  router.push('/shop/member/import')
}

function addPet() {
  if (!dialogForm.pets) {
    dialogForm.pets = []
  }
  dialogForm.pets.push({
    name: '',
    breed: '',
    gender: undefined,
    birthday: '',
    vaccineTime: '',
    dewormTime: '',
    washTime: ''
  })
}

function removePet(index: number) {
  dialogForm.pets?.splice(index, 1)
}

async function handleSave() {
  if (!dialogFormRef.value) return
  dialogForm.tags = selectedTags.value.join(',') || undefined
  await dialogFormRef.value.validate(async (valid) => {
    if (valid) {
      saveLoading.value = true
      try {
        if (isEdit.value && dialogForm.id) {
          const updateData: UpdateMemberReq = {
            id: dialogForm.id,
            name: dialogForm.name,
            phone: dialogForm.phone,
            tags: dialogForm.tags,
            pets: dialogForm.pets
          }
          await updateMember(updateData)
          ElMessage.success('更新成功')
        } else {
          await createMember(dialogForm)
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

async function handleDelete(row: MemberVO) {
  try {
    await ElMessageBox.confirm('确定要删除该会员吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // TODO: 待后端补充删除接口
    ElMessage.warning('删除功能待后端接口支持')
    // await deleteMember(row.id)
    // ElMessage.success('删除成功')
    // loadData()
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
.member-list {
  .search-form {
    margin-bottom: 16px;
  }

  .toolbar {
    margin-bottom: 16px;
  }

  .pet-table-wrapper {
    overflow-x: auto;
  }

  .el-pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}

// 弹窗样式全局覆盖
:deep(.el-dialog) {
  .el-dialog__body {
    max-height: 70vh;
    overflow-y: auto;
    padding: 20px;
  }
}
</style>
