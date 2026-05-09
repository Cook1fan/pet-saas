<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardData.todayOrders || 0 }}</div>
              <div class="stat-label">今日订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ dashboardData.todayGMV || 0 }}</div>
              <div class="stat-label">今日 GMV</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardData.totalMembers || 0 }}</div>
              <div class="stat-label">会员总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardData.lowStockGoods || 0 }}</div>
              <div class="stat-label">库存预警</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-lg">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>订单趋势</span>
            </div>
          </template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快捷入口</span>
            </div>
          </template>
          <div class="quick-actions">
            <router-link to="/shop/cashier/index" class="action-item">
              <el-icon class="action-icon" style="color: #409eff"><CreditCard /></el-icon>
              <span>开单收银</span>
            </router-link>
            <router-link to="/shop/member/list" class="action-item">
              <el-icon class="action-icon" style="color: #67c23a"><User /></el-icon>
              <span>会员管理</span>
            </router-link>
            <router-link to="/shop/inventory/goods" class="action-item">
              <el-icon class="action-icon" style="color: #e6a23c"><Box /></el-icon>
              <span>商品管理</span>
            </router-link>
            <router-link to="/shop/ai/copywriting" class="action-item">
              <el-icon class="action-icon" style="color: #f56c6c"><MagicStick /></el-icon>
              <span>AI 文案</span>
            </router-link>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-lg">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近订单</span>
              <router-link to="/shop/order/list">查看全部</router-link>
            </div>
          </template>
          <el-table :data="dashboardData.recentOrders || []" stripe>
            <el-table-column prop="orderNo" label="订单号" />
            <el-table-column prop="memberName" label="会员" />
            <el-table-column prop="amount" label="金额">
              <template #default="{ row }">
                ¥{{ row.amount }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" />
            <el-table-column prop="createTime" label="创建时间" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { Document, Money, User, Warning, CreditCard, Box, MagicStick } from '@element-plus/icons-vue'
import { getShopDashboard, type ShopDashboardData } from '@/api/dashboard'

const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const dashboardData = ref<ShopDashboardData>({
  todayOrders: 0,
  todayGMV: 0,
  totalMembers: 0,
  lowStockGoods: 0,
  recentOrders: [],
  orderTrend: []
})

async function loadDashboard() {
  try {
    dashboardData.value = await getShopDashboard()
    renderChart()
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

function renderChart() {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  const trend = dashboardData.value.orderTrend || []
  chartInstance.setOption({
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['订单数', '交易额']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trend.map((item) => item.date)
    },
    yAxis: [
      {
        type: 'value',
        name: '订单数'
      },
      {
        type: 'value',
        name: '交易额'
      }
    ],
    series: [
      {
        name: '订单数',
        type: 'line',
        smooth: true,
        data: trend.map((item) => item.count)
      },
      {
        name: '交易额',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: trend.map((item) => item.amount)
      }
    ]
  })
}

onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', () => chartInstance?.resize())
})

onUnmounted(() => {
  chartInstance?.dispose()
  window.removeEventListener('resize', () => chartInstance?.resize())
})
</script>

<style scoped lang="scss">
.dashboard {
  .stat-cards {
    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-icon {
          width: 56px;
          height: 56px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;

          .el-icon {
            font-size: 28px;
            color: #fff;
          }
        }

        .stat-info {
          flex: 1;

          .stat-value {
            font-size: 24px;
            font-weight: bold;
            color: #303133;
            margin-bottom: 4px;
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
          }
        }
      }
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    a {
      color: #409eff;
      font-size: 14px;
      text-decoration: none;
    }
  }

  .chart-container {
    height: 300px;
  }

  .quick-actions {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;

    .action-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 24px 16px;
      background: #f5f7fa;
      border-radius: 8px;
      text-decoration: none;
      color: #606266;
      transition: all 0.3s;

      &:hover {
        background: #ecf5ff;
        color: #409eff;
      }

      .action-icon {
        font-size: 32px;
        margin-bottom: 8px;
      }
    }
  }
}
</style>
