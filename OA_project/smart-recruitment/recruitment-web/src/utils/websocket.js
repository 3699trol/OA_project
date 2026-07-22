import { useInboxStore } from '@/stores/inbox'

const MAX_RECONNECT_ATTEMPTS = 3

let socket = null
let reconnectTimer = null
let reconnectAttempts = 0
let isConnected = false

export function connectWebSocket(username) {
  // If Mock mode is enabled, do not connect to real ws, just log and return
  const isMock = localStorage.getItem('use_mock') !== 'false'
  if (isMock) {
    console.log('[WebSocket] Mock mode enabled. Real connection bypassed.')
    return
  }

  if (socket) {
    socket.close()
  }

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  // Use hostname and port or direct local server
  const wsUrl = `${protocol}//${window.location.host}/api/ws/message?username=${username}`

  console.log(`[WebSocket] Connecting to: ${wsUrl}`)
  
  try {
    socket = new WebSocket(wsUrl)

    socket.onopen = () => {
      console.log('[WebSocket] Connection established successfully.')
      isConnected = true
      reconnectAttempts = 0
      if (reconnectTimer) {
        clearInterval(reconnectTimer)
        reconnectTimer = null
      }
    }

    socket.onmessage = (event) => {
      try {
        const payload = JSON.parse(event.data)
        const inboxStore = useInboxStore()
        inboxStore.addMessage({
          title: payload.title || '🔔 新系统消息',
          content: payload.content || '',
          type: payload.type || 'SYSTEM',
          link: payload.link || ''
        })
      } catch (e) {
        console.error('[WebSocket] Error parsing socket payload:', e)
      }
    }

    socket.onclose = () => {
      console.log('[WebSocket] Connection closed. Retrying in 10s...')
      isConnected = false
      scheduleReconnect(username)
    }

    socket.onerror = (err) => {
      console.warn('[WebSocket] Connection error (后端可能未部署WebSocket服务):', err.message || err)
      socket.close()
    }
  } catch (error) {
    console.error('[WebSocket] Connection failed to initialize:', error)
    scheduleReconnect(username)
  }
}

function scheduleReconnect(username) {
  if (!reconnectTimer) {
    reconnectTimer = setInterval(() => {
      if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
        console.log('[WebSocket] 已达最大重连次数(' + MAX_RECONNECT_ATTEMPTS + ')，停止重连。后端未部署WebSocket服务，消息推送功能暂不可用。')
        clearInterval(reconnectTimer)
        reconnectTimer = null
        return
      }
      reconnectAttempts++
      console.log('[WebSocket] Attempting to reconnect (attempt ' + reconnectAttempts + '/' + MAX_RECONNECT_ATTEMPTS + ')...')
      connectWebSocket(username)
    }, 10000)
  }
}

export function disconnectWebSocket() {
  if (socket) {
    socket.close()
    socket = null
  }
  if (reconnectTimer) {
    clearInterval(reconnectTimer)
    reconnectTimer = null
  }
  isConnected = false
}

/**
 * Simulate an incoming WebSocket push notification (For Demo / Mock testing)
 */
export function simulateWebSocketPush(type) {
  const inboxStore = useInboxStore()
  
  const templates = {
    RESUME_PASS: {
      title: '🎉 简历初筛通过通知',
      content: '您的【高级Java开发工程师】简历初筛已通过。HR李经理已发送面试安排邀请，请点击卡片并前往投递记录查看后续的具体面试排期！',
      type: 'RESUME_PASS',
      link: '/candidate/applications'
    },
    INTERVIEW_SCHEDULE: {
      title: '📅 正式面试安排邀请',
      content: '安排成功：您的【高级Java开发工程师】岗位的正式一轮线上技术面试已锁定。时间为：2026-07-22 15:30，面试官为技术架构师王工。请保持网络畅通，届时准时出席！',
      type: 'INTERVIEW_SCHEDULE',
      link: '/candidate/applications'
    },
    OFFER: {
      title: '💌 智慧大厂录用通知 (Offer Letter)',
      content: '恭喜您！经过几轮评审，您在本次【高级Java开发工程师】岗位的日常选拔中脱颖而出。由于技术亮点契合、高并发攻坚能力强，现决定对您发出正式录用信！Offer详细说明文件及福利包已发送到您的邮箱，请注意查收并点击进行确认。',
      type: 'OFFER',
      link: '/candidate/applications'
    },
    REJECT: {
      title: '🍂 简历筛选感谢信',
      content: '非常感谢您对【资深产品经理】岗位的关注。您的教育背景和经验给我们留下了深刻印象，但由于岗位契合度等原因，本次我们暂无法安排面试。您的简历已存入我们的人才储备库，期待未来能有合作的机会！',
      type: 'REJECT',
      link: '/candidate/applications'
    }
  }

  const selectedTemplate = templates[type] || templates.RESUME_PASS
  
  console.log(`[WebSocket Mock] Simulating push of type: ${type}`)
  
  // Simulate network flight latency before push
  setTimeout(() => {
    inboxStore.addMessage(selectedTemplate)
  }, 600)
}
