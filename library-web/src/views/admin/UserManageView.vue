<template>
  <div class="admin-page">
    <!-- 搜索筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form inline @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="学号 / 姓名"
            clearable
            style="width: 200px"
            @keyup.enter="onSearch"
            @clear="onSearch"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="query.roleId"
            placeholder="全部角色"
            clearable
            style="width: 150px"
            @change="onSearch"
          >
            <el-option
              v-for="r in roleList"
              :key="r.roleId"
              :label="r.roleName"
              :value="r.roleId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="query.status"
            placeholder="全部状态"
            clearable
            style="width: 130px"
            @change="onSearch"
          >
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card shadow="never">
      <div class="table-header">
        <span class="table-title">用户列表</span>
        <el-button type="primary" @click="openAdd">＋ 新增用户</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="username" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" min-width="90" />
        <el-table-column label="角色" width="130">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row)" effect="light">
              {{ row.roleName || '未知角色' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="信用分" width="90" align="center">
          <template #default="{ row }">
            <span :class="['credit', creditClass(row.creditScore)]">
              {{ row.creditScore }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="预约禁用" min-width="150">
          <template #default="{ row }">
            <template v-if="row.isDisabled === 1">
              <el-tag type="warning" effect="light">禁用中</el-tag>
              <span v-if="row.disableEndTime" class="text-secondary">
                至 {{ formatDateTime(row.disableEndTime, 'MM-DD HH:mm') }}
              </span>
            </template>
            <el-tag v-else type="info" effect="plain">未禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">
              编辑
            </el-button>
            <el-button type="warning" link size="small" @click="onResetPassword(row)">
              重置密码
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState text="暂无匹配的用户~" />
        </template>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, prev, pager, next, sizes"
          background
          @current-change="fetchList"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <!-- 新增 / 编辑用户弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="520px"
      @closed="onDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="账号" prop="username">
          <el-input
            v-model="form.username"
            :disabled="isEdit"
            placeholder="学号/登录账号，4-20位字母或数字"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" maxlength="20" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" maxlength="20" placeholder="选填，不填默认同姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="选填，1开头的11位手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="r in roleList"
              :key="r.roleId"
              :label="r.roleName"
              :value="r.roleId"
            />
          </el-select>
        </el-form-item>
        <!-- 新增时可设置初始密码 -->
        <el-form-item v-if="!isEdit" label="初始密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="6-20位，不填默认为 123456"
          />
        </el-form-item>
        <!-- 编辑时可调整账号状态 -->
        <el-form-item v-else label="账号状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="正常"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getUserPage,
  getRoleList,
  addUser,
  updateUser,
  resetUserPassword
} from '@/api/admin/user'
import { formatDateTime } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

// ===== 列表查询 =====
const query = reactive({ keyword: '', roleId: null, status: null })
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// ===== 角色数据（roleId -> role 映射用于按 roleKey 着色）=====
const roleList = ref([])
const roleMap = ref({})

// 角色标签颜色：student 学生 info / librarian 馆员 warning / super_admin 超管 danger
const ROLE_TAG = { student: 'info', librarian: 'warning', super_admin: 'danger' }

function roleTagType(row) {
  const key = String(roleMap.value[row.roleId]?.roleKey || '').toLowerCase()
  return ROLE_TAG[key] || 'info'
}

// 信用分着色：<60 危险 / <80 警告 / 其余正常
function creditClass(score) {
  if (score == null) return ''
  if (score < 60) return 'credit-danger'
  if (score < 80) return 'credit-warning'
  return 'credit-ok'
}

// 拉取用户分页
async function fetchList() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (query.keyword.trim()) params.keyword = query.keyword.trim()
    if (query.roleId != null) params.roleId = query.roleId
    if (query.status != null) params.status = query.status
    const data = await getUserPage(params)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

// 拉取角色列表
async function fetchRoles() {
  const roles = await getRoleList()
  roleList.value = roles || []
  roleMap.value = Object.fromEntries(roles.map((r) => [r.roleId, r]))
}

// 搜索（重置页码）
function onSearch() {
  pageNum.value = 1
  fetchList()
}

// 重置筛选条件
function onReset() {
  query.keyword = ''
  query.roleId = null
  query.status = null
  onSearch()
}

// 切换每页条数
function onSizeChange() {
  pageNum.value = 1
  fetchList()
}

// ===== 新增 / 编辑弹窗 =====
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editingId = ref(null)

const DEFAULT_FORM = {
  username: '',
  name: '',
  nickname: '',
  phone: '',
  roleId: null,
  password: '',
  status: 1
}
const form = reactive({ ...DEFAULT_FORM })

// 校验规则与后端 UserSaveDTO / UserUpdateDTO 注解保持一致
const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9]{4,20}$/, message: '账号为4-20位字母或数字', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 20, message: '姓名不能超过20字', trigger: 'blur' }
  ],
  nickname: [{ max: 20, message: '昵称不能超过20字', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }],
  password: [{ min: 6, max: 20, message: '密码为6-20位', trigger: 'blur' }]
}

