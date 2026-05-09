<template>
  <el-dialog
    v-model="visible"
    title="选择会员"
    width="800px"
    @close="handleClose"
  >
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-input
        v-model="searchKeyword"
        placeholder="请输入手机号或姓名搜索"
        clearable
        @keyup.enter="handleSearch"
        @input="handleInput"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch" :loading="loading">
        搜索
      </el-button>
    </div>

    <!-- 会员列表 -->
    <el-table
      :data="tableData"
      stripe
      style="margin-top: 16px"
      height="400"
      @row-click="handleRowClick"
      highlight-current-row
    >
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="tags" label="标签">
        <template #default="{ row }">
          <el-tag
            v-for="tag in getTagList(row.tags)"
            :key="tag"
            :type="getTagType(tag)"
            size="small"
            style="margin-right: 4px"
          >
            {{ tag }}
          </el-tag>
          <span v-if="!getTagList(row.tags)?.length">-</span>
        </template>
      </el-table-column>
      <el-table-column label="宠物概要">
        <template #default="{ row }">
          <span>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="primary" link @click.stop="handleSelect(row)">
            选择
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="queryForm.pageNum"
      v-model:page-size="queryForm.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadData"
      @current-change="loadData"
      style="margin-top: 16px; justify-content: flex-end"
    />

    <!-- 选择宠物区域（需要选择宠物时显示） -->
    <div v-if="needPetSelect && selectedMember" class="pet-select-area">
      <el-divider>选择宠物（可选）</el-divider>
      <el-radio-group v-model="selectedPetId">
        <el-radio :value="null">不选择宠物</el-radio>
        <el-radio v-for="pet in mockPets" :key="pet.id" :value="pet.id">
          {{ pet.name }}（{{ pet.breed }}）
        </el-radio>
      </el-radio-group>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleConfirmSelect" :disabled="!selectedMember">
        确认选择
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import {
  getMemberList,
  PRESET_TAGS,
  type MemberVO,
  type MemberListParams,
  type PetInfo
} from '@/api/member'

interface Props {
  modelValue: boolean
  needPetSelect?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'select', member: MemberVO & { pets?: PetInfo[], selectedPet?: PetInfo }): void
}

const props = withDefaults(defineProps<Props>(), {
  needPetSelect: false
})

const emit = defineEmits<Emits>()

const loading = ref(false)
const tableData = ref<MemberVO[]>([])
const total = ref(0)
const searchKeyword = ref('')
let searchTimer: number | null = null

const selectedMember = ref<MemberVO | null>(null)
const selectedPetId = ref<number | null>(null)

// 模拟宠物数据（待后端接口）
const mockPets: PetInfo[] = [
  { id: 1, name: '咪咪', breed: '英短蓝猫', gender: 1 },
  { id: 2, name: '旺财', breed: '金毛', gender: 0 }
]

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const queryForm = reactive<MemberListParams>({
  pageNum: 1,
  pageSize: 10,
  keyword: ''
})

function getTagList(tags?: string): string[] {
  if (!tags) return []
  return tags.split(',').filter(t => t.trim())
}

function getTagType(tag: string): '' | 'success' | 'warning' | 'info' | 'primary' | 'danger' {
  const found = PRESET_TAGS.find(t => t.value === tag)
  return found?.color || ''
}

async function loadData() {
  loading.value = true
  try {
    const res = await getMemberList(queryForm)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryForm.pageNum = 1
  queryForm.keyword = searchKeyword.value.trim()
  loadData()
}

function handleInput() {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = window.setTimeout(() => {
    handleSearch()
  }, 300)
}

function handleRowClick(row: MemberVO) {
  selectedMember.value = row
  if (props.needPetSelect) {
    selectedPetId.value = null
  }
}

function handleSelect(row: MemberVO) {
  selectedMember.value = row
  if (props.needPetSelect) {
    selectedPetId.value = null
  } else {
    handleConfirmSelect()
  }
}

function handleConfirmSelect() {
  if (!selectedMember.value) return

  let selectedPet: PetInfo | undefined
  if (props.needPetSelect && selectedPetId.value) {
    selectedPet = mockPets.find(p => p.id === selectedPetId.value)
  }

  emit('select', {
    ...selectedMember.value,
    pets: mockPets,
    selectedPet
  })
  visible.value = false
}

function handleClose() {
  selectedMember.value = null
  selectedPetId.value = null
  searchKeyword.value = ''
  queryForm.keyword = ''
  queryForm.pageNum = 1
}

watch(visible, (newVal) => {
  if (newVal) {
    loadData()
  }
})
</script>

<style scoped lang="scss">
.search-area {
  display: flex;
  gap: 12px;
  align-items: center;

  .el-input {
    flex: 1;
  }
}

.pet-select-area {
  margin-top: 16px;
  padding-top: 8px;
}
</style>
