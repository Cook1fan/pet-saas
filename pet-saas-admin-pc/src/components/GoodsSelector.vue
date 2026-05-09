<template>
  <el-dialog
    v-model="dialogVisible"
    title="选择商品"
    width="900px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="goods-selector">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品名称"
          clearable
          style="width: 300px"
          @clear="loadGoods"
          @keyup.enter="loadGoods"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="loadGoods" :loading="loading">搜索</el-button>
      </div>

      <div class="content">
        <div class="category-nav">
          <div class="nav-title">商品分类</div>
          <div
            v-for="cat in firstLevelCategories"
            :key="cat.id"
            class="category-item"
            :class="{ active: selectedFirstCategoryId === cat.id }"
            @click="selectFirstCategory(cat)"
          >
            {{ cat.categoryName }}
          </div>
        </div>

        <div class="goods-list">
          <div v-if="selectedFirstCategoryId && secondLevelCategories.length > 0" class="second-level-nav">
            <div
              class="second-category-item"
              :class="{ active: selectedSecondCategoryId === undefined }"
              @click="selectSecondCategory(undefined)"
            >
              全部
            </div>
            <div
              v-for="cat in secondLevelCategories"
              :key="cat.id"
              class="second-category-item"
              :class="{ active: selectedSecondCategoryId === cat.id }"
              @click="selectSecondCategory(cat.id)"
            >
              {{ cat.categoryName }}
            </div>
          </div>

          <el-scrollbar class="goods-scrollbar">
            <div class="goods-grid" v-loading="loading">
              <div
                v-for="goods in goodsList"
                :key="goods.id"
                class="goods-card"
                @click="openSpecDialog(goods)"
              >
                <div class="goods-image">
                  <el-image
                    v-if="goods.mainImage"
                    :src="goods.mainImage"
                    fit="cover"
                  />
                  <el-icon v-else class="placeholder-icon"><Picture /></el-icon>
                </div>
                <div class="goods-info">
                  <div class="goods-name">{{ goods.goodsName }}</div>
                  <div class="goods-price">
                    <span v-if="goods.skuList && goods.skuList.length > 0">
                      ¥{{ getMinPrice(goods.skuList) }}起
                    </span>
                    <span v-else>暂无价格</span>
                  </div>
                  <div class="goods-spec-count">
                    {{ goods.skuList?.length || 0 }}个规格
                  </div>
                </div>
              </div>
            </div>
          </el-scrollbar>

          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="queryForm.pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadGoods"
            style="margin-top: 16px; justify-content: center"
          />
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="specDialogVisible"
    title="选择规格"
    width="600px"
    :close-on-click-modal="false"
  >
    <div class="spec-dialog">
      <div class="selected-goods-info">
        <el-image
          v-if="currentGoods?.mainImage"
          :src="currentGoods.mainImage"
          fit="cover"
          class="goods-thumb"
        />
        <div class="goods-detail">
          <div class="goods-name">{{ currentGoods?.goodsName }}</div>
          <div class="category-name">{{ currentGoods?.categoryName }}</div>
        </div>
      </div>

      <el-divider />

      <div class="spec-list">
        <div
          v-for="sku in currentGoods?.skuList"
          :key="sku.id"
          class="spec-item"
          :class="{ disabled: !isSkuAvailable(sku) }"
          @click="selectSku(sku)"
        >
          <div class="spec-value">{{ sku.specValue }}</div>
          <div class="spec-price">¥{{ sku.price.toFixed(2) }}</div>
          <div class="spec-stock">
            <span v-if="currentGoods?.isService === 1">库存不限</span>
            <span v-else>库存: {{ sku.stock }}</span>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="specDialogVisible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Picture } from '@element-plus/icons-vue'
import {
  getCategoryTree,
  getGoodsList,
  type Goods,
  type GoodsSku,
  type Category,
  type GoodsQuery
} from '@/api/inventory'

interface Props {
  modelValue: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'select', sku: GoodsSku & { goodsName: string; goodsId: number }): void
}>()

const loading = ref(false)
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})
const specDialogVisible = ref(false)

const categoryList = ref<Category[]>([])
const goodsList = ref<Goods[]>([])
const total = ref(0)
const currentPage = ref(1)
const searchKeyword = ref('')
const selectedFirstCategoryId = ref<number>()
const selectedSecondCategoryId = ref<number>()
const currentGoods = ref<Goods | null>(null)

const queryForm = reactive<GoodsQuery>({
  pageNum: 1,
  pageSize: 12,
  keyword: '',
  categoryId: undefined
})

const firstLevelCategories = computed(() => {
  return categoryList.value.filter(c => c.parentId === 0)
})

const secondLevelCategories = computed(() => {
  if (!selectedFirstCategoryId.value) return []
  return categoryList.value.filter(c => c.parentId === selectedFirstCategoryId.value)
})

async function loadCategories() {
  try {
    categoryList.value = await getCategoryTree()
    if (firstLevelCategories.value.length > 0) {
      selectedFirstCategoryId.value = firstLevelCategories.value[0].id
    }
  } catch (e) {
    ElMessage.error('加载分类失败')
  }
}

