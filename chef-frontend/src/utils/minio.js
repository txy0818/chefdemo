import axios from 'axios'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

async function getPresignedUpload(file, folder) {
  return request({
    url: '/file/presigned/upload',
    method: 'post',
    data: {
      folder,
      fileName: file.name,
      contentType: file.type || 'application/octet-stream'
    }
  })
}

export async function uploadFile(file, folder = 'images') {
  try {
    const res = await getPresignedUpload(file, folder)
    const uploadUrl = res.data.uploadUrl
    const fileUrl = res.data.fileUrl

    await axios.put(uploadUrl, file, {
      headers: {
        'Content-Type': file.type || 'application/octet-stream'
      }
    })

    return fileUrl
  } catch (error) {
    console.error('文件上传失败:', error)
    ElMessage.error(error.response?.data?.baseResp?.desc || error.message || '文件上传失败，请重试')
    throw error
  }
}

export async function uploadFiles(files, folder = 'images') {
  const uploadPromises = Array.from(files).map(file => uploadFile(file, folder))
  return Promise.all(uploadPromises)
}

export async function uploadImage(file, folder = 'images', maxSize = 5) {
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('只支持上传 JPG、PNG、GIF、WEBP 格式的图片')
    throw new Error('不支持的图片格式')
  }

  const maxSizeBytes = maxSize * 1024 * 1024
  if (file.size > maxSizeBytes) {
    ElMessage.error(`图片大小不能超过 ${maxSize}MB`)
    throw new Error('图片大小超出限制')
  }

  return uploadFile(file, folder)
}

export async function uploadImages(files, folder = 'images', maxSize = 5) {
  const uploadPromises = Array.from(files).map(file => uploadImage(file, folder, maxSize))
  return Promise.all(uploadPromises)
}

export async function initMinIO() {
  return true
}

export default {
  uploadFile,
  uploadFiles,
  uploadImage,
  uploadImages,
  initMinIO
}
