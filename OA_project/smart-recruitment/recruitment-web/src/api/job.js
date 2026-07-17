import request from '@/utils/request'

export function getJobList(params) {
  return request.get('/job/list', { params })
}

export function getJobDetail(id) {
  return request.get(`/job/${id}`)
}

export function createJob(data) {
  return request.post('/job', data)
}

export function updateJob(id, data) {
  return request.put(`/job/${id}`, data)
}

export function publishJob(id) {
  return request.post(`/job/${id}/publish`)
}

export function unpublishJob(id) {
  return request.post(`/job/${id}/unpublish`)
}

export function getJobCategories() {
  return request.get('/job/categories')
}
