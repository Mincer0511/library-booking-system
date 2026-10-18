<template>
  <div class="page-container">
    <!-- 顶部公告卡片 -->
    <div class="notice-card card-container">
      <img class="notice-img" :src="noticeImg" alt="公告插画" />
      <div class="notice-text">
        <h3>📢 欢迎使用罗文校区图书馆自习室预约系统</h3>
        <p>请文明预约、按时签到~ 爽约将影响信用分哦</p>
      </div>
    </div>

    <!-- 楼层 Tabs -->
    <div class="card-container floor-card">
      <el-tabs v-model="activeFloor" @tab-change="onFilterChange">
        <el-tab-pane
          v-for="floor in appStore.floors"
          :key="floor.floorId"
          :label="floor.floorName || floor.floorNum + 'F'"
          :name="String(floor.floorId)"
        />
      </el-tabs>

      <!-- 筛选行：类型 + 关键词 -->
      <div class="filter-row">
        <el-select
          v-model="roomType"
          placeholder="自习室类型"
          style="width: 160px"
          @change="onFilterChange"
        >
          <el-option label="全部" value="" />
          <el-option label="普通自习室" value="普通" />
          <el-option label="静音自习室" value="静音" />
          <el-option label="研讨室" value="研讨" />
        </el-select>
        <el-input
          v-model="keyword"
          placeholder="搜索自习室名称"
          clearable
          style="width: 240px"
          :prefix-icon="Search"
          @input="onKeywordInput"
        />
      </div>

      <!-- 自习室卡片网格 -->
      <div v-loading="loading" class="room-grid">
        <RoomCard v-for="room in rooms" :key="room.roomId" :room="room" />
      </div>

      <!-- 空结果 -->
      <EmptyState v-if="!loading && !rooms.length" text="没有找到符合条件的自习室，换个条件试试~" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { getRoomList } from '@/api/room'
import RoomCard from '@/components/RoomCard.vue'
import EmptyState from '@/components/EmptyState.vue'

// 公告插画
const noticeImg =
  'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=' +
  encodeURIComponent(
    'cute cartoon library study room, blue and white, books and seats, flat vector illustration, soft pastel, kawaii campus style'
  ) +
  '&image_size=square_hd'

const appStore = useAppStore()

const activeFloor = ref('')
const roomType = ref('')
const keyword = ref('')
const rooms = ref([])
const loading = ref(false)

// 简单防抖：关键词输入 300ms 后请求
let timer = null
function onKeywordInput() {
  clearTimeout(timer)
  timer = setTimeout(fetchRooms, 300)
}

function onFilterChange() {
  fetchRooms()
}

// 拉取自习室列表
async function fetchRooms() {
  loading.value = true
  try {
    const params = {}
    if (activeFloor.value) params.floorId = activeFloor.value
    if (roomType.value) params.roomType = roomType.value
    if (keyword.value) params.keyword = keyword.value
    rooms.value = await getRoomList(params)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  // 先加载楼层，再默认选中第一层并拉取自习室
  const floors = await appStore.fetchFloors()
  if (floors.length) {
    activeFloor.value = String(floors[0].floorId)
  }
  fetchRooms()
})
</script>

<style scoped>
.notice-card {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.notice-img {
  width: 84px;
  height: 84px;
  border-radius: 12px;
  object-fit: cover;
}

.notice-text h3 {
  font-size: 17px;
  color: var(--color-text-primary);
}

.notice-text p {
  margin-top: 6px;
  color: var(--color-text-secondary);
  font-size: 13px;
}

.floor-card {
  min-height: 400px;
}

.filter-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

/* 响应式卡片网格：一行 3~4 个 */
.room-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

@media (max-width: 1100px) {
  .room-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 820px) {
  .room-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
