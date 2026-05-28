<template>
  <div class="points-exchange">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>积分兑换预约</span>
          <el-button @click="$router.push('/user/points')">返回积分中心</el-button>
        </div>
      </template>

      <!-- 积分信息卡片 -->
      <el-alert
        :title="`当前积分：${pointsInfo.totalPoints}，可兑换时长：${pointsInfo.exchangeableHours} 小时`"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <!-- 长期租赁冲突提示 -->
      <el-alert
        v-if="activeLease"
        :title="`你已经长期租赁了 ${activeLease.roomName || '未知区域'} ${activeLease.seatNumber || '未知'}号座位，不能再进行预约！`"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />

      <el-form :model="reservationForm" :rules="rules" ref="reservationFormRef" label-width="120px">
        <el-form-item label="选择自习室" prop="roomId">
          <el-select 
            v-model="reservationForm.roomId" 
            placeholder="请选择自习室"
            style="width: 100%"
            filterable
            @change="handleRoomChange"
          >
            <el-option
              v-for="room in availableRooms"
              :key="room.id"
              :label="`${room.roomNumber} - ${room.roomName}`"
              :value="room.id"
            >
              <span style="float: left">{{ room.roomNumber }} - {{ room.roomName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                容量: {{ room.capacity }}座
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="reservationForm.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
            @change="syncStart"
          />
        </el-form-item>

        <el-form-item label="开始时间" prop="startClock">
          <el-time-select
            v-model="reservationForm.startClock"
            placeholder="选择开始时间"
            style="width: 100%"
            start="00:00"
            end="23:45"
            step="00:15"
            :clearable="false"
            @change="syncStart"
          />
        </el-form-item>

        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="reservationForm.endDate"
            type="date"
            placeholder="选择结束日期"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
            @change="syncEnd"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endClock">
          <el-time-select
            v-model="reservationForm.endClock"
            placeholder="选择结束时间"
            style="width: 100%"
            start="00:00"
            end="23:45"
            step="00:15"
            :clearable="false"
            @change="syncEnd"
          />
        </el-form-item>

        <el-form-item label="选择座位" prop="seatId" v-if="reservationForm.roomId && reservationForm.startTime && reservationForm.endTime">
          <el-select 
            v-model="reservationForm.seatId" 
            :placeholder="seatPlaceholder"
            style="width: 100%"
            filterable
            :loading="seatsLoading"
            @change="handleSeatChange"
          >
            <el-option
              v-for="seat in availableSeats"
              :key="seat.id"
              :label="`${seat.seatNumber} - ${seat.seatName || seat.seatNumber}`"
              :value="seat.id"
            >
              <span style="float: left">{{ seat.seatNumber }} - {{ seat.seatName || seat.seatNumber }}</span>
              <span style="float: right; color: #67c23a; font-size: 13px">
                可用
              </span>
            </el-option>
          </el-select>
          <div v-if="availableSeats.length === 0 && !seatsLoading && reservationForm.startTime && reservationForm.endTime" 
               style="color: #909399; font-size: 12px; margin-top: 5px">
            该时间段内暂无可用座位，请选择其他时间段
          </div>
        </el-form-item>

        <el-form-item v-if="selectedRoom">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="自习室编号">{{ selectedRoom.roomNumber }}</el-descriptions-item>
            <el-descriptions-item label="自习室名称">{{ selectedRoom.roomName }}</el-descriptions-item>
            <el-descriptions-item label="位置">{{ selectedRoom.location }}</el-descriptions-item>
            <el-descriptions-item label="已选座位" v-if="selectedSeat">
              {{ selectedSeat.seatNumber }} - {{ selectedSeat.seatName || selectedSeat.seatNumber }}
            </el-descriptions-item>
            <el-descriptions-item label="预约时长">
              <span style="color: #409eff; font-size: 18px; font-weight: bold">
                {{ calculatedHours }} 小时
              </span>
              <span v-if="hoursError" style="color: #f56c6c; font-size: 12px; margin-left: 10px">
                {{ hoursError }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="所需积分">
              <span 
                v-if="requiredPoints === null"
                style="color: #909399; font-size: 18px; font-weight: bold"
              >
                请选择预约时间
              </span>
              <template v-else>
                <span :style="{ color: canExchange ? '#67c23a' : '#f56c6c', fontSize: '18px', fontWeight: 'bold' }">
                  {{ requiredPoints }} 积分
                </span>
                <span style="color: #909399; font-size: 12px; margin-left: 10px">
                  (30积分=2小时)
                </span>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="兑换后剩余积分" v-if="requiredPoints !== null">
              <span :style="{ color: canExchange ? '#67c23a' : '#f56c6c', fontSize: '16px', fontWeight: 'bold' }">
                {{ remainingPoints }} 积分
              </span>
            </el-descriptions-item>
          </el-descriptions>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading" 
            :disabled="!!activeLease || !canExchange || requiredPoints === null"
            @click="handleSubmit" 
            style="width: 100%"
          >
            {{ activeLease ? '已有长期租赁，无法预约' : (!canExchange ? '积分不足，无法兑换' : '确认兑换') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import customParseFormat from 'dayjs/plugin/customParseFormat'

dayjs.extend(customParseFormat)
import { getStudyRoomList } from '@/api/studyroom'
import { getAvailableSeats } from '@/api/seat'
import { createReservationWithPoints } from '@/api/reservation'
import { checkActiveLease, checkActiveLeaseByTime } from '@/api/lease'
import { getPointsInfo, calculateExchangePoints } from '@/api/points'

const router = useRouter()

const reservationFormRef = ref(null)
const loading = ref(false)
const availableRooms = ref([])
const selectedRoom = ref(null)
const availableSeats = ref([])
const selectedSeat = ref(null)
const seatsLoading = ref(false)
const activeLease = ref(null)
const pointsInfo = ref({
  totalPoints: 0,
  exchangeableHours: 0
})
const requiredPoints = ref(null)
const canExchange = ref(false)
const hoursError = ref('')

const reservationForm = reactive({
  roomId: null,
  seatId: null,
  startDate: '',
  startClock: '',
  endDate: '',
  endClock: '',
  startTime: '',
  endTime: ''
})

const rules = {
  roomId: [{ required: true, message: '请选择自习室', trigger: 'change' }],
  seatId: [{ required: true, message: '请选择座位', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  startClock: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  endClock: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const calculatedHours = computed(() => {
  if (!reservationForm.startTime || !reservationForm.endTime) {
    return 0
  }
  const start = dayjs(reservationForm.startTime)
  const end = dayjs(reservationForm.endTime)
  
  if (start.isAfter(end)) {
    return 0
  }
  
  const hours = end.diff(start, 'hour', true)
  return Math.floor(hours * 100) / 100 // 保留两位小数
})

const remainingPoints = computed(() => {
  if (requiredPoints.value === null) {
    return pointsInfo.value.totalPoints
  }
  return Math.max(0, pointsInfo.value.totalPoints - requiredPoints.value)
})

const disabledDate = (time) => {
  // 只能预约今天及以后的时间
  return time.getTime() < Date.now() - 8.64e7
}

const syncStart = async () => {
  if (reservationForm.startDate && reservationForm.startClock) {
    reservationForm.startTime = `${reservationForm.startDate} ${reservationForm.startClock}:00`
  } else {
    reservationForm.startTime = ''
  }
  calculateRequiredPoints()
  // 时间变化时，如果已选择自习室，重新筛选座位
  if (reservationForm.roomId && reservationForm.startTime && reservationForm.endTime) {
    loadAvailableSeats()
  } else {
    reservationForm.seatId = null
    selectedSeat.value = null
    availableSeats.value = []
  }
}

const syncEnd = () => {
  if (reservationForm.endDate && reservationForm.endClock) {
    reservationForm.endTime = `${reservationForm.endDate} ${reservationForm.endClock}:00`
  } else {
    reservationForm.endTime = ''
  }
  calculateRequiredPoints()
  // 时间变化时，如果已选择自习室，重新筛选座位
  if (reservationForm.roomId && reservationForm.startTime && reservationForm.endTime) {
    loadAvailableSeats()
  } else {
    reservationForm.seatId = null
    selectedSeat.value = null
    availableSeats.value = []
  }
}

const calculateRequiredPoints = async () => {
  if (!reservationForm.startTime || !reservationForm.endTime) {
    requiredPoints.value = null
    canExchange.value = false
    hoursError.value = ''
    activeLease.value = null
    return
  }

  const start = dayjs(reservationForm.startTime)
  const end = dayjs(reservationForm.endTime)
  
  if (start.isAfter(end)) {
    requiredPoints.value = null
    canExchange.value = false
    hoursError.value = '结束时间必须晚于开始时间'
    activeLease.value = null
    return
  }
  
  if (start.isBefore(dayjs())) {
    requiredPoints.value = null
    canExchange.value = false
    hoursError.value = '开始时间不能早于当前时间'
    activeLease.value = null
    return
  }
  
  // 检查所选时间段内是否有长期租赁冲突
  await checkLeaseConflict()
  
  // 如果有长期租赁冲突，直接返回
  if (activeLease.value) {
    requiredPoints.value = null
    canExchange.value = false
    hoursError.value = ''
    return
  }
  
  // 计算总分钟数
  const totalMinutes = end.diff(start, 'minute')
  
  if (totalMinutes <= 0) {
    requiredPoints.value = null
    canExchange.value = false
    hoursError.value = '预约时长必须大于0'
    return
  }
  
  // 检查是否为2小时的整数倍（120分钟）
  if (totalMinutes % 120 !== 0) {
    requiredPoints.value = null
    canExchange.value = false
    hoursError.value = '必须是两个小时的整数倍'
    return
  }
  
  hoursError.value = ''
  
  // 计算2小时的倍数
  const twoHourMultiples = totalMinutes / 120
  
  // 计算所需积分：30积分 * 倍数
  const points = twoHourMultiples * 30
  requiredPoints.value = points
  
  // 检查积分是否足够
  canExchange.value = pointsInfo.value.totalPoints >= points
}

const handleRoomChange = async (roomId) => {
  selectedRoom.value = availableRooms.value.find(r => r.id === roomId)
  reservationForm.seatId = null
  selectedSeat.value = null
  availableSeats.value = []
  
  if (roomId && reservationForm.startTime && reservationForm.endTime) {
    await loadAvailableSeats()
  }
}

const handleSeatChange = (seatId) => {
  selectedSeat.value = availableSeats.value.find(s => s.id === seatId)
}

const loadAvailableSeats = async () => {
  if (!reservationForm.roomId || !reservationForm.startTime || !reservationForm.endTime) {
    return
  }
  
  const start = dayjs(reservationForm.startTime)
  const end = dayjs(reservationForm.endTime)
  
  if (start.isAfter(end)) {
    availableSeats.value = []
    reservationForm.seatId = null
    selectedSeat.value = null
    return
  }
  
  if (start.isBefore(dayjs())) {
    availableSeats.value = []
    reservationForm.seatId = null
    selectedSeat.value = null
    return
  }
  
  seatsLoading.value = true
  try {
    const res = await getAvailableSeats(
      reservationForm.roomId,
      reservationForm.startTime,
      reservationForm.endTime
    )
    if (res.code === 200) {
      availableSeats.value = res.data || []
      if (reservationForm.seatId) {
        const seatExists = (res.data || []).some(s => s.id === reservationForm.seatId)
        if (!seatExists) {
          reservationForm.seatId = null
          selectedSeat.value = null
        }
      }
    } else {
      ElMessage.warning(res.message || '查询可用座位失败')
      availableSeats.value = []
    }
  } catch (error) {
    ElMessage.error('加载可用座位失败：' + (error.message || '未知错误'))
    availableSeats.value = []
  } finally {
    seatsLoading.value = false
  }
}

const seatPlaceholder = computed(() => {
  if (!reservationForm.startTime || !reservationForm.endTime) {
    return '请先选择预约时间'
  }
  if (seatsLoading.value) {
    return '正在筛选可用座位...'
  }
  if (availableSeats.value.length === 0) {
    return '该时间段内暂无可用座位'
  }
  return `共找到 ${availableSeats.value.length} 个可用座位，请选择`
})

const loadStudyRooms = async () => {
  try {
    const res = await getStudyRoomList()
    if (res.code === 200) {
      availableRooms.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载自习室列表失败')
  }
}

const loadPointsInfo = async () => {
  try {
    const res = await getPointsInfo()
    if (res.code === 200 && res.data) {
      pointsInfo.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载积分信息失败')
  }
}

const handleSubmit = async () => {
  if (!reservationFormRef.value) return
  
  if (activeLease.value) {
    ElMessage.warning(`你已经长期租赁了 ${activeLease.value.roomName || '未知区域'} ${activeLease.value.seatNumber || '未知'}号座位，不能再进行预约！`)
    return
  }
  
  if (!canExchange.value || requiredPoints.value === null) {
    ElMessage.warning('积分不足或预约时长不符合要求')
    return
  }
  
  await reservationFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await createReservationWithPoints({
          roomId: reservationForm.roomId,
          seatId: reservationForm.seatId,
          startTime: reservationForm.startTime,
          endTime: reservationForm.endTime
        })
        
        if (res.code === 200) {
          ElMessage.success('积分兑换预约成功')
          await loadPointsInfo() // 刷新积分信息
          router.push('/user/reservation')
        }
      } catch (error) {
        ElMessage.error(error.message || '积分兑换预约失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 检查所选时间段内是否有长期租赁冲突
const checkLeaseConflict = async () => {
  if (!reservationForm.startTime || !reservationForm.endTime) {
    activeLease.value = null
    return
  }
  
  try {
    // 格式化时间为后端需要的格式
    const startTime = dayjs(reservationForm.startTime).format('YYYY-MM-DD HH:mm:ss')
    const endTime = dayjs(reservationForm.endTime).format('YYYY-MM-DD HH:mm:ss')
    
    const res = await checkActiveLeaseByTime(startTime, endTime)
    if (res.code === 200) {
      activeLease.value = res.data
    } else {
      activeLease.value = null
    }
  } catch (error) {
    // 静默失败，不影响页面加载
    console.error('检查长期租赁冲突失败:', error)
    activeLease.value = null
  }
}

// 加载当前日期是否有长期租赁（用于页面初始化时的提示）
const loadActiveLease = async () => {
  try {
    const res = await checkActiveLease()
    if (res.code === 200) {
      // 只在用户未选择时间段时显示，如果已选择时间段则使用 checkLeaseConflict 的结果
      if (!reservationForm.startTime || !reservationForm.endTime) {
        activeLease.value = res.data
      }
    }
  } catch (error) {
    // 静默失败，不影响页面加载
    console.error('检查长期租赁状态失败:', error)
  }
}

onMounted(() => {
  loadStudyRooms()
  loadPointsInfo()
  loadActiveLease()
  const today = dayjs().format('YYYY-MM-DD')
  reservationForm.startDate = today
  reservationForm.endDate = today
  reservationForm.startClock = '00:00'
  reservationForm.endClock = '02:00' // 默认2小时
  syncStart()
  syncEnd()
})
</script>

<style scoped>
.points-exchange {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

:deep(.el-card) {
  border-radius: 4px;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-date-editor) {
  width: 100%;
  border-radius: 8px;
}

:deep(.el-date-editor .el-input__wrapper) {
  border-radius: 8px;
}
</style>

