<template>
  <div class="goods-edit">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span>编辑商品</span>
            <el-button type="info" link @click="showChangeLog" :disabled="!form.id">
              <el-icon><Document /></el-icon>
              变更历史
            </el-button>
          </div>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="goodsName">
              <el-input v-model="form.goodsName" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId">
              <el-cascader
                v-model="categoryValue"
                :options="categoryOptions"
                :props="cascaderProps"
                placeholder="请选择分类"
                clearable
                change-on-select
                @change="handleCategoryChange"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品主图">
              <div class="image-upload-wrapper">
                <el-upload
                  class="image-uploader"
                  :show-file-list="false"
                  :http-request="handleCustomUpload"
                  :before-upload="beforeImageUpload"
                  :disabled="uploading"
                >
                  <img v-if="mainImageSignUrl || form.mainImage" :src="mainImageSignUrl || form.mainImage" class="image" />
                  <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
                </el-upload>
                <el-progress
                  v-if="uploading"
                  :percentage="uploadProgress"
                  :stroke-width="6"
                  style="margin-top: 12px; width: 178px"
                />
                <div class="compress-info" v-if="compressResult">
                  <div class="compress-row">
                    <span class="compress-label">原图:</span>
                    <span class="compress-value">{{ formatFileSize(compressResult.originalSize) }}</span>
                  </div>
                  <div class="compress-row">
                    <span class="compress-label">压缩后:</span>
                    <span class="compress-value">{{ formatFileSize(compressResult.compressedSize) }}</span>
                  </div>
                  <div class="compress-row">
                    <span class="compress-label">节省:</span>
                    <span class="compress-value success">-{{ (compressResult.compressionRatio * 100).toFixed(0) }}%</span>
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品类型">
              <el-switch v-model="isService" :active-value="1" :inactive-value="0" @change="handleServiceChange" />
              <span style="margin-left: 8px">{{ isService ? '服务类商品' : '实物商品' }}</span>
            </el-form-item>
            <el-form-item label="商品状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">上架</el-radio>
                <el-radio :value="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">规格管理</el-divider>

        <div class="form-item-wrapper">
          <span class="form-label">规格名称</span>
          <div class="form-item-content">
            <el-input v-model="specName" placeholder="如：重量、体型、规格" style="width: 200px; margin-right: 12px" />
            <el-button type="primary" @click="addSkuRow" :disabled="!specName">
              <el-icon><Plus /></el-icon>添加规格
            </el-button>
          </div>
        </div>

        <el-table :data="form.skuList" border style="width: 100%">
          <el-table-column label="编码" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.id" type="info">{{ row.id }}</el-tag>
              <span v-else class="text-gray-400">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="barcode" label="商品条码" min-width="200">
            <template #default="{ row }">
              <el-input v-model="row.barcode" placeholder="扫码或输入条码" />
            </template>
          </el-table-column>
          <el-table-column prop="specValue" label="规格值" min-width="200">
            <template #default="{ row }">
              <el-input v-model="row.specValue" placeholder="如：1.5kg" />
            </template>
          </el-table-column>
          <el-table-column prop="price" label="售价(元)" width="150">
            <template #default="{ row }">
              <el-input v-model.number="row.price" type="number" placeholder="0.00" />
            </template>
          </el-table-column>
          <el-table-column prop="costPrice" label="成本价(元)" width="150">
            <template #default="{ row }">
              <el-input v-model.number="row.costPrice" type="number" placeholder="0.00" />
            </template>
          </el-table-column>
          <el-table-column label="无限库存" width="100" v-if="!isService">
            <template #default="{ row }">
              <el-switch v-model="row.isUnlimitedStock" :active-value="1" :inactive-value="0" />
            </template>
          </el-table-column>
          <el-table-column label="当前库存" width="100" v-if="!isService">
            <template #default="{ row }">
              <el-tag v-if="row.id && !row.isUnlimitedStock" type="success">{{ row.stock }}</el-tag>
              <el-tag v-else-if="row.isUnlimitedStock" type="warning">无限</el-tag>
              <el-tag v-else type="info">-</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="规格状态" width="100">
            <template #default="{ row }">
              <el-switch v-model="row.status" :active-value="1" :inactive-value="0" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button type="danger" link @click="removeSkuRow($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-form-item style="margin-top: 32px">
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">保存</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 变更历史抽屉 -->
    <ChangeLogDrawer v-model="changeLogDrawerVisible" :data-id="currentChangeLogDataId" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Document } from '@element-plus/icons-vue'
import {
  getCategoryTree,
  getGoods,
  saveGoods,
  type Goods,
  type GoodsSku
} from '@/api/inventory'
import { useImageUpload } from '@/composables/useImageUpload'
import { getSignUrl } from '@/api/file'
import ChangeLogDrawer from '@/components/ChangeLogDrawer/index.vue'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitLoading = ref(false)

const { uploading, uploadProgress, uploadToOss, compressResult, formatFileSize } = useImageUpload()

const isService = ref(0)
const specName = ref('')
const categoryValue = ref<number[]>([])
const categoryOptions = ref<any[]>([])

// 变更历史相关
const changeLogDrawerVisible = ref(false)
const currentChangeLogDataId = ref<number | null>(null)

const cascaderProps = {
  value: 'id',
  label: 'categoryName',
  children: 'children',
  checkStrictly: true,
  emitPath: false
}

