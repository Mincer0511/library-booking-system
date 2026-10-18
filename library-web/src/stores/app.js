import { defineStore } from 'pinia'
import { getFloors } from '@/api/room'

// 应用级状态：楼层列表缓存（只请求一次）
export const useAppStore = defineStore('app', {
  state: () => ({
    floors: [],
    floorsLoaded: false
  }),
  actions: {
    async fetchFloors() {
      // 已加载过则直接返回缓存
      if (this.floorsLoaded) return this.floors
      const data = await getFloors()
      this.floors = data || []
      this.floorsLoaded = true
      return this.floors
    }
  }
})
