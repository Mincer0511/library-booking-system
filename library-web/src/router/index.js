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
      },
      // ===== Sprint 2 新增页面 =====
      {
        path: 'my/booking/:id',
        name: 'MyBookingDetail',
        component: () => import('@/views/my/BookingDetailView.vue'),
        meta: { title: '预约详情', requireAuth: true }
      },
      {
        path: 'my/credit',
        name: 'MyCredit',
        component: () => import('@/views/my/CreditView.vue'),
        meta: { title: '我的信用', requireAuth: true }
      },
      {
        path: 'my/violation',
        name: 'MyViolation',
        component: () => import('@/views/my/ViolationView.vue'),
        meta: { title: '违规记录', requireAuth: true }
      },
      {
        path: 'my/profile',
        name: 'MyProfile',
        component: () => import('@/views/my/ProfileView.vue'),
        meta: { title: '个人中心', requireAuth: true }
      },
      {
        path: 'my/favorite',
        name: 'MyFavorite',
        component: () => import('@/views/my/FavoriteView.vue'),
        meta: { title: '我的收藏', requireAuth: true }
      },
      {
        path: 'my/message',
        name: 'MyMessage',
        component: () => import('@/views/my/MessageView.vue'),
        meta: { title: '消息中心', requireAuth: true }
      },
      // 公告与帮助：未登录也可访问（不加 requireAuth）
      {
        path: 'notice',
        name: 'NoticeList',
        component: () => import('@/views/notice/NoticeListView.vue'),
        meta: { title: '公告列表' }
      },
      {
        path: 'notice/:id',
        name: 'NoticeDetail',
        component: () => import('@/views/notice/NoticeDetailView.vue'),
        meta: { title: '公告详情' }
      },
      {
        path: 'help',
        name: 'Help',
        component: () => import('@/views/HelpView.vue'),
        meta: { title: '帮助中心' }
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
