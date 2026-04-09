import request from '@/api/request'

export function getRoleList() {
  return request({ url: '/system/role/list', method: 'get' })
}

export function getRolePage(params) {
  return request({ url: '/system/role/page', method: 'get', params })
}

export function addRole(data) {
  return request({ url: '/system/role', method: 'post', data })
}

export function updateRole(data) {
  return request({ url: '/system/role', method: 'put', data })
}

export function deleteRole(id) {
  return request({ url: `/system/role/${id}`, method: 'delete' })
}

export function getRoleMenuIds(roleId) {
  return request({ url: `/system/role/menuIds/${roleId}`, method: 'get' })
}

export function assignRoleMenus(roleId, menuIds) {
  return request({ url: '/system/role/menus', method: 'put', data: { roleId, menuIds } })
}
