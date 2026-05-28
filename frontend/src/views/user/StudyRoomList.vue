<template>
  <div class="studyroom-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>自习室列表</span>
          <div class="header-actions">
            <el-select v-model="filterStatus" placeholder="筛选状态" style="width: 150px" @change="loadStudyRooms">
              <el-option label="全部" value="" />
              <el-option label="有座" :value="0" />
              <el-option label="无座" :value="1" />
            </el-select>
            <el-button v-if="isAdmin" type="success" style="margin-left: 10px" @click="openAddDialog">新增自习室</el-button>
            <el-button type="primary" style="margin-left: 10px" @click="loadStudyRooms">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="pagedStudyRooms" stripe style="width: 100%">
        <el-table-column prop="roomNumber" label="自习室编号" width="150" />
        <el-table-column prop="roomName" label="自习室名称" width="200" />
        <el-table-column prop="capacity" label="容量（座位数）" width="120" align="center" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 详情对话框 - 显示座位图 -->
      <el-dialog v-model="detailVisible" :title="currentRoom ? `${currentRoom.roomName} - 座位图` : '自习室详情'" width="900px">
        <div v-if="currentRoom" v-loading="seatsLoading">
          <el-descriptions :column="2" border style="margin-bottom: 20px">
            <el-descriptions-item label="自习室编号">{{ currentRoom.roomNumber }}</el-descriptions-item>
            <el-descriptions-item label="自习室名称">{{ currentRoom.roomName }}</el-descriptions-item>
            <el-descriptions-item label="容量（座位数）">{{ currentRoom.capacity }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(currentRoom.status)">
                {{ getStatusText(currentRoom.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="位置" :span="2">{{ currentRoom.location }}</el-descriptions-item>
          </el-descriptions>
          
          <!-- 座位图 -->
          <div class="seat-map-container">
            <div class="seat-map-header">
              <div class="legend">
                <span class="legend-item">
                  <span class="legend-color available"></span>
                  <span>空闲</span>
                </span>
                <span class="legend-item">
                  <span class="legend-color reserved"></span>
                  <span>已预约</span>
                </span>
                <span class="legend-item">
                  <span class="legend-color leased"></span>
                  <span>被长期租赁</span>
                </span>
                <span class="legend-item">
                  <span class="legend-color maintenance"></span>
                  <span>维护中</span>
                </span>
                <span class="legend-item">
                  <span class="legend-color locked"></span>
                  <span>已锁定</span>
                </span>
              </div>
            </div>
            <div class="seat-map">
              <div 
                v-for="seat in seatList" 
                :key="seat.id"
                class="seat-item"
                :class="getSeatClass(seat)"
                :title="`${seat.seatNumber} - ${getSeatStatusText(seat)}`"
                @click="openTimelineDialog(seat)"
              >
                <div class="seat-number">{{ seat.seatNumber }}</div>
                <div class="seat-status">{{ getSeatStatusText(seat) }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-dialog>

      <!-- 座位使用情况时间轴对话框 -->
      <el-dialog
        v-model="timelineDialogVisible"
        title="座位使用情况时间轴"
        width="90%"
        :close-on-click-modal="false"
      >
        <div v-if="currentSeat" class="timeline-container">
          <div class="timeline-header">
            <div class="seat-info">
              <h3>{{ currentSeat.seatNumber }}{{ currentSeat.seatName ? ' - ' + currentSeat.seatName : '' }}</h3>
              <p>大区：{{ currentRoom?.roomNumber }} - {{ currentRoom?.roomName }}</p>
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
                    :title="`${slot.startTime} - ${slot.endTime} ${slot.statusText}`"
                  >
                    <div class="slot-content">
                      <div class="slot-time">{{ slot.startTime }} - {{ slot.endTime }}</div>
                      <div class="slot-status">{{ slot.statusText }}</div>
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

      <el-dialog v-model="addDialogVisible" title="新增自习室" width="520px" destroy-on-close @closed="resetAddForm">
        <el-form ref="addFormRef" :model="addForm" :rules="addFormRules" label-width="110px">
          <el-form-item label="自习室编号" prop="roomNumber">
            <el-input v-model="addForm.roomNumber" maxlength="20" show-word-limit placeholder="如 SR006" />
          </el-form-item>
          <el-form-item label="自习室名称" prop="roomName">
            <el-input v-model="addForm.roomName" maxlength="100" show-word-limit placeholder="自习室名称" />
          </el-form-item>
          <el-form-item label="容量（座位数）" prop="capacity">
            <el-input-number v-model="addForm.capacity" :min="1" :max="500" :step="1" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="位置" prop="location">
            <el-input v-model="addForm.location" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="选填" />
          </el-form-item>
          <el-form-item label="描述" prop="description">
            <el-input v-model="addForm.description" type="textarea" :rows="3" placeholder="选填" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="addSubmitting" @click="submitAddRoom">确定</el-button>
        </template>
      </el-dialog>

      <div class="pagination">
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :total="studyRoomList.length"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import request from '@/utils/request'
import { getStudyRoomList, getStudyRoomsByStatus, createAdminStudyRoom } from '@/api/studyroom'
import { getSeatsByRoomId, getSeatUsageTimeline } from '@/api/seat'

const route = useRoute()
const isAdmin = computed(() => route.name === 'AdminStudyRoomManagement')

const loading = ref(false)
const pageSize = 10
const currentPage = ref(1)
const studyRoomList = ref([])
const filterStatus = ref('')
const detailVisible = ref(false)
const currentRoom = ref(null)
const seatList = ref([])
const activeLeases = ref([]) // 当前日期在使用中的长期租赁
const seatsLoading = ref(false)
const timelineDialogVisible = ref(false)
const currentSeat = ref(null)
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const timeline = ref([])
const timelineLoading = ref(false)

const addDialogVisible = ref(false)
const addFormRef = ref(null)
const addSubmitting = ref(false)
const addForm = ref({
  roomNumber: '',
  roomName: '',
  capacity: 20,
  location: '',
  description: ''
})
const addFormRules = {
  roomNumber: [{ required: true, message: '请输入自习室编号', trigger: 'blur' }],
  roomName: [{ required: true, message: '请输入自习室名称', trigger: 'blur' }],
  capacity: [{ required: true, message: '请设置容量', trigger: 'change' }]
}

const openAddDialog = () => {
  resetAddForm()
  addDialogVisible.value = true
}

const resetAddForm = () => {
  addForm.value = {
    roomNumber: '',
    roomName: '',
    capacity: 20,
    location: '',
    description: ''
  }
  addFormRef.value?.resetFields?.()
}

const submitAddRoom = async () => {
  if (!addFormRef.value) return
  try {
    await addFormRef.value.validate()
  } catch {
    return
  }
  addSubmitting.value = true
  try {
    const payload = {
      roomNumber: addForm.value.roomNumber.trim(),
      roomName: addForm.value.roomName.trim(),
      capacity: addForm.value.capacity,
      location: addForm.value.location?.trim() || undefined,
      description: addForm.value.description?.trim() || undefined
    }
    const res = await createAdminStudyRoom(payload)
    if (res.code === 200) {
      ElMessage.success(res.message || '新增成功')
      addDialogVisible.value = false
      await loadStudyRooms()
    }
  } catch (e) {
    /* 错误信息由 request 拦截器提示 */
  } finally {
    addSubmitting.value = false
  }
}

const getStatusText = (status) => {
  const statusMap = {
    0: '有座',
    1: '无座'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status) => {
  const typeMap = {
    0: 'success',
    1: 'danger'
  }
  return typeMap[status] || 'info'
}

// 检查座位是否有当前日期在使用中的长期租赁
const isSeatInActiveLease = (seat) => {
  if (!seat || !seat.id) return false
  const today = dayjs().format('YYYY-MM-DD')
  return activeLeases.value.some(lease => {
    if (lease.seatId !== seat.id) return false
    // 检查当前日期是否在开始和结束日期之间
    const startDate = dayjs(lease.startDate).format('YYYY-MM-DD')
    const endDate = dayjs(lease.endDate).format('YYYY-MM-DD')
    return today >= startDate && today <= endDate
  })
}

const getSeatStatusText = (seat) => {
  if (!seat) return '未知'
  
  // 如果座位状态是2（被长期租赁），但当前日期不在使用中，显示为"空闲"
  if (seat.status === 2) {
    if (isSeatInActiveLease(seat)) {
      return '被长期租赁'
    } else {
      return '空闲' // 已付款待使用，还未开始
    }
  }
  
  const statusMap = {
    0: '空闲',
    1: '已预约',
    2: '被长期租赁',
    3: '维护中',
    4: '已锁定'
  }
  return statusMap[seat.status] || '未知'
}

const getSeatClass = (seat) => {
  if (!seat) return 'seat-unknown'
  
  // 如果座位状态是2（被长期租赁），但当前日期不在使用中，显示为"空闲"
  if (seat.status === 2) {
    if (isSeatInActiveLease(seat)) {
      return 'seat-leased'
    } else {
      return 'seat-available' // 已付款待使用，还未开始
    }
  }
  
  const classMap = {
    0: 'seat-available',
    1: 'seat-reserved',
    2: 'seat-leased',
    3: 'seat-maintenance',
    4: 'seat-locked'
  }
  return classMap[seat.status] || 'seat-unknown'
}

const loadStudyRooms = async () => {
  loading.value = true
  try {
    let res
    if (filterStatus.value === '') {
      res = await getStudyRoomList()
    } else {
      res = await getStudyRoomsByStatus(filterStatus.value)
    }
    
    if (res.code === 200) {
      studyRoomList.value = res.data
      currentPage.value = 1
    }
  } catch (error) {
    ElMessage.error('加载自习室列表失败')
  } finally {
    loading.value = false
  }
}

const pagedStudyRooms = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return studyRoomList.value.slice(start, start + pageSize)
})

const handlePageChange = (page) => {
  currentPage.value = page
}

const viewDetail = async (room) => {
  currentRoom.value = room
  detailVisible.value = true
  // 加载座位列表和当前使用中的长期租赁
  await Promise.all([
    loadSeats(room.id),
    loadActiveLeases()
  ])
}

const loadSeats = async (roomId) => {
  if (!roomId) return
  seatsLoading.value = true
  try {
    const res = await getSeatsByRoomId(roomId)
    if (res.code === 200) {
      seatList.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载座位列表失败')
  } finally {
    seatsLoading.value = false
  }
}

// 加载当前日期在使用中的长期租赁
const loadActiveLeases = async () => {
  try {
    const res = await request({
      url: '/lease/active/current',
      method: 'get'
    })
    if (res.code === 200 && res.data) {
      activeLeases.value = res.data || []
    } else {
      activeLeases.value = []
    }
  } catch (error) {
    console.error('加载长期租赁列表失败:', error)
    activeLeases.value = []
  }
}

// 监听对话框关闭，清空座位列表
watch(detailVisible, (newVal) => {
  if (!newVal) {
    seatList.value = []
  }
})

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
  const totalMinutes = 24 * 60 // 一天总分钟数
  const left = (startMinutes / totalMinutes) * 100
  const width = Math.max(((endMinutes - startMinutes) / totalMinutes) * 100, 0.5) // 最小宽度0.5%
  
  return {
    left: `${left}%`,
    width: `${width}%`,
    minWidth: width < 2 ? '40px' : 'auto' // 如果宽度太小，设置最小宽度
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

onMounted(() => {
  loadStudyRooms()
})
</script>

<style scoped>
.studyroom-list {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #303133;
}

.header-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0;
}

/* 座位图样式 */
.seat-map-container {
  margin-top: 20px;
}

.seat-map-header {
  margin-bottom: 15px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.legend {
  display: flex;
  gap: 20px;
  justify-content: center;
  flex-wrap: wrap;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  padding: 5px 10px;
  border-radius: 4px;
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.legend-color.available {
  background-color: #67c23a;
}

.legend-color.reserved {
  background-color: #e6a23c;
}

.legend-color.leased {
  /* 被长期租赁：蓝色 */
  background-color: #409eff;
}

.legend-color.maintenance {
  background-color: #909399;
}

.legend-color.locked {
  /* 已锁定：红色 */
  background-color: #f56c6c;
}

.seat-map {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 4px;
  max-height: 500px;
  overflow-y: auto;
}

.seat-item {
  aspect-ratio: 1;
  border: 1px solid #ddd;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 8px;
  font-weight: 500;
}

.seat-item:hover {
  border-color: #409EFF;
}

.seat-item.seat-available {
  background-color: #67c23a;
  border-color: #67c23a;
  color: #fff;
}

.seat-item.seat-reserved {
  background-color: #e6a23c;
  border-color: #e6a23c;
  color: #fff;
}

.seat-item.seat-leased {
  /* 被长期租赁：蓝色 */
  background-color: #409eff;
  border-color: #409eff;
  color: #fff;
}

.seat-item.seat-maintenance {
  background-color: #909399;
  border-color: #909399;
  color: #fff;
}

.seat-item.seat-locked {
  /* 已锁定：红色 */
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: #fff;
}

.seat-number {
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 4px;
}

.seat-status {
  font-size: 12px;
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
</style>

