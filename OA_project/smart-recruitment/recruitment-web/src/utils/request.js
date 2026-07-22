import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { handleMockRequest } from '@/mock'
import { useUserStore } from '@/stores/user'

// --- Mock 总开关（代码级）---
// 若希望在代码层面彻底关闭 Mock（不受用户在页面上切换或 localStorage 影响），
// 将下方常量改为 true 即可：此时无论 localStorage.use_mock 是什么值，都会强制走真实后端。
const FORCE_DISABLE_MOCK = true
const REFRESH_TOKEN_PATH = '/auth/refresh-token'
let refreshPromise = null

// Keep all frontend transports on the same mode. A demo token cannot be
// authenticated by the real Spring Security filter chain.
if (FORCE_DISABLE_MOCK) {
  localStorage.setItem('use_mock', 'false')
  const token = localStorage.getItem('token') || ''
  if (token.startsWith('demo-token-')) {
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
  }
}

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

function clearSession() {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userInfo')
}

function redirectToLogin() {
  clearSession()
  if (router.currentRoute.value.path !== '/login') {
    router.push('/login')
  }
}

async function renewSession() {
  const storedRefreshToken = localStorage.getItem('refreshToken')
  if (!storedRefreshToken) {
    throw new Error('登录已过期，请重新登录')
  }

  const response = await axios.post(
    `/api${REFRESH_TOKEN_PATH}`,
    { refreshToken: storedRefreshToken },
    { timeout: 30000 }
  )
  const result = response.data
  const data = result?.data
  if (result?.code !== 200 || !data?.token || !data?.refreshToken) {
    throw new Error(result?.message || '登录已过期，请重新登录')
  }

  localStorage.setItem('token', data.token)
  localStorage.setItem('refreshToken', data.refreshToken)
  const userStore = useUserStore()
  userStore.setToken(data.token)
  userStore.setRefreshToken(data.refreshToken)
  if (data.userInfo) {
    localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
  }
  return data.token
}

async function retryAfterRefresh(config) {
  const cannotRetry = !config || config._retry || config.url === REFRESH_TOKEN_PATH
  if (cannotRetry || !localStorage.getItem('refreshToken')) {
    redirectToLogin()
    throw new Error('登录已过期，请重新登录')
  }

  config._retry = true
  try {
    if (!refreshPromise) {
      refreshPromise = renewSession().finally(() => {
        refreshPromise = null
      })
    }
    const newToken = await refreshPromise
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${newToken}`
    return request(config)
  } catch (error) {
    redirectToLogin()
    throw error
  }
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // --- Mock 拦截逻辑 ---
    // 默认启用 Mock，可以在浏览器控制台通过 localStorage.setItem('use_mock', 'false') 切换为真实后端
    // 注意：绝不能在此处直接 resolve 一个伪造的“响应对象”，因为 Axios 请求拦截器返回值最终会被
    // 当作 config 传给 dispatchRequest/adapter（继续走真实网络流程），伪造响应对象没有 method 等字段，
    // 会在底层触发 "Cannot read properties of undefined (reading 'toUpperCase')"。
    // 正确做法：以 reject 的方式短路请求链，彻底跳过 dispatchRequest，在响应拦截器的 rejected 分支里
    // 捕获该标记并转换为 resolve，从而安全地模拟网络延迟和业务返回。
    const useMock = !FORCE_DISABLE_MOCK && localStorage.getItem('use_mock') !== 'false'
    if (useMock) {
      const mockRes = handleMockRequest(config)
      if (mockRes) {
        console.log(`%c[Mock Intercepted]%c ${(config.method || 'get').toUpperCase()} ${config.url}`, 'color: #ED8936; font-weight: bold;', 'color: inherit;', mockRes.data)
        const mockSignal = new Error('MOCK_SHORT_CIRCUIT')
        mockSignal.__isMock = true
        mockSignal.mockRes = mockRes
        return Promise.reject(mockSignal)
      }
    }

    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // blob 响应（文件下载）直接返回，不走业务 code 校验
    if (response.config.responseType === 'blob') {
      return response.data
    }
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401) {
        return retryAfterRefresh(response.config)
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    // 捕获来自请求拦截器的 Mock 短路信号，转换为正常的成功/失败返回
    if (error && error.__isMock) {
      const { mockRes } = error
      return new Promise((resolve, reject) => {
        setTimeout(() => {
          const res = mockRes.data
          if (res && res.code === 200) {
            resolve(res)
          } else {
            const msg = (res && res.message) || mockRes.message || '请求失败'
            ElMessage.error(msg)
            if (res && res.code === 401) {
              localStorage.removeItem('token')
              router.push('/login')
            }
            reject(new Error(msg))
          }
        }, mockRes.delay || 300)
      })
    }

    // 尝试从后端返回的 JSON 中提取错误信息
    const data = error.response?.data
    if (error.response?.status === 401 || data?.code === 401) {
      return retryAfterRefresh(error.config)
    }
    const msg = data?.message || error.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
