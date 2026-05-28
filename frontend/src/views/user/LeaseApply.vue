<template>
  <div class="lease-apply">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>长期租赁申请</span>
        </div>
      </template>

      <el-alert
        title="长期租赁说明"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      >
        <template #default>
          <p>1. 长期租赁期间，该自习室将不会被其他用户预约</p>
          <p>2. 申请提交后系统将自动审核通过</p>
          <p>3. 审核通过后需要在2小时内完成付款</p>
          <p>4. 长期租赁按天计费</p>
          <p style="color: #e6a23c; font-weight: 600; margin-top: 8px;">请至少提前一天申请长期租赁</p>
        </template>
      </el-alert>

      <el-form
        :model="leaseForm"
        :rules="rules"
        ref="leaseFormRef"
        :label-width="isMobile ? 'auto' : '120px'"
        :label-position="isMobile ? 'top' : 'right'"
      >
        <el-form-item label="选择自习室" prop="roomId">
          <el-select 
            v-model="leaseForm.roomId" 
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
            />
          </el-select>
        </el-form-item>

        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="leaseForm.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
            @change="handleDateChange"
          />
        </el-form-item>

        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="leaseForm.endDate"
            type="date"
            placeholder="选择结束日期"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledEndDate"
            @change="handleDateChange"
          />
        </el-form-item>

        <el-form-item label="选择座位" prop="seatId" v-if="leaseForm.roomId && leaseForm.startDate && leaseForm.endDate">
          <el-select 
            v-model="leaseForm.seatId" 
            placeholder="请先选择自习室和时间段"
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
            </el-option>
          </el-select>
          <div v-if="availableSeats.length === 0 && !seatsLoading && leaseForm.startDate && leaseForm.endDate" style="color: #909399; font-size: 12px; margin-top: 5px">
            该日期范围内暂无可用座位
          </div>
        </el-form-item>

        <el-form-item label="申请理由" prop="applyReason">
          <el-input
            v-model="leaseForm.applyReason"
            type="textarea"
            :rows="4"
            placeholder="请填写申请长期租赁的理由"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item v-if="selectedRoom && leaseForm.startDate && leaseForm.endDate">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="自习室编号">{{ selectedRoom.roomNumber }}</el-descriptions-item>
            <el-descriptions-item label="自习室名称">{{ selectedRoom.roomName }}</el-descriptions-item>
            <el-descriptions-item label="位置">{{ selectedRoom.location }}</el-descriptions-item>
            <el-descriptions-item label="已选座位" v-if="selectedSeat">
              {{ selectedSeat.seatNumber }} - {{ selectedSeat.seatName || selectedSeat.seatNumber }}
            </el-descriptions-item>
            <el-descriptions-item label="开始日期">
              {{ leaseForm.startDate }}
            </el-descriptions-item>
            <el-descriptions-item label="结束日期">
              {{ leaseForm.endDate }}
            </el-descriptions-item>
            <el-descriptions-item label="租赁天数">
              <span style="font-weight: bold">{{ calculatedDays }} 天</span>
            </el-descriptions-item>
            <el-descriptions-item label="预计金额">
              <span style="color: #f56c6c; font-size: 18px; font-weight: bold">
                ¥{{ calculatedAmount }}
              </span>
              <span style="color: #909399; font-size: 12px; margin-left: 10px">
                ({{ pricePerDay }}元/天)
              </span>
            </el-descriptions-item>
          </el-descriptions>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleSubmit" style="width: 100%">
            提交申请
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getStudyRoomList } from '@/api/studyroom'
import { getAvailableSeats } from '@/api/seat'
import { applyLease } from '@/api/lease'
import { getLeasePrice } from '@/api/priceConfig'

const router = useRouter()

const leaseFormRef = ref(null)
const loading = ref(false)
const availableRooms = ref([])
const selectedRoom = ref(null)
const availableSeats = ref([])
const selectedSeat = ref(null)
const seatsLoading = ref(false)
const pricePerDay = ref(30) // 长期租赁每天价格，默认30元

const isMobile = ref(false)
const updateIsMobile = () => {
  isMobile.value = window.matchMedia('(max-width: 768px)').matches
}

const leaseForm = reactive({
  roomId: null,
  seatId: null,
  startDate: '',
  endDate: '',
  applyReason: ''
})

