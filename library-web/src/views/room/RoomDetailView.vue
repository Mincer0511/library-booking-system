<template>
  <div class="page-container" v-loading="loading">
    <template v-if="seatMapData.roomId">
      <!-- 顶部：自习室信息 + 日期切换 -->
      <div class="card-container header-card">
        <div class="room-info">
          <h2 class="room-title">
            {{ seatMapData.roomName }}
            <el-tag size="small" type="info" effect="plain">{{ seatMapData.floorNum }}F</el-tag>
            <el-tag size="small" :type="typeTag" effect="light" round>{{ typeText }}</el-tag>
          </h2>
          <p class="open-time">
            开放时间：{{ formatTime(seatMapData.openTime) }} - {{ formatTime(seatMapData.closeTime) }}
          </p>
        </div>
        <el-radio-group v-model="dateType" @change="onDateChange">
          <el-radio-button value="today">今天</el-radio-button>
          <el-radio-button value="tomorrow">明天</el-radio-button>
        </el-radio-group>
      </div>

      <div class="detail-body">
        <!-- 左侧：座位图 -->
        <div class="card-container seat-card">
          <SeatMap
            :seats="seatMapData.seats || []"
            :selected-id="selectedSeat?.seatId"
            @select="onSeatSelect"
          />
        </div>

        <!-- 右侧：选座信息卡 -->
        <div class="card-container side-card">
          <h3 class="side-title">选座信息</h3>

          <template v-if="selectedSeat">
            <div class="seat-detail">
              <div class="detail-row">
                <span class="label">座位编号</span>
                <span class="value seat-code">{{ selectedSeat.seatCode }}</span>
              </div>
              <div class="detail-row">
                <span class="label">座位类型</span>
                <span class="value">{{ selectedSeat.seatType }}</span>
              </div>
              <div class="detail-row">
                <span class="label">插座</span>
                <span class="value">{{ selectedSeat.hasSocket === 1 ? '🔌 有插座' : '无' }}</span>
              </div>
            </div>

            <!-- 时段选择器 -->
            <h3 class="side-title">预约时段（{{ bookingDate }}）</h3>
            <TimeSlotPicker
              :open-time="seatMapData.openTime"
              :close-time="seatMapData.closeTime"
              :bookings="selectedSeat.bookings || []"
              @change="onTimeChange"
            />

            <!-- 已选摘要 -->
            <div v-if="timeSlot.valid" class="summary">
              已选：{{ bookingDate }} {{ timeSlot.start }} - {{ timeSlot.end }}
            </div>

            <el-button
              type="primary"
              round
              size="large"
              style="width: 100%"
              :disabled="!canBook"
              @click="goConfirm"
            >
              立即预约
            </el-button>
          </template>

          <el-empty v-else description="点击左侧座位开始选座" :image-size="100" />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { getSeatMap } from '@/api/room'
import { formatTime, roomTypeText, roomTypeTag } from '@/utils/format'
import SeatMap from '@/components/SeatMap.vue'
import TimeSlotPicker from '@/components/TimeSlotPicker.vue'

const route = useRoute()
const router = useRouter()
const roomId = route.params.id

const loading = ref(false)
const seatMapData = ref({})
const selectedSeat = ref(null)
const timeSlot = ref({ start: '', end: '', valid: false })
const dateType = ref('today')

// 预约日期
const bookingDate = computed(() =>
  dateType.value === 'today'
    ? dayjs().format('YYYY-MM-DD')
    : dayjs().add(1, 'day').format('YYYY-MM-DD')
)

const typeText = computed(() => roomTypeText(seatMapData.value.roomType))
const typeTag = computed(() => roomTypeTag(seatMapData.value.roomType))

// 立即预约按钮可用条件
const canBook = computed(() => selectedSeat.value && timeSlot.value.valid)

// 加载座位图
async function loadSeatMap() {
  loading.value = true
  selectedSeat.value = null
  timeSlot.value = { start: '', end: '', valid: false }
  try {
    seatMapData.value = await getSeatMap(roomId, bookingDate.value)
  } finally {
    loading.value = false
  }
}

// 切换今天 / 明天
function onDateChange() {
  loadSeatMap()
}

// 选中座位
function onSeatSelect(seat) {
  selectedSeat.value = seat
  timeSlot.value = { start: '', end: '', valid: false }
}

// 时段变化回调
function onTimeChange(payload) {
  timeSlot.value = payload
}

// 跳确认页
function goConfirm() {
  router.push({
    path: '/book/confirm',
    query: {
      seatId: selectedSeat.value.seatId,
      date: bookingDate.value,
      start: timeSlot.value.start,
      end: timeSlot.value.end,
      roomId
    }
  })
}

onMounted(loadSeatMap)
</script>

<style scoped>
.header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.room-title {
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.open-time {
  margin-top: 6px;
  color: var(--color-text-secondary);
  font-size: 13px;
}

.detail-body {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.seat-card {
  flex: 1;
  min-width: 0;
}

.side-card {
  width: 340px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.side-title {
  font-size: 15px;
  color: var(--color-text-primary);
}

.seat-detail {
  background: var(--color-primary-light);
  border-radius: 10px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.label {
  color: var(--color-text-secondary);
}

.seat-code {
  font-weight: 700;
  color: var(--color-primary);
  font-size: 15px;
}

.summary {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #237804;
  border-radius: 10px;
  padding: 10px;
  font-size: 13px;
  text-align: center;
}

@media (max-width: 900px) {
  .detail-body {
    flex-direction: column;
  }
  .side-card {
    width: 100%;
  }
}
</style>
