<template>
  <div class="chef-search">
    <section class="section-heading search-heading">
      <div>
        <span class="hero-kicker">用户端精选</span>
        <h2>挑选今天的私厨人选</h2>
        <p>先按服务场景和预算缩小范围，再从评分与菜系里找到更贴近你口味的一位。</p>
      </div>
      <div class="search-summary">
        <div class="metric-pill">当前结果 {{ chefList.length }} 位</div>
        <span class="summary-note">推荐先看评分，再看擅长菜系与服务区域</span>
      </div>
    </section>

    <el-card class="search-card glass-panel" shadow="never">
      <div class="form-intro">
        <div>
          <h3>筛选条件</h3>
          <p>先缩小范围，再选择更贴近口味与预算的厨师。</p>
        </div>
      </div>
      <el-form
        :model="searchForm"
        class="search-form"
        label-position="left"
        label-width="150px"
      >
        <el-form-item label="关键词" class="filter-item filter-keyword">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入厨师昵称"
            clearable
            class="full-input"
          />
        </el-form-item>

        <el-form-item label="擅长菜系" class="filter-item filter-cuisine">
          <el-select
            v-model="searchForm.cuisineTypeList"
            multiple
            collapse-tags
            collapse-tags-tooltip
            clearable
            placeholder="请选择擅长菜系"
            class="full-input"
          >
            <el-option
              v-for="item in cuisineTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="服务区域" class="filter-item filter-area">
          <el-input
            v-model="searchForm.serviceArea"
            placeholder="请输入服务区域，如朝阳区、浦东新区"
            clearable
            class="full-input"
          />
        </el-form-item>
        
        <el-form-item label="价格范围(元/小时)" class="filter-item filter-price">
          <div class="price-range-wrap">
            <el-input-number
              v-model="searchForm.minPrice"
              :min="0"
              placeholder="最低价格"
              class="price-input"
            />
            <span class="price-separator">-</span>
            <el-input-number
              v-model="searchForm.maxPrice"
              :min="0"
              placeholder="最高价格"
              class="price-input"
            />
            <span class="unit-text">元</span>
          </div>
        </el-form-item>
        
        <el-form-item label="评分(1-5分)" class="filter-item filter-score">
          <div class="score-input-wrap">
            <el-input-number
              v-model="searchForm.minScore"
              :min="1"
              :max="5"
              :precision="1"
              placeholder="请输入最低评分"
              class="number-input"
            />
            <span class="unit-text">分</span>
          </div>
        </el-form-item>
        
        <el-form-item label="排序" class="filter-item filter-sort">
          <el-select v-model="searchForm.sortType" class="full-input">
            <el-option label="评分降序" value="scoreDesc" />
            <el-option label="价格升序" value="priceAsc" />
            <el-option label="价格降序" value="priceDesc" />
          </el-select>
        </el-form-item>
        
        <el-form-item class="filter-item filter-actions">
          <el-button class="primary-action" type="primary" @click="handleSearch">开始搜索</el-button>
          <el-button class="secondary-action" @click="handleReset">恢复默认</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <div class="chef-list" v-loading="loading">
      <el-empty v-if="chefList.length === 0 && !loading" description="暂时没有匹配的厨师，换个条件试试" />
      
      <el-row :gutter="20">
        <el-col
          v-for="chef in chefList"
          :key="chef.chefUserId"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card class="chef-card" shadow="never" @click="goToDetail(chef.chefUserId)">
            <div class="chef-card-top">
              <div class="chef-avatar">
                <el-avatar :size="84" :src="chef.avatar" />
              </div>
              <el-tag class="service-tag" effect="dark" round>
                {{ chef.cuisineTypeDesc || '家常定制' }}
              </el-tag>
            </div>
            
            <div class="chef-info">
              <div class="chef-name-row">
                <h3>{{ chef.displayName }}</h3>
                <span class="chef-price-chip">{{ chef.priceDesc }}元/小时</span>
              </div>
              <p class="chef-summary">适合家庭聚餐、轻宴请和定制家宴场景，支持按区域预约上门。</p>
              
              <div class="info-item">
                <el-icon><Location /></el-icon>
                <span>{{ chef.serviceArea }}</span>
              </div>
              
              <div class="info-item">
                <el-icon><Food /></el-icon>
                <span>{{ chef.cuisineTypeDesc }}</span>
              </div>
              
              <div class="info-item">
                <el-icon><Money /></el-icon>
                <span class="price-text">参考价格 {{ chef.priceDesc }} 元/小时</span>
              </div>
              
              <div class="rating">
                <el-rate
                  v-model="chef.score"
                  disabled
                  show-score
                  text-color="#ff9900"
                />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { searchChef } from '@/api/user'
