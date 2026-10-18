import router from './index'
import { getToken } from '@/utils/auth'

const SYSTEM_NAME = '罗文校区图书馆自习室预约系统'

// 全局前置守卫：登录校验 + 页面标题
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - ${SYSTEM_NAME}` : SYSTEM_NAME

  const token = getToken()

  // 需要登录的页面：无 token 则跳登录页并携带回跳地址
  if (to.meta.requireAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 已登录用户访问登录页：直接回首页
  if (to.path === '/login' && token) {
    next('/home')
    return
  }

  next()
})
