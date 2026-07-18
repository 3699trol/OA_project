import request from '@/utils/request'

export function searchJob(params) {
  return request.get('/search/job', { params })
}

export function searchResume(params) {
  return request.get('/search/resume', { params })
}

export function rebuildIndex() {
  return request.post('/search/rebuild-index')
}
