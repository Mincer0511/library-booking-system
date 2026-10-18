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
        <el-form-item label="自习室">
          <el-select
            v-model="query.roomId"
            placeholder="全部自习室"
            clearable
            filterable
            style="width: 180px"
          >
            <el-option
              v-for="room in roomOptions"
              :key="room.roomId"
              :label="room.roomName"
              :value="room.roomId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期">
          <el-date-picker
            v-model="query.bookingDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
            <el-option
              v-for="(item, key) in STATUS_TAG"
              :key="key"
              :label="STATUS_TEXT[key]"
              :value="Number(key)"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">🔍 查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="orderId" label="订单号" width="90" />
        <el-table-column label="用户" min-width="130">
          <template #default="{ row }">
            <div>{{ row.realName || '-' }}</div>
            <div class="text-secondary">{{ row.username }}</div>
          </template>
        </el-table-column>
        <el-table-column label="座位" min-width="160">
          <template #default="{ row }">
            {{ row.roomName }}
            <span class="text-secondary">({{ row.floorNum }}F)</span>
            · {{ row.seatCode }}
          </template>
        </el-table-column>
        <el-table-column label="日期 / 时段" width="180">
          <template #default="{ row }">
            {{ row.bookingDate }}
            <div>{{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="95">
          <template #default="{ row }">
            <el-tag :type="STATUS_TAG[row.status] || 'info'" effect="light">
              {{ row.statusText || STATUS_TEXT[row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0 || row.status === 1"
              type="danger"
              size="small"
              plain
              @click="openCancelDialog(row)"
            >
              强制取消
            </el-button>
            <el-button
              v-if="row.status === 0 || row.status === 1"
              type="primary"
              size="small"
              plain
              @click="openTimeDialog(row)"
            >
              修改时间
            </el-button>
            <el-button type="primary" size="small" plain @click="openRemarkDialog(row)">
              备注
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无订单数据" />
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

    <!-- 强制取消弹窗 -->
    <el-dialog v-model="cancelDialogVisible" title="强制取消订单" width="460px" destroy-on-close>
      <el-form ref="cancelFormRef" :model="cancelForm" :rules="{ reason: [{ required: true, message: '请填写取消原因', trigger: 'blur' }] }">
        <el-form-item label="取消原因" prop="reason">
          <el-input
            v-model="cancelForm.reason"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="必填，将通知到用户（200字以内）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitCancel">确认取消订单</el-button>
      </template>
    </el-dialog>

    <!-- 修改时间弹窗 -->
    <el-dialog v-model="timeDialogVisible" title="修改订单时间" width="460px" destroy-on-close>
      <el-form :model="timeForm" label-width="90px">
        <el-form-item label="预约日期" required>
          <el-date-picker
            v-model="timeForm.bookingDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-time-picker
            v-model="timeForm.startTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-time-picker
            v-model="timeForm.endTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="timeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitTime">保存</el-button>
      </template>
    </el-dialog>

    <!-- 备注弹窗 -->
    <el-dialog v-model="remarkDialogVisible" title="修改订单备注" width="460px" destroy-on-close>
      <el-input
        v-model="remarkForm.remark"
        type="textarea"
        :rows="3"
        maxlength="255"
        show-word-limit
        placeholder="填写管理员备注"
      />
      <template #footer>
        <el-button @click="remarkDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitRemark">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getBookingPage,
  forceCancelOrder,
  updateBookingTime,
  updateBookingRemark
} from '@/api/admin/booking'
import { getRoomPage } from '@/api/admin/room'
import { formatTime, formatDateTime } from '@/utils/format'

// 订单状态 → Tag 类型（0待签到 info / 1进行中 primary / 2已完成 success / 3已取消 info / 4已爽约 danger）
const STATUS_TAG = { 0: 'info', 1: 'primary', 2: 'success', 3: 'info', 4: 'danger' }
const STATUS_TEXT = { 0: '待签到', 1: '进行中', 2: '已完成', 3: '已取消', 4: '已爽约' }

// ==================== 列表与筛选 ====================
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const submitting = ref(false)
const roomOptions = ref([])
const query = reactive({ userId: undefined, roomId: null, bookingDate: '', status: null })

// 拉取订单分页
async function fetchList() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (query.userId != null) params.userId = query.userId
    if (query.roomId !== null && query.roomId !== '') params.roomId = query.roomId
    if (query.bookingDate) params.bookingDate = query.bookingDate
    if (query.status !== null && query.status !== '') params.status = query.status
    const data = await getBookingPage(params)
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
  query.roomId = null
  query.bookingDate = ''
  query.status = null
  onSearch()
}

// 改变每页条数：重置页码后重新查询
function onSizeChange() {
  pageNum.value = 1
  fetchList()
}

// ==================== 强制取消 ====================
const cancelDialogVisible = ref(false)
const cancelFormRef = ref(null)
const cancelForm = reactive({ orderId: null, reason: '' })

function openCancelDialog(row) {
  cancelForm.orderId = row.orderId
  cancelForm.reason = ''
  cancelDialogVisible.value = true
}

async function submitCancel() {
  await cancelFormRef.value.validate()
  submitting.value = true
  try {
    await forceCancelOrder(cancelForm.orderId, { reason: cancelForm.reason })
    ElMessage.success('订单已强制取消')
    cancelDialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

// ==================== 修改时间 ====================
const timeDialogVisible = ref(false)
const timeForm = reactive({ orderId: null, bookingDate: '', startTime: '', endTime: '' })

function openTimeDialog(row) {
  timeForm.orderId = row.orderId
  timeForm.bookingDate = row.bookingDate
  timeForm.startTime = formatTime(row.startTime)
  timeForm.endTime = formatTime(row.endTime)
  timeDialogVisible.value = true
}

async function submitTime() {
  if (!timeForm.bookingDate || !timeForm.startTime || !timeForm.endTime) {
    ElMessage.warning('请完整填写日期与起止时间')
    return
  }
  submitting.value = true
  try {
    await updateBookingTime(timeForm.orderId, {
      bookingDate: timeForm.bookingDate,
      startTime: timeForm.startTime,
      endTime: timeForm.endTime
    })
    ElMessage.success('订单时间已修改')
    timeDialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

// ==================== 备注 ====================
const remarkDialogVisible = ref(false)
const remarkForm = reactive({ orderId: null, remark: '' })

function openRemarkDialog(row) {
  remarkForm.orderId = row.orderId
  remarkForm.remark = row.remark || ''
  remarkDialogVisible.value = true
}

async function submitRemark() {
  submitting.value = true
  try {
    await updateBookingRemark(remarkForm.orderId, remarkForm.remark)
    ElMessage.success('备注已保存')
    remarkDialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  fetchList()
  // 加载自习室下拉选项（一次取足量）
  const data = await getRoomPage({ pageNum: 1, pageSize: 100 })
  roomOptions.value = data.list || []
})
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
</style>
