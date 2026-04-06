<template>
  <el-container class="chef-layout">
    <el-header>
      <div class="header-content page-shell-wide">
        <div class="brand-block">
          <div class="logo">厨师工作台</div>
          <p>管理资料、开放档期并处理预约订单</p>
        </div>
        <el-menu
          class="top-nav"
          :default-active="activeMenu"
          mode="horizontal"
          :router="false"
          :ellipsis="false"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/chef/account">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
          <el-menu-item index="/chef/profile">
            <el-icon><Tickets /></el-icon>
            <span>个人资料</span>
          </el-menu-item>
          <el-menu-item index="/chef/time-manage" :disabled="!isAuditApproved">
            <el-icon><Clock /></el-icon>
            <span>时间管理</span>
          </el-menu-item>
          <el-menu-item index="/chef/order-manage" :disabled="!isAuditApproved">
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/chef/notifications">
            <el-badge :value="unreadCount" :hidden="unreadCount <= 0" :max="99">
              <el-icon><Bell /></el-icon>
            </el-badge>
            <span>通知</span>
          </el-menu-item>
        </el-menu>
        <div class="header-actions">
          <HeaderUserInfo :user-name="displayUserName" :avatar="displayAvatar" />
          <el-button class="logout-button" type="success" plain @click="handleLogout">退出</el-button>
        </div>
      </div>
    </el-header>
    
    <el-main>
      <div class="page-shell-wide main-shell">
        <router-view />
      </div>
    </el-main>
  </el-container>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { logout, queryInfo } from '@/api/auth'
import { getProfile as getChefProfile, notificationList as fetchNotificationList } from '@/api/chef'
import HeaderUserInfo from '@/components/HeaderUserInfo.vue'
import { NOTIFICATION_UNREAD_CHANGE_EVENT, showRealtimeNotification } from '@/utils/notification'
import { createUserSocket } from '@/utils/ws'
import { User, Tickets, Clock, List, Bell } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isAuditApproved = ref(false)
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
const displayUserName = computed(() => userStore.userInfo.username || '厨师')
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
    console.error('加载厨师未读通知失败:', error)
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

const loadChefAuditStatus = async () => {
  if (!userStore.userInfo.userId) return
  try {
    const res = await getChefProfile({})
    isAuditApproved.value = res.data?.auditStatus === '通过'
  } catch (error) {
    isAuditApproved.value = false
  }
}

const handleMenuSelect = (index) => {
  if ((index === '/chef/time-manage' || index === '/chef/order-manage') && !isAuditApproved.value) {
    ElMessage.warning('厨师审核尚未通过，请先完善资料并等待审核')
    router.push('/chef/profile')
    return
  }
  router.push(index)
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
    onMessage: async payload => {
      if (!payload?.type) return
      if (payload.type === 'FORCE_LOGOUT') {
        handleForceLogout(payload.content)
        return
      }
      unreadCount.value += 1
      if (payload.data?.id) {
        knownUnreadIds.add(payload.data.id)
      }
      if (payload.type === 'CHEF_AUDIT_RESULT') {
        await loadChefAuditStatus()
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
  loadChefAuditStatus()
  connectSocket()
  startNotificationPolling()
  window.addEventListener(NOTIFICATION_UNREAD_CHANGE_EVENT, handleUnreadChange)
})

watch(
  () => route.path,
  path => {
    if (path === '/chef/notifications') {
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
.chef-layout {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(74, 222, 128, 0.16), transparent 24%),
    radial-gradient(circle at top right, rgba(250, 204, 21, 0.12), transparent 18%),
    linear-gradient(180deg, #f7fff7 0%, #fbfff6 46%, #f2faea 100%);
}

.el-header {
  position: sticky;
  top: 0;
  z-index: 20;
  height: var(--topbar-height);
  background-color: rgba(250, 255, 248, 0.84);
  backdrop-filter: blur(18px);
  box-shadow: 0 12px 28px rgba(34, 84, 47, 0.09);
  padding: 0 20px;
  border-bottom: 1px solid rgba(34, 197, 94, 0.10);
}

.header-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 22px;
}

.brand-block {
  min-width: 0;
  flex-shrink: 0;
}

.brand-block p {
  margin: 4px 0 0;
  color: #50755a;
  font-size: 12px;
}

.logo {
  font-family: var(--font-display);
  font-size: 1.5rem;
  line-height: 1;
  letter-spacing: 0.02em;
  color: #166534;
}

.top-nav {
  flex: 1;
  min-width: 0;
  border-bottom: none;
  background: transparent;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
  margin-left: 20px;
}

.el-main {
  padding: 28px 20px 40px;
}

.main-shell {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.top-nav :deep(.el-menu-item) {
  height: calc(var(--topbar-height) - 14px);
  margin: 7px 2px;
  border-radius: 16px;
  color: #42634b;
  transition: background-color 220ms ease, color 220ms ease, transform 220ms ease;
}

.top-nav :deep(.el-menu-item:hover) {
  background: rgba(34, 197, 94, 0.09);
  color: #166534;
}

.top-nav :deep(.el-menu-item.is-active) {
  color: #166534;
  background: linear-gradient(135deg, rgba(240, 253, 244, 0.98), rgba(254, 252, 232, 0.96));
  box-shadow: inset 0 0 0 1px rgba(34, 197, 94, 0.14);
}

.top-nav :deep(.el-menu--horizontal > .el-menu-item.is-active) {
  border-bottom: none;
}

.logout-button {
  min-width: 90px;
}

@media (max-width: 1080px) {
  .brand-block p {
    display: none;
  }
}

@media (max-width: 920px) {
  .el-header {
    height: auto;
    padding-top: 14px;
    padding-bottom: 14px;
  }

  .header-content {
    align-items: flex-start;
    flex-direction: column;
  }

  .top-nav {
    width: 100%;
  }

  .header-actions {
    margin-left: 0;
  }
}

@media (max-width: 640px) {
  .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .el-main {
    padding-inline: 14px;
  }
}
</style>
