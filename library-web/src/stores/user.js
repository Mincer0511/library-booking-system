import { defineStore } from 'pinia'
import { login as loginApi, getUserInfoApi } from '@/api/auth'
import {
  getToken,
  setToken,
  getUserInfo,
  setUserInfo,
  clearAuth
} from '@/utils/auth'

// 用户状态：token + userInfo
export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userInfo: getUserInfo()
  }),
  getters: {
    isLogin: (state) => !!state.token
  },
  actions: {
    // 登录：存 token + userInfo（后端角色字段为 roleKey，统一归一化为 role 供路由守卫与布局使用）
    async login(form) {
      const data = await loginApi(form)
      data.userInfo.role = data.userInfo.roleKey || data.userInfo.role || 'student'
      this.token = data.token
      this.userInfo = data.userInfo
      setToken(data.token)
      setUserInfo(data.userInfo)
      return data
    },
    // 拉取最新用户信息
    async fetchUserInfo() {
      const data = await getUserInfoApi()
      data.role = data.roleKey || data.role || 'student'
      this.userInfo = data
      setUserInfo(data)
      return data
    },
    // 退出登录：清理本地状态并跳登录页
    logout() {
      clearAuth()
      this.token = ''
      this.userInfo = null
      window.location.href = '/login'
    }
  }
})
