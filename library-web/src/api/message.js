import request from './request'

// 消息中心相关接口

// 消息分页 {isRead, pageNum, pageSize} → {list, total}
export function getMessagePage(params) {
  return request.get('/message/page', { params })
}

// 单条标记已读
export function markMessageRead(id) {
  return request.put(`/message/${id}/read`)
}

// 全部标记已读
export function markAllMessageRead() {
  return request.put('/message/read-all')
}

// 未读数（data 为数字）
export function getUnreadCount() {
  return request.get('/message/unread-count')
}
