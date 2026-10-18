import request from './request'

// 自习室收藏相关接口

// 收藏列表
export function getFavoriteList() {
  return request.get('/favorite/list')
}

// 已收藏的自习室 id 数组
export function getFavoriteIds() {
  return request.get('/favorite/ids')
}

// 收藏自习室
export function addFavorite(roomId) {
  return request.post(`/favorite/${roomId}`)
}

// 取消收藏
export function removeFavorite(roomId) {
  return request.delete(`/favorite/${roomId}`)
}
