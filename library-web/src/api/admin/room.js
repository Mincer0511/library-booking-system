import request from '@/api/request'

// 管理端：楼层 / 自习室 / 座位管理（对应 AdminRoomController）

// 楼层列表
export function getFloorList() {
  return request.get('/admin/room/floors')
}

// 自习室分页（keyword 关键字、floorId 楼层、status 状态）
export function getRoomPage(params) {
  return request.get('/admin/room/page', { params })
}

// 新增自习室
export function addRoom(data) {
  return request.post('/admin/room', data)
}

// 修改自习室
export function updateRoom(roomId, data) {
  return request.put(`/admin/room/${roomId}`, data)
}

// 修改自习室开放状态
export function updateRoomStatus(roomId, status) {
  return request.put(`/admin/room/${roomId}/status/${status}`)
}

// 删除自习室
export function deleteRoom(roomId) {
  return request.delete(`/admin/room/${roomId}`)
}

// 座位列表（平面图坐标排序）
export function getSeatList(roomId) {
  return request.get(`/admin/room/${roomId}/seats`)
}

// 批量生成座位
export function batchGenerateSeats(roomId, data) {
  return request.post(`/admin/room/${roomId}/seats/batch`, data)
}

// 修改座位
export function updateSeat(seatId, data) {
  return request.put(`/admin/room/seat/${seatId}`, data)
}

// 删除座位
export function deleteSeat(seatId) {
  return request.delete(`/admin/room/seat/${seatId}`)
}
