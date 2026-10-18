<template>
  <div class="admin-layout">
    <!-- 左侧固定侧边栏 -->
    <aside class="admin-aside">
      <!-- logo 区 -->
      <div class="aside-logo" @click="$router.push('/admin/dashboard')">
        <span class="logo-icon">📚</span>
        <span class="logo-text">图书馆管理后台</span>
      </div>

      <!-- 导航菜单：按当前登录角色过滤 -->
      <el-menu
        :default-active="activeMenu"
        router
        class="aside-menu"
      >
        <el-menu-item
          v-for="item in menuList"
          :key="item.path"
          :index="item.path"
        >
          <span>{{ item.icon }} {{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- 右侧主区域 -->
    <div class="admin-container">
      <!-- 顶部 header -->
      <header class="admin-header">
        <div class="header-title">{{ currentTitle }}</div>

        <!-- 右侧管理员信息 + 下拉 -->
        <el-dropdown @command="handleCommand">
          <span class="admin-name">
            👨‍💼 {{ userStore.userInfo?.name || userStore.userInfo?.username || '管理员' }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="home">🏠 返回前台</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <!-- 主内容区 -->
      <main class="admin-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前角色（小写键：librarian / super_admin / student）
const role = computed(() => String(userStore.userInfo?.role || '').toLowerCase())

// 全量菜单定义；superAdminOnly 为 true 的菜单仅超管可见
const ALL_MENUS = [
  { path: '/admin/dashboard', icon: '📊', title: '数据看板', superAdminOnly: false },
  { path: '/admin/room', icon: '🏢', title: '自习室管理', superAdminOnly: false },
  { path: '/admin/booking', icon: '📋', title: '订单运营', superAdminOnly: false },
  { path: '/admin/violation', icon: '⚠️', title: '违规管理', superAdminOnly: false },
  { path: '/admin/notice', icon: '📢', title: '公告管理', superAdminOnly: false },
  { path: '/admin/statistics', icon: '📈', title: '数据统计', superAdminOnly: false },
  { path: '/admin/disable', icon: '🚫', title: '禁用管理', superAdminOnly: true },
  { path: '/admin/user', icon: '👥', title: '用户管理', superAdminOnly: true },
  { path: '/admin/setting', icon: '⚙️', title: '系统设置', superAdminOnly: true },
  { path: '/admin/log', icon: '📜', title: '操作日志', superAdminOnly: true }
]

// 按角色过滤后的菜单
const menuList = computed(() =>
  ALL_MENUS.filter((item) => !item.superAdminOnly || role.value === 'super_admin')
)

// 当前激活菜单（与路由路径一致）
const activeMenu = computed(() => route.path)

// 页面标题：取当前菜单名
const currentTitle = computed(() => {
  const hit = ALL_MENUS.find((item) => item.path === route.path)
  return hit ? hit.title : '管理后台'
})

// 下拉指令处理
function handleCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    return
  }
  router.push('/home')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: flex;
  background: var(--color-bg-page);
}

/* 左侧边栏：白底卡片感 */
.admin-aside {
  width: 220px;
  flex-shrink: 0;
  background: #fff;
  box-shadow: var(--shadow-card);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
}

.aside-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 20px 18px;
  cursor: pointer;
  border-bottom: 1px solid var(--color-primary-light);
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-primary);
}

.aside-menu {
  flex: 1;
  border-right: none;
  padding: 12px 10px;
  overflow-y: auto;
}

/* 菜单项：圆角胶囊感，选中 #4A90E2 */
.aside-menu :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin-bottom: 6px;
  border-radius: var(--radius-base);
  color: var(--color-text-primary);
  transition: all 0.2s ease;
}

.aside-menu :deep(.el-menu-item:hover) {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.aside-menu :deep(.el-menu-item.is-active) {
  background: var(--color-primary);
  color: #fff;
}

/* 右侧主区域 */
.admin-container {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.admin-header {
  height: 60px;
  background: #fff;
  box-shadow: var(--shadow-card);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.admin-name {
  cursor: pointer;
  color: var(--color-text-primary);
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 主内容区：内边距 + 卡片留白 */
.admin-main {
  flex: 1;
  padding: 20px;
}
</style>
