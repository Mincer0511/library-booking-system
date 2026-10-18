import request from './request'

// 预约相关接口

// 创建预约 {seatId, bookingDate, startTime, endTime}
export function createBooking(data) {
  return request.post('/booking', data)
}

// 取消预约
export function cancelBooking(id) {
  return request.put(`/booking/${id}/cancel`)
}

// 我的预约分页列表
export function getMyBookings(params) {
  return request.get('/booking/my', { params })
}

// 我的预约详情
export function getBookingDetail(id) {
  return request.get(`/booking/my/${id}`)
}
