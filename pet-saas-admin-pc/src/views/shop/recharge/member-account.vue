<template>
  <div class="member-account">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="会员姓名">
          <el-input v-model="queryForm.memberName" placeholder="请输入会员姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryForm.memberPhone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="memberName" label="会员姓名" />
        <el-table-column prop="memberPhone" label="手机号" />
        <el-table-column prop="balance" label="当前余额">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">¥{{ row.balance }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalRecharge" label="累计储值">
          <template #default="{ row }">
            <span>¥{{ row.totalRecharge }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalConsume" label="累计消费">
          <template #default="{ row }">
            <span>¥{{ row.totalConsume || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="开卡时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">
              查看详情
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

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="会员储值账户详情"
      width="800px"
    >
      <div v-if="currentAccount" class="account-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="会员姓名">{{ currentAccount.memberName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentAccount.memberPhone }}</el-descriptions-item>
          <el-descriptions-item label="当前余额">
            <span style="color: #f56c6c; font-weight: bold; font-size: 18px">¥{{ currentAccount.balance }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="累计储值">¥{{ currentAccount.totalRecharge }}</el-descriptions-item>
          <el-descriptions-item label="累计消费">¥{{ currentAccount.totalConsume || 0 }}</el-descriptions-item>
          <el-descriptions-item label="开卡时间">{{ currentAccount.createTime }}</el-descriptions-item>
        </el-descriptions>

        <div class="record-section">
          <h4>储值/消费记录</h4>
          <el-table :data="accountRecords" stripe style="margin-top: 16px">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.type === 'recharge' ? 'success' : row.type === 'consume' ? 'primary' : 'warning'">
                  {{ row.type === 'recharge' ? '储值' : row.type === 'consume' ? '消费' : '退款' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额">
              <template #default="{ row }">
                <span :style="{ color: row.amount > 0 ? '#67c23a' : '#f56c6c' }">
                  {{ row.amount > 0 ? '+' : '' }}¥{{ row.amount }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="balanceBefore" label="变动前余额" />
            <el-table-column prop="balanceAfter" label="变动后余额" />
            <el-table-column prop="remark" label="备注" />
            <el-table-column prop="createTime" label="时间" width="180" />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getMemberAccountList, getRechargeRecordList } from '@/api/recharge-card'

interface MemberAccount {
  id: number
  memberId: number
  memberName: string
  memberPhone: string
  balance: number
  totalRecharge: number
  totalConsume: number
  createTime: string
}

interface RechargeRecord {
  id: number
  type: 'recharge' | 'consume' | 'refund'
  amount: number
  balanceBefore: number
  balanceAfter: number
  remark: string
  createTime: string
}

const loading = ref(false)
const tableData = ref<MemberAccount[]>([])
const total = ref(0)
const detailDialogVisible = ref(false)
const currentAccount = ref<MemberAccount | null>(null)
const accountRecords = ref<RechargeRecord[]>([])

const queryForm = reactive({
  page: 1,
  pageSize: 10,
  memberName: '',
  memberPhone: ''
})

async function loadData() {
  loading.value = true
  try {
    const res = await getMemberAccountList(queryForm)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.page = 1
  queryForm.memberName = ''
  queryForm.memberPhone = ''
  loadData()
}

async function viewDetail(row: MemberAccount) {
  currentAccount.value = row
  try {
    const recordRes = await getRechargeRecordList({ memberId: row.memberId, page: 1, pageSize: 100 })
    accountRecords.value = recordRes.records || []
  } catch (e) {
    ElMessage.error('加载记录失败')
  }
  detailDialogVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.member-account {
  .search-form {
    margin-bottom: 16px;
  }

  .el-pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }

  .account-detail {
    .record-section {
      margin-top: 24px;

      h4 {
        margin: 0 0 12px 0;
        font-size: 16px;
        color: #303133;
      }
    }
  }
}
</style>
