import request from '@/api/request'

// 修复工单列表
export function getRepairList(params) {
  return request({ url: '/repair/order/list', method: 'get', params })
}

// 修复工单详情
export function getRepairDetail(id) {
  return request({ url: `/repair/order/${id}`, method: 'get' })
}

// 创建修复工单
export function addRepair(data) {
  return request({ url: '/repair/order', method: 'post', data })
}

// 删除修复工单
export function deleteRepair(id) {
  return request({ url: `/repair/order/${id}`, method: 'delete' })
}
