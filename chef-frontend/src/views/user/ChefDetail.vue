<template>
  <div class="chef-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
          <span>厨师详情</span>
        </div>
      </template>
      
      <div class="chef-content" v-if="chef">
        <!-- 基本信息 -->
        <div class="chef-basic">
          <el-avatar :size="120" :src="chef.avatar" />
          <div class="chef-info">
            <h2>{{ chef.displayName }}</h2>
            <div class="rating">
              <el-rate v-model="chef.score" disabled show-score />
              <span class="review-count">({{ chef.reviews?.length || 0 }}条评价)</span>
            </div>
            <div class="info-tags">
              <el-tag v-for="(item, index) in chef.cuisineType" :key="index" style="margin-right: 10px">
                {{ item }}
              </el-tag>
            </div>
            <div class="info-items">
              <div class="info-item">
                <el-icon><Location /></el-icon>
                <span>{{ chef.serviceArea }}</span>
              </div>
              <div class="info-item">
                <el-icon><Money /></el-icon>
                <span>¥{{ (chef.price / 100).toFixed(0) }}/小时</span>
              </div>
              <div class="info-item">
                <el-icon><User /></el-icon>
                <span>{{ chef.minPeople }}-{{ chef.maxPeople }}人</span>
              </div>
              <div class="info-item">
                <el-icon><Clock /></el-icon>
                <span>{{ chef.workYears }}年经验</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 服务描述 -->
        <el-divider content-position="left">服务描述</el-divider>
        <p class="service-desc">{{ chef.serviceDesc || '暂无描述' }}</p>
        
        <!-- 证件资质 -->
        <el-divider content-position="left">证件资质</el-divider>
        <div class="certificates">
          <div class="cert-group" v-if="chef.healthCertImgs && chef.healthCertImgs.length">
            <h4>健康证</h4>
            <el-image
              v-for="(img, index) in chef.healthCertImgs"
              :key="index"
              :src="img"
              @click="openImagePreview(img)"
              style="width: 150px; height: 150px; margin-right: 10px"
              fit="cover"
              class="clickable-image"
            />
          </div>
          <div class="cert-group" v-if="chef.chefCertImgs && chef.chefCertImgs.length">
            <h4>厨师证</h4>
            <el-image
              v-for="(img, index) in chef.chefCertImgs"
              :key="index"
              :src="img"
              @click="openImagePreview(img)"
              style="width: 150px; height: 150px; margin-right: 10px"
              fit="cover"
              class="clickable-image"
            />
          </div>
        </div>
        
        <!-- 可预约时间段 -->
        <el-divider content-position="left">可预约时间段</el-divider>
        <div class="time-slots">
          <el-empty v-if="!chef.availableTimes || chef.availableTimes.length === 0" description="暂无可预约时间" />
          <el-tag
            v-for="time in chef.availableTimes"
            :key="time.id"
            :type="time.status === 1 ? 'success' : 'info'"
            size="large"
            style="margin: 5px"
            @click="handleSelectTime(time)"
            :class="{ 'time-slot-clickable': time.status === 1 }"
          >
            {{ formatTime(time.startTime) }} - {{ formatTime(time.endTime) }}
          </el-tag>
        </div>
        
        <!-- 评价列表 -->
        <el-divider content-position="left">用户评价</el-divider>
        <div class="reviews">
          <el-empty v-if="!chef.reviews || chef.reviews.length === 0" description="暂无评价" />
          <el-card
            v-for="review in chef.reviews"
            :key="review.id"
            class="review-card"
            shadow="never"
          >
            <div class="review-header">
              <span class="reviewer-name">{{ review.userName }}</span>
              <el-rate v-model="review.score" disabled />
            </div>
            <p class="review-content">{{ review.content }}</p>
            <span class="review-time">{{ formatTime(review.createTime) }}</span>
          </el-card>
        </div>
        
        <!-- 预约按钮 -->
        <div class="action-bar">
          <el-button type="primary" size="large" @click="handleBook">
            立即预约
          </el-button>
        </div>
      </div>
    </el-card>

    <el-dialog
      v-model="imagePreviewVisible"
      width="720px"
      append-to-body
      class="image-preview-dialog"
      destroy-on-close
    >
      <div class="image-preview-wrap">
        <img v-if="previewImageUrl" :src="previewImageUrl" alt="证件预览" class="preview-image" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { chefDetail, chefTimes, chefReviews } from '@/api/user'
import { Location, Money, User, Clock, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const chef = ref(null)
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')

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

const loadData = async () => {
  loading.value = true
  try {
    const chefUserId = parseInt(route.params.id)
    const [detailRes, timeRes, reviewRes] = await Promise.all([
      chefDetail({ chefUserId }),
      chefTimes({ chefUserId, page: 1, size: 50 }),
      chefReviews({ chefUserId, page: 1, size: 50 })
    ])
    chef.value = {
      ...detailRes.data,
      availableTimes: timeRes.data || [],
      reviews: (reviewRes.data || []).map(review => ({
        ...review,
        score: Number(review.score || 0)
      }))
    }
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const handleSelectTime = (time) => {
  if (time.status !== 1) {
    ElMessage.warning('该时间段不可预约')
    return
  }
  router.push(`/user/create-order/${chef.value.chefUserId}?timeId=${time.id}`)
}

const handleBook = () => {
  router.push(`/user/create-order/${chef.value.chefUserId}`)
}

const openImagePreview = (img) => {
  if (!img) return
  previewImageUrl.value = img
  imagePreviewVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.chef-detail {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 18px;
  font-weight: bold;
}

.chef-basic {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

.chef-info {
  flex: 1;
}

.chef-info h2 {
  margin: 0 0 10px 0;
}

.rating {
  margin-bottom: 15px;
}

.review-count {
  margin-left: 10px;
  color: #999;
}

.info-tags {
  margin-bottom: 15px;
}

.info-items {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
}

.service-desc {
  line-height: 1.8;
  color: #666;
}

.certificates {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cert-group h4 {
  margin: 0 0 10px 0;
}

.clickable-image {
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
}

.time-slots {
  display: flex;
  flex-wrap: wrap;
}

.time-slot-clickable {
  cursor: pointer;
}

.time-slot-clickable:hover {
  opacity: 0.8;
}

.reviews {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.review-card {
  border: 1px solid #eee;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.reviewer-name {
  font-weight: bold;
}

.review-content {
  margin: 10px 0;
  line-height: 1.6;
}

.review-time {
  color: #999;
  font-size: 12px;
}

.action-bar {
  margin-top: 30px;
  text-align: center;
}

.image-preview-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 320px;
}

.preview-image {
  max-width: 100%;
  max-height: 70vh;
  border-radius: 12px;
  object-fit: contain;
}
</style>
