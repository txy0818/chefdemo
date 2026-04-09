<template>
  <div class="chef-detail">
    <section class="section-heading detail-heading">
      <div>
        <h2>厨师详情</h2>
        <p>查看厨师擅长菜系、服务范围、资质证明和可预约时间，再决定是否立即下单。</p>
      </div>
      <el-button class="back-button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </section>

    <div v-loading="loading" class="detail-stack">
      <section v-if="chef" class="chef-overview glass-panel">
        <div class="chef-basic">
          <div class="chef-avatar-shell">
            <el-avatar :size="124" :src="chef.avatar" />
          </div>

          <div class="chef-info">
            <div class="chef-title-row">
              <div>
                <h1>{{ chef.displayName }}</h1>
                <p class="chef-subtitle">适合家庭聚餐、纪念日晚餐和轻量宴请的上门私厨服务。</p>
              </div>
              <div class="price-badge">{{ chef.priceDesc }}元/小时</div>
            </div>

            <div class="rating">
              <el-rate v-model="chef.score" disabled show-score />
              <span class="review-count">{{ chef.reviews?.length || 0 }} 条真实评价</span>
            </div>

            <div class="info-tags">
              <el-tag v-for="(item, index) in cuisineTags" :key="index" class="cuisine-tag" round>
                {{ item }}
              </el-tag>
            </div>

            <div class="info-items">
              <div class="info-item">
                <el-icon><Location /></el-icon>
                <div>
                  <span class="item-label">服务区域</span>
                  <strong>{{ chef.serviceArea || '待补充' }}</strong>
                </div>
              </div>
              <div class="info-item">
                <el-icon><User /></el-icon>
                <div>
                  <span class="item-label">适合人数</span>
                  <strong>{{ chef.minPeople }} - {{ chef.maxPeople }} 人</strong>
                </div>
              </div>
              <div class="info-item">
                <el-icon><Clock /></el-icon>
                <div>
                  <span class="item-label">从业经验</span>
                  <strong>{{ chef.workYears }} 年</strong>
                </div>
              </div>
              <div class="info-item">
                <el-icon><Money /></el-icon>
                <div>
                  <span class="item-label">参考价格</span>
                  <strong>按小时计费</strong>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="overview-actions">
          <el-button class="ghost-action" @click="goBack">继续挑选</el-button>
          <el-button class="primary-action" type="primary" @click="handleBook">立即预约</el-button>
        </div>
      </section>

      <section v-if="chef" class="content-grid">
        <div class="content-main">
          <el-card class="content-card glass-panel" shadow="never">
            <template #header>
              <div class="block-title">
                <span>服务描述</span>
              </div>
            </template>
            <p class="service-desc">{{ chef.serviceDesc || '暂无描述' }}</p>
          </el-card>

          <el-card class="content-card glass-panel" shadow="never">
            <template #header>
              <div class="block-title">
                <span>可预约时间</span>
                <small>点击绿色时间段可直接进入下单</small>
              </div>
            </template>
            <div class="time-slots">
              <el-empty v-if="!chef.availableTimes || chef.availableTimes.length === 0" description="当前暂无可预约时间段" />
              <button
                v-for="time in chef.availableTimes"
                :key="time.id"
                type="button"
                class="time-slot"
                :class="{ 'is-available': time.status === 1, 'is-disabled': time.status !== 1 }"
                @click="handleSelectTime(time)"
              >
                <strong>{{ time.startTimeDesc }}</strong>
                <span>{{ time.endTimeDesc }}</span>
                <div v-if="time.occupiedTimeDescList && time.occupiedTimeDescList.length" class="occupied-time-list">
                  <small
                    v-for="(occupiedTime, index) in time.occupiedTimeDescList"
                    :key="`${time.id}-occupied-${index}`"
                    class="occupied-time-item"
                  >
                    已预约：{{ occupiedTime }}
                  </small>
                </div>
              </button>
            </div>
          </el-card>

          <el-card class="content-card glass-panel" shadow="never">
            <template #header>
              <div class="block-title">
                <span>用户评价</span>
                <small>帮助你判断口味稳定度与服务体验</small>
              </div>
            </template>
            <div class="reviews">
              <el-empty v-if="!chef.reviews || chef.reviews.length === 0" description="暂时还没有评价记录" />
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
                <span class="review-time">{{ review.createTimeDesc || formatDateTime(review.createTime) }}</span>
              </el-card>
            </div>
          </el-card>
        </div>

        <aside class="content-side">
          <el-card class="content-card glass-panel" shadow="never">
            <template #header>
              <div class="block-title">
                <span>资质证明</span>
              </div>
            </template>
            <div class="certificates">
              <div class="cert-group" v-if="chef.healthCertImgs && chef.healthCertImgs.length">
                <h4>健康证</h4>
                <div class="cert-list">
                  <el-image
                    v-for="(img, index) in chef.healthCertImgs"
                    :key="index"
                    :src="img"
                    @click="openImagePreview(img)"
                    fit="cover"
                    class="clickable-image"
                  />
                </div>
              </div>
              <div class="cert-group" v-if="chef.chefCertImgs && chef.chefCertImgs.length">
                <h4>厨师证</h4>
                <div class="cert-list">
                  <el-image
                    v-for="(img, index) in chef.chefCertImgs"
                    :key="index"
                    :src="img"
                    @click="openImagePreview(img)"
                    fit="cover"
                    class="clickable-image"
                  />
                </div>
              </div>
              <el-empty
                v-if="(!chef.healthCertImgs || chef.healthCertImgs.length === 0) && (!chef.chefCertImgs || chef.chefCertImgs.length === 0)"
                description="暂未上传资质图片"
              />
            </div>
          </el-card>
        </aside>
      </section>
    </div>

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
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { chefDetail, chefTimes, chefReviews } from '@/api/user'
import { Location, Money, User, Clock, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '@/utils/datetime'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const chef = ref(null)
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')
const cuisineTags = computed(() => {
  if (!chef.value?.cuisineTypeDesc) return []
  return Array.isArray(chef.value.cuisineTypeDesc)
    ? chef.value.cuisineTypeDesc
    : String(chef.value.cuisineTypeDesc).split(/[、,，/\s]+/).filter(Boolean)
})

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
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-heading {
  margin-bottom: 2px;
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

.chef-overview {
  padding: 28px;
  border-radius: 32px;
}

.chef-basic {
  display: flex;
  gap: 28px;
  align-items: flex-start;
}

.chef-avatar-shell {
  display: inline-flex;
  flex-shrink: 0;
  padding: 6px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(248, 113, 113, 0.9), rgba(161, 98, 7, 0.72));
  box-shadow: 0 20px 36px rgba(220, 38, 38, 0.18);
}

.chef-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.chef-title-row {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: flex-start;
}

.chef-info h1 {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(2rem, 4vw, 3rem);
  font-weight: 400;
  color: var(--color-title);
}

.chef-subtitle {
  margin: 10px 0 0;
  max-width: 42rem;
  color: var(--color-text-soft);
  line-height: 1.75;
}

.price-badge {
  flex-shrink: 0;
  min-height: 60px;
  padding: 14px 18px;
  border-radius: 18px;
  background: linear-gradient(135deg, #fff1eb, #fffbeb);
  color: #9a3412;
  font-size: 1.4rem;
  font-weight: 800;
  box-shadow: inset 0 0 0 1px rgba(220, 38, 38, 0.08);
}

.rating {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.review-count {
  color: #8b6b6b;
}

.info-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.cuisine-tag {
  border-color: rgba(220, 38, 38, 0.1);
  color: #9a3412;
  background: rgba(255, 245, 245, 0.92);
}

.info-items {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(220, 38, 38, 0.08);
}

.info-item .el-icon {
  color: #dc2626;
  font-size: 18px;
}

.item-label {
  display: block;
  margin-bottom: 4px;
  font-size: 12px;
  color: #8b6b6b;
}

.info-item strong {
  color: #3f1111;
}

.overview-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.ghost-action,
.primary-action {
  min-width: 132px;
  min-height: 46px;
  border-radius: 14px;
}

.ghost-action {
  border-color: rgba(220, 38, 38, 0.12);
  color: #7f1d1d;
}

.service-desc {
  margin: 0;
  line-height: 1.9;
  color: #6b4a4a;
  white-space: pre-wrap;
  word-break: break-word;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(320px, 0.9fr);
  gap: 20px;
}

.content-main,
.content-side {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.content-card {
  border: none;
  border-radius: 28px;
}

.block-title {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

.block-title span {
  font-size: 1.05rem;
  font-weight: 700;
  color: #3f1111;
}

.block-title small {
  color: #8b6b6b;
}

.certificates {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cert-group h4 {
  margin: 0 0 12px;
  color: #6b3f3f;
}

.cert-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.clickable-image {
  cursor: pointer;
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid rgba(220, 38, 38, 0.08);
  transition: transform 220ms ease, box-shadow 220ms ease;
}

.clickable-image:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 24px rgba(127, 29, 29, 0.1);
}

.time-slots {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.time-slot {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  min-width: 168px;
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  background: rgba(255, 255, 255, 0.84);
  color: #6b4a4a;
  cursor: pointer;
  transition: transform 220ms ease, box-shadow 220ms ease, border-color 220ms ease;
}

.time-slot strong {
  color: #3f1111;
}

.time-slot.is-available {
  border-color: rgba(21, 128, 61, 0.18);
  background: rgba(240, 253, 244, 0.95);
}

.time-slot.is-available:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 24px rgba(21, 128, 61, 0.12);
}

.time-slot.is-disabled {
  opacity: 0.66;
  cursor: not-allowed;
}

.occupied-time-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 6px;
}

.occupied-time-item {
  color: #9f1239;
  line-height: 1.5;
  white-space: normal;
  word-break: break-word;
}

.reviews {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.review-card {
  border: 1px solid rgba(220, 38, 38, 0.08);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.72);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.reviewer-name {
  font-weight: 700;
  color: #3f1111;
}

.review-content {
  margin: 12px 0;
  line-height: 1.8;
  color: #6b4a4a;
}

.review-time {
  color: #8b6b6b;
  font-size: 12px;
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

@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .chef-overview {
    padding: 22px 18px;
    border-radius: 24px;
  }

  .chef-basic,
  .chef-title-row {
    flex-direction: column;
  }

  .info-items {
    grid-template-columns: 1fr;
  }

  .overview-actions {
    flex-direction: column;
  }

  .ghost-action,
  .primary-action {
    width: 100%;
  }

  .cert-list {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
