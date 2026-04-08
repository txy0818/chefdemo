import request from '@/utils/request'

// tab1: 查询厨师列表
export function queryChefList(data) {
  return request({
    url: '/admin/chef/list',
    method: 'post',
    data
  })
}

// tab2: 查询用户列表
export function queryUserList(data) {
  return request({
    url: '/admin/user/list',
    method: 'post',
    data
  })
}

// tab2: 更新用户状态（冻结/正常）
export function updateUserStatus(data) {
  return request({
    url: '/admin/user/status/updateUserStatus',
    method: 'post',
    data
  })
}

// tab3: 查询待审核厨师
export function queryAuditChef(data) {
  return request({
    url: '/admin/chef/queryAuditChef',
    method: 'post',
    data
  })
}

// tab3: 审核厨师
export function auditChef(data) {
  return request({
    url: '/admin/chef/audit',
    method: 'post',
    data
  })
}

export function sendChefMessage(data) {
  return request({
    url: '/admin/chef/sendMessage',
    method: 'post',
    data
  })
}

// tab4: 查询评论列表
export function reviewList(data) {
  return request({
    url: '/admin/review/reviewList',
    method: 'post',
    data
  })
}

// tab4: 审核评论
export function auditReview(data) {
  return request({
    url: '/admin/review/audit',
    method: 'post',
    data
  })
}

// tab4: 删除评论
export function deleteReview(data) {
  return request({
    url: '/admin/review/delete',
    method: 'post',
    data
  })
}

// tab5: 查询举报列表
export function reportList(data) {
  return request({
    url: '/admin/report/reportList',
    method: 'post',
    data
  })
}

// tab5: 处理举报
export function handleReport(data) {
  return request({
    url: '/admin/report/handle',
    method: 'post',
    data
  })
}

// tab6: 查询订单列表
export function orderList(data) {
  return request({
    url: '/admin/order/list',
    method: 'post',
    data
  })
}

// tab7: 订单统计
export function orderStatistics() {
  return request({
    url: '/admin/statistics/order',
    method: 'post'
  })
}
