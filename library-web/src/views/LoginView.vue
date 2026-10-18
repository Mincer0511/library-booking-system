<template>
  <div class="login-page">
    <div class="login-box">
      <!-- 左侧卡通插画 -->
      <div class="illustration">
        <img :src="loginImg" alt="自习室插画" />
        <p class="slogan">📚 预约好座位，学习更高效</p>
      </div>

      <!-- 右侧登录卡片 -->
      <div class="login-card">
        <h2 class="title">欢迎登录</h2>
        <p class="subtitle">罗文校区图书馆自习室预约系统</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          @keyup.enter="onLogin"
        >
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入学号" :prefix-icon="User" />
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
            <el-checkbox v-model="remember">记住学号</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              style="width: 100%"
              round
              :loading="loading"
              @click="onLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="links">
          <router-link to="/register" class="link">去注册</router-link>
          <router-link to="/forget" class="link">忘记密码</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { isValidStudentNo } from '@/utils/validate'

// 登录页卡通插画
const loginImg =
  'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=' +
  encodeURIComponent(
    'cute cartoon library study room, blue and white, books and seats, flat vector illustration, soft pastel, kawaii campus style'
  ) +
  '&image_size=square_hd'

const REMEMBER_KEY = 'library_remember_username'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const remember = ref(false)
const form = ref({ username: '', password: '' })

// 表单校验规则
const rules = {
  username: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    {
      validator: (r, v, cb) =>
        isValidStudentNo(v) ? cb() : cb(new Error('学号为 6-20 位数字')),
      trigger: 'blur'
    }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 挂载时回填记住的学号
onMounted(() => {
  const saved = localStorage.getItem(REMEMBER_KEY)
  if (saved) {
    form.value.username = saved
    remember.value = true
  }
})

// 登录提交
async function onLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form.value)
    // 记住学号处理
    if (remember.value) {
      localStorage.setItem(REMEMBER_KEY, form.value.username)
    } else {
      localStorage.removeItem(REMEMBER_KEY)
    }
    ElMessage.success('登录成功，欢迎回来~')
    router.push(route.query.redirect || '/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-box {
  display: flex;
  width: 860px;
  max-width: 100%;
  background: #fff;
  border-radius: 20px;
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

.illustration {
  flex: 1;
  background: var(--color-primary-light);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
  gap: 16px;
}

.illustration img {
  width: 100%;
  max-width: 340px;
  border-radius: 16px;
}

.slogan {
  color: var(--color-primary);
  font-size: 15px;
  font-weight: 600;
}

.login-card {
  flex: 1;
  padding: 48px 40px;
}

.title {
  font-size: 24px;
  color: var(--color-text-primary);
}

.subtitle {
  margin: 8px 0 24px;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.links {
  display: flex;
  justify-content: space-between;
}

.link {
  color: var(--color-primary);
  font-size: 13px;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

/* 小屏适配：上下布局 */
@media (max-width: 720px) {
  .login-box {
    flex-direction: column;
  }
}
</style>
