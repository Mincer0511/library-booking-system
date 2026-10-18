import router from './index'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores/user'

const SYSTEM_NAME = '罗文校区图书馆自习室预约系统'

// 当前用户角色（小写键：student / librarian / super_admin）
function currentRole() {
  const userStore = useUserStore()
  return String(userStore.userInfo?.role || '').toLowerCase()
}

// 全局前置守卫：登录校验 + 角色校验 + 页面标题
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - ${SYSTEM_NAME}` : SYSTEM_NAME

  const token = getToken()

  // 需要登录的页面：无 token 则跳登录页并携带回跳地址
  if (to.meta.requireAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 角色校验：页面声明了 roles 且当前角色不在其中 → 403
  if (to.meta.roles && !to.meta.roles.includes(currentRole())) {
    next('/403')
    return
  }

  // 已登录用户访问学生端登录页：直接回首页
  if (to.path === '/login' && token) {
    next('/home')
    return
  }

  // 已登录用户访问管理端登录页：管理员回管理看板，学生回前台首页
  if (to.path === '/admin/login' && token) {
    const role = currentRole()
    if (role === 'librarian' || role === 'super_admin') {
      next('/admin/dashboard')
    } else {
      next('/home')
    }
    return
  }

  next()
})
