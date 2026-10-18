<template>
  <div class="page-container">
    <div class="card-container">
      <h2 class="title">违规记录 📋</h2>
      <p class="tip text-secondary">
        爽约一次扣 10 分，信用分低于 60 分将被禁用预约 7 天，请文明使用自习室~
      </p>

      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="violationTagType(row.violationType)" effect="light" round>
              {{ row.violationType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="violationDesc"
          label="违规描述"
          min-width="240"
          show-overflow-tooltip
        />
        <el-table-column label="扣分" width="100">
          <template #default="{ row }">
            <span class="deduct">-{{ row.deductScore }} 分</span>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState text="暂无违规记录，继续保持哦~" />
        </template>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyViolations } from '@/api/user'
import { formatDateTime, violationTagType } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const list = ref([])
const loading = ref(false)

// 拉取全量违规记录
async function fetchList() {
  loading.value = true
  try {
    list.value = (await getMyViolations()) || []
  } finally {
    loading.value = false
  }
}

onMounted(fetchList)
</script>

<style scoped>
.title {
  font-size: 20px;
  color: var(--color-text-primary);
}

.tip {
  margin: 8px 0 16px;
}

.deduct {
  color: var(--color-danger);
  font-weight: 600;
}
</style>
