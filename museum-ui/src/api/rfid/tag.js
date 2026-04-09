import request from '@/api/request'

// 标签列表
export function getTagList(params) {
  return request({ url: '/rfid/tag/list', method: 'get', params })
}

// 新增标签
export function addTag(data) {
  return request({ url: '/rfid/tag', method: 'post', data })
}

// 修改标签
export function updateTag(data) {
  return request({ url: '/rfid/tag', method: 'put', data })
}

// 解绑标签
export function unbindTag(id) {
  return request({ url: `/rfid/tag/unbind/${id}`, method: 'put' })
}

// 删除标签
export function deleteTag(id) {
  return request({ url: `/rfid/tag/${id}`, method: 'delete' })
}
