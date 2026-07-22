import request from '@/utils/request'

const AI_REQUEST_TIMEOUT = 120000

export function startMockInterview(data) {
  return request.post('/ai/mock-interview/start', data, { timeout: AI_REQUEST_TIMEOUT })
}

export function chatMockInterview(data) {
  return request.post('/ai/mock-interview/chat', data, { timeout: AI_REQUEST_TIMEOUT })
}

export function submitMockInterview(data) {
  return request.post('/ai/mock-interview/submit', data, { timeout: AI_REQUEST_TIMEOUT })
}

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

export function aiMatch(data) {
  return request.post('/ai/match', data, { timeout: AI_REQUEST_TIMEOUT })
}

export function generateFormalQuestions(data) {
  return request.post('/ai/question/generate', data, { timeout: AI_REQUEST_TIMEOUT })
}

export function aiParseResume(data) {
  return request.post('/ai/resume/parse', data, { timeout: AI_REQUEST_TIMEOUT })
}

export function aiParseResumeFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/ai/resume/parse-file', formData, {
    timeout: AI_REQUEST_TIMEOUT,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function aiEvaluateResume(resumeContent) {
  return request.post('/ai/resume/evaluate', resumeContent)
}

export function aiOptimizeResume(resumeContent) {
  return request.post('/ai/resume/optimize', resumeContent)
}
