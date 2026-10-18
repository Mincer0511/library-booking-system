<template>
  <div class="forget-page">
    <div class="forget-card">
      <img class="forget-img" :src="forgetImg" alt="忘记密码" />
      <h2 class="title">找回密码</h2>

      <!-- 步骤条 -->
      <el-steps :active="step" align-center finish-status="success" class="steps">
        <el-step title="验证身份" />
        <el-step title="重置密码" />
      </el-steps>

      <!-- 步骤 1：学号 + 手机号，发送模拟验证码 -->
      <el-form
        v-if="step === 1"
        ref="step1FormRef"
        :model="step1Form"
        :rules="step1Rules"
        size="large"
        style="width: 100%"
        @keyup.enter="onSendCode"
      >
        <el-form-item prop="username">
          <el-input v-model="step1Form.username" placeholder="请输入学号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="step1Form.phone" placeholder="请输入绑定手机号" :prefix-icon="Iphone" />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            round
            :loading="sending"
            @click="onSendCode"
          >
            获取验证码
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 步骤 2：验证码 + 新密码 -->
      <el-form
        v-else
        ref="step2FormRef"
        :model="step2Form"
        :rules="step2Rules"
        size="large"
        style="width: 100%"
        @keyup.enter="onReset"
      >
        <el-form-item prop="code">
          <el-input v-model="step2Form.code" placeholder="请输入验证码" :prefix-icon="Key" />
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input
            v-model="step2Form.newPassword"
            type="password"
            placeholder="6-20 位新密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="step2Form.confirmPassword"
            type="password"
            placeholder="再次输入新密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            round
            :loading="resetting"
            @click="onReset"
          >
            重置密码
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button style="width: 100%" round @click="step = 1">返回上一步</el-button>
        </el-form-item>
      </el-form>

      <router-link to="/login" class="link">返回登录</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock, Iphone, Key } from '@element-plus/icons-vue'
import { sendForgetCode, resetForgetPassword } from '@/api/auth'
import { isValidStudentNo, isValidPhone } from '@/utils/validate'

// 卡通插画
const forgetImg =
  'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=' +
  encodeURIComponent('cute cartoon empty open book, blue pastel, flat illustration') +
  '&image_size=square_hd'

const router = useRouter()

// 当前步骤：1 验证身份 / 2 重置密码
const step = ref(1)

// ===== 步骤 1 =====
const step1FormRef = ref(null)
const sending = ref(false)
const step1Form = ref({ username: '', phone: '' })

const step1Rules = {
  username: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    {
      validator: (r, v, cb) =>
        isValidStudentNo(v) ? cb() : cb(new Error('学号为 6-20 位数字')),
      trigger: 'blur'
    }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      validator: (r, v, cb) =>
        isValidPhone(v) ? cb() : cb(new Error('手机号为 1 开头 11 位数字')),
      trigger: 'blur'
    }
  ]
}

// 发送模拟验证码：回显 code 并自动填入步骤 2
async function onSendCode() {
  await step1FormRef.value.validate()
  sending.value = true
  try {
    const data = await sendForgetCode({
      username: step1Form.value.username,
      phone: step1Form.value.phone
    })
    // 模拟验证码直接回显
    await ElMessageBox.alert(
      `模拟验证码：${data.code}，${data.expireMinutes} 分钟内有效`,
      '验证码已发送',
      { confirmButtonText: '我知道了', type: 'success' }
    )
    // 自动填入下一步输入框
    step2Form.value.code = String(data.code)
    step.value = 2
  } finally {
    sending.value = false
  }
}

// ===== 步骤 2 =====
const step2FormRef = ref(null)
const resetting = ref(false)
const step2Form = ref({ code: '', newPassword: '', confirmPassword: '' })

const step2Rules = {
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      validator: (r, v, cb) =>
        v.length >= 6 && v.length <= 20 ? cb() : cb(new Error('新密码长度为 6-20 位')),
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (r, v, cb) =>
        v === step2Form.value.newPassword ? cb() : cb(new Error('两次输入的密码不一致')),
      trigger: 'blur'
    }
  ]
}

// 提交重置
async function onReset() {
  await step2FormRef.value.validate()
  resetting.value = true
  try {
    await resetForgetPassword({
      username: step1Form.value.username,
      code: step2Form.value.code,
      newPassword: step2Form.value.newPassword
    })
    ElMessage.success('密码重置成功，请使用新密码登录~')
    router.push('/login')
  } finally {
    resetting.value = false
  }
}
</script>

<style scoped>
.forget-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.forget-card {
  width: 460px;
  max-width: 100%;
  background: #fff;
  border-radius: 20px;
  box-shadow: var(--shadow-card);
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18px;
}

.forget-img {
  width: 120px;
  height: 120px;
  border-radius: 16px;
  object-fit: cover;
}

.title {
  font-size: 22px;
  color: var(--color-text-primary);
}

.steps {
  width: 100%;
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
