<template>
  <div class="seat-map-wrapper">
    <!-- 前方讲台标识 -->
    <div class="seat-map-front">📖 讲台 / 自习区前方</div>

    <!-- 座位矩阵：按最大 positionX / positionY 生成网格 -->
    <div
      v-if="gridRows.length"
      class="seat-map-grid"
      :style="{ gridTemplateColumns: `repeat(${gridCols}, 52px)` }"
    >
      <template v-for="(row, rIdx) in gridRows" :key="rIdx">
        <template v-for="(seat, cIdx) in row" :key="cIdx">
          <!-- 空白格占位 -->
          <div v-if="!seat" class="seat-item seat-blank"></div>
          <!-- 座位块 -->
          <div
            v-else
            class="seat-item"
            :class="seatClass(seat)"
            @click="onSelect(seat)"
          >
            <span>{{ seat.seatCode }}</span>
            <span v-if="seat.hasSocket === 1" class="seat-socket">🔌</span>
          </div>
        </template>
      </template>
    </div>
    <el-empty v-else description="暂无座位数据" :image-size="80" />

    <!-- 图例 -->
    <div class="seat-map-legend">
      <span class="legend-item"><i class="legend-color free"></i>空闲</span>
      <span class="legend-item"><i class="legend-color busy"></i>使用中</span>
      <span class="legend-item"><i class="legend-color booked"></i>已预约</span>
      <span class="legend-item"><i class="legend-color disabled"></i>不可用</span>
      <span class="legend-item"><i class="legend-color mine"></i>我的</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 座位数组（含 positionX/positionY/bookings）
  seats: {
    type: Array,
    default: () => []
  },
  // 当前选中的座位 id
  selectedId: {
    type: [Number, String],
    default: null
  }
})

const emit = defineEmits(['select'])

const gridCols = computed(() => {
  if (!props.seats.length) return 0
  return Math.max(...props.seats.map((s) => s.positionX ?? 0))
})

// 计算网格行（先算列数再按行填充）
const gridRows = computed(() => {
  if (!props.seats.length) return []
  const maxY = Math.max(...props.seats.map((s) => s.positionY ?? 0))
  const rows = []
  for (let y = 1; y <= maxY; y++) {
    const row = []
    for (let x = 1; x <= gridCols.value; x++) {
      row.push(props.seats.find((s) => s.positionX === x && s.positionY === y) || null)
    }
    rows.push(row)
  }
  return rows
})

// 当前时间 HH:mm:ss（用于判断"使用中"）
function nowTime() {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

// 座位样式分类
function seatClass(seat) {
  const classes = []
  if (!seat.usable) {
    classes.push('seat-disabled')
    return classes
  }
  const bookings = seat.bookings || []
  // 当前时刻落在 status∈{0,1} 的订单区间内 → 使用中
  const now = nowTime()
  const inUse = bookings.some(
    (b) => (b.status === 0 || b.status === 1) && b.startTime <= now && now <= b.endTime
  )
  if (inUse) {
    classes.push('seat-busy')
  } else if (bookings.some((b) => b.status === 0 || b.status === 1)) {
    // 当日还有其他有效订单 → 已预约
    classes.push('seat-booked')
  } else {
    classes.push('seat-free', 'seat-clickable')
  }
  // 我的订单描边
  if (bookings.some((b) => b.mine)) {
    classes.push('seat-mine')
  }
  // 选中高亮
  if (props.selectedId === seat.seatId) {
    classes.push('seat-selected')
  }
  return classes
}

// 点击座位：可用座位均可选（已有预约的时段由 TimeSlotPicker 拦截冲突）
function onSelect(seat) {
  if (!seat.usable) return
  emit('select', seat)
}
</script>