// 在分类树中查找分类的完整路径
function findCategoryPath(categories: any[], targetId: number, path: number[] = []): number[] {
  for (const cat of categories) {
    const currentPath = [...path, cat.id]
    if (cat.id === targetId) {
      return currentPath
    }
    if (cat.children && cat.children.length > 0) {
      const result = findCategoryPath(cat.children, targetId, currentPath)
      if (result.length > 0) {
        return result
      }
    }
  }
  return []
}

const form = reactive<Partial<Goods>>({
  id: undefined,
  goodsName: '',
  categoryId: undefined,
  mainImage: '',
  isService: 0,
  status: 1,
  version: undefined,
  skuList: []
})

// 用于显示的签名 URL（不提交给后端）
const mainImageSignUrl = ref('')

const rules: FormRules = {
  goodsName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }]
}

async function loadCategories() {
  try {
    categoryOptions.value = await getCategoryTree()
  } catch (e) {
    ElMessage.error('加载分类失败')
  }
}

async function loadGoods() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const data = await getGoods(id)
    Object.assign(form, data)
    isService.value = data.isService
    // 查找分类的完整路径
    if (data.categoryId) {
      categoryValue.value = findCategoryPath(categoryOptions.value, data.categoryId)
    }
    if (data.skuList && data.skuList.length > 0) {
      specName.value = data.skuList[0].specName
    }
    // 如果有主图，获取签名 URL 用于显示
    if (data.mainImage) {
      try {
        mainImageSignUrl.value = await getSignUrl(data.mainImage)
      } catch (e) {
        console.error('获取签名 URL 失败:', e)
        // 如果获取失败，直接使用原始 URL
        mainImageSignUrl.value = data.mainImage
      }
    }
  } catch (e) {
    ElMessage.error('加载商品信息失败')
  } finally {
    loading.value = false
  }
}

function showChangeLog() {
  if (!form.id) return
  currentChangeLogDataId.value = form.id
  changeLogDrawerVisible.value = true
}

function handleCategoryChange(value: number) {
  form.categoryId = value
}

function handleServiceChange(val: number) {
  form.isService = val
  if (form.skuList) {
    form.skuList.forEach(sku => {
      sku.specName = specName.value
    })
  }
}

function addSkuRow() {
  if (!form.skuList) {
    form.skuList = []
  }
  const newSku: Partial<GoodsSku> = {
    specName: specName.value,
    specValue: '',
    price: 0,
    costPrice: 0,
    barcode: '',
    isUnlimitedStock: 0,
    status: 1
  }
  form.skuList.push(newSku as GoodsSku)
}

function removeSkuRow(index: number) {
  form.skuList?.splice(index, 1)
}

async function handleCustomUpload(options: { file: File; onSuccess: () => void; onError: (error: any) => void }) {
  try {
    const result = await uploadToOss(options.file, false)
    // mainImage 保存原始 URL，提交给后端
    form.mainImage = result.originalUrl
    // mainImageSignUrl 用于前端显示
    mainImageSignUrl.value = result.signUrl
    options.onSuccess()
  } catch (error) {
    options.onError(error)
  }
}

function beforeImageUpload(file: File) {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (!form.skuList || form.skuList.length === 0) {
        ElMessage.warning('请至少添加一个规格')
        return
      }

      const invalidSku = form.skuList.find(sku =>
        !sku.specValue || sku.price === undefined || sku.price === null || sku.price < 0 || sku.costPrice === undefined || sku.costPrice === null || sku.costPrice < 0
      )
      if (invalidSku) {
        ElMessage.warning('请完善规格信息（规格值、售价、成本价不能为空）')
        return
      }

      submitLoading.value = true
      try {
        await saveGoods(form)
        ElMessage.success('保存成功')
        router.push('/shop/goods/list')
      } catch (e) {
        ElMessage.error('保存失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

function goBack() {
  router.push('/shop/inventory/goods')
}

onMounted(async () => {
  await loadCategories()
  await loadGoods()
})
</script>

<style scoped lang="scss">
.goods-edit {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
    }
  }

  .form-item-wrapper {
    margin-bottom: 22px;
    display: flex;
    align-items: flex-start;

    span.form-label {
      width: 120px;
      flex-shrink: 0;
      text-align: right;
      margin-right: 12px;
      padding-top: 2px;
      font-size: 14px;
      color: #606266;
    }

    .form-item-content {
      flex: 1;
    }
  }

  .image-uploader {
    :deep(.el-upload) {
      border: 1px dashed var(--el-border-color);
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration-fast);

      &:hover {
        border-color: var(--el-color-primary);
      }
    }

    .image-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 178px;
      height: 178px;
      text-align: center;
      line-height: 178px;
    }

    .image {
      width: 178px;
      height: 178px;
      display: block;
      object-fit: cover;
    }

    .compress-info {
      margin-top: 12px;
      padding: 12px;
      background: var(--el-fill-color-lighter);
      border-radius: 6px;
      border: 1px solid var(--el-border-color-light);

      .compress-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 6px;
        font-size: 13px;

        &:last-child {
          margin-bottom: 0;
        }

        .compress-label {
          color: var(--el-text-color-secondary);
        }

        .compress-value {
          font-weight: 500;
          color: var(--el-text-color-primary);

          &.success {
            color: var(--el-color-success);
          }
        }
      }
    }

    .compress-option {
      color: var(--el-text-color-secondary);
    }
  }
}
</style>
