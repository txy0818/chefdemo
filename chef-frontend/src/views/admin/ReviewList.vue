<template>
  <div class="review-list">
    <section class="section-heading page-heading">
      <div>
        <span class="hero-kicker">评论治理</span>
        <h2>评论管理</h2>
        <p>统一审核用户评价，快速通过、驳回或删除异常内容。</p>
      </div>
      <div class="metric-pill">共 {{ total }} 条</div>
    </section>

    <el-card class="panel-card glass-panel" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <strong>筛选评论</strong>
            <p>优先处理待审核内容，必要时直接删除明显异常评论。</p>
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
        
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 表格 -->
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="reservationOrderId" label="订单ID" width="100" />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="chefName" label="厨师" width="120" />
        <el-table-column prop="score" label="评分(1-5分)" width="120">
          <template #default="{ row }">
            <el-rate v-model="row.score" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200" />
        <el-table-column prop="auditStatusDesc" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAuditStatusType(row.auditStatus)">
              {{ row.auditStatusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditReason" label="审核原因" width="150" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button
              v-if="row.auditStatus === 1"
              type="success"
              size="small"
              @click="handleAudit(row, 2)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.auditStatus === 1"
              type="warning"
              size="small"
              @click="handleAudit(row, 3)"
            >
              驳回
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
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
    
    <!-- 审核对话框 -->
    <el-dialog v-model="auditVisible" :title="auditTitle" width="500px">
      <el-form ref="auditFormRef" :model="auditForm" :rules="auditRules" label-width="80px">
        <el-form-item label="审核结果">
          <el-tag :type="auditForm.auditStatus === 2 ? 'success' : 'warning'">
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
            placeholder="请填写驳回原因，便于用户理解审核结果"
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
import { reviewList, auditReview, deleteReview } from '@/api/admin'
import { getReviewAuditStatusOptions } from '@/api/constant'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const auditing = ref(false)
const tableData = ref([])
const total = ref(0)
const auditVisible = ref(false)
const auditTitle = ref('')
const currentRow = ref(null)
const auditFormRef = ref(null)
const auditStatusOptions = ref([])

const queryForm = reactive({
  auditStatus: null,
  page: 1,
  size: 10
})

const buildQueryParams = () => {
  const params = {
    page: queryForm.page,
    size: queryForm.size
  }
  if (queryForm.auditStatus != null) {
    params.auditStatus = queryForm.auditStatus
  }
  return params
}

const auditForm = reactive({
  orderId: null,
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
          callback(new Error('请输入驳回原因'))
          return
        }
        if (value.trim().length > 100) {
          callback(new Error('驳回原因最多100字'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const getAuditStatusType = (status) => {
  const map = {
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return map[status] || 'info'
}

const getAuditStatusLabel = (status) => {
  const option = auditStatusOptions.value.find(item => item.value === status)
  return option ? option.label : ''
}

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await reviewList(buildQueryParams())
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
  queryForm.page = 1
  handleQuery()
}

const handleAudit = (row, status) => {
  currentRow.value = row
  auditForm.orderId = row.reservationOrderId
  auditForm.auditStatus = status
  auditForm.reason = ''
  auditTitle.value = `审核${getAuditStatusLabel(status)}`
  auditVisible.value = true
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
    await auditReview(auditForm)
    ElMessage.success('审核成功')
    auditVisible.value = false
    handleQuery()
  } catch (error) {
    console.error('审核失败:', error)
  } finally {
    auditing.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteReview({ orderId: row.reservationOrderId })
      ElMessage.success('删除成功')
      handleQuery()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

onMounted(() => {
  getReviewAuditStatusOptions()
    .then(res => {
      auditStatusOptions.value = Array.isArray(res.data) ? res.data : []
    })
    .catch(error => {
      console.error('加载评论审核状态枚举失败:', error)
    })
  handleQuery()
})
</script>

<style scoped>
.review-list {
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

.review-list :deep(.el-form-item) {
  margin-bottom: 12px;
}

.review-list :deep(.el-pagination) {
  margin-top: 24px;
  justify-content: flex-end;
}
</style>
