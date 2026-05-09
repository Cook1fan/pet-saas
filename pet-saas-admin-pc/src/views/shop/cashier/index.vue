<template>
  <div class="cashier-page">
    <el-row :gutter="20" class="cashier-layout">
      <!-- 左侧：商品选择 -->
      <el-col :span="8">
        <el-card class="goods-panel">
          <template #header>
            <div class="panel-header">
              <span>商品/服务</span>
            </div>
          </template>
          <!-- 搜索和扫码 -->
          <div class="search-bar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索商品名称或扫码"
              class="search-input"
              clearable
              @clear="loadGoods"
              @keyup.enter="loadGoods"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="loadGoods" :loading="goodsLoading">搜索</el-button>
          </div>
          <!-- 分类导航 -->
          <div class="category-nav">
            <div
              class="category-item"
              :class="{ active: selectedCategoryId === undefined }"
              @click="selectCategory(undefined)"
            >
              全部
            </div>
            <div
              v-for="cat in categoryList"
              :key="cat.id"
              class="category-item"
              :class="{ active: selectedCategoryId === cat.id }"
              @click="selectCategory(cat)"
            >
              {{ cat.categoryName }}
            </div>
          </div>
          <!-- 商品列表 -->
          <div class="goods-list-wrapper">
            <el-scrollbar class="goods-scrollbar">
              <div class="goods-grid" v-loading="goodsLoading">
                <div
                  v-for="goods in goodsList"
                  :key="goods.id"
                  class="goods-card"
                  @click="openSkuDialog(goods)"
                >
                  <div class="goods-image">
                    <el-image
                      v-if="goods.mainImage"
                      :src="goods.mainImage"
                      fit="cover"
                    />
                    <el-icon v-else class="placeholder-icon"><Box /></el-icon>
                  </div>
                  <div class="goods-info">
                    <div class="goods-name">{{ goods.goodsName }}</div>
                    <div class="goods-price">
                      <span>
                        ¥{{ getMinPrice(goods.skuList).toFixed(2) }}
                      </span>
                    </div>
                    <div class="goods-tag">
                      <el-tag size="small" :type="goods.isService === 1 ? 'success' : 'primary'">
                        {{ goods.isService === 1 ? '服务' : '商品' }}
                      </el-tag>
                      <span class="sku-count" v-if="goods.skuList">
                        {{ goods.skuList.length }}个规格
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </el-scrollbar>
            <!-- 分页 -->
            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="goodsQuery.pageNum"
                v-model:page-size="goodsQuery.pageSize"
                :total="goodsTotal"
                layout="prev, pager, next"
                @current-change="loadGoods"
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 中间：已选商品 -->
      <el-col :span="8">
        <el-card class="selected-panel">
          <template #header>
            <div class="panel-header">
              <span>已选商品</span>
              <el-button type="danger" link size="small" @click="clearAll">清空</el-button>
            </div>
          </template>
          <div class="selected-list">
            <div v-for="(item, index) in selectedList" :key="index" class="selected-item">
              <div class="item-info">
                <div class="item-name">{{ item.goodsName }}</div>
                <div class="item-spec" v-if="item.specValue">{{ item.specValue }}</div>
                <div class="item-price">¥{{ item.price.toFixed(2) }}</div>
              </div>
              <div class="item-actions">
                <el-button size="small" circle @click="decreaseQuantity(index)" :disabled="item.num <= 1">
                  <el-icon><Minus /></el-icon>
                </el-button>
                <span class="item-quantity">{{ item.num }}</span>
                <el-button size="small" circle type="primary" @click="increaseQuantity(index)">
                  <el-icon><Plus /></el-icon>
                </el-button>
                <el-button size="small" circle type="danger" @click="removeItem(index)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            <el-empty v-if="selectedList.length === 0" description="暂无商品" />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：会员、金额、支付 -->
      <el-col :span="8">
        <el-card class="payment-panel">
          <template #header>
            <span>收银台</span>
          </template>

          <!-- 会员选择 -->
          <div class="member-section">
            <div class="section-title">会员</div>
            <div class="member-selector" v-if="!selectedMember">
              <el-input
                v-model="memberKeyword"
                placeholder="输入手机号或姓名搜索会员"
                clearable
                class="member-input"
                @input="searchMemberInput"
              />
              <div class="member-suggestions" v-if="memberSuggestions.length > 0">
                <div
                  v-for="m in memberSuggestions"
                  :key="m.id"
                  class="member-suggestion-item"
                  @click="selectMember(m)"
                >
                  <div class="member-name">{{ m.name }}</div>
                  <div class="member-phone">{{ m.phone }}</div>
                </div>
              </div>
              <el-button type="primary" link class="no-member-btn" @click="skipMember">
                散客（不选择会员）
              </el-button>
            </div>
            <div class="selected-member-info" v-else>
              <el-tag type="success" size="small">会员</el-tag>
              <span class="member-name">{{ selectedMember.name }}</span>
              <span class="member-phone">{{ selectedMember.phone }}</span>
              <el-button type="primary" link size="small" @click="clearMember">更换</el-button>
              <div class="member-account" v-if="memberAccount">
                余额: <span class="balance">¥{{ memberAccount.balance.toFixed(2) }}</span>
              </div>
            </div>
          </div>

          <el-divider />

          <!-- 金额明细 -->
          <div class="amount-section">
            <div class="section-title">金额明细</div>
            <div class="amount-row">
              <span>商品总额</span>
              <span class="amount-value">¥{{ totalAmount.toFixed(2) }}</span>
            </div>
            <div class="amount-row">
              <span>优惠</span>
              <span class="amount-value discount">-¥{{ discountAmount.toFixed(2) }}</span>
            </div>
            <el-divider />
            <div class="amount-row total">
              <span>应付金额</span>
              <span class="amount-value">¥{{ payAmount.toFixed(2) }}</span>
            </div>
          </div>

          <el-divider />

          <!-- 支付方式 -->
          <div class="pay-section">
            <div class="section-title">支付方式</div>
            <div class="pay-type-group">
              <div
                class="pay-type-card"
                :class="{ active: payType === PayTypeEnum.WECHAT }"
                @click="payType = PayTypeEnum.WECHAT"
              >
                <el-icon class="pay-icon wechat"><ChatDotRound /></el-icon>
                <span class="pay-label">微信支付</span>
              </div>
              <div
                class="pay-type-card"
                :class="{ active: payType === PayTypeEnum.CASH }"
                @click="payType = PayTypeEnum.CASH"
              >
                <el-icon class="pay-icon cash"><Wallet /></el-icon>
                <span class="pay-label">现金</span>
              </div>
              <div
                class="pay-type-card"
                :class="{ active: payType === PayTypeEnum.BALANCE, disabled: !selectedMember }"
                @click="selectedMember && (payType = PayTypeEnum.BALANCE)"
                v-if="selectedMember"
              >
                <el-icon class="pay-icon balance"><Coin /></el-icon>
                <span class="pay-label">余额</span>
              </div>
              <div
                class="pay-type-card"
                :class="{ active: payType === PayTypeEnum.CARD, disabled: !selectedMember }"
                @click="selectedMember && (payType = PayTypeEnum.CARD)"
                v-if="selectedMember"
              >
                <el-icon class="pay-icon card"><Ticket /></el-icon>
                <span class="pay-label">次卡</span>
              </div>
            </div>

            <!-- 次卡选择器 -->
            <div class="card-selector" v-if="payType === PayTypeEnum.CARD && selectedMember">
              <el-select
                v-model="selectedCardId"
                placeholder="请选择次卡"
                style="width: 100%"
              >
                <el-option
                  v-for="card in memberCards"
                  :key="card.id"
                  :label="`${card.cardName} (剩余${card.remainTimes}次)`"
                  :value="card.id"
                  :disabled="card.remainTimes <= 0 || card.status !== 1"
                />
              </el-select>
            </div>
          </div>

          <el-divider />

          <!-- 备注 -->
          <div class="remark-section">
            <el-input
              v-model="remark"
              type="textarea"
              :rows="2"
              placeholder="备注（可选）"
            />
          </div>

          <!-- 收银按钮 -->
          <el-button
            type="primary"
            size="large"
            class="cashier-btn"
            :loading="submitLoading"
            :disabled="!canSubmit"
            @click="handleCashier"
          >
            收银 ¥{{ payAmount.toFixed(2) }}
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <!-- SKU 选择弹窗 -->
    <el-dialog
      v-model="skuDialogVisible"
      title="选择规格"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="sku-dialog">
        <div class="selected-goods-info" v-if="currentGoods">
          <el-image
            v-if="currentGoods.mainImage"
            :src="currentGoods.mainImage"
            fit="cover"
            class="goods-thumb"
          />
          <el-icon v-else class="goods-thumb-icon"><Box /></el-icon>
          <div class="goods-detail">
            <div class="goods-name">{{ currentGoods.goodsName }}</div>
            <div class="category-name">{{ currentGoods.categoryName }}</div>
          </div>
        </div>
        <el-divider />
        <div class="sku-list">
          <div
            v-for="sku in currentGoods?.skuList"
            :key="sku.id"
            class="sku-item"
            :class="{ disabled: !isSkuAvailable(sku) }"
            @click="selectSku(sku)"
          >
            <div class="sku-spec">{{ sku.specValue }}</div>
            <div class="sku-price">¥{{ sku.price.toFixed(2) }}</div>
            <div class="sku-stock">
              <span v-if="currentGoods?.isService === 1">库存不限</span>
              <span v-else>库存: {{ sku.stock }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="skuDialogVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 微信支付二维码弹窗 -->
    <el-dialog
      v-model="wechatPayDialogVisible"
      title="请扫码支付"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div class="wechat-pay-content">
        <div class="qrcode-wrapper">
          <img v-if="wechatCodeUrl" :src="wechatCodeUrl" alt="微信支付二维码" class="qrcode-img" />
          <el-icon v-else class="qrcode-loading"><Loading /></el-icon>
        </div>
        <div class="pay-amount">
          <span class="amount-label">支付金额</span>
          <span class="amount-value">¥{{ payAmount.toFixed(2) }}</span>
        </div>
        <div class="pay-tip">请使用微信扫描二维码支付</div>
      </div>
      <template #footer>
        <el-button @click="cancelWechatPay">取消</el-button>
        <el-button type="primary" @click="confirmWechatPay" :loading="confirmLoading">
          已支付
        </el-button>
      </template>
    </el-dialog>

    <!-- 收银成功弹窗 -->
    <el-dialog v-model="successDialogVisible" title="收银成功" width="400px">
      <div class="success-content">
        <el-icon class="success-icon"><CircleCheck /></el-icon>
        <div class="success-text">收银成功！</div>
        <div class="success-order-no" v-if="currentOrderNo">订单号：{{ currentOrderNo }}</div>
      </div>
      <template #footer>
        <el-button type="primary" @click="onSuccessConfirm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Plus,
  Minus,
  Delete,
  ChatDotRound,
  Wallet,
  Coin,
  Ticket,
  Box,
  Loading,
  CircleCheck
} from '@element-plus/icons-vue'
import {
  getMemberCards,
  getMemberAccount,
  searchMember,
  type CashierMember,
  type MemberAccount,
  type MemberCard
} from '@/api/cashier'
import {
  getGoodsList,
  type GoodsListQuery
} from '@/api/inventory'
import {
  createOrder,
  PayTypeEnum,
  PayStatusEnum,
  getPayTypeText,
  getPayStatusText,
  getPayStatusTagType,
  type PayType,
  type CreateOrderRequest,
  type CreateOrderResponse
} from '@/api/order'
import { getCategoryTree, getGoodsList as getGoodsListFromInventory, type Goods, type GoodsSku, type Category } from '@/api/inventory'

