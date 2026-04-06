<template>
  <div class="notifications">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>通知列表</span>
          <el-switch
            v-model="unreadOnly"
            active-text="只看未读"
            @change="loadData"
          />
        </div>
      </template>
      
      <div class="notification-list" v-loading="loading">
        <el-empty v-if="notificationList.length === 0 && !loading" description="当前还没有通知消息" />
        
        <el-card
          v-for="notification in notificationList"
          :key="notification.id"
          class="notification-card"
          :class="{ 'unread': notification.readStatus === 1 }"
          shadow="hover"
          @click="handleRead(notification)"
        >
          <div class="notification-header">
            <el-icon v-if="notification.readStatus === 1" color="#409EFF"><Bell /></el-icon>
            <el-icon v-else color="#ccc"><BellFilled /></el-icon>
            <span class="notification-time">{{ formatTime(notification.createTime) }}</span>
          </div>
          
          <div class="notification-content">
            {{ notification.content }}
          </div>
          
          <el-tag
            v-if="notification.readStatus === 1"
            type="primary"
            size="small"
            style="margin-top: 10px"
          >
            未读
          </el-tag>
        </el-card>
      </div>

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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { notificationList as fetchNotificationList, readNotification } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Bell, BellFilled } from '@element-plus/icons-vue'
import { emitNotificationUnreadChange } from '@/utils/notification'

const userStore = useUserStore()
const loading = ref(false)
const notificationList = ref([])
const unreadOnly = ref(false)
const total = ref(0)
const queryForm = ref({
  page: 1,
  size: 10
})

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  // 1分钟内
  if (diff < 60000) {
    return '刚刚'
  }
  // 1小时内
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }
  // 今天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  // 昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  // 其他
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
    const res = await fetchNotificationList({
      unreadOnly: unreadOnly.value,
      page: queryForm.value.page,
      size: queryForm.value.size
    })
    notificationList.value = res.data || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

const handleRead = async (notification) => {
  if (notification.readStatus === 2) return
  
  try {
    await readNotification({
      notificationId: notification.id
    })
    notification.readStatus = 2
    emitNotificationUnreadChange(-1)
    if (unreadOnly.value) {
      notificationList.value = notificationList.value.filter(item => item.id !== notification.id)
      total.value = Math.max(0, total.value - 1)
    }
    ElMessage.success('已标记为已读')
  } catch (error) {
    console.error('标记失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.notifications {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  min-height: 400px;
}

.notification-card {
  cursor: pointer;
  transition: all 0.3s;
}

.notification-card.unread {
  background-color: #f0f9ff;
  border-left: 3px solid #409EFF;
}

.notification-card:hover {
  transform: translateX(5px);
}

.notification-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  color: #999;
  font-size: 14px;
}

.notification-time {
  margin-left: auto;
}

.notification-content {
  line-height: 1.6;
  color: #333;
}

.pagination {
  margin-top: 24px;
  justify-content: flex-end;
}
</style>
