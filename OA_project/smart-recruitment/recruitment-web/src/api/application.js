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

export function getCandidateDetail(id) {
  return request.get(`/application/${id}/detail`)
}

// Aliases for convenience
export const apply = createApplication
export const getApplications = getApplicationList
