<template>
  <div class="page-container">
    <div class="card-container">
      <h2 class="title">我的收藏 ⭐</h2>

      <div v-loading="loading">
        <!-- 收藏卡片网格 -->
        <div v-if="list.length" class="fav-grid">
          <div v-for="f in list" :key="f.favoriteId" class="fav-card" @click="goRoom(f)">
            <div class="fav-head">
              <span class="fav-name">{{ f.roomName }}</span>
              <el-tag :type="roomTypeTag(f.roomType)" size="small" effect="light" round>
                {{ roomTypeText(f.roomType) }}
              </el-tag>
            </div>
            <div class="fav-meta">
              <el-tag size="small" type="info" effect="plain">{{ f.floorNum }}F</el-tag>
              <span class="open-time">
                🕐 {{ formatTime(f.openTime) }} - {{ formatTime(f.closeTime) }}
              </span>
            </div>
            <div class="fav-foot">
              <span class="text-secondary">
                收藏于 {{ formatDateTime(f.createTime, 'YYYY-MM-DD') }}
              </span>
              <el-button type="danger" size="small" plain round @click.stop="onRemove(f)">
                取消收藏
              </el-button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <EmptyState v-else-if="!loading" text="还没有收藏自习室，去首页逛逛吧~">
          <el-button type="primary" round @click="$router.push('/home')">去逛逛</el-button>
        </EmptyState>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFavoriteList, removeFavorite } from '@/api/favorite'
import { formatTime, formatDateTime, roomTypeText, roomTypeTag } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()

const list = ref([])
const loading = ref(false)

// 拉取收藏列表
async function fetchList() {
  loading.value = true
  try {
    list.value = (await getFavoriteList()) || []
  } finally {
    loading.value = false
  }
}

// 点击卡片进入自习室详情
function goRoom(f) {
  router.push(`/room/${f.roomId}`)
}

// 取消收藏（二次确认）
async function onRemove(f) {
  try {
    await ElMessageBox.confirm(`确定取消收藏「${f.roomName}」吗？`, '取消收藏', {
      confirmButtonText: '确定',
      cancelButtonText: '再想想',
      type: 'warning'
    })
  } catch (e) {
    return // 用户取消
  }
  await removeFavorite(f.roomId)
  ElMessage.success('已取消收藏')
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.title {
  font-size: 20px;
  margin-bottom: 16px;
  color: var(--color-text-primary);
}

/* 响应式收藏卡片网格 */
.fav-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

@media (max-width: 900px) {
  .fav-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .fav-grid {
    grid-template-columns: 1fr;
  }
}

.fav-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-base);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fff;
}

.fav-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card);
}

.fav-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.fav-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.fav-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.fav-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
