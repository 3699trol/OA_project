import { defineStore } from 'pinia'
import { ref } from 'vue'
import { connectWebSocket, disconnectWebSocket } from '@/utils/websocket'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  function setToken(val) {
    token.value = val
    localStorage.setItem('token', val)
  }

  function setRefreshToken(val) {
    refreshToken.value = val
    localStorage.setItem('refreshToken', val)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
    if (info && info.username) {
      connectWebSocket(info.username)
    }
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    disconnectWebSocket()
  }

  return { token, refreshToken, userInfo, setToken, setRefreshToken, setUserInfo, logout }
})
