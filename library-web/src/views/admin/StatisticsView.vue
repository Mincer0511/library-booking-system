<template>
  <div class="admin-page">
    <!-- 区块一：每日订单趋势 -->
    <el-card shadow="never">
      <div class="block-header">
        <span class="block-title">📈 每日订单趋势</span>
        <el-radio-group v-model="trendDays" size="small" @change="fetchTrend">
          <el-radio-button :value="7">近7天</el-radio-button>
          <el-radio-button :value="14">近14天</el-radio-button>
          <el-radio-button :value="30">近30天</el-radio-button>
        </el-radio-group>
      </div>
      <div ref="trendRef" class="chart-box"></div>
    </el-card>

    <!-- 区块二：热门时段 -->
    <el-card shadow="never">
      <div class="block-header">
        <span class="block-title">⏰ 热门开始时段</span>
        <el-radio-group v-model="hotDays" size="small" @change="fetchHot">
          <el-radio-button :value="7">近7天</el-radio-button>
          <el-radio-button :value="14">近14天</el-radio-button>
          <el-radio-button :value="30">近30天</el-radio-button>
        </el-radio-group>
      </div>
      <div ref="hotRef" class="chart-box"></div>
    </el-card>

    <!-- 区块三：自习室利用率 -->
    <el-card shadow="never">
      <div class="block-header">
        <span class="block-title">🏫 自习室利用率</span>
        <div class="util-toolbar">
          <el-date-picker
            v-model="utilRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :clearable="false"
            style="width: 260px"
            @change="fetchUtilization"
          />
          <el-select
            v-model="floorId"
            placeholder="全部楼层"
            clearable
            style="width: 140px"
            @change="fetchUtilization"
          >
            <el-option
              v-for="f in floorList"
              :key="f.floorId"
              :label="`${f.floorNum}F ${f.floorName}`"
              :value="f.floorId"
            />
          </el-select>
          <el-button @click="fetchUtilization">查询</el-button>
          <el-button type="success" :loading="exporting" @click="onExport">
            导出 Excel
          </el-button>
        </div>
      </div>

      <el-table :data="utilList" v-loading="utilLoading" stripe max-height="420">
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="roomName" label="自习室" min-width="140" />
        <el-table-column label="楼层" width="80" align="center">
          <template #default="{ row }">{{ row.floorNum }}F</template>
        </el-table-column>
        <el-table-column prop="totalSeats" label="座位总数" width="100" align="center" />
        <el-table-column label="预约时长" width="130" align="center">
          <template #default="{ row }">{{ row.bookedMinutes }} 分钟</template>
        </el-table-column>
        <el-table-column label="利用率" min-width="200">
          <template #default="{ row }">
            <el-progress
              :percentage="clampPct(row.utilization)"
              :color="progressColor"
              :stroke-width="10"
              :format="() => (row.utilization ?? 0) + '%'"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { getDailyCount, getHotHours, getUtilization, exportUtilization } from '@/api/admin/stat'
import { getFloorList } from '@/api/admin/room'

// ===== ECharts 实例与 DOM 引用 =====
const trendRef = ref(null)
const hotRef = ref(null)
let trendChart = null
let hotChart = null

// ===== 区块一：每日订单趋势 =====
const trendDays = ref(7)

async function fetchTrend() {
  const data = (await getDailyCount(trendDays.value)) || []
  trendChart = trendChart || echarts.init(trendRef.value)
  trendChart.setOption(
    {
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 24, top: 30, bottom: 40 },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        // 日期仅显示 月-日，避免 30 天时坐标拥挤
        data: data.map((i) => String(i.date).slice(5))
      },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        {
          name: '订单数',
          type: 'line',
          smooth: true,
          symbolSize: 6,
          data: data.map((i) => i.count),
          itemStyle: { color: '#4A90E2' },
          lineStyle: { width: 3 },
          areaStyle: { color: 'rgba(74, 144, 226, 0.15)' }
        }
      ]
    },
    true
  )
}

// ===== 区块二：热门时段 =====
const hotDays = ref(30)

async function fetchHot() {
  const data = (await getHotHours(hotDays.value)) || []
  // 补齐 0-23 点，无数据的时段记 0
  const countMap = new Map(data.map((i) => [i.hour, i.count]))
  const hours = Array.from({ length: 24 }, (_, h) => h)
  hotChart = hotChart || echarts.init(hotRef.value)
  hotChart.setOption(
    {
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 24, top: 30, bottom: 40 },
      xAxis: {
        type: 'category',
        data: hours.map((h) => `${h}时`),
        axisLabel: { interval: 1 }
      },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        {
          name: '预约数',
          type: 'bar',
          barMaxWidth: 24,
          data: hours.map((h) => countMap.get(h) || 0),
          itemStyle: { color: '#4A90E2', borderRadius: [6, 6, 0, 0] }
        }
      ]
    },
    true
  )
}

// ===== 区块三：自习室利用率 =====
const utilRange = ref([
  dayjs().subtract(6, 'day').format('YYYY-MM-DD'),
  dayjs().format('YYYY-MM-DD')
])
const floorId = ref(null)
const floorList = ref([])
const utilList = ref([])
const utilLoading = ref(false)
const exporting = ref(false)

async function fetchUtilization() {
  if (!utilRange.value || utilRange.value.length !== 2) {
    ElMessage.warning('请选择开始与结束日期')
    return
  }
  utilLoading.value = true
  try {
    const params = { startDate: utilRange.value[0], endDate: utilRange.value[1] }
    if (floorId.value != null) params.floorId = floorId.value
    utilList.value = (await getUtilization(params)) || []
  } finally {
    utilLoading.value = false
  }
}

async function loadFloors() {
  floorList.value = (await getFloorList()) || []
}

// 导出 Excel（后端导出全量数据，不含楼层过滤）
async function onExport() {
  if (!utilRange.value || utilRange.value.length !== 2) {
    ElMessage.warning('请选择开始与结束日期')
    return
  }
  exporting.value = true
  try {
    await exportUtilization(utilRange.value[0], utilRange.value[1])
    ElMessage.success('导出成功')
  } finally {
    exporting.value = false
  }
}

// 利用率进度条：百分比钳制到 0-100 供进度条渲染
function clampPct(v) {
  return Math.max(0, Math.min(100, Math.round(Number(v) || 0)))
}

// 按利用率着色：>=80 绿 / >=50 蓝 / 其余橙
function progressColor(p) {
  if (p >= 80) return '#52C41A'
  if (p >= 50) return '#4A90E2'
  return '#FAAD14'
}

// ===== 自适应与销毁 =====
function handleResize() {
  trendChart?.resize()
  hotChart?.resize()
}

onMounted(() => {
  fetchTrend()
  fetchHot()
  fetchUtilization()
  loadFloors()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  hotChart?.dispose()
  trendChart = null
  hotChart = null
})
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 卡片：白底 12px 圆角 + 柔和阴影 */
.admin-page :deep(.el-card) {
  border-radius: var(--radius-base);
  border: none;
  box-shadow: var(--shadow-card);
}

/* 按钮 hover 轻微上浮 */
.admin-page :deep(.el-button) {
  transition: all 0.2s ease;
}
.admin-page :deep(.el-button:not(.is-link):not(.is-text):hover) {
  transform: translateY(-1px);
}

.block-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
}

.block-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.util-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

/* 图表容器固定高度，保证 ECharts 正常渲染 */
.chart-box {
  width: 100%;
  height: 340px;
}
</style>
