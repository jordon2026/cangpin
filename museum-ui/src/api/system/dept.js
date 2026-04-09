import request from '@/api/request'

export function getDeptList() {
  return request({ url: '/system/dept/list', method: 'get' })
}

export function addDept(data) {
  return request({ url: '/system/dept', method: 'post', data })
}

export function updateDept(id, data) {
  return request({ url: `/system/dept/${id}`, method: 'put', data })
}

export function deleteDept(id) {
  return request({ url: `/system/dept/${id}`, method: 'delete' })
}
