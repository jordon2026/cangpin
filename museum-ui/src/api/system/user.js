import request from '@/api/request'

// 用户列表
export function getUserList(params) {
  return request({ url: '/system/user/list', method: 'get', params })
}

// 用户详情
export function getUserDetail(id) {
  return request({ url: `/system/user/${id}`, method: 'get' })
}

// 新增用户
export function addUser(data) {
  return request({ url: '/system/user', method: 'post', data })
}

// 修改用户
export function updateUser(data) {
  return request({ url: '/system/user', method: 'put', data })
}

// 删除用户
export function deleteUser(id) {
  return request({ url: `/system/user/${id}`, method: 'delete' })
}

// 重置密码
export function resetPwd(userId) {
  return request({ url: '/system/user/resetPwd', method: 'put', data: { userId } })
}

// 修改状态
export function updateUserStatus(userId, status) {
  return request({ url: '/system/user/status', method: 'put', data: { userId, status } })
}
