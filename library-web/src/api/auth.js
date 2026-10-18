import request from './request'

// 认证与用户相关接口

// 注册
export function register(data) {
  return request.post('/auth/register', data)
}

// 登录
export function login(data) {
  return request.post('/auth/login', data)
}

// 获取当前登录用户信息
export function getUserInfoApi() {
  return request.get('/auth/info')
}
