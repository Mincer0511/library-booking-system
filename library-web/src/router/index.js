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
  { path: '/403', name: 'Forbidden', component: () => import('@/views/error/ForbiddenView.vue'), meta: { title: '无权限' } },

  // ===== 管理端（Sprint 3）=====
  {
    // 管理端独立登录页
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/AdminLoginView.vue'),
    meta: { title: '管理端登录' }
  },
  {
    // 管理端布局：librarian / super_admin 可进，页面级 roles 再细分
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    meta: { requireAuth: true, roles: ['librarian', 'super_admin'] },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '数据看板', requireAuth: true, roles: ['librarian', 'super_admin'] }
      },
      {
        path: 'room',
        name: 'AdminRoom',
        component: () => import('@/views/admin/RoomManageView.vue'),
        meta: { title: '自习室管理', requireAuth: true, roles: ['librarian', 'super_admin'] }
      },
      {
        path: 'booking',
        name: 'AdminBooking',
        component: () => import('@/views/admin/BookingManageView.vue'),
        meta: { title: '订单运营', requireAuth: true, roles: ['librarian', 'super_admin'] }
      },
      {
        path: 'violation',
        name: 'AdminViolation',
        component: () => import('@/views/admin/ViolationManageView.vue'),
        meta: { title: '违规管理', requireAuth: true, roles: ['librarian', 'super_admin'] }
      },
      {
        path: 'notice',
        name: 'AdminNotice',
        component: () => import('@/views/admin/NoticeManageView.vue'),
        meta: { title: '公告管理', requireAuth: true, roles: ['librarian', 'super_admin'] }
      },
      {
        path: 'statistics',
        name: 'AdminStatistics',
        component: () => import('@/views/admin/StatisticsView.vue'),
        meta: { title: '数据统计', requireAuth: true, roles: ['librarian', 'super_admin'] }
      },
      // ===== 以下仅超管可见 =====
      {
        path: 'disable',
        name: 'AdminDisable',
        component: () => import('@/views/admin/DisableManageView.vue'),
        meta: { title: '禁用管理', requireAuth: true, roles: ['super_admin'] }
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: () => import('@/views/admin/UserManageView.vue'),
        meta: { title: '用户管理', requireAuth: true, roles: ['super_admin'] }
      },
      {
        path: 'setting',
        name: 'AdminSetting',
        component: () => import('@/views/admin/SettingView.vue'),
        meta: { title: '系统设置', requireAuth: true, roles: ['super_admin'] }
      },
      {
        path: 'log',
        name: 'AdminLog',
        component: () => import('@/views/admin/LogView.vue'),
        meta: { title: '操作日志', requireAuth: true, roles: ['super_admin'] }
      }
    ]
  },
  // 兜底路由
  { path: '/:pathMatch(.*)*', redirect: '/404' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
