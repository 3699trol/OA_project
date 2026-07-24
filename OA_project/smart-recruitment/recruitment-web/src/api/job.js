import request from '@/utils/request'

export function getJobList(params) {
  return request.get('/job/list', { params })
}

export function getRecommendedJobs(params) {
  return request.get('/job/recommend', { params })
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

export function getJobCategories(params) {
  return request.get('/job/category/list', { params })
}

export function getActiveCategoryNames() {
  return request.get('/job/category/active-names')
}

export function createJobCategory(data) {
  return request.post('/job/category', data)
}

export function updateJobCategory(id, data) {
  return request.put(`/job/category/${id}`, data)
}

export function deleteJobCategory(id) {
  return request.delete(`/job/category/${id}`)
}

export function updateJobCategoryStatus(id, status) {
  return request.put(`/job/category/${id}/status`, null, { params: { status } })
}
