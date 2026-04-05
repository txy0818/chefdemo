import config from '@/config'

function buildWebSocketUrl(userId) {
  const baseUrl = config.apiBaseUrl || window.location.origin
  const wsBase = baseUrl.replace(/^http/, 'ws')
  const token = localStorage.getItem('token') || ''
  const query = token ? `?token=${encodeURIComponent(token)}` : ''
  return `${wsBase}/ws/${userId}${query}`
}

export function createUserSocket(userId, handlers = {}) {
  if (!userId) {
    return null
  }

  const socket = new WebSocket(buildWebSocketUrl(userId))

  socket.onmessage = event => {
    try {
      const payload = JSON.parse(event.data)
      handlers.onMessage?.(payload)
    } catch (error) {
      console.error('解析 WebSocket 消息失败:', error)
    }
  }

  socket.onerror = error => {
    handlers.onError?.(error)
  }

  socket.onclose = event => {
    handlers.onClose?.(event)
  }

  socket.onopen = event => {
    handlers.onOpen?.(event)
  }

  return socket
}
