<template>
  <div class="page-container">
    <div class="card-container">
      <h2 class="title">我的预约</h2>

      <!-- 状态筛选 Tabs -->
      <el-tabs v-model="activeStatus" @tab-change="onStatusChange">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="待签到" name="0" />
        <el-tab-pane label="进行中" name="1" />
        <el-tab-pane label="已完成" name="2" />
        <el-tab-pane label="已取消" name="3" />
        <el-tab-pane label="已爽约" name="4" />
      </el-tabs>

      <!-- 预约表格（点击行进入详情） -->
      <el-table
        :data="list"
        v-loading="loading"
        style="width: 100%"
        class="booking-table"
        @row-click="goDetail"
      >
        <el-table-column prop="roomName" label="自习室" min-width="120">
          <template #default="{ row }">
            {{ row.roomName }}
            <span class="text-secondary">({{ row.floorNum }}F)</span>
          </template>
        </el-table-column>
        <el-table-column prop="seatCode" label="座位" width="80" />
        <el-table-column prop="bookingDate" label="日期" width="110" />
        <el-table-column label="时段" width="140">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <!-- 按 canXxx 动态显示操作按钮 -->
            <BookingActions :booking="row" size="small" @refresh="fetchList" />
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState text="暂无预约记录，快去首页预约一个座位吧~" />
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          background
          @current-change="fetchList"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyBookings } from '@/api/booking'
import { formatTime, formatDateTime } from '@/utils/format'
import StatusTag from '@/components/StatusTag.vue'
import EmptyState from '@/components/EmptyState.vue'
import BookingActions from '@/components/BookingActions.vue'

const router = useRouter()

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const activeStatus = ref('')
const loading = ref(false)

// 拉取预约列表
async function fetchList() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (activeStatus.value !== '') params.status = activeStatus.value
    const data = await getMyBookings(params)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

// 切换状态筛选：重置页码
function onStatusChange() {
  pageNum.value = 1
  fetchList()
}

// 点击行进入预约详情
function goDetail(row) {
  router.push(`/my/booking/${row.orderId}`)
}

onMounted(fetchList)
</script>

<style scoped>
.title {
  font-size: 20px;
  margin-bottom: 12px;
  color: var(--color-text-primary);
}

/* 行可点击手势 */
.booking-table :deep(.el-table__row) {
  cursor: pointer;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
