<template>
  <div class="reservation-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的预约记录</span>
          <el-button type="primary" @click="$router.push('/user/reservation/create')">新建预约</el-button>
        </div>
      </template>

      <!-- 筛选区域 -->
      <div class="filter-section">
        <el-select 
          v-model="filterRoom" 
          placeholder="筛选大区" 
          style="width: 180px; margin-right: 10px" 
          @change="handleFilterChange" 
          clearable
        >
          <el-option label="全部" value="" />
          <el-option
            v-for="room in roomOptions"
            :key="room.roomId"
            :label="`${room.roomNumber} - ${room.roomName}`"
            :value="room.roomId"
          />
        </el-select>
        <el-select 
          v-model="filterSeat" 
          placeholder="筛选座位" 
          style="width: 180px; margin-right: 10px" 
          @change="handleFilterChange" 
          clearable
        >
          <el-option label="全部" value="" />
          <el-option
            v-for="seat in seatOptions"
            :key="seat.seatId"
            :label="seat.seatNumber + (seat.seatName ? ` - ${seat.seatName}` : '')"
            :value="seat.seatId"
          />
        </el-select>
        <el-select 
          v-model="filterStatus" 
          placeholder="筛选状态" 
          style="width: 150px; margin-right: 10px" 
          @change="handleFilterChange"
          clearable
        >
          <el-option label="全部" value="" />
          <el-option label="待付款" :value="0" />
          <el-option label="已付款待使用" :value="1" />
          <el-option label="使用中" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
          <el-option label="已退款" value="refunded" />
        </el-select>
        <el-button type="info" @click="handleResetFilter">重置筛选</el-button>
      </div>

      <el-table v-loading="loading" :data="pagedReservations" stripe style="width: 100%">
        <el-table-column prop="reservationNumber" label="预约编号" width="180" />
        <el-table-column prop="roomName" label="大区" width="200">
          <template #default="{ row }">
            {{ row.roomNumber }} - {{ row.roomName }}
          </template>
        </el-table-column>
        <el-table-column prop="seatNumber" label="座位" width="150">
          <template #default="{ row }">
            <span v-if="row.seatNumber">
              {{ row.seatNumber }}<span v-if="row.seatName"> - {{ row.seatName }}</span>
            </span>
            <span v-else style="color: #909399">未指定</span>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100" align="center">
          <template #default="{ row }">
            ¥{{ formatAmount(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.paymentStatus === 1 ? 'success' : 'warning'">
              {{ row.paymentStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="支付时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.paymentTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)">
              {{ getStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-tooltip
              v-if="row.paymentStatus === 0 && row.status !== 4"
              content="订单提交成功，请在5分钟内完成支付，若超时未支付，系统将自动取消订单。"
              placement="top"
            >
              <el-button
                type="success"
                size="small"
                @click="handlePay(row)"
              >
                支付
              </el-button>
            </el-tooltip>
            <el-button 
              v-if="row.paymentStatus === 0 && row.status !== 4"
              type="danger" 
              size="small"
              @click="handleCancelOrRefund(row)"
            >
              取消
            </el-button>
            <el-button
              v-if="row.paymentStatus === 1 && row.status !== 2 && row.status !== 3 && row.status !== 4"
              type="warning"
              size="small"
              @click="handleCancelOrRefund(row)"
            >
              {{ Number(row.amount || 0) === 0 ? '取消' : '退款' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :total="filteredReservations.length"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyReservations, cancelReservation, createPayment } from '@/api/reservation'
import dayjs from 'dayjs'

const route = useRoute()

const loading = ref(false)
const pageSize = 10
const currentPage = ref(1)
const reservationList = ref([])
const filterRoom = ref('')
const filterSeat = ref('')
const filterStatus = ref('')

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const formatAmount = (amount) => {
  if (amount == null || amount === undefined || amount === '') return '0.00'
  // 确保金额始终显示两位小数
  const num = typeof amount === 'string' ? parseFloat(amount) : Number(amount)
  if (isNaN(num) || num < 0) return '0.00'
  return num.toFixed(2)
}

const getStatusText = (row) => {
  if (!row) return '未知'
  // 支付过且已取消，并且退款成功时，显示为“已退款”
  if (row.status === 4 && row.paymentStatus === 1 && row.refundStatus === 1) {
    return '已退款'
  }
  const statusMap = {
    0: '待付款',
    1: '已付款待使用',
    2: '使用中',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[row.status] || '未知'
}

const getStatusType = (row) => {
  if (!row) return 'info'
  // “已退款”使用 info 类型
  if (row.status === 4 && row.paymentStatus === 1 && row.refundStatus === 1) {
    return 'info'
  }
  const typeMap = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'info',
    4: 'danger'
  }
  return typeMap[row.status] || 'info'
}

const loadReservations = async () => {
  loading.value = true
  try {
    const res = await getMyReservations()
    if (res.code === 200) {
      reservationList.value = res.data
      currentPage.value = 1
    }
  } catch (error) {
    ElMessage.error('加载预约列表失败')
  } finally {
    loading.value = false
  }
}

const handlePay = async (row) => {
  try {
    await ElMessageBox.confirm(
      `
        <div style="line-height: 1.7">
          <div style="margin-bottom: 8px">
            确认支付 <b>¥${formatAmount(row.amount)}</b> 吗？
          </div>
          <div style="color: #e6a23c">
            订单提交成功，请在 <b>5分钟内</b> 完成支付，若超时未支付，系统将自动取消订单。
          </div>
        </div>
      `,
      '确认支付',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }
    )
    
    loading.value = true
    const res = await createPayment(row.id)
    loading.value = false
    
    if (res.code === 200 && res.data) {
      // 检查返回的数据是否是HTML表单
      if (typeof res.data === 'string' && res.data.includes('<form')) {
        // 创建表单并在当前标签页发起支付
        const div = document.createElement('div')
        div.innerHTML = res.data
        document.body.appendChild(div)
        
        // 提交表单
        const form = div.querySelector('form')
        if (form) {
          // 留在当前标签页，支付完成后由网关回跳携带 paySuccess 参数
          form.target = '_self'
          form.submit()
          document.body.removeChild(div)
          ElMessage.success('正在跳转到支付页面...')
        } else {
          document.body.removeChild(div)
          console.error('支付表单HTML:', res.data)
          ElMessage.error('支付表单格式错误，请查看控制台')
        }
      } else {
        console.error('支付表单数据:', res.data)
        ElMessage.error('支付表单生成失败，返回数据格式不正确')
      }
    } else {
      ElMessage.error(res.message || '创建支付订单失败')
    }
  } catch (error) {
    loading.value = false
    if (error !== 'cancel') {
      console.error('支付错误:', error)
      ElMessage.error(error.message || '支付失败，请稍后重试')
    }
  }
}

const handleCancelOrRefund = async (row) => {
  const isPointsOrder = Number(row.amount || 0) === 0
  // 非积分订单且已支付的情况，才走退款逻辑
  const isRefund = !isPointsOrder && row.paymentStatus === 1

  try {
    // 使用中的订单不能取消/退款
    if (row.status === 2) {
      ElMessage.warning('该预约正在使用中，无法取消或退款')
      return
    }

    // 积分兑换订单：不走退款逻辑，在预约开始前可直接取消
    if (isPointsOrder) {
      if (!row.startTime) {
        ElMessage.error('预约开始时间不存在，无法判断是否可取消')
        return
      }

      const startTime = new Date(row.startTime)
      const now = new Date()

      if (now >= startTime) {
        ElMessage.error('已到预约开始时间，无法取消')
        return
      }

      const confirmResult = await ElMessageBox.confirm(
        '积分兑换订单取消后积分不予退还，确认取消该预约吗？',
        '确认取消',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )

      if (confirmResult !== 'confirm') {
        return // 用户点击了取消
      }
    } else if (isRefund) {
      if (!row.paymentTime) {
        ElMessage.error('支付时间不存在，无法判断是否可退款')
        return
      }
      if (!row.startTime) {
        ElMessage.error('预约开始时间不存在，无法判断是否可退款')
        return
      }

      const paymentTime = new Date(row.paymentTime)
      const startTime = new Date(row.startTime)
      const now = new Date()

      // 必须在预约开始前
      if (now >= startTime) {
        ElMessage.error('已到预约开始时间，无法退款')
        return
      }

      const timeDiffInMinutes = (now - paymentTime) / (1000 * 60)
      const refundWindowMinutes = 30 // 30分钟退款窗口

      if (timeDiffInMinutes > refundWindowMinutes) {
        // 超过退款时间，显示提示
        ElMessage.error('超过退款允许时间，退款失败')
        return
      }
      
      // 在退款时间内，显示确认对话框
      const confirmMessage = `
        <div style="line-height: 1.7">
          <div style="margin-bottom: 8px">
            <b>满足以下条件可退款：</b>
          </div>
          <ul style="margin: 0 0 10px 18px; padding: 0">
            <li>预约开始前</li>
            <li>付款时间 <b>${refundWindowMinutes} 分钟内</b></li>
          </ul>
        </div>
      `
      const confirmResult = await ElMessageBox.confirm(
        confirmMessage,
        '确认退款',
        {
          distinguishCancelAndClose: true,
          confirmButtonText: '退款',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: true
        }
      )
      
      if (confirmResult !== 'confirm') {
        return // 用户点击了取消或关闭
      }
    } else {
      // 未支付的订单，直接取消
      const confirmResult = await ElMessageBox.confirm(
      '确认取消该预约吗？',
      '确认取消',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
      )
      
      if (confirmResult !== 'confirm') {
        return // 用户点击了取消
      }
    }
    
    // 调用取消/退款接口（包括积分订单直接取消、未支付取消和已支付退款三种情况）
    const res = await cancelReservation(row.id)
    if (res.code === 200) {
      ElMessage.success(isRefund ? '退款成功' : '取消成功')
      loadReservations()
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('Error:', error)
      // 检查错误信息是否包含退款时间相关的提示
      const errorMessage = error.message || ''
      if (errorMessage.includes('超过退款允许时间')) {
        ElMessage.error('超过退款允许时间，退款失败')
      } else {
        ElMessage.error(errorMessage || (isRefund ? '退款失败' : '取消失败'))
      }
    }
  }
}

onMounted(() => {
  loadReservations()
  
  // 检查是否从支付回调返回
  if (route.query.paySuccess === 'true') {
    ElMessage.success('支付成功，正在刷新订单状态...')
    // 清除URL参数
    window.history.replaceState({}, '', '/user/reservation')
    // 延迟刷新，确保后端已处理完回调
    setTimeout(() => {
      loadReservations()
    }, 1000)
  }
})

// 获取所有唯一的大区选项
const roomOptions = computed(() => {
  const roomMap = new Map()
  reservationList.value.forEach(reservation => {
    if (reservation.roomId && reservation.roomName) {
      if (!roomMap.has(reservation.roomId)) {
        roomMap.set(reservation.roomId, {
          roomId: reservation.roomId,
          roomName: reservation.roomName,
          roomNumber: reservation.roomNumber
        })
      }
    }
  })
  return Array.from(roomMap.values()).sort((a, b) => {
    // 按大区编号排序
    if (a.roomNumber && b.roomNumber) {
      return a.roomNumber.localeCompare(b.roomNumber)
    }
    return 0
  })
})

// 获取所有唯一的座位选项
const seatOptions = computed(() => {
  const seatMap = new Map()
  reservationList.value.forEach(reservation => {
    if (reservation.seatId && reservation.seatNumber) {
      if (!seatMap.has(reservation.seatId)) {
        seatMap.set(reservation.seatId, {
          seatId: reservation.seatId,
          seatNumber: reservation.seatNumber,
          seatName: reservation.seatName
        })
      }
    }
  })
  return Array.from(seatMap.values()).sort((a, b) => {
    // 按座位号排序
    if (a.seatNumber && b.seatNumber) {
      return a.seatNumber.localeCompare(b.seatNumber)
    }
    return 0
  })
})

// 筛选后的预约列表
const filteredReservations = computed(() => {
  let result = reservationList.value
  
  // 大区筛选
  if (filterRoom.value !== '') {
    result = result.filter(r => r.roomId === filterRoom.value)
  }
  
  // 座位筛选
  if (filterSeat.value !== '') {
    result = result.filter(r => r.seatId === filterSeat.value)
  }
  
  // 状态筛选
  if (filterStatus.value !== '') {
    if (filterStatus.value === 'refunded') {
      // 已退款：status=4 且 paymentStatus=1 且 refundStatus=1
      result = result.filter(r => {
        return r.status === 4 && r.paymentStatus === 1 && r.refundStatus === 1
      })
    } else {
      // 其他状态：直接匹配status
      result = result.filter(r => r.status === filterStatus.value)
    }
  }
  
  return result
})

const pagedReservations = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredReservations.value.slice(start, start + pageSize)
})

const handlePageChange = (page) => {
  currentPage.value = page
}

const handleFilterChange = () => {
  // 筛选时重置到第一页
  currentPage.value = 1
}

const handleResetFilter = () => {
  filterRoom.value = ''
  filterSeat.value = ''
  filterStatus.value = ''
  currentPage.value = 1
}
</script>

<style scoped>
.reservation-list {
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

.filter-section {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

:deep(.el-card) {
  border-radius: 4px;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>

