<template>
  <div class="order-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
        </div>
      </template>
      
      <!-- 状态筛选 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="0" />
        <el-tab-pane label="待支付" name="1" />
        <el-tab-pane label="待接单" name="2" />
        <el-tab-pane label="已接单" name="3" />
        <el-tab-pane label="已拒单" name="4" />
        <el-tab-pane label="已完成" name="5" />
        <el-tab-pane label="已取消" name="6" />
      </el-tabs>
      
      <!-- 订单列表 -->
      <div class="order-list" v-loading="loading">
        <el-empty v-if="orderList.length === 0 && !loading" description="暂无订单" />
        
        <el-card
          v-for="order in orderList"
          :key="order.id"
          class="order-card"
          shadow="hover"
        >
          <div class="order-header">
            <span class="order-id">订单ID: {{ order.id }}</span>
            <el-tag :type="getStatusType(order.status)">
              {{ getStatusText(order.status) }}
            </el-tag>
          </div>
          
          <el-descriptions :column="2" class="order-info">
            <el-descriptions-item label="用户">{{ order.userName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ order.contactPhone }}</el-descriptions-item>
            <el-descriptions-item label="所属时间段" :span="2">
              {{ formatTimeRange(order.chefAvailableStartTime, order.chefAvailableEndTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="开始时间">{{ formatTime(order.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">{{ formatTime(order.endTime) }}</el-descriptions-item>
            <el-descriptions-item label="用餐人数">{{ order.peopleCount }}人</el-descriptions-item>
            <el-descriptions-item label="订单金额">¥{{ (order.totalAmount / 100).toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="支付状态">{{ order.payStatusDesc }}</el-descriptions-item>
            <el-descriptions-item label="联系地址" :span="2">{{ order.contactAddress }}</el-descriptions-item>
            <el-descriptions-item label="特殊要求" :span="2">
              {{ order.specialRequirements || '无' }}
            </el-descriptions-item>
          </el-descriptions>
          
          <div class="order-actions">
            <el-button
              v-if="order.status === 2"
              type="success"
              @click="handleAccept(order)"
            >
              接单
            </el-button>
            <el-button
              v-if="order.status === 2"
              type="danger"
              @click="handleReject(order)"
            >
              拒单
            </el-button>
            <el-button
              v-if="order.status === 3"
              type="primary"
              :disabled="!canCompleteOrder(order)"
              @click="handleComplete(order)"
            >
              完成订单
            </el-button>
            <el-button type="info" @click="handleViewDetail(order)">
              查看详情
            </el-button>
          </div>
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
    
    <!-- 拒单对话框 -->
    <el-dialog v-model="rejectVisible" title="拒单" width="500px">
      <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules" label-width="80px">
        <el-form-item label="拒单原因" prop="reason">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            maxlength="100"
            show-word-limit
            placeholder="请输入拒单原因，最多100字"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject" :loading="rejecting">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="700px">
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="订单ID">{{ currentOrder.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.status)">
            {{ getStatusText(currentOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户">{{ currentOrder.userName }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentOrder.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrder.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="所属时间段" :span="2">
          {{ formatTimeRange(currentOrder.chefAvailableStartTime, currentOrder.chefAvailableEndTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="用餐人数">{{ currentOrder.peopleCount }}人</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatTime(currentOrder.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatTime(currentOrder.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ (currentOrder.totalAmount / 100).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">
          {{ currentOrder.payStatusDesc }}
        </el-descriptions-item>
        <el-descriptions-item label="联系地址" :span="2">{{ currentOrder.contactAddress }}</el-descriptions-item>
        <el-descriptions-item label="特殊要求" :span="2">
          {{ currentOrder.specialRequirements || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { orderList as fetchOrderList, acceptOrder, rejectOrder, completeOrder, getProfile as getChefProfile } from '@/api/chef'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const rejecting = ref(false)
const orderList = ref([])
const activeTab = ref('0')
const total = ref(0)
const rejectVisible = ref(false)
const detailVisible = ref(false)
const currentOrder = ref(null)
const rejectFormRef = ref(null)
const queryForm = reactive({
  page: 1,
  size: 10
})

const rejectForm = reactive({
  orderId: null,
  reason: ''
})

const rejectRules = {
  reason: [
    { required: true, message: '请输入拒单原因', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (!value || !value.trim()) {
          callback(new Error('请输入拒单原因'))
          return
        }
        if (value.trim().length > 100) {
          callback(new Error('拒单原因最多100字'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatTimeRange = (startTime, endTime) => {
  if (!startTime || !endTime) return '-'
  return `${formatTime(startTime)} - ${formatTime(endTime)}`
}

const canCompleteOrder = (order) => {
  if (!order?.endTime) return false
  return Date.now() >= Number(order.endTime)
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

const ensureAuditApproved = async () => {
  const res = await getChefProfile({})
  if (res.data?.auditStatus !== '通过') {
    ElMessage.warning('厨师审核尚未通过，请先完善资料并等待审核')
    router.replace('/chef/profile')
    return false
  }
  return true
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: queryForm.page,
      size: queryForm.size
    }
    if (activeTab.value !== '0') {
      params.status = parseInt(activeTab.value)
    }
    const res = await fetchOrderList(params)
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

const handleAccept = (order) => {
  ElMessageBox.confirm('确定要接受这个订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await acceptOrder({
        orderId: order.id
      })
      
      ElMessage.success('接单成功')
      queryForm.page = 1
      loadData()
    } catch (error) {
      console.error('接单失败:', error)
    }
  })
}

const handleReject = (order) => {
  currentOrder.value = order
  rejectForm.orderId = order.id
  rejectForm.reason = ''
  rejectVisible.value = true
}

const confirmReject = async () => {
  if (!rejectFormRef.value) return
  const valid = await rejectFormRef.value.validate().catch(() => false)
  if (!valid) return

  rejecting.value = true
  try {
    rejectForm.reason = rejectForm.reason.trim()
    await rejectOrder(rejectForm)
    ElMessage.success('拒单成功')
    rejectVisible.value = false
    queryForm.page = 1
    loadData()
  } catch (error) {
    console.error('拒单失败:', error)
  } finally {
    rejecting.value = false
  }
}

const handleComplete = (order) => {
  if (!canCompleteOrder(order)) {
    ElMessage.warning('未到预约结束时间，暂不能完成订单')
    return
  }
  ElMessageBox.confirm('确定要完成这个订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await completeOrder({
        orderId: order.id
      })
      
      ElMessage.success('订单已完成')
      queryForm.page = 1
      loadData()
    } catch (error) {
      console.error('完成订单失败:', error)
    }
  })
}

const handleViewDetail = (order) => {
  currentOrder.value = order
  detailVisible.value = true
}

onMounted(() => {
  ensureAuditApproved().then((passed) => {
    if (passed) {
      loadData()
    }
  })
})
</script>

<style scoped>
.order-manage {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.order-list {
  min-height: 400px;
}

.order-card {
  margin-bottom: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.order-id {
  font-weight: bold;
  color: #333;
}

.order-info {
  margin-bottom: 15px;
}

.order-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.pagination {
  margin-top: 24px;
  justify-content: flex-end;
}
</style>
