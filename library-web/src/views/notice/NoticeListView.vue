<template>
  <div class="page-container">
    <div class="card-container">
      <h2 class="title">公告列表 📢</h2>

      <div v-loading="loading" class="notice-list">
        <template v-if="list.length">
          <div
            v-for="n in list"
            :key="n.noticeId"
            class="notice-item"
            @click="$router.push(`/notice/${n.noticeId}`)"
          >
            <div class="notice-main">
              <div class="notice-title-row">
                <!-- 置顶角标 -->
                <span v-if="n.isTop === 1" class="top-badge">置顶</span>
                <span class="notice-title">{{ n.title }}</span>
              </div>
              <p class="notice-summary">{{ summary(n.content) }}</p>
            </div>
            <span class="notice-time">{{ formatDateTime(n.createTime, 'YYYY-MM-DD') }}</span>
          </div>
        </template>
        <EmptyState v-else-if="!loading" text="暂无公告~" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getNoticeList } from '@/api/notice'
import { formatDateTime } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const list = ref([])
const loading = ref(false)

// 拉取公告列表（后端已按置顶优先 + 时间倒序）
async function fetchList() {
  loading.value = true
  try {
    list.value = (await getNoticeList()) || []
  } finally {
    loading.value = false
  }
}

// 摘要：截断 60 字
function summary(content) {
  if (!content) return ''
  return content.length > 60 ? content.slice(0, 60) + '…' : content
}

onMounted(fetchList)
</script>

<style scoped>
.title {
  font-size: 20px;
  margin-bottom: 16px;
  color: var(--color-text-primary);
}

.notice-list {
  min-height: 200px;
  display: flex;
  flex-direction: column;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 10px;
  border-bottom: 1px dashed var(--color-border);
  cursor: pointer;
  border-radius: 10px;
  transition: background 0.2s ease;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-item:hover {
  background: var(--color-primary-light);
}

.notice-main {
  flex: 1;
  min-width: 0;
}

.notice-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
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

.notice-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-summary {
  margin-top: 6px;
  font-size: 13px;
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-time {
  font-size: 12px;
  color: var(--color-text-secondary);
  flex-shrink: 0;
}
</style>
