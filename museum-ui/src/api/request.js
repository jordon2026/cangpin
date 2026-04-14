import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      // 确保 token 带有 Bearer 前缀
      config.headers['Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 对于 blob 响应，直接返回 response 对象，让业务层处理
    if (response.config.responseType === 'blob') {
      return response.data
    }
    
    const res = response.data
    if (res.code === 200) {
      return res
    } else if (res.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
      return Promise.reject(new Error(res.msg || '未登录'))
    } else {
      // 避免重复提示权限错误（让业务层自己处理）
      if (res.code !== 403) {
        ElMessage.error(res.msg || '请求失败')
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
  },
  async error => {
    if (error.response) {
      const status = error.response.status
      let data = error.response.data
      
      // 如果响应是 blob 类型，尝试解析为 JSON
      if (data instanceof Blob) {
        try {
          const text = await data.text()
          data = JSON.parse(text)
        } catch (e) {
          // 解析失败，保持原样
        }
      }
      
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      } else if (status === 403) {
        // 显示具体错误信息
        const msg = data?.msg || data?.message || '没有操作权限'
        ElMessage.error(msg)
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else {
        const msg = data?.msg || data?.message || '服务器异常，请稍后重试'
        ElMessage.error(msg)
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default request
