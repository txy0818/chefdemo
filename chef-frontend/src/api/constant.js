import request from '@/utils/request'

export function getUserRoleOptions() {
  return request({
    url: '/constant/userRole',
    method: 'get'
  })
}

export function getUserStatusOptions() {
  return request({
    url: '/constant/userStatus',
    method: 'get'
  })
}

export function getAuditStatusOptions() {
  return request({
    url: '/constant/auditStatus',
    method: 'get'
  })
}

export function getReviewAuditStatusOptions() {
  return request({
    url: '/constant/reviewAuditStatus',
    method: 'get'
  })
}

export function getReportStatusOptions() {
  return request({
    url: '/constant/reportStatus',
    method: 'get'
  })
}

export function getOrderStatusOptions() {
  return request({
    url: '/constant/orderStatus',
    method: 'get'
  })
}

export function getPaymentTypeOptions() {
  return request({
    url: '/constant/paymentType',
    method: 'get'
  })
}

export function getGenderOptions() {
  return request({
    url: '/constant/gender',
    method: 'get'
  })
}

export function getCuisineTypeOptions() {
  return request({
    url: '/constant/cuisineType',
    method: 'get'
  })
}
