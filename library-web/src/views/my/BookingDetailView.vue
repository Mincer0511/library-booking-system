<template>
  <div class="page-container" v-loading="loading">
    <template v-if="detail">
      <!-- 信息卡 -->
      <div class="card-container info-card">
        <div class="card-head">
          <h2 class="title">预约详情 📅</h2>
          <StatusTag :status="detail.status" />
        </div>

        <div class="info-grid">
          <div class="info-item">
            <span class="label">自习室</span>
            <span class="value">{{ detail.roomName }}</span>
          </div>
          <div class="info-item">
            <span class="label">楼层</span>
            <span class="value">{{ detail.floorNum }}F</span>
          </div>
          <div class="info-item">
            <span class="label">座位号</span>
            <span class="value highlight">{{ detail.seatCode }}</span>
          </div>
          <div class="info-item">
            <span class="label">预约日期</span>
            <span class="value">{{ detail.bookingDate }}</span>
          </div>
          <div class="info-item">
            <span class="label">预约时段</span>
            <span class="value">
              {{ formatTime(detail.startTime) }} - {{ formatTime(detail.endTime) }}
            </span>
          </div>
          <div class="info-item">
            <span class="label">订单编号</span>
            <span class="value">{{ detail.orderId }}</span>
          </div>
          <div class="info-item" v-if="detail.remark">
            <span class="label">备注</span>
            <span class="value">{{ detail.remark }}</span>
          </div>
        </div>

        <!-- 操作区：同列表页 canXxx 逻辑 -->
        <div class="actions">
          <BookingActions :booking="detail" @refresh="fetchDetail" />
        </div>
      </div>

      <!-- 状态时间轴 -->
      <div class="card-container timeline-card">
        <h3 class="sub-title">📋 状态时间轴</h3>
        <el-timeline>
          <!-- 创建 -->
          <el-timeline-item
            type="primary"
            :timestamp="formatDateTime(detail.createTime)"
            placement="top"
          >
            创建预约
          </el-timeline-item>
          <!-- 签到 -->
          <el-timeline-item
            :type="detail.checkinTime ? 'success' : 'info'"
            :hollow="!detail.checkinTime"
            :timestamp="detail.checkinTime ? formatDateTime(detail.checkinTime) : '待进行'"
            placement="top"
          >
            签到
          </el-timeline-item>
          <!-- 终态节点：签退 / 爽约 / 取消 -->
          <el-timeline-item
            :type="lastNode.type"
            :hollow="lastNode.hollow"
            :timestamp="lastNode.timestamp"
            placement="top"
          >
            {{ lastNode.text }}
          </el-timeline-item>
        </el-timeline>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getBookingDetail } from '@/api/booking'
import { formatTime, formatDateTime } from '@/utils/format'
import StatusTag from '@/components/StatusTag.vue'
import BookingActions from '@/components/BookingActions.vue'

const route = useRoute()

const loading = ref(false)
const detail = ref(null)

// 拉取预约详情
async function fetchDetail() {
  loading.value = true
  try {
    detail.value = await getBookingDetail(route.params.id)
  } finally {
    loading.value = false
  }
}

// 时间轴终态节点：按 status 渲染签退 / 爽约 / 取消 / 待进行
const lastNode = computed(() => {
  const d = detail.value
  if (!d) return { text: '签退', type: 'info', hollow: true, timestamp: '待进行' }
  // 已完成：显示签退时间
  if (Number(d.status) === 2) {
    return {
      text: '签退完成',
      type: 'success',
      hollow: false,
      timestamp: d.checkoutTime ? formatDateTime(d.checkoutTime) : ''
    }
  }
  // 已取消
  if (Number(d.status) === 3) {
    return { text: '预约已取消', type: 'info', hollow: false, timestamp: '' }
  }
  // 已爽约
  if (Number(d.status) === 4) {
    return { text: '未按时签到，已爽约', type: 'danger', hollow: false, timestamp: '' }
  }
  // 进行中 / 待签到：签退待进行
  return { text: '签退', type: 'info', hollow: true, timestamp: '待进行' }
})

onMounted(fetchDetail)
</script>

<style scoped>
.info-card {
  max-width: 720px;
  margin: 20px auto;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.title {
  font-size: 20px;
  color: var(--color-text-primary);
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  border-bottom: 1px dashed var(--color-border);
  padding-bottom: 12px;
}

.label {
  color: var(--color-text-secondary);
}

.value {
  font-weight: 600;
  color: var(--color-text-primary);
}

.highlight {
  color: var(--color-primary);
  font-size: 16px;
}

.actions {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.timeline-card {
  max-width: 720px;
  margin: 0 auto 20px;
}

.sub-title {
  font-size: 16px;
  margin-bottom: 16px;
  color: var(--color-text-primary);
}
</style>
