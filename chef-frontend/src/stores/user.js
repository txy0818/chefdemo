import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // 设置 token
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 设置用户信息
  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  // 合并更新用户信息
  function patchUserInfo(info) {
    const mergedInfo = {
      ...userInfo.value,
      ...info
    }
    userInfo.value = mergedInfo
    localStorage.setItem('userInfo', JSON.stringify(mergedInfo))
  }

  // 清除用户信息
  function clearUserInfo() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 判断是否登录
  function isLoggedIn() {
    return !!token.value
  }

  // 获取用户角色
  function getUserRole() {
    return userInfo.value.role || 0
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    patchUserInfo,
    clearUserInfo,
    isLoggedIn,
    getUserRole
  }
})
