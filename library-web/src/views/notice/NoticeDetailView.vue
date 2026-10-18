<template>
  <div class="page-container" v-loading="loading">
    <div v-if="notice" class="card-container detail-card">
      <!-- 标题区 -->
      <div class="head">
        <span v-if="notice.isTop === 1" class="top-badge">置顶</span>
        <h2 class="title">{{ notice.title }}</h2>
      </div>
      <p class="time text-secondary">发布时间：{{ formatDateTime(notice.createTime) }}</p>

      <el-divider />

      <!-- 正文：保留换行 -->
      <div class="content">{{ notice.content }}</div>

      <div class="back-row">
        <el-button round @click="$router.back()">返回</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getNoticeDetail } from '@/api/notice'
import { formatDateTime } from '@/utils/format'

const route = useRoute()

const notice = ref(null)
const loading = ref(false)

// 拉取公告详情
async function fetchDetail() {
  loading.value = true
  try {
    notice.value = await getNoticeDetail(route.params.id)
  } finally {
    loading.value = false
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.detail-card {
  max-width: 760px;
  margin: 20px auto;
  padding: 28px;
}

.head {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 置顶角标 */
.top-badge {
  background: var(--color-danger);
  color: #fff;
  font-size: 12px;
  padding: 1px 8px;
  border-radius: 8px;
  flex-shrink: 0;
}

.title {
  font-size: 22px;
  color: var(--color-text-primary);
}

.time {
  margin-top: 10px;
}

/* 正文保留换行 */
.content {
  font-size: 15px;
  line-height: 1.9;
  color: var(--color-text-primary);
  white-space: pre-wrap;
  min-height: 120px;
}

.back-row {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
