<template>
  <div class="platform-dashboard">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardData.totalTenants || 0 }}</div>
              <div class="stat-label">总门店数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a">
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
            <div class="stat-icon" style="background: #e6a23c">
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
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ dashboardData.todayGMV || 0 }}</div>
              <div class="stat-label">今日 GMV</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-lg">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>平台订单趋势</span>
          </template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { OfficeBuilding, User, Document, Money } from '@element-plus/icons-vue'
import { getPlatformDashboard, type PlatformDashboardData } from '@/api/platform'

const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const dashboardData = ref<PlatformDashboardData>({
  totalTenants: 0,
  totalMembers: 0,
  todayOrders: 0,
  todayGMV: 0,
  orderTrend: []
})

async function loadDashboard() {
  try {
    dashboardData.value = await getPlatformDashboard()
    renderChart()
  } catch (e) {
    // 接口暂未实现，使用默认空数据
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
.platform-dashboard {
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

  .chart-container {
    height: 400px;
  }
}
</style>
