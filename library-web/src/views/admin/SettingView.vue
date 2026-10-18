<template>
  <div class="admin-page">
    <!-- 页顶说明条 -->
    <el-alert
      type="info"
      show-icon
      :closable="false"
      title="设置保存后实时生效"
      description="以下参数为预约规则与信用分规则配置，修改保存后立即对全体用户生效，无需重启服务。"
    />

    <!-- 配置列表 -->
    <el-card shadow="never">
      <div class="table-header">
        <span class="table-title">系统配置</span>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="configKey" label="配置键" min-width="200" />
        <el-table-column label="配置值" width="120" align="center">
          <template #default="{ row }">
            <span class="config-value">{{ row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="280" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">
              修改
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 修改配置弹窗（仅允许修改配置值） -->
    <el-dialog v-model="dialogVisible" title="修改配置" width="460px" @closed="onDialogClosed">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="配置键">
          <el-input :model-value="form.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置值" required>
          <el-input-number
            v-model="form.configValue"
            :min="0"
            :max="999999"
            :precision="0"
            :step="1"
            :value-on-clear="0"
            controls-position="right"
            style="width: 100%"
          />
          <div v-if="form.description" class="form-tip">{{ form.description }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfigList, updateConfig } from '@/api/admin/config'

// ===== 配置列表 =====
const list = ref([])
const loading = ref(false)

async function fetchList() {
  loading.value = true
  try {
    list.value = (await getConfigList()) || []
  } finally {
    loading.value = false
  }
}

// ===== 修改弹窗 =====
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ configKey: '', configValue: 0, description: '' })

function openEdit(row) {
  form.configKey = row.configKey
  // 配置值以数字输入框编辑（后端校验 ^\d+$ 非负整数）
  form.configValue = Number(row.configValue) || 0
  form.description = row.description || ''
  dialogVisible.value = true
}

function onDialogClosed() {
  formRef.value?.clearValidate()
}

async function onSubmit() {
  // el-input-number 清空时为 null，需拦截
  if (form.configValue === null || form.configValue === undefined || form.configValue === '') {
    ElMessage.warning('请输入配置值')
    return
  }
  submitting.value = true
  try {
    // API 内部会将配置值转为字符串提交
    await updateConfig(form.configKey, form.configValue)
    ElMessage.success('保存成功，设置已实时生效')
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

onMounted(fetchList)
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

/* 配置值突出显示 */
.config-value {
  font-weight: 700;
  color: var(--color-primary);
  font-size: 15px;
}

.form-tip {
  width: 100%;
  font-size: 12px;
  color: var(--color-text-secondary);
  line-height: 1.5;
  margin-top: 4px;
}
</style>
