<template>
  <div class="usage-record-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>使用记录</span>
          <el-button @click="loadRecords">刷新</el-button>
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
          v-model="filterType" 
          placeholder="筛选使用类型" 
          style="width: 180px; margin-right: 10px" 
          @change="handleFilterChange" 
          clearable
        >
          <el-option label="全部" value="" />
          <el-option label="预约使用" value="1" />
          <el-option label="长期租赁使用" value="2" />
        </el-select>
        <el-button type="info" @click="handleResetFilter">重置筛选</el-button>
      </div>

      <el-table v-loading="loading" :data="pagedRecords" stripe style="width: 100%">
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
        <el-table-column prop="type" label="使用类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'primary' : 'success'">
              {{ row.type === 1 ? '预约使用' : '长期租赁使用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template #default="{ row }">
            {{ row.endTime ? formatTime(row.endTime) : '进行中' }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="使用时长" width="120" align="center">
          <template #default="{ row }">
            {{ formatDuration(row.duration) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="记录时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && filteredRecords.length === 0" description="暂无使用记录" />
      <div class="pagination" v-if="filteredRecords.length > 0">
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :total="filteredRecords.length"
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
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getMyReservations } from '@/api/reservation'
import { getMyLeases } from '@/api/lease'

const loading = ref(false)
const pageSize = 10
const currentPage = ref(1)
const recordList = ref([])
const filterRoom = ref('')
const filterSeat = ref('')
const filterType = ref('')

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const formatDuration = (minutes) => {
  if (!minutes && minutes !== 0) return '-'
  if (minutes < 60) {
    return `${minutes}分钟`
  }
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (mins === 0) {
    return `${hours}小时`
  }
  return `${hours}小时${mins}分钟`
}

const safeToDate = (val) => {
  if (!val) return null
  const d = new Date(val)
  return isNaN(d.getTime()) ? null : d
}

const diffMinutes = (start, end) => {
  const s = safeToDate(start)
  const e = safeToDate(end)
  if (!s || !e) return null
  const minutes = Math.floor((e.getTime() - s.getTime()) / (1000 * 60))
  return minutes >= 0 ? minutes : null
}

const normalizeLeaseTimeRange = (lease) => {
  // 优先使用后端返回的 startTime/endTime；如果没有，则用 startDate/endDate 生成当天起止时间
  const startTime = lease?.startTime || (lease?.startDate ? dayjs(lease.startDate).startOf('day').toDate() : null)
  const endTime = lease?.endTime || (lease?.endDate ? dayjs(lease.endDate).endOf('day').toDate() : null)
  return { startTime, endTime }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const [resvRes, leaseRes] = await Promise.all([getMyReservations(), getMyLeases()])

    const reservations = resvRes?.code === 200 ? (resvRes.data || []) : []
    const leases = leaseRes?.code === 200 ? (leaseRes.data || []) : []

    // 预约订单：状态=已完成(3) -> 预约使用(type=1)
    const reservationRecords = reservations
      .filter(r => Number(r?.status) === 3)
      .map(r => {
        const duration = diffMinutes(r.startTime, r.endTime)
        return {
          id: `reservation-${r.id}`,
          roomId: r.roomId,
          roomNumber: r.roomNumber,
          roomName: r.roomName,
          seatId: r.seatId,
          seatNumber: r.seatNumber,
          seatName: r.seatName,
          type: 1,
          startTime: r.startTime,
          endTime: r.endTime,
          duration,
          // 使用记录时间：用结束时间作为记录时间（更符合“已完成”）
          createTime: r.endTime || r.startTime
        }
      })

    // 长期租赁：状态=使用结束(6) -> 租赁使用(type=2)
    const leaseRecords = leases
      .filter(l => Number(l?.status) === 6)
      .map(l => {
        const { startTime, endTime } = normalizeLeaseTimeRange(l)
        const duration = diffMinutes(startTime, endTime)
        return {
          id: `lease-${l.id}`,
          roomId: l.roomId,
          roomNumber: l.roomNumber,
          roomName: l.roomName,
          seatId: l.seatId,
          seatNumber: l.seatNumber,
          seatName: l.seatName,
          type: 2,
          startTime,
          endTime,
          duration,
          createTime: endTime || startTime
        }
      })

    const merged = [...reservationRecords, ...leaseRecords]
      .sort((a, b) => {
        const ta = safeToDate(a.createTime)?.getTime?.() || 0
        const tb = safeToDate(b.createTime)?.getTime?.() || 0
        return tb - ta
      })

    recordList.value = merged
    currentPage.value = 1

    if (resvRes?.code !== 200) {
      ElMessage.warning(resvRes?.message || '加载预约订单失败')
    }
    if (leaseRes?.code !== 200) {
      ElMessage.warning(leaseRes?.message || '加载长期租赁订单失败')
    }
  } catch (error) {
    ElMessage.error('加载使用记录失败')
  } finally {
    loading.value = false
  }
}

// 获取所有唯一的大区选项
const roomOptions = computed(() => {
  const roomMap = new Map()
  recordList.value.forEach(record => {
    if (record.roomId && record.roomName) {
      if (!roomMap.has(record.roomId)) {
        roomMap.set(record.roomId, {
          roomId: record.roomId,
          roomName: record.roomName,
          roomNumber: record.roomNumber
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
  recordList.value.forEach(record => {
    if (record.seatId && record.seatNumber) {
      if (!seatMap.has(record.seatId)) {
        seatMap.set(record.seatId, {
          seatId: record.seatId,
          seatNumber: record.seatNumber,
          seatName: record.seatName
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

// 筛选后的使用记录列表
const filteredRecords = computed(() => {
  let result = recordList.value
  
  // 大区筛选
  if (filterRoom.value !== '') {
    result = result.filter(r => r.roomId === filterRoom.value)
  }
  
  // 座位筛选
  if (filterSeat.value !== '') {
    result = result.filter(r => r.seatId === filterSeat.value)
  }

  // 使用类型筛选（1: 预约使用；2: 长期租赁使用）
  if (filterType.value !== '') {
    const t = Number(filterType.value)
    result = result.filter(r => r.type === t)
  }
  
  return result
})

const pagedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredRecords.value.slice(start, start + pageSize)
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
  filterType.value = ''
  currentPage.value = 1
}

onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.usage-record-list {
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
</style>

