import request from '@/api/request'

export function getOperLogPage(params) {
  return request({ url: '/system/operlog/page', method: 'get', params })
}

export function deleteOperLog(ids) {
  return request({ url: `/system/operlog/${ids}`, method: 'delete' })
}
