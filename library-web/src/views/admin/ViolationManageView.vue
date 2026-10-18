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
        <el-form-item label="违规类型">
          <el-select
            v-model="query.violationType"
            placeholder="全部类型"
            clearable
            style="width: 140px"
          >
            <el-option v-for="t in TYPE_OPTIONS" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">🔍 查询</el-button>
          <el-button @click="onReset">重置</el-button>
          <el-button type="danger" plain @click="openAddDialog">＋ 添加违规</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 违规记录表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="violationId" label="编号" width="80" />
        <el-table-column label="用户" min-width="130">
          <template #default="{ row }">
            <div>{{ row.realName || '-' }}</div>
            <div class="text-secondary">{{ row.username }}</div>
          </template>
        </el-table-column>
        <el-table-column label="违规类型" width="110">
          <template #default="{ row }">
            <el-tag :type="violationTagType(row.violationType)" effect="light">
              {{ row.violationType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="违规描述" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.violationDesc || '-' }}</template>
        </el-table-column>
        <el-table-column label="关联订单" width="100">
          <template #default="{ row }">{{ row.orderId || '-' }}</template>
        </el-table-column>
        <el-table-column label="扣分" width="90">
          <template #default="{ row }">
            <span class="deduct-score">-{{ row.deductScore }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作人" width="100">
          <template #default="{ row }">管理员 #{{ row.createBy }}</template>
        </el-table-column>
        <el-table-column label="记录时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="warning" size="small" plain @click="onRevoke(row)">撤销</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无违规记录" />
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

    <!-- 添加违规弹窗 -->
    <el-dialog v-model="addDialogVisible" title="添加违规记录" width="500px" destroy-on-close>
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="90px">
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="addForm.userId" :min="1" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="关联订单">
          <el-input-number
            v-model="addForm.orderId"
            :min="1"
            :controls="false"
            placeholder="选填"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="违规类型" prop="violationType">
          <el-select
            v-model="addForm.violationType"
            filterable
            allow-create
            default-first-option
            placeholder="选择或输入类型"
            style="width: 100%"
          >
            <el-option v-for="t in TYPE_OPTIONS" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="违规描述">
          <el-input
            v-model="addForm.violationDesc"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="选填，违规情况说明"
          />
        </el-form-item>
        <el-form-item label="扣除分数" prop="deductScore">
          <el-input-number v-model="addForm.deductScore" :min="1" :max="100" />
          <span class="form-tip">（1-100 分，将同步扣减用户信用分）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitAdd">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getViolationPage, addViolation, revokeViolation } from '@/api/admin/violation'
import { formatDateTime, violationTagType } from '@/utils/format'

// 常见违规类型选项
const TYPE_OPTIONS = ['爽约', '占座', '其他']

// ==================== 列表与筛选 ====================
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const query = reactive({ userId: undefined, violationType: null })

// 拉取违规记录分页
async function fetchList() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (query.userId != null) params.userId = query.userId
    if (query.violationType) params.violationType = query.violationType
    const data = await getViolationPage(params)
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
  query.violationType = null
  onSearch()
}

// 改变每页条数：重置页码后重新查询
function onSizeChange() {
  pageNum.value = 1
  fetchList()
}

// ==================== 添加违规 ====================
const addDialogVisible = ref(false)
const submitting = ref(false)
const addFormRef = ref(null)
const addForm = reactive({
  userId: undefined,
  orderId: undefined,
  violationType: '',
  violationDesc: '',
  deductScore: 5
})

const addRules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'change' }],
  violationType: [{ required: true, message: '请选择违规类型', trigger: 'change' }],
  deductScore: [{ required: true, message: '请输入扣除分数', trigger: 'change' }]
}

function openAddDialog() {
  addForm.userId = undefined
  addForm.orderId = undefined
  addForm.violationType = ''
  addForm.violationDesc = ''
  addForm.deductScore = 5
  addDialogVisible.value = true
}

// 提交添加违规（ViolationAddDTO，后端联动扣分与禁用判定）
async function submitAdd() {
  await addFormRef.value.validate()
  submitting.value = true
  try {
    await addViolation({
      userId: addForm.userId,
      orderId: addForm.orderId || null,
      violationType: addForm.violationType,
      violationDesc: addForm.violationDesc,
      deductScore: addForm.deductScore
    })
    ElMessage.success('违规记录已添加')
    addDialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

// ==================== 撤销违规 ====================
// 撤销后后端联动恢复信用分
async function onRevoke(row) {
  const confirmed = await ElMessageBox.confirm(
    `确定撤销「${row.username}」的「${row.violationType}」违规记录吗？撤销后将恢复该记录扣除的 ${row.deductScore} 分信用分。`,
    '撤销确认',
    { type: 'warning', confirmButtonText: '确认撤销', cancelButtonText: '取消' }
  ).catch(() => false)
  if (!confirmed) return
  await revokeViolation(row.violationId)
  ElMessage.success('已撤销并恢复信用分')
  fetchList()
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

.deduct-score {
  color: var(--color-danger);
  font-weight: 600;
}

.form-tip {
  color: var(--color-text-secondary);
  font-size: 12px;
  margin-left: 8px;
}
</style>
