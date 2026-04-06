<template>
  <div class="login-container">
    <div class="auth-hero">
      <div class="hero-eyebrow">PRIVATE CHEF PLATFORM</div>
      <h1>厨师预约系统</h1>
      <p class="hero-text">从预约、接单到后台治理，把用户端、厨师端和管理端放到一套清晰的工作流里。</p>
      <div class="hero-points">
        <div class="hero-point">
          <strong>用户端</strong>
          <span>搜索私厨、下单预约、追踪通知</span>
        </div>
        <div class="hero-point">
          <strong>厨师端</strong>
          <span>管理资料、开放档期、处理订单</span>
        </div>
        <div class="hero-point">
          <strong>管理端</strong>
          <span>审核、治理与订单数据总览</span>
        </div>
      </div>
    </div>
    <el-card class="login-card glass-panel" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="hero-kicker">欢迎回来</span>
          <h2>欢迎登录</h2>
          <p>请选择身份后进入对应工作台</p>
        </div>
      </template>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="80px"
        class="auth-form"
      >
        <el-form-item label="角色" prop="role">
          <el-select v-model="loginForm.role" placeholder="请选择登录身份" style="width: 100%">
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
            placeholder="请输入你的用户名"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入登录密码"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            class="primary-submit"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
        
        <el-form-item>
          <el-button
            class="secondary-submit"
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
    radial-gradient(circle at top left, rgba(248, 113, 113, 0.22), transparent 28%),
    radial-gradient(circle at bottom left, rgba(245, 158, 11, 0.18), transparent 22%),
    linear-gradient(135deg, #fff8f5 0%, #fffdf9 44%, #fff2ea 100%);
}

.auth-hero {
  max-width: 420px;
}

.hero-eyebrow {
  font-size: 12px;
  letter-spacing: 0.2em;
  font-weight: 700;
  color: #b45309;
}

.auth-hero h1 {
  margin: 12px 0 16px;
  font-family: var(--font-display);
  font-size: 46px;
  line-height: 1.1;
  color: #3f1111;
}

.hero-text {
  margin: 0 0 24px;
  color: #795c55;
  line-height: 1.8;
}

.hero-points {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.hero-point {
  padding: 16px 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.58);
  border: 1px solid rgba(220, 38, 38, 0.08);
  box-shadow: 0 16px 30px rgba(127, 29, 29, 0.07);
}

.hero-point strong {
  display: block;
  margin-bottom: 6px;
  color: #4b2d2d;
}

.hero-point span {
  color: #8a6a62;
  font-size: 14px;
}

.login-card {
  width: 450px;
  border: none;
  border-radius: 28px;
  box-shadow: 0 24px 52px rgba(127, 29, 29, 0.12);
}

.card-header h2 {
  margin: 10px 0 0;
  text-align: center;
  color: #3f1111;
}

.card-header p {
  margin: 8px 0 0;
  text-align: center;
  color: #8a6a62;
}

.auth-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.auth-form :deep(.el-input__wrapper),
.auth-form :deep(.el-select__wrapper) {
  min-height: 48px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
}

.primary-submit,
.secondary-submit {
  width: 100%;
}

.primary-submit {
  border: none;
  background: linear-gradient(135deg, #dc2626, #ea580c);
}

.secondary-submit {
  border-color: rgba(220, 38, 38, 0.14);
  color: #7f1d1d;
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
