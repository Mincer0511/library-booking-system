import request from '@/api/request'

// 管理端：公告管理（对应 AdminNoticeController）

// 公告分页（keyword 标题模糊、status 上下架状态）
export function getNoticePage(params) {
  return request.get('/admin/notice/page', { params })
}

// 发布公告（data: { title, content, isTop, status }）
export function addNotice(data) {
  return request.post('/admin/notice', data)
}

// 修改公告
export function updateNotice(noticeId, data) {
  return request.put(`/admin/notice/${noticeId}`, data)
}

// 删除公告
export function deleteNotice(noticeId) {
  return request.delete(`/admin/notice/${noticeId}`)
}

// 设置公告置顶（isTop: 1 置顶 / 0 取消）
export function updateNoticeTop(noticeId, isTop) {
  return request.put(`/admin/notice/${noticeId}/top/${isTop}`)
}

// 设置公告上下架（status: 1 上架 / 0 下架）
export function updateNoticeStatus(noticeId, status) {
  return request.put(`/admin/notice/${noticeId}/status/${status}`)
}
