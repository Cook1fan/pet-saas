<template>
  <div class="qr-code-stats">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>扫码统计（近30天）</span>
        </div>
      </template>
      <el-row :gutter="20" class="stat-items">
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ stats.totalScan || 0 }}</div>
            <div class="stat-label">总扫码</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item new-user">
            <div class="stat-value">{{ stats.newUser || 0 }}</div>
            <div class="stat-label">新用户</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item old-user">
            <div class="stat-value">{{ stats.oldUser || 0 }}</div>
            <div class="stat-label">老用户</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item guest-user">
            <div class="stat-value">{{ stats.guestUser || 0 }}</div>
            <div class="stat-label">游客访问</div>
          </div>
        </el-col>
      </el-row>
      <div ref="chartRef" class="chart-container"></div>
    </el-card>

    <el-card class="mt-md">
      <template #header>
        <div class="card-header">
          <span>每日明细</span>
        </div>
      </template>
      <el-table :data="dailyStats" stripe>
        <el-table-column prop="date" label="日期" width="150" />
        <el-table-column prop="scanCount" label="扫码数" width="120" />
        <el-table-column prop="newUser" label="新用户" width="120" />
        <el-table-column prop="oldUser" label="老用户" width="120" />
        <el-table-column prop="guestUser" label="游客" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import type { DailyStat } from '@/api/qrcode'

interface Props {
  stats: {
    totalScan?: number
    newUser?: number
    oldUser?: number
    guestUser?: number
    dailyStats?: DailyStat[]
  }
}

const props = defineProps<Props>()

const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const dailyStats = computed(() => {
  return (props.stats.dailyStats || []).map(item => ({
    date: item.date,
    scanCount: item.scanCount,
    newUser: item.newUser || 0,
    oldUser: (item.scanCount || 0) - (item.newUser || 0),
    guestUser: 0
  }))
})

function renderChart() {
  if (!chartRef.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const data = props.stats.dailyStats || []

  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['扫码数', '新用户']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.date)
    },
    yAxis: {
      type: 'value',
      name: '扫码数'
    },
    series: [
      {
        name: '扫码数',
        type: 'bar',
        data: data.map(item => item.scanCount),
        itemStyle: {
          color: '#409eff'
        }
      },
      {
        name: '新用户',
        type: 'bar',
        data: data.map(item => item.newUser || 0),
        itemStyle: {
          color: '#67c23a'
        }
      }
    ]
  })
}

function initChart() {
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value)
    renderChart()
  }
}

function resizeChart() {
  chartInstance?.resize()
}

watch(() => props.stats, () => {
  renderChart()
}, { deep: true })

onMounted(() => {
  initChart()
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  chartInstance?.dispose()
  window.removeEventListener('resize', resizeChart)
})
</script>

<style scoped lang="scss">
.qr-code-stats {
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .stat-items {
    margin-bottom: 20px;
  }

  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16px;
    border-radius: 8px;
    background: #f5f7fa;

    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #303133;
    }

    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 4px;
    }

    &.new-user .stat-value {
      color: #67c23a;
    }

    &.old-user .stat-value {
      color: #409eff;
    }

    &.guest-user .stat-value {
      color: #e6a23c;
    }
  }

  .chart-container {
    height: 250px;
  }

  .mt-md {
    margin-top: 20px;
  }
}
</style>
