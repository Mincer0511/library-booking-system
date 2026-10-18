import request from '@/api/request'

// 管理端：禁用管理与信用分调整（对应 AdminDisableController，超管专属）

// 禁用记录分页（userId、isLifted）
export function getDisablePage(params) {
  return request.get('/admin/disable/page', { params })
}

// 手动提前解禁（恢复信用分 + 留痕 + 通知）
export function liftDisable(userId) {
  return request.put(`/admin/disable/lift/${userId}`)
}

// 调整用户信用分（data: { score, reason }）
export function adjustCredit(userId, data) {
  return request.put(`/admin/disable/credit/${userId}`, data)
}
