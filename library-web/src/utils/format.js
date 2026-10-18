// 格式化工具函数
import dayjs from 'dayjs'

// 将 "HH:mm:ss" 格式化为 "HH:mm"（为空则原样返回）
export function formatTime(t) {
  if (!t) return t
  return t.slice(0, 5)
}

// 日期时间格式化（默认 yyyy-MM-dd HH:mm）
export function formatDateTime(t, pattern = 'YYYY-MM-DD HH:mm') {
  if (!t) return ''
  return dayjs(t).format(pattern)
}

// 订单状态映射
export const BOOKING_STATUS = {
  0: { text: '待签到', type: 'warning' },
  1: { text: '进行中', type: 'primary' },
  2: { text: '已完成', type: 'success' },
  3: { text: '已取消', type: 'info' },
  4: { text: '已爽约', type: 'danger' }
}

export function bookingStatusText(status) {
  return BOOKING_STATUS[status] ? BOOKING_STATUS[status].text : '未知'
}

// 自习室类型映射（后端直接返回中文：普通/静音/研讨）
export const ROOM_TYPE = {
  普通: { text: '普通自习室', type: 'primary' },
  静音: { text: '静音自习室', type: 'success' },
  研讨: { text: '研讨室', type: 'warning' }
}

export function roomTypeText(type) {
  return ROOM_TYPE[type] ? ROOM_TYPE[type].text : type
}

export function roomTypeTag(type) {
  return ROOM_TYPE[type] ? ROOM_TYPE[type].type : 'primary'
}

// 违规类型标签颜色（爽约/占座/其他）
export function violationTagType(type) {
  const map = { 爽约: 'danger', 占座: 'warning' }
  return map[type] || 'info'
}