async function loadGoods() {
  loading.value = true
  try {
    queryForm.pageNum = currentPage.value
    queryForm.keyword = searchKeyword.value
    queryForm.categoryId = selectedSecondCategoryId.value ?? selectedFirstCategoryId.value
    const res = await getGoodsList(queryForm)
    goodsList.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载商品失败')
  } finally {
    loading.value = false
  }
}

function selectFirstCategory(cat: Category) {
  selectedFirstCategoryId.value = cat.id
  selectedSecondCategoryId.value = undefined
  currentPage.value = 1
  loadGoods()
}

function selectSecondCategory(catId: number | undefined) {
  selectedSecondCategoryId.value = catId
  currentPage.value = 1
  loadGoods()
}

function openSpecDialog(goods: Goods) {
  if (!goods.skuList || goods.skuList.length === 0) {
    ElMessage.warning('该商品暂无规格')
    return
  }
  currentGoods.value = goods
  specDialogVisible.value = true
}

function isSkuAvailable(sku: GoodsSku): boolean {
  if (!currentGoods.value) return false
  if (currentGoods.value.isService === 1) return true
  return sku.stock > 0
}

function selectSku(sku: GoodsSku) {
  if (!isSkuAvailable(sku)) {
    ElMessage.warning('该规格库存不足')
    return
  }
  if (!currentGoods.value) return

  emit('select', {
    ...sku,
    goodsName: currentGoods.value.goodsName,
    goodsId: currentGoods.value.id!
  })
  specDialogVisible.value = false
  dialogVisible.value = false
}

function handleClose() {
  searchKeyword.value = ''
  currentPage.value = 1
  currentGoods.value = null
}

watch(() => props.modelValue, (val) => {
  if (val) {
    loadCategories().then(() => loadGoods())
  }
})
</script>

<style scoped lang="scss">
.goods-selector {
  .search-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
  }

  .content {
    display: flex;
    height: 500px;
    border: 1px solid var(--el-border-color-light);
    border-radius: 4px;
  }

  .category-nav {
    width: 150px;
    border-right: 1px solid var(--el-border-color-light);
    background: var(--el-bg-color-page);
    overflow-y: auto;

    .nav-title {
      padding: 12px;
      font-weight: bold;
      border-bottom: 1px solid var(--el-border-color-light);
    }

    .category-item {
      padding: 12px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: var(--el-fill-color-light);
      }

      &.active {
        background: var(--el-color-primary);
        color: white;
      }
    }
  }

  .goods-list {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 16px;

    .second-level-nav {
      display: flex;
      gap: 8px;
      margin-bottom: 16px;
      flex-wrap: wrap;

      .second-category-item {
        padding: 6px 16px;
        border-radius: 4px;
        cursor: pointer;
        background: var(--el-fill-color-light);
        transition: all 0.2s;

        &:hover {
          background: var(--el-fill-color);
        }

        &.active {
          background: var(--el-color-primary);
          color: white;
        }
      }
    }

    .goods-scrollbar {
      flex: 1;

      .goods-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 16px;

        .goods-card {
          border: 1px solid var(--el-border-color-light);
          border-radius: 8px;
          overflow: hidden;
          cursor: pointer;
          transition: all 0.2s;

          &:hover {
            border-color: var(--el-color-primary);
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
          }

          .goods-image {
            height: 120px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: var(--el-fill-color-lighter);

            .el-image {
              width: 100%;
              height: 100%;
            }

            .placeholder-icon {
              font-size: 48px;
              color: var(--el-text-color-placeholder);
            }
          }

          .goods-info {
            padding: 12px;

            .goods-name {
              font-size: 14px;
              font-weight: 500;
              margin-bottom: 8px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }

            .goods-price {
              color: var(--el-color-danger);
              font-size: 16px;
              font-weight: bold;
              margin-bottom: 4px;
            }

            .goods-spec-count {
              color: var(--el-text-color-secondary);
              font-size: 12px;
            }
          }
        }
      }
    }

    :deep(.el-pagination) {
      display: flex;
    }
  }
}

.spec-dialog {
  .selected-goods-info {
    display: flex;
    gap: 16px;

    .goods-thumb {
      width: 80px;
      height: 80px;
      border-radius: 8px;
    }

    .goods-detail {
      flex: 1;

      .goods-name {
        font-size: 16px;
        font-weight: bold;
        margin-bottom: 8px;
      }

      .category-name {
        color: var(--el-text-color-secondary);
        font-size: 14px;
      }
    }
  }

  .spec-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;

    .spec-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      border: 1px solid var(--el-border-color-light);
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover:not(.disabled) {
        border-color: var(--el-color-primary);
      }

      &.disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }

      .spec-value {
        font-weight: 500;
      }

      .spec-price {
        color: var(--el-color-danger);
        font-weight: bold;
      }

      .spec-stock {
        color: var(--el-text-color-secondary);
        font-size: 12px;
      }
    }
  }
}
</style>
