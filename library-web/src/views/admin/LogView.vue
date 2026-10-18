<template>
  <div class="admin-page">
    <!-- 筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form inline @submit.prevent>
        <el-form-item label="操作模块">
          <el-select
            v-model="query.module"
            placeholder="全部模块"
            clearable
            style="width: 180px"
            @change="onSearch"
          >
            <el-option v-for="m in MODULES" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card shadow="never">
      <div class="table-header">
        <span class="table-title">操作日志</span>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="模块" width="130">
          <template #default="{ row }">
            <el-tag type="info" effect="plain">{{ row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="操作内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="150" />
        <el-table-column label="操作人" width="100" align="center">
          <template #default="{ row }">#{{ row.userId }}</template>
        </el-table-column>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState text="暂无操作日志~" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getLogPage } from '@/api/admin/log'
import { formatDateTime } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

// 操作模块下拉选项（与后端 @OperationLog 注解的 module 取值保持一致）
const MODULES = [
  '用户管理',
  '自习室管理',
  '座位管理',
  '订单管理',
  '违规管理',
  '禁用管理',
  '信用分管理',
  '公告管理',
  '数据统计',
  '系统设置'
]

// ===== 列表查询 =====
const query = reactive({ module: null })
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

async function fetchList() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (query.module) params.module = query.module
    const data = await getLogPage(params)
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
  query.module = null
  onSearch()
}

function onSizeChange() {
  pageNum.value = 1
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

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
