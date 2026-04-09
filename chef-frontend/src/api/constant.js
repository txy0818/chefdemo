import request from '@/utils/request'

let auditStatusOptionsPromise = null
let orderStatusOptionsPromise = null
let availableTimeStatusOptionsPromise = null
let payStatusOptionsPromise = null
let reportTypeOptionsPromise = null

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

async function getCachedAuditStatusOptions() {
  if (!auditStatusOptionsPromise) {
    auditStatusOptionsPromise = getAuditStatusOptions()
      .then(res => (Array.isArray(res.data) ? res.data : []))
      .catch(error => {
        auditStatusOptionsPromise = null
        throw error
      })
  }
  return auditStatusOptionsPromise
}

export async function isChefAuditApproved(auditStatus) {
  const options = await getCachedAuditStatusOptions()
  const approvedOption = options.find(item => item.value === 2)
  return approvedOption ? auditStatus === approvedOption.value : false
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

export function getReportTypeOptions() {
  return request({
    url: '/constant/reportType',
    method: 'get'
  })
}

export function getOrderStatusOptions() {
  return request({
    url: '/constant/orderStatus',
    method: 'get'
  })
}

export function getAvailableTimeStatusOptions() {
  return request({
    url: '/constant/availableTimeStatus',
    method: 'get'
  })
}

export function getPayStatusOptions() {
  return request({
    url: '/constant/payStatus',
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

async function getCachedOrderStatusOptions() {
  if (!orderStatusOptionsPromise) {
    orderStatusOptionsPromise = getOrderStatusOptions()
      .then(res => (Array.isArray(res.data) ? res.data : []))
      .catch(error => {
        orderStatusOptionsPromise = null
        throw error
      })
  }
  return orderStatusOptionsPromise
}

async function getCachedAvailableTimeStatusOptions() {
  if (!availableTimeStatusOptionsPromise) {
    availableTimeStatusOptionsPromise = getAvailableTimeStatusOptions()
      .then(res => (Array.isArray(res.data) ? res.data : []))
      .catch(error => {
        availableTimeStatusOptionsPromise = null
        throw error
      })
  }
  return availableTimeStatusOptionsPromise
}

async function getCachedPayStatusOptions() {
  if (!payStatusOptionsPromise) {
    payStatusOptionsPromise = getPayStatusOptions()
      .then(res => (Array.isArray(res.data) ? res.data : []))
      .catch(error => {
        payStatusOptionsPromise = null
        throw error
      })
  }
  return payStatusOptionsPromise
}

async function getCachedReportTypeOptions() {
  if (!reportTypeOptionsPromise) {
    reportTypeOptionsPromise = getReportTypeOptions()
      .then(res => (Array.isArray(res.data) ? res.data : []))
      .catch(error => {
        reportTypeOptionsPromise = null
        throw error
      })
  }
  return reportTypeOptionsPromise
}

export async function getOrderStatusTabOptions() {
  const options = await getCachedOrderStatusOptions()
  return [{ label: '全部', value: 0 }, ...options]
}

export async function getAvailableTimeStatusLabelMap() {
  const options = await getCachedAvailableTimeStatusOptions()
  return options.reduce((result, item) => {
    result[item.value] = item.label
    return result
  }, {})
}

export async function getPayStatusLabelMap() {
  const options = await getCachedPayStatusOptions()
  return options.reduce((result, item) => {
    result[item.value] = item.label
    return result
  }, {})
}

export async function getReportTypeLabelMap() {
  const options = await getCachedReportTypeOptions()
  return options.reduce((result, item) => {
    result[item.value] = item.label
    return result
  }, {})
}
