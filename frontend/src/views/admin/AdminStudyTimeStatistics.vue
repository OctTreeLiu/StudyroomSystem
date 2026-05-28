<template>
  <div class="admin-study-time-statistics">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ selectedUserId ? '用户学习时长统计' : '整体用户学习时长统计' }}</span>
          <div style="display: flex; gap: 10px; align-items: center;">
            <el-select
              v-model="selectedUserId"
              placeholder="选择用户（留空查看所有用户）"
              clearable
              filterable
              style="width: 250px"
              @change="handleUserChange"
            >
              <el-option
                v-for="user in userList"
                :key="user.id"
                :label="`${user.realName || user.username} (${user.username})`"
                :value="user.id"
              />
            </el-select>
            <el-button @click="loadStatistics">刷新</el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <!-- 总览卡片 -->
        <el-row :gutter="20" class="overview-cards">
          <el-col :xs="24" :sm="12">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-label">过去7天{{ selectedUserId ? '' : '（所有用户）' }}</div>
                <div class="stat-value">{{ formatTime(statistics.last7Days) }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-label">过去30天{{ selectedUserId ? '' : '（所有用户）' }}</div>
                <div class="stat-value">{{ formatTime(statistics.last30Days) }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 学习时长柱状图 -->
        <StudyTimeChart
          :hours-data="hoursData"
          :days-data="daysData"
          :total-minutes="selectedUserId ? todayTotalMinutes : statistics.last24Hours"
          :comparison-text="comparisonText"
          :is-admin="true"
        />

        <!-- 学习红人榜 -->
        <el-row :gutter="20" style="margin-top: 20px">
          <el-col :xs="24" :lg="16">
            <el-card class="ranking-card">
              <template #header>
                <div class="ranking-header">
                  <span>学习红人榜</span>
                  <el-radio-group v-model="rankingPeriod" @change="loadRanking" size="small">
                    <el-radio-button label="daily">每日</el-radio-button>
                    <el-radio-button label="weekly">每周</el-radio-button>
                    <el-radio-button label="monthly">每月</el-radio-button>
                  </el-radio-group>
                </div>
              </template>
              
              <div v-loading="rankingLoading" class="ranking-content">
                <!-- 前三名特别展示 -->
                <div class="top-three">
                  <div
                    v-for="(item, index) in topThree"
                    :key="item.userId"
                    :class="['top-item', `rank-${index + 1}`]"
                  >
                    <div class="rank-badge">{{ item.rank }}</div>
                    <div class="user-info">
                      <el-avatar
                        :size="60"
                        :src="item.avatarUrl"
                        class="user-avatar"
                      >
                        <span>{{ (item.username || item.realName || '匿名')?.charAt(0)?.toUpperCase() }}</span>
                      </el-avatar>
                      <div class="username">{{ item.username || item.realName }}</div>
                      <div class="study-time">{{ formatTime(item.minutes) }}</div>
                    </div>
                  </div>
                </div>
                
                <!-- 其余排名列表 -->
                <div class="ranking-list">
                  <div
                    v-for="item in remainingRanking"
                    :key="item.userId"
                    class="ranking-item"
                  >
                    <span class="rank-number">{{ item.rank }}</span>
                    <el-avatar
                      :size="40"
                      :src="item.avatarUrl"
                      class="user-avatar-small"
                    >
                      <span>{{ (item.username || item.realName || '匿名')?.charAt(0)?.toUpperCase() }}</span>
                    </el-avatar>
                    <span class="user-name">{{ item.username || item.realName }}</span>
                    <span class="time-value">{{ formatTime(item.minutes) }}</span>
                  </div>
                </div>
                
                <div v-if="rankingList.length === 0" class="empty-ranking">
                  暂无数据
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllUsersStudyTimeStatistics, getStudyTimeRanking, getAllUsers24HoursStatistics, getAllUsers7DaysStatistics, getUserStatisticsByAdmin, getUser24HoursStatisticsByAdmin, getUser7DaysStatisticsByAdmin } from '@/api/studyTime'
import { getAllUsers } from '@/api/admin/user'
import StudyTimeChart from '@/components/StudyTimeChart.vue'

const loading = ref(false)
const rankingLoading = ref(false)
const rankingPeriod = ref('daily')
const statistics = ref({
  last24Hours: 0,
  last7Days: 0,
  last30Days: 0,
  dailyStatistics: []
})
const rankingList = ref([])
const hoursData = ref([])
const daysData = ref([])
const comparisonText = ref('')
const todayTotalMinutes = ref(0) // 今日总时长（分钟）
const selectedUserId = ref(null)
const userList = ref([])

// 前三名
const topThree = computed(() => {
  return rankingList.value.slice(0, 3)
})

// 其余排名
const remainingRanking = computed(() => {
  return rankingList.value.slice(3)
})

const formatTime = (minutes) => {
  if (!minutes || minutes === 0) {
    return '0分钟'
  }
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0 && mins > 0) {
    return `${hours}小时${mins}分钟`
  } else if (hours > 0) {
    return `${hours}小时`
  } else {
    return `${mins}分钟`
  }
}

