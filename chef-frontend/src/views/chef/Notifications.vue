<template>
  <div class="notifications-page">
    <el-card class="notifications-card">
      <template #header>
        <div class="card-header">
          <div>
            <h2>通知中心</h2>
            <p>查看审核结果、账号状态变更和系统提醒。</p>
          </div>
          <el-switch
            v-model="unreadOnly"
            active-text="只看未读"
            @change="loadData"
          />
        </div>
      </template>

      <div class="notification-list" v-loading="loading">
        <el-empty v-if="notificationList.length === 0 && !loading" description="暂无通知" />

        <el-card
          v-for="notification in notificationList"
          :key="notification.id"
          class="notification-item"
          :class="{ unread: notification.readStatus === 1 }"
          shadow="hover"
          @click="handleRead(notification)"
        >
          <div class="notification-top">
            <div class="notification-title-wrap">
              <el-icon v-if="notification.readStatus === 1" color="#67c23a"><Bell /></el-icon>
              <el-icon v-else color="#b6c2b2"><BellFilled /></el-icon>
              <span class="notification-title">{{ notification.title || '系统通知' }}</span>
            </div>
            <span class="notification-time">{{ formatTime(notification.createTime) }}</span>
          </div>

          <div class="notification-content">{{ notification.content }}</div>

          <div class="notification-footer">
            <el-tag v-if="notification.readStatus === 1" type="success" size="small">未读</el-tag>
            <el-tag v-else size="small" effect="plain">已读</el-tag>
          </div>
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
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, BellFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { notificationList as fetchNotificationList, readNotification } from '@/api/chef'
import { emitNotificationUnreadChange } from '@/utils/notification'

const userStore = useUserStore()
const loading = ref(false)
const unreadOnly = ref(false)
const notificationList = ref([])
const total = ref(0)
const queryForm = ref({
  page: 1,
  size: 10
})

const formatTime = timestamp => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }
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
    console.error('加载厨师通知失败:', error)
  } finally {
    loading.value = false
  }
}

const handleRead = async notification => {
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
    console.error('标记厨师通知已读失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.notifications-page {
  max-width: 980px;
  margin: 0 auto;
}

.notifications-card {
  border-radius: 24px;
  box-shadow: 0 18px 40px rgba(70, 104, 55, 0.12);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.card-header h2 {
  margin: 0 0 6px;
  color: #26361f;
}

.card-header p {
  margin: 0;
  color: #83917d;
}

.notification-list {
  min-height: 420px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notification-item {
  cursor: pointer;
  border-radius: 18px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.notification-item.unread {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.08), rgba(103, 194, 58, 0.02));
  border-left: 4px solid #67c23a;
}

.notification-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 26px rgba(67, 102, 52, 0.12);
}

.notification-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 10px;
}

.notification-title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.notification-title {
  font-size: 16px;
  font-weight: 600;
  color: #26361f;
}

.notification-time {
  flex-shrink: 0;
  color: #95a08f;
  font-size: 13px;
}

.notification-content {
  color: #485643;
  line-height: 1.75;
}

.notification-footer {
  margin-top: 12px;
}

.pagination {
  margin-top: 24px;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .card-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .notification-top {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
