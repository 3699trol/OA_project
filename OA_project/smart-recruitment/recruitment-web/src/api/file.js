import request from '@/utils/request'

/**
 * 上传文件
 * @param {File} file - 文件对象
 * @param {string} fileType - 文件分类: resume/avatar/other
 */
export function uploadFile(file, fileType = 'other') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('fileType', fileType)
  return request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 下载文件（返回 blob）
 * @param {number} id - 文件记录ID
 */
export function downloadFile(id) {
  return request.get(`/file/download/${id}`, { responseType: 'blob' })
}

/**
 * 获取文件信息
 * @param {number} id - 文件记录ID
 */
export function getFileInfo(id) {
  return request.get(`/file/info/${id}`)
}

/**
 * 获取文件下载URL（用于直接链接/a标签下载）
 * @param {number} id - 文件记录ID
 */
export function getFileUrl(id) {
  return `/api/file/download/${id}`
}

/**
 * 删除文件（同时删除磁盘文件和数据库记录）
 * @param {number} id - 文件记录ID
 */
export function deleteFile(id) {
  return request.delete(`/file/${id}`)
}