function openAdd() {
  isEdit.value = false
  editingId.value = null
  Object.assign(form, DEFAULT_FORM)
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  editingId.value = row.userId
  Object.assign(form, {
    username: row.username,
    name: row.name,
    nickname: row.nickname || '',
    phone: row.phone || '',
    roleId: row.roleId,
    password: '',
    status: row.status
  })
  dialogVisible.value = true
}

// 弹窗关闭后清理校验状态
function onDialogClosed() {
  formRef.value?.clearValidate()
}

// 提交新增 / 编辑
async function onSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      // 编辑：姓名/昵称/手机号/角色/状态（账号不可改）
      await updateUser(editingId.value, {
        name: form.name.trim(),
        nickname: form.nickname || null,
        phone: form.phone || null,
        roleId: form.roleId,
        status: form.status
      })
      ElMessage.success('修改成功')
    } else {
      // 新增：不传密码后端默认 123456（空字符串会触发 @Size 校验，需剔除）
      const payload = {
        username: form.username.trim(),
        name: form.name.trim(),
        nickname: form.nickname || null,
        phone: form.phone || null,
        roleId: form.roleId
      }
      if (form.password) payload.password = form.password
      await addUser(payload)
      ElMessage.success(form.password ? '新增用户成功' : '新增用户成功，初始密码为 123456')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

// 重置密码（后端重置为默认密码 123456）
async function onResetPassword(row) {
  try {
    await ElMessageBox.confirm(
      `确定重置用户「${row.name}」的密码吗？重置后密码为 123456。`,
      '重置密码',
      { type: 'warning', confirmButtonText: '确定重置', cancelButtonText: '取消' }
    )
  } catch (e) {
    return // 用户取消
  }
  await resetUserPassword(row.userId)
  ElMessage.success('密码已重置为 123456')
}

onMounted(() => {
  fetchRoles()
  fetchList()
})
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 卡片：白底 12px 圆角 + 柔和阴影 */
.admin-page :deep(.el-card) {
  border-radius: var(--radius-base);
  border: none;
  box-shadow: var(--shadow-card);
}

/* 按钮 hover 轻微上浮 */
.admin-page :deep(.el-button) {
  transition: all 0.2s ease;
}
.admin-page :deep(.el-button:not(.is-link):not(.is-text):hover) {
  transform: translateY(-1px);
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.table-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

/* 信用分着色 */
.credit {
  font-weight: 600;
}
.credit-ok {
  color: var(--color-success);
}
.credit-warning {
  color: var(--color-warning);
}
.credit-danger {
  color: var(--color-danger);
}

.text-secondary {
  color: var(--color-text-secondary);
  font-size: 12px;
  margin-left: 4px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
