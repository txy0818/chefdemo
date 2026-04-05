<template>
  <el-container class="admin-layout">
    <el-aside width="200px">
      <div class="logo">
        <h3>管理后台</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
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
        <div class="header-content">
          <div class="header-right">
            <HeaderUserInfo :user-name="displayUserName" :avatar="displayAvatar" />
            <el-button type="danger" @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <router-view />
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
  height: 100vh;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4b;
  color: #fff;
}

.logo h3 {
  margin: 0;
}

.el-aside {
  background-color: #304156;
}

.el-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.el-main {
  background-color: #f0f2f5;
  padding: 28px 32px 36px;
}
</style>
