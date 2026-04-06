<template>
  <div class="review">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
          <span>评价厨师</span>
        </div>
      </template>
      
      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewRules"
        label-width="100px"
        v-loading="loading"
      >
        <el-form-item label="订单ID">
          <span>{{ route.params.orderId }}</span>
        </el-form-item>
        
        <el-form-item label="评分(1-5分)" prop="score">
          <el-rate
            v-model="reviewForm.score"
            :texts="['非常差', '差', '一般', '好', '非常好']"
            show-text
          />
        </el-form-item>
        
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="6"
            placeholder="请如实填写对厨师服务的评价，建议说明体验亮点或需要改进的地方"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            提交评价
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
import { createReview, orderDetail } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const reviewFormRef = ref(null)

const reviewForm = reactive({
  orderId: parseInt(route.params.orderId),
  score: 5,
  content: ''
})

const reviewRules = {
  score: [
    { required: true, message: '请选择评分', trigger: 'change' },
    {
      validator: (_, value, callback) => {
        if (value == null || Number(value) < 1 || Number(value) > 5) {
          callback(new Error('评分范围为1到5分'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 10, message: '评价内容至少10个字', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!reviewFormRef.value) return
  
  await reviewFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await createReview({
        orderId: reviewForm.orderId,
        score: reviewForm.score,
        content: reviewForm.content
      })
      ElMessage.success('评价提交成功')
      router.push('/user/order-list')
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitting.value = false
    }
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
      ElMessage.warning('仅已完成订单可评价')
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
.review {
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
