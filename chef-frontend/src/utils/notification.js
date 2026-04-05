import { ElNotification } from 'element-plus'

export const NOTIFICATION_UNREAD_CHANGE_EVENT = 'notification-unread-change'

export function emitNotificationUnreadChange(delta) {
  window.dispatchEvent(new CustomEvent(NOTIFICATION_UNREAD_CHANGE_EVENT, {
    detail: { delta }
  }))
}

export function showRealtimeNotification(payload) {
  if (!payload?.title && !payload?.content) return
  ElNotification({
    title: payload.title || '新通知',
    message: payload.content || '',
    type: 'info',
    position: 'top-right',
    duration: 4500,
    offset: 72
  })
}
