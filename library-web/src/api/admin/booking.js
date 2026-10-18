import request from '@/api/request'

// 管理端：订单运营（对应 AdminBookingController）

// 订单分页筛选（userId、roomId、bookingDate、status）
export function getBookingPage(params) {
  return request.get('/admin/booking/page', { params })
}

// 强制取消订单（data: { reason }）
export function forceCancelOrder(orderId, data) {
  return request.put(`/admin/booking/${orderId}/force-cancel`, data)
}

// 修改订单时间（data: { startTime, endTime } 等，以后端 BookingTimeDTO 为准）
export function updateBookingTime(orderId, data) {
  return request.put(`/admin/booking/${orderId}/time`, data)
}

// 修改订单备注
export function updateBookingRemark(orderId, remark) {
  return request.put(`/admin/booking/${orderId}/remark`, { remark })
}
