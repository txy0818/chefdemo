<template>
  <div class="chef-list">
    <section class="section-heading page-heading">
      <div>
        <span class="hero-kicker">厨师资料</span>
        <h2>厨师资料列表</h2>
        <p>查看审核状态、服务能力和证件资料，快速定位需要重点复核的厨师。</p>
      </div>
      <div class="metric-pill">共 {{ total }} 位</div>
    </section>

    <el-card class="panel-card glass-panel" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <strong>筛选厨师</strong>
            <p>按审核状态、用户 ID 或用户名快速收窄范围。</p>
          </div>
        </div>
      </template>
      
      <!-- 查询条件 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="审核状态">
          <el-select v-model="queryForm.auditStatus" placeholder="请选择审核状态" clearable style="width: 200px">
            <el-option
              v-for="item in auditStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="请输入用户名关键词" clearable />
        </el-form-item>
        
        <el-form-item label="用户ID">
          <el-input v-model="queryForm.userId" placeholder="请输入目标用户 ID" clearable />
        </el-form-item>
        
        <el-form-item class="query-actions">
          <el-button class="query-btn query-btn-primary" type="primary" @click="handleQuery">查询</el-button>
          <el-button class="query-btn query-btn-secondary" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 表格 -->
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="displayName" label="昵称" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="cuisineTypeDesc" label="擅长菜系" width="150">
          <template #default="{ row }">
            <el-tag v-for="(item, index) in row.cuisineTypeDesc" :key="index" size="small" style="margin-right: 5px">
              {{ item }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="serviceArea" label="服务区域" width="120" />
        <el-table-column prop="price" label="价格(元/小时)" width="120">
          <template #default="{ row }">
            {{ row.priceDesc }}
          </template>
        </el-table-column>
        <el-table-column prop="auditStatusDesc" label="审核状态" width="100" />
        <el-table-column label="操作" fixed="right" width="232" align="center" header-align="center">
          <template #default="{ row }">
            <div class="action-stack">
              <el-button class="action-btn" type="primary" size="small" @click="handleViewDetail(row)">
                查看详情
              </el-button>
              <el-button class="action-btn" type="warning" size="small" @click="handleSendMessage(row)">
                发消息
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handleQuery"
        class="app-pagination"
      />
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="厨师详情" width="800px">
      <el-descriptions :column="2" border v-if="currentChef">
        <el-descriptions-item label="用户ID">{{ currentChef.userId }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ currentChef.displayName }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ currentChef.realName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentChef.phone }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ currentChef.age }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ currentChef.genderDesc }}</el-descriptions-item>
        <el-descriptions-item label="从业年限">{{ currentChef.workYears }}年</el-descriptions-item>
        <el-descriptions-item label="审核状态">{{ currentChef.auditStatusDesc }}</el-descriptions-item>
        <el-descriptions-item label="擅长菜系" :span="2">
          <el-tag v-for="(item, index) in currentChef.cuisineTypeDesc" :key="index" style="margin-right: 5px">
            {{ item }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="服务区域" :span="2">
          <div class="multiline-text">{{ currentChef.serviceArea }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="服务描述" :span="2">
          <div class="multiline-text">{{ currentChef.serviceDesc }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="价格">{{ currentChef.priceDesc }}元/小时</el-descriptions-item>
        <el-descriptions-item label="服务人数">{{ currentChef.minPeople }}-{{ currentChef.maxPeople }}人</el-descriptions-item>
        <el-descriptions-item label="身份证照片" :span="2">
          <el-image
            v-for="(img, index) in currentChef.idCardImgs"
            :key="index"
            :src="img"
            @click="openImagePreview(img)"
            style="width: 100px; height: 100px; margin-right: 10px"
            fit="cover"
            class="clickable-image"
          />
        </el-descriptions-item>
        <el-descriptions-item label="健康证照片" :span="2">
          <el-image
            v-for="(img, index) in currentChef.healthCertImgs"
            :key="index"
            :src="img"
            @click="openImagePreview(img)"
            style="width: 100px; height: 100px; margin-right: 10px"
            fit="cover"
            class="clickable-image"
          />
        </el-descriptions-item>
        <el-descriptions-item label="厨师证照片" :span="2">
          <el-image
            v-for="(img, index) in currentChef.chefCertImgs"
            :key="index"
            :src="img"
            @click="openImagePreview(img)"
            style="width: 100px; height: 100px; margin-right: 10px"
            fit="cover"
            class="clickable-image"
          />
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

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

    <el-dialog v-model="messageVisible" title="发送厨师提醒" width="560px">
      <el-form ref="messageFormRef" :model="messageForm" :rules="messageRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="messageForm.title" maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="messageForm.content"
            type="textarea"
            :rows="5"
            maxlength="300"
            show-word-limit
            placeholder="例如：您的健康证将在近期过期，请尽快更新资料，否则系统将冻结账号。"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="messageVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSendMessage" :loading="sendingMessage">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { queryChefList, sendChefMessage } from '@/api/admin'
import { getAuditStatusOptions } from '@/api/constant'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentChef = ref(null)
const auditStatusOptions = ref([])
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')
const messageVisible = ref(false)
const sendingMessage = ref(false)
const messageFormRef = ref(null)

const queryForm = reactive({
  auditStatus: null,
  username: '',
  userId: '',
  page: 1,
  size: 10
})

const messageForm = reactive({
  chefUserId: null,
  title: '证件更新提醒',
  content: '您的证件即将过期，请尽快更新厨师资料，否则系统可能直接冻结账号。'
})

const messageRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const buildQueryParams = () => {
  const params = {
    page: queryForm.page,
    size: queryForm.size
  }
  if (queryForm.auditStatus != null) {
    params.auditStatus = queryForm.auditStatus
  }
  if (queryForm.username.trim()) {
    params.username = queryForm.username.trim()
  }
  if (queryForm.userId !== '' && queryForm.userId != null) {
    params.userId = Number(queryForm.userId)
  }
  return params
}

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await queryChefList(buildQueryParams())
    tableData.value = res.data
    total.value = res.total
  } catch (error) {
    console.error('查询失败:', error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.auditStatus = null
  queryForm.username = ''
  queryForm.userId = ''
  queryForm.page = 1
  handleQuery()
}

const handleViewDetail = (row) => {
  currentChef.value = row
  detailVisible.value = true
}

const handleSendMessage = row => {
  messageForm.chefUserId = row.userId
  messageForm.title = '证件更新提醒'
  messageForm.content = '您的证件即将过期，请尽快更新厨师资料，否则系统可能直接冻结账号。'
  messageVisible.value = true
}

const confirmSendMessage = async () => {
  if (!messageFormRef.value) return
  const valid = await messageFormRef.value.validate().catch(() => false)
  if (!valid) return
  sendingMessage.value = true
  try {
    await sendChefMessage({
      chefUserId: messageForm.chefUserId,
      title: messageForm.title.trim(),
      content: messageForm.content.trim()
    })
    ElMessage.success('发送成功')
    messageVisible.value = false
  } catch (error) {
    console.error('发送提醒失败:', error)
  } finally {
    sendingMessage.value = false
  }
}

const openImagePreview = (img) => {
  if (!img) return
  previewImageUrl.value = img
  imagePreviewVisible.value = true
}

onMounted(() => {
  getAuditStatusOptions()
    .then(res => {
      auditStatusOptions.value = Array.isArray(res.data) ? res.data : []
    })
    .catch(error => {
      console.error('加载审核状态枚举失败:', error)
    })
  handleQuery()
})
</script>

<style scoped>
.chef-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.card-header strong {
  color: #24375a;
  font-size: 1.08rem;
}

.card-header p {
  margin: 8px 0 0;
  color: #66758d;
  font-size: 13px;
}

.panel-card {
  border: none;
}

.query-form {
  margin-bottom: 20px;
  padding: 20px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid rgba(59, 130, 246, 0.08);
}

.clickable-image {
  cursor: pointer;
  border-radius: 10px;
  overflow: hidden;
}

.chef-list :deep(.el-form-item) {
  margin-bottom: 12px;
}

.query-actions :deep(.el-form-item__content) {
  display: flex;
  align-items: center;
  gap: 10px;
}

.query-btn {
  min-width: 92px;
  min-height: 38px;
  border-radius: 12px;
  font-weight: 600;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.query-btn:hover {
  transform: translateY(-1px);
}

.query-btn-primary {
  box-shadow: 0 10px 18px rgba(59, 130, 246, 0.18);
}

.query-btn-secondary {
  border-color: rgba(148, 163, 184, 0.35);
  color: #475569;
}

.action-stack {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
}

.action-btn {
  min-width: 132px;
  min-height: 32px;
  margin-left: 0;
  border-radius: 10px;
  font-weight: 600;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 16px rgba(15, 23, 42, 0.08);
}

.multiline-text {
  white-space: pre-wrap;
  word-break: break-word;
}

.chef-list :deep(.el-pagination) {
  margin-top: 24px;
  justify-content: flex-end;
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
