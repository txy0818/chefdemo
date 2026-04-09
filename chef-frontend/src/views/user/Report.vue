<template>
  <div class="report">
    <section class="section-heading action-heading">
      <div>
        <h2>举报厨师</h2>
        <p>请尽量准确描述问题，我们会根据订单信息和提交内容进行核实处理。</p>
      </div>
      <el-button class="back-button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回订单详情
      </el-button>
    </section>

    <el-card class="report-card glass-panel" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <strong>举报表单</strong>
            <p>举报提交后会进入后台审核，请尽量提供完整且清晰的描述。</p>
          </div>
        </div>
      </template>
      
      <el-alert
        title="请如实填写举报信息，我们会认真核实并处理每一条举报"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px"
      />
      
      <el-form
        ref="reportFormRef"
        :model="reportForm"
        :rules="reportRules"
        label-width="100px"
        v-loading="loading"
      >
        <el-form-item label="订单ID">
          <span>{{ route.params.orderId }}</span>
        </el-form-item>
        
        <el-form-item label="举报类型" prop="reportType">
          <el-select v-model="reportForm.reportType" placeholder="请选择举报类型" style="width: 100%">
            <el-option
              v-for="item in reportTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="举报原因" prop="reason">
          <el-input
            v-model="reportForm.reason"
            type="textarea"
            :rows="8"
            placeholder="请尽量写清楚发生了什么、涉及哪类问题以及你的具体感受"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="danger" @click="handleSubmit" :loading="submitting">
            提交举报
          </el-button>
          <el-button class="ghost-button" @click="goBack">返回订单详情</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createReport, orderDetail } from '@/api/user'
import { getReportTypeLabelMap, getReportTypeOptions } from '@/api/constant'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const reportFormRef = ref(null)
const reportTypeOptions = ref([])
const reportTypeLabelMap = ref({})

const reportForm = reactive({
  orderId: parseInt(route.params.orderId),
  reportType: null,
  reason: ''
})

const reportRules = {
  reportType: [{ required: true, message: '请选择举报类型', trigger: 'change' }],
  reason: [
    { required: true, message: '请输入举报原因', trigger: 'blur' },
    { min: 20, message: '举报原因至少20个字', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (!value || !value.trim()) {
          callback(new Error('请输入举报原因'))
          return
        }
        if (value.trim().length < 20) {
          callback(new Error('举报原因至少20个字'))
          return
        }
        if (value.trim().length > 200) {
          callback(new Error('举报原因最多200字'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const handleSubmit = async () => {
  if (!reportFormRef.value) return
  
  await reportFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    ElMessageBox.confirm('确定要提交举报吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      submitting.value = true
      try {
        const reportReason = reportForm.reason.trim()
        const reportTypeLabel = reportTypeLabelMap.value[reportForm.reportType] || '其他问题'
        await createReport({
          orderId: reportForm.orderId,
          reason: `【${reportTypeLabel}】${reportReason}`
        })
        ElMessage.success('举报提交成功，我们会尽快处理')
        router.push('/user/order-list')
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        submitting.value = false
      }
    })
  })
}

const loadReportTypeOptions = async () => {
  try {
    const [options, labelMap] = await Promise.all([
      getReportTypeOptions().then(res => (Array.isArray(res.data) ? res.data : [])),
      getReportTypeLabelMap()
    ])
    reportTypeOptions.value = options
    reportTypeLabelMap.value = labelMap
  } catch (error) {
    console.error('加载举报类型失败:', error)
  }
}

const goBack = () => {
  router.back()
}

const checkOrderStatus = async () => {
  loading.value = true
  try {
    const res = await orderDetail({
      orderId: parseInt(route.params.orderId)
    })
    if (res.data?.status !== 5) {
      ElMessage.warning('仅已完成订单可举报')
      router.replace(`/user/order-detail/${route.params.orderId}`)
    }
  } catch (error) {
    console.error('校验订单状态失败:', error)
    router.replace('/user/order-list')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadReportTypeOptions()
  checkOrderStatus()
})
</script>

<style scoped>
.report {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.report-card {
  border: none;
}

.action-heading {
  align-items: center;
}

.action-heading h2 {
  margin: 0 0 8px;
}

.action-heading p {
  margin: 0;
  color: #8a5c52;
}

.back-button {
  min-width: 148px;
  border-radius: 999px;
  border-color: rgba(194, 110, 44, 0.2);
  color: #8f4e20;
  background: rgba(255, 248, 243, 0.92);
  box-shadow: 0 12px 24px rgba(181, 103, 43, 0.08);
}

.back-button:hover {
  border-color: rgba(194, 110, 44, 0.34);
  color: #7f4116;
  background: #fff4ea;
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.card-header strong {
  color: #3a2416;
  font-size: 1.05rem;
}

.card-header p {
  margin: 8px 0 0;
  color: #9b7d68;
  font-size: 13px;
}

.ghost-button {
  border-radius: 999px;
}

@media (max-width: 768px) {
  .action-heading {
    align-items: flex-start;
  }

  .back-button {
    width: 100%;
  }
}
</style>
