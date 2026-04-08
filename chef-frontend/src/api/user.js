import request from '@/utils/request'

// page1: 搜索厨师列表
export function searchChef(data) {
  return request({
    url: '/user/chef/search',
    method: 'post',
    data
  })
}

// page2: 查询厨师详情
export function chefDetail(data) {
  return request({
    url: '/user/chef/detail',
    method: 'post',
    data
  })
}

export function chefTimes(data) {
  return request({
    url: '/user/chef/times',
    method: 'post',
    data
  })
}

export function chefReviews(data) {
  return request({
    url: '/user/chef/reviews',
    method: 'post',
    data
  })
}

// page3: 创建订单
export function createOrder(data) {
  return request({
    url: '/user/order/create',
    method: 'post',
    data
  })
}

// page4: 查询用户订单列表
export function orderList(data) {
  return request({
    url: '/user/order/list',
    method: 'post',
    data
  })
}

// page5: 查询订单详情
export function orderDetail(data) {
  return request({
    url: '/user/order/detail',
    method: 'post',
    data
  })
}

// page5: 支付订单
export function payOrder(data) {
  return request({
    url: '/user/order/pay',
    method: 'post',
    data
  })
}

// page5: 取消订单
export function cancelOrder(data) {
  return request({
    url: '/user/order/cancel',
    method: 'post',
    data
  })
}

// page6: 创建评论
export function createReview(data) {
  return request({
    url: '/user/review/create',
    method: 'post',
    data
  })
}

// page7: 创建举报
export function createReport(data) {
  return request({
    url: '/user/report/create',
    method: 'post',
    data
  })
}

// page8: 查询用户资料
export function getProfile() {
  return request({
    url: '/profile/queryInfo',
    method: 'post'
  })
}

// page8: 更新用户资料
export function updateProfile(data) {
  return request({
    url: '/profile/updateUserInfo',
    method: 'post',
    data
  })
}

// page8: 修改密码
export function changePassword(data) {
  return request({
    url: '/profile/updatePassword',
    method: 'post',
    data
  })
}

// page9: 查询通知列表
export function notificationList(data) {
  return request({
    url: '/profile/notification/list',
    method: 'post',
    data
  })
}

// page9: 标记通知已读
export function readNotification(data) {
  return request({
    url: '/profile/notification/read',
    method: 'post',
    data
  })
}

// page10: 查询钱包余额
export function getWalletBalance() {
  return request({
    url: '/user/wallet/balance',
    method: 'post'
  })
}

// page10: 分页查询钱包流水
export function getWalletRecords(data) {
  return request({
    url: '/user/wallet/records',
    method: 'post',
    data
  })
}

// page10: 模拟充值
export function rechargeWallet(data) {
  return request({
    url: '/user/wallet/recharge',
    method: 'post',
    data
  })
}
