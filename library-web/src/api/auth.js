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

// 忘记密码：发送模拟验证码 {username, phone} → {code, expireMinutes, tip}
export function sendForgetCode(data) {
  return request.post('/auth/forget/send-code', data)
}

// 忘记密码：验证码重置密码 {username, code, newPassword}
export function resetForgetPassword(data) {
  return request.post('/auth/forget/reset', data)
}
