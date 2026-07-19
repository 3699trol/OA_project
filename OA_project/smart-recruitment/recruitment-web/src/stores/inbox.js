import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElNotification } from 'element-plus'

export const useInboxStore = defineStore('inbox', () => {
  const messages = ref(JSON.parse(localStorage.getItem('inbox_messages')) || [
    {
      id: 1,
      title: '📋 模拟面试报告已就绪',
      content: '您的【高级Java开发工程师】模拟面试诊断评估报告已生成。本次面试综合得分为 87 分，推荐定级为高级工程师。点击即可查看详细的技术亮点及STAR优化反馈。',
      time: '2026-07-18 10:25:00',
      type: 'MOCK_REPORT',
      read: false,
      link: '/candidate/mock-interview/report?id=301'
    },
    {
      id: 2,
      title: '🎉 简历初筛通过通知',
      content: '恭喜！您的简历已通过【高级Java开发工程师】职位的初筛。HR李经理已发起在线面试邀约。请留意后续的正式面试安排。',
      time: '2026-07-17 14:10:00',
      type: 'RESUME_PASS',
      read: true,
      link: '/candidate/applications'
    }
  ])

  const unreadCount = computed(() => {
    return messages.value.filter(m => !m.read).length
  })

  function addMessage(msg) {
    const newMessage = {
      id: Date.now(),
      time: new Date().toLocaleString(),
      read: false,
      ...msg
    }
    messages.value.unshift(newMessage)
    saveToStorage()

    // Trigger sliding desktop notification toast
    ElNotification({
      title: newMessage.title,
      message: newMessage.content,
      type: getNotificationType(newMessage.type),
      duration: 5000
    })
  }

  function markAsRead(id) {
    const msg = messages.value.find(m => m.id === id)
    if (msg) {
      msg.read = true
      saveToStorage()
    }
  }

  function markAllAsRead() {
    messages.value.forEach(m => {
      m.read = true
    })
    saveToStorage()
  }

  function deleteMessage(id) {
    messages.value = messages.value.filter(m => m.id !== id)
    saveToStorage()
  }

  function clearAll() {
    messages.value = []
    saveToStorage()
  }

  function saveToStorage() {
    localStorage.setItem('inbox_messages', JSON.stringify(messages.value))
  }

  function getNotificationType(type) {
    switch (type) {
      case 'OFFER':
      case 'RESUME_PASS':
        return 'success'
      case 'INTERVIEW_SCHEDULE':
      case 'MOCK_REPORT':
        return 'info'
      case 'REJECT':
        return 'error'
      default:
        return 'warning'
    }
  }

  return {
    messages,
    unreadCount,
    addMessage,
    markAsRead,
    markAllAsRead,
    deleteMessage,
    clearAll
  }
})
