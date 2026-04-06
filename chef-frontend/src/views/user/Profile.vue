<template>
  <div class="profile-page">
    <div class="profile-hero" v-loading="loading">
      <div class="hero-main">
        <el-upload
          :action="''"
          :http-request="handleAvatarUpload"
          :show-file-list="false"
          :before-upload="beforeAvatarUpload"
        >
          <el-avatar :size="92" :src="profileForm.avatar" v-if="profileForm.avatar" class="hero-avatar" />
          <el-icon v-else class="avatar-uploader-icon" :size="56"><Plus /></el-icon>
        </el-upload>
        <el-button v-if="profileForm.avatar" link type="primary" @click="openAvatarPreview">
          查看头像
        </el-button>
        <div class="hero-copy">
          <div class="hero-eyebrow">PERSONAL CENTER</div>
          <h2>{{ profileForm.username || '个人中心' }}</h2>
        </div>
      </div>
      <div class="hero-stats">
        <div class="stat-card">
          <span class="stat-label">手机号</span>
          <span class="stat-value">{{ profileForm.phone || '未填写' }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">账号类型</span>
          <span class="stat-value">普通用户</span>
        </div>
      </div>
    </div>

    <div class="profile-grid">
      <el-card class="profile-panel" shadow="hover">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">资料维护</div>
            </div>
          </div>
        </template>

        <el-form
          ref="profileFormRef"
          :model="profileForm"
          :rules="profileRules"
          label-width="96px"
          class="panel-form"
          v-loading="loading"
        >
          <el-form-item label="头像">
            <div class="avatar-row">
              <el-upload
                :action="''"
                :http-request="handleAvatarUpload"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
              >
                <el-avatar :size="84" :src="profileForm.avatar" v-if="profileForm.avatar" />
                <el-icon v-else class="avatar-uploader-icon small-uploader" :size="42"><Plus /></el-icon>
              </el-upload>
              <div class="avatar-tip">
                <div class="avatar-tip-title">建议上传正方形头像</div>
                <div class="avatar-tip-desc">支持 JPG/PNG，大小不超过 2MB。</div>
                <el-button v-if="profileForm.avatar" link type="primary" @click="openAvatarPreview">
                  查看大图
                </el-button>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="用户名">
            <el-input v-model="profileForm.username" disabled />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="profileForm.phone"
              placeholder="请输入本人常用的 11 位手机号"
              maxlength="11"
              clearable
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleUpdateProfile" :loading="updating">
              保存资料
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="profile-panel security-panel" shadow="hover">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">账号安全</div>
            </div>
          </div>
        </template>

        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="96px"
          class="panel-form"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入当前登录密码"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入 6 到 20 位新密码"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码确认"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <el-dialog
      v-model="avatarPreviewVisible"
      width="520px"
      append-to-body
      class="image-preview-dialog"
      destroy-on-close
    >
      <div class="image-preview-wrap">
        <img v-if="profileForm.avatar" :src="profileForm.avatar" alt="头像预览" class="preview-image avatar-preview-image" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getProfile, updateProfile, changePassword } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { uploadFile } from '@/utils/minio'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const updating = ref(false)
const changingPassword = ref(false)
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const avatarPreviewVisible = ref(false)

const profileForm = reactive({
  username: '',
  avatar: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const profileRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6 到 20 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const patchHeaderUserInfo = () => {
  userStore.patchUserInfo({
    username: profileForm.username || userStore.userInfo.username,
    avatar: profileForm.avatar || ''
  })
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleAvatarUpload = async ({ file }) => {
  try {
    const url = await uploadFile(file, 'avatars')
    profileForm.avatar = url
    patchHeaderUserInfo()
    ElMessage.success('头像上传成功')
  } catch (error) {
    ElMessage.error('头像上传失败')
  }
}

const openAvatarPreview = () => {
  if (!profileForm.avatar) return
  avatarPreviewVisible.value = true
}

const loadProfile = async () => {
  loading.value = true
  try {
    const res = await getProfile()
    Object.assign(profileForm, {
      username: res.data.username,
      avatar: res.data.avatar === '-' ? '' : res.data.avatar,
      phone: res.data.phone === '-' ? '' : res.data.phone
    })
    patchHeaderUserInfo()
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return
  
  await profileFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    updating.value = true
    try {
      await updateProfile(profileForm)
      patchHeaderUserInfo()
      ElMessage.success('保存成功')
      loadProfile()
    } catch (error) {
      console.error('保存失败:', error)
    } finally {
      updating.value = false
    }
  })
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    changingPassword.value = true
    try {
      await changePassword(passwordForm)
      ElMessage.success('密码修改成功，请重新登录')
      
      // 清空表单
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      
      // 延迟跳转到登录页
      setTimeout(() => {
        userStore.clearUserInfo()
        window.location.href = '/login'
      }, 1500)
    } catch (error) {
      console.error('修改失败:', error)
    } finally {
      changingPassword.value = false
    }
  })
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-page {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(260px, 0.9fr);
  gap: 20px;
  padding: 28px 32px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top right, rgba(64, 158, 255, 0.20), transparent 35%),
    linear-gradient(135deg, #ffffff 0%, #f4f8ff 100%);
  box-shadow: 0 18px 40px rgba(38, 66, 122, 0.10);
}

.hero-main {
  display: flex;
  align-items: center;
  gap: 20px;
}

.hero-avatar {
  box-shadow: 0 12px 28px rgba(64, 158, 255, 0.22);
}

.hero-copy h2 {
  margin: 8px 0 0;
  font-size: 30px;
  line-height: 1.2;
  color: #1f2a44;
}

.hero-eyebrow {
  font-size: 12px;
  letter-spacing: 0.18em;
  color: #6ea8ff;
  font-weight: 700;
}

.hero-stats {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
}

.stat-card {
  padding: 18px 20px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(64, 158, 255, 0.12);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-label {
  font-size: 13px;
  color: #8a94a6;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #1f2a44;
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(360px, 0.9fr);
  gap: 24px;
}

.profile-panel {
  border-radius: 22px;
  box-shadow: 0 14px 36px rgba(15, 23, 42, 0.08);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2a44;
}

.panel-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #8a94a6;
}

.panel-form {
  padding-top: 4px;
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-tip-title {
  font-weight: 600;
  color: #1f2a44;
}

.avatar-tip-desc {
  margin-top: 4px;
  font-size: 13px;
  color: #8a94a6;
}

.avatar-uploader-icon {
  border: 1px dashed #d9d9d9;
  border-radius: 22px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  width: 92px;
  height: 92px;
  background: linear-gradient(135deg, #ffffff 0%, #eef5ff 100%);
}

.avatar-uploader-icon:hover {
  border-color: #409EFF;
}

.small-uploader {
  width: 84px;
  height: 84px;
}

.image-preview-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 280px;
}

.preview-image {
  max-width: 100%;
  max-height: 70vh;
  border-radius: 14px;
  object-fit: contain;
}

.avatar-preview-image {
  max-width: 320px;
  max-height: 320px;
}

@media (max-width: 1200px) {
  .profile-hero,
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
