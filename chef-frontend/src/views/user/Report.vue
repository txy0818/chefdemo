<template>
  <div class="report">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
          <span>举报厨师</span>
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
            <el-option label="服务态度恶劣" value="服务态度恶劣" />
            <el-option label="资质不符" value="资质不符" />
            <el-option label="食品安全问题" value="食品安全问题" />
            <el-option label="未按约定提供服务" value="未按约定提供服务" />
            <el-option label="其他问题" value="其他问题" />
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
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createReport, orderDetail } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const reportFormRef = ref(null)

const reportForm = reactive({
  orderId: parseInt(route.params.orderId),
  reportType: '',
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
        await createReport({
          orderId: reportForm.orderId,
          reason: `【${reportForm.reportType}】${reportReason}`
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
  checkOrderStatus()
})
</script>

<style scoped>
.report {
  max-width: 700px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 18px;
  font-weight: bold;
}
</style>
