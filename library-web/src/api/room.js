import request from './request'

// 楼层与自习室相关接口

// 获取楼层列表
export function getFloors() {
  return request.get('/room/floors')
}

// 获取自习室列表（支持楼层 / 类型 / 关键词筛选）
export function getRoomList(params) {
  return request.get('/room/list', { params })
}

// 获取自习室详情
export function getRoomDetail(id) {
  return request.get(`/room/${id}`)
}

// 获取自习室座位图（date: yyyy-MM-dd）
export function getSeatMap(id, date) {
  return request.get(`/room/${id}/seat-map`, { params: { date } })
}
