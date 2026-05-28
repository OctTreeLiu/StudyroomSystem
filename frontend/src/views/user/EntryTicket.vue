<template>
  <div class="entry-ticket">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="entryTicket" class="ticket-container">
      <el-card :class="['ticket-card', getMemberCardClass(entryTicket.memberType)]">
        <div class="ticket-header">
          <h2>入场凭证</h2>
          <el-tag :type="getStatusType(entryTicket.status)" size="large">
            {{ entryTicket.status }}
          </el-tag>
        </div>
        
        <div class="ticket-content">
          <div class="info-section">
            <div class="info-item">
              <span class="label">当前时间：</span>
              <span class="value">{{ localNow }}</span>
            </div>
            <div class="info-item">
              <span class="label">用户名：</span>
              <span class="value">{{ entryTicket.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">自习室：</span>
              <span class="value">{{ entryTicket.roomName }}（{{ entryTicket.roomNumber }}）</span>
            </div>
            <div class="info-item">
              <span class="label">座位：</span>
              <span class="value">{{ entryTicket.seatNumber }}{{ entryTicket.seatName ? ` - ${entryTicket.seatName}` : '' }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ entryTicket.type === 'lease' ? '租赁时间' : '预约时间' }}：</span>
              <span class="value">
                {{ formatDateTime(entryTicket.startTime) }} 至 {{ formatDateTime(entryTicket.endTime) }}
              </span>
            </div>
            <div class="info-item">
              <span class="label">{{ entryTicket.type === 'lease' ? '租赁编号' : '预约编号' }}：</span>
              <span class="value">{{ entryTicket.type === 'lease' ? entryTicket.leaseNumber : entryTicket.reservationNumber }}</span>
            </div>
            <div class="info-item" v-if="entryTicket.memberType !== null && entryTicket.memberType !== undefined && entryTicket.memberType > 0">
              <span class="label">会员类型：</span>
              <span class="value">
                <el-tag :type="getMemberTagType(entryTicket.memberType)" size="small">
                  {{ getMemberTypeText(entryTicket.memberType) }}
                </el-tag>
                <span v-if="entryTicket.memberExpireTime" style="margin-left: 10px; color: #909399; font-size: 13px">
                  （到期时间：{{ formatDateTime(entryTicket.memberExpireTime) }}）
                </span>
              </span>
            </div>
          </div>
        </div>
        
        <div class="ticket-footer">
          <el-button type="primary" @click="refreshTicket">刷新凭证</el-button>
          <el-button v-if="entryTicket.type === 'lease'" @click="$router.push('/user/lease')">查看我的租赁</el-button>
          <el-button v-else @click="$router.push('/user/reservation')">查看我的预约</el-button>
        </div>
      </el-card>
    </div>
    
    <div v-else class="no-ticket-container">
      <el-card class="no-ticket-card">
        <div class="no-ticket-content">
          <el-icon class="no-ticket-icon" :size="80"><DocumentRemove /></el-icon>
          <h3 class="no-ticket-title">您还没有有效的入场凭证</h3>
          <p class="no-ticket-desc">请先预约座位或申请长期租赁后再查看入场凭证</p>
          <div class="no-ticket-actions">
            <el-button type="primary" size="large" @click="$router.push('/user/reservation/create')">
              立即预约
            </el-button>
            <el-button size="large" @click="$router.push('/user/reservation')">
              查看我的预约
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentRemove } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getEntryTicket } from '@/api/reservation'

const loading = ref(false)
const entryTicket = ref(null)
const localNow = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'))
let refreshTimer = null
let clockTimer = null

const tickLocalNow = () => {
  localNow.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
}

const loadEntryTicket = async () => {
  loading.value = true
  try {
    const res = await getEntryTicket()
    if (res.code === 200 && res.data) {
      entryTicket.value = res.data
    } else {
      // 无预约是正常情况，不显示弹窗，只显示页面提示
      entryTicket.value = null
    }
  } catch (error) {
    // 所有错误都不显示弹窗，只显示页面上的提示
    entryTicket.value = null
  } finally {
    loading.value = false
  }
}

const refreshTicket = () => {
  loadEntryTicket()
  ElMessage.success('凭证已刷新')
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm')
}

const getStatusType = (status) => {
  if (status === '可入场') {
    return 'success'
  } else if (status === '即将开始') {
    return 'warning'
  } else {
    return 'info'
  }
}

const getMemberTypeText = (memberType) => {
  if (memberType === 1) {
    return 'VIP会员'
  } else if (memberType === 2) {
    return 'SVIP会员'
  } else {
    return '普通用户'
  }
}

const getMemberTagType = (memberType) => {
  if (memberType === 1) {
    return 'warning'
  } else if (memberType === 2) {
    return 'danger'
  } else {
    return 'info'
  }
}

const getMemberCardClass = (memberType) => {
  if (memberType === 1) {
    return 'ticket-card-vip'
  } else if (memberType === 2) {
    return 'ticket-card-svip'
  } else {
    return ''
  }
}

// 当前时间每秒刷新；凭证数据每30秒自动拉取一次
onMounted(() => {
  tickLocalNow()
  clockTimer = setInterval(tickLocalNow, 1000)
  loadEntryTicket()
  refreshTimer = setInterval(() => {
    loadEntryTicket()
  }, 30000) // 30秒刷新一次
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  if (clockTimer) {
    clearInterval(clockTimer)
  }
})
</script>

<style scoped>
.entry-ticket {
  min-height: 500px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 20px;
}

.loading-container {
  width: 100%;
  max-width: 600px;
}

.ticket-container {
  width: 100%;
  max-width: 600px;
}

.ticket-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.ticket-card-vip {
  border: 3px solid #f56c6c;
  box-shadow: 0 4px 20px rgba(245, 108, 108, 0.3);
  background: linear-gradient(135deg, #fff5f5 0%, #ffffff 100%);
}

.ticket-card-vip .ticket-header {
  border-bottom-color: #f56c6c;
}

.ticket-card-vip .ticket-header h2 {
  color: #f56c6c;
}

.ticket-card-svip {
  border: 3px solid #ffd700;
  box-shadow: 0 4px 20px rgba(255, 215, 0, 0.4);
  background: linear-gradient(135deg, #fffef0 0%, #ffffff 100%);
}

.ticket-card-svip .ticket-header {
  border-bottom-color: #ffd700;
}

.ticket-card-svip .ticket-header h2 {
  color: #d4af37;
  text-shadow: 1px 1px 2px rgba(212, 175, 55, 0.3);
}

.ticket-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e4e7ed;
}

.ticket-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
  font-weight: bold;
}

.ticket-content {
  margin: 30px 0;
}

.info-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.info-item:hover {
  background: #ecf5ff;
}

.info-item .label {
  font-weight: 600;
  color: #606266;
  min-width: 100px;
  font-size: 15px;
}

.info-item .value {
  color: #303133;
  font-size: 15px;
  flex: 1;
}

.ticket-footer {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.no-ticket-container {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.no-ticket-card {
  width: 100%;
  max-width: 500px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.no-ticket-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  text-align: center;
}

.no-ticket-icon {
  color: #909399;
  margin-bottom: 20px;
}

.no-ticket-title {
  margin: 0 0 15px 0;
  font-size: 20px;
  color: #303133;
  font-weight: 600;
}

.no-ticket-desc {
  margin: 0 0 30px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.no-ticket-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

:deep(.el-card__body) {
  padding: 30px;
}

:deep(.el-empty) {
  padding: 40px 0;
}
</style>

