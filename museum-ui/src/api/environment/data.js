import request from '@/api/request'

// 环境数据列表
export function getEnvironmentList(params) {
  return request({ url: '/environment/data/list', method: 'get', params })
}

// 刷新环境数据
export function refreshEnvironment() {
  return request({ url: '/environment/data/refresh', method: 'post' })
}
