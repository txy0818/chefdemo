<template>
  <div class="register-container">
    <div class="auth-hero">
      <div class="hero-eyebrow">JOIN THE PLATFORM</div>
      <h1>创建新账号</h1>
      <p>选择角色后完成注册，普通用户可预约下单，厨师可提交资料入驻。</p>
      <div class="hero-points">
        <div class="hero-point">
          <strong>普通用户</strong>
          <span>注册后即可搜索厨师、下单预约并接收实时通知。</span>
        </div>
        <div class="hero-point">
          <strong>厨师入驻</strong>
          <span>注册后完善资料和证件，审核通过后开放可预约时间段。</span>
        </div>
      </div>
    </div>
    <el-card class="register-card glass-panel" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="hero-kicker">快速入驻</span>
          <h2>账号注册</h2>
          <p>请填写基础信息，注册成功后即可登录</p>
        </div>
      </template>
      
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="100px"
        class="auth-form"
      >
        <el-form-item label="角色" prop="role">
          <el-select v-model="registerForm.role" placeholder="请选择注册身份" style="width: 100%">
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
            v-model="registerForm.username"
            placeholder="请输入 3 到 20 位用户名"
            maxlength="20"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入不少于 6 位的登录密码"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入登录密码"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入本人常用的 11 位手机号"
            maxlength="11"
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            class="primary-submit"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
        
        <el-form-item>
          <el-button
            class="secondary-submit"
            @click="goToLogin"
          >
            已有账号？去登录
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
import { register } from '@/api/auth'
import { getUserRoleOptions } from '@/api/constant'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)
const roleOptions = ref([])

const registerForm = reactive({
  role: 3,
  username: '',
  password: '',
  confirmPassword: '',
  phone: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await register({
        userRole: registerForm.role,
        username: registerForm.username,
        phone: registerForm.phone,
        password: registerForm.password
      })
      
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } catch (error) {
      console.error('注册失败:', error)
    } finally {
      loading.value = false
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}

const loadRoleOptions = async () => {
  try {
    const res = await getUserRoleOptions()
    roleOptions.value = (Array.isArray(res.data) ? res.data : []).filter(item => item.value !== 1)
  } catch (error) {
    console.error('加载角色枚举失败:', error)
  }
}

onMounted(() => {
  loadRoleOptions()
})
</script>

<style scoped>
.register-container {
  display: flex;
  gap: 48px;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 24px;
  background:
    radial-gradient(circle at top right, rgba(74, 222, 128, 0.18), transparent 24%),
    radial-gradient(circle at bottom left, rgba(250, 204, 21, 0.16), transparent 22%),
    linear-gradient(135deg, #f7fff7 0%, #fbfff6 42%, #eef9eb 100%);
}

.auth-hero {
  max-width: 420px;
}

.hero-eyebrow {
  font-size: 12px;
  letter-spacing: 0.2em;
  font-weight: 700;
  color: #4d7c0f;
}

.auth-hero h1 {
  margin: 12px 0 16px;
  font-family: var(--font-display);
  font-size: 44px;
  line-height: 1.12;
  color: #203022;
}

.auth-hero p {
  margin: 0 0 24px;
  color: #617165;
  line-height: 1.9;
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
  border: 1px solid rgba(34, 197, 94, 0.08);
  box-shadow: 0 16px 30px rgba(34, 84, 47, 0.08);
}

.hero-point strong {
  display: block;
  margin-bottom: 6px;
  color: #203022;
}

.hero-point span {
  color: #6b7c6d;
  font-size: 14px;
}

.register-card {
  width: 500px;
  border: none;
  border-radius: 28px;
  box-shadow: 0 24px 52px rgba(61, 96, 59, 0.14);
}

.card-header h2 {
  margin: 10px 0 0;
  text-align: center;
  color: #203022;
}

.card-header p {
  margin: 8px 0 0;
  text-align: center;
  color: #8a948e;
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
  background: linear-gradient(135deg, #22c55e, #65a30d);
}

.secondary-submit {
  border-color: rgba(34, 197, 94, 0.18);
  color: #166534;
}

@media (max-width: 960px) {
  .register-container {
    flex-direction: column;
    align-items: stretch;
  }

  .auth-hero {
    max-width: none;
    text-align: center;
  }

  .register-card {
    width: 100%;
    max-width: 560px;
  }
}
</style>
