import request from './request'

// 个人中心相关接口

// 修改基本资料 {nickname, phone}
export function updateProfile(data) {
  return request.put('/user/profile', data)
}

// 上传头像（FormData，字段名 file）→ 返回 url 字符串
export function uploadAvatar(formData) {
  return request.post('/user/avatar', formData)
}

// 修改密码 {oldPassword, newPassword}
export function updatePassword(data) {
  return request.put('/user/password', data)
}

// 我的信用 {creditScore, isDisabled, disableEndTime, disableThreshold, recentViolations}
export function getMyCredit() {
  return request.get('/user/credit')
}

// 我的违规记录（全量数组）
export function getMyViolations() {
  return request.get('/user/violation')
}
