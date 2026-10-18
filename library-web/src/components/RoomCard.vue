<template>
  <div class="room-card" @click="goDetail">
    <!-- 收藏按钮：未收藏灰色描边星，已收藏橙黄填充星 -->
    <button
      class="fav-btn"
      :class="{ favored }"
      :title="favored ? '取消收藏' : '收藏自习室'"
      @click.stop="$emit('toggle-favorite', room)"
    >
      <el-icon :size="18">
        <StarFilled v-if="favored" />
        <Star v-else />
      </el-icon>
    </button>

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
import { Star, StarFilled } from '@element-plus/icons-vue'
import { formatTime, roomTypeText } from '@/utils/format'

const props = defineProps({
  room: {
    type: Object,
    required: true
  },
  // 是否已收藏（控制星标填充）
  favored: {
    type: Boolean,
    default: false
  }
})

// 点击收藏按钮时通知父组件切换收藏状态
defineEmits(['toggle-favorite'])

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
  position: relative;
}

/* 收藏星标按钮：右上角悬浮 */
.fav-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: var(--color-primary-light);
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 2;
}

.fav-btn:hover {
  transform: scale(1.12);
}

/* 已收藏：橙黄填充 */
.fav-btn.favored {
  color: #faad14;
  background: #fff7e6;
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
