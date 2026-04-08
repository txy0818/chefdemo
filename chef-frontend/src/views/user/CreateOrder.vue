<template>
  <div class="create-order">
    <section class="section-heading create-heading">
      <div>
        <h2>创建订单</h2>
        <p>把时间、人数和联系方式一次填清楚，右侧会实时同步本次预约的关键摘要。</p>
      </div>
      <el-button class="back-button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回详情
      </el-button>
    </section>

    <div class="create-grid" v-loading="loading">
      <el-card class="form-card glass-panel" shadow="never">
        <el-form
          ref="orderFormRef"
          :model="orderForm"
          :rules="orderRules"
          label-width="120px"
          class="order-form"
        >
          <div class="form-group">
            <div class="group-title">
              <h3>预约安排</h3>
              <p>先锁定时间，再微调实际开始和结束时段。</p>
            </div>

            <el-form-item label="预约时间段" prop="chefAvailableTimeId">
              <el-select
                v-model="orderForm.chefAvailableTimeId"
                placeholder="请选择可预约的大时间段"
                style="width: 100%"
                @change="handleTimeChange"
              >
                <el-option
                  v-for="time in availableTimes"
                  :key="time.id"
                  :label="`${time.startTimeDesc} - ${time.endTimeDesc}`"
                  :value="time.id"
                  :disabled="time.status !== 1"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="开始时间" prop="startTime" v-if="selectedTime">
              <el-date-picker
                v-model="orderForm.startTime"
                type="datetime"
                format="YYYY-MM-DD HH:mm"
                value-format="x"
                placeholder="请在所选时间段内选择开始时间"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="结束时间" prop="endTime" v-if="selectedTime">
              <el-date-picker
                v-model="orderForm.endTime"
                type="datetime"
                format="YYYY-MM-DD HH:mm"
                value-format="x"
                placeholder="请在所选时间段内选择结束时间"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item v-if="selectedTime">
              <div class="time-range-tip">
                可选范围：{{ selectedTime.startTimeDesc }} 至 {{ selectedTime.endTimeDesc }}
              </div>
            </el-form-item>

            <el-form-item label="用餐人数(人)" prop="peopleCount">
              <div class="people-count-wrap">
                <el-input-number
                  v-model="orderForm.peopleCount"
                  :min="chefInfo?.minPeople || 1"
                  :max="chefInfo?.maxPeople || 20"
                />
                <span class="unit-text">人</span>
                <span class="range-tip" v-if="chefInfo">
                  可选范围：{{ chefInfo.minPeople }}-{{ chefInfo.maxPeople }}人
                </span>
              </div>
            </el-form-item>

            <el-form-item label="特殊要求">
              <el-input
                v-model="orderForm.specialRequirements"
                type="textarea"
                :rows="4"
                placeholder="请填写口味偏好、忌口、儿童餐或其他上门服务要求"
              />
            </el-form-item>
          </div>

          <div class="form-group">
            <div class="group-title">
              <h3>联系信息</h3>
              <p>用于厨师上门沟通和订单履约确认。</p>
            </div>

            <el-form-item label="联系人" prop="contactName">
              <el-input
                v-model="orderForm.contactName"
                placeholder="请输入真实联系人姓名，2 到 20 个字"
                maxlength="20"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="联系电话" prop="contactPhone">
              <el-input
                v-model="orderForm.contactPhone"
                placeholder="请输入可联系的 11 位手机号"
                maxlength="11"
                clearable
              />
            </el-form-item>

            <el-form-item label="联系地址" prop="contactAddress">
              <el-input
                v-model="orderForm.contactAddress"
                placeholder="请输入详细上门地址，建议写到门牌号"
                maxlength="120"
                show-word-limit
              />
            </el-form-item>
          </div>

          <div class="form-actions">
            <el-button class="submit-button" type="primary" @click="handleSubmit" :loading="submitting">
              提交订单
            </el-button>
            <el-button class="cancel-button" @click="goBack">取消</el-button>
          </div>
        </el-form>
      </el-card>

      <aside class="summary-column">
        <el-card class="summary-card glass-panel" shadow="never">
          <div class="group-title">
            <h3>订单摘要</h3>
            <p>下单前把关键条件再确认一遍。</p>
          </div>

          <div class="chef-mini" v-if="chefInfo">
            <div class="mini-avatar">
              <el-avatar :size="56" :src="chefInfo.avatar" />
            </div>
            <div>
              <strong>{{ chefInfo.displayName }}</strong>
              <span>{{ chefInfo.priceDesc }}元/小时</span>
            </div>
          </div>

          <div class="summary-list">
            <div class="summary-item">
              <span>预约时段</span>
              <strong>{{ selectedTime ? `${selectedTime.startTimeDesc} - ${selectedTime.endTimeDesc}` : '未选择' }}</strong>
            </div>
            <div class="summary-item">
              <span>服务时长</span>
              <strong>{{ durationText }}</strong>
            </div>
            <div class="summary-item">
              <span>用餐人数</span>
              <strong>{{ orderForm.peopleCount || 0 }} 人</strong>
            </div>
            <div class="summary-item">
              <span>联系人</span>
              <strong>{{ orderForm.contactName || '待填写' }}</strong>
            </div>
          </div>

          <div class="amount-panel">
            <span>预计订单金额</span>
            <strong>{{ totalAmountDesc }}</strong>
            <small>最终金额会按照所选时长和厨师单价计算。</small>
          </div>
        </el-card>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { chefDetail, chefTimes, createOrder } from '@/api/user'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const orderFormRef = ref(null)
