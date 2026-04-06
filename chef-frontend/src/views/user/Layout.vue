<template>
  <el-container class="user-layout">
    <el-header class="user-header">
      <div class="header-content page-shell">
        <div class="brand-block">
          <div class="brand-mark">Chef Demo</div>
          <div class="brand-copy">
            <div class="logo">私厨上门体验</div>
            <p>更快找到合适厨师，按场景下单，实时掌握订单进度。</p>
          </div>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="user-nav"
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
          <el-menu-item index="/user/profile">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
        </el-menu>
        <div class="header-actions">
          <HeaderUserInfo :user-name="displayUserName" :avatar="displayAvatar" />
          <el-button class="logout-button" @click="handleLogout">退出</el-button>
        </div>
      </div>
    </el-header>
    
    <el-main class="user-main">
      <div class="page-shell">
        <section class="hero-banner glass-panel">
          <div class="hero-copy">
            <span class="hero-kicker">精选家宴与上门私厨服务</span>
            <h1>把一顿饭，升级成值得期待的到家体验。</h1>
            <p>
              按价格、菜系、评分与服务区域快速筛选，订单、通知与余额都能在同一套体验里顺畅完成。
            </p>
          </div>
          <div class="hero-metrics" aria-label="平台功能亮点">
            <div class="metric-card">
              <strong>多维筛选</strong>
              <span>按菜系、价格、评分和区域锁定合适厨师</span>
            </div>
            <div class="metric-card">
              <strong>状态透明</strong>
              <span>消息提醒、待办状态和订单阶段一眼就清楚</span>
            </div>
            <div class="metric-card">
              <strong>结算顺滑</strong>
              <span>余额管理与支付路径收束在统一页面里</span>
            </div>
          </div>
        </section>

        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
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
import { notificationList as fetchNotificationList } from '@/api/user'
import HeaderUserInfo from '@/components/HeaderUserInfo.vue'
import { NOTIFICATION_UNREAD_CHANGE_EVENT, showRealtimeNotification } from '@/utils/notification'
import { createUserSocket } from '@/utils/ws'
import { Search, List, Bell, User, Wallet } from '@element-plus/icons-vue'

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
}

.user-header {
  height: auto;
  padding: 18px 20px 0;
  background: transparent;
}

.header-content {
  min-height: 88px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 18px;
  padding: 18px 22px;
  border: 1px solid rgba(220, 38, 38, 0.1);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 20px 50px rgba(127, 29, 29, 0.08);
  backdrop-filter: blur(18px);
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 84px;
  height: 52px;
  padding: 0 16px;
  border-radius: 18px;
  background: linear-gradient(135deg, #dc2626, #f59e0b);
  box-shadow: 0 16px 32px rgba(220, 38, 38, 0.22);
  font-family: var(--font-display);
  font-size: 1rem;
  color: #fffdf8;
}

.brand-copy {
  min-width: 0;
}

.logo {
  margin: 0;
  font-family: var(--font-display);
  font-size: 1.5rem;
  font-weight: 400;
  line-height: 1.1;
  color: var(--color-title);
}

.brand-copy p {
  margin: 6px 0 0;
  color: var(--color-text-soft);
  font-size: 0.92rem;
}

.user-nav {
  flex: 1;
  border-bottom: none;
  min-width: min(100%, 420px);
  background: transparent;
}

.user-nav :deep(.el-menu-item) {
  min-height: 44px;
  border-radius: 14px;
  color: #7c5b5b;
  transition: background-color 220ms ease, color 220ms ease, transform 220ms ease;
}

.user-nav :deep(.el-menu-item:hover),
.user-nav :deep(.el-menu-item.is-active) {
  background: rgba(255, 241, 235, 0.92);
  color: #b91c1c;
}

.user-nav :deep(.el-menu-item.is-active) {
  font-weight: 700;
}

.header-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 14px;
  flex-wrap: wrap;
}

.logout-button {
  min-height: 44px;
  padding: 0 18px;
  border-radius: 14px;
  border: 1px solid rgba(220, 38, 38, 0.15);
  background: rgba(255, 246, 246, 0.9);
  color: #b91c1c;
  font-weight: 600;
}

.logout-button:hover {
  background: #b91c1c;
  color: #fff;
  border-color: #b91c1c;
}

.user-main {
  padding: 22px 20px 40px;
}

.hero-banner {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(0, 1fr);
  gap: 20px;
  margin-bottom: 28px;
  padding: 28px;
  border-radius: 32px;
}

.hero-kicker {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(255, 241, 235, 0.98);
  color: #a16207;
  font-size: 0.84rem;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.hero-copy h1 {
  margin: 18px 0 14px;
  max-width: 10ch;
  font-family: var(--font-display);
  font-size: clamp(2.2rem, 5vw, 3.7rem);
  line-height: 1.02;
  font-weight: 400;
  color: var(--color-title);
}

.hero-copy p {
  margin: 0;
  max-width: 640px;
  font-size: 1rem;
  line-height: 1.75;
  color: var(--color-text-soft);
}

.hero-metrics {
  display: grid;
  gap: 14px;
}

.metric-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
  min-height: 108px;
  padding: 18px 20px;
  border-radius: 22px;
  border: 1px solid rgba(220, 38, 38, 0.1);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(255, 247, 237, 0.92));
}

.metric-card strong {
  font-size: 1rem;
  color: var(--color-title);
}

.metric-card span {
  font-size: 0.92rem;
  line-height: 1.6;
  color: var(--color-text-soft);
}

@media (max-width: 1120px) {
  .header-content {
    justify-content: center;
  }

  .brand-block,
  .header-actions {
    width: 100%;
  }

  .user-nav {
    order: 3;
    width: 100%;
  }

  .hero-banner {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .user-header,
  .user-main {
    padding-left: 14px;
    padding-right: 14px;
  }

  .header-content {
    padding: 16px;
    border-radius: 24px;
  }

  .brand-block {
    align-items: flex-start;
  }

  .brand-mark {
    min-width: 72px;
    height: 46px;
    padding: 0 12px;
  }

  .brand-copy p {
    font-size: 0.88rem;
  }

  .hero-banner {
    padding: 22px 18px;
    border-radius: 24px;
  }

  .hero-copy h1 {
    max-width: none;
    font-size: 2.5rem;
  }

  .header-actions {
    justify-content: space-between;
  }
}
</style>