// ==================== 状态定义 ====================

// 商品相关
const goodsLoading = ref(false)
const goodsList = ref<Goods[]>([])
const goodsTotal = ref(0)
const categoryList = ref<Category[]>([])
const selectedCategoryId = ref<number | undefined>(undefined)
const searchKeyword = ref('')

const goodsQuery = reactive({
  pageNum: 1,
  pageSize: 20,
  categoryId: undefined as number | undefined,
  keyword: '',
  status: 1
})

// SKU 选择弹窗
const skuDialogVisible = ref(false)
const currentGoods = ref<Goods | null>(null)

// 已选商品
interface SelectedItem {
  goodsId: number
  skuId: number
  goodsName: string
  specValue?: string
  price: number
  num: number
}
const selectedList = ref<SelectedItem[]>([])

// 会员相关
const memberKeyword = ref('')
const memberSuggestions = ref<CashierMember[]>([])
const selectedMember = ref<CashierMember | null>(null)
const memberAccount = ref<MemberAccount | null>(null)
const memberCards = ref<MemberCard[]>([])

// 支付相关
const payType = ref<PayType>(PayTypeEnum.WECHAT)
const selectedCardId = ref<number | null>(null)
const remark = ref('')
const submitLoading = ref(false)
const discountAmount = ref(0)

