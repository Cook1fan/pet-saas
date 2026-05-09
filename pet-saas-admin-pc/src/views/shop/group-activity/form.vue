<template>
  <div class="group-activity-form">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button @click="handleBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <span>{{ isEdit ? '编辑拼团活动' : '新建拼团活动' }}</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" style="max-width: 800px">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入活动标题" />
        </el-form-item>
        <el-form-item label="选择商品" prop="goodsId">
          <el-button type="primary" @click="handleSelectGoods">
            <el-icon><Goods /></el-icon>
            选择商品
          </el-button>
          <div v-if="selectedGoods" class="selected-goods">
            <el-image :src="selectedGoods.image" fit="cover" style="width: 80px; height: 80px" />
            <div class="goods-info">
              <div class="goods-name">{{ selectedGoods.name }}</div>
              <div class="goods-price">¥{{ selectedGoods.price }}</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item v-if="selectedGoods && selectedGoods.skus" label="选择规格" prop="skuId">
          <el-radio-group v-model="form.skuId">
            <el-radio v-for="sku in selectedGoods.skus" :key="sku.id" :value="sku.id">
              {{ sku.specs.join(' / ') }} - 库存: {{ sku.stock }} - 价格: ¥{{ sku.price }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" disabled style="width: 200px" />
          <span style="margin-left: 8px; color: #999">（从商品自动获取）</span>
        </el-form-item>
        <el-form-item label="拼团价格" prop="groupPrice">
          <el-input-number v-model="form.groupPrice" :min="0" :precision="2" style="width: 200px" />
          <span style="margin-left: 8px; color: #f56c6c">（必须小于原价）</span>
        </el-form-item>
        <el-form-item label="成团人数" prop="groupSize">
          <el-input-number v-model="form.groupSize" :min="2" :max="10" style="width: 200px" />
          <span style="margin-left: 8px; color: #999">（2-10人）</span>
        </el-form-item>
        <el-form-item label="拼团有效期" prop="expireHours">
          <el-input-number v-model="form.expireHours" :min="1" :max="168" style="width: 200px" />
          <span style="margin-left: 8px; color: #999">（小时，默认24）</span>
        </el-form-item>
        <el-form-item label="活动库存" prop="stock">
          <el-input-number v-model="form.stock" :min="1" :max="maxStock" style="width: 200px" />
          <span style="margin-left: 8px; color: #999">（不能超过商品库存: {{ maxStock }}）</span>
        </el-form-item>
        <el-form-item label="限购数量" prop="limitPerUser">
          <el-input-number v-model="form.limitPerUser" :min="1" style="width: 200px" />
          <span style="margin-left: 8px; color: #999">（每人最多购买几件）</span>
        </el-form-item>
        <el-form-item label="活动开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="活动结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            <el-icon><Check /></el-icon>
            提交
          </el-button>
          <el-button @click="handleBack">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 商品选择弹窗 -->
    <el-dialog v-model="goodsDialogVisible" title="选择商品" width="800px">
      <el-table :data="goodsList" stripe @row-click="handleGoodsRowClick" highlight-current-row>
        <el-table-column label="商品图片" width="80">
          <template #default="{ row }">
            <el-image :src="row.image" fit="cover" style="width: 60px; height: 60px" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
      </el-table>
      <template #footer>
        <el-button @click="goodsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmGoods">确认选择</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft, Check, Close, Goods } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { createGroupActivity, type CreateGroupActivityRequest } from '@/api/groupActivity'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const goodsDialogVisible = ref(false)
const isEdit = ref(false)

const form = reactive<CreateGroupActivityRequest>({
  title: '',
  goodsId: 0,
  skuId: 0,
  groupPrice: 0,
  groupSize: 2,
  expireHours: 24,
  stock: 0,
  limitPerUser: 1,
  startTime: '',
  endTime: ''
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  goodsId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  skuId: [{ required: true, message: '请选择规格', trigger: 'change' }],
  groupPrice: [
    { required: true, message: '请输入拼团价格', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value >= form.originalPrice) {
          callback(new Error('拼团价格必须小于原价'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  groupSize: [
    { required: true, message: '请输入成团人数', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value < 2 || value > 10) {
          callback(new Error('成团人数需在2-10人之间'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  expireHours: [{ required: true, message: '请输入拼团有效期', trigger: 'blur' }],
  stock: [
    { required: true, message: '请输入活动库存', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value > maxStock.value) {
          callback(new Error(`活动库存不能超过商品库存: ${maxStock.value}`))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  limitPerUser: [{ required: true, message: '请输入限购数量', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择活动开始时间', trigger: 'change' }],
  endTime: [
    { required: true, message: '请选择活动结束时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (form.startTime && value <= form.startTime) {
          callback(new Error('活动结束时间必须晚于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 模拟商品数据（实际项目中应该从API获取）
const goodsList = ref([
  {
    id: 1,
    name: '宠物猫粮 皇家成猫粮 2kg',
    image: 'https://via.placeholder.com/100',
    price: 89.00,
    stock: 100,
    skus: [
      { id: 101, specs: ['2kg'], price: 89.00, stock: 100 },
      { id: 102, specs: ['5kg'], price: 199.00, stock: 50 }
    ]
  },
  {
    id: 2,
    name: '宠物狗粮 皇家幼犬粮 1.5kg',
    image: 'https://via.placeholder.com/100',
    price: 68.00,
    stock: 80,
    skus: [
      { id: 201, specs: ['1.5kg'], price: 68.00, stock: 80 },
      { id: 202, specs: ['3kg'], price: 128.00, stock: 40 }
    ]
  }
])

const selectedGoods = ref<any>(null)
const selectedGoodsId = ref<number>(0)

const maxStock = computed(() => {
  if (!selectedGoods.value) return 0
  if (!selectedGoods.value.skus) return selectedGoods.value.stock
  const selectedSku = selectedGoods.value.skus.find((sku: any) => sku.id === form.skuId)
  return selectedSku ? selectedSku.stock : 0
})

function handleSelectGoods() {
  goodsDialogVisible.value = true
}

function handleGoodsRowClick(row: any) {
  selectedGoodsId.value = row.id
}

function handleConfirmGoods() {
  const goods = goodsList.value.find(g => g.id === selectedGoodsId.value)
  if (!goods) {
    ElMessage.warning('请选择商品')
    return
  }
  selectedGoods.value = goods
  form.goodsId = goods.id
  form.originalPrice = goods.price
  if (goods.skus && goods.skus.length > 0) {
    form.skuId = goods.skus[0].id
  }
  goodsDialogVisible.value = false
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await createGroupActivity(form)
        ElMessage.success('创建成功')
        handleBack()
      } catch (e) {
        ElMessage.error('创建失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

function handleBack() {
  router.push('/shop/group-activity')
}

onMounted(() => {
  if (route.params.id) {
    isEdit.value = true
    // 如果是编辑，这里应该加载活动详情
  }
})
</script>

<style scoped lang="scss">
.group-activity-form {
  .card-header {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .selected-goods {
    display: flex;
    gap: 16px;
    margin-top: 16px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 4px;

    .goods-info {
      .goods-name {
        font-weight: bold;
        margin-bottom: 8px;
      }
      .goods-price {
        color: #f56c6c;
        font-size: 16px;
      }
    }
  }
}
</style>
