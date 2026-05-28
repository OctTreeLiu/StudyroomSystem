<template>
  <div class="study-time-statistics">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学习时长统计</span>
          <el-button @click="loadStatistics">刷新</el-button>
        </div>
      </template>

      <div v-loading="loading">
        <!-- 总览卡片 -->
        <el-row :gutter="20" class="overview-cards">
          <el-col :xs="24" :sm="12">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-label">过去7天</div>
                <div class="stat-value">{{ formatTime(statistics.last7Days) }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-label">过去30天</div>
                <div class="stat-value">{{ formatTime(statistics.last30Days) }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 学习时长柱状图 -->
        <StudyTimeChart
          :hours-data="hoursData"
          :days-data="daysData"
          :total-minutes="todayTotalMinutes"
          :comparison-text="comparisonText"
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
import { getMyStudyTimeStatistics, getMy24HoursStatistics, getMy7DaysStatistics, getMyStudyTimeRanking } from '@/api/studyTime'
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
const hoursData = ref([])
const daysData = ref([])
const comparisonText = ref('')
const todayTotalMinutes = ref(0) // 今日总时长（分钟）
const rankingList = ref([])

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

const loadStatistics = async () => {
  loading.value = true
  try {
    // 加载基础统计
    const res = await getMyStudyTimeStatistics().catch(err => {
      console.error('加载统计失败:', err)
      return { code: 500, message: '加载统计失败', data: null }
    })
    
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
      }
    } else {
      // 静默处理，不显示错误消息，使用默认值
      statistics.value = {
        last24Hours: 0,
        last7Days: 0,
        last30Days: 0,
        dailyStatistics: []
      }
      todayTotalMinutes.value = 0
      comparisonText.value = ''
    }

    // 加载24小时详细数据
    try {
      const hoursRes = await getMy24HoursStatistics()
      if (hoursRes && hoursRes.code === 200) {
        hoursData.value = hoursRes.data?.hoursData || hoursRes.data || []
      } else {
        hoursData.value = []
      }
    } catch (error) {
      // 静默处理，不显示错误
      console.warn('加载24小时数据失败，使用默认数据:', error)
      hoursData.value = []
    }

    // 加载7天详细数据
    try {
      const daysRes = await getMy7DaysStatistics()
      if (daysRes && daysRes.code === 200) {
        daysData.value = daysRes.data?.daysData || daysRes.data || []
      } else {
        daysData.value = []
      }
    } catch (error) {
      // 静默处理，不显示错误
      console.warn('加载7天数据失败，使用默认数据:', error)
      daysData.value = []
    }
  } catch (error) {
    console.error('加载学习时长统计失败:', error)
    // 静默处理，不显示错误消息
    statistics.value = {
      last24Hours: 0,
      last7Days: 0,
      last30Days: 0,
      dailyStatistics: []
    }
    todayTotalMinutes.value = 0
    comparisonText.value = ''
    hoursData.value = []
    daysData.value = []
  } finally {
    loading.value = false
  }
}

const loadRanking = async () => {
  rankingLoading.value = true
  try {
    const res = await getMyStudyTimeRanking(rankingPeriod.value).catch(err => {
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
  loadStatistics()
  loadRanking()
})
</script>

<style scoped>
.study-time-statistics {
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
  color: #409EFF;
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

.user-name {
  flex: 1;
  font-size: 14px;
  color: #303133;
  margin-left: 10px;
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

