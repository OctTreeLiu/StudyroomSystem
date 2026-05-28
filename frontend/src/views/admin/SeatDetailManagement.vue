<template>
  <div class="seat-detail-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button 
            text 
            @click="goBack"
            class="back-btn"
          >
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <span class="header-title">座位管理 - {{ currentRoom?.roomName || '加载中...' }}</span>
        </div>
      </template>

      <!-- 大区信息 -->
      <div v-if="currentRoom" class="room-info-section">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="大区编号">{{ currentRoom.roomNumber }}</el-descriptions-item>
          <el-descriptions-item label="大区名称">{{ currentRoom.roomName }}</el-descriptions-item>
          <el-descriptions-item label="位置" :span="2">{{ currentRoom.location }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 筛选和操作 -->
      <div class="filter-section">
        <el-form :inline="true">
          <el-form-item label="筛选状态">
            <el-select 
              v-model="filterStatus" 
              placeholder="全部状态" 
              style="width: 150px" 
              clearable
              @change="handleStatusFilterChange"
            >
              <el-option label="空闲" :value="0" />
              <el-option label="已预约" :value="1" />
              <el-option label="长期租赁" :value="2" />
              <el-option label="维护中" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadSeats">刷新</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 座位列表 -->
      <div v-loading="loading">
        <el-table 
          :data="paginatedSeatList" 
          stripe 
          style="width: 100%; margin-top: 20px"
        >
          <el-table-column prop="seatNumber" label="座位编号" width="120" />
          <el-table-column prop="seatName" label="座位名称" width="200" />
          <el-table-column prop="status" label="当前状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getStatusType(row.status)" 
                :class="['status-tag', `status-${row.status}`]"
              >
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="openTimelineDialog(row)">使用情况</el-button>
              <el-tooltip
                :content="getDisableSetStatusTip(row)"
                :disabled="!isSeatStatusProtected(row?.status)"
                placement="top"
              >
                <span>
                  <el-button
                    type="primary"
                    size="small"
                    :disabled="isSeatStatusProtected(row?.status)"
                    @click="openStatusDialog(row)"
                  >
                    设置状态
                  </el-button>
                </span>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页组件 -->
        <div class="pagination-container" v-if="seatList.length > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="seatList.length"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>

      <!-- 使用情况时间轴对话框 -->
      <el-dialog
        v-model="timelineDialogVisible"
        title="座位使用情况时间轴"
        width="90%"
        :close-on-click-modal="false"
      >
        <div v-if="currentSeat" class="timeline-container">
          <div class="timeline-header">
            <div class="seat-info">
              <h3>{{ currentSeat.seatNumber }} - {{ currentSeat.seatName }}</h3>
              <p>大区：{{ currentSeat.roomNumber }} - {{ currentSeat.roomName }}</p>
            </div>
            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="loadTimeline"
              style="width: 200px"
            />
          </div>
          
          <div v-loading="timelineLoading" class="timeline-content">
            <div v-if="timeline.length === 0" class="empty-timeline">
              <el-empty description="暂无使用记录" />
            </div>
            <div v-else class="timeline-wrapper">
              <!-- 时间轴 -->
              <div class="timeline-axis">
                <div class="time-labels">
                  <div v-for="hour in 24" :key="hour" class="time-label">
                    {{ String(hour - 1).padStart(2, '0') }}:00
                  </div>
                </div>
                <div class="timeline-slots">
                  <div
                    v-for="(slot, index) in timeline"
                    :key="index"
                    class="time-slot"
                    :class="`slot-${slot.statusType}`"
                    :style="getSlotStyle(slot)"
                    :title="`${slot.startTime} - ${slot.endTime} ${slot.statusText}${slot.username ? ' (' + slot.username + ')' : ''}`"
                  >
                    <div class="slot-content">
                      <div class="slot-time">{{ slot.startTime }} - {{ slot.endTime }}</div>
                      <div class="slot-status">{{ slot.statusText }}</div>
                      <div v-if="slot.username" class="slot-user">{{ slot.username }}</div>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 图例 -->
              <div class="timeline-legend">
                <div class="legend-item">
                  <span class="legend-color free"></span>
                  <span>空闲</span>
                </div>
                <div class="legend-item">
                  <span class="legend-color reserved"></span>
                  <span>已预约</span>
                </div>
                <div class="legend-item">
                  <span class="legend-color locked"></span>
                  <span>已锁定</span>
                </div>
                <div class="legend-item">
                  <span class="legend-color leased"></span>
                  <span>长期租赁</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-dialog>

      <!-- 内联编辑表单 -->
      <div v-if="statusDialogVisible" class="edit-form-container">
        <div class="edit-form-header">
          <h3>设置座位状态 - {{ currentSeat?.seatNumber }}</h3>
          <el-button text @click="statusDialogVisible = false">关闭</el-button>
        </div>
        <el-form :model="statusForm" label-width="100px" class="edit-form">
          <el-form-item label="当前状态">
            <el-tag 
              :type="getStatusType(currentSeat?.status)" 
              :class="['status-tag', `status-${currentSeat?.status}`]"
            >
              {{ getStatusText(currentSeat?.status) }}
            </el-tag>
          </el-form-item>
          <el-form-item label="座位信息">
            <div style="line-height: 1.8">
              <div><strong>大区：</strong>{{ currentSeat?.roomNumber }} - {{ currentSeat?.roomName }}</div>
              <div><strong>座位编号：</strong>{{ currentSeat?.seatNumber }}</div>
              <div><strong>座位名称：</strong>{{ currentSeat?.seatName }}</div>
            </div>
          </el-form-item>
          <el-form-item label="设置状态" required>
            <el-radio-group v-model="statusForm.status">
              <el-radio :label="0">
                <el-tag 
                  type="success" 
                  class="status-tag status-0"
                  style="margin-right: 5px"
                >
                  空闲
                </el-tag>
              </el-radio>
              <el-radio :label="1">
                <el-tag 
                  type="warning" 
                  class="status-tag status-1"
                  style="margin-right: 5px"
                >
                  已预约
                </el-tag>
              </el-radio>
              <el-radio :label="2">
                <el-tag 
                  class="status-tag status-2"
                  style="margin-right: 5px"
                >
                  长期租赁
                </el-tag>
              </el-radio>
              <el-radio :label="3">
                <el-tag 
                  class="status-tag status-3"
                  style="margin-right: 5px"
                >
                  维护中
                </el-tag>
              </el-radio>
              <el-radio :label="4">
                <el-tag 
                  type="danger" 
                  class="status-tag status-4"
                  style="margin-right: 5px"
                >
                  锁定中
                </el-tag>
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button @click="statusDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="updateLoading" @click="handleUpdateStatus">确定</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getSeatsByRoomId, updateSeatStatus, getSeatUsageTimeline } from '@/api/admin/seat'
import { getStudyRoomById } from '@/api/studyroom'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const updateLoading = ref(false)
const seatList = ref([])
const currentRoom = ref(null)
const filterStatus = ref(null)
const statusDialogVisible = ref(false)
const timelineDialogVisible = ref(false)
const currentSeat = ref(null)
const statusForm = ref({
  status: null
})
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const timeline = ref([])
const timelineLoading = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 计算当前页显示的座位列表
const paginatedSeatList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return seatList.value.slice(start, end)
})

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(seatList.value.length / pageSize.value)
})

