<template>
  <div class="admin-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form inline :model="query" @submit.prevent>
        <el-form-item label="关键字">
          <el-input
            v-model="query.keyword"
            placeholder="自习室名称"
            clearable
            style="width: 180px"
            @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="楼层">
          <el-select
            v-model="query.floorId"
            placeholder="全部楼层"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="floor in floorList"
              :key="floor.floorId"
              :label="`${floor.floorNum}F ${floor.floorName || ''}`"
              :value="floor.floorId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
            <el-option label="开放" :value="1" />
            <el-option label="关闭" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">🔍 查询</el-button>
          <el-button @click="onReset">重置</el-button>
          <el-button type="success" plain @click="openRoomDialog()">＋ 新增自习室</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 自习室表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="roomName" label="自习室名称" min-width="140" />
        <el-table-column label="楼层" width="90">
          <template #default="{ row }">
            {{ floorMap[row.floorId] ? `${floorMap[row.floorId].floorNum}F` : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="开放时间" width="130">
          <template #default="{ row }">
            {{ formatTime(row.openTime) }} - {{ formatTime(row.closeTime) }}
          </template>
        </el-table-column>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="roomTypeTag(row.roomType)" effect="light">{{ row.roomType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="seatCount" label="座位数" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="onStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" plain @click="openSeatDrawer(row)">
              座位管理
            </el-button>
            <el-button type="primary" size="small" @click="openRoomDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" plain @click="onDeleteRoom(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无自习室数据" />
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="onSizeChange"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 新增/编辑自习室弹窗 -->
    <el-dialog
      v-model="roomDialogVisible"
      :title="roomForm.roomId ? '编辑自习室' : '新增自习室'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="roomFormRef" :model="roomForm" :rules="roomRules" label-width="90px">
        <el-form-item label="所属楼层" prop="floorId">
          <el-select v-model="roomForm.floorId" placeholder="请选择楼层" style="width: 100%">
            <el-option
              v-for="floor in floorList"
              :key="floor.floorId"
              :label="`${floor.floorNum}F ${floor.floorName || ''}`"
              :value="floor.floorId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="自习室名称" prop="roomName">
          <el-input v-model="roomForm.roomName" maxlength="50" placeholder="如：三楼静音区" />
        </el-form-item>
        <el-form-item label="开放时间" prop="openTime">
          <el-time-picker
            v-model="roomForm.openTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="开放时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="关闭时间" prop="closeTime">
          <el-time-picker
            v-model="roomForm.closeTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="关闭时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="类型" prop="roomType">
          <el-select v-model="roomForm.roomType" placeholder="请选择类型" style="width: 100%">
            <el-option label="普通" value="普通" />
            <el-option label="静音" value="静音" />
            <el-option label="研讨" value="研讨" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roomForm.status">
            <el-radio :value="1">开放</el-radio>
            <el-radio :value="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input
            v-model="roomForm.description"
            type="textarea"
            :rows="3"
            maxlength="255"
            placeholder="选填，简单介绍该自习室"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roomDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitRoom">保存</el-button>
      </template>
    </el-dialog>

    <!-- 座位管理抽屉 -->
    <el-drawer v-model="seatDrawerVisible" :title="`座位管理 - ${currentRoom?.roomName || ''}`" size="62%">
      <div class="drawer-toolbar">
        <el-alert
          type="info"
          :closable="false"
          :title="`当前共 ${seatList.length} 个座位，可单行编辑或批量生成`"
        />
        <el-button type="warning" @click="batchDialogVisible = true">🪑 批量生成座位</el-button>
      </div>

      <el-table :data="seatList" v-loading="seatLoading" stripe size="small">
        <el-table-column prop="seatCode" label="座位编号" width="100" />
        <el-table-column prop="seatType" label="座位类型" width="130">
          <template #default="{ row }">
            <el-select v-if="editingId === row.seatId" v-model="row.seatType" size="small">
              <el-option label="普通" value="普通" />
              <el-option label="靠窗" value="靠窗" />
              <el-option label="带插座" value="带插座" />
            </el-select>
            <span v-else>{{ row.seatType }}</span>
          </template>
        </el-table-column>
        <el-table-column label="有插座" width="100">
          <template #default="{ row }">
            <el-switch
              v-if="editingId === row.seatId"
              v-model="row.hasSocket"
              :active-value="1"
              :inactive-value="0"
            />
            <el-tag v-else :type="row.hasSocket === 1 ? 'success' : 'info'" effect="light">
              {{ row.hasSocket === 1 ? '有' : '无' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-if="editingId === row.seatId"
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
            />
            <el-tag v-else :type="row.status === 1 ? 'success' : 'danger'" effect="light">
              {{ row.status === 1 ? '可用' : '不可用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="editingId === row.seatId">
              <el-button type="success" size="small" @click="saveSeat(row)">保存</el-button>
              <el-button size="small" @click="cancelEdit(row)">取消</el-button>
            </template>
            <template v-else>
              <el-button type="primary" size="small" plain @click="startEdit(row)">编辑</el-button>
              <el-button type="danger" size="small" plain @click="onDeleteSeat(row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无座位，可点击右上角批量生成" />
        </template>
      </el-table>
    </el-drawer>

    <!-- 批量生成座位弹窗 -->
    <el-dialog v-model="batchDialogVisible" title="批量生成座位" width="440px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="行数">
          <el-input-number v-model="batchForm.rows" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="列数">
          <el-input-number v-model="batchForm.cols" :min="1" :max="20" />
        </el-form-item>
      </el-form>
      <p class="batch-warning">⚠️ 将清空该自习室全部座位并重新生成，未完成订单会受阻！</p>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitBatch">确认生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getFloorList,
  getRoomPage,
  addRoom,
  updateRoom,
  updateRoomStatus,
  deleteRoom,
  getSeatList,
  batchGenerateSeats,
  updateSeat,
  deleteSeat
} from '@/api/admin/room'
import { formatTime, roomTypeTag } from '@/utils/format'

// ==================== 自习室列表 ====================
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const floorList = ref([])
const query = reactive({ keyword: '', floorId: null, status: null })

// 楼层 id -> 楼层信息映射（表格展示楼层号）
const floorMap = computed(() => {
  const map = {}
  floorList.value.forEach((f) => (map[f.floorId] = f))
  return map
})

// 拉取自习室分页
async function fetchList() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (query.keyword) params.keyword = query.keyword
    if (query.floorId !== null && query.floorId !== '') params.floorId = query.floorId
    if (query.status !== null && query.status !== '') params.status = query.status
    const data = await getRoomPage(params)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

// 查询：重置页码
function onSearch() {
  pageNum.value = 1
  fetchList()
}

// 重置筛选条件
function onReset() {
  query.keyword = ''
  query.floorId = null
  query.status = null
  onSearch()
}

// 改变每页条数：重置页码后重新查询
function onSizeChange() {
  pageNum.value = 1
  fetchList()
}

// 启用/停用开关（失败时回滚状态）
async function onStatusChange(row) {
  try {
    await updateRoomStatus(row.roomId, row.status)
    ElMessage.success(row.status === 1 ? '已开放' : '已关闭')
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
  }
}

// 删除自习室
async function onDeleteRoom(row) {
  const confirmed = await ElMessageBox.confirm(
    `确定删除自习室「${row.roomName}」吗？其下座位与关联数据可能受影响，删除后不可恢复。`,
    '删除确认',
    { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
  ).catch(() => false)
  if (!confirmed) return
  await deleteRoom(row.roomId)
  ElMessage.success('删除成功')
  fetchList()
}

// ==================== 新增/编辑弹窗 ====================
const roomDialogVisible = ref(false)
const submitting = ref(false)
const roomFormRef = ref(null)
const roomForm = reactive({
  roomId: null,
  floorId: null,
  roomName: '',
  openTime: '08:00',
  closeTime: '22:00',
  roomType: '普通',
  status: 1,
  description: ''
})

const roomRules = {
  floorId: [{ required: true, message: '请选择楼层', trigger: 'change' }],
  roomName: [{ required: true, message: '请输入自习室名称', trigger: 'blur' }],
  openTime: [{ required: true, message: '请选择开放时间', trigger: 'change' }],
  closeTime: [{ required: true, message: '请选择关闭时间', trigger: 'change' }],
  roomType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 打开弹窗（row 为空即新增）
function openRoomDialog(row) {
  if (row) {
    Object.assign(roomForm, {
      roomId: row.roomId,
      floorId: row.floorId,
      roomName: row.roomName,
      openTime: formatTime(row.openTime),
      closeTime: formatTime(row.closeTime),
      roomType: row.roomType,
      status: row.status,
      description: row.description || ''
    })
  } else {
    Object.assign(roomForm, {
      roomId: null,
      floorId: null,
      roomName: '',
      openTime: '08:00',
      closeTime: '22:00',
      roomType: '普通',
      status: 1,
      description: ''
    })
  }
  roomDialogVisible.value = true
}

// 提交新增/编辑
async function submitRoom() {
  await roomFormRef.value.validate()
  const data = {
    floorId: roomForm.floorId,
    roomName: roomForm.roomName,
    openTime: roomForm.openTime,
    closeTime: roomForm.closeTime,
    roomType: roomForm.roomType,
    status: roomForm.status,
    description: roomForm.description
  }
  submitting.value = true
  try {
    if (roomForm.roomId) {
      await updateRoom(roomForm.roomId, data)
      ElMessage.success('修改成功')
    } else {
      await addRoom(data)
      ElMessage.success('新增成功')
    }
    roomDialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

// ==================== 座位管理抽屉 ====================
const seatDrawerVisible = ref(false)
const seatLoading = ref(false)
const seatList = ref([])
const currentRoom = ref(null)
const editingId = ref(null)

// 打开抽屉并加载座位列表
async function openSeatDrawer(room) {
  currentRoom.value = room
  seatDrawerVisible.value = true
  editingId.value = null
  seatLoading.value = true
  try {
    seatList.value = await getSeatList(room.roomId)
  } finally {
    seatLoading.value = false
  }
}

// 进入行编辑模式（备份原值用于取消）
let editBackup = null
function startEdit(row) {
  editingId.value = row.seatId
  editBackup = { seatType: row.seatType, hasSocket: row.hasSocket, status: row.status }
}

// 取消行编辑并还原
function cancelEdit() {
  if (editBackup) {
    const row = seatList.value.find((s) => s.seatId === editingId.value)
    if (row) Object.assign(row, editBackup)
  }
  editingId.value = null
  editBackup = null
}

// 保存行编辑（SeatUpdateDTO）
async function saveSeat(row) {
  await updateSeat(row.seatId, {
    seatType: row.seatType,
    hasSocket: row.hasSocket,
    status: row.status
  })
  ElMessage.success(`座位 ${row.seatCode} 已更新`)
  editingId.value = null
  editBackup = null
}

// 删除单个座位
async function onDeleteSeat(row) {
  const confirmed = await ElMessageBox.confirm(
    `确定删除座位「${row.seatCode}」吗？`,
    '删除确认',
    { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
  ).catch(() => false)
  if (!confirmed) return
  await deleteSeat(row.seatId)
  ElMessage.success('删除成功')
  seatList.value = seatList.value.filter((s) => s.seatId !== row.seatId)
}

// ==================== 批量生成座位 ====================
const batchDialogVisible = ref(false)
const batchForm = reactive({ rows: 4, cols: 8 })

// 提交批量生成（SeatBatchDTO：rows/cols）
async function submitBatch() {
  submitting.value = true
  try {
    await batchGenerateSeats(currentRoom.value.roomId, {
      rows: batchForm.rows,
      cols: batchForm.cols
    })
    ElMessage.success(`已生成 ${batchForm.rows * batchForm.cols} 个座位`)
    batchDialogVisible.value = false
    // 刷新座位列表与自习室座位数
    seatList.value = await getSeatList(currentRoom.value.roomId)
    fetchList()
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  floorList.value = await getFloorList()
  fetchList()
})
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card,
.table-card {
  border-radius: var(--radius-base);
  box-shadow: var(--shadow-card);
  border: none;
}

/* 搜索表单去掉底部间距 */
.search-card :deep(.el-form-item) {
  margin-bottom: 0;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* 座位抽屉工具栏 */
.drawer-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

/* 批量生成红色警示文案 */
.batch-warning {
  color: var(--color-danger);
  font-weight: 600;
  text-align: center;
  margin: 0;
}
</style>
