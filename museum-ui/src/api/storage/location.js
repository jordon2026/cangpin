import request from '@/api/request'

// 库位列表
export function getLocationList(params) {
  return request({ url: '/storage/location/list', method: 'get', params })
}

// 新增库位
export function addLocation(data) {
  return request({ url: '/storage/location', method: 'post', data })
}

// 修改库位
export function updateLocation(data) {
  return request({ url: '/storage/location', method: 'put', data })
}

// 删除库位
export function deleteLocation(id) {
  return request({ url: `/storage/location/${id}`, method: 'delete' })
}
