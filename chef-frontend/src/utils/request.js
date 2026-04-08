import axios from 'axios'
import { ElMessage } from 'element-plus'
import config from '@/config'

let authRedirecting = false

function isPlainObject(value) {
  return Object.prototype.toString.call(value) === '[object Object]'
}

function sanitizePayload(value) {
  if (value === null || value === undefined || value === '') {
    return undefined
  }
  if (Array.isArray(value)) {
    return value.map(item => sanitizePayload(item)).filter(item => item !== undefined)
  }
  if (isPlainObject(value)) {
    return Object.entries(value).reduce((acc, [key, val]) => {
      const sanitizedValue = sanitizePayload(val)
      if (sanitizedValue !== undefined) {
        acc[key] = sanitizedValue
      }
      return acc
    }, {})
  }
  return value
}

function handleAuthExpired(message) {
  if (authRedirecting) {
    return
  }
  authRedirecting = true
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  ElMessage.error(message || '登录状态已失效，请重新登录')
  window.setTimeout(() => {
    if (window.location.pathname !== '/login') {
      window.location.replace('/login')
    }
    authRedirecting = false
  }, 150)
}

// 创建 axios 实例
const request = axios.create({
  baseURL: config.apiBaseUrl,
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    if (config.data && isPlainObject(config.data)) {
      config.data = sanitizePayload(config.data)
    }
    if (config.params && isPlainObject(config.params)) {
      config.params = sanitizePayload(config.params)
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 后端成功码是 1，其他都是错误
    // 如果有 baseResp，检查 baseResp.code
    const code = res.baseResp ? res.baseResp.code : res.code
    const message = res.baseResp ? res.baseResp.desc : res.msg
    
    if (code !== 1) {
      if (code === 11) {
        handleAuthExpired(message)
        return Promise.reject(new Error(message || '登录状态已失效'))
      }
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误:', error)
    const responseData = error.response?.data
    const message = responseData?.baseResp?.desc || responseData?.msg || error.message || '网络错误'
    if (error.response?.status === 401 || responseData?.baseResp?.code === 11 || responseData?.code === 11) {
      handleAuthExpired(message)
      return Promise.reject(error)
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
