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
