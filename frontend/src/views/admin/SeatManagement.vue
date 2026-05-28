<template>
  <div class="seat-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>座位管理</span>
        </div>
      </template>

      <!-- 大区列表 -->
      <div class="room-list">
        <div 
          v-for="room in roomList" 
          :key="room.id" 
          class="room-item"
        >
          <div class="room-info">
            <div class="room-number">{{ room.roomNumber }}</div>
            <div class="room-details">
              <div class="room-name">{{ room.roomName }}</div>
              <div class="room-location">{{ room.location }}</div>
            </div>
          </div>
          <el-button 
            type="primary" 
            @click="goToRoomDetail(room.id)"
            class="manage-btn"
          >
            进入管理
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty 
        v-if="roomList.length === 0 && !loading" 
        description="暂无大区数据"
        :image-size="100"
      />
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

    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getStudyRoomList } from '@/api/studyroom'

const router = useRouter()
const loading = ref(false)
const roomList = ref([])

const loadRooms = async () => {
  loading.value = true
  try {
    const res = await getStudyRoomList()
    if (res.code === 200) {
      roomList.value = res.data || []
    }
  } catch (error) {
    console.error('加载大区列表失败:', error)
  } finally {
    loading.value = false
  }
}

const goToRoomDetail = (roomId) => {
  router.push({
    name: 'AdminSeatDetailManagement',
    params: { roomId }
  })
}

onMounted(() => {
  loadRooms()
})
</script>

<style scoped>
.seat-management {
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

.room-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.room-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s;
  cursor: pointer;
}

.room-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
}

.room-info {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
}

.room-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  min-width: 80px;
}

.room-details {
  flex: 1;
}

.room-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.room-location {
  font-size: 14px;
  color: #909399;
}

.manage-btn {
  min-width: 100px;
}

:deep(.el-card) {
  border-radius: 4px;
}
</style>

