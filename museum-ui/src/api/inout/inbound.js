import request from '@/api/request'

// 入库列表
export function getInboundList(params) {
  return request({ url: '/inout/inbound/list', method: 'get', params })
}

// 入库详情
export function getInboundDetail(id) {
  return request({ url: `/inout/inbound/${id}`, method: 'get' })
}

// 新增入库
export function addInbound(data) {
  return request({ url: '/inout/inbound', method: 'post', data })
}

// 删除入库
export function deleteInbound(id) {
  return request({ url: `/inout/inbound/${id}`, method: 'delete' })
}
