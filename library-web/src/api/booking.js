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

// 签到
export function checkinBooking(id) {
  return request.put(`/booking/${id}/checkin`)
}

// 签退
export function checkoutBooking(id) {
  return request.put(`/booking/${id}/checkout`)
}

// 续时 {newEndTime: "HH:mm"}
export function extendBooking(id, newEndTime) {
  return request.put(`/booking/${id}/extend`, { newEndTime })
}

// 我的预约分页列表
export function getMyBookings(params) {
  return request.get('/booking/my', { params })
}

// 我的预约详情
export function getBookingDetail(id) {
  return request.get(`/booking/my/${id}`)
}
