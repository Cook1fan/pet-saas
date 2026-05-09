<template>
  <div class="member-detail">
    <el-page-header @back="goBack" content="会员详情" />

    <!-- 会员基本信息 -->
    <el-card class="mt-md">
      <template #header>
        <div class="card-header">
          <span>会员基本信息</span>
          <el-button type="primary" size="small" @click="handleEditMember">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
        </div>
      </template>
      <el-descriptions :column="2" border v-if="memberInfo">
        <el-descriptions-item label="姓名">{{ memberInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ memberInfo.phone }}</el-descriptions-item>
        <el-descriptions-item label="标签">
          <el-tag v-for="tag in getTagList(memberInfo.tags)" :key="tag" :type="getTagType(tag)" size="small" style="margin-right: 4px">
            {{ tag }}
          </el-tag>
          <span v-if="!getTagList(memberInfo.tags)?.length">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ memberInfo.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 储值账户信息 -->
    <el-card class="mt-md">
      <template #header>
        <div class="card-header">
          <span>储值账户</span>
          <el-button type="primary" size="small" @click="goToRechargeRecords">
            查看核销记录
          </el-button>
        </div>
      </template>
      <el-descriptions :column="2" border v-if="accountInfo">
        <el-descriptions-item label="当前余额">
          <span class="balance-text">¥{{ accountInfo.balance }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="累计储值">¥{{ accountInfo.totalRecharge }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无储值账户信息" />
    </el-card>

    <!-- 宠物档案 -->
    <el-card class="mt-md">
      <template #header>
        <div class="card-header">
          <span>宠物档案</span>
          <el-button type="primary" size="small" @click="handleAddPet">
            <el-icon><Plus /></el-icon>
            添加宠物
          </el-button>
        </div>
      </template>
      <div class="pet-list" v-if="memberInfo?.pets?.length">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="pet in memberInfo.pets" :key="pet.id">
            <el-card class="pet-card" shadow="hover">
              <template #header>
                <div class="pet-card-header">
                  <span class="pet-name">{{ pet.name }}</span>
                  <el-dropdown>
                    <el-icon class="more-icon"><MoreFilled /></el-icon>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item @click="handleEditPet(pet)">编辑</el-dropdown-item>
                        <el-dropdown-item @click="handleDeletePet(pet)" divided>删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </template>
              <div class="pet-info">
                <p><span class="label">品种：</span>{{ pet.breed || '-' }}</p>
                <p><span class="label">性别：</span>{{ getGenderText(pet.gender) }}</p>
                <p><span class="label">生日：</span>{{ pet.birthday || '-' }}</p>
                <el-divider style="margin: 10px 0" />
                <p :class="{ 'warning-text': isDateExpired(pet.vaccineTime) }">
                  <span class="label">疫苗时间：</span>{{ pet.vaccineTime || '-' }}
                </p>
                <p :class="{ 'warning-text': isDateExpired(pet.dewormTime) }">
                  <span class="label">驱虫时间：</span>{{ pet.dewormTime || '-' }}
                </p>
                <p :class="{ 'warning-text': isDateExpired(pet.washTime) }">
                  <span class="label">洗护时间：</span>{{ pet.washTime || '-' }}
                </p>
                <p v-if="pet.nextRemindTime" class="remind-text">
                  <span class="label">下次提醒：</span>{{ pet.nextRemindTime }}
                </p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      <el-empty v-else description="暂无宠物档案" />
    </el-card>

    <!-- 编辑会员弹窗 -->
    <el-dialog
      v-model="memberDialogVisible"
      title="编辑会员"
      width="500px"
    >
      <el-form ref="memberFormRef" :model="memberForm" :rules="memberRules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="memberForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="selectedTags" multiple placeholder="请选择标签" style="width: 100%">
            <el-option v-for="tag in PRESET_TAGS" :key="tag.value" :label="tag.value" :value="tag.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="memberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveMember" :loading="memberSaveLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑/添加宠物弹窗 -->
    <el-dialog
      v-model="petDialogVisible"
      :title="isPetEdit ? '编辑宠物' : '添加宠物'"
      width="500px"
    >
      <el-form ref="petFormRef" :model="petForm" :rules="petRules" label-width="100px">
        <el-form-item label="名字" prop="name">
          <el-input v-model="petForm.name" placeholder="请输入名字" />
        </el-form-item>
        <el-form-item label="品种" prop="breed">
          <el-input v-model="petForm.breed" placeholder="请输入品种" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="petForm.gender">
            <el-radio :value="0">公</el-radio>
            <el-radio :value="1">母</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="petForm.birthday"
            type="date"
            placeholder="请选择生日"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="疫苗时间">
          <el-date-picker
            v-model="petForm.vaccineTime"
            type="date"
            placeholder="请选择疫苗时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="驱虫时间">
          <el-date-picker
            v-model="petForm.dewormTime"
            type="date"
            placeholder="请选择驱虫时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="洗护时间">
          <el-date-picker
            v-model="petForm.washTime"
            type="date"
            placeholder="请选择洗护时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="petDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePet" :loading="petSaveLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Edit, Plus, MoreFilled } from '@element-plus/icons-vue'
import {
  getMemberAccount,
  PRESET_TAGS,
  type MemberVO,
  type MemberAccountVO,
  type PetInfo
} from '@/api/member'

const route = useRoute()
const router = useRouter()

const memberInfo = ref<(MemberVO & { pets?: PetInfo[] }) | null>(null)
const accountInfo = ref<MemberAccountVO | null>(null)

const memberDialogVisible = ref(false)
const memberSaveLoading = ref(false)
const memberFormRef = ref<FormInstance>()
const selectedTags = ref<string[]>([])

const petDialogVisible = ref(false)
const isPetEdit = ref(false)
const petSaveLoading = ref(false)
const petFormRef = ref<FormInstance>()
const editingPetId = ref<number | null>(null)

const memberForm = reactive<Partial<MemberVO>>({
  name: '',
  tags: ''
})

const petForm = reactive<Partial<PetInfo>>({
  name: '',
  breed: '',
  gender: undefined,
  birthday: '',
  vaccineTime: '',
  dewormTime: '',
  washTime: ''
})

const memberRules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const petRules: FormRules = {
  name: [{ required: true, message: '请输入名字', trigger: 'blur' }]
}

function goBack() {
  router.back()
}

function goToRechargeRecords() {
  router.push('/shop/recharge/records')
}

function getTagList(tags?: string): string[] {
  if (!tags) return []
  return tags.split(',').filter(t => t.trim())
}

function getTagType(tag: string): '' | 'success' | 'warning' | 'info' | 'primary' | 'danger' {
  const found = PRESET_TAGS.find(t => t.value === tag)
  return found?.color || ''
}

function getGenderText(gender?: number): string {
  if (gender === 0) return '公'
  if (gender === 1) return '母'
  return '-'
}

function isDateExpired(dateStr?: string): boolean {
  if (!dateStr) return false
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = diff / (1000 * 60 * 60 * 24)
  return days > 90  // 超过90天显示警告
}

async function loadDetail() {
  const id = Number(route.params.id)
  if (!id) return
  try {
    // TODO: 待后端补充获取会员详情接口（含宠物列表）
    // memberInfo.value = await getMemberDetail(id)

    // 模拟数据
    memberInfo.value = {
      id,
      name: '张三',
      phone: '13888888888',
      tags: 'VIP客户,养猫',
      createTime: '2024-01-01 10:00:00',
      pets: [
        {
          id: 1,
          name: '咪咪',
          breed: '英短蓝猫',
          gender: 1,
          birthday: '2023-01-01',
          vaccineTime: '2024-01-15',
          dewormTime: '2024-02-01',
          washTime: '2024-02-10'
        }
      ]
    }

    // 加载储值账户
    await loadAccount(id)
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

async function loadAccount(memberId: number) {
  try {
    accountInfo.value = await getMemberAccount(memberId)
  } catch (e) {
    console.error('加载储值账户失败', e)
  }
}

function handleEditMember() {
  if (!memberInfo.value) return
  memberForm.name = memberInfo.value.name
  memberForm.tags = memberInfo.value.tags
  selectedTags.value = getTagList(memberInfo.value.tags)
  memberDialogVisible.value = true
}

async function handleSaveMember() {
  if (!memberFormRef.value) return
  memberForm.tags = selectedTags.value.join(',')
  await memberFormRef.value.validate(async (valid) => {
    if (valid) {
      memberSaveLoading.value = true
      try {
        // TODO: 待后端补充更新会员接口
        ElMessage.warning('编辑功能待后端接口支持')
        memberDialogVisible.value = false
        // loadDetail()
      } catch (e) {
        ElMessage.error('保存失败')
      } finally {
        memberSaveLoading.value = false
      }
    }
  })
}

function handleAddPet() {
  isPetEdit.value = false
  editingPetId.value = null
  Object.assign(petForm, {
    name: '',
    breed: '',
    gender: undefined,
    birthday: '',
    vaccineTime: '',
    dewormTime: '',
    washTime: ''
  })
  petDialogVisible.value = true
}

function handleEditPet(pet: PetInfo) {
  isPetEdit.value = true
  editingPetId.value = pet.id || null
  Object.assign(petForm, {
    name: pet.name,
    breed: pet.breed,
    gender: pet.gender,
    birthday: pet.birthday,
    vaccineTime: pet.vaccineTime,
    dewormTime: pet.dewormTime,
    washTime: pet.washTime
  })
  petDialogVisible.value = true
}

async function handleSavePet() {
  if (!petFormRef.value) return
  await petFormRef.value.validate(async (valid) => {
    if (valid) {
      petSaveLoading.value = true
      try {
        if (isPetEdit.value && editingPetId.value) {
          // TODO: 待后端补充更新宠物接口
          ElMessage.warning('编辑功能待后端接口支持')
        } else {
          // TODO: 待后端补充添加宠物接口
          ElMessage.warning('添加功能待后端接口支持')
        }
        petDialogVisible.value = false
        // loadDetail()
      } catch (e) {
        ElMessage.error('保存失败')
      } finally {
        petSaveLoading.value = false
      }
    }
  })
}

async function handleDeletePet(pet: PetInfo) {
  try {
    await ElMessageBox.confirm('确定要删除该宠物吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // TODO: 待后端补充删除宠物接口
    ElMessage.warning('删除功能待后端接口支持')
    // await deletePet(memberInfo.value!.id, pet.id!)
    // ElMessage.success('删除成功')
    // loadDetail()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
.member-detail {
  .mt-md {
    margin-top: 16px;
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .balance-text {
    color: #f56c6c;
    font-weight: bold;
    font-size: 18px;
  }

  .pet-list {
    .pet-card {
      margin-bottom: 20px;

      .pet-card-header {
        display: flex;
        align-items: center;
        justify-content: space-between;

        .pet-name {
          font-weight: bold;
          font-size: 16px;
        }

        .more-icon {
          cursor: pointer;
          font-size: 18px;
        }
      }

      .pet-info {
        p {
          margin: 8px 0;
          font-size: 14px;

          .label {
            color: #909399;
          }
        }

        .warning-text {
          color: #e6a23c;
        }

        .remind-text {
          color: #f56c6c;
          font-weight: bold;
        }
      }
    }
  }
}
</style>
