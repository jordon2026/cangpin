import request from '@/api/request'

export function getMenuList() {
  return request({ url: '/system/menu/list', method: 'get' })
}

export function addMenu(data) {
  return request({ url: '/system/menu', method: 'post', data })
}

export function updateMenu(id, data) {
  return request({ url: `/system/menu/${id}`, method: 'put', data })
}

export function deleteMenu(id) {
  return request({ url: `/system/menu/${id}`, method: 'delete' })
}