const availableTimes = ref([])
const chefInfo = ref(null)
const selectedTime = ref(null)

const orderForm = reactive({
  chefUserId: parseInt(route.params.chefId),
  chefAvailableTimeId: null,
  startTime: null,
  endTime: null,
  peopleCount: 2,
  specialRequirements: '',
  contactName: '',
  contactPhone: '',
  contactAddress: ''
})

const orderRules = {
  chefAvailableTimeId: [{ required: true, message: '请选择时间段', trigger: 'change' }],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' },
    {
      validator: (_, value, callback) => {
        if (!selectedTime.value) {
          callback(new Error('请先选择预约时间段'))
          return
        }
        if (!value) {
          callback(new Error('请选择开始时间'))
          return
        }
        const start = Number(value)
        if (start < selectedTime.value.startTime || start > selectedTime.value.endTime) {
          callback(new Error('开始时间需在预约时间段范围内'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (_, value, callback) => {
        if (!selectedTime.value) {
          callback(new Error('请先选择预约时间段'))
          return
        }
        if (!value) {
          callback(new Error('请选择结束时间'))
          return
        }
        const end = Number(value)
        const start = Number(orderForm.startTime)
        if (end < selectedTime.value.startTime || end > selectedTime.value.endTime) {
          callback(new Error('结束时间需在预约时间段范围内'))
          return
        }
        if (!start || end <= start) {
          callback(new Error('结束时间必须晚于开始时间'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  peopleCount: [
    { required: true, message: '请输入用餐人数', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        const min = chefInfo.value?.minPeople || 1
        const max = chefInfo.value?.maxPeople || 20
        if (value == null || Number.isNaN(Number(value))) {
          callback(new Error('请输入正确的用餐人数，单位为人'))
          return
        }
        if (Number(value) < min || Number(value) > max) {
          callback(new Error(`用餐人数需在${min}-${max}人之间`))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  contactName: [
    { required: true, message: '请输入联系人', trigger: 'blur' },
    { min: 2, max: 20, message: '联系人姓名长度为 2 到 20 个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  contactAddress: [
    { required: true, message: '请输入联系地址', trigger: 'blur' },
    { min: 5, max: 120, message: '联系地址长度为 5 到 120 个字符', trigger: 'blur' }
  ]
}

const totalAmount = computed(() => {
  if (!chefInfo.value || !orderForm.startTime || !orderForm.endTime) return 0
  const hours = (Number(orderForm.endTime) - Number(orderForm.startTime)) / (1000 * 60 * 60)
  if (hours <= 0) return 0
  return Math.ceil(chefInfo.value.price * hours)
})
const totalAmountDesc = computed(() => `¥${(totalAmount.value / 100).toFixed(2)}`)
const durationText = computed(() => {
  if (!orderForm.startTime || !orderForm.endTime) return '待确认'
  const hours = (Number(orderForm.endTime) - Number(orderForm.startTime)) / (1000 * 60 * 60)
  if (hours <= 0) return '待确认'
  return `${hours.toFixed(hours % 1 === 0 ? 0 : 1)} 小时`
})

const loadData = async () => {
  loading.value = true
  try {
    const [detailRes, timeRes] = await Promise.all([
      chefDetail({ chefUserId: orderForm.chefUserId }),
      chefTimes({ chefUserId: orderForm.chefUserId, page: 1, size: 50 })
    ])
    chefInfo.value = detailRes.data
    availableTimes.value = timeRes.data || []
    
    // 如果 URL 中有 timeId，自动选中
    const timeId = route.query.timeId
    if (timeId) {
      orderForm.chefAvailableTimeId = parseInt(timeId)
      handleTimeChange(orderForm.chefAvailableTimeId)
    }
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

const handleTimeChange = (timeId) => {
  selectedTime.value = availableTimes.value.find(t => t.id === timeId)
  orderForm.startTime = selectedTime.value?.startTime || null
  orderForm.endTime = selectedTime.value?.endTime || null
}

const handleSubmit = async () => {
  if (!orderFormRef.value) return
  
  await orderFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const res = await createOrder(orderForm)
      ElMessage.success('订单创建成功')
      router.push(`/user/order-detail/${res.data.id}`)
    } catch (error) {
      console.error('创建订单失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.create-order {
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

.create-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(320px, 0.85fr);
  gap: 20px;
  align-items: start;
}

.form-card,
.summary-card {
  border: none;
  border-radius: 28px;
}

.order-form {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.form-group {
  padding: 22px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.64);
  border: 1px solid rgba(220, 38, 38, 0.08);
}

.group-title {
  margin-bottom: 18px;
}

.group-title h3 {
  margin: 0 0 8px;
  font-size: 1.08rem;
  color: #3f1111;
}

.group-title p {
  margin: 0;
  color: #8b6b6b;
  line-height: 1.7;
}

.amount {
  font-size: 24px;
  font-weight: 800;
  color: #b91c1c;
}

.time-range-tip {
  color: #8b6b6b;
  font-size: 13px;
  line-height: 1.6;
}

.people-count-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.unit-text {
  color: #6b4a4a;
  white-space: nowrap;
}

.range-tip {
  color: #8b6b6b;
  font-size: 12px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.submit-button,
.cancel-button {
  min-width: 132px;
  min-height: 46px;
  border-radius: 14px;
}

.summary-column {
  position: sticky;
  top: 16px;
}

.chef-mini {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(255, 245, 245, 0.95), rgba(255, 247, 237, 0.95));
}

.mini-avatar {
  display: inline-flex;
  padding: 3px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(248, 113, 113, 0.9), rgba(161, 98, 7, 0.72));
}

.chef-mini strong,
.summary-item strong,
.amount-panel strong {
  color: #3f1111;
}

.chef-mini div {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.chef-mini span {
  color: #8b6b6b;
  font-size: 0.92rem;
}

.summary-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 18px 0;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid rgba(220, 38, 38, 0.08);
}

.summary-item span {
  color: #8b6b6b;
}

.amount-panel {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(135deg, #fff1eb, #fffbeb);
}

.amount-panel strong {
  font-size: 2rem;
  color: #b91c1c;
}

.amount-panel small {
  color: #8b6b6b;
  line-height: 1.6;
}

.order-form :deep(.el-form-item__label) {
  color: #6b3f3f;
  font-weight: 600;
}

.order-form :deep(.el-input__wrapper),
.order-form :deep(.el-select__wrapper),
.order-form :deep(.el-textarea__inner),
.order-form :deep(.el-input-number),
.order-form :deep(.el-date-editor.el-input__wrapper) {
  border-radius: 16px;
  box-shadow: none;
}

.order-form :deep(.el-input__wrapper),
.order-form :deep(.el-select__wrapper),
.order-form :deep(.el-textarea__inner),
.order-form :deep(.el-date-editor.el-input__wrapper) {
  min-height: 46px;
  background: rgba(255, 255, 255, 0.92);
}

@media (max-width: 1024px) {
  .create-grid {
    grid-template-columns: 1fr;
  }

  .summary-column {
    position: static;
  }
}

@media (max-width: 768px) {
  .form-group {
    padding: 18px;
    border-radius: 20px;
  }

  .form-actions {
    flex-direction: column;
  }

  .submit-button,
  .cancel-button {
    width: 100%;
  }
}
</style>
