import request from '@/api/request'

// 管理端：违规管理（对应 AdminViolationController）

// 违规记录分页（userId、violationType）
export function getViolationPage(params) {
  return request.get('/admin/violation/page', { params })
}

// 手动添加违规（联动扣分 + 禁用判定）
export function addViolation(data) {
  return request.post('/admin/violation', data)
}

// 撤销违规记录（联动恢复信用分）
export function revokeViolation(violationId) {
  return request.put(`/admin/violation/${violationId}/revoke`)
}
