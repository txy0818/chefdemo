<template>
  <div class="order-list">
    <el-card>
      <template #header>
        <span>我的订单</span>
      </template>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="0" />
        <el-tab-pane label="待支付" name="1" />
        <el-tab-pane label="待接单" name="2" />
        <el-tab-pane label="已接单" name="3" />
        <el-tab-pane label="已拒单" name="4" />
        <el-tab-pane label="已完成" name="5" />
        <el-tab-pane label="已取消" name="6" />
      </el-tabs>
      
      <div class="order-cards" v-loading="loading">
        <el-empty v-if="orderList.length === 0 && !loading" description="暂无订单" />
        
        <el-card
          v-for="order in orderList"
          :key="order.id"
          class="order-card"
          shadow="hover"
          @click="goToDetail(order.id)"
        >
          <div class="order-header">
            <span>订单ID: {{ order.id }}</span>
            <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
          </div>
          <el-descriptions :column="2">
            <el-descriptions-item label="厨师">{{ order.chefName }}</el-descriptions-item>
            <el-descriptions-item label="金额">¥{{ (order.totalAmount / 100).toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="时间">{{ formatTime(order.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="人数">{{ order.peopleCount }}人</el-descriptions-item>
            <el-descriptions-item label="支付状态">{{ order.payStatusDesc }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        class="pagination"
        background
        layout="total, prev, pager, next"
        :total="total"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderList as fetchOrderList } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const orderList = ref([])
const activeTab = ref('0')
const total = ref(0)
const queryForm = reactive({
  page: 1,
  size: 10
})

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleString('zh-CN')
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

const getStatusText = (status) => {
  const map = {
    1: '待支付',
    2: '待接单',
    3: '已接单',
    4: '已拒单',
    5: '已完成',
    6: '已取消'
  }
  return map[status] || '未知'
}

const buildQueryParams = () => {
  const params = {
    page: queryForm.page,
    size: queryForm.size
  }
  const status = parseInt(activeTab.value)
  if (status > 0) {
    params.status = status
  }
  return params
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await fetchOrderList(buildQueryParams())
    orderList.value = res.data || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  queryForm.page = 1
  loadData()
}

const goToDetail = (id) => {
  router.push(`/user/order-detail/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.order-cards {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-card {
  cursor: pointer;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  font-weight: bold;
}

.pagination {
  margin-top: 24px;
  justify-content: flex-end;
}
</style>
