<template>
  <div class="page-container">
    <div class="card-container">
      <div class="head-row">
        <h2 class="title">消息中心 🔔</h2>
        <el-button
          type="primary"
          plain
          round
          size="small"
          :disabled="!list.some((m) => m.isRead === 0)"
          @click="onReadAll"
        >
          全部已读
        </el-button>
      </div>

      <!-- 已读状态筛选 -->
      <el-radio-group v-model="isRead" @change="onFilterChange" class="filter-tabs">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button :value="0">未读</el-radio-button>
        <el-radio-button :value="1">已读</el-radio-button>
      </el-radio-group>

      <!-- 消息列表 -->
      <div v-loading="loading" class="msg-list">
        <template v-if="list.length">
          <div
            v-for="msg in list"
            :key="msg.msgId"
            class="msg-item"
            :class="{ unread: msg.isRead === 0 }"
            @click="onMsgClick(msg)"
          >
            <!-- 类型图标：预约📅蓝 / 违规⚠红 / 系统📢灰 -->
            <div class="msg-icon" :class="typeClass(msg.type)">{{ typeIcon(msg.type) }}</div>
            <div class="msg-body">
              <div class="msg-head">
                <span v-if="msg.isRead === 0" class="dot" />
                <span class="msg-title" :class="{ bold: msg.isRead === 0 }">{{ msg.title }}</span>
                <el-tag size="small" effect="plain" :type="typeTag(msg.type)" round>
                  {{ msg.type }}
                </el-tag>
                <span class="msg-time">{{ formatDateTime(msg.createTime) }}</span>
              </div>
              <!-- 点击展开内容 -->
              <p v-if="expandedId === msg.msgId" class="msg-content">{{ msg.content }}</p>
            </div>
          </div>
        </template>
        <EmptyState v-else-if="!loading" text="暂无消息，去预约一个座位开始学习吧~" />
      </div>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          background
          @current-change="fetchList"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMessagePage, markMessageRead, markAllMessageRead } from '@/api/message'
import { formatDateTime } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
// '' 全部 / 0 未读 / 1 已读
const isRead = ref('')
const loading = ref(false)
// 当前展开的消息 id
const expandedId = ref(null)

// 拉取消息分页
async function fetchList() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (isRead.value !== '') params.isRead = isRead.value
    const data = await getMessagePage(params)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

// 切换筛选：重置页码与展开状态
function onFilterChange() {
  pageNum.value = 1
  expandedId.value = null
  fetchList()
}

// 类型图标与样式映射
function typeIcon(type) {
  const map = { 预约: '📅', 违规: '⚠️', 系统: '📢' }
  return map[type] || '📢'
}

function typeClass(type) {
  const map = { 预约: 'blue', 违规: 'red', 系统: 'gray' }
  return map[type] || 'gray'
}

function typeTag(type) {
  const map = { 预约: 'primary', 违规: 'danger', 系统: 'info' }
  return map[type] || 'info'
}

// 点击单条：展开内容 + 未读则标记已读
async function onMsgClick(msg) {
  expandedId.value = expandedId.value === msg.msgId ? null : msg.msgId
  if (msg.isRead === 0) {
    await markMessageRead(msg.msgId)
    msg.isRead = 1
  }
}

// 全部已读
async function onReadAll() {
  await markAllMessageRead()
  ElMessage.success('已全部标记为已读')
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.head-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-size: 20px;
  color: var(--color-text-primary);
}

.filter-tabs {
  margin: 14px 0 18px;
}

.msg-list {
  display: flex;
  flex-direction: column;
  min-height: 200px;
}

.msg-item {
  display: flex;
  gap: 12px;
  padding: 14px 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s ease;
  border-bottom: 1px dashed var(--color-border);
}

.msg-item:last-child {
  border-bottom: none;
}

.msg-item:hover {
  background: var(--color-primary-light);
}

/* 类型圆角图标 */
.msg-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.msg-icon.blue {
  background: var(--color-primary-light);
}

.msg-icon.red {
  background: #fff1f0;
}

.msg-icon.gray {
  background: #f3f4f6;
}

.msg-body {
  flex: 1;
  min-width: 0;
}

.msg-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 未读蓝点 */
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary);
  flex-shrink: 0;
}

.msg-title {
  font-size: 14px;
  color: var(--color-text-primary);
}

.msg-title.bold {
  font-weight: 700;
}

.msg-time {
  margin-left: auto;
  font-size: 12px;
  color: var(--color-text-secondary);
  flex-shrink: 0;
}

.msg-content {
  margin-top: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.7;
  background: var(--color-primary-light);
  border-radius: 8px;
  padding: 10px 12px;
  white-space: pre-wrap;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
