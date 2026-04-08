import request from '@/utils/request'

// page1: 查询厨师资料
export function getProfile() {
  return request({
    url: '/chef/profile/get',
    method: 'post'
  })
}

export function getOfficialProfile() {
  return request({
    url: '/chef/profile/official/get',
    method: 'post'
  })
}

// page1: 保存厨师资料
export function saveProfile(data) {
  return request({
    url: '/chef/profile/save',
    method: 'post',
    data
  })
}

// page2: 新增可预约时间段
export function addAvailableTime(data) {
  return request({
    url: '/chef/time/add',
    method: 'post',
    data
  })
}

// page2: 查询可预约时间段列表
export function listAvailableTimes(data) {
  return request({
    url: '/chef/time/list',
    method: 'post',
    data
  })
}

// page2: 删除可预约时间段
export function deleteAvailableTime(data) {
  return request({
    url: '/chef/time/delete',
    method: 'post',
    data
  })
}

// page3: 查询厨师订单列表
export function orderList(data) {
  return request({
    url: '/chef/order/list',
    method: 'post',
    data
  })
}

// page3: 查询订单详情
export function orderDetail(data) {
  return request({
    url: '/chef/order/detail',
    method: 'post',
    data
  })
}

// page3: 接单
export function acceptOrder(data) {
  return request({
    url: '/chef/order/accept',
    method: 'post',
    data
  })
}

// page3: 拒单
export function rejectOrder(data) {
  return request({
    url: '/chef/order/reject',
    method: 'post',
    data
  })
}

// page3: 完成订单
export function completeOrder(data) {
  return request({
    url: '/chef/order/complete',
    method: 'post',
    data
  })
}

// 通知列表
export function notificationList(data) {
  return request({
    url: '/profile/notification/list',
    method: 'post',
    data
  })
}

// 标记通知已读
export function readNotification(data) {
  return request({
    url: '/profile/notification/read',
    method: 'post',
    data
  })
}
