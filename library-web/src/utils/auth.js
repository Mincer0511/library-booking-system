// token 与用户信息本地存储工具
const TOKEN_KEY = 'library_token'
const USER_KEY = 'library_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getUserInfo() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

export function setUserInfo(userInfo) {
  localStorage.setItem(USER_KEY, JSON.stringify(userInfo))
}

// 清理所有认证信息（token + userInfo）
export function clearAuth() {
  removeToken()
  localStorage.removeItem(USER_KEY)
}