const loadUserList = async () => {
  try {
    const res = await getAllUsers()
    if (res.code === 200) {
      userList.value = res.data || []
    }
  } catch (error) {
    console.warn('加载用户列表失败:', error)
  }
}

const handleUserChange = () => {
  loadStatistics()
}

const loadStatistics = async () => {
  loading.value = true
  try {
    let res
    let hoursRes
    let daysRes
    
    if (selectedUserId.value) {
      // 加载指定用户的统计数据
      res = await getUserStatisticsByAdmin(selectedUserId.value).catch(err => {
        console.error('加载用户统计失败:', err)
        return { code: 500, message: '加载统计失败', data: null }
      })
      
      hoursRes = await getUser24HoursStatisticsByAdmin(selectedUserId.value)
      daysRes = await getUser7DaysStatisticsByAdmin(selectedUserId.value)
    } else {
      // 加载所有用户的统计数据
      res = await getAllUsersStudyTimeStatistics().catch(err => {
        console.error('加载统计失败:', err)
        return { code: 500, message: '加载统计失败', data: null }
      })
      
      hoursRes = await getAllUsers24HoursStatistics()
      daysRes = await getAllUsers7DaysStatistics()
    }
    
    if (res.code === 200) {
      statistics.value = res.data || {
        last24Hours: 0,
        last7Days: 0,
        last30Days: 0,
        dailyStatistics: []
      }
      
      // 计算今日总时长（从dailyStatistics中找到今天的记录）
      // 使用本地时间获取今天的日期字符串，格式：YYYY-MM-DD
      const today = new Date()
      const year = today.getFullYear()
      const month = String(today.getMonth() + 1).padStart(2, '0')
      const day = String(today.getDate()).padStart(2, '0')
      const todayStr = `${year}-${month}-${day}`
      const todayStat = statistics.value.dailyStatistics?.find(item => item.date === todayStr)
      todayTotalMinutes.value = todayStat?.minutes || 0
      
      // 计算对比文本（与昨天对比）
      if (statistics.value.dailyStatistics && statistics.value.dailyStatistics.length > 0) {
        const yesterday = new Date(today)
        yesterday.setDate(yesterday.getDate() - 1)
        const yesterdayYear = yesterday.getFullYear()
        const yesterdayMonth = String(yesterday.getMonth() + 1).padStart(2, '0')
        const yesterdayDay = String(yesterday.getDate()).padStart(2, '0')
        const yesterdayStr = `${yesterdayYear}-${yesterdayMonth}-${yesterdayDay}`
        const yesterdayStat = statistics.value.dailyStatistics.find(item => item.date === yesterdayStr)
        const yesterdayMinutes = yesterdayStat?.minutes || 0
        
        const diff = todayTotalMinutes.value - yesterdayMinutes
        if (diff > 0) {
          comparisonText.value = `${formatTime(diff)} 比昨天多`
        } else if (diff < 0) {
          comparisonText.value = `${formatTime(Math.abs(diff))} 比昨天少`
        } else {
          comparisonText.value = '与昨天相同'
        }
      } else {
        comparisonText.value = ''
      }
    } else {
      ElMessage.warning(res.message || '加载学习时长统计失败')
      todayTotalMinutes.value = 0
      comparisonText.value = ''
    }

    // 加载24小时详细数据
    if (hoursRes && hoursRes.code === 200) {
      hoursData.value = hoursRes.data?.hoursData || hoursRes.data || []
    } else {
      hoursData.value = []
    }

    // 加载7天详细数据
    if (daysRes && daysRes.code === 200) {
      daysData.value = daysRes.data?.daysData || daysRes.data || []
    } else {
      daysData.value = []
    }
  } catch (error) {
    console.error('加载统计失败:', error)
    ElMessage.error('加载学习时长统计失败：' + (error.message || '未知错误'))
    statistics.value = {
      last24Hours: 0,
      last7Days: 0,
      last30Days: 0,
      dailyStatistics: []
    }
    todayTotalMinutes.value = 0
    comparisonText.value = ''
  } finally {
    loading.value = false
  }
}

