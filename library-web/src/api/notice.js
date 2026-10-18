import request from './request'

// 公告相关接口

// 公告列表（已按置顶优先 + 时间倒序）
export function getNoticeList() {
  return request.get('/notice/list')
}

// 公告详情
export function getNoticeDetail(id) {
  return request.get(`/notice/detail/${id}`)
}