const getStatusText = (status) => {
  const statusMap = {
    0: '空闲',
    1: '已预约',
    2: '长期租赁',
    3: '维护中',
    4: '已锁定'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status) => {
  const typeMap = {
    0: 'success',
    1: 'warning',
    // 2(长期租赁) 使用自定义蓝色样式，不依赖内置 type
    2: '',
    // 3(维护中) 使用自定义灰色样式
    3: '',
    // 4(已锁定) 使用红色
    4: 'danger'
  }
  return typeMap[status] || 'info'
}

const isSeatStatusProtected = (status) => {
  const s = Number(status)
  // 仅当座位为“空闲(0)”或“维护中(3)”时允许修改，其余状态全部禁止
  return !(s === 0 || s === 3)
}

const getDisableSetStatusTip = (seat) => {
  const s = Number(seat?.status)
  if (s === 1) return '该座位当前为“已预约”，仅系统流程可变更状态，管理员不能直接修改'
  if (s === 2) return '该座位当前为“长期租赁”，仅系统流程可变更状态，管理员不能直接修改'
  if (s === 4) return '该座位当前为“锁定中（未支付）”，请通过处理相关订单来变更状态'
  return '仅当座位为“空闲”或“维护中”时才允许修改状态'
}

const loadRoom = async () => {
  const roomId = route.params.roomId
  if (!roomId) {
    ElMessage.error('大区ID不存在')
    goBack()
    return
  }
  
  try {
    const res = await getStudyRoomById(roomId)
    if (res.code === 200) {
      currentRoom.value = res.data
      loadSeats()
    } else {
      ElMessage.error('加载大区信息失败')
      goBack()
    }
  } catch (error) {
    ElMessage.error('加载大区信息失败')
    goBack()
  }
}

const loadSeats = async () => {
  const roomId = route.params.roomId
  if (!roomId) {
    return
  }
  
  loading.value = true
  try {
    const res = await getSeatsByRoomId(roomId)
    if (res.code === 200) {
      let seats = res.data || []
      
      // 按状态筛选
      if (filterStatus.value !== null && filterStatus.value !== undefined && filterStatus.value !== '') {
        seats = seats.filter(seat => seat.status === filterStatus.value)
      }
      
      seatList.value = seats
      // 如果当前页超出范围，重置到第一页
      if (currentPage.value > totalPages.value && totalPages.value > 0) {
        currentPage.value = 1
      }
    }
  } catch (error) {
    ElMessage.error('加载座位列表失败')
  } finally {
    loading.value = false
  }
}

// 分页变化处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

const openStatusDialog = (seat) => {
  if (isSeatStatusProtected(seat?.status)) {
    ElMessage.warning(getDisableSetStatusTip(seat) || '仅当座位为“空闲”或“维护中”时才允许修改状态')
    return
  }
  currentSeat.value = seat
  statusForm.value.status = seat.status !== null && seat.status !== undefined ? seat.status : 0
  statusDialogVisible.value = true
}

const handleStatusFilterChange = () => {
  currentPage.value = 1
  loadSeats()
}

const handleUpdateStatus = async () => {
  if (isSeatStatusProtected(currentSeat.value?.status)) {
    ElMessage.warning(getDisableSetStatusTip(currentSeat.value) || '仅当座位为“空闲”或“维护中”时才允许修改状态')
    return
  }
  if (statusForm.value.status === null || statusForm.value.status === undefined) {
    ElMessage.warning('请选择状态')
    return
  }
  
  if (statusForm.value.status === currentSeat.value.status) {
    ElMessage.info('状态未发生变化')
    statusDialogVisible.value = false
    return
  }
  
  // 如果设置为维护中，需要确认
  if (statusForm.value.status === 3) {
    try {
      await ElMessageBox.confirm(
        '确定要将该座位设置为"维护中"吗？维护中的座位将无法被预约或租赁。',
        '确认操作',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } catch {
      return
    }
  }
  
  updateLoading.value = true
  try {
    const res = await updateSeatStatus(currentSeat.value.id, statusForm.value.status)
    if (res.code === 200) {
      ElMessage.success('座位状态更新成功')
      statusDialogVisible.value = false
      loadSeats()
    }
  } catch (error) {
    ElMessage.error('更新座位状态失败')
  } finally {
    updateLoading.value = false
  }
}

const openTimelineDialog = (seat) => {
  if (seat?.status === 3) {
    ElMessage.warning('该座位维护中，暂不可查看使用时间轴')
    return
  }
  currentSeat.value = seat
  selectedDate.value = dayjs().format('YYYY-MM-DD')
  timelineDialogVisible.value = true
  loadTimeline()
}

const loadTimeline = async () => {
  if (!currentSeat.value || !selectedDate.value) {
    return
  }
  
  timelineLoading.value = true
  try {
    const res = await getSeatUsageTimeline(currentSeat.value.id, selectedDate.value)
    if (res.code === 200) {
      timeline.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载使用情况失败')
      timeline.value = []
    }
  } catch (error) {
    ElMessage.error('加载使用情况失败')
    timeline.value = []
  } finally {
    timelineLoading.value = false
  }
}

// 计算时间段样式
const getSlotStyle = (slot) => {
  const startMinutes = timeToMinutes(slot.startTime)
  const endMinutes = timeToMinutes(slot.endTime)
  const totalMinutes = 24 * 60
  const left = (startMinutes / totalMinutes) * 100
  const width = Math.max(((endMinutes - startMinutes) / totalMinutes) * 100, 0.5)
  
  return {
    left: `${left}%`,
    width: `${width}%`,
    minWidth: width < 2 ? '40px' : 'auto'
  }
}

// 将时间字符串转换为分钟数
const timeToMinutes = (timeStr) => {
  if (timeStr === '24:00') {
    return 24 * 60
  }
  const [hours, minutes] = timeStr.split(':').map(Number)
  return hours * 60 + minutes
}

const goBack = () => {
  router.push({ name: 'AdminSeatManagement' })
}

onMounted(() => {
  loadRoom()
})
</script>

<style scoped>
.seat-detail-management {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
}

.header-title {
  flex: 1;
}

.room-info-section {
  margin-bottom: 20px;
}

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 4px;
}

:deep(.el-card) {
  border-radius: 4px;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

:deep(.el-radio-group) {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

:deep(.el-pagination) {
  margin-top: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

.edit-form-container {
  margin-top: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.edit-form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.edit-form-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

/* 时间轴样式 */
.timeline-container {
  padding: 20px;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e4e7ed;
}

.seat-info h3 {
  margin: 0 0 10px 0;
  font-size: 20px;
  color: #303133;
}

.seat-info p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.timeline-content {
  min-height: 200px;
}

.empty-timeline {
  padding: 40px 0;
}

.timeline-wrapper {
  margin-top: 20px;
}

.timeline-axis {
  position: relative;
  height: 120px;
  background: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 30px;
}

.time-labels {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 30px;
  display: flex;
  border-bottom: 1px solid #e4e7ed;
  background: white;
}

.time-label {
  flex: 1;
  text-align: center;
  font-size: 11px;
  color: #909399;
  line-height: 30px;
  border-right: 1px solid #e4e7ed;
}

.time-label:last-child {
  border-right: none;
}

.timeline-slots {
  position: relative;
  top: 30px;
  height: 90px;
  width: 100%;
}

.time-slot {
  position: absolute;
  height: 60px;
  top: 15px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
  box-sizing: border-box;
}

.time-slot:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 10;
}

.slot-free {
  background: #67c23a;
  color: white;
}

.slot-reserved {
  background: #e6a23c;
  color: white;
}

.slot-locked {
  background: #f56c6c;
  color: white;
}

.slot-leased {
  background: #409eff;
  color: white;
}

.slot-content {
  text-align: center;
  padding: 5px;
  width: 100%;
  overflow: hidden;
  white-space: nowrap;
}

.slot-time {
  font-size: 11px;
  font-weight: bold;
  margin-bottom: 2px;
}

.slot-status {
  font-size: 12px;
  font-weight: 500;
}

.slot-user {
  font-size: 10px;
  margin-top: 2px;
  opacity: 0.9;
  overflow: hidden;
  text-overflow: ellipsis;
}

.timeline-legend {
  display: flex;
  justify-content: center;
  gap: 30px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 4px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 4px;
}

.legend-color.free {
  background: #67c23a;
}

.legend-color.reserved {
  background: #e6a23c;
}

.legend-color.locked {
  background: #f56c6c;
}

.legend-color.leased {
  background: #409eff;
}

/* 状态标签颜色：0空闲-绿，1已预约-橙，2长期租赁-蓝，3维护中-灰，4已锁定-红 */
.status-tag {
  border-radius: 4px;
}

.status-0 {
  background-color: #67c23a !important;
  border-color: #67c23a !important;
  color: #fff !important;
}

.status-1 {
  background-color: #e6a23c !important;
  border-color: #e6a23c !important;
  color: #fff !important;
}

.status-2 {
  background-color: #409eff !important;
  border-color: #409eff !important;
  color: #fff !important;
}

.status-3 {
  background-color: #909399 !important;
  border-color: #909399 !important;
  color: #fff !important;
}

.status-4 {
  background-color: #f56c6c !important;
  border-color: #f56c6c !important;
  color: #fff !important;
}
</style>

