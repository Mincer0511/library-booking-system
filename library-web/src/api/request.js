import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth } from '@/utils/auth'

// 创建 axios 实例：统一走 /api（vite 代理到后端）
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器：注入 token
request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理 {code, msg, data} 契约
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 成功：直接返回 data
    if (res.code === 200) {
      return res.data
    }
    // 未登录：清理本地认证信息并跳转登录页
    if (res.code === 401) {
      clearAuth()
      const redirect = window.location.pathname + window.location.search
      window.location.href = `/login?redirect=${encodeURIComponent(redirect)}`
      return Promise.reject(new Error(res.msg || '未登录'))
    }
    // 其他业务错误：提示后端 msg
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    // HTTP 层错误：后端返回 401 状态码的情况
    if (error.response && error.response.status === 401) {
      clearAuth()
      const redirect = window.location.pathname + window.location.search
      window.location.href = `/login?redirect=${encodeURIComponent(redirect)}`
      return Promise.reject(error)
    }
    // 网络异常兜底
    ElMessage.error('网络异常，请稍后再试')
    return Promise.reject(error)
  }
)

export default request
