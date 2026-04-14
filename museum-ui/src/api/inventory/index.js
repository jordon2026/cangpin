import request from '@/api/request'

// 盘点任务列表
export function getTaskList(params) {
  return request({ url: '/inventory/task/list', method: 'get', params })
}

// 盘点任务详情
export function getTaskDetail(id) {
  return request({ url: `/inventory/task/${id}`, method: 'get' })
}

// 盘点任务详情（别名）
export function getTaskById(id) {
  return request({ url: `/inventory/task/${id}`, method: 'get' })
}

// 创建盘点任务
export function addTask(data) {
  return request({ url: '/inventory/task', method: 'post', data })
}

// 删除盘点任务
export function deleteTask(id) {
  return request({ url: `/inventory/task/${id}`, method: 'delete' })
}

// 盘点记录列表
export function getRecordList(params) {
  return request({ url: '/inventory/record/list', method: 'get', params })
}

// 盘点记录详情
export function getRecordDetail(id) {
  return request({ url: `/inventory/record/${id}`, method: 'get' })
}
