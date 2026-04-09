<template>
  <div class="review">
    <section class="section-heading action-heading">
      <div>
        <h2>评价厨师</h2>
        <p>分享本次上门服务的真实体验，帮助平台持续优化服务质量。</p>
      </div>
      <el-button class="back-button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回订单详情
      </el-button>
    </section>

    <el-card class="review-card glass-panel" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <strong>评价表单</strong>
            <p>建议描述服务过程、沟通体验和菜品完成度，内容会在审核后展示。</p>
          </div>
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
          <el-button class="ghost-button" @click="goBack">返回订单详情</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createReview, orderDetail } from '@/api/user'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
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
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.review-card {
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
