import request from '@/utils/request'

export function getResume(id) {
  return request.get(`/resume/${id}`)
}

export function saveResume(data) {
  return request.post('/resume', data)
}

export function updateResume(id, data) {
  return request.put(`/resume/${id}`, data)
}

export function parseResume(id) {
  return request.post(`/resume/${id}/parse`)
}

export function evaluateResume(id) {
  return request.post(`/resume/${id}/evaluate`)
}
