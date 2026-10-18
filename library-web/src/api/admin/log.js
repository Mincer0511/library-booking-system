import request from '@/api/request'

// 管理端：操作日志（对应 AdminLogController）

// 操作日志分页（module 模块模糊、userId 操作人精确）
export function getLogPage(params) {
  return request.get('/admin/log/page', { params })
}
