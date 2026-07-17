import request from '@/utils/request'

export function getUserList(params) {
  return request.get('/system/user/list', { params })
}

export function getUserById(id) {
  return request.get(`/system/user/${id}`)
}

export function getRoleList() {
  return request.get('/system/role/list')
}

export function getPermissionList() {
  return request.get('/system/permission/list')
}

export function getOperationLogs(params) {
  return request.get('/system/log/list', { params })
}
