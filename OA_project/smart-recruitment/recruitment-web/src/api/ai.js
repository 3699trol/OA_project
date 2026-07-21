import request from '@/utils/request'

const AI_REQUEST_TIMEOUT = 120000

export function startMockInterview(data) {
  return request.post('/ai/mock-interview/start', data)
}

export function chatMockInterview(data) {
  return request.post('/ai/mock-interview/chat', data)
}

export function submitMockInterview(data) {
  return request.post('/ai/mock-interview/submit', data)
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

export function aiEvaluateResume(resumeContent) {
  return request.post('/ai/resume/evaluate', resumeContent)
}

export function aiOptimizeResume(resumeContent) {
  return request.post('/ai/resume/optimize', resumeContent)
}
