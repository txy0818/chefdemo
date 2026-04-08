<template>
  <div class="chef-audit">
    <section class="section-heading page-heading">
      <div>
        <span class="hero-kicker">审核工作台</span>
        <h2>厨师审核列表</h2>
        <p>集中处理待审核厨师资料，重点核验证件、服务能力与报价信息。</p>
      </div>
      <div class="metric-pill">共 {{ total }} 位</div>
    </section>

    <el-card class="panel-card glass-panel" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <strong>待审核资料</strong>
            <p>查看完整资料后再决定通过或拒绝，拒绝时要给出明确原因。</p>
          </div>
        </div>
      </template>
      
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
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-button type="success" size="small" @click="handleAudit(row, 2)">
              通过
            </el-button>
            <el-button type="danger" size="small" @click="handleAudit(row, 3)">
              拒绝
            </el-button>
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
        <el-descriptions-item label="服务区域" :span="2">{{ currentChef.serviceArea }}</el-descriptions-item>
        <el-descriptions-item label="服务描述" :span="2">{{ currentChef.serviceDesc }}</el-descriptions-item>
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
    
    <!-- 审核对话框 -->
    <el-dialog v-model="auditVisible" :title="auditTitle" width="500px">
      <el-form ref="auditFormRef" :model="auditForm" :rules="auditRules" label-width="80px">
        <el-form-item label="审核结果">
          <el-tag :type="auditForm.auditStatus === 2 ? 'success' : 'danger'">
            {{ getAuditStatusLabel(auditForm.auditStatus) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="原因" prop="reason" v-if="auditForm.auditStatus === 3">
          <el-input
            v-model="auditForm.reason"
            type="textarea"
            :rows="4"
            maxlength="100"
            show-word-limit
            placeholder="请输入拒绝原因，最多100字"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAudit" :loading="auditing">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { queryAuditChef, auditChef } from '@/api/admin'
import { getAuditStatusOptions } from '@/api/constant'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const auditing = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const auditVisible = ref(false)
const auditTitle = ref('')
const currentChef = ref(null)
const auditFormRef = ref(null)
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')
const auditStatusOptions = ref([])

const queryForm = reactive({
  page: 1,
  size: 10
})

const auditForm = reactive({
  chefUserId: null,
  auditStatus: 2,
  reason: ''
})

const auditRules = {
  reason: [
    {
      validator: (_, value, callback) => {
        if (auditForm.auditStatus !== 3) {
          callback()
          return
        }
        if (!value || !value.trim()) {
          callback(new Error('请输入拒绝原因'))
          return
        }
        if (value.trim().length > 100) {
          callback(new Error('拒绝原因最多100字'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await queryAuditChef(queryForm)
    tableData.value = res.data
    total.value = res.total
  } catch (error) {
    console.error('查询失败:', error)
  } finally {
    loading.value = false
  }
}

const handleViewDetail = (row) => {
  currentChef.value = row
  detailVisible.value = true
}

const openImagePreview = (img) => {
  if (!img) return
  previewImageUrl.value = img
  imagePreviewVisible.value = true
}

const handleAudit = (row, status) => {
  currentChef.value = row
  auditForm.chefUserId = row.userId
  auditForm.auditStatus = status
  auditForm.reason = ''
  auditTitle.value = `审核${getAuditStatusLabel(status)}`
  auditVisible.value = true
}

const getAuditStatusLabel = (status) => {
  const option = auditStatusOptions.value.find(item => item.value === status)
  return option ? option.label : ''
}

const confirmAudit = async () => {
  if (auditForm.auditStatus === 3) {
    if (!auditFormRef.value) return
    const valid = await auditFormRef.value.validate().catch(() => false)
    if (!valid) return
    auditForm.reason = auditForm.reason.trim()
  }

  auditing.value = true
  try {
    await auditChef(auditForm)
    ElMessage.success('审核成功')
    auditVisible.value = false
    handleQuery()
  } catch (error) {
    console.error('审核失败:', error)
  } finally {
    auditing.value = false
  }
}

onMounted(() => {
  getAuditStatusOptions()
    .then(res => {
      auditStatusOptions.value = Array.isArray(res.data) ? res.data : []
    })
    .catch(error => {
      console.error('加载厨师审核状态枚举失败:', error)
    })
  handleQuery()
})
</script>

<style scoped>
.chef-audit {
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

.clickable-image {
  cursor: pointer;
  border-radius: 10px;
  overflow: hidden;
}

.chef-audit :deep(.el-pagination) {
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
