import request from '@/api/request'

// 藏品列表
export function getCollectionList(params) {
  return request({ url: '/collection/list', method: 'get', params })
}

// 藏品详情
export function getCollectionDetail(id) {
  return request({ url: `/collection/${id}`, method: 'get' })
}

// 新增藏品
export function addCollection(data) {
  return request({ url: '/collection', method: 'post', data })
}

// 修改藏品
export function updateCollection(data) {
  return request({ url: '/collection', method: 'put', data })
}

// 删除藏品
export function deleteCollection(id) {
  return request({ url: `/collection/${id}`, method: 'delete' })
}

// 批量删除藏品
export function batchDeleteCollection(ids) {
  return request({ url: '/collection/batch', method: 'delete', data: ids })
}

// 获取所有分类
export function getCategories() {
  return request({ url: '/collection/categories', method: 'get' })
}
