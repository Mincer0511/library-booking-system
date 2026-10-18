<template>
  <div class="time-slot-picker">
    <!-- 开始 / 结束时间选择 -->
    <div class="picker-row">
      <el-select
        v-model="startVal"
        placeholder="开始时间"
        style="width: 130px"
        @change="onStartChange"
      >
        <el-option
          v-for="t in startOptions"
          :key="t"
          :label="t"
          :value="t"
        />
      </el-select>
      <span class="sep">至</span>
      <el-select
        v-model="endVal"
        placeholder="结束时间"
        style="width: 130px"
        :disabled="!startVal"
        @change="onEndChange"
      >
        <el-option
          v-for="t in endOptions"
          :key="t"
          :label="t"
          :value="t"
        />
      </el-select>
    </div>

    <!-- 冲突错误提示 -->
    <div v-if="errorMsg" class="error-msg">⚠️ {{ errorMsg }}</div>

    <!-- 当日已被预约的时段 -->
    <div v-if="busySlots.length" class="busy-slots">
      <span class="busy-label">已被预约：</span>
      <el-tag
        v-for="(b, i) in busySlots"
        :key="i"
        size="small"
        type="info"
        effect="plain"
        class="busy-tag"
      >
        {{ formatTime(b.startTime) }}-{{ formatTime(b.endTime) }}
      </el-tag>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { formatTime } from '@/utils/format'

const props = defineProps({
  // 开放时间 "HH:mm:ss"
  openTime: { type: String, default: '08:00:00' },
  closeTime: { type: String, default: '22:00:00' },
  // 该座位当日订单 [{startTime, endTime, status, mine}]
  bookings: { type: Array, default: () => [] }
})

const emit = defineEmits(['change'])

const startVal = ref('')
const endVal = ref('')
const errorMsg = ref('')

// 生成 30 分钟粒度的时间点（open..close）
function genSlots(open, close) {
  const toMin = (t) => {
    const [h, m] = t.slice(0, 5).split(':').map(Number)
    return h * 60 + m
  }
  const toStr = (min) => {
    const h = String(Math.floor(min / 60)).padStart(2, '0')
    const m = String(min % 60).padStart(2, '0')
    return `${h}:${m}`
  }
  const list = []
  for (let t = toMin(open); t <= toMin(close); t += 30) {
    list.push(toStr(t))
  }
  return list
}

const allSlots = computed(() => genSlots(props.openTime, props.closeTime))

// 开始时间选项：不能选最后一个时间点
const startOptions = computed(() => allSlots.value.slice(0, -1))

// 结束时间选项：开始+30min ~ 开始+4h，且不超闭馆时间
const endOptions = computed(() => {
  if (!startVal.value) return []
  const idx = allSlots.value.indexOf(startVal.value)
  if (idx === -1) return []
  // 最多 4 小时 = 8 个 30 分钟粒度，且不超过闭馆时间点
  const maxIdx = Math.min(idx + 8, allSlots.value.length - 1)
  return allSlots.value.slice(idx + 1, maxIdx + 1)
})

// 有效订单时段（status 0/1）
const busySlots = computed(() =>
  (props.bookings || []).filter((b) => b.status === 0 || b.status === 1)
)

// 两个 "HH:mm" 区间是否相交
function isOverlap(s1, e1, s2, e2) {
  const toMin = (t) => {
    const [h, m] = t.slice(0, 5).split(':').map(Number)
    return h * 60 + m
  }
  return toMin(s1) < toMin(e2) && toMin(s2) < toMin(e1)
}

// 校验所选区间与已有订单是否冲突
function validate() {
  errorMsg.value = ''
  if (!startVal.value || !endVal.value) {
    emitChange(false)
    return
  }
  const conflict = busySlots.value.some((b) =>
    isOverlap(startVal.value, endVal.value, b.startTime, b.endTime)
  )
  if (conflict) {
    errorMsg.value = '所选时段与已有预约冲突，请重新选择'
    emitChange(false)
    return
  }
  emitChange(true)
}

function emitChange(valid) {
  emit('change', {
    start: startVal.value || '',
    end: endVal.value || '',
    valid
  })
}

function onStartChange() {
  // 开始时间变化后重置结束时间
  endVal.value = ''
  validate()
}

function onEndChange() {
  validate()
}

// 外部切换座位/日期时重置
watch(
  () => props.bookings,
  () => {
    startVal.value = ''
    endVal.value = ''
    errorMsg.value = ''
    emitChange(false)
  },
  { deep: true }
)
</script>

<style scoped>
.time-slot-picker {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.picker-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sep {
  color: var(--color-text-secondary);
  font-size: 13px;
}

.error-msg {
  color: var(--color-danger);
  font-size: 13px;
}

.busy-slots {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.busy-label {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.busy-tag {
  border-radius: 8px;
}
</style>
