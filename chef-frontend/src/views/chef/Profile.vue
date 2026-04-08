<template>
  <div class="chef-profile-page">
    <div class="chef-hero" v-loading="loading">
      <div class="hero-main">
        <div class="hero-copy">
          <div class="hero-eyebrow">CHEF PROFILE</div>
          <h2>{{ profileForm.realName || userStore.userInfo.username || '厨师个人资料' }}</h2>
          <div class="hero-meta">
            <el-tag v-if="profile.auditStatusDesc" :type="getAuditStatusType(profile.auditStatus)" effect="light">
              {{ profile.auditStatusDesc }}
            </el-tag>
            <span v-if="profileForm.serviceArea">{{ profileForm.serviceArea }}</span>
          </div>
        </div>
      </div>
      <div class="hero-stats">
        <div class="stat-card">
          <span class="stat-label">价格</span>
          <span class="stat-value">{{ Number(profileForm.price || 0).toFixed(0) }} 元/小时</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">服务人数</span>
          <span class="stat-value">{{ profileForm.minPeople }} - {{ profileForm.maxPeople }} 人</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">从业年限</span>
          <span class="stat-value">{{ profileForm.workYears }} 年</span>
        </div>
      </div>
    </div>

    <div class="profile-grid">
      <el-card class="profile-panel" shadow="hover">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">基础资料</div>
              <div class="panel-subtitle">填写实名认证与从业信息，作为资料展示和审核依据。</div>
            </div>
          </div>
        </template>

        <el-form
          ref="profileFormRef"
          :model="profileForm"
          :rules="profileRules"
          label-width="112px"
          class="panel-form"
          v-loading="loading"
        >
          <el-form-item label="真实姓名" prop="realName">
            <el-input
              v-model="profileForm.realName"
              placeholder="请输入真实姓名，2 到 20 个字"
              maxlength="20"
              show-word-limit
            />
          </el-form-item>

          <div class="inline-grid two-col">
            <el-form-item label="年龄(岁)" prop="age">
              <el-input-number v-model="profileForm.age" :min="18" :max="70" :precision="0" :step="1" />
            </el-form-item>
          </div>

          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="profileForm.gender">
              <el-radio
                v-for="item in genderOptions"
                :key="item.value"
                :label="item.value"
              >
                {{ item.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>

          <div class="inline-grid two-col">
            <el-form-item label="从业年限(年)" prop="workYears">
              <el-input-number v-model="profileForm.workYears" :min="1" :max="50" :precision="0" :step="1" />
            </el-form-item>
          </div>
        </el-form>
      </el-card>

      <el-card class="profile-panel" shadow="hover">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">服务信息</div>
              <div class="panel-subtitle">设置菜系、服务区域、定价和可服务人数范围。</div>
            </div>
          </div>
        </template>

        <el-form
          :model="profileForm"
          :rules="profileRules"
          label-width="112px"
          class="panel-form"
        >
          <el-form-item label="擅长菜系" prop="cuisineType">
            <el-select
              v-model="profileForm.cuisineType"
              multiple
              placeholder="请选择擅长菜系，可多选"
              style="width: 100%"
            >
              <el-option
                v-for="item in cuisineOptions"
                :key="item.value"
                :label="item.label"
                :value="String(item.value)"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="服务区域" prop="serviceArea">
            <el-input
              v-model="profileForm.serviceArea"
              placeholder="请输入主要服务区域，如北京市朝阳区"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="服务描述" prop="serviceDesc">
            <el-input
              v-model="profileForm.serviceDesc"
              type="textarea"
              :rows="5"
              placeholder="请介绍擅长风格、上门经验、可提供的服务内容"
              maxlength="300"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="价格" prop="price">
            <div class="unit-input-wrap">
              <el-input-number v-model="profileForm.price" :min="0.01" :precision="2" :step="0.1" />
              <span class="unit-text">元/小时</span>
            </div>
          </el-form-item>

          <el-form-item label="服务人数" prop="minPeople">
            <div class="people-range-wrap">
              <el-input-number v-model="profileForm.minPeople" :min="1" :precision="0" :step="1" />
              <span class="range-separator">-</span>
              <el-input-number v-model="profileForm.maxPeople" :min="1" :precision="0" :step="1" />
              <span class="unit-text">人</span>
            </div>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="profile-panel certificates-panel" shadow="hover">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">证件资料</div>
              <div class="panel-subtitle">上传完整证件资料后再提交审核，审核通过后可接单。</div>
            </div>
          </div>
        </template>

        <el-form
          :model="profileForm"
          :rules="profileRules"
          label-width="112px"
          class="panel-form"
        >
          <el-form-item label="身份证照片" prop="idCardImgs">
            <ImageUpload v-model="profileForm.idCardImgs" :limit="5" />
          </el-form-item>

          <el-form-item label="健康证照片" prop="healthCertImgs">
            <ImageUpload v-model="profileForm.healthCertImgs" :limit="5" />
          </el-form-item>

          <el-form-item label="厨师证照片" prop="chefCertImgs">
            <ImageUpload v-model="profileForm.chefCertImgs" :limit="5" />
          </el-form-item>

          <el-form-item class="submit-row">
            <el-button type="primary" @click="handleSave" :loading="saving">
              保存资料
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, saveProfile } from '@/api/chef'
import { getCuisineTypeOptions, getGenderOptions } from '@/api/constant'
import { useUserStore } from '@/stores/user'
import ImageUpload from '@/components/ImageUpload.vue'

const userStore = useUserStore()
const profileFormRef = ref(null)
const loading = ref(false)
const saving = ref(false)
const profile = ref({})
const cuisineOptions = ref([])
const genderOptions = ref([])

const profileForm = reactive({
  realName: '',
  age: 25,
  gender: 1,
  workYears: 0,
  cuisineType: [],
  serviceArea: '',
  serviceDesc: '',
  price: 100,
  minPeople: 1,
  maxPeople: 10,
  idCardImgs: [],
  healthCertImgs: [],
  chefCertImgs: []
})

const profileRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '真实姓名长度为 2 到 20 个字符', trigger: 'blur' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value == null || !Number.isInteger(Number(value)) || Number(value) <= 0) {
          callback(new Error('年龄必须为正整数'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  workYears: [
    { required: true, message: '请输入从业年限', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value == null || !Number.isInteger(Number(value)) || Number(value) <= 0) {
          callback(new Error('从业年限必须为正整数'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  cuisineType: [{ required: true, message: '请选择擅长菜系', trigger: 'change' }],
  serviceArea: [{ required: true, message: '请输入服务区域', trigger: 'blur' }],
  serviceDesc: [{ required: true, message: '请输入服务描述', trigger: 'blur' }],
  idCardImgs: [
    {
      required: true,
      validator: (_, value, callback) => {
        if (!Array.isArray(value) || value.length === 0) {
          callback(new Error('身份证照片必填，请至少上传一张'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  healthCertImgs: [
    {
      required: true,
      validator: (_, value, callback) => {
        if (!Array.isArray(value) || value.length === 0) {
          callback(new Error('健康证照片必填，请至少上传一张'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  chefCertImgs: [
    {
      required: true,
      validator: (_, value, callback) => {
        if (!Array.isArray(value) || value.length === 0) {
          callback(new Error('厨师证照片必填，请至少上传一张'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  minPeople: [
    { required: true, message: '请输入最少服务人数', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value == null || !Number.isInteger(Number(value)) || Number(value) <= 0) {
          callback(new Error('最少服务人数必须为正整数'))
          return
        }
        if (profileForm.maxPeople != null && Number(value) > Number(profileForm.maxPeople)) {
          callback(new Error('最少服务人数不能大于最多服务人数'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  maxPeople: [
    { required: true, message: '请输入最多服务人数', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value == null || !Number.isInteger(Number(value)) || Number(value) <= 0) {
          callback(new Error('最多服务人数必须为正整数'))
          return
        }
        if (profileForm.minPeople != null && Number(value) < Number(profileForm.minPeople)) {
          callback(new Error('最多服务人数不能小于最少服务人数'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value == null || Number.isNaN(Number(value))) {
          callback(new Error('请输入正确的价格，单位为元/小时'))
          return
        }
        if (Number(value) <= 0) {
          callback(new Error('价格必须为正数，单位为元/小时'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const getAuditStatusType = (status) => {
  const map = {
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return map[status] || 'info'
}

const loadProfile = async () => {
  loading.value = true
  try {
    const res = await getProfile()
    if (res.data) {
      profile.value = res.data
      Object.assign(profileForm, {
        ...res.data,
        cuisineType: Array.isArray(res.data.cuisineType) ? res.data.cuisineType.map(item => String(item)) : [],
        idCardImgs: Array.isArray(res.data.idCardImgs) ? res.data.idCardImgs : [],
        healthCertImgs: Array.isArray(res.data.healthCertImgs) ? res.data.healthCertImgs : [],
        chefCertImgs: Array.isArray(res.data.chefCertImgs) ? res.data.chefCertImgs : [],
        gender: res.data.gender || 1,
        price: res.data.price ? res.data.price / 100 : 100
      })
    }
  } catch (error) {
    console.error('加载资料失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!profileFormRef.value) return

  await profileFormRef.value.validate(async (valid) => {
    if (!valid) return

    saving.value = true
    try {
      await saveProfile({
        ...profileForm,
        cuisineType: profileForm.cuisineType.map(item => Number(item)),
        price: String(profileForm.price)
      })

      ElMessage.success('保存成功')
      loadProfile()
    } catch (error) {
      console.error('保存失败:', error)
    } finally {
      saving.value = false
    }
  })
}

const handleReset = () => {
  loadProfile()
}

onMounted(() => {
  Promise.all([getCuisineTypeOptions(), getGenderOptions()])
    .then(([cuisineRes, genderRes]) => {
      cuisineOptions.value = Array.isArray(cuisineRes.data) ? cuisineRes.data : []
      genderOptions.value = Array.isArray(genderRes.data) ? genderRes.data : []
    })
    .catch(error => {
      console.error('加载厨师资料枚举失败:', error)
    })
  loadProfile()
})
</script>

<style scoped>
.chef-profile-page {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.chef-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(320px, 1fr);
  gap: 22px;
  padding: 30px 34px;
  border-radius: 26px;
  background:
    radial-gradient(circle at top right, rgba(103, 194, 58, 0.18), transparent 35%),
    linear-gradient(135deg, #ffffff 0%, #f5fff1 100%);
  box-shadow: 0 20px 42px rgba(24, 49, 27, 0.10);
}

.hero-main {
  display: flex;
  align-items: center;
  gap: 22px;
}

.hero-copy h2 {
  margin: 10px 0;
  font-size: 32px;
  line-height: 1.2;
  color: #1b2a1f;
}

.hero-eyebrow {
  font-size: 12px;
  letter-spacing: 0.18em;
  color: #77b255;
  font-weight: 700;
}

.hero-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #68756c;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.stat-card {
  min-height: 110px;
  padding: 18px 20px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(103, 194, 58, 0.14);
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}

.stat-label {
  font-size: 13px;
  color: #8c988f;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #233528;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
}

.certificates-panel {
  grid-column: 1 / -1;
}

.profile-panel {
  border-radius: 24px;
  box-shadow: 0 16px 36px rgba(17, 24, 39, 0.08);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
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

.inline-grid {
  display: grid;
  gap: 0 16px;
}

.two-col {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.unit-input-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.people-range-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.range-separator,
.unit-text {
  color: #606266;
  white-space: nowrap;
}

.submit-row :deep(.el-form-item__content) {
  justify-content: center;
}

@media (max-width: 1280px) {
  .chef-hero,
  .profile-grid,
  .two-col {
    grid-template-columns: 1fr;
  }

  .hero-stats {
    grid-template-columns: 1fr;
  }
}
</style>
