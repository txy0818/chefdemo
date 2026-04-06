<template>
  <el-container class="user-layout">
    <el-header>
      <div class="header-content">
        <div class="logo">厨师预约系统</div>
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          router
          :ellipsis="false"
        >
          <el-menu-item index="/user/chef-search">
            <el-icon><Search /></el-icon>
            <span>搜索厨师</span>
          </el-menu-item>
          <el-menu-item index="/user/order-list">
            <el-icon><List /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="/user/notifications">
            <el-badge :value="unreadCount" :hidden="unreadCount <= 0" :max="99">
              <el-icon><Bell /></el-icon>
            </el-badge>
            <span>通知</span>
          </el-menu-item>
          <el-menu-item index="/user/wallet">
            <el-icon><Wallet /></el-icon>
            <span>余额</span>
          </el-menu-item>
        </el-menu>
        <div class="header-actions">
          <HeaderUserInfo
            :user-name="displayUserName"
            :avatar="displayAvatar"
            @click="router.push('/user/profile')"
          />
          <el-button type="danger" @click="handleLogout">退出</el-button>
        </div>
      </div>
    </el-header>
    
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { logout, queryInfo } from '@/api/auth'
import { notificationList as fetchNotificationList } from '@/api/user'
import HeaderUserInfo from '@/components/HeaderUserInfo.vue'
import { NOTIFICATION_UNREAD_CHANGE_EVENT, showRealtimeNotification } from '@/utils/notification'
import { createUserSocket } from '@/utils/ws'
import { Search, List, Bell, Wallet } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const socketRef = ref(null)
const forceLogoutRef = ref(false)
const unreadCount = ref(0)
const pollingTimerRef = ref(null)
const knownUnreadIds = new Set()
const handleUnreadChange = event => {
  const delta = Number(event?.detail?.delta || 0)
  unreadCount.value = Math.max(0, unreadCount.value + delta)
}

const activeMenu = computed(() => route.path)
const displayUserName = computed(() => userStore.userInfo.username || '用户')
const displayAvatar = computed(() => userStore.userInfo.avatar || '')

const loadUnreadCount = async () => {
  if (!userStore.userInfo.userId) return
  try {
    const res = await fetchNotificationList({
      unreadOnly: true
    })
    const unreadList = Array.isArray(res.data) ? res.data : []
    unreadCount.value = unreadList.length
    return unreadList
  } catch (error) {
    console.error('加载用户未读通知失败:', error)
    return []
  }
}

const loadHeaderUserInfo = async () => {
  if (!userStore.userInfo.userId) return
  try {
    const res = await queryInfo()
    userStore.patchUserInfo({
      username: res.data?.username || userStore.userInfo.username,
      avatar: res.data?.avatar && res.data.avatar !== '-' ? res.data.avatar : ''
    })
  } catch (error) {
    console.error('加载顶部用户信息失败:', error)
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await logout()
    } catch (error) {
      console.error('退出登录失败:', error)
    }
    userStore.clearUserInfo()
    router.push('/login')
  })
}

const handleForceLogout = (content) => {
  if (forceLogoutRef.value) return
  forceLogoutRef.value = true
  ElMessage.error(content || '账号状态已变更，请重新登录')
  userStore.clearUserInfo()
  closeSocket()
  router.replace('/login')
}

const connectSocket = () => {
  if (!userStore.userInfo.userId) return
  closeSocket()
  socketRef.value = createUserSocket(userStore.userInfo.userId, {
    onMessage: payload => {
      if (!payload?.type) return
      if (payload.type === 'FORCE_LOGOUT') {
        handleForceLogout(payload.content)
        return
      }
      unreadCount.value += 1
      if (payload.data?.id) {
        knownUnreadIds.add(payload.data.id)
      }
      showRealtimeNotification(payload)
    }
  })
}

const closeSocket = () => {
  if (socketRef.value) {
    socketRef.value.close()
    socketRef.value = null
  }
}

const startNotificationPolling = async () => {
  const initialUnreadList = await loadUnreadCount()
  initialUnreadList.forEach(item => {
    if (item?.id) {
      knownUnreadIds.add(item.id)
    }
  })
  stopNotificationPolling()
  pollingTimerRef.value = setInterval(async () => {
    const unreadList = await loadUnreadCount()
    unreadList.forEach(item => {
      if (!item?.id || knownUnreadIds.has(item.id)) {
        return
      }
      knownUnreadIds.add(item.id)
      showRealtimeNotification({
        title: item.title,
        content: item.content
      })
    })
  }, 3000)
}

const stopNotificationPolling = () => {
  if (pollingTimerRef.value) {
    clearInterval(pollingTimerRef.value)
    pollingTimerRef.value = null
  }
}

onMounted(() => {
  loadHeaderUserInfo()
  connectSocket()
  startNotificationPolling()
  window.addEventListener(NOTIFICATION_UNREAD_CHANGE_EVENT, handleUnreadChange)
})

watch(
  () => route.path,
  path => {
    if (path === '/user/notifications') {
      loadUnreadCount()
    }
  }
)

onBeforeUnmount(() => {
  window.removeEventListener(NOTIFICATION_UNREAD_CHANGE_EVENT, handleUnreadChange)
  stopNotificationPolling()
  closeSocket()
})
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  background:
    radial-gradient(circle at top, rgba(64, 158, 255, 0.08), transparent 28%),
    linear-gradient(180deg, #f7f9fc 0%, #eef3f9 100%);
}

.el-header {
  background-color: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(14px);
  box-shadow: 0 10px 24px rgba(44, 76, 120, 0.10);
  padding: 0 28px;
}

.header-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
  margin-right: 40px;
}

.el-menu {
  flex: 1;
  border-bottom: none;
  min-width: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
  margin-left: 20px;
}

.el-main {
  padding: 28px 32px 36px;
}
</style>
