import request from '@/api/request'

// 库房列表
export function getStorageList(params) {
  return request({ url: '/storage/info/list', method: 'get', params })
}

// 库房详情
export function getStorageDetail(id) {
  return request({ url: `/storage/info/${id}`, method: 'get' })
}

// 新增库房
export function addStorage(data) {
  return request({ url: '/storage/info', method: 'post', data })
}

// 修改库房
export function updateStorage(data) {
  return request({ url: '/storage/info', method: 'put', data })
}

// 删除库房
export function deleteStorage(id) {
  return request({ url: `/storage/info/${id}`, method: 'delete' })
}
