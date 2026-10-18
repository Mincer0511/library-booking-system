<template>
  <div class="room-card" @click="goDetail">
    <!-- 标题行：名称 + 类型标签 -->
    <div class="card-header">
      <span class="room-name">{{ room.roomName }}</span>
      <el-tag :type="typeTag" size="small" effect="light" round>{{ typeText }}</el-tag>
    </div>

    <!-- 楼层与开放时间 -->
    <div class="card-meta">
      <el-tag size="small" type="info" effect="plain">{{ room.floorNum || room.floorId }}F</el-tag>
      <span class="open-time">
        🕐 {{ formatTime(room.openTime) }} - {{ formatTime(room.closeTime) }}
      </span>
    </div>

    <!-- 座位情况 -->
    <div class="seat-info">
      <span class="free-num">{{ freeCount }}</span>
      <span class="seat-total">空余 / 共 {{ room.seatCount }} 座</span>
      <span v-if="room.freeNow" class="free-badge">当前空闲</span>
    </div>

    <!-- 描述 -->
    <p v-if="room.description" class="room-desc">{{ room.description }}</p>

    <!-- 操作按钮 -->
    <el-button type="primary" round class="book-btn" @click.stop="goDetail">
      进入预约
    </el-button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { formatTime, roomTypeText } from '@/utils/format'

const props = defineProps({
  room: {
    type: Object,
    required: true
  }
})

const router = useRouter()

// 自习室类型文案与标签颜色
const typeText = computed(() => roomTypeText(props.room.roomType))
const typeTag = computed(() => {
  const map = { NORMAL: 'primary', QUIET: 'success', DISCUSS: 'warning' }
  return map[props.room.roomType] || 'primary'
})

// 空余座位数（优先 usableSeats 字段）
const freeCount = computed(() => {
  if (props.room.usableSeats != null) return props.room.usableSeats
  return props.room.freeNow ? props.room.seatCount : 0
})

// 跳转自习室详情
function goDetail() {
  router.push(`/room/${props.room.roomId}`)
}
</script>

<style scoped>
.room-card {
  background: #fff;
  border-radius: var(--radius-base);
  box-shadow: var(--shadow-card);
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

/* hover 上浮效果 */
.room-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(74, 144, 226, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.seat-info {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.free-num {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-success);
}

.seat-total {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.free-badge {
  margin-left: auto;
  font-size: 12px;
  color: var(--color-success);
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  padding: 2px 8px;
  border-radius: 10px;
}

.room-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-btn {
  align-self: flex-end;
}
</style>
