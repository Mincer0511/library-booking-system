<template>
  <div class="page-container">
    <div class="card-container profile-card">
      <h2 class="title">个人中心 👤</h2>

      <el-tabs v-model="activeTab">
        <!-- ===== 基本资料 ===== -->
        <el-tab-pane label="基本资料" name="profile">
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="80px"
            style="max-width: 460px"
          >
            <el-form-item label="学号">
              <el-input :model-value="userStore.userInfo?.username" disabled />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" maxlength="20" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" round :loading="savingProfile" @click="onSaveProfile">
                保存资料
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- ===== 头像上传 ===== -->
        <el-tab-pane label="头像上传" name="avatar">
          <div class="avatar-section">
            <div class="avatar-preview">
              <img v-if="avatarUrl" :src="avatarUrl" alt="当前头像" />
              <span v-else class="avatar-placeholder">🧑‍🎓</span>
            </div>
            <p class="text-secondary">支持 jpg / png / webp，大小不超过 2MB</p>
            <el-upload
              :show-file-list="false"
              accept=".jpg,.jpeg,.png,.webp"
              :before-upload="beforeAvatarUpload"
              :http-request="doUploadAvatar"
            >
              <el-button type="primary" round :loading="uploading">选择图片上传</el-button>
            </el-upload>
          </div>
        </el-tab-pane>

        <!-- ===== 修改密码 ===== -->
        <el-tab-pane label="修改密码" name="password">
          <el-form
            ref="pwdFormRef"
            :model="pwdForm"
            :rules="pwdRules"
            label-width="80px"
            style="max-width: 460px"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="pwdForm.oldPassword"
                type="password"
                placeholder="请输入原密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="pwdForm.newPassword"
                type="password"
                placeholder="6-20 位新密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="pwdForm.confirmPassword"
                type="password"
                placeholder="再次输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" round :loading="savingPwd" @click="onSavePassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateProfile, uploadAvatar, updatePassword } from '@/api/user'
import { isValidPhone } from '@/utils/validate'

const userStore = useUserStore()

const activeTab = ref('profile')

// ===== 基本资料 =====
const profileFormRef = ref(null)
const savingProfile = ref(false)
const profileForm = ref({ nickname: '', phone: '' })

const profileRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      validator: (r, v, cb) => (isValidPhone(v) ? cb() : cb(new Error('手机号为 1 开头 11 位数字'))),
      trigger: 'blur'
    }
  ]
}

// 回填当前资料
onMounted(() => {
  profileForm.value.nickname = userStore.userInfo?.nickname || ''
  profileForm.value.phone = userStore.userInfo?.phone || ''
})

// 保存基本资料
async function onSaveProfile() {
  await profileFormRef.value.validate()
  savingProfile.value = true
  try {
    await updateProfile({
      nickname: profileForm.value.nickname,
      phone: profileForm.value.phone
    })
    // 刷新 user store 中的用户信息
    await userStore.fetchUserInfo()
    ElMessage.success('资料保存成功~')
  } finally {
    savingProfile.value = false
  }
}

// ===== 头像上传 =====
const uploading = ref(false)
// 上传后时间戳，用于刷新头像缓存
const avatarTs = ref(0)

// 头像完整地址（后端返回 /uploads/avatar/xxx.png，需拼 8080 域名）
const avatarUrl = computed(() => {
  const url = userStore.userInfo?.avatar
  if (!url) return ''
  return `http://localhost:8080${url}?t=${avatarTs.value}`
})

// 上传前校验：格式 + 大小
function beforeAvatarUpload(file) {
  const okTypes = ['image/jpeg', 'image/png', 'image/webp']
  if (!okTypes.includes(file.type)) {
    ElMessage.error('仅支持 jpg / png / webp 格式的图片')
    return false
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('头像大小不能超过 2MB')
    return false
  }
  return true
}

// 自定义上传：FormData 字段名 file
async function doUploadAvatar(options) {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    await uploadAvatar(formData)
    // 刷新用户信息并更新缓存时间戳
    await userStore.fetchUserInfo()
    avatarTs.value = Date.now()
    ElMessage.success('头像上传成功~')
  } finally {
    uploading.value = false
  }
}

// ===== 修改密码 =====
const pwdFormRef = ref(null)
const savingPwd = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
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
        v === pwdForm.value.newPassword ? cb() : cb(new Error('两次输入的密码不一致')),
      trigger: 'blur'
    }
  ]
}

// 提交修改密码：成功后提示重新登录并退出
async function onSavePassword() {
  await pwdFormRef.value.validate()
  savingPwd.value = true
  try {
    await updatePassword({
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    })
    await ElMessageBox.alert('密码修改成功，请使用新密码重新登录', '修改成功', {
      confirmButtonText: '去登录',
      type: 'success'
    })
    userStore.logout()
  } finally {
    savingPwd.value = false
  }
}
</script>

<style scoped>
.profile-card {
  max-width: 720px;
  margin: 20px auto;
}

.title {
  font-size: 20px;
  margin-bottom: 12px;
  color: var(--color-text-primary);
}

/* 头像区 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  padding: 20px 0;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: var(--color-primary-light);
  border: 3px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 52px;
}
</style>