const rules = {
  roomId: [
    { required: true, message: '请选择自习室', trigger: 'change' }
  ],
  seatId: [
    { required: true, message: '请选择座位', trigger: 'change' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ],
  applyReason: [
    { required: true, message: '请填写申请理由', trigger: 'blur' },
    { min: 10, message: '申请理由至少10个字符', trigger: 'blur' }
  ]
}

// 计算开始时间和结束时间（用于筛选可用座位，使用当天的开始和结束时间）
const leaseFormStartTime = computed(() => {
  if (!leaseForm.startDate) {
    return null
  }
  return dayjs(leaseForm.startDate).startOf('day').format('YYYY-MM-DD HH:mm:ss')
})

const leaseFormEndTime = computed(() => {
  if (!leaseForm.endDate) {
    return null
  }
  return dayjs(leaseForm.endDate).endOf('day').format('YYYY-MM-DD HH:mm:ss')
})

const calculatedDays = computed(() => {
  if (!leaseForm.startDate || !leaseForm.endDate) {
    return 0
  }
  const start = dayjs(leaseForm.startDate)
  const end = dayjs(leaseForm.endDate)
  return end.diff(start, 'day') + 1
})

const calculatedAmount = computed(() => {
  return (calculatedDays.value * pricePerDay.value).toFixed(2)
})

const disabledDate = (time) => {
  // 开始日期必须晚于今日，即今日日期不可选
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return time.getTime() <= today.getTime()
}

const disabledEndDate = (time) => {
  // 结束日期必须晚于或等于开始日期
  if (leaseForm.startDate) {
    const start = dayjs(leaseForm.startDate)
    const end = dayjs(time)
    return end.isBefore(start)
  }
  // 结束日期必须晚于今日（即今日日期不可选）
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return time.getTime() <= today.getTime()
}

const getSeatStatusText = (status) => {
  const statusMap = {
    0: '空闲',
    1: '已被预约',
    2: '被长期租赁',
    3: '维护中',
    4: '已锁定'
  }
  return statusMap[status] || '未知'
}

const handleRoomChange = async (roomId) => {
  selectedRoom.value = availableRooms.value.find(r => r.id === roomId)
  // 重置座位选择
  leaseForm.seatId = null
  selectedSeat.value = null
  availableSeats.value = []
  
  // 如果已选择日期范围，重新加载可用座位
  if (roomId && leaseForm.startDate && leaseForm.endDate) {
    await loadAvailableSeats()
  }
}

const handleSeatChange = (seatId) => {
  selectedSeat.value = availableSeats.value.find(s => s.id === seatId)
}

// 日期变化时重新加载可用座位
const handleDateChange = async () => {
  // 重置座位选择
  leaseForm.seatId = null
  selectedSeat.value = null
  availableSeats.value = []
  
  // 如果已选择自习室和日期范围，加载可用座位
  if (leaseForm.roomId && leaseForm.startDate && leaseForm.endDate) {
    await loadAvailableSeats()
  }
}

// 根据日期范围加载可用座位（检查整个日期范围内是否有冲突）
const loadAvailableSeats = async () => {
  if (!leaseForm.roomId || !leaseForm.startDate || !leaseForm.endDate) {
    return
  }
  
  // 验证日期有效性
  const start = dayjs(leaseForm.startDate)
  const end = dayjs(leaseForm.endDate)
  
  if (start.isAfter(end)) {
    availableSeats.value = []
    leaseForm.seatId = null
    selectedSeat.value = null
    return
  }
  
  // 开始日期必须晚于今天（即今天不可选，至少是明天）
  if (start.isBefore(dayjs().add(1, 'day').startOf('day'))) {
    availableSeats.value = []
    leaseForm.seatId = null
    selectedSeat.value = null
    return
  }
  
  seatsLoading.value = true
  try {
    // 使用开始日期的00:00:00和结束日期的23:59:59来检查整个日期范围
    const res = await getAvailableSeats(
      leaseForm.roomId,
      leaseFormStartTime.value,
      leaseFormEndTime.value
    )
    if (res.code === 200) {
      availableSeats.value = res.data || []
      // 如果当前选中的座位不在可用列表中，清空选择
      if (leaseForm.seatId) {
        const seatExists = (res.data || []).some(s => s.id === leaseForm.seatId)
        if (!seatExists) {
          leaseForm.seatId = null
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

const handleSubmit = async () => {
  if (!leaseFormRef.value) return
  
  await leaseFormRef.value.validate(async (valid) => {
    if (valid) {
      // 验证日期
      if (!leaseForm.startDate || !leaseForm.endDate) {
        ElMessage.error('请选择完整的日期范围')
        return
      }
      
      const start = dayjs(leaseForm.startDate)
      const end = dayjs(leaseForm.endDate)
      
      if (start.isAfter(end)) {
        ElMessage.error('开始日期不能晚于结束日期')
        return
      }
      
      // 开始日期必须晚于今天（即今天不可选，至少是明天）
      if (start.isBefore(dayjs().add(1, 'day').startOf('day'))) {
        ElMessage.error('开始日期必须晚于今天，请至少提前一天申请')
        return
      }
      
      loading.value = true
      try {
        // 构建提交数据（使用开始日期的00:00:00和结束日期的23:59:59）
        const submitData = {
          roomId: leaseForm.roomId,
          seatId: leaseForm.seatId,
          startTime: leaseFormStartTime.value,
          endTime: leaseFormEndTime.value,
          applyReason: leaseForm.applyReason
        }
        
        const res = await applyLease(submitData)
        
        if (res.code === 200) {
          ElMessage.success('申请提交成功，系统已自动审核通过，请在2小时内完成付款')
          router.push('/user/lease')
        }
      } catch (error) {
        ElMessage.error(error.message || '提交申请失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 加载价格配置
const loadPriceConfig = async () => {
  try {
    const res = await getLeasePrice()
    if (res.code === 200 && res.data) {
      pricePerDay.value = parseFloat(res.data.pricePerDay) || 30
    }
  } catch (error) {
    console.error('加载价格配置失败:', error)
    // 失败时使用默认值30元
    pricePerDay.value = 30
  }
}

onMounted(() => {
  updateIsMobile()
  window.addEventListener('resize', updateIsMobile)
  loadPriceConfig() // 加载价格配置
  loadStudyRooms()
})

onUnmounted(() => {
  window.removeEventListener('resize', updateIsMobile)
})
</script>

<style scoped>
.lease-apply {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

@media (max-width: 768px) {
  .lease-apply {
    padding: 12px;
  }

  /* 表单项在移动端更紧凑，避免横向挤压导致错位 */
  :deep(.el-form-item__label) {
    padding: 0 0 6px 0;
    line-height: 1.2;
  }

  :deep(.el-descriptions__label),
  :deep(.el-descriptions__content) {
    word-break: break-word;
  }
}
</style>

