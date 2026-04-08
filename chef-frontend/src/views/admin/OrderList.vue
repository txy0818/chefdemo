<template>
  <div class="order-list">
    <section class="section-heading page-heading">
      <div>
        <span class="hero-kicker">订单总览</span>
        <h2>订单列表</h2>
        <p>统一查看平台订单状态、时间范围和联系人信息，便于后台排查问题。</p>
      </div>
      <div class="metric-pill">共 {{ total }} 笔</div>
    </section>

    <el-card class="panel-card glass-panel" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <strong>筛选订单</strong>
            <p>按订单状态快速定位待处理、已完成或异常关闭的订单。</p>
          </div>
        </div>
      </template>
      
      <!-- 查询条件 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="订单状态">
          <el-select v-model="queryForm.status" placeholder="请选择订单状态" clearable style="width: 200px">
            <el-option
              v-for="item in orderStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 表格 -->
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="订单ID" width="100" />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="chefName" label="厨师" width="120" />
        <el-table-column label="开始时间" width="180">
          <template #default="{ row }">
            {{ row.startTimeDesc }}
          </template>
        </el-table-column>
        <el-table-column label="结束时间" width="180">
          <template #default="{ row }">
            {{ row.endTimeDesc }}
          </template>
        </el-table-column>
        <el-table-column label="金额(元)" width="100">
          <template #default="{ row }">
            {{ row.totalAmountDesc }}
          </template>
        </el-table-column>
        <el-table-column prop="peopleCount" label="人数(人)" width="90" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handleQuery"
        class="app-pagination"
      />
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="700px">
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="订单ID">{{ currentOrder.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.status)">
            {{ currentOrder.statusDesc }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户">{{ currentOrder.userName }}</el-descriptions-item>
        <el-descriptions-item label="厨师">{{ currentOrder.chefName }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentOrder.startTimeDesc }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentOrder.endTimeDesc }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">{{ currentOrder.totalAmountDesc }}</el-descriptions-item>
        <el-descriptions-item label="用餐人数">{{ currentOrder.peopleCount }}人</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentOrder.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrder.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="联系地址" :span="2">{{ currentOrder.contactAddress }}</el-descriptions-item>
        <el-descriptions-item label="特殊要求" :span="2">{{ currentOrder.specialRequirements || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { orderList } from '@/api/admin'
import { getOrderStatusOptions } from '@/api/constant'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentOrder = ref(null)
const orderStatusOptions = ref([])

const queryForm = reactive({
  status: null,
  page: 1,
  size: 10
})

const buildQueryParams = () => {
  const params = {
    page: queryForm.page,
    size: queryForm.size
  }
  if (queryForm.status != null) {
    params.status = queryForm.status
  }
  return params
}

const getStatusType = (status) => {
  const map = {
    1: 'warning',
    2: 'info',
    3: 'primary',
    4: 'danger',
    5: 'success',
    6: 'info'
  }
  return map[status] || 'info'
}

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await orderList(buildQueryParams())
    tableData.value = res.data
    total.value = res.total
  } catch (error) {
    console.error('查询失败:', error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.status = null
  queryForm.page = 1
  handleQuery()
}

const handleViewDetail = (row) => {
  currentOrder.value = row
  detailVisible.value = true
}

onMounted(() => {
  getOrderStatusOptions()
    .then(res => {
      orderStatusOptions.value = Array.isArray(res.data) ? res.data : []
    })
    .catch(error => {
      console.error('加载订单状态枚举失败:', error)
    })
  handleQuery()
})
</script>

<style scoped>
.order-list {
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

.query-form {
  margin-bottom: 20px;
  padding: 20px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid rgba(59, 130, 246, 0.08);
}

.order-list :deep(.el-form-item) {
  margin-bottom: 12px;
}

.order-list :deep(.el-pagination) {
  margin-top: 24px;
  justify-content: flex-end;
}
</style>
