import request from '@/api/request'

// 出库列表
export function getOutboundList(params) {
  return request({ url: '/outbound/list', method: 'get', params })
}

// 出库详情
export function getOutboundDetail(id) {
  return request({ url: `/outbound/${id}`, method: 'get' })
}

// 新增出库
export function addOutbound(data) {
  return request({ url: '/outbound', method: 'post', data })
}

// 审批出库
export function approveOutbound(id, approved, remark) {
  return request({ url: `/outbound/approve/${id}`, method: 'put', params: { approved, remark } })
}

// 归还藏品
export function returnCollection(id) {
  return request({ url: `/outbound/return/${id}`, method: 'put' })
}

// 删除出库
export function deleteOutbound(id) {
  return request({ url: `/outbound/${id}`, method: 'delete' })
}
