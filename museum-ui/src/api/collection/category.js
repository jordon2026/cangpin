import request from '@/api/request'

// 分类列表（树形）
export function getCategoryList() {
  return request({ url: '/collection/category/list', method: 'get' })
}

// 新增分类
export function addCategory(data) {
  return request({ url: '/collection/category', method: 'post', data })
}

// 修改分类
export function updateCategory(data) {
  return request({ url: '/collection/category', method: 'put', data })
}

// 删除分类
export function deleteCategory(id) {
  return request({ url: `/collection/category/${id}`, method: 'delete' })
}
