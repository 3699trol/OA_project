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

export function getRoleById(id) {
  return request.get(`/system/role/${id}`)
}

export function updateRole(data) {
  return request.put(`/system/role/${data.id}`, data)
}

export function getPermissionList() {
  return request.get('/system/permission/list')
}

export function getOperationLogs(params) {
  return request.get('/system/log/list', { params })
}

export function resetPassword(id) {
  return request.put(`/system/user/${id}/reset-password`)
}

export function deleteUser(id) {
  return request.delete(`/system/user/${id}`)
}

export function restoreUser(id) {
  return request.put(`/system/user/${id}/restore`)
}

export function updateUser(data) {
  return request.put(`/system/user/${data.id}`, data)
}
