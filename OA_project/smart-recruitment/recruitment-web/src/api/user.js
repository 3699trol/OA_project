import request from '@/utils/request'

export function getUserList(params) {
  return request.get('/system/user/list', { params })
}

export function getUserById(id) {
  return request.get(`/system/user/${id}`)
}
