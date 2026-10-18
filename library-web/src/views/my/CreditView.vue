<template>
  <div class="page-container" v-loading="loading">
    <template v-if="credit">
      <!-- 顶部：信用分仪表卡 + 状态卡 -->
      <div class="top-row">
        <!-- 信用分仪表卡 -->
        <div class="card-container score-card">
          <!-- 卡通装饰 -->
          <span class="deco deco-left">🎈</span>
          <span class="deco deco-right">🌈</span>
          <el-progress
            type="dashboard"
            :percentage="credit.creditScore"
            :width="190"
            :stroke-width="14"
            :color="scoreColor"
          >
            <template #default>
              <div class="score-inner">
                <div class="score-num" :style="{ color: scoreColor }">
                  {{ credit.creditScore }}
                </div>
                <div class="score-label">我的信用分</div>
              </div>
            </template>
          </el-progress>
          <p class="score-tip">{{ scoreTip }}</p>
        </div>

        <!-- 禁用状态卡 -->
        <div v-if="credit.isDisabled === 1" class="status-card disabled">
          <div class="status-icon">🚫</div>
          <h3 class="status-title">预约功能禁用中</h3>
          <p class="status-line">
            解禁时间：<b>{{ formatDateTime(credit.disableEndTime) }}</b>
          </p>
          <p class="countdown">{{ countdownText }}</p>
          <p class="status-desc">
            信用分低于 {{ credit.disableThreshold }} 分将被禁用预约 7 天，解禁后信用分恢复至 60 分
          </p>
        </div>

        <!-- 正常状态卡 -->
        <div v-else class="status-card normal">
          <div class="status-icon">✅</div>
          <h3 class="status-title">预约功能正常</h3>
          <p class="status-line">信用状态良好，请继续保持按时签到的好习惯~</p>
          <p class="status-desc">
            信用分低于 {{ credit.disableThreshold }} 分将被禁用预约 7 天
          </p>
        </div>
      </div>

      <!-- 近期扣分记录 -->
      <div class="card-container violation-card">
        <h3 class="sub-title">📉 近期扣分记录</h3>
        <template v-if="credit.recentViolations && credit.recentViolations.length">
          <div v-for="v in credit.recentViolations" :key="v.violationId" class="v-item">
            <el-tag :type="violationTagType(v.violationType)" effect="light" round>
              {{ v.violationType }}
            </el-tag>
            <span class="v-desc">{{ v.violationDesc }}</span>
            <span class="v-score">-{{ v.deductScore }} 分</span>
            <span class="v-time">{{ formatDateTime(v.createTime) }}</span>
          </div>
        </template>
        <EmptyState v-else text="暂无扣分记录，表现很棒哦~" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { getMyCredit } from '@/api/user'
import { formatDateTime, violationTagType } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const loading = ref(false)
const credit = ref(null)

// 拉取信用信息
async function fetchCredit() {
  loading.value = true
  try {
    credit.value = await getMyCredit()
  } finally {
    loading.value = false
  }
}

// 分数颜色：≥80 蓝 / 60-79 橙 / <60 红
const scoreColor = computed(() => {
  const s = credit.value?.creditScore ?? 100
  if (s >= 80) return '#4A90E2'
  if (s >= 60) return '#FAAD14'
  return '#FF4D4F'
})

// 分数提示文案
const scoreTip = computed(() => {
  const s = credit.value?.creditScore ?? 100
  if (s >= 80) return '信用优秀，继续保持哦~'
  if (s >= 60) return '信用一般，注意按时签到'
  return '信用偏低，即将被禁用预约'
})

// 解禁倒计时文案
const countdownText = computed(() => {
  const end = credit.value?.disableEndTime
  if (!end) return ''
  const endTime = dayjs(end)
  const now = dayjs()
  if (endTime.isBefore(now)) return '解禁时间已到，请刷新页面查看'
  const days = endTime.diff(now, 'day')
  const hours = endTime.diff(now, 'hour') % 24
  const minutes = endTime.diff(now, 'minute') % 60
  if (days > 0) return `⏳ 约 ${days} 天 ${hours} 小时后解禁`
  return `⏳ 约 ${hours} 小时 ${minutes} 分钟后解禁`
})

onMounted(fetchCredit)
</script>

<style scoped>
.top-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

/* 信用分仪表卡 */
.score-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28px 20px;
  position: relative;
}

.deco {
  position: absolute;
  font-size: 30px;
}

.deco-left {
  top: 16px;
  left: 22px;
}

.deco-right {
  top: 16px;
  right: 22px;
}

.score-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.score-num {
  font-size: 40px;
  font-weight: 700;
  line-height: 1.1;
}

.score-label {
  margin-top: 6px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.score-tip {
  margin-top: 14px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

/* 状态卡（禁用 / 正常） */
.status-card {
  flex: 1;
  border-radius: var(--radius-base);
  padding: 28px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 8px;
  box-shadow: var(--shadow-card);
}

.status-card.disabled {
  background: #fff1f0;
  border: 1px solid #ffccc7;
}

.status-card.normal {
  background: #f6ffed;
  border: 1px solid #d9f7be;
}

.status-icon {
  font-size: 38px;
}

.status-title {
  font-size: 18px;
  color: var(--color-text-primary);
}

.status-card.disabled .status-title {
  color: var(--color-danger);
}

.status-card.normal .status-title {
  color: var(--color-success);
}

.status-line {
  font-size: 14px;
  color: var(--color-text-primary);
}

.countdown {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-danger);
}

.status-desc {
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* 扣分记录 */
.violation-card {
  padding: 24px 20px;
}

.sub-title {
  font-size: 16px;
  margin-bottom: 16px;
  color: var(--color-text-primary);
}

.v-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px dashed var(--color-border);
  font-size: 14px;
}

.v-item:last-child {
  border-bottom: none;
}

.v-desc {
  flex: 1;
  color: var(--color-text-primary);
}

.v-score {
  color: var(--color-danger);
  font-weight: 600;
}

.v-time {
  color: var(--color-text-secondary);
  font-size: 13px;
}

/* 小屏上下排列 */
@media (max-width: 720px) {
  .top-row {
    flex-direction: column;
  }
}
</style>
