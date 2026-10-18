<template>
  <div class="register-page">
    <div class="register-card">
      <h2 class="title">注册账号</h2>
      <p class="subtitle">加入罗文图书馆自习室，轻松预约好座位</p>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" label-width="80px">
        <el-form-item label="学号" prop="username">
          <el-input v-model="form.username" placeholder="6-20 位数字学号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="1 开头 11 位手机号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="至少 6 位密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" round style="width: 100%" :loading="loading" @click="onRegister">
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="links">
        <router-link to="/login" class="link">已有账号？去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'
import { isValidStudentNo, isValidPhone } from '@/utils/validate'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = ref({
  username: '',
  name: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

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
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      validator: (r, v, cb) =>
        isValidPhone(v) ? cb() : cb(new Error('手机号格式不正确')),
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (r, v, cb) =>
        v === form.value.password ? cb() : cb(new Error('两次输入的密码不一致')),
      trigger: 'blur'
    }
  ]
}

// 注册提交
async function onRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    const { username, password, name, phone } = form.value
    await register({ username, password, name, phone })
    ElMessage.success('注册成功，请登录~')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-card {
  width: 480px;
  max-width: 100%;
  background: #fff;
  border-radius: 20px;
  box-shadow: var(--shadow-card);
  padding: 40px;
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
  text-align: center;
}

.link {
  color: var(--color-primary);
  font-size: 13px;
  text-decoration: none;
}
</style>
