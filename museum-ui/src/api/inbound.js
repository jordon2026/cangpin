import request from '@/api/request'

// 入库列表
export function getInboundList(params) {
  return request({ url: '/inbound/list', method: 'get', params })
}

// 入库详情
export function getInboundDetail(id) {
  return request({ url: `/inbound/${id}`, method: 'get' })
}

// 新增入库
export function addInbound(data) {
  return request({ url: '/inbound', method: 'post', data })
}

// 审批入库
export function approveInbound(id, approved, remark) {
  return request({ url: `/inbound/approve/${id}`, method: 'put', params: { approved, remark } })
}

// 删除入库
export function deleteInbound(id) {
  return request({ url: `/inbound/${id}`, method: 'delete' })
}
