<template>
  <div class="notifications-page">
    <el-card class="notifications-card">
      <template #header>
        <div class="card-header">
          <div>
            <h2>通知中心</h2>
            <p>查看订单状态变化、账号提醒和系统消息。</p>
          </div>
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
          class="notification-item"
          :class="{ unread: notification.readStatus === 1 }"
          shadow="hover"
          @click="handleRead(notification)"
        >
          <div class="notification-top">
            <div class="notification-title-wrap">
              <el-icon v-if="notification.readStatus === 1" color="#f59e0b"><Bell /></el-icon>
              <el-icon v-else color="#d1b48a"><BellFilled /></el-icon>
              <span class="notification-title">{{ notification.title || '系统通知' }}</span>
            </div>
            <span class="notification-time">{{ formatDateTime(notification.createTime) }}</span>
          </div>

          <div class="notification-content">
            {{ notification.content }}
          </div>

          <div class="notification-footer">
            <el-tag v-if="notification.readStatus === 1" type="warning" size="small">未读</el-tag>
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
import { ref, onMounted } from 'vue'
import { notificationList as fetchNotificationList, readNotification } from '@/api/user'
import { ElMessage } from 'element-plus'
import { Bell, BellFilled } from '@element-plus/icons-vue'
import { emitNotificationUnreadChange } from '@/utils/notification'
import { formatDateTime } from '@/utils/datetime'

const loading = ref(false)
const notificationList = ref([])
const unreadOnly = ref(false)
const total = ref(0)
const queryForm = ref({
  page: 1,
  size: 10
})

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
    console.error('标记失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.notifications-page {
  width: 100%;
}

.notifications-card {
  border-radius: 24px;
  box-shadow: 0 18px 40px rgba(194, 110, 44, 0.12);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.card-header h2 {
  margin: 0 0 6px;
  color: #3a2416;
}

.card-header p {
  margin: 0;
  color: #9b7d68;
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
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.1), rgba(245, 158, 11, 0.03));
  border-left: 4px solid #f59e0b;
}

.notification-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 26px rgba(181, 103, 43, 0.14);
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
  color: #3a2416;
}

.notification-time {
  flex-shrink: 0;
  color: #b1907b;
  font-size: 13px;
}

.notification-content {
  color: #5a4334;
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
}
</style>
