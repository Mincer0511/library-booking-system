<template>
  <div class="user-layout">
    <!-- 顶部导航 -->
    <header class="layout-header">
      <div class="header-inner">
        <!-- 左侧 logo 区 -->
        <div class="logo-area" @click="$router.push('/home')">
          <span class="logo-icon">📚</span>
          <span class="logo-text">罗文图书馆自习室</span>
        </div>

        <!-- 中间菜单 -->
        <nav class="nav-menu">
          <router-link to="/home" class="nav-item" active-class="active">首页</router-link>
          <router-link to="/my/booking" class="nav-item" active-class="active">我的预约</router-link>
        </nav>

        <!-- 右侧用户区 -->
        <div class="user-area">
          <el-dropdown @command="handleCommand">
            <span class="user-name">
              {{ userStore.userInfo?.name || userStore.userInfo?.username || '同学' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 内容区 -->
    <main class="layout-main">
      <router-view />
    </main>

    <!-- 简洁 footer -->
    <footer class="layout-footer">
      <p>罗文校区图书馆自习室预约系统 · 请文明预约、按时签到</p>
    </footer>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { ArrowDown } from '@element-plus/icons-vue'

const userStore = useUserStore()

// 下拉菜单指令处理
function handleCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
  }
}
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.layout-header {
  background: #fff;
  box-shadow: var(--shadow-card);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.logo-icon {
  font-size: 26px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary);
}

.nav-menu {
  flex: 1;
  display: flex;
  gap: 24px;
}

.nav-item {
  text-decoration: none;
  color: var(--color-text-secondary);
  font-size: 15px;
  padding: 6px 14px;
  border-radius: 20px;
  transition: all 0.2s ease;
}

.nav-item:hover {
  color: var(--color-primary);
  background: var(--color-primary-light);
}

.nav-item.active {
  color: #fff;
  background: var(--color-primary);
}

.user-name {
  cursor: pointer;
  color: var(--color-text-primary);
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.layout-main {
  flex: 1;
}

.layout-footer {
  text-align: center;
  padding: 18px 0;
  color: var(--color-text-secondary);
  font-size: 13px;
}
</style>
