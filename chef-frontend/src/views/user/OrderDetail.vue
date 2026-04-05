<template>
  <div class="order-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
          <span>订单详情</span>
        </div>
      </template>
      
      <div v-if="order">
        <!-- 订单状态 -->
        <el-alert
          :title="`订单状态：${getStatusText(order.status)}，支付状态：${order.payStatusDesc}`"
          :type="getStatusAlertType(order.status)"
          :closable="false"
          style="margin-bottom: 20px"
        />
        
        <!-- 订单信息 -->
        <el-descriptions title="订单信息" :column="2" border>
          <el-descriptions-item label="订单ID">{{ order.id }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(order.status)">
              {{ getStatusText(order.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="厨师">{{ order.chefName }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">
            <span style="color: #f56c6c; font-weight: bold">
              ¥{{ (order.totalAmount / 100).toFixed(2) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="所属时间段" :span="2">
            {{ formatTimeRange(order.chefAvailableStartTime, order.chefAvailableEndTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatTime(order.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatTime(order.endTime) }}</el-descriptions-item>
          <el-descriptions-item label="用餐人数">{{ order.peopleCount }}人</el-descriptions-item>
          <el-descriptions-item label="支付状态">
            {{ order.payStatusDesc || (order.payStatus === 1 ? '未支付' : '已支付') }}
          </el-descriptions-item>
          <el-descriptions-item label="联系人">{{ order.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ order.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="联系地址" :span="2">{{ order.contactAddress }}</el-descriptions-item>
          <el-descriptions-item label="特殊要求" :span="2">
            {{ order.specialRequirements || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="取消原因" :span="2" v-if="order.cancelReason && order.cancelReason !== '-'">
            {{ order.cancelReason }}
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 操作按钮 -->
        <div class="action-bar">
          <el-button
            v-if="order.status === 1"
            type="primary"
            size="large"
            @click="handlePay"
          >
            立即支付
          </el-button>
          
          <el-button
            v-if="order.status === 1 || order.status === 2"
            type="danger"
            size="large"
            @click="handleCancel"
          >
            取消订单
          </el-button>
          
          <el-button
            v-if="order.status === 5"
            type="warning"
            size="large"
            @click="goToReview"
          >
            评价厨师
          </el-button>
          
          <el-button
            v-if="order.status === 5"
            type="danger"
            size="large"
            @click="goToReport"
          >
            举报厨师
          </el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 取消订单对话框 -->
    <el-dialog v-model="cancelVisible" title="取消订单" width="500px">
      <el-form ref="cancelFormRef" :model="cancelForm" :rules="cancelRules" label-width="100px">
        <el-form-item label="取消原因" prop="cancelReason">
          <el-input
            v-model="cancelForm.cancelReason"
            type="textarea"
            :rows="4"
            maxlength="100"
            show-word-limit
            placeholder="请输入取消原因，最多100字"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="cancelVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCancel" :loading="cancelling">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 支付对话框 -->
    <el-dialog v-model="payVisible" title="选择支付方式" width="400px">
      <el-radio-group v-model="payForm.payType" style="display: flex; flex-direction: column; gap: 15px">
        <el-radio
          v-for="item in paymentTypeOptions"
          :key="item.value"
          :label="item.value"
        >
          <el-icon><Wallet /></el-icon>
          <span style="margin-left: 10px">{{ item.label }}</span>
        </el-radio>
      </el-radio-group>
      
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPay" :loading="paying">
          确认支付 ¥{{ (order?.totalAmount / 100).toFixed(2) }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderDetail, payOrder, cancelOrder } from '@/api/user'
import { getPaymentTypeOptions } from '@/api/constant'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Wallet } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const cancelling = ref(false)
const paying = ref(false)
const order = ref(null)
const cancelVisible = ref(false)
const payVisible = ref(false)
const cancelFormRef = ref(null)
const paymentTypeOptions = ref([])

const cancelForm = reactive({
  orderId: null,
  cancelReason: ''
})

const cancelRules = {
  cancelReason: [
    { required: true, message: '请输入取消原因', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (!value || !value.trim()) {
          callback(new Error('请输入取消原因'))
          return
        }
        if (value.trim().length > 100) {
          callback(new Error('取消原因最多100字'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const payForm = reactive({
  orderId: null,
  payType: 1
})

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleString('zh-CN')
}

const formatTimeRange = (startTime, endTime) => {
  if (!startTime || !endTime) return '-'
  return `${formatTime(startTime)} - ${formatTime(endTime)}`
}

const getStatusType = (status) => {
  const map = { 1: 'warning', 2: 'info', 3: 'primary', 4: 'danger', 5: 'success', 6: 'info' }
  return map[status] || 'info'
}

const getStatusAlertType = (status) => {
  const map = { 1: 'warning', 2: 'info', 3: 'success', 4: 'error', 5: 'success', 6: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 1: '待支付', 2: '待接单', 3: '已接单', 4: '已拒单', 5: '已完成', 6: '已取消' }
  return map[status] || '未知'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await orderDetail({
      orderId: parseInt(route.params.id)
    })
    order.value = res.data
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

const loadPaymentTypeOptions = async () => {
  try {
    const res = await getPaymentTypeOptions()
    paymentTypeOptions.value = Array.isArray(res.data) ? res.data : []
    if (paymentTypeOptions.value.length > 0) {
      payForm.payType = paymentTypeOptions.value[0].value
    }
  } catch (error) {
    console.error('加载支付方式失败:', error)
  }
}

const handlePay = () => {
  payForm.orderId = order.value.id
  payForm.payType = 1
  payVisible.value = true
}

const confirmPay = async () => {
  paying.value = true
  try {
    await payOrder(payForm)
    ElMessage.success('支付成功')
    payVisible.value = false
    loadData()
  } catch (error) {
    console.error('支付失败:', error)
  } finally {
    paying.value = false
  }
}

const handleCancel = () => {
  cancelForm.orderId = order.value.id
  cancelForm.cancelReason = ''
  cancelVisible.value = true
}

const confirmCancel = async () => {
  if (!cancelFormRef.value) return
  await cancelFormRef.value.validate(async valid => {
    if (!valid) return

    cancelling.value = true
    try {
      cancelForm.cancelReason = cancelForm.cancelReason.trim()
      await cancelOrder(cancelForm)
      ElMessage.success('订单已取消')
      cancelVisible.value = false
      loadData()
    } catch (error) {
      console.error('取消失败:', error)
    } finally {
      cancelling.value = false
    }
  })
}

const goToReview = () => {
  router.push(`/user/review/${order.value.id}`)
}

const goToReport = () => {
  router.push(`/user/report/${order.value.id}`)
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadData()
  loadPaymentTypeOptions()
})
</script>

<style scoped>
.order-detail {
  max-width: 900px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 18px;
  font-weight: bold;
}

.action-bar {
  margin-top: 30px;
  display: flex;
  gap: 15px;
  justify-content: center;
}
</style>
