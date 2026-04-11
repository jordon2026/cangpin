import request from './request'

// 获取验证码
export function getCaptcha() {
  return request.get('/auth/captcha')
}

// 用户登录
export function login(data) {
  return request.post('/auth/login', data)
}

// 退出登录
export function logout() {
  return request.post('/auth/logout')
}

// 获取当前用户信息
export function getUserInfo() {
  return request.get('/auth/userinfo')
}

// 获取用户菜单
export function getMenus() {
  return request.get('/auth/menus')
}

// 获取仪表盘统计数据
export function getDashboard() {
  return request.get('/auth/dashboard')
}
