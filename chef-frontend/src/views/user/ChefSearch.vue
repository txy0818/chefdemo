<template>
  <div class="chef-search">
    <!-- 搜索筛选区 -->
    <el-card class="search-card">
      <el-form
        :model="searchForm"
        class="search-form"
        label-position="left"
        label-width="150px"
      >
        <el-form-item label="关键词" class="filter-item filter-keyword">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索厨师昵称或菜系"
            clearable
            class="full-input"
          />
        </el-form-item>
        
        <el-form-item label="服务区域" class="filter-item filter-area">
          <el-input
            v-model="searchForm.serviceArea"
            placeholder="输入服务区域"
            clearable
            class="full-input"
          />
        </el-form-item>
        
        <el-form-item label="价格范围(元/小时)" class="filter-item filter-price">
          <div class="price-range-wrap">
            <el-input-number
              v-model="searchForm.minPrice"
              :min="0"
              placeholder="最低"
              class="price-input"
            />
            <span class="price-separator">-</span>
            <el-input-number
              v-model="searchForm.maxPrice"
              :min="0"
              placeholder="最高"
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
              placeholder="最低评分"
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
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 厨师卡片列表 -->
    <div class="chef-list" v-loading="loading">
      <el-empty v-if="chefList.length === 0 && !loading" description="暂无厨师" />
      
      <el-row :gutter="20">
        <el-col
          v-for="chef in chefList"
          :key="chef.chefUserId"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card class="chef-card" shadow="hover" @click="goToDetail(chef.chefUserId)">
            <div class="chef-avatar">
              <el-avatar :size="80" :src="chef.avatar" />
            </div>
            
            <div class="chef-info">
              <h3>{{ chef.displayName }}</h3>
              
              <div class="info-item">
                <el-icon><Location /></el-icon>
                <span>{{ chef.serviceArea }}</span>
              </div>
              
              <div class="info-item">
                <el-icon><Food /></el-icon>
                <span>{{ chef.cuisineType }}</span>
              </div>
              
              <div class="info-item">
                <el-icon><Money /></el-icon>
                <span>¥{{ (chef.price / 100).toFixed(0) }}/小时</span>
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

const router = useRouter()
const loading = ref(false)
const chefList = ref([])

const searchForm = reactive({
  keyword: '',
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
  handleSearch()
})
</script>

<style scoped>
.chef-search {
  max-width: 1400px;
  margin: 0 auto;
}

.search-card {
  margin-bottom: 20px;
  border-radius: 22px;
  box-shadow: 0 16px 36px rgba(23, 43, 77, 0.08);
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
  cursor: pointer;
  transition: transform 0.3s;
}

.chef-card:hover {
  transform: translateY(-5px);
}

.chef-avatar {
  text-align: center;
  margin-bottom: 15px;
}

.chef-info h3 {
  text-align: center;
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #333;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  color: #666;
  font-size: 14px;
}

.info-item .el-icon {
  margin-right: 8px;
  color: #409EFF;
}

.rating {
  margin-top: 15px;
  text-align: center;
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
  color: #475467;
  line-height: 1.4;
  height: auto;
  display: flex;
  align-items: center;
  justify-content: flex-end;
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
}
</style>
