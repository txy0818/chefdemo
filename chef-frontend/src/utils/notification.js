import { ElNotification } from 'element-plus'

export const NOTIFICATION_UNREAD_CHANGE_EVENT = 'notification-unread-change'
export const REALTIME_DATA_REFRESH_EVENT = 'realtime-data-refresh'
const RECENT_NOTIFICATION_KEYS = new Map()
const NOTIFICATION_DEDUP_WINDOW = 5000

export function emitNotificationUnreadChange(delta) {
  window.dispatchEvent(new CustomEvent(NOTIFICATION_UNREAD_CHANGE_EVENT, {
    detail: { delta }
  }))
}

export function emitRealtimeDataRefresh(payload = {}) {
  window.dispatchEvent(new CustomEvent(REALTIME_DATA_REFRESH_EVENT, {
    detail: { payload }
  }))
}

export function isRealtimeNotificationPayload(payload) {
  return payload?.type === 'NOTIFICATION'
}

export function isOrderRealtimePayload(payload) {
  if (!isRealtimeNotificationPayload(payload)) {
    return false
  }
  const text = `${payload?.title || ''} ${payload?.content || ''}`
  return text.includes('订单')
}

export function isWalletRealtimePayload(payload) {
  if (!isRealtimeNotificationPayload(payload)) {
    return false
  }
  const text = `${payload?.title || ''} ${payload?.content || ''}`
  return text.includes('钱包') || text.includes('余额') || text.includes('退款') || text.includes('支付')
}

export function showRealtimeNotification(payload) {
  if (!payload?.title && !payload?.content) return
  const notificationId = payload?.data?.id || payload?.id
  const key = notificationId
    ? `notification:${notificationId}`
    : `${payload?.title || ''}::${payload?.content || ''}`
  const now = Date.now()
  const previousTime = RECENT_NOTIFICATION_KEYS.get(key)
  if (previousTime && now - previousTime < NOTIFICATION_DEDUP_WINDOW) {
    return
  }
  RECENT_NOTIFICATION_KEYS.set(key, now)
  RECENT_NOTIFICATION_KEYS.forEach((time, itemKey) => {
    if (now - time >= NOTIFICATION_DEDUP_WINDOW) {
      RECENT_NOTIFICATION_KEYS.delete(itemKey)
    }
  })
  ElNotification({
    title: payload.title || '新通知',
    message: payload.content || '',
    type: 'info',
    position: 'top-right',
    duration: 4500,
    offset: 72
  })
}
