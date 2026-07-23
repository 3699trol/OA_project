import dayjs from 'dayjs'

/**
 * 格式化日期时间，去除 ISO-8601 中的 'T'。
 * @param {string|number|Date} value 后端返回的 LocalDateTime 字符串，如 "2026-07-23T11:51:39"
 * @param {string} pattern 目标格式，默认 "YYYY-MM-DD HH:mm:ss"
 * @returns {string} 格式化后的字符串，空值或非法值返回 "-"
 */
export function formatDateTime(value, pattern = 'YYYY-MM-DD HH:mm:ss') {
  if (!value && value !== 0) return '-'
  const d = dayjs(value)
  return d.isValid() ? d.format(pattern) : String(value)
}

/** 仅日期，默认 "YYYY-MM-DD" */
export function formatDate(value, pattern = 'YYYY-MM-DD') {
  return formatDateTime(value, pattern)
}
