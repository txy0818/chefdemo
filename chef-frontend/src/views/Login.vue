<template>
  <div class="login-container">
    <div class="auth-hero">
      <div class="hero-eyebrow">PRIVATE CHEF PLATFORM</div>
      <h1>厨师预约系统</h1>
    </div>
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>欢迎登录</h2>
          <p>请选择身份后进入对应工作台</p>
        </div>
      </template>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="80px"
      >
        <el-form-item label="角色" prop="role">
          <el-select v-model="loginForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="item in roleOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
        
        <el-form-item>
          <el-button
            style="width: 100%"
            @click="goToRegister"
          >
            还没有账号？去注册
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { getUserRoleOptions } from '@/api/constant'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)
const roleOptions = ref([])

const loginForm = reactive({
  role: 3,
  username: '',
  password: ''
})

const loginRules = {
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      const res = await login(loginForm)
      
      console.log('登录响应:', res)
      
      // 响应拦截器返回的是整个 res，不是 res.data
      // 后端返回: { baseResp: {...}, token: "xxx", userName: "admin", userId: "1" }
      userStore.setToken(res.token)
      userStore.setUserInfo({
        userId: res.userId,
        username: res.userName || loginForm.username,
        role: loginForm.role
      })
      
      console.log('保存的用户信息:', {
        token: res.token,
        userId: res.userId,
        username: res.userName,
        role: loginForm.role
      })
      
      ElMessage.success('登录成功')
      
      // 等待下一个 tick 确保 store 更新完成
      await new Promise(resolve => setTimeout(resolve, 100))
      
      console.log('准备跳转，角色:', loginForm.role)
      
      // 根据角色跳转到不同页面
      if (loginForm.role === 1) {
        console.log('跳转到管理员页面')
        await router.push('/admin/chef-list')
      } else if (loginForm.role === 2) {
        console.log('跳转到厨师页面')
        await router.push('/chef/account')
      } else {
        console.log('跳转到用户页面')
        await router.push('/user/chef-search')
      }
    } catch (error) {
      console.error('登录失败:', error)
    } finally {
      loading.value = false
    }
  })
}

const goToRegister = () => {
  router.push('/register')
}

const loadRoleOptions = async () => {
  try {
    const res = await getUserRoleOptions()
    roleOptions.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error('加载角色枚举失败:', error)
  }
}

onMounted(() => {
  loadRoleOptions()
})
</script>

<style scoped>
.login-container {
  display: flex;
  gap: 48px;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(64, 158, 255, 0.24), transparent 30%),
    linear-gradient(135deg, #f5f9ff 0%, #eaf1ff 45%, #f8fbff 100%);
}

.auth-hero {
  max-width: 420px;
}

.hero-eyebrow {
  font-size: 12px;
  letter-spacing: 0.2em;
  font-weight: 700;
  color: #6ea8ff;
}

.auth-hero h1 {
  margin: 12px 0 16px;
  font-size: 44px;
  line-height: 1.1;
  color: #1f2a44;
}

.login-card {
  width: 450px;
  border-radius: 24px;
  box-shadow: 0 20px 48px rgba(43, 77, 135, 0.14);
}

.card-header h2 {
  margin: 0;
  text-align: center;
  color: #1f2a44;
}

.card-header p {
  margin: 8px 0 0;
  text-align: center;
  color: #8a94a6;
}

@media (max-width: 960px) {
  .login-container {
    flex-direction: column;
    align-items: stretch;
  }

  .auth-hero {
    max-width: none;
    text-align: center;
  }

  .login-card {
    width: 100%;
    max-width: 520px;
  }
}
</style>
