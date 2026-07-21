import request from '@/utils/request'

/**
 * 获取 HR 仪表板统计数据
 */
export function getDashboardStats() {
  return request.get('/dashboard/stats')
}

/**
 * 获取面试官工作台统计数据
 */
export function getInterviewerStats() {
  return request.get('/dashboard/interviewer-stats')
}
