<template>
  <div class="register-container">
    <div class="auth-hero">
      <div class="hero-eyebrow">JOIN THE PLATFORM</div>
      <h1>创建新账号</h1>
      <p>选择角色后完成注册，普通用户可预约下单，厨师可提交资料入驻。</p>
    </div>
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>账号注册</h2>
          <p>请填写基础信息，注册成功后即可登录</p>
        </div>
      </template>
      
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="100px"
      >
        <el-form-item label="角色" prop="role">
          <el-select v-model="registerForm.role" placeholder="请选择角色" style="width: 100%">
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
            placeholder="请输入3-20位用户名"
            maxlength="20"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入11位手机号"
            maxlength="11"
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
        
        <el-form-item>
          <el-button
            style="width: 100%"
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
    radial-gradient(circle at top right, rgba(103, 194, 58, 0.20), transparent 28%),
    linear-gradient(135deg, #f7fcf5 0%, #eef8eb 42%, #f8fcf8 100%);
}

.auth-hero {
  max-width: 420px;
}

.hero-eyebrow {
  font-size: 12px;
  letter-spacing: 0.2em;
  font-weight: 700;
  color: #7eb067;
}

.auth-hero h1 {
  margin: 12px 0 16px;
  font-size: 42px;
  line-height: 1.12;
  color: #203022;
}

.auth-hero p {
  margin: 0;
  color: #617165;
  line-height: 1.9;
}

.register-card {
  width: 500px;
  border-radius: 24px;
  box-shadow: 0 20px 48px rgba(61, 96, 59, 0.14);
}

.card-header h2 {
  margin: 0;
  text-align: center;
  color: #203022;
}

.card-header p {
  margin: 8px 0 0;
  text-align: center;
  color: #8a948e;
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