// 微信支付相关
const wechatPayDialogVisible = ref(false)
const wechatCodeUrl = ref('')
const confirmLoading = ref(false)
const currentOrderId = ref<number | null>(null)
const currentOrderNo = ref('')

// 成功弹窗
const successDialogVisible = ref(false)

// ==================== 计算属性 ====================

const totalAmount = computed(() => {
  return selectedList.value.reduce((sum, item) => sum + item.price * item.num, 0)
})

const payAmount = computed(() => {
  return Math.max(0, totalAmount.value - discountAmount.value)
})

const canSubmit = computed(() => {
  if (selectedList.value.length === 0) return false
  if (payType.value === PayTypeEnum.CARD) {
    return selectedCardId.value !== null
  }
  return true
})

// ==================== 商品相关函数 ====================

function getMinPrice(skuList: GoodsSku[] | undefined): number {
  if (!skuList || skuList.length === 0) return 0
  return Math.min(...skuList.map(s => s.price))
}

async function loadCategories() {
  try {
    const res = await getCategoryTree()
    categoryList.value = res.filter(c => c.parentId === 0)
    // 默认不选中任何分类，显示"全部"
    selectedCategoryId.value = undefined
    await loadGoods()
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

async function loadGoods() {
  goodsLoading.value = true
  try {
    const params: GoodsListQuery = {
      pageNum: goodsQuery.pageNum,
      pageSize: goodsQuery.pageSize,
      keyword: searchKeyword.value,
      status: 1
    }
    if (selectedCategoryId.value) {
      params.categoryId = selectedCategoryId.value
    }
    const res = await getGoodsList(params)
    goodsList.value = res.records || []
    goodsTotal.value = res.total || 0
  } catch (e) {
    console.error('加载商品失败', e)
    ElMessage.error('加载商品失败')
  } finally {
    goodsLoading.value = false
  }
}

function selectCategory(cat: Category | undefined) {
  if (cat === undefined) {
    selectedCategoryId.value = undefined
  } else {
    selectedCategoryId.value = cat.id
  }
  goodsQuery.pageNum = 1
  loadGoods()
}

function openSkuDialog(goods: Goods) {
  // 如果没有 SKU 列表，直接添加商品
  if (!goods.skuList || goods.skuList.length === 0) {
    // 直接添加一个默认项
    const existing = selectedList.value.find(item => item.goodsId === goods.id)
    if (existing) {
      existing.num += 1
    } else {
      selectedList.value.push({
        goodsId: goods.id!,
        skuId: 0,
        goodsName: goods.goodsName,
        specValue: '',
        price: 0,
        num: 1
      })
    }
    return
  }
  currentGoods.value = goods
  skuDialogVisible.value = true
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

  // 检查是否已存在相同 SKU
  const existing = selectedList.value.find(item => item.skuId === sku.id)
  if (existing) {
    existing.num += 1
  } else {
    selectedList.value.push({
      goodsId: currentGoods.value.id!,
      skuId: sku.id!,
      goodsName: currentGoods.value.goodsName,
      specValue: sku.specValue,
      price: sku.price,
      num: 1
    })
  }

  skuDialogVisible.value = false
}

// ==================== 已选商品操作 ====================

function increaseQuantity(index: number) {
  selectedList.value[index].num += 1
}

function decreaseQuantity(index: number) {
  if (selectedList.value[index].num > 1) {
    selectedList.value[index].num -= 1
  } else {
    removeItem(index)
  }
}

function removeItem(index: number) {
  selectedList.value.splice(index, 1)
}

function clearAll() {
  if (selectedList.value.length === 0) return
  ElMessageBox.confirm('确定清空已选商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    selectedList.value = []
  }).catch(() => {})
}

// ==================== 会员相关函数 ====================

async function searchMemberInput() {
  if (!memberKeyword.value.trim()) {
    memberSuggestions.value = []
    return
  }
  try {
    memberSuggestions.value = await searchMember(memberKeyword.value)
  } catch (e) {
    console.error('搜索会员失败', e)
  }
}

async function selectMember(member: CashierMember) {
  selectedMember.value = member
  memberKeyword.value = ''
  memberSuggestions.value = []

  // 加载会员账户信息
  try {
    memberAccount.value = await getMemberAccount(member.id)
  } catch (e) {
    console.error('加载会员账户失败', e)
  }

  // 加载会员次卡列表
  try {
    memberCards.value = await getMemberCards(member.id)
  } catch (e) {
    console.error('加载会员次卡失败', e)
  }
}

function skipMember() {
  selectedMember.value = null
  memberKeyword.value = ''
  memberSuggestions.value = []
  memberAccount.value = null
  memberCards.value = []
  selectedCardId.value = null
  if (payType.value === PayTypeEnum.BALANCE || payType.value === PayTypeEnum.CARD) {
    payType.value = PayTypeEnum.WECHAT
  }
}

function clearMember() {
  selectedMember.value = null
  memberAccount.value = null
  memberCards.value = []
  selectedCardId.value = null
  if (payType.value === PayTypeEnum.BALANCE || payType.value === PayTypeEnum.CARD) {
    payType.value = PayTypeEnum.WECHAT
  }
}

// ==================== 收银相关函数 ====================

async function handleCashier() {
  if (selectedList.value.length === 0) {
    ElMessage.warning('请选择商品')
    return
  }
  if (payType.value === PayTypeEnum.CARD && !selectedCardId.value) {
    ElMessage.warning('请选择次卡')
    return
  }

  try {
    await ElMessageBox.confirm(`确认收银 ¥${payAmount.value.toFixed(2)}？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    submitLoading.value = true
    const req: CreateOrderRequest = {
      items: selectedList.value.map(item => ({
        skuId: item.skuId,
        num: item.num
      })),
      payType: payType.value
    }
    if (selectedMember.value) {
      req.memberId = selectedMember.value.id
    }
    if (payType.value === PayTypeEnum.CARD && selectedCardId.value) {
      req.cardId = selectedCardId.value
    }

    const res: CreateOrderResponse = await createOrder(req)

    if (payType.value === PayTypeEnum.WECHAT && res.wechatPayParams?.codeUrl) {
      // 微信支付，展示二维码
      currentOrderId.value = res.id
      currentOrderNo.value = res.orderNo
      wechatCodeUrl.value = res.wechatPayParams.codeUrl
      wechatPayDialogVisible.value = true
    } else {
      // 其他支付方式，直接成功
      showSuccess(res.orderNo)
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('收银失败')
    }
  } finally {
    submitLoading.value = false
  }
}

// 取消微信支付
function cancelWechatPay() {
  wechatPayDialogVisible.value = false
  wechatCodeUrl.value = ''
}

// 确认微信已支付
async function confirmWechatPay() {
  confirmLoading.value = true
  try {
    // 这里应该调用接口查询订单状态
    // 为简化示例，直接显示成功
    await new Promise(resolve => setTimeout(resolve, 1000))
    wechatPayDialogVisible.value = false
    showSuccess(currentOrderNo.value)
  } catch (e) {
    ElMessage.error('确认支付失败')
  } finally {
    confirmLoading.value = false
  }
}

// 显示成功弹窗
function showSuccess(orderNo: string) {
  currentOrderNo.value = orderNo
  successDialogVisible.value = true
}

// 成功确认，重置页面
function onSuccessConfirm() {
  successDialogVisible.value = false
  resetPage()
}

// 重置页面
function resetPage() {
  selectedList.value = []
  selectedMember.value = null
  memberAccount.value = null
  memberCards.value = []
  selectedCardId.value = null
  remark.value = ''
  discountAmount.value = 0
  payType.value = PayTypeEnum.WECHAT
  currentOrderId.value = null
  currentOrderNo.value = ''
  wechatCodeUrl.value = ''
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped lang="scss">
.cashier-page {
  .cashier-layout {
    height: calc(100vh - 120px);
  }

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .goods-panel,
  .selected-panel,
  .payment-panel {
    height: 100%;
    display: flex;
    flex-direction: column;

    :deep(.el-card__body) {
      flex: 1;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }
  }

  // 商品选择区样式
  .search-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;

    .search-input {
      flex: 1;
    }
  }

  .category-nav {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
    overflow-x: auto;
    padding-bottom: 4px;

    .category-item {
      padding: 8px 16px;
      background: var(--el-fill-color-light);
      border-radius: 4px;
      cursor: pointer;
      white-space: nowrap;
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

  .goods-list-wrapper {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .goods-scrollbar {
      flex: 1;

      .goods-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 12px;
        padding: 4px;

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
            height: 100px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: var(--el-fill-color-lighter);

            .el-image {
              width: 100%;
              height: 100%;
            }

            .placeholder-icon {
              font-size: 40px;
              color: var(--el-text-color-placeholder);
            }
          }

          .goods-info {
            padding: 10px;

            .goods-name {
              font-size: 14px;
              font-weight: 500;
              margin-bottom: 6px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }

            .goods-price {
              color: var(--el-color-danger);
              font-size: 16px;
              font-weight: bold;
              margin-bottom: 6px;
            }

            .goods-tag {
              display: flex;
              align-items: center;
              gap: 8px;

              .sku-count {
                color: var(--el-text-color-secondary);
                font-size: 12px;
              }
            }
          }
        }
      }
    }

    .pagination-wrapper {
      margin-top: 12px;
      display: flex;
      justify-content: center;
    }
  }

  // 已选商品区
  .selected-list {
    flex: 1;
    overflow-y: auto;
  }

  .selected-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px;
    border-bottom: 1px solid #f0f0f0;

    .item-info {
      flex: 1;

      .item-name {
        font-weight: bold;
        margin-bottom: 4px;
      }

      .item-spec {
        color: #909399;
        font-size: 12px;
        margin-bottom: 4px;
      }

      .item-price {
        color: #f56c6c;
      }
    }

    .item-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .item-quantity {
      min-width: 30px;
      text-align: center;
      font-weight: bold;
    }
  }

  .section-title {
    font-weight: bold;
    margin-bottom: 12px;
  }

  // 会员区
  .member-section {
    .member-selector {
      position: relative;
    }

    .member-input {
      margin-bottom: 8px;
    }

    .member-suggestions {
      position: absolute;
      top: 100%;
      left: 0;
      right: 0;
      background: white;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      z-index: 100;
      max-height: 200px;
      overflow-y: auto;

      .member-suggestion-item {
        padding: 8px 12px;
        cursor: pointer;

        &:hover {
          background: #f5f7fa;
        }

        .member-name {
          font-weight: bold;
        }

        .member-phone {
          color: #909399;
          font-size: 12px;
        }
      }
    }

    .no-member-btn {
      padding: 0;
    }

    .selected-member-info {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;

      .member-name {
        font-weight: bold;
      }

      .member-phone {
        color: #909399;
      }

      .member-account {
        width: 100%;
        margin-top: 8px;
        color: #606266;

        .balance {
          color: #f56c6c;
          font-weight: bold;
          margin: 0 4px;
        }
      }
    }
  }

  // 金额区
  .amount-section {
    .amount-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;

      &.total {
        font-size: 18px;
        font-weight: bold;

        .amount-value {
          color: #f56c6c;
          font-size: 24px;
        }
      }

      .discount {
        color: #67c23a;
      }
    }
  }

  // 支付方式区
  .pay-section {
    .pay-type-group {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;
      margin-bottom: 16px;

      .pay-type-card {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 20px 12px;
        border: 2px solid var(--el-border-color-light);
        border-radius: 12px;
        cursor: pointer;
        transition: all 0.2s;
        background: var(--el-fill-color-lighter);
        gap: 8px;

        &:hover:not(.disabled) {
          border-color: var(--el-color-primary-light-5);
          background: var(--el-color-primary-light-9);
        }

        &.active {
          border-color: var(--el-color-primary);
          background: var(--el-color-primary-light-7);
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
        }

        &.disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .pay-icon {
          font-size: 28px;

          &.wechat {
            color: #07c160;
          }

          &.cash {
            color: #e6a23c;
          }

          &.balance {
            color: #409eff;
          }

          &.card {
            color: #909399;
          }
        }

        .pay-label {
          font-size: 14px;
          font-weight: 500;
          color: var(--el-text-color-primary);
        }
      }
    }

    .card-selector {
      margin-top: 12px;
    }
  }

  .remark-section {
    margin-bottom: 16px;
  }

  .cashier-btn {
    width: 100%;
    height: 50px;
    font-size: 18px;
    font-weight: bold;
  }

  // SKU 弹窗
  .sku-dialog {
    .selected-goods-info {
      display: flex;
      gap: 16px;

      .goods-thumb {
        width: 80px;
        height: 80px;
        border-radius: 8px;
      }

      .goods-thumb-icon {
        width: 80px;
        height: 80px;
        font-size: 48px;
        color: var(--el-text-color-placeholder);
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

    .sku-list {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;

      .sku-item {
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

        .sku-spec {
          font-weight: 500;
        }

        .sku-price {
          color: var(--el-color-danger);
          font-weight: bold;
        }

        .sku-stock {
          color: var(--el-text-color-secondary);
          font-size: 12px;
        }
      }
    }
  }

  // 微信支付弹窗
  .wechat-pay-content {
    text-align: center;

    .qrcode-wrapper {
      width: 200px;
      height: 200px;
      margin: 0 auto 20px;
      border: 1px solid #e4e7ed;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;

      .qrcode-img {
        width: 180px;
        height: 180px;
      }

      .qrcode-loading {
        font-size: 48px;
        color: #909399;
        animation: rotating 2s linear infinite;
      }
    }

    .pay-amount {
      margin-bottom: 12px;

      .amount-label {
        margin-right: 12px;
      }

      .amount-value {
        color: #f56c6c;
        font-size: 24px;
        font-weight: bold;
      }
    }

    .pay-tip {
      color: #909399;
      font-size: 14px;
    }
  }

  // 成功弹窗
  .success-content {
    text-align: center;
    padding: 20px 0;

    .success-icon {
      font-size: 64px;
      color: #67c23a;
      margin-bottom: 16px;
    }

    .success-text {
      font-size: 20px;
      font-weight: bold;
      margin-bottom: 12px;
    }

    .success-order-no {
      color: #909399;
    }
  }
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
