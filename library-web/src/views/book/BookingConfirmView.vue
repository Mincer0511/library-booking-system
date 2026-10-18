<template>
  <div class="page-container" v-loading="loading">
    <div class="card-container confirm-card">
      <h2 class="title">确认预约信息</h2>

      <template v-if="seat">
        <!-- 预约信息明细 -->
        <div class="info-list">
          <div class="info-row">
            <span class="label">自习室</span>
            <span class="value">{{ seatMapData.roomName }}</span>
          </div>
          <div class="info-row">
            <span class="label">楼层</span>
            <span class="value">{{ seatMapData.floorNum }}F</span>
          </div>
          <div class="info-row">
            <span class="label">座位号</span>
            <span class="value highlight">{{ seat.seatCode }}</span>
          </div>
          <div class="info-row">
            <span class="label">预约日期</span>
            <span class="value">{{ query.date }}</span>
          </div>
          <div class="info-row">
            <span class="label">预约时段</span>
            <span class="value">{{ query.start }} - {{ query.end }}</span>
          </div>
          <div class="info-row">
            <span class="label">时长</span>
            <span class="value">{{ duration }}</span>
          </div>
        </div>

        <!-- 预约规则提示 -->
        <div class="rules">
          <p class="rules-title">📌 预约规则</p>
          <ul>
            <li>每日最多预约 2 次</li>
            <li>单次预约最长 4 小时</li>
            <li>预约开始后 30 分钟内需签到，否则记爽约</li>
          </ul>
        </div>

        <!-- 操作按钮 -->
        <div class="actions">
          <el-button round @click="$router.back()">返回修改</el-button>
          <el-button type="primary" round :loading="submitting" @click="onSubmit">
            确认提交
          </el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSeatMap } from '@/api/room'
import { createBooking } from '@/api/booking'

const route = useRoute()
const router = useRouter()

// 从 query 读取预约参数
const query = route.query

const loading = ref(false)
const submitting = ref(false)
const seatMapData = ref({})
const seat = ref(null)

// 计算时长（小时，保留 1 位小数）
const duration = computed(() => {
  if (!query.start || !query.end) return ''
  const toMin = (t) => {
    const [h, m] = t.split(':').map(Number)
    return h * 60 + m
  }
  const diff = toMin(query.end) - toMin(query.start)
  return `${(diff / 60).toFixed(1)} 小时`
})

// 加载自习室与座位信息用于确认展示
async function loadData() {
  loading.value = true
  try {
    seatMapData.value = await getSeatMap(query.roomId, query.date)
    seat.value = (seatMapData.value.seats || []).find(
      (s) => String(s.seatId) === String(query.seatId)
    )
    if (!seat.value) {
      ElMessage.error('座位信息不存在，请重新选择')
      router.back()
    }
  } finally {
    loading.value = false
  }
}

// 提交预约
async function onSubmit() {
  submitting.value = true
  try {
    await createBooking({
      seatId: Number(query.seatId),
      bookingDate: query.date,
      startTime: query.start,
      endTime: query.end
    })
    ElMessage.success('预约成功，请按时签到~')
    router.replace('/my/booking')
  } catch (e) {
    // 失败提示由响应拦截器统一弹出（后端 msg）
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.confirm-card {
  max-width: 560px;
  margin: 20px auto;
}

.title {
  font-size: 20px;
  margin-bottom: 20px;
  color: var(--color-text-primary);
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.info-row {
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

.rules {
  margin-top: 20px;
  background: var(--color-primary-light);
  border-radius: 10px;
  padding: 14px;
}

.rules-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--color-primary);
}

.rules ul {
  padding-left: 18px;
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.9;
}

.actions {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>
