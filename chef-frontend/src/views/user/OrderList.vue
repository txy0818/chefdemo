<template>
  <div class="order-list">
    <section class="section-heading order-heading">
      <div>
        <span class="hero-kicker">进度总览</span>
        <h2>我的订单</h2>
        <p>从待支付到已完成，所有订单都在这里按状态归档，便于你快速追踪进度。</p>
      </div>
      <div class="metric-pill">共 {{ total }} 笔</div>
    </section>

    <el-card class="order-shell glass-panel" shadow="never">
      <div class="status-strip" aria-label="订单状态筛选">
        <div
          v-for="item in tabOptions"
          :key="item.name"
          class="status-pill"
          :class="{ 'is-active': activeTab === item.name }"
          @click="activeTab = item.name; handleTabChange()"
        >
          <strong>{{ item.label }}</strong>
          <span>{{ getCountText(item.name) }}</span>
        </div>
      </div>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane v-for="item in tabOptions" :key="item.name" :label="item.label" :name="item.name" />
      </el-tabs>
      
      <div class="order-cards" v-loading="loading">
        <el-empty v-if="orderList.length === 0 && !loading" description="当前筛选条件下还没有订单记录" />
        
        <el-card
          v-for="order in orderList"
          :key="order.id"
          class="order-card"
          shadow="never"
          @click="goToDetail(order.id)"
        >
          <div class="order-header">
            <div class="order-header-main">
              <span class="order-no">订单 #{{ order.id }}</span>
              <span class="order-chef">{{ order.chefName }}</span>
            </div>
            <div class="order-badges">
              <el-tag :type="getStatusType(order.status)">{{ order.statusDesc }}</el-tag>
              <span class="pay-status">{{ order.payStatusDesc }}</span>
            </div>
          </div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="厨师">{{ order.chefName }}</el-descriptions-item>
            <el-descriptions-item label="金额">{{ order.totalAmountDesc }}</el-descriptions-item>
            <el-descriptions-item label="时间">{{ order.startTimeDesc }}</el-descriptions-item>
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
import { ref, reactive, onBeforeUnmount, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderList as fetchOrderList } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { getOrderStatusTabOptions } from '@/api/constant'
import { isOrderRealtimePayload, REALTIME_DATA_REFRESH_EVENT } from '@/utils/notification'

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
const tabOptions = ref([{ label: '全部', name: '0' }])

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

const getCountText = (tabName) => {
  if (tabName === activeTab.value) {
    return `${orderList.value.length} 条当前结果`
  }
  if (tabName === '0') {
    return `${total.value} 笔订单`
  }
  return '点击筛选'
}

const handleRealtimeRefresh = async (event) => {
  if (!isOrderRealtimePayload(event?.detail?.payload)) return
  await loadData()
}

onMounted(() => {
  getOrderStatusTabOptions()
    .then(options => {
      tabOptions.value = options.map(item => ({
        label: item.label,
        name: String(item.value)
      }))
    })
    .catch(error => {
      console.error('加载订单状态枚举失败:', error)
    })
  loadData()
  window.addEventListener(REALTIME_DATA_REFRESH_EVENT, handleRealtimeRefresh)
})

onBeforeUnmount(() => {
  window.removeEventListener(REALTIME_DATA_REFRESH_EVENT, handleRealtimeRefresh)
})
</script>

<style scoped>
.order-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.order-shell {
  border: none;
  border-radius: var(--radius-card);
}

.status-strip {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.status-pill {
  display: flex;
  flex-direction: column;
  gap: 6px;
  justify-content: center;
  min-height: 78px;
  padding: 14px 16px;
  border: 1px solid rgba(220, 38, 38, 0.08);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
  cursor: pointer;
  transition: transform 220ms ease, border-color 220ms ease, box-shadow 220ms ease;
}

.status-pill strong {
  color: #3f1111;
}

.status-pill span {
  font-size: 0.82rem;
  color: #8b6b6b;
}

.status-pill.is-active,
.status-pill:hover {
  transform: translateY(-2px);
  border-color: rgba(220, 38, 38, 0.18);
  box-shadow: 0 16px 24px rgba(127, 29, 29, 0.08);
}

.status-pill.is-active {
  background: linear-gradient(135deg, rgba(255, 245, 245, 0.98), rgba(255, 247, 237, 0.98));
}

.order-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  cursor: pointer;
  padding: 6px;
  border: 1px solid rgba(220, 38, 38, 0.08);
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(255, 249, 245, 0.96));
  box-shadow: 0 18px 36px rgba(127, 29, 29, 0.08);
  transition: transform 220ms ease, box-shadow 220ms ease, border-color 220ms ease;
}

.order-card:hover {
  transform: translateY(-4px);
  border-color: rgba(220, 38, 38, 0.18);
  box-shadow: 0 24px 44px rgba(127, 29, 29, 0.12);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
}

.order-badges {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.order-header-main {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.order-no {
  font-size: 1rem;
  font-weight: 800;
  color: #3f1111;
}

.order-chef {
  color: #8b6b6b;
  font-size: 0.92rem;
}

.pay-status {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(255, 247, 237, 0.92);
  color: #9a3412;
  font-size: 0.82rem;
  font-weight: 700;
}

.pagination {
  margin-top: 24px;
  justify-content: flex-end;
}

.order-shell :deep(.el-tabs__header) {
  margin-bottom: 18px;
}

.order-shell :deep(.el-tabs__nav-wrap::after) {
  background-color: rgba(220, 38, 38, 0.08);
}

.order-shell :deep(.el-tabs__item) {
  color: #8b6b6b;
}

.order-shell :deep(.el-tabs__item.is-active) {
  color: #b91c1c;
  font-weight: 700;
}

.order-shell :deep(.el-tabs__active-bar) {
  background-color: #dc2626;
}

.order-shell :deep(.el-descriptions__label) {
  width: 100px;
  color: #8b6b6b;
}

.order-shell :deep(.el-descriptions__body) {
  background: transparent;
}

@media (max-width: 1100px) {
  .status-strip {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .status-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .order-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .order-badges {
    width: 100%;
  }
}
</style>
