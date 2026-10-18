<template>
  <div class="admin-page">
    <!-- 搜索筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form inline @submit.prevent>
        <el-form-item label="标题关键词">
          <el-input
            v-model="query.keyword"
            placeholder="公告标题"
            clearable
            style="width: 220px"
            @keyup.enter="onSearch"
            @clear="onSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="query.status"
            placeholder="全部状态"
            clearable
            style="width: 130px"
            @change="onSearch"
          >
            <el-option label="已发布" :value="1" />
            <el-option label="已下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 公告列表 -->
    <el-card shadow="never">
      <div class="table-header">
        <span class="table-title">公告列表</span>
        <el-button type="primary" @click="openAdd">＋ 发布公告</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="置顶" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isTop === 1" type="danger" effect="light">置顶</el-tag>
            <span v-else class="text-secondary">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ row.status === 1 ? '已发布' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-switch
              :model-value="row.isTop"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="顶"
              inactive-text="否"
              @change="(val) => onTopChange(row, val)"
            />
            <el-switch
              :model-value="row.status"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="架"
              inactive-text="下"
              class="status-switch"
              @change="(val) => onStatusChange(row, val)"
            />
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState text="暂无公告，发布第一条公告吧~" />
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

    <!-- 新增 / 编辑公告弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑公告' : '发布公告'"
      width="640px"
      @closed="onDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="form.title"
            maxlength="100"
            show-word-limit
            placeholder="请输入公告标题（100字以内）"
          />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch
            v-model="form.isTop"
            :active-value="1"
            :inactive-value="0"
            active-text="置顶显示"
            inactive-text="普通排序"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="发布"
            inactive-text="下架"
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
  getNoticePage,
  addNotice,
  updateNotice,
  deleteNotice,
  updateNoticeTop,
  updateNoticeStatus
} from '@/api/admin/notice'
import { formatDateTime } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

// ===== 列表查询 =====
const query = reactive({ keyword: '', status: null })
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

async function fetchList() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (query.keyword.trim()) params.keyword = query.keyword.trim()
    if (query.status != null) params.status = query.status
    const data = await getNoticePage(params)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  pageNum.value = 1
  fetchList()
}

function onReset() {
  query.keyword = ''
  query.status = null
  onSearch()
}

function onSizeChange() {
  pageNum.value = 1
  fetchList()
}

// ===== 行内置顶 / 上下架切换（失败不改动行数据，错误提示由拦截器统一弹出）=====
async function onTopChange(row, val) {
  await updateNoticeTop(row.noticeId, val)
  row.isTop = val
  ElMessage.success(val === 1 ? '已置顶' : '已取消置顶')
}

async function onStatusChange(row, val) {
  await updateNoticeStatus(row.noticeId, val)
  row.status = val
  ElMessage.success(val === 1 ? '已上架' : '已下架')
  // 当前按状态筛选时，被切走的公告不应停留在结果里
  if (query.status != null && query.status !== val) fetchList()
}

// 删除公告
async function onDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除公告「${row.title}」吗？删除后不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
  } catch (e) {
    return // 用户取消
  }
  await deleteNotice(row.noticeId)
  ElMessage.success('删除成功')
  // 若删完当前页只剩这条，回退一页
  if (list.value.length === 1 && pageNum.value > 1) pageNum.value -= 1
  fetchList()
}

// ===== 新增 / 编辑弹窗 =====
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editingId = ref(null)

const DEFAULT_FORM = { title: '', content: '', isTop: 0, status: 1 }
const form = reactive({ ...DEFAULT_FORM })

// 校验规则与后端 NoticeSaveDTO 注解保持一致
const rules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { max: 100, message: '标题不能超过100字', trigger: 'blur' }
  ],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

function openAdd() {
  isEdit.value = false
  editingId.value = null
  Object.assign(form, DEFAULT_FORM)
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  editingId.value = row.noticeId
  Object.assign(form, {
    title: row.title,
    content: row.content,
    isTop: row.isTop,
    status: row.status
  })
  dialogVisible.value = true
}

function onDialogClosed() {
  formRef.value?.clearValidate()
}

async function onSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = {
      title: form.title.trim(),
      content: form.content,
      isTop: form.isTop,
      status: form.status
    }
    if (isEdit.value) {
      await updateNotice(editingId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await addNotice(payload)
      ElMessage.success('发布成功')
    }
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

/* 行内开关与按钮间距 */
.status-switch {
  margin: 0 12px 0 8px;
}

.text-secondary {
  color: var(--color-text-secondary);
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
