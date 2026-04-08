<template>
  <div class="order-detail">
    <section class="section-heading detail-heading">
      <div>
        <h2>订单详情</h2>
        <p>这里会集中展示订单状态、支付进度、联系方式和后续可执行操作。</p>
      </div>
      <el-button class="back-button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回订单列表
      </el-button>
    </section>

    <div v-loading="loading" class="detail-stack" v-if="order">
      <section class="status-hero glass-panel">
        <div class="status-main">
          <span class="status-kicker">订单 #{{ order.id }}</span>
          <h1>{{ order.statusDesc }}</h1>
          <p>支付状态：{{ order.payStatusDesc || payStatusLabelMap[order.payStatus] }}</p>
        </div>
        <div class="status-side">
          <el-tag class="status-tag" :type="getStatusType(order.status)" size="large">
            {{ order.statusDesc }}
          </el-tag>
          <strong>{{ order.totalAmountDesc }}</strong>
        </div>
      </section>

      <div class="detail-grid">
        <div class="detail-main">
          <el-card class="detail-card glass-panel" shadow="never">
            <template #header>
              <div class="block-title">
                <span>订单信息</span>
              </div>
            </template>

            <el-descriptions :column="2" border>
              <el-descriptions-item label="订单ID">{{ order.id }}</el-descriptions-item>
              <el-descriptions-item label="订单状态">
                <el-tag :type="getStatusType(order.status)">
                  {{ order.statusDesc }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="厨师">{{ order.chefName }}</el-descriptions-item>
              <el-descriptions-item label="订单金额">
                <span class="amount-text">{{ order.totalAmountDesc }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="所属时间段" :span="2">
                {{ order.chefAvailableTimeDesc }}
              </el-descriptions-item>
              <el-descriptions-item label="开始时间">{{ order.startTimeDesc }}</el-descriptions-item>
              <el-descriptions-item label="结束时间">{{ order.endTimeDesc }}</el-descriptions-item>
              <el-descriptions-item label="用餐人数">{{ order.peopleCount }}人</el-descriptions-item>
              <el-descriptions-item label="支付状态">
                {{ order.payStatusDesc || payStatusLabelMap[order.payStatus] }}
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
          </el-card>
        </div>

        <aside class="detail-side">
          <el-card class="detail-card glass-panel" shadow="never">
            <template #header>
              <div class="block-title">
                <span>当前可操作</span>
              </div>
            </template>

            <div class="action-stack">
              <el-button
                v-if="order.status === 1"
                class="primary-action"
                type="primary"
                size="large"
                @click="handlePay"
              >
                立即支付
              </el-button>

              <el-button
                v-if="order.status === 1 || order.status === 2"
                class="danger-action"
                type="danger"
                size="large"
                @click="handleCancel"
              >
                取消订单
              </el-button>

              <el-button
                v-if="order.status === 5"
                class="warm-action"
                type="warning"
                size="large"
                @click="goToReview"
              >
                评价厨师
              </el-button>

              <el-button
                v-if="order.status === 5"
                class="danger-action"
                type="danger"
                size="large"
                @click="goToReport"
              >
                举报厨师
              </el-button>

              <el-alert
                :title="`订单状态：${order.statusDesc}，支付状态：${order.payStatusDesc || payStatusLabelMap[order.payStatus]}`"
                :type="getStatusAlertType(order.status)"
                :closable="false"
              />
            </div>
          </el-card>
        </aside>
      </div>
    </div>
    
    <!-- 取消订单对话框 -->
    <el-dialog v-model="cancelVisible" title="填写取消原因" width="500px">
      <el-form ref="cancelFormRef" :model="cancelForm" :rules="cancelRules" label-width="100px">
        <el-form-item label="取消原因" prop="cancelReason">
          <el-input
            v-model="cancelForm.cancelReason"
            type="textarea"
            :rows="4"
            maxlength="100"
            show-word-limit
            placeholder="请说明取消原因，方便系统记录并通知厨师"
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
    <el-dialog v-model="payVisible" title="确认支付方式" width="400px">
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
          确认支付 {{ order?.totalAmountDesc }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderDetail, payOrder, cancelOrder } from '@/api/user'
import { getPaymentTypeOptions, getPayStatusLabelMap } from '@/api/constant'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Wallet } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const cancelling = ref(false)
const paying = ref(false)
const order = ref(null)
const cancelVisible = ref(false)
const payVisible = ref(false)
const cancelFormRef = ref(null)
const paymentTypeOptions = ref([])
const payStatusLabelMap = ref({})

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

const getStatusType = (status) => {
  const map = { 1: 'warning', 2: 'info', 3: 'primary', 4: 'danger', 5: 'success', 6: 'info' }
  return map[status] || 'info'
}

const getStatusAlertType = (status) => {
  const map = { 1: 'warning', 2: 'info', 3: 'success', 4: 'error', 5: 'success', 6: 'info' }
  return map[status] || 'info'
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

const loadPayStatusOptions = async () => {
  try {
    payStatusLabelMap.value = await getPayStatusLabelMap()
  } catch (error) {
    console.error('加载支付状态枚举失败:', error)
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
  loadPayStatusOptions()
})
</script>

<style scoped>
.order-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 44px;
  padding: 0 18px;
  border-radius: 14px;
  border: 1px solid rgba(220, 38, 38, 0.12);
  color: #7f1d1d;
}

.detail-stack {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.status-hero {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
  padding: 28px;
  border-radius: 30px;
}

.status-kicker {
  display: inline-flex;
  min-height: 34px;
  align-items: center;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(255, 241, 235, 0.95);
  color: #a16207;
  font-weight: 700;
  font-size: 0.84rem;
}

.status-main h1 {
  margin: 16px 0 10px;
  font-family: var(--font-display);
  font-size: clamp(2.1rem, 4vw, 3rem);
  font-weight: 400;
  color: #3f1111;
}

.status-main p {
  margin: 0;
  color: #8b6b6b;
}

.status-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.status-side strong {
  font-size: 2rem;
  color: #b91c1c;
}

.status-tag {
  border-radius: 999px;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(300px, 0.9fr);
  gap: 20px;
}

.detail-card {
  border: none;
  border-radius: 28px;
}

.block-title span {
  font-size: 1.05rem;
  font-weight: 700;
  color: #3f1111;
}

.amount-text {
  color: #b91c1c;
  font-weight: 800;
}

.action-stack {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.primary-action,
.danger-action,
.warm-action {
  width: 100%;
  min-height: 46px;
  margin-left: 0;
  border-radius: 14px;
}

.detail-card :deep(.el-descriptions__label) {
  width: 110px;
  color: #8b6b6b;
}

.detail-card :deep(.el-descriptions__body) {
  background: transparent;
}

.detail-card :deep(.el-radio) {
  min-height: 44px;
  align-items: center;
}

@media (max-width: 1024px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .status-hero {
    padding: 22px 18px;
    border-radius: 24px;
    flex-direction: column;
    align-items: flex-start;
  }

  .status-side {
    width: 100%;
    align-items: flex-start;
  }
}
</style>
