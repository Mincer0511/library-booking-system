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
          <router-link to="/notice" class="nav-item" active-class="active">公告</router-link>
          <router-link to="/help" class="nav-item" active-class="active">帮助中心</router-link>
        </nav>

        <!-- 右侧用户区 -->
        <div class="user-area">
          <template v-if="userStore.isLogin">
            <!-- 消息入口（学生角色显示，带未读角标） -->
            <el-badge
              v-if="isStudent"
              :value="unreadCount"
              :hidden="!unreadCount"
              :max="99"
              class="msg-badge"
            >
              <el-icon class="msg-bell" :size="20" @click="$router.push('/my/message')">
                <Bell />
              </el-icon>
            </el-badge>

            <el-dropdown @command="handleCommand">
              <span class="user-name">
                {{ userStore.userInfo?.name || userStore.userInfo?.nickname || userStore.userInfo?.username || '同学' }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">👤 个人中心</el-dropdown-item>
                  <el-dropdown-item command="booking">📅 我的预约</el-dropdown-item>
                  <el-dropdown-item command="credit">💯 我的信用</el-dropdown-item>
                  <el-dropdown-item command="violation">📋 违规记录</el-dropdown-item>
                  <el-dropdown-item command="favorite">⭐ 我的收藏</el-dropdown-item>
                  <el-dropdown-item command="message">🔔 消息中心</el-dropdown-item>
                  <el-dropdown-item command="help">🎡 帮助中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>

          <!-- 未登录：显示登录入口（公告 / 帮助页可免登录访问） -->
          <router-link v-else to="/login" class="nav-item">登录 / 注册</router-link>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ArrowDown, Bell } from '@element-plus/icons-vue'
import { getUnreadCount } from '@/api/message'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 未读消息数
const unreadCount = ref(0)

// 学生角色才显示消息入口（管理员不展示；role 缺省按学生处理）
const isStudent = computed(
  () => String(userStore.userInfo?.role || 'STUDENT').toUpperCase() === 'STUDENT'
)

// 拉取未读数
async function fetchUnread() {
  if (!userStore.isLogin || !isStudent.value) return
  try {
    unreadCount.value = (await getUnreadCount()) || 0
  } catch (e) {
    // 未读数加载失败不阻塞页面
  }
}

onMounted(fetchUnread)

// 路由切换后刷新未读数（如读完消息返回其他页面）
watch(() => route.path, fetchUnread)

// 下拉菜单指令处理
function handleCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    return
  }
  // 帮助中心为公共页面，其余在 /my 下
  router.push(cmd === 'help' ? '/help' : `/my/${cmd}`)
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

.user-area {
  display: flex;
  align-items: center;
  gap: 18px;
}

.msg-badge {
  display: flex;
  align-items: center;
}

.msg-bell {
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: color 0.2s ease;
}

.msg-bell:hover {
  color: var(--color-primary);
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