const loadRanking = async () => {
  rankingLoading.value = true
  try {
    const res = await getStudyTimeRanking(rankingPeriod.value).catch(err => {
      console.error('加载排行榜失败:', err)
      return { code: 500, message: '加载学习红人榜失败', data: null }
    })
    
    if (res.code === 200) {
      rankingList.value = res.data?.rankingList || []
      // 如果没有数据，不显示错误，只显示"暂无数据"
      if (rankingList.value.length === 0) {
        // 静默处理，不显示错误消息
      }
    } else {
      // 显示警告而不是错误，因为可能是真的没有数据
      if (res.message && !res.message.includes('暂无')) {
        ElMessage.warning(res.message || '加载学习红人榜失败')
      }
      rankingList.value = []
    }
  } catch (error) {
    console.error('加载排行榜失败:', error)
    ElMessage.warning('加载学习红人榜失败：' + (error.message || '未知错误'))
    rankingList.value = []
  } finally {
    rankingLoading.value = false
  }
}

onMounted(() => {
  loadUserList()
  loadStatistics()
  loadRanking()
})
</script>

<style scoped>
.admin-study-time-statistics {
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

.overview-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  padding: 20px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #67C23A;
}


/* 学习红人榜样式 */
.ranking-card {
  margin-top: 20px;
}

.ranking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.ranking-content {
  min-height: 300px;
  max-height: 500px;
  overflow-y: auto;
}

.top-three {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
}

.top-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 8px;
  background: #f5f7fa;
  transition: all 0.3s;
}

.top-item.rank-1 {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.3);
}

.top-item.rank-2 {
  background: linear-gradient(135deg, #C0C0C0 0%, #A0A0A0 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(192, 192, 192, 0.3);
}

.top-item.rank-3 {
  background: linear-gradient(135deg, #CD7F32 0%, #B87333 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(205, 127, 50, 0.3);
}

.rank-badge {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  margin-right: 15px;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.user-avatar {
  margin-bottom: 10px;
  border: 3px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.username {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
}

.study-time {
  font-size: 14px;
  opacity: 0.9;
}

.ranking-list {
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f7fa;
}

.ranking-item:last-child {
  border-bottom: none;
}

.rank-number {
  width: 30px;
  text-align: center;
  font-weight: bold;
  color: #909399;
  font-size: 14px;
}

.user-avatar-small {
  margin: 0 10px;
  flex-shrink: 0;
}

.user-name {
  flex: 1;
  font-size: 14px;
  color: #303133;
}

.time-value {
  font-size: 14px;
  color: #409EFF;
  font-weight: 500;
}

.empty-ranking {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chart-container {
    height: 300px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .top-three {
    gap: 10px;
  }
  
  .top-item {
    padding: 12px;
  }
}
</style>
