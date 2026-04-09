import request from '@/api/request'

// 藏品列表
export function getCollectionList(params) {
  return request({ url: '/collection/info/list', method: 'get', params })
}

// 藏品详情
export function getCollectionDetail(id) {
  return request({ url: `/collection/info/${id}`, method: 'get' })
}

// 新增藏品
export function addCollection(data) {
  return request({ url: '/collection/info', method: 'post', data })
}

// 修改藏品
export function updateCollection(data) {
  return request({ url: '/collection/info', method: 'put', data })
}

// 删除藏品
export function deleteCollection(id) {
  return request({ url: `/collection/info/${id}`, method: 'delete' })
}
