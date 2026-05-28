<template>
  <div class="reservation-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预约管理</span>
          <div>
            <el-input
              v-model="searchUsername"
              placeholder="请输入用户名或用户ID"
              style="width: 220px; margin-right: 10px"
              clearable
              @input="handleSearch"
              @clear="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-date-picker
              v-model="searchDate"
              type="date"
              placeholder="按日期查找"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 180px; margin-right: 10px"
              clearable
              @change="handleDateChange"
            />
            <el-select v-model="filterRoom" placeholder="筛选大区" style="width: 150px" @change="handleFilterChange" clearable>
              <el-option label="全部" value="" />
              <el-option
                v-for="room in roomOptions"
                :key="room.roomId"
                :label="room.roomName"
                :value="room.roomId"
              />
            </el-select>
            <el-select v-model="filterStatus" placeholder="筛选状态" style="width: 150px" @change="handleFilterChange">
              <el-option label="全部" value="" />
              <el-option label="待付款" :value="0" />
              <el-option label="已付款待使用" :value="1" />
              <el-option label="使用中" :value="2" />
              <el-option label="已完成" :value="3" />
              <el-option label="已取消" :value="4" />
              <el-option label="已退款" value="refunded" />
            </el-select>
            <el-button type="primary" style="margin-left: 10px" @click="loadReservations">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="pagedReservations" stripe style="width: 100%">
        <el-table-column prop="reservationNumber" label="预约编号" width="180" />
        <el-table-column prop="username" label="用户" width="120" />
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
            ¥{{ row.amount }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="支付时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.paymentTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.paymentStatus === 1 ? 'success' : 'warning'">
              {{ row.paymentStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)">
              {{ getStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getAllReservations } from '@/api/admin/reservation'

const loading = ref(false)
const reservationList = ref([])
const filterStatus = ref('')
const filterRoom = ref('')
const searchUsername = ref('')
const searchDate = ref('')

const pageSize = 10
const currentPage = ref(1)

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

const filteredReservations = computed(() => {
  let result = reservationList.value
  
  // 大区筛选
  if (filterRoom.value !== '') {
    result = result.filter(r => r.roomId === filterRoom.value)
  }
  
  // 按状态筛选
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
  
  // 按用户筛选（支持用户名和用户ID模糊搜索）
  if (searchUsername.value && searchUsername.value.trim() !== '') {
    const keyword = searchUsername.value.trim()
    result = result.filter(r => {
      // 用户名模糊匹配（不区分大小写）
      const username = (r.username || '').toLowerCase()
      const usernameMatch = username.includes(keyword.toLowerCase())
      
      // 用户ID精确或模糊匹配（支持数字ID）
      const userId = r.userId ? String(r.userId) : ''
      const userIdMatch = userId.includes(keyword)
      
      return usernameMatch || userIdMatch
    })
  }
  
  return result
})

const pagedReservations = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredReservations.value.slice(start, start + pageSize)
})

const handleSearch = () => {
  // 搜索时不需要重新加载数据，computed会自动更新
  currentPage.value = 1
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const getStatusText = (row) => {
  if (!row) return '未知'
  // 支付过后取消且退款成功的订单，统一显示为“已退款”
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
    const res = await getAllReservations(searchDate.value)
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

const handleDateChange = () => {
  loadReservations()
}

const handleFilterChange = () => {
  currentPage.value = 1
}

onMounted(() => {
  loadReservations()
})

const handlePageChange = (page) => {
  currentPage.value = page
}
</script>

<style scoped>
.reservation-management {
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

:deep(.el-card) {
  border-radius: 4px;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-tag) {
  border-radius: 6px;
  font-weight: 500;
}
</style>

