import request from '@/api/request'

// 外借申请列表
export function getLoanList(params) {
  return request({ url: '/loan/apply/list', method: 'get', params })
}

// 外借详情
export function getLoanDetail(id) {
  return request({ url: `/loan/apply/${id}`, method: 'get' })
}

// 新增外借申请
export function addLoan(data) {
  return request({ url: '/loan/apply', method: 'post', data })
}

// 撤回外借申请
export function cancelLoan(id) {
  return request({ url: `/loan/apply/cancel/${id}`, method: 'put' })
}
