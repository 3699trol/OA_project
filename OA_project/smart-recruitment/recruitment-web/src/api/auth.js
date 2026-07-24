import request from '@/utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function getCurrentUser() {
  return request.get('/auth/current-user')
}

export function refreshToken(data) {
  return request.post('/auth/refresh-token', data)
}

export function changePassword(data) {
  return request.post('/auth/change-password', data)
}

export function sendForgotPasswordCode(data) {
  return request.post('/auth/forgot-password/send-code', data)
}

export function resetForgotPassword(data) {
  return request.post('/auth/forgot-password/reset', data)
}

export function updateProfile(data) {
  return request.put('/auth/profile', data)
}
