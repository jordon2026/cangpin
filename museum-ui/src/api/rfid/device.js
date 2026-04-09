import request from '@/api/request'

// 设备列表
export function getDeviceList(params) {
  return request({ url: '/rfid/device/list', method: 'get', params })
}

// 新增设备
export function addDevice(data) {
  return request({ url: '/rfid/device', method: 'post', data })
}

// 修改设备
export function updateDevice(data) {
  return request({ url: '/rfid/device', method: 'put', data })
}

// 删除设备
export function deleteDevice(id) {
  return request({ url: `/rfid/device/${id}`, method: 'delete' })
}

// 设备诊断
export function diagnoseDevice(id) {
  return request({ url: `/rfid/device/diagnose/${id}`, method: 'get' })
}
