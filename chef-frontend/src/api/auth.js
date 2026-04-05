import request from '@/utils/request'

// 注册
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 登出
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

// 查询当前用户基本资料
export function queryInfo() {
  return request({
    url: '/profile/queryInfo',
    method: 'post'
  })
}
