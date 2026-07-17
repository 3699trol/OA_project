import request from '@/utils/request'

export function generateMockInterview(data) {
  return request.post('/ai/mock-interview/generate', data)
}

export function recordAnswer(data) {
  return request.post('/ai/mock-interview/record', data)
}

export function followUp(data) {
  return request.post('/ai/mock-interview/follow-up', data)
}

export function getMockInterviewReport(sessionId) {
  return request.get(`/ai/mock-interview/report/${sessionId}`)
}
