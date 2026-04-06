<template>
  <el-container class="admin-layout">
    <el-aside width="236px">
      <div class="logo">
        <div>
          <p class="logo-kicker">Chef Demo</p>
          <h3>管理后台</h3>
        </div>
      </div>
      <el-menu
        class="admin-menu"
        :default-active="activeMenu"
        router
      >
        <el-menu-item index="/admin/chef-list">
          <el-icon><User /></el-icon>
          <span>厨师资料</span>
        </el-menu-item>
        <el-menu-item index="/admin/user-list">
          <el-icon><UserFilled /></el-icon>
          <span>用户列表</span>
        </el-menu-item>
        <el-menu-item index="/admin/chef-audit">
          <el-icon><DocumentChecked /></el-icon>
          <span>厨师审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/review-list">
          <el-icon><ChatDotRound /></el-icon>
          <span>评论管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/report-list">
          <el-icon><Warning /></el-icon>
          <span>举报管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/order-list">
          <el-icon><List /></el-icon>
          <span>订单列表</span>
        </el-menu-item>
        <el-menu-item index="/admin/statistics">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header>
        <div class="header-content page-shell-wide">
          <div class="header-copy">
            <span class="hero-kicker">后台总览</span>
            <p>统一处理厨师资料、用户、评论、举报与订单数据。</p>
          </div>
          <div class="header-right">
            <HeaderUserInfo :user-name="displayUserName" :avatar="displayAvatar" />
            <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <div class="page-shell-wide main-shell">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { logout, queryInfo } from '@/api/auth'
import HeaderUserInfo from '@/components/HeaderUserInfo.vue'
import {
  User,
  UserFilled,
  DocumentChecked,
  ChatDotRound,
  Warning,
  List,
  DataAnalysis
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const displayUserName = computed(() => userStore.userInfo.username || '管理员')
const displayAvatar = computed(() => userStore.userInfo.avatar || '')

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

onMounted(() => {
  loadHeaderUserInfo()
})
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(59, 130, 246, 0.12), transparent 24%),
    linear-gradient(180deg, #f7f9ff 0%, #eef4ff 44%, #f4f6fb 100%);
}

.logo {
  height: 88px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 0 24px;
  background: linear-gradient(180deg, rgba(18, 33, 56, 0.98), rgba(33, 54, 86, 0.98));
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.logo-kicker {
  margin: 0 0 6px;
  color: rgba(191, 219, 254, 0.88);
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.logo h3 {
  margin: 0;
  font-family: var(--font-display);
  font-size: 1.35rem;
  font-weight: 400;
  letter-spacing: 0.02em;
}

.el-aside {
  background: linear-gradient(180deg, rgba(24, 40, 68, 1), rgba(34, 52, 81, 0.98));
  box-shadow: 18px 0 44px rgba(23, 35, 64, 0.18);
}

.el-header {
  position: sticky;
  top: 0;
  z-index: 12;
  height: var(--topbar-height);
  background-color: rgba(255, 255, 255, 0.78);
  box-shadow: 0 10px 26px rgba(50, 71, 113, 0.08);
  backdrop-filter: blur(18px);
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid rgba(59, 130, 246, 0.10);
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.header-copy p {
  margin: 10px 0 0;
  color: #61718d;
  font-size: 13px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.el-main {
  padding: 28px 20px 40px;
}

.main-shell {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.admin-menu {
  border-right: none;
  background: transparent;
}

.admin-menu :deep(.el-menu-item) {
  height: 48px;
  margin: 8px 14px;
  border-radius: 14px;
  color: rgba(226, 232, 240, 0.82);
  transition: background-color 220ms ease, color 220ms ease, transform 220ms ease;
}

.admin-menu :deep(.el-menu-item:hover) {
  background: rgba(96, 165, 250, 0.14);
  color: #ffffff;
}

.admin-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.24), rgba(14, 165, 233, 0.24));
  color: #ffffff;
  box-shadow: inset 0 0 0 1px rgba(147, 197, 253, 0.16);
}

@media (max-width: 1024px) {
  .header-content {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .el-aside {
    width: 88px !important;
  }

  .logo {
    justify-content: center;
    padding: 0 10px;
  }

  .logo-kicker,
  .logo h3 {
    display: none;
  }

  .admin-menu :deep(.el-menu-item span) {
    display: none;
  }

  .admin-menu :deep(.el-menu-item) {
    justify-content: center;
    margin-inline: 10px;
  }

  .el-main {
    padding-inline: 14px;
  }
}
</style>
