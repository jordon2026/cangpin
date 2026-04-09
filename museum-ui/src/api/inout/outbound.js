import request from '@/api/request'

// 出库申请列表
export function getOutboundList(params) {
  return request({ url: '/inout/outbound/list', method: 'get', params })
}

// 出库详情
export function getOutboundDetail(id) {
  return request({ url: `/inout/outbound/${id}`, method: 'get' })
}

// 新增出库申请
export function addOutbound(data) {
  return request({ url: '/inout/outbound', method: 'post', data })
}

// 撤回出库申请
export function cancelOutbound(id) {
  return request({ url: `/inout/outbound/cancel/${id}`, method: 'put' })
}

// 审批出库
export function approveOutbound(data) {
  return request({ url: '/inout/outbound/approve', method: 'put', data })
}
