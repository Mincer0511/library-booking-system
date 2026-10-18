import request from '@/api/request'

// 管理端：用户管理（对应 AdminUserController，超管专属）

// 用户分页（keyword 学号/姓名模糊、roleId、status）
export function getUserPage(params) {
  return request.get('/admin/user/page', { params })
}

// 角色列表（下拉选择用）
export function getRoleList() {
  return request.get('/admin/user/role/list')
}

// 新增用户（不传 password 默认 123456）
export function addUser(data) {
  return request.post('/admin/user', data)
}

// 修改用户信息（姓名/昵称/手机号/角色/状态）
export function updateUser(userId, data) {
  return request.put(`/admin/user/${userId}`, data)
}

// 重置用户密码（重置为 123456）
export function resetUserPassword(userId) {
  return request.put(`/admin/user/${userId}/reset-password`)
}
