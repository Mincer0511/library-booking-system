<template>
  <!-- 预约操作按钮组：按 canXxx 动态显示，全部操作二次确认 -->
  <div class="booking-actions" @click.stop>
    <el-button v-if="booking.canCheckin" type="success" :size="size" round @click="onCheckin">
      签到
    </el-button>
    <el-button v-if="booking.canCheckout" type="warning" :size="size" round @click="onCheckout">
      签退
    </el-button>
    <el-button v-if="booking.canExtend" type="primary" :size="size" round plain @click="openExtend">
      续时
    </el-button>
    <el-button v-if="booking.canCancel" type="danger" :size="size" round plain @click="onCancel">
      取消预约
    </el-button>
    <span v-if="!hasAnyAction" class="text-secondary no-action">暂无可用操作</span>

    <!-- 续时弹窗：30 分钟粒度选择新的结束时间 -->
    <el-dialog v-model="extendVisible" title="🕐 预约续时" width="400px" append-to-body>
      <p class="extend-tip">
        当前结束时间 <b>{{ formatTime(booking.endTime) }}</b>，可延长至 22:00 前
      </p>
      <el-select
        v-model="newEndTime"
        placeholder="请选择新的结束时间"
        style="width: 100%"
        :disabled="!extendOptions.length"
      >
        <el-option v-for="t in extendOptions" :key="t" :label="t" :value="t" />
      </el-select>
      <p v-if="!extendOptions.length" class="extend-empty">已无可续时段（最晚至 22:00）</p>
      <template #footer>
        <el-button round @click="extendVisible = false">取消</el-button>
        <el-button
          type="primary"
          round
          :disabled="!newEndTime"
          :loading="submitting"
          @click="onExtendConfirm"
        >
          确认续时
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  cancelBooking,
  checkinBooking,
  checkoutBooking,
  extendBooking
} from '@/api/booking'
import { formatTime } from '@/utils/format'

const props = defineProps({
  booking: {
    type: Object,
    required: true
  },
  size: {
    type: String,
    default: 'default'
  }
})

// 操作成功后通知父组件刷新数据
const emit = defineEmits(['refresh'])

const extendVisible = ref(false)
const newEndTime = ref('')
const submitting = ref(false)

// 是否有任一可用操作
const hasAnyAction = computed(
  () =>
    props.booking.canCancel ||
    props.booking.canCheckin ||
    props.booking.canCheckout ||
    props.booking.canExtend
)

// 续时可选项：当前结束时间 +30min 起，至 22:00 止，30 分钟粒度（HH:mm）
const extendOptions = computed(() => {
  const options = []
  if (!props.booking.endTime) return options
  const [h, m] = props.booking.endTime.slice(0, 5).split(':').map(Number)
  let cur = h * 60 + m + 30
  const limit = 22 * 60
  while (cur <= limit) {
    const hh = String(Math.floor(cur / 60)).padStart(2, '0')
    const mm = String(cur % 60).padStart(2, '0')
    options.push(`${hh}:${mm}`)
    cur += 30
  }
  return options
})

// 预约禁用期（code 1001）醒目弹窗
function showDisabledAlert(msg) {
  ElMessageBox.alert(
    `${msg}。解禁后信用分将恢复至 60 分，届时可正常预约。`,
    '预约功能禁用中',
    { confirmButtonText: '我知道了', type: 'warning' }
  )
}

// 统一执行操作：成功提示 + 通知刷新；禁用期错误特殊弹窗
async function runAction(fn, successText) {
  try {
    await fn()
    ElMessage.success(successText)
    emit('refresh')
    return true
  } catch (e) {
    if (e?.code === 1001) showDisabledAlert(e.message)
    return false
  }
}

// 二次确认（用户点取消则静默返回）
async function confirm(message, title, confirmButtonText) {
  try {
    await ElMessageBox.confirm(message, title, {
      confirmButtonText,
      cancelButtonText: '再想想',
      type: 'warning'
    })
    return true
  } catch (e) {
    return false
  }
}

// 签到
async function onCheckin() {
  const ok = await confirm(
    `${props.booking.bookingDate} ${formatTime(props.booking.startTime)} 的预约，确认现在签到吗？`,
    '签到',
    '确认签到'
  )
  if (!ok) return
  runAction(() => checkinBooking(props.booking.orderId), '签到成功，开始学习吧~')
}

// 签退
async function onCheckout() {
  const ok = await confirm('确认签退并结束本次预约吗？座位将释放给其他同学~', '签退', '确认签退')
  if (!ok) return
  runAction(() => checkoutBooking(props.booking.orderId), '签退成功，辛苦了~')
}

// 取消预约
async function onCancel() {
  const ok = await confirm(
    `确定取消 ${props.booking.bookingDate} ${formatTime(props.booking.startTime)} 的预约吗？`,
    '取消预约',
    '确定取消'
  )
  if (!ok) return
  runAction(() => cancelBooking(props.booking.orderId), '已取消预约')
}

// 打开续时弹窗
function openExtend() {
  newEndTime.value = ''
  extendVisible.value = true
}

// 确认续时（弹窗内确认按钮即二次确认）
async function onExtendConfirm() {
  submitting.value = true
  try {
    const ok = await runAction(
      () => extendBooking(props.booking.orderId, newEndTime.value),
      '续时成功~'
    )
    if (ok) extendVisible.value = false
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.booking-actions {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

/* 按钮组内去掉 el-button 默认左间距，改用 gap 控制 */
.booking-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.no-action {
  font-size: 12px;
}

.extend-tip {
  margin-bottom: 14px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.extend-tip b {
  color: var(--color-primary);
}

.extend-empty {
  margin-top: 10px;
  font-size: 13px;
  color: var(--color-warning);
}
</style>
