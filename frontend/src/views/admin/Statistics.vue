<template>
  <div class="statistics">
    <el-row :gutter="20">
      <!-- 时间段选择 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>统计模式</span>
            </div>
          </template>
          <el-radio-group v-model="statMode" @change="handleModeChange" style="margin-bottom: 20px">
            <el-radio-button label="current">当前时间点统计</el-radio-button>
            <el-radio-button label="timeRange">时间段统计</el-radio-button>
          </el-radio-group>
          
          <div v-if="statMode === 'timeRange'" style="margin-top: 20px">
            <el-form :inline="true" :model="timeRangeForm">
              <el-form-item label="开始时间">
                <el-date-picker
                  v-model="timeRangeForm.startTime"
                  type="datetime"
                  placeholder="选择开始时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="结束时间">
                <el-date-picker
                  v-model="timeRangeForm.endTime"
                  type="datetime"
                  placeholder="选择结束时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadTimeRangeStatistics" :loading="loading">
                  查询统计
                </el-button>
                <el-button @click="resetTimeRange">重置</el-button>
              </el-form-item>
            </el-form>
            <div v-if="timeRangeForm.startTime && timeRangeForm.endTime" style="color: #909399; font-size: 12px; margin-top: 10px">
              统计时间段：{{ timeRangeForm.startTime }} 至 {{ timeRangeForm.endTime }}
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 总体统计 -->
      <el-col :span="24" style="margin-top: 20px">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>{{ statMode === 'current' ? '座位总体统计（五个大区座位之和）' : '时间段座位统计（五个大区座位之和）' }}</span>
              <el-button @click="loadStatistics">刷新</el-button>
            </div>
          </template>
          
          <el-row :gutter="20" v-loading="loading">
            <el-col :xs="24" :sm="12" :md="6" v-for="stat in overallStats" :key="stat.label">
              <div class="stat-item">
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- 五个大区统计 -->
      <el-col :span="24" style="margin-top: 20px">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>五个大区统计</span>
            </div>
          </template>
          
          <el-table v-loading="areaLoading" :data="areaStats" stripe style="width: 100%">
            <el-table-column prop="roomNumber" label="大区编号" width="120" />
            <el-table-column prop="roomName" label="大区名称" width="200" />
            <el-table-column prop="totalSeats" label="座位总数" width="100" align="center" />
            <el-table-column v-if="statMode === 'current'" prop="freeSeats" label="空闲座位" width="100" align="center">
              <template #default="{ row }">
                <span style="color: #67c23a">{{ row.freeSeats }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="statMode === 'current'" prop="reservedSeats" label="已预约座位" width="120" align="center">
              <template #default="{ row }">
                <span style="color: #e6a23c">{{ row.reservedSeats }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="statMode === 'current'" prop="leasedSeats" label="长期租赁座位" width="130" align="center">
              <template #default="{ row }">
                <span style="color: #f56c6c">{{ row.leasedSeats }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="statMode === 'current'" prop="maintenanceSeats" label="维护中座位" width="120" align="center">
              <template #default="{ row }">
                <span style="color: #909399">{{ row.maintenanceSeats }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="statMode === 'timeRange'" prop="freeSeats" label="空闲座位" width="120" align="center">
              <template #default="{ row }">
                <span style="color: #67c23a; font-weight: bold">{{ row.freeSeats }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="statMode === 'timeRange'" prop="occupiedSeats" label="占用座位" width="120" align="center">
              <template #default="{ row }">
                <span style="color: #f56c6c; font-weight: bold">{{ row.occupiedSeats || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="usageRate" label="使用率" width="100" align="center">
              <template #default="{ row }">
                <span style="font-weight: bold">{{ formatPercentage(row.usageRate) }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="statMode === 'timeRange'" prop="freeRate" label="空闲率" width="100" align="center">
              <template #default="{ row }">
                <span style="font-weight: bold; color: #67c23a">{{ formatPercentage(row.freeRate) }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="statMode === 'current'" prop="reservationCount" label="预约次数" width="100" align="center" />
            <el-table-column v-if="statMode === 'current'" prop="completedCount" label="已完成次数" width="120" align="center" />
            <el-table-column v-if="statMode === 'current'" prop="paidCount" label="预约支付次数" width="120" align="center" />
            <el-table-column prop="revenue" label="营业额" width="150" align="center">
              <template #default="{ row }">
                <span style="font-weight: bold; color: #67c23a">{{ formatCurrency(row.revenue) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getStudyRoomStatistics, getAreaStatistics, getSeatStatisticsByTimeRange, getAreaStatisticsByTimeRange } from '@/api/admin/statistics'

const loading = ref(false)
const areaLoading = ref(false)
const statistics = ref({})
const areaStats = ref([])
const statMode = ref('current') // 'current' 或 'timeRange'

const timeRangeForm = reactive({
  startTime: '',
  endTime: ''
})

const overallStats = computed(() => {
  if (statMode.value === 'timeRange') {
    return [
      {
        label: '座位总数',
        value: statistics.value.totalSeats || 0
      },
      {
        label: '空闲座位',
        value: statistics.value.freeSeats || 0
      },
      {
        label: '占用座位',
        value: statistics.value.occupiedSeats || 0
      },
      {
        label: '空闲率',
        value: statistics.value.freeRate ? `${statistics.value.freeRate}%` : '0%'
      },
      {
        label: '使用率',
        value: statistics.value.usageRate ? `${statistics.value.usageRate}%` : '0%'
      },
      {
        label: '总营业额',
        value: formatCurrency(statistics.value.totalRevenue)
      }
    ]
  } else {
    return [
      {
        label: '座位总数',
        value: statistics.value.totalSeats || 0
      },
      {
        label: '空闲座位',
        value: statistics.value.freeSeats || 0
      },
      {
        label: '已预约座位',
        value: statistics.value.reservedSeats || 0
      },
      {
        label: '长期租赁座位',
        value: statistics.value.leasedSeats || 0
      },
      {
        label: '维护中座位',
        value: statistics.value.maintenanceSeats || 0
      },
      {
        label: '空闲率',
        value: statistics.value.freeRate ? `${statistics.value.freeRate}%` : '0%'
      },
      {
        label: '总预约次数',
        value: statistics.value.totalReservations || 0
      },
      {
        label: '已完成预约',
        value: statistics.value.completedReservations || 0
      },
      {
        label: '已取消预约',
        value: statistics.value.cancelledReservations || 0
      },
      {
        label: '已支付预约',
        value: statistics.value.paidReservations || 0
      },
      {
        label: '总营业额',
        value: formatCurrency(statistics.value.totalRevenue)
      }
    ]
  }
})

const loadStatistics = async () => {
  if (statMode.value === 'timeRange') {
    if (!timeRangeForm.startTime || !timeRangeForm.endTime) {
      ElMessage.warning('请先选择时间段')
      return
    }
    await loadTimeRangeStatistics()
  } else {
    loading.value = true
    areaLoading.value = true
    
    try {
      const [statsRes, areaRes] = await Promise.all([
        getStudyRoomStatistics(),
        getAreaStatistics()
      ])
      
      if (statsRes.code === 200) {
        statistics.value = statsRes.data
      }
      
      if (areaRes.code === 200) {
        areaStats.value = areaRes.data
      }
    } catch (error) {
      ElMessage.error('加载统计信息失败')
    } finally {
      loading.value = false
      areaLoading.value = false
    }
  }
}

const loadTimeRangeStatistics = async () => {
  if (!timeRangeForm.startTime || !timeRangeForm.endTime) {
    ElMessage.warning('请选择开始时间和结束时间')
    return
  }
  
  const start = dayjs(timeRangeForm.startTime)
  const end = dayjs(timeRangeForm.endTime)
  
  if (start.isAfter(end)) {
    ElMessage.error('开始时间不能晚于结束时间')
    return
  }
  
  loading.value = true
  areaLoading.value = true
  
  try {
    const [statsRes, areaRes] = await Promise.all([
      getSeatStatisticsByTimeRange(timeRangeForm.startTime, timeRangeForm.endTime),
      getAreaStatisticsByTimeRange(timeRangeForm.startTime, timeRangeForm.endTime)
    ])
    
    if (statsRes.code === 200) {
      statistics.value = statsRes.data
    } else {
      ElMessage.error(statsRes.message || '获取统计信息失败')
    }
    
    if (areaRes.code === 200) {
      areaStats.value = areaRes.data
    } else {
      ElMessage.error(areaRes.message || '获取大区统计信息失败')
    }
  } catch (error) {
    ElMessage.error('加载时间段统计信息失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
    areaLoading.value = false
  }
}

const handleModeChange = (mode) => {
  if (mode === 'current') {
    loadStatistics()
  } else {
    // 切换到时间段模式时，初始化默认时间（今天）
    const today = dayjs().format('YYYY-MM-DD')
    timeRangeForm.startTime = `${today} 00:00:00`
    timeRangeForm.endTime = `${today} 23:59:59`
  }
}

const resetTimeRange = () => {
  timeRangeForm.startTime = ''
  timeRangeForm.endTime = ''
  statistics.value = {}
  areaStats.value = []
}

// 格式化百分比显示
const formatPercentage = (value) => {
  if (value === null || value === undefined) {
    return '0%'
  }
  const num = typeof value === 'number' ? value : parseFloat(value)
  if (isNaN(num)) {
    return '0%'
  }
  return `${num.toFixed(2)}%`
}

// 格式化货币显示
const formatCurrency = (value) => {
  if (value === null || value === undefined) {
    return '¥0.00'
  }
  const num = typeof value === 'number' ? value : parseFloat(value)
  if (isNaN(num)) {
    return '¥0.00'
  }
  return `¥${num.toFixed(2)}`
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.statistics {
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

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 4px;
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

:deep(.el-card) {
  border-radius: 4px;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>

