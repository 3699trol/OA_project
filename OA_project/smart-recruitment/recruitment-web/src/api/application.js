import request from '@/utils/request'

export function createApplication(data) {
  return request.post('/application', data)
}

export function getApplicationList(params) {
  return request.get('/application/list', { params })
}

export function updateApplicationStatus(id, data) {
  return request.put(`/application/${id}/status`, data)
}
