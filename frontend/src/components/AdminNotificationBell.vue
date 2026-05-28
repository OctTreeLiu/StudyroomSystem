<template>
  <div class="notification-bell">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
      <el-button
        :icon="Bell"
        circle
        :class="{ 'bell-highlight': unreadCount > 0 }"
        @click="handleBellClick"
      />
    </el-badge>

    <!-- 新消息弹窗 -->
    <el-dialog
      v-model="showNotificationPopup"
      :title="currentNotification?.title || '新消息'"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      :modal="false"
      class="notification-popup"
      top="20px"
    >
      <div class="popup-content">
        <p>{{ currentNotification?.content || '' }}</p>
        <div class="popup-time">
          {{ formatTime(currentNotification?.createTime) }}
        </div>
      </div>
    </el-dialog>

    <!-- 消息抽屉 -->
    <el-drawer
      v-model="showNotificationDrawer"
      title="消息通知"
      :size="400"
      direction="rtl"
      @open="handleDrawerOpen"
    >
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center; width: 100%">
          <span>消息通知</span>
          <el-button
            v-if="unreadCount > 0"
            text
            size="small"
            @click="handleMarkAllAsRead"
          >
            全部标记为已读
          </el-button>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="notificationList.length === 0" description="暂无消息" />
        <div v-else>
          <div
            v-for="notification in notificationList"
            :key="notification.id"
            :class="['notification-item', { 'unread': notification.isRead === 0 }]"
            @click="handleMarkAsRead(notification)"
          >
            <div class="notification-header">
              <el-tag :type="notification.type === 1 ? 'warning' : 'info'" size="small">
                {{ notification.type === 1 ? '预约提醒' : '系统通知' }}
              </el-tag>
              <span class="notification-time">{{ formatTime(notification.createTime) }}</span>
            </div>
            <div class="notification-title">{{ notification.title }}</div>
            <div class="notification-content">{{ notification.content }}</div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import { getAdminNotifications, getAdminUnreadCount, markAsRead, markAllAsRead } from '@/api/admin/notification'

const showNotificationDrawer = ref(false)
const loading = ref(false)
const notificationList = ref([])
const unreadCount = ref(0)
const showNotificationPopup = ref(false)
const currentNotification = ref(null)
const hasNewMessage = ref(false)
let pollTimer = null
let popupTimer = null
let lastUnreadCount = 0

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const loadNotifications = async () => {
  // 如果抽屉未打开，不显示loading，避免闪烁
  if (!showNotificationDrawer.value) {
    loading.value = false
  } else {
    loading.value = true
  }
  try {
    const res = await getAdminNotifications()
    if (res.code === 200) {
      notificationList.value = res.data || []
    }
  } catch (error) {
    console.error('加载消息列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const res = await getAdminUnreadCount()
    if (res.code === 200) {
      const newUnreadCount = res.data?.unreadCount || 0
      
      // 检测到新消息（未读数量增加）
      if (newUnreadCount > lastUnreadCount) {
        // 获取最新的未读消息
        await loadLatestUnreadNotification()
        // 标记有新消息，用于高亮显示
        hasNewMessage.value = true
        // 3秒后取消高亮（如果用户没有点击）
        setTimeout(() => {
          hasNewMessage.value = false
        }, 3000)
      }
      
      unreadCount.value = newUnreadCount
      lastUnreadCount = newUnreadCount
    }
  } catch (error) {
    console.error('加载未读消息数量失败:', error)
  }
}

const loadLatestUnreadNotification = async () => {
  try {
    const res = await getAdminNotifications()
    if (res.code === 200 && res.data && res.data.length > 0) {
      // 找到最新的未读消息
      const unreadNotifications = res.data.filter(n => n.isRead === 0)
      if (unreadNotifications.length > 0) {
        // 按创建时间排序，取最新的
        unreadNotifications.sort((a, b) => {
          return new Date(b.createTime) - new Date(a.createTime)
        })
        currentNotification.value = unreadNotifications[0]
        showNotificationPopup.value = true
        
        // 5秒后自动关闭
        if (popupTimer) {
          clearTimeout(popupTimer)
        }
        popupTimer = setTimeout(() => {
          showNotificationPopup.value = false
          currentNotification.value = null
        }, 5000)
      }
    }
  } catch (error) {
    console.error('加载最新消息失败:', error)
  }
}

const handleMarkAsRead = async (notification) => {
  if (notification.isRead === 1) {
    return
  }
  
  try {
    const res = await markAsRead(notification.id)
    if (res.code === 200) {
      notification.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  } catch (error) {
    console.error('标记消息已读失败:', error)
  }
}

const handleMarkAllAsRead = async () => {
  try {
    const res = await markAllAsRead()
    if (res.code === 200) {
      notificationList.value.forEach(n => {
        n.isRead = 1
      })
      unreadCount.value = 0
      ElMessage.success('已标记所有消息为已读')
    }
  } catch (error) {
    ElMessage.error('标记消息失败')
  }
}

const handleBellClick = () => {
  // 点击铃铛时取消新消息高亮
  hasNewMessage.value = false
  showNotificationDrawer.value = true
}

const handleDrawerOpen = () => {
  // 打开抽屉时刷新消息列表和未读数量
  loadNotifications()
  loadUnreadCount()
}

// 定时轮询未读消息数量和通知列表（每5秒）
const startPolling = () => {
  pollTimer = setInterval(() => {
    loadUnreadCount()
    // 如果通知抽屉已打开，同时刷新通知列表
    if (showNotificationDrawer.value) {
      loadNotifications()
    }
  }, 5000) // 5秒轮询一次，确保通知能及时显示
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onMounted(() => {
  loadNotifications()
  loadUnreadCount().then(() => {
    // 初始化完成后，记录初始未读数量，避免首次加载时显示弹窗
    lastUnreadCount = unreadCount.value
  })
  startPolling()
})

onUnmounted(() => {
  stopPolling()
  if (popupTimer) {
    clearTimeout(popupTimer)
  }
})
</script>

<style scoped>
.notification-bell {
  margin-right: 10px;
}

.notification-item {
  padding: 15px;
  margin-bottom: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #ecf5ff;
  border-color: #b3d8ff;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.notification-title {
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 5px;
  color: #303133;
}

.notification-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.notification-popup {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 3000;
}

:deep(.notification-popup .el-dialog) {
  margin: 0;
  position: fixed;
  top: 20px;
  right: 20px;
}

:deep(.notification-popup .el-dialog__header) {
  background: #409EFF;
  color: white;
  padding: 15px 20px;
}

:deep(.notification-popup .el-dialog__title) {
  color: white;
  font-weight: 600;
}

:deep(.notification-popup .el-dialog__body) {
  padding: 20px;
}

.popup-content {
  text-align: center;
}

.popup-content p {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}

.popup-time {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}

.bell-highlight {
  background: linear-gradient(135deg, #f56c6c 0%, #ff7875 100%) !important;
  color: white !important;
  animation: bell-pulse 2s infinite;
  box-shadow: 0 0 10px rgba(245, 108, 108, 0.5);
}

@keyframes bell-pulse {
  0%, 100% {
    box-shadow: 0 0 10px rgba(245, 108, 108, 0.5);
  }
  50% {
    box-shadow: 0 0 20px rgba(245, 108, 108, 0.8);
  }
}
</style>

