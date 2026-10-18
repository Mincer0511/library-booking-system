<template>
  <div class="page-container">
    <!-- 顶部公告卡片：接入真实公告，前 3 条轮播 -->
    <div class="notice-card card-container">
      <img class="notice-img" :src="noticeImg" alt="公告插画" />
      <div class="notice-body">
        <el-carousel
          v-if="notices.length"
          height="84px"
          indicator-position="none"
          :interval="4000"
          arrow="never"
        >
          <el-carousel-item v-for="n in notices" :key="n.noticeId">
            <div class="notice-item" @click="$router.push(`/notice/${n.noticeId}`)">
              <h3>
                📢 {{ n.title }}
                <el-tag v-if="n.isTop === 1" size="small" type="danger" effect="light" round>
                  置顶
                </el-tag>
              </h3>
              <p>{{ noticeSummary(n.content) }}</p>
            </div>
          </el-carousel-item>
        </el-carousel>
        <!-- 无公告时的默认欢迎语 -->
        <div v-else class="notice-text">
          <h3>📢 欢迎使用罗文校区图书馆自习室预约系统</h3>
          <p>请文明预约、按时签到~ 爽约将影响信用分哦</p>
        </div>
      </div>
      <el-button class="notice-more" text type="primary" @click="$router.push('/notice')">
        全部公告 →
      </el-button>
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

      <!-- 自习室卡片网格（带收藏星标） -->
      <div v-loading="loading" class="room-grid">
        <RoomCard
          v-for="room in rooms"
          :key="room.roomId"
          :room="room"
          :favored="favIds.has(String(room.roomId))"
          @toggle-favorite="onToggleFavorite"
        />
      </div>

      <!-- 空结果 -->
      <EmptyState v-if="!loading && !rooms.length" text="没有找到符合条件的自习室，换个条件试试~" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { getRoomList } from '@/api/room'
import { getNoticeList } from '@/api/notice'
import { getFavoriteIds, addFavorite, removeFavorite } from '@/api/favorite'
import RoomCard from '@/components/RoomCard.vue'
import EmptyState from '@/components/EmptyState.vue'

// 公告插画
const noticeImg =
  'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=' +
  encodeURIComponent(
    'cute cartoon library study room, blue and white, books and seats, flat vector illustration, soft pastel, kawaii campus style'
  ) +
  '&image_size=square_hd'

const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const activeFloor = ref('')
const roomType = ref('')
const keyword = ref('')
const rooms = ref([])
const loading = ref(false)

// 首页公告（前 3 条）
const notices = ref([])

// 已收藏自习室 id 集合（字符串化便于比较）
const favIds = ref(new Set())

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

// 公告摘要：截断 60 字
function noticeSummary(content) {
  if (!content) return ''
  return content.length > 60 ? content.slice(0, 60) + '…' : content
}

// 拉取公告：取前 3 条轮播
async function fetchNotices() {
  try {
    const data = (await getNoticeList()) || []
    notices.value = data.slice(0, 3)
  } catch (e) {
    // 公告加载失败不阻塞首页
  }
}

// 拉取收藏 id 列表（登录状态下）
async function fetchFavIds() {
  if (!userStore.isLogin) return
  try {
    const ids = (await getFavoriteIds()) || []
    favIds.value = new Set(ids.map(String))
  } catch (e) {
    // 收藏状态加载失败不阻塞首页
  }
}

// 切换收藏状态
async function onToggleFavorite(room) {
  // 未登录：提示去登录
  if (!userStore.isLogin) {
    try {
      await ElMessageBox.confirm('收藏自习室需要先登录，是否前往登录？', '提示', {
        confirmButtonText: '去登录',
        cancelButtonText: '再逛逛',
        type: 'warning'
      })
      router.push({ path: '/login', query: { redirect: '/home' } })
    } catch (e) {
      // 用户取消
    }
    return
  }
  const id = String(room.roomId)
  if (favIds.value.has(id)) {
    await removeFavorite(room.roomId)
    favIds.value.delete(id)
    ElMessage.success('已取消收藏')
  } else {
    await addFavorite(room.roomId)
    favIds.value.add(id)
    ElMessage.success('收藏成功，可在「我的收藏」查看~')
  }
}

onMounted(async () => {
  // 先加载楼层，再默认选中第一层并拉取自习室
  const floors = await appStore.fetchFloors()
  if (floors.length) {
    activeFloor.value = String(floors[0].floorId)
  }
  fetchRooms()
  fetchNotices()
  fetchFavIds()
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
  flex-shrink: 0;
}

.notice-body {
  flex: 1;
  min-width: 0;
}

.notice-item {
  cursor: pointer;
}

.notice-item h3,
.notice-text h3 {
  font-size: 17px;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-item p,
.notice-text p {
  margin-top: 6px;
  color: var(--color-text-secondary);
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-more {
  flex-shrink: 0;
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
