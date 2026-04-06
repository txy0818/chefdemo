<template>
  <div class="statistics">
    <section class="section-heading page-heading">
      <div>
        <span class="hero-kicker">平台概览</span>
        <h2>订单数据统计</h2>
        <p>汇总关键订单指标，快速观察当前平台运行状态和近 7 天趋势变化。</p>
      </div>
      <el-button type="primary" @click="loadData" :loading="loading">
        刷新数据
      </el-button>
    </section>

    <el-card class="panel-card glass-panel" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <strong>关键指标</strong>
            <p>结合状态分布与近 7 天趋势，辅助后台快速发现异常波动。</p>
          </div>
        </div>
      </template>
      
      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-cards">
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="metric-card" shadow="never">
            <div class="stat-card">
              <div class="stat-icon" style="background: #409EFF">
                <el-icon :size="30"><Document /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.total }}</div>
                <div class="stat-label">总订单数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="metric-card" shadow="never">
            <div class="stat-card">
              <div class="stat-icon" style="background: #E6A23C">
                <el-icon :size="30"><Clock /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.pendingPayment }}</div>
                <div class="stat-label">待支付</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="metric-card" shadow="never">
            <div class="stat-card">
              <div class="stat-icon" style="background: #67C23A">
                <el-icon :size="30"><Check /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.completed }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="metric-card" shadow="never">
            <div class="stat-card">
              <div class="stat-icon" style="background: #F56C6C">
                <el-icon :size="30"><Close /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.cancelled }}</div>
                <div class="stat-label">已取消</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 图表 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <span>今日订单状态分布</span>
            </template>
            <div ref="pieChartRef" style="height: 400px"></div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <span>近7天订单趋势</span>
            </template>
            <div ref="lineChartRef" style="height: 400px"></div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { orderStatistics } from '@/api/admin'
import { use } from 'echarts/core'
import { PieChart, LineChart } from 'echarts/charts'
import {
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import * as echarts from 'echarts/core'
import { Document, Clock, Check, Close } from '@element-plus/icons-vue'

use([
  PieChart,
  LineChart,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  CanvasRenderer
])

const loading = ref(false)
const pieChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let lineChart = null
const handleResize = () => {
  pieChart?.resize()
  lineChart?.resize()
}

const stats = reactive({
  total: 0,
  pendingPayment: 0,
  pendingAccept: 0,
  accepted: 0,
  completed: 0,
  cancelled: 0,
  rejected: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await orderStatistics()
    const data = res.data
    
    // 更新统计数据
    Object.assign(stats, {
      total: data.total || 0,
      pendingPayment: data.pendingPayment || 0,
      pendingAccept: data.pendingAccept || 0,
      accepted: data.accepted || 0,
      completed: data.completed || 0,
      cancelled: data.cancelled || 0,
      rejected: data.rejected || 0
    })
    
    // 渲染图表
    await nextTick()
    renderPieChart(data.todayPie || [])
    renderLineChart(data.dates || [], data.counts || [])
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const renderPieChart = (pieData) => {
  if (!pieChartRef.value) return
  
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '订单状态',
        type: 'pie',
        radius: '50%',
        data: pieData.map(item => ({
          name: item.name,
          value: item.value
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  pieChart.setOption(option)
}

const renderLineChart = (dates, counts) => {
  if (!lineChartRef.value) return
  
  if (!lineChart) {
    lineChart = echarts.init(lineChartRef.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '订单数量',
        type: 'line',
        data: counts,
        smooth: true,
        areaStyle: {
          color: 'rgba(64, 158, 255, 0.2)'
        },
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  }
  
  lineChart.setOption(option)
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  loadData()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  lineChart?.dispose()
  pieChart = null
  lineChart = null
})
</script>

<style scoped>
.statistics {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.card-header strong {
  color: #24375a;
  font-size: 1.08rem;
}

.card-header p {
  margin: 8px 0 0;
  color: #66758d;
  font-size: 13px;
}

.panel-card {
  border: none;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
}

.metric-card,
.chart-card {
  border: 1px solid rgba(59, 130, 246, 0.08);
  background: rgba(255, 255, 255, 0.82);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 15px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #24375a;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #7a879d;
}

@media (max-width: 768px) {
  .statistics :deep(.el-button) {
    width: 100%;
  }
}
</style>
