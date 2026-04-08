<template>
  <div class="time-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预约时间段管理</span>
          <el-button type="primary" @click="showAddDialog">新增时间段</el-button>
        </div>
      </template>
      
      <!-- 时间段列表 -->
      <el-table
        :data="tableData"
        border
        stripe
        class="time-table"
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="100" align="center" header-align="center" />
        <el-table-column label="开始时间" min-width="220" align="center" header-align="center">
          <template #default="{ row }">
            {{ row.startTimeDesc }}
          </template>
        </el-table-column>
        <el-table-column label="结束时间" min-width="220" align="center" header-align="center">
          <template #default="{ row }">
            {{ row.endTimeDesc }}
          </template>
        </el-table-column>
        <el-table-column label="时长" min-width="140" align="center" header-align="center">
          <template #default="{ row }">
            {{ calculateDuration(row.startTime, row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="140" align="center" header-align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status, row.statusDesc) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="150" align="center" header-align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
            <el-tag v-else type="info">不可删除</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        class="pagination"
        background
        layout="total, prev, pager, next"
        :total="total"
        @current-change="loadData"
      />
    </el-card>
    
    <!-- 新增时间段对话框 -->
    <el-dialog v-model="addVisible" title="新增可预约时间段" width="500px">
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="100px">
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="addForm.startTime"
            type="datetime"
            format="YYYY-MM-DD HH:mm"
            placeholder="请选择时间段开始时间"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="addForm.endTime"
            type="datetime"
            format="YYYY-MM-DD HH:mm"
            placeholder="请选择时间段结束时间"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <div class="time-tip">时间格式：年-月-日 时:分，结束时间需晚于开始时间。</div>
      </el-form>
      
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAdd" :loading="adding">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listAvailableTimes, addAvailableTime, deleteAvailableTime } from '@/api/chef'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getProfile as getChefProfile } from '@/api/chef'
import { getAvailableTimeStatusLabelMap, isChefAuditApproved } from '@/api/constant'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const adding = ref(false)
const tableData = ref([])
const total = ref(0)
const addVisible = ref(false)
const addFormRef = ref(null)
const availableTimeStatusLabelMap = ref({})
const queryForm = reactive({
  page: 1,
  size: 10
})

const addForm = reactive({
  startTime: null,
  endTime: null
})

const addRules = {
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const calculateDuration = (start, end) => {
  const hours = (end - start) / (1000 * 60 * 60)
  return `${hours.toFixed(1)}小时`
}

const getStatusType = (status) => {
  const map = {
    1: 'success',
    2: 'warning',
    3: 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status, statusDesc) => statusDesc || availableTimeStatusLabelMap.value[status] || ''

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

const ensureAuditApproved = async () => {
  const res = await getChefProfile({})
  const approved = await isChefAuditApproved(res.data?.auditStatus)
  if (!approved) {
    ElMessage.warning('厨师审核尚未通过，请先完善资料并等待审核')
    router.replace('/chef/profile')
    return false
  }
  return true
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await listAvailableTimes({
      page: queryForm.page,
      size: queryForm.size
    })
    tableData.value = res.data || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  addForm.startTime = null
  addForm.endTime = null
  addVisible.value = true
}

const confirmAdd = async () => {
  if (!addFormRef.value) return
  
  await addFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (addForm.startTime.getTime() <= Date.now()) {
      ElMessage.error('开始时间必须晚于当前时间')
      return
    }
    if (addForm.endTime <= addForm.startTime) {
      ElMessage.error('结束时间必须晚于开始时间')
      return
    }
    
    adding.value = true
    try {
      await addAvailableTime({
        startTime: addForm.startTime.getTime(),
        endTime: addForm.endTime.getTime()
      })
      
      ElMessage.success('添加成功')
      addVisible.value = false
      queryForm.page = 1
      loadData()
    } catch (error) {
      console.error('添加失败:', error)
    } finally {
      adding.value = false
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这个时间段吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAvailableTime({
        timeId: row.id
      })
      
      ElMessage.success('删除成功')
      queryForm.page = 1
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

onMounted(() => {
  getAvailableTimeStatusLabelMap()
    .then(map => {
      availableTimeStatusLabelMap.value = map
    })
    .catch(error => {
      console.error('加载可预约时间状态枚举失败:', error)
    })
  ensureAuditApproved().then((passed) => {
    if (passed) {
      loadData()
    }
  })
})
</script>

<style scoped>
.time-manage {
  max-width: 1200px;
  margin: 0 auto;
}

.time-table :deep(.el-table__cell) {
  vertical-align: middle;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.time-tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

.pagination {
  margin-top: 24px;
  justify-content: flex-end;
}
</style>
