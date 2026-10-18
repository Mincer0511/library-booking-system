<template>
  <div v-loading="loading" class="dashboard-page">
    <!-- 顶部统计卡片 -->
    <div class="stat-cards">
      <div v-for="card in statCards" :key="card.label" class="stat-card">
        <div class="stat-icon" :style="{ background: card.bg }">{{ card.icon }}</div>
        <div class="stat-info">
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-row">
      <el-card shadow="never" class="chart-card">
        <template #header>
          <span class="chart-title">📈 近 7 日预约趋势</span>
        </template>
        <div ref="trendRef" class="chart-box" />
      </el-card>

      <el-card shadow="never" class="chart-card">
        <template #header>
          <span class="chart-title">🔥 近 30 天热门开始时段</span>
        </template>
        <div ref="hourRef" class="chart-box" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboard } from '@/api/admin/stat'
import dayjs from 'dayjs'

// 看板聚合数据
const dashboard = ref({})
const loading = ref(true)

// 顶部 4 个统计卡片（按 DashboardVO 实际字段）
const statCards = computed(() => [
  { icon: '📅', label: '今日预约数', value: dashboard.value.todayBookings ?? 0, bg: '#E8F3FF' },
  { icon: '🔥', label: '进行中订单', value: dashboard.value.inUseCount ?? 0, bg: '#FFF1E8' },
  { icon: '💤', label: '今日爽约数', value: dashboard.value.todayNoShow ?? 0, bg: '#FFF0F0' },
  { icon: '👥', label: '注册用户数', value: dashboard.value.userCount ?? 0, bg: '#EFFFF2' }
])

// 图表 DOM 与实例
const trendRef = ref(null)
const hourRef = ref(null)
let trendChart = null
let hourChart = null

// 近 7 日预约趋势折线图
function renderTrend(list) {
  const dates = (list || []).map((i) => dayjs(i.date).format('MM-DD'))
  const counts = (list || []).map((i) => i.count ?? 0)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#D6E4FF' } },
      axisLabel: { color: '#6B7280' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#E8F3FF' } },
      axisLabel: { color: '#6B7280' }
    },
    series: [
      {
        name: '预约数',
        type: 'line',
        data: counts,
        smooth: true,
        symbolSize: 8,
        itemStyle: { color: '#4A90E2' },
        lineStyle: { width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(74,144,226,0.35)' },
            { offset: 1, color: 'rgba(74,144,226,0.02)' }
          ])
        }
      }
    ]
  })
}

// 热门时段柱状图
function renderHotHours(list) {
  const sorted = [...(list || [])].sort((a, b) => a.hour - b.hour)
  const hours = sorted.map((i) => `${i.hour}时`)
  const counts = sorted.map((i) => i.count ?? 0)
  hourChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: {
      type: 'category',
      data: hours,
      axisLine: { lineStyle: { color: '#D6E4FF' } },
      axisLabel: { color: '#6B7280' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#E8F3FF' } },
      axisLabel: { color: '#6B7280' }
    },
    series: [
      {
        name: '预约数',
        type: 'bar',
        data: counts,
        barMaxWidth: 28,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#7BB8F5' },
            { offset: 1, color: '#4A90E2' }
          ]),
          borderRadius: [6, 6, 0, 0]
        }
      }
    ]
  })
}

// 窗口尺寸变化时图表自适应
function handleResize() {
  trendChart && trendChart.resize()
  hourChart && hourChart.resize()
}

onMounted(async () => {
  try {
    dashboard.value = await getDashboard()
  } finally {
    loading.value = false
  }
  await nextTick()
  trendChart = echarts.init(trendRef.value)
  hourChart = echarts.init(hourRef.value)
  renderTrend(dashboard.value.weekTrend)
  renderHotHours(dashboard.value.hotHours)
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart && trendChart.dispose()
  hourChart && hourChart.dispose()
  trendChart = null
  hourChart = null
})
</script>

<style scoped>
/* 统计卡片行 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border-radius: var(--radius-base);
  box-shadow: var(--shadow-card);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

/* 按钮/卡片 hover 轻微上浮 */
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(74, 144, 226, 0.2);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  flex-shrink: 0;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

/* 图表区域 */
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.chart-card {
  border-radius: var(--radius-base);
  box-shadow: var(--shadow-card);
  border: none;
}

.chart-title {
  font-weight: 600;
  color: var(--color-text-primary);
}

/* 图表容器必须有固定高度 */
.chart-box {
  width: 100%;
  height: 320px;
}

/* 窄屏适配 */
@media (max-width: 1100px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  .chart-row {
    grid-template-columns: 1fr;
  }
}
</style>
