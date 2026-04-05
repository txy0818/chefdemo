<template>
  <div class="image-upload">
    <el-upload
      :action="''"
      :http-request="handleUpload"
      :file-list="fileList"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
      :before-upload="beforeUpload"
      :limit="limit"
      :multiple="multiple"
      list-type="picture-card"
      :disabled="disabled"
    >
      <el-icon><Plus /></el-icon>
    </el-upload>
    
    <el-dialog
      v-model="previewVisible"
      width="720px"
      append-to-body
      class="image-preview-dialog"
      destroy-on-close
    >
      <div class="image-preview-wrap">
        <img :src="previewUrl" class="preview-image" alt="图片预览" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { uploadFile } from '@/utils/minio'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  limit: {
    type: Number,
    default: 5
  },
  multiple: {
    type: Boolean,
    default: true
  },
  disabled: {
    type: Boolean,
    default: false
  },
  maxSize: {
    type: Number,
    default: 5 // MB
  }
})

const emit = defineEmits(['update:modelValue'])

const fileList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')

// 监听 modelValue 变化，更新 fileList
watch(() => props.modelValue, (newVal) => {
  fileList.value = newVal.map((url, index) => ({
    uid: index,
    name: `image-${index}`,
    url: url
  }))
}, { immediate: true })

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < props.maxSize
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB!`)
    return false
  }
  return true
}

const handleUpload = async ({ file }) => {
  try {
    const url = await uploadFile(file, 'chef-images')
    
    // 更新文件列表
    fileList.value.push({
      uid: Date.now(),
      name: file.name,
      url: url
    })
    
    // 触发更新
    emitUpdate()
    
    ElMessage.success('上传成功')
  } catch (error) {
    ElMessage.error('上传失败: ' + error.message)
  }
}

const handleRemove = (file) => {
  const index = fileList.value.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
    emitUpdate()
  }
}

const handlePreview = (file) => {
  previewUrl.value = file.url
  previewVisible.value = true
}

const emitUpdate = () => {
  const urls = fileList.value.map(item => item.url)
  emit('update:modelValue', urls)
}
</script>

<style scoped>
.image-upload {
  width: 100%;
}

.image-preview-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 320px;
}

.preview-image {
  max-width: 100%;
  max-height: 70vh;
  border-radius: 12px;
  object-fit: contain;
}
</style>