import { ElMessage } from 'element-plus'
import { Location, Food, Money } from '@element-plus/icons-vue'
import { getCuisineTypeOptions } from '@/api/constant'

const router = useRouter()
const loading = ref(false)
const chefList = ref([])
const cuisineTypeOptions = ref([])

const searchForm = reactive({
  keyword: '',
  cuisineTypeList: [],
  serviceArea: '',
  minPrice: null,
  maxPrice: null,
  minScore: null,
  sortType: 'scoreDesc'
})

const buildSearchParams = () => {
  const params = {
    page: 1,
    size: 20,
    sortType: searchForm.sortType
  }
  if (searchForm.keyword.trim()) {
    params.keyword = searchForm.keyword.trim()
  }
  if (Array.isArray(searchForm.cuisineTypeList) && searchForm.cuisineTypeList.length > 0) {
    params.cuisineTypeList = searchForm.cuisineTypeList
  }
  if (searchForm.serviceArea.trim()) {
    params.serviceArea = searchForm.serviceArea.trim()
  }
  if (searchForm.minPrice != null) {
    params.minPrice = searchForm.minPrice * 100
  }
  if (searchForm.maxPrice != null) {
    params.maxPrice = searchForm.maxPrice * 100
  }
  if (searchForm.minScore != null) {
    params.minScore = searchForm.minScore
  }
  return params
}

const handleSearch = async () => {
  if (searchForm.minPrice != null && Number(searchForm.minPrice) < 0) {
    ElMessage.warning('最低价格不能小于0元/小时')
    return
  }
  if (searchForm.maxPrice != null && Number(searchForm.maxPrice) < 0) {
    ElMessage.warning('最高价格不能小于0元/小时')
    return
  }
  if (searchForm.minPrice != null && searchForm.maxPrice != null && Number(searchForm.minPrice) > Number(searchForm.maxPrice)) {
    ElMessage.warning('最低价格不能大于最高价格，单位为元/小时')
    return
  }
  if (searchForm.minScore != null && (Number(searchForm.minScore) < 1 || Number(searchForm.minScore) > 5)) {
    ElMessage.warning('评分范围为1到5分')
    return
  }
  loading.value = true
  try {
    const res = await searchChef(buildSearchParams())
    chefList.value = res.data || []
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.cuisineTypeList = []
  searchForm.serviceArea = ''
  searchForm.minPrice = null
  searchForm.maxPrice = null
  searchForm.minScore = null
  searchForm.sortType = 'scoreDesc'
  handleSearch()
}

const goToDetail = (chefId) => {
  router.push(`/user/chef-detail/${chefId}`)
}

onMounted(() => {
  getCuisineTypeOptions()
    .then(res => {
      cuisineTypeOptions.value = Array.isArray(res.data) ? res.data : []
    })
    .catch(error => {
      console.error('加载菜系枚举失败:', error)
    })
  handleSearch()
})
</script>

<style scoped>
.chef-search {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 18px;
}

.search-heading {
  margin-bottom: 2px;
}

.search-summary {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.summary-note {
  color: #8c6a60;
  font-size: 13px;
}

.search-card {
  border: none;
  border-radius: var(--radius-card);
}

.form-intro {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.form-intro h3 {
  margin: 0;
  color: #3f1111;
  font-size: 1.08rem;
}

.form-intro p {
  margin: 8px 0 0;
  color: #7c5b5b;
}

.search-form {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  row-gap: 20px;
  column-gap: 20px;
  align-items: center;
}

.filter-item {
  margin-bottom: 0;
  align-self: stretch;
}

.filter-keyword {
  grid-column: 1 / span 6;
}

.filter-area {
  grid-column: 1 / span 6;
}

.filter-cuisine {
  grid-column: 7 / span 6;
}

.filter-price {
  grid-column: 1 / span 12;
}

.filter-score {
  grid-column: 1 / span 4;
}

.filter-sort {
  grid-column: 5 / span 3;
}

.filter-actions {
  grid-column: 8 / span 5;
}

.full-input {
  width: 100%;
}

.chef-list {
  min-height: 400px;
}

.chef-card {
  margin-bottom: 20px;
  padding: 6px;
  cursor: pointer;
  border: 1px solid rgba(220, 38, 38, 0.08);
  border-radius: 24px;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(255, 249, 245, 0.96));
  box-shadow: 0 18px 36px rgba(127, 29, 29, 0.08);
  transition: transform 220ms ease, box-shadow 220ms ease, border-color 220ms ease;
}

.chef-card:hover {
  transform: translateY(-6px);
  border-color: rgba(220, 38, 38, 0.18);
  box-shadow: 0 24px 48px rgba(127, 29, 29, 0.12);
}

.chef-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}

.chef-avatar {
  display: inline-flex;
  padding: 4px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(248, 113, 113, 0.9), rgba(161, 98, 7, 0.75));
  box-shadow: 0 16px 28px rgba(248, 113, 113, 0.22);
}

