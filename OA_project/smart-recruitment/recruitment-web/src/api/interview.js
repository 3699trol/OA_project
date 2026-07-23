import request from '@/utils/request'

export function createInterview(data) {
  return request.post('/interview', data)
}

export function getInterviewList(params) {
  return request.get('/interview/list', { params })
}

export function getInterviewDetail(id) {
  return request.get(`/interview/${id}`)
}

export function getInterviewerTasks(params) {
  return request.get('/interview/my-tasks', { params })
}

export function getInterviewOptions() {
  return request.get('/interview/options')
}

export function getCandidateApplications(userId) {
  return request.get('/interview/candidate-applications', { params: { userId } })
}

export function generateQuestions(data) {
  return request.post('/interview/question/generate', data)
}

export function saveFormalQuestion(data) {
  return request.post('/interview/question', data)
}

export function getQuestionsByInterview(interviewId) {
  return request.get(`/interview/question/${interviewId}`)
}

export function saveEvaluation(data) {
  return request.post('/interview/evaluation', data)
}

export function getEvaluationByInterview(interviewId) {
  return request.get(`/interview/evaluation/${interviewId}`)
}

export function cancelInterview(id) {
  return request.post(`/interview/${id}/cancel`)
}

export function processInterviewResult(id, hireDecision) {
  return request.post(`/interview/${id}/process`, { hireDecision })
}

export function getMyInterviews() {
    return request.get('/interview/my-interviews')
}

export function getTodayInterviews() {
  return request.get('/interview/today')
}

export function getRecentEvaluations(limit = 5) {
  return request.get('/interview/recent-evaluations', { params: { limit } })
}

export function getCandidateQuestions(interviewId) {
  return request.get(`/interview/question/candidate/${interviewId}`)
}

export function submitAnswer(data) {
  return request.put('/interview/question/answer', data)
}
