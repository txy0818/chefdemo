<template>
  <div class="report-list">
    <section class="section-heading page-heading">
      <div>
        <span class="hero-kicker">举报治理</span>
        <h2>举报管理</h2>
        <p>查看举报进度与处理结果，优先解决待处理的投诉记录。</p>
      </div>
      <div class="metric-pill">共 {{ total }} 条</div>
    </section>

    <el-card class="panel-card glass-panel" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <strong>筛选举报</strong>
            <p>待处理举报需要尽快判断是否属实，并补充清晰说明。</p>
          </div>
        </div>
      </template>
      
      <!-- 查询条件 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="举报状态">
          <el-select v-model="queryForm.status" placeholder="请选择举报状态" style="width: 200px">
            <el-option
              v-for="item in reportStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item class="query-actions">
          <el-button class="query-btn query-btn-primary" type="primary" @click="handleQuery">查询</el-button>
          <el-button class="query-btn query-btn-secondary" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 表格 -->
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="reservationOrderId" label="订单ID" width="100" />
        <el-table-column prop="reporterName" label="举报人" width="120" />
        <el-table-column prop="targetUserName" label="被举报人" width="120" />
        <el-table-column prop="reason" label="举报原因" min-width="200" />
        <el-table-column prop="processResult" label="处理结果" width="150" />
        <el-table-column prop="statusDesc" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'warning' : 'success'">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180" align="center" header-align="center">
          <template #default="{ row }">
            <div class="action-stack">
              <el-button
                v-if="row.status === 1"
                type="primary"
                size="small"
                @click="handleProcess(row)"
              >
                处理
              </el-button>
              <el-tag v-else type="success" effect="light">已处理</el-tag>
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
    
    <!-- 处理对话框 -->
    <el-dialog v-model="processVisible" title="填写举报处理结果" width="600px">
      <el-form ref="processFormRef" :model="processForm" :rules="processRules" label-width="100px">
        <el-form-item label="举报原因">
          <el-input
            v-model="currentReport.reason"
            type="textarea"
            :rows="3"
            disabled
          />
        </el-form-item>
        
        <el-form-item label="处理结果">
          <el-radio-group v-model="processForm.status">
            <el-radio :label="2">举报属实</el-radio>
            <el-radio :label="3">举报不实</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="处理说明" prop="processResult">
          <el-input
            v-model="processForm.processResult"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="请写明核实结果、处理依据和最终结论"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="processVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmProcess" :loading="processing">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { reportList, handleReport } from '@/api/admin'
import { getReportStatusOptions } from '@/api/constant'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const processing = ref(false)
const tableData = ref([])
const total = ref(0)
const processVisible = ref(false)
const currentReport = ref({})
const processFormRef = ref(null)
const reportStatusOptions = ref([])

const queryForm = reactive({
  status: 1,
  page: 1,
  size: 10
})

const processForm = reactive({
  orderId: null,
  status: 2,
  processResult: ''
})

const processRules = {
  processResult: [
    { required: true, message: '请输入处理说明', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (!value || !value.trim()) {
          callback(new Error('请输入处理说明'))
          return
        }
        if (value.trim().length > 200) {
          callback(new Error('处理说明最多200字'))
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
    const res = await reportList(queryForm)
    tableData.value = res.data
    total.value = res.total
  } catch (error) {
    console.error('查询失败:', error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.status = 1
  queryForm.page = 1
  handleQuery()
}

const handleProcess = (row) => {
  currentReport.value = row
  processForm.orderId = row.reservationOrderId
  processForm.status = 2
  processForm.processResult = ''
  processVisible.value = true
}

const confirmProcess = async () => {
  if (!processFormRef.value) return
  const valid = await processFormRef.value.validate().catch(() => false)
  if (!valid) return

  processing.value = true
  try {
    processForm.processResult = processForm.processResult.trim()
    await handleReport(processForm)
    ElMessage.success('处理成功')
    processVisible.value = false
    handleQuery()
  } catch (error) {
    console.error('处理失败:', error)
  } finally {
    processing.value = false
  }
}

onMounted(() => {
  getReportStatusOptions()
    .then(res => {
      reportStatusOptions.value = Array.isArray(res.data) ? res.data : []
    })
    .catch(error => {
      console.error('加载举报状态枚举失败:', error)
    })
  handleQuery()
})
</script>

<style scoped>
.report-list {
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

.report-list :deep(.el-form-item) {
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

.report-list :deep(.el-pagination) {
  margin-top: 24px;
  justify-content: flex-end;
}
</style>