.service-tag {
  border: none;
  background: rgba(161, 98, 7, 0.1);
  color: #9a670d;
}

.chef-info {
  text-align: left;
}

.chef-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.chef-info h3 {
  margin: 0;
  font-size: 1.18rem;
  color: #3f1111;
}

.chef-price-chip {
  flex-shrink: 0;
  min-height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(255, 237, 213, 0.92);
  color: #9a3412;
  font-size: 13px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}

.chef-summary {
  margin: 0 0 18px;
  color: #7c5b5b;
  line-height: 1.7;
  font-size: 0.92rem;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  color: #6c4a4a;
  font-size: 14px;
}

.info-item .el-icon {
  color: #dc2626;
}

.price-text {
  font-weight: 700;
  color: #9a3412;
}

.rating {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid rgba(220, 38, 38, 0.08);
}

.review-count {
  margin-left: 10px;
  color: #999;
  font-size: 12px;
}

.price-range-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  max-width: 620px;
  flex-wrap: wrap;
}

.price-input {
  flex: 0 0 220px;
  min-width: 220px;
}

.price-separator {
  color: #909399;
  font-weight: 500;
  font-size: 16px;
  flex-shrink: 0;
}

.score-input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: flex-start;
  width: 100%;
}

.filter-score .number-input {
  width: 220px;
  min-width: 220px;
}

.unit-text {
  color: #606266;
  white-space: nowrap;
}

.number-input {
  width: 100%;
  min-width: 0;
}

.search-form :deep(.el-form-item) {
  display: flex;
  align-items: center;
}

.search-form :deep(.el-form-item__label) {
  padding-bottom: 0;
  font-weight: 600;
  color: #6b3f3f;
  line-height: 1.4;
  height: auto;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.search-form :deep(.el-input__wrapper),
.search-form :deep(.el-textarea__inner),
.search-form :deep(.el-select__wrapper),
.search-form :deep(.el-input-number) {
  border-radius: 16px;
  box-shadow: none;
}

.search-form :deep(.el-input__wrapper),
.search-form :deep(.el-select__wrapper),
.search-form :deep(.el-textarea__inner) {
  min-height: 46px;
  background: rgba(255, 255, 255, 0.92);
}

.search-form :deep(.el-input-number) {
  width: 100%;
}

.search-form :deep(.el-input-number .el-input__inner) {
  text-align: center;
}

.search-form :deep(.el-form-item__content) {
  min-width: 0;
  min-height: 40px;
  display: flex;
  align-items: center;
}

.filter-actions :deep(.el-form-item__content) {
  margin-left: 0 !important;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-actions :deep(.el-button) {
  min-width: 120px;
  min-height: 46px;
  border-radius: 14px;
}

.primary-action {
  border: none;
  background: linear-gradient(135deg, #dc2626, #ea580c);
  box-shadow: 0 16px 30px rgba(220, 38, 38, 0.22);
}

.secondary-action {
  border-color: rgba(220, 38, 38, 0.12);
  color: #7f1d1d;
}

.rating :deep(.el-rate) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
  max-width: 100%;
}

.rating :deep(.el-rate__text) {
  margin-left: 0;
  line-height: 1.4;
}

@media (max-width: 1280px) {
  .filter-keyword,
  .filter-area,
  .filter-price {
    grid-column: span 6;
  }

  .filter-score,
  .filter-sort,
  .filter-actions {
    grid-column: span 4;
  }
}

@media (max-width: 900px) {
  .search-summary {
    align-items: flex-start;
  }

  .chef-name-row {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (max-width: 900px) {
  .search-form {
    grid-template-columns: 1fr;
  }

  .filter-keyword,
  .filter-area,
  .filter-price,
  .filter-score,
  .filter-sort,
  .filter-actions {
    grid-column: span 1;
  }

  .price-range-wrap,
  .score-input-wrap {
    flex-wrap: wrap;
  }

  .search-form :deep(.el-form-item__label) {
    justify-content: flex-start;
  }

  .search-form :deep(.el-form-item) {
    flex-wrap: wrap;
  }

  .search-form :deep(.el-form-item__label) {
    width: 100% !important;
    margin-bottom: 8px;
  }

  .search-form :deep(.el-form-item__content) {
    margin-left: 0 !important;
    width: 100%;
  }

  .chef-card-top {
    align-items: center;
  }
}
</style>
