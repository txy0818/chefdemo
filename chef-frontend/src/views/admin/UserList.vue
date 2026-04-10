<template>
  <div class="user-list">
    <section class="section-heading page-heading">
      <div>
        <span class="hero-kicker">用户治理</span>
        <h2>用户列表</h2>
        <p>统一查看账号状态、筛选角色，并处理冻结与恢复操作。</p>
      </div>
      <div class="metric-pill">共 {{ total }} 位</div>
    </section>

    <el-card class="panel-card glass-panel" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <strong>筛选用户</strong>
            <p>优先关注被冻结用户和近期需要处理的异常账号。</p>
          </div>
        </div>
      </template>
      
      <!-- 查询条件 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="角色">
          <el-select v-model="queryForm.role" placeholder="请选择角色" clearable style="width: 200px">
            <el-option
              v-for="item in roleOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 200px">
            <el-option
              v-for="item in statusOptions"
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
        <el-table-column prop="id" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="roleDesc" label="角色" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="statusDesc" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTimeDesc" label="最后登录时间" width="180" />
        <el-table-column label="操作" fixed="right" width="180" align="center" header-align="center">
          <template #default="{ row }">
            <div class="action-stack">
              <el-button
                v-if="row.status === 1"
                type="danger"
                size="small"
                @click="handleUpdateStatus(row, 2)"
              >
                冻结
              </el-button>
              <el-button
                v-else
                type="success"
                size="small"
                @click="handleUpdateStatus(row, 1)"
              >
                解冻
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
    
    <!-- 冻结/解冻对话框 -->
    <el-dialog v-model="statusDialogVisible" :title="statusDialogTitle" width="500px">
      <el-form ref="statusFormRef" :model="statusForm" :rules="statusRules" label-width="80px">
        <el-form-item label="原因" prop="reason">
          <el-input
            v-model="statusForm.reason"
            type="textarea"
            :rows="4"
            maxlength="100"
            show-word-limit
            placeholder="请填写操作原因，便于后续审计和通知用户"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmUpdateStatus" :loading="updating">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { queryUserList, updateUserStatus } from '@/api/admin'
import { getUserRoleOptions, getUserStatusOptions } from '@/api/constant'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const updating = ref(false)
const tableData = ref([])
const total = ref(0)
const statusDialogVisible = ref(false)
const statusDialogTitle = ref('')
const currentRow = ref(null)
const targetStatus = ref(1)
const statusFormRef = ref(null)
const roleOptions = ref([])
const statusOptions = ref([])

const queryForm = reactive({
  role: null,
  status: null,
  username: '',
  userId: '',
  page: 1,
  size: 10
})

const buildQueryParams = () => {
  const params = {
    page: queryForm.page,
    size: queryForm.size
  }
  if (queryForm.role != null) {
    params.role = queryForm.role
  }
  if (queryForm.status != null) {
    params.status = queryForm.status
  }
  if (queryForm.username.trim()) {
    params.username = queryForm.username.trim()
  }
  if (queryForm.userId !== '' && queryForm.userId != null) {
    params.userId = Number(queryForm.userId)
  }
  return params
}

const statusForm = reactive({
  reason: ''
})

const statusRules = {
  reason: [
    { required: true, message: '请输入原因', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (!value || !value.trim()) {
          callback(new Error('请输入原因'))
          return
        }
        if (value.trim().length > 100) {
          callback(new Error('原因最多100字'))
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
    const res = await queryUserList(buildQueryParams())
    tableData.value = res.data
    total.value = res.total
  } catch (error) {
    console.error('查询失败:', error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.role = null
  queryForm.status = null
  queryForm.username = ''
  queryForm.userId = ''
  queryForm.page = 1
  handleQuery()
}

const handleUpdateStatus = (row, status) => {
  currentRow.value = row
  targetStatus.value = status
  statusDialogTitle.value = status === 2 ? '冻结用户' : '解冻用户'
  statusForm.reason = ''
  statusDialogVisible.value = true
}

const confirmUpdateStatus = async () => {
  if (!statusFormRef.value) return
  const valid = await statusFormRef.value.validate().catch(() => false)
  if (!valid) return

  updating.value = true
  try {
    statusForm.reason = statusForm.reason.trim()
    await updateUserStatus({
      userId: currentRow.value.id,
      status: targetStatus.value,
      reason: statusForm.reason
    })
    
    ElMessage.success('操作成功')
    statusDialogVisible.value = false
    handleQuery()
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    updating.value = false
  }
}

onMounted(() => {
  Promise.all([getUserRoleOptions(), getUserStatusOptions()])
    .then(([roleRes, statusRes]) => {
      roleOptions.value = Array.isArray(roleRes.data) ? roleRes.data : []
      statusOptions.value = Array.isArray(statusRes.data) ? statusRes.data : []
    })
    .catch(error => {
      console.error('加载用户枚举失败:', error)
    })
  handleQuery()
})
</script>

<style scoped>
.user-list {
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

.action-stack {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.action-stack :deep(.el-button) {
  min-width: 118px;
  min-height: 32px;
  border-radius: 10px;
  font-weight: 600;
}

.query-form {
  margin-bottom: 20px;
  padding: 20px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid rgba(59, 130, 246, 0.08);
}

.query-form :deep(.el-input) {
  width: 220px;
}

.query-form :deep(.el-input-number) {
  width: 220px;
}

.query-form :deep(.el-form-item) {
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

.user-list :deep(.el-table .cell) {
  line-height: 1.5;
}

.user-list :deep(.el-pagination) {
  margin-top: 24px;
  justify-content: flex-end;
}
</style>
