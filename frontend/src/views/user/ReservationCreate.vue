<template>
  <div class="reservation-create">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预约自习室</span>
        </div>
      </template>

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
            <el-descriptions-item label="预计金额">
              <span 
                v-if="calculatedAmount.amount === 'error'"
                style="color: #f56c6c; font-size: 18px; font-weight: bold"
              >
                error
              </span>
              <template v-else>
                <div style="display: flex; flex-direction: column; gap: 5px">
                  <div>
                    <span style="color: #f56c6c; font-size: 18px; font-weight: bold">
                      ¥{{ calculatedAmount.amount }}
                    </span>
                    <span v-if="calculatedAmount.discount < 1" style="color: #67c23a; font-size: 12px; margin-left: 8px; font-weight: 600">
                      ({{ getMemberTypeText }})
                    </span>
                  </div>
                  <div v-if="calculatedAmount.discount < 1" style="font-size: 12px; color: #909399">
                    <span style="text-decoration: line-through">原价：¥{{ calculatedAmount.originalAmount }}</span>
                    <span style="margin-left: 8px; color: #67c23a">节省：¥{{ (parseFloat(calculatedAmount.originalAmount) - parseFloat(calculatedAmount.amount)).toFixed(2) }}</span>
                  </div>
                  <div style="font-size: 12px; color: #909399">
                    ({{ pricePerHour }}元/小时{{ calculatedAmount.discount < 1 && getMemberTypeText ? `，${getMemberTypeText}` : '' }})
                  </div>
                </div>
              </template>
            </el-descriptions-item>
          </el-descriptions>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading" 
            :disabled="!!activeLease"
            @click="handleSubmit" 
            style="width: 100%"
          >
            {{ activeLease ? '已有长期租赁，无法预约' : '确认预约' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import customParseFormat from 'dayjs/plugin/customParseFormat'

dayjs.extend(customParseFormat)
import { getStudyRoomList } from '@/api/studyroom'
import { getSeatsByRoomId, getAvailableSeats } from '@/api/seat'
import { createReservation } from '@/api/reservation'
import { checkActiveLeaseByTime } from '@/api/lease'
import { checkActiveLease } from '@/api/lease'
import { useUserStore } from '@/stores'
import { getCurrentUserInfo } from '@/api/user'
import { getReservationPrice, getMemberInfo } from '@/api/priceConfig'

const router = useRouter()
const userStore = useUserStore()

const reservationFormRef = ref(null)
const loading = ref(false)
const availableRooms = ref([])
const selectedRoom = ref(null)
const availableSeats = ref([])
const selectedSeat = ref(null)
const seatsLoading = ref(false)
const activeLease = ref(null) // 用户当前有效的长期租赁订单
const pricePerHour = ref(3) // 预约每小时价格，默认3元
const memberDiscounts = ref({
  vip: 0.9, // VIP折扣，默认9折
  svip: 0.8, // SVIP折扣，默认8折
  vipText: 'VIP会员9折',
  svipText: 'SVIP会员8折'
})

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

const getStatusText = (status) => {
  const statusMap = {
    0: '空闲',
    1: '已被预约',
    2: '被长期租赁',
    3: '维护中'
  }
  return statusMap[status] || '未知'
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

// 获取会员折扣率（从配置获取）
const getMemberDiscount = computed(() => {
  const userInfo = userStore.userInfo
  if (!userInfo || !userInfo.memberType || userInfo.memberType === 0) {
    return 1.0 // 普通用户无折扣
  }
  
  // 检查会员是否有效（未过期）
  if (userInfo.memberExpireTime) {
    const expireTime = dayjs(userInfo.memberExpireTime)
    if (expireTime.isBefore(dayjs())) {
      return 1.0 // 会员已过期
    }
  }
  
  if (userInfo.memberType === 1) {
    return memberDiscounts.value.vip // VIP折扣（从配置获取）
  } else if (userInfo.memberType === 2) {
    return memberDiscounts.value.svip // SVIP折扣（从配置获取）
  }
  
  return 1.0
})

// 获取会员类型文本（从配置获取）
const getMemberTypeText = computed(() => {
  const userInfo = userStore.userInfo
  if (!userInfo || !userInfo.memberType || userInfo.memberType === 0) {
    return ''
  }
  
  // 检查会员是否有效
  if (userInfo.memberExpireTime) {
    const expireTime = dayjs(userInfo.memberExpireTime)
    if (expireTime.isBefore(dayjs())) {
      return '' // 会员已过期
    }
  }
  
  if (userInfo.memberType === 1) {
    return memberDiscounts.value.vipText // VIP折扣文本（从配置获取）
  } else if (userInfo.memberType === 2) {
    return memberDiscounts.value.svipText // SVIP折扣文本（从配置获取）
  }
  
  return ''
})

const calculatedAmount = computed(() => {
  if (!reservationForm.startTime || !reservationForm.endTime) {
    return { amount: '0.00', originalAmount: '0.00', discount: 1.0 }
  }
  const start = dayjs(reservationForm.startTime)
  const end = dayjs(reservationForm.endTime)
  
  // 如果开始时间晚于结束时间，显示 error
  if (start.isAfter(end)) {
    return { amount: 'error', originalAmount: '0.00', discount: 1.0 }
  }
  
  const hours = end.diff(start, 'hour', true)
  const originalAmount = hours * pricePerHour.value
  const discount = getMemberDiscount.value
  const finalAmount = originalAmount * discount
  
  return {
    amount: finalAmount.toFixed(2),
    originalAmount: originalAmount.toFixed(2),
    discount: discount
  }
})

const disabledDate = (time) => {
  // 只能预约今天及以后的时间
  return time.getTime() < Date.now() - 8.64e7
}

const syncStart = () => {
  if (reservationForm.startDate && reservationForm.startClock) {
    reservationForm.startTime = `${reservationForm.startDate} ${reservationForm.startClock}:00`
  } else {
    reservationForm.startTime = ''
  }
  // 时间变化时，如果已选择自习室，重新筛选座位
  if (reservationForm.roomId && reservationForm.startTime && reservationForm.endTime) {
    loadAvailableSeats()
    // 检查所选时间段内是否有长期租赁冲突
    checkLeaseConflict()
  } else {
    // 如果时间不完整，清空座位选择和冲突提示
    reservationForm.seatId = null
    selectedSeat.value = null
    availableSeats.value = []
    activeLease.value = null
  }
}

const syncEnd = () => {
  if (reservationForm.endDate && reservationForm.endClock) {
    reservationForm.endTime = `${reservationForm.endDate} ${reservationForm.endClock}:00`
  } else {
    reservationForm.endTime = ''
  }
  // 时间变化时，如果已选择自习室，重新筛选座位
  if (reservationForm.roomId && reservationForm.startTime && reservationForm.endTime) {
    loadAvailableSeats()
    // 检查所选时间段内是否有长期租赁冲突
    checkLeaseConflict()
  } else {
    // 如果时间不完整，清空座位选择和冲突提示
    reservationForm.seatId = null
    selectedSeat.value = null
    availableSeats.value = []
    activeLease.value = null
  }
}

const handleRoomChange = async (roomId) => {
  selectedRoom.value = availableRooms.value.find(r => r.id === roomId)
  // 重置座位选择
  reservationForm.seatId = null
  selectedSeat.value = null
  availableSeats.value = []
  
  // 如果已选择时间，加载可用座位
  if (roomId && reservationForm.startTime && reservationForm.endTime) {
    await loadAvailableSeats()
  }
}

const handleSeatChange = (seatId) => {
  selectedSeat.value = availableSeats.value.find(s => s.id === seatId)
}

const loadSeats = async (roomId) => {
  seatsLoading.value = true
  try {
    const res = await getSeatsByRoomId(roomId)
    if (res.code === 200) {
      availableSeats.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载座位列表失败')
  } finally {
    seatsLoading.value = false
  }
}

// 根据时间段加载可用座位
const loadAvailableSeats = async () => {
  if (!reservationForm.roomId || !reservationForm.startTime || !reservationForm.endTime) {
    return
  }
  
  // 验证时间有效性
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
      // 如果当前选中的座位不在可用列表中，清空选择
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

// 座位选择框的提示文本
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

const handleSubmit = async () => {
  if (!reservationFormRef.value) return
  
  // 再次检查是否有长期租赁冲突
  if (activeLease.value) {
    ElMessage.warning(`你已经长期租赁了 ${activeLease.value.roomName || '未知区域'} ${activeLease.value.seatNumber || '未知'}号座位，不能再进行预约！`)
    return
  }
  
  await reservationFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await createReservation({
          roomId: reservationForm.roomId,
          seatId: reservationForm.seatId,
          startTime: reservationForm.startTime,
          endTime: reservationForm.endTime
        })
        
        if (res.code === 200) {
          await ElMessageBox.alert(
            '订单提交成功，请在5分钟内完成支付，若超时未支付，系统将自动取消订单。',
            '支付提示',
            {
              confirmButtonText: '知道了',
              type: 'warning'
            }
          )
          router.push('/user/reservation')
        }
      } catch (error) {
        ElMessage.error(error.message || '预约失败')
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

// 加载用户信息（用于获取会员类型）
const loadUserInfo = async () => {
  try {
    // 如果store中没有用户信息，则获取
    if (!userStore.userInfo) {
      const res = await getCurrentUserInfo()
      if (res.code === 200 && res.data) {
        userStore.setUserInfo(res.data)
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 加载价格配置
const loadPriceConfig = async () => {
  try {
    // 加载预约价格
    const priceRes = await getReservationPrice()
    if (priceRes.code === 200 && priceRes.data) {
      pricePerHour.value = parseFloat(priceRes.data.pricePerHour) || 3
    }
    
    // 加载会员折扣配置
    const memberRes = await getMemberInfo()
    if (memberRes.code === 200 && memberRes.data) {
      if (memberRes.data.vip) {
        memberDiscounts.value.vip = parseFloat(memberRes.data.vip.discount) || 0.9
        memberDiscounts.value.vipText = `VIP会员${memberRes.data.vip.discountText || '9折'}`
      }
      if (memberRes.data.svip) {
        memberDiscounts.value.svip = parseFloat(memberRes.data.svip.discount) || 0.8
        memberDiscounts.value.svipText = `SVIP会员${memberRes.data.svip.discountText || '8折'}`
      }
    }
  } catch (error) {
    console.error('加载价格配置失败:', error)
    // 失败时使用默认值
    pricePerHour.value = 3
    memberDiscounts.value.vip = 0.9
    memberDiscounts.value.svip = 0.8
    memberDiscounts.value.vipText = 'VIP会员9折'
    memberDiscounts.value.svipText = 'SVIP会员8折'
  }
}

onMounted(() => {
  loadUserInfo() // 加载用户信息以获取会员类型
  loadPriceConfig() // 加载价格配置
  loadStudyRooms()
  loadActiveLease() // 检查是否有已生效的长期租赁订单
  const today = dayjs().format('YYYY-MM-DD')
  reservationForm.startDate = today
  reservationForm.endDate = today
  reservationForm.startClock = '00:00'
  reservationForm.endClock = '00:15'
  syncStart()
  syncEnd()
})
</script>

<style scoped>
.reservation-create {
  padding: 20px;
}

.card-header {
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

