<template>
  <div class="points-center">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>积分中心</h2>
        </div>
      </template>

      <!-- 积分信息卡片 -->
      <div class="points-info-card">
        <div class="points-display">
          <div class="points-value">{{ pointsInfo.totalPoints || 0 }}</div>
          <div class="points-label">当前积分</div>
        </div>
        <div class="points-actions">
          <el-button 
            type="primary" 
            size="large"
            :disabled="pointsInfo.hasSignedIn"
            :loading="signingIn"
            @click="handleSignIn"
          >
            {{ pointsInfo.hasSignedIn ? '今日已签到' : '每日签到' }}
          </el-button>
          <el-button 
            type="success" 
            size="large"
            @click="$router.push('/user/points/exchange')"
          >
            积分兑换预约
          </el-button>
          <el-button 
            type="info" 
            size="large"
            @click="$router.push('/user/points/history')"
          >
            查看积分流水
          </el-button>
        </div>
      </div>

      <!-- 积分规则说明 -->
      <el-card class="rules-card" shadow="never">
        <template #header>
          <div class="rules-header">积分规则</div>
        </template>
        <div class="rules-content">
          <div class="rule-item">
            <el-icon class="rule-icon"><Star /></el-icon>
            <div>
              <div class="rule-title">积分获取</div>
              <div class="rule-desc">每日签到：+1 积分</div>
              <div class="rule-desc">每预约2小时：+1 积分（预约座位状态变为"使用中"后自动赠送）</div>
              <div class="rule-desc">长期租赁每日奖励：+15 积分（每日00:00自动赠送，需有有效的长期租赁订单）</div>
            </div>
          </div>
          <div class="rule-item">
            <el-icon class="rule-icon"><ShoppingBag /></el-icon>
            <div>
              <div class="rule-title">积分使用</div>
              <div class="rule-desc">30 积分 = 2 小时学习时长</div>
              <div class="rule-desc">仅支持以 30 的倍数进行兑换（如：60 积分 = 4 小时）</div>
              <div class="rule-desc">积分兑换预约不跳转支付宝，直接扣除积分</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 可兑换时长提示 -->
      <el-alert
        v-if="pointsInfo.exchangeableHours > 0"
        :title="`您当前可兑换 ${pointsInfo.exchangeableHours} 小时学习时长`"
        type="success"
        :closable="false"
        style="margin-top: 20px"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Star, ShoppingBag } from '@element-plus/icons-vue'
import { getPointsInfo, dailySignIn } from '@/api/points'

const pointsInfo = ref({
  totalPoints: 0,
  hasSignedIn: false,
  exchangeableHours: 0
})
const signingIn = ref(false)

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

const handleSignIn = async () => {
  if (pointsInfo.value.hasSignedIn) {
    ElMessage.warning('今日已签到，请明天再来')
    return
  }

  signingIn.value = true
  try {
    const res = await dailySignIn()
    if (res.code === 200) {
      ElMessage.success(res.message || '签到成功，获得1积分')
      await loadPointsInfo()
    }
  } catch (error) {
    ElMessage.error(error.message || '签到失败')
  } finally {
    signingIn.value = false
  }
}

onMounted(() => {
  loadPointsInfo()
})
</script>

<style scoped>
.points-center {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.points-info-card {
  text-align: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
  margin-bottom: 20px;
}

.points-display {
  margin-bottom: 30px;
}

.points-value {
  font-size: 72px;
  font-weight: bold;
  line-height: 1;
  margin-bottom: 10px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.points-label {
  font-size: 18px;
  opacity: 0.9;
}

.points-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
}

.rules-card {
  margin-top: 20px;
}

.rules-header {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.rules-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.rule-item {
  display: flex;
  gap: 15px;
  align-items: flex-start;
}

.rule-icon {
  font-size: 24px;
  color: #409eff;
  margin-top: 2px;
}

.rule-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.rule-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  margin-bottom: 4px;
}
</style>

