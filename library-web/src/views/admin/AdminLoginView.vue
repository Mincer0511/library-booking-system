<template>
  <div class="admin-login-page">
    <div class="login-card">
      <!-- logo 区 -->
      <div class="logo-area">
        <span class="logo-icon">📚</span>
        <h2 class="title">图书馆管理后台</h2>
        <p class="subtitle">罗文校区图书馆自习室预约系统</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        @keyup.enter="onLogin"
      >
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入管理员账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            round
            :loading="loading"
            @click="onLogin"
          >
            管理员登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="links">
        <router-link to="/login" class="link">← 返回学生端登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const form = ref({ username: '', password: '' })

// 表单校验：管理端账号不限于学号格式，仅做非空校验
const rules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 管理端登录：复用 userStore.login()，成功后按角色跳转
async function onLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form.value)
    const role = String(userStore.userInfo?.role || '').toLowerCase()
    if (role === 'librarian' || role === 'super_admin') {
      ElMessage.success('登录成功，欢迎回来~')
      router.push('/admin/dashboard')
    } else {
      // 学生账号无管理端权限，停留在登录页
      ElMessage.warning('当前账号无管理端权限')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 蓝白渐变背景 + 居中白卡片 */
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: linear-gradient(135deg, #dceeff 0%, #eef7ff 45%, #f7fbff 100%);
}

.login-card {
  width: 420px;
  max-width: 100%;
  background: #fff;
  border-radius: 20px;
  box-shadow: var(--shadow-card);
  padding: 48px 40px;
}

.logo-area {
  text-align: center;
  margin-bottom: 28px;
}

.logo-icon {
  font-size: 44px;
  display: inline-block;
}

.title {
  margin: 8px 0 0;
  font-size: 22px;
  color: var(--color-primary);
}

.subtitle {
  margin-top: 8px;
  color: var(--color-text-secondary);
  font-size: 13px;
}

.links {
  text-align: center;
  margin-top: 8px;
}

.link {
  color: var(--color-primary);
  font-size: 13px;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}
</style>
