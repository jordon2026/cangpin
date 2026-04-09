import request from '../request'

// API基础路径
const baseURL = '/backup'

/**
 * 执行数据库备份
 */
export function createBackup() {
  return request({
    url: baseURL + '/create',
    method: 'post'
  })
}

/**
 * 获取备份文件列表
 */
export function listBackups() {
  return request({
    url: baseURL + '/list',
    method: 'get'
  })
}

/**
 * 下载备份文件
 */
export function downloadBackup(fileName) {
  return request({
    url: baseURL + '/download',
    method: 'get',
    params: { fileName },
    responseType: 'blob'
  })
}

/**
 * 上传并恢复备份
 */
export function restoreFromUpload(file, confirmed = true) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('confirmed', confirmed)
  return request({
    url: baseURL + '/restore/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 从服务器文件恢复备份
 */
export function restoreFromFile(fileName, confirmed = true) {
  return request({
    url: baseURL + '/restore/file',
    method: 'post',
    params: { fileName, confirmed }
  })
}

/**
 * 删除备份文件
 */
export function deleteBackup(fileName) {
  return request({
    url: baseURL + '/' + encodeURIComponent(fileName),
    method: 'delete'
  })
}
