<template>
  <div class="create-order">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
          <span>创建订单</span>
        </div>
      </template>
      
      <el-form
        ref="orderFormRef"
        :model="orderForm"
        :rules="orderRules"
        label-width="120px"
        v-loading="loading"
      >
        <!-- 选择时间段 -->
        <el-form-item label="预约时间段" prop="chefAvailableTimeId">
          <el-select
            v-model="orderForm.chefAvailableTimeId"
            placeholder="请选择预约时间段"
            style="width: 100%"
            @change="handleTimeChange"
          >
            <el-option
              v-for="time in availableTimes"
              :key="time.id"
              :label="`${formatTime(time.startTime)} - ${formatTime(time.endTime)}`"
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
            placeholder="请选择开始时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime" v-if="selectedTime">
          <el-date-picker
            v-model="orderForm.endTime"
            type="datetime"
            format="YYYY-MM-DD HH:mm"
            value-format="x"
            placeholder="请选择结束时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item v-if="selectedTime">
          <div class="time-range-tip">
            可选范围：{{ formatDetailTime(selectedTime.startTime) }} 至 {{ formatDetailTime(selectedTime.endTime) }}
          </div>
        </el-form-item>
        
        <!-- 用餐人数 -->
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
        
        <!-- 特殊要求 -->
        <el-form-item label="特殊要求">
          <el-input
            v-model="orderForm.specialRequirements"
            type="textarea"
            :rows="4"
            placeholder="请输入特殊要求（如忌口、偏好等）"
          />
        </el-form-item>
        
        <!-- 联系信息 -->
        <el-divider content-position="left">联系信息</el-divider>
        
        <el-form-item label="联系人" prop="contactName">
          <el-input
            v-model="orderForm.contactName"
            placeholder="请输入2-20位联系人姓名"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input
            v-model="orderForm.contactPhone"
            placeholder="请输入11位联系电话"
            maxlength="11"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="联系地址" prop="contactAddress">
          <el-input
            v-model="orderForm.contactAddress"
            placeholder="请输入5-120位详细地址"
            maxlength="120"
            show-word-limit
          />
        </el-form-item>
        
        <!-- 订单金额 -->
        <el-divider content-position="left">订单信息</el-divider>
        
        <el-form-item label="订单金额">
          <span class="amount">¥{{ (totalAmount / 100).toFixed(2) }}</span>
        </el-form-item>
        
        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            提交订单
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { chefDetail, chefTimes, createOrder } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
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

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatDetailTime = (timestamp) => {
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
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 18px;
  font-weight: bold;
}

.amount {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}

.time-range-tip {
  color: #909399;
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
  color: #606266;
  white-space: nowrap;
}

.range-tip {
  color: #909399;
  font-size: 12px;
}
</style>
