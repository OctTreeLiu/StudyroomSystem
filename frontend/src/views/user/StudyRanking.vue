<template>
  <div class="study-ranking">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>学习红人榜</h2>
          <p class="subtitle">通过排行榜机制提升用户活跃度，营造良性学习竞争氛围</p>
        </div>
      </template>

      <!-- 榜单切换 -->
      <div class="ranking-tabs">
        <el-radio-group v-model="currentPeriod" @change="loadRanking" size="large">
          <el-radio-button label="daily">今日榜</el-radio-button>
          <el-radio-button label="weekly">本周榜</el-radio-button>
          <el-radio-button label="monthly">本月榜</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 排行榜内容 -->
      <div v-loading="loading" class="ranking-content">
        <!-- 前三名特别展示 -->
        <div v-if="topThree.length > 0" class="top-three">
          <div
            v-for="(item, index) in topThree"
            :key="item.userId"
            :class="['top-item', `rank-${index + 1}`]"
          >
            <div class="rank-badge">
              <el-icon v-if="index === 0" class="crown-icon"><Trophy /></el-icon>
              <span class="rank-number">{{ item.rank }}</span>
            </div>
            <div class="user-info">
              <el-avatar
                :size="60"
                :src="item.avatarUrl"
                class="user-avatar"
              >
                <span>{{ (item.username || item.realName || '匿名')?.charAt(0)?.toUpperCase() }}</span>
              </el-avatar>
              <div class="username">{{ item.username || item.realName || '匿名用户' }}</div>
              <div class="study-time">
                <span class="time-value">{{ formatTime(item.minutes) }}</span>
                <span class="time-label">学习时长</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 其余排名列表 -->
        <div v-if="remainingRanking.length > 0" class="ranking-list">
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
            <span class="user-name">{{ item.username || item.realName || '匿名用户' }}</span>
            <span class="time-value">{{ formatTime(item.minutes) }}</span>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && rankingList.length === 0" class="empty-ranking">
          <el-empty description="暂无排行榜数据" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Trophy } from '@element-plus/icons-vue'
import { getMyStudyTimeRanking } from '@/api/studyTime'

const loading = ref(false)
const currentPeriod = ref('daily')
const rankingList = ref([])

// 前三名
const topThree = computed(() => {
  return rankingList.value.slice(0, 3)
})

// 其余排名（第4-10名）
const remainingRanking = computed(() => {
  return rankingList.value.slice(3, 10)
})

// 格式化学习时长
const formatTime = (minutes) => {
  if (!minutes || minutes === 0) {
    return '0分钟'
  }
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0) {
    return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`
  }
  return `${mins}分钟`
}

// 加载排行榜数据
const loadRanking = async () => {
  loading.value = true
  try {
    const res = await getMyStudyTimeRanking(currentPeriod.value)
    if (res.code === 200) {
      rankingList.value = res.data?.rankingList || []
      if (rankingList.value.length === 0) {
        ElMessage.info('暂无排行榜数据')
      }
    } else {
      ElMessage.warning(res.message || '加载学习红人榜失败')
      rankingList.value = []
    }
  } catch (error) {
    console.error('加载排行榜失败:', error)
    ElMessage.error('加载学习红人榜失败：' + (error.message || '未知错误'))
    rankingList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRanking()
})
</script>

<style scoped>
.study-ranking {
  padding: 20px;
  min-height: 500px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
  font-weight: bold;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.ranking-tabs {
  display: flex;
  justify-content: center;
  margin: 30px 0;
}

.ranking-content {
  min-height: 400px;
}

/* 前三名特别展示 */
.top-three {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 30px;
  margin: 40px 0 50px;
  padding: 0 20px;
}

.top-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  min-width: 180px;
}

.top-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.rank-1 {
  order: 2;
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  border: 3px solid #ffd700;
}

.rank-2 {
  order: 1;
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
  border: 3px solid #c0c0c0;
}

.rank-3 {
  order: 3;
  background: linear-gradient(135deg, #cd7f32 0%, #e6a85c 100%);
  border: 3px solid #cd7f32;
}

.rank-badge {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.crown-icon {
  position: absolute;
  top: -10px;
  right: -10px;
  font-size: 30px;
  color: #ffd700;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.rank-number {
  font-size: 24px;
  font-weight: bold;
}

.user-info {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.user-avatar {
  margin-bottom: 10px;
  border: 3px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.username {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
  word-break: break-all;
}

.study-time {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.time-value {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.time-label {
  font-size: 12px;
  color: #909399;
}

/* 其余排名列表 */
.ranking-list {
  max-width: 800px;
  margin: 0 auto;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  margin-bottom: 10px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.ranking-item:hover {
  background: #f5f7fa;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transform: translateX(5px);
}

.ranking-item .rank-number {
  width: 40px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #606266;
}

.user-avatar-small {
  margin: 0 15px;
  flex-shrink: 0;
}

.ranking-item .user-name {
  flex: 1;
  font-size: 16px;
  color: #303133;
}

.ranking-item .time-value {
  font-size: 16px;
  font-weight: 600;
  color: #409EFF;
  min-width: 100px;
  text-align: right;
}

/* 空状态 */
.empty-ranking {
  padding: 60px 20px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .top-three {
    flex-direction: column;
    align-items: center;
    gap: 20px;
  }

  .top-item {
    min-width: 200px;
  }

  .rank-1,
  .rank-2,
  .rank-3 {
    order: 0;
  }
}
</style>

