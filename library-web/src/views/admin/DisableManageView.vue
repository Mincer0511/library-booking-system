<template>
  <div class="admin-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form inline :model="query" @submit.prevent>
        <el-form-item label="用户ID">
          <el-input-number
            v-model="query.userId"
            :min="1"
            :controls="false"
            placeholder="用户ID"
            style="width: 120px"
          />
        </el-form-item>
        <el-form-item label="解除状态">
          <el-select v-model="query.isLifted" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="禁用中" :value="0" />
            <el-option label="已解除" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">🔍 查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 禁用记录表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="disableId" label="编号" width="80" />
        <el-table-column label="用户" min-width="130">
          <template #default="{ row }">
            <div>{{ row.realName || '-' }}</div>
            <div class="text-secondary">{{ row.username }}</div>
          </template>
        </el-table-column>
        <el-table-column label="禁用原因" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.disableReason || '-' }}</template>
        </el-table-column>
        <el-table-column label="开始时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isLifted === 1 ? 'success' : 'danger'" effect="light">
              {{ row.isLifted === 1 ? '已解除' : '禁用中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="解除时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.liftTime) || '-' }}</template>
        </el-table-column>
        <el-table-column label="记录时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.isLifted === 0"
              type="success"
              size="small"
              plain
              @click="onLift(row)"
            >
              提前解禁
            </el-button>
            <el-button type="primary" size="small" plain @click="openCreditDialog(row)">
              信用分调整
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无禁用记录" />
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="onSizeChange"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 信用分调整弹窗 -->
    <el-dialog v-model="creditDialogVisible" title="调整用户信用分" width="460px" destroy-on-close>
      <el-form ref="creditFormRef" :model="creditForm" :rules="creditRules" label-width="90px">
        <el-form-item label="目标用户">
          <span>{{ creditForm.realName || creditForm.username }}（ID: {{ creditForm.userId }}）</span>
        </el-form-item>
        <el-form-item label="信用分数" prop="score">
          <el-input-number v-model="creditForm.score" :min="0" :max="100" />
          <span class="form-tip">（0-100 分）</span>
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input
            v-model="creditForm.reason"
            type="textarea"
            :rows="3"
            maxlength="100"
            show-word-limit
            placeholder="必填，将随站内信通知用户"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="creditDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCredit">确认调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDisablePage, liftDisable, adjustCredit } from '@/api/admin/disable'
import { formatDateTime } from '@/utils/format'

// ==================== 列表与筛选 ====================
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const query = reactive({ userId: undefined, isLifted: null })

// 拉取禁用记录分页
async function fetchList() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (query.userId != null) params.userId = query.userId
    if (query.isLifted !== null && query.isLifted !== '') params.isLifted = query.isLifted
    const data = await getDisablePage(params)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

// 查询：重置页码
function onSearch() {
  pageNum.value = 1
  fetchList()
}

// 重置筛选
function onReset() {
  query.userId = undefined
  query.isLifted = null
  onSearch()
}

// 改变每页条数：重置页码后重新查询
function onSizeChange() {
  pageNum.value = 1
  fetchList()
}

// ==================== 提前解禁 ====================
// 复用后端 liftDisable：恢复信用分 + 留痕 + 通知
async function onLift(row) {
  const confirmed = await ElMessageBox.confirm(
    `确定提前解除「${row.realName || row.username}」的禁用吗？将按规则恢复其信用分并发送通知。`,
    '提前解禁确认',
    { type: 'warning', confirmButtonText: '确认解禁', cancelButtonText: '取消' }
  ).catch(() => false)
  if (!confirmed) return
  await liftDisable(row.userId)
  ElMessage.success('已提前解禁')
  fetchList()
}

// ==================== 信用分调整 ====================
const creditDialogVisible = ref(false)
const submitting = ref(false)
const creditFormRef = ref(null)
const creditForm = reactive({ userId: null, username: '', realName: '', score: 100, reason: '' })

const creditRules = {
  score: [{ required: true, message: '请输入信用分数', trigger: 'change' }],
  reason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
}

function openCreditDialog(row) {
  creditForm.userId = row.userId
  creditForm.username = row.username
  creditForm.realName = row.realName
  creditForm.score = 100
  creditForm.reason = ''
  creditDialogVisible.value = true
}

// 提交信用分调整（CreditAdjustDTO：score + reason）
async function submitCredit() {
  await creditFormRef.value.validate()
  submitting.value = true
  try {
    await adjustCredit(creditForm.userId, {
      score: creditForm.score,
      reason: creditForm.reason
    })
    ElMessage.success('信用分已调整')
    creditDialogVisible.value = false
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

.search-card,
.table-card {
  border-radius: var(--radius-base);
  box-shadow: var(--shadow-card);
  border: none;
}

.search-card :deep(.el-form-item) {
  margin-bottom: 0;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.text-secondary {
  color: var(--color-text-secondary);
  font-size: 12px;
}

.form-tip {
  color: var(--color-text-secondary);
  font-size: 12px;
  margin-left: 8px;
}
</style>
