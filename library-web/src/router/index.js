import { createRouter, createWebHistory } from 'vue-router'

// 路由表：Sprint 1 页面
const routes = [
  { path: '/', redirect: '/home' },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/forget',
    name: 'Forget',
    component: () => import('@/views/ForgetView.vue'),
    meta: { title: '忘记密码' }
  },
  {
    // 登录后的用户布局
    path: '/',
    component: () => import('@/layout/UserLayout.vue'),
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/HomeView.vue'),
        meta: { title: '首页', requireAuth: true }
      },
      {
        path: 'room/:id',
        name: 'RoomDetail',
        component: () => import('@/views/room/RoomDetailView.vue'),
        meta: { title: '自习室详情', requireAuth: true }
      },
      {
        path: 'book/confirm',
        name: 'BookingConfirm',
        component: () => import('@/views/book/BookingConfirmView.vue'),
        meta: { title: '确认预约', requireAuth: true }
      },
      {
        path: 'my/booking',
        name: 'MyBooking',
        component: () => import('@/views/my/BookingListView.vue'),
        meta: { title: '我的预约', requireAuth: true }
      }
    ]
  },
  { path: '/404', name: 'NotFound', component: () => import('@/views/error/NotFoundView.vue'), meta: { title: '页面不存在' } },
  // 兜底路由
  { path: '/:pathMatch(.*)*', redirect: '/404' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
