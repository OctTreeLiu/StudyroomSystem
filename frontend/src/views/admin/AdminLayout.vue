<template>
  <div class="admin-layout">
    <div class="header">
      <div class="header-content">
        <el-button
          v-if="isMobile"
          text
          class="mobile-menu-btn"
          @click="mobileMenuVisible = true"
          aria-label="打开菜单"
        >
          <el-icon><Menu /></el-icon>
        </el-button>
        <div class="logo">
          <el-icon><Setting /></el-icon>
          <span>逐光自习室 - 管理员后台</span>
        </div>
        <div class="user-info">
          <el-button text @click="$router.push('/contact')" style="color: white;">联系我们</el-button>
          <AdminNotificationBell />
          <el-avatar
            :size="36"
            :src="userInfo?.avatarUrl"
            :icon="User"
            @click="$router.push('/admin')"
          >
            <template #default>
              <span>{{ userInfo?.username?.charAt(0)?.toUpperCase() }}</span>
            </template>
          </el-avatar>
          <span>管理员：{{ userInfo?.username }}</span>
          <el-button text @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </div>
    <div class="body-container">
      <div v-if="!isMobile" class="sidebar">
        <div class="menu-item" :class="{ active: activeMenu === '/admin' }" @click="$router.push('/admin')">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/user' }" @click="$router.push('/admin/user')">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/studyroom' }" @click="$router.push('/admin/studyroom')">
          <el-icon><OfficeBuilding /></el-icon>
          <span>自习室管理</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/seat' }" @click="$router.push('/admin/seat')">
          <el-icon><Grid /></el-icon>
          <span>座位管理</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/reservation' }" @click="$router.push('/admin/reservation')">
          <el-icon><Calendar /></el-icon>
          <span>预约情况</span>
        </div>
        <div 
          class="menu-item" 
          :class="{ 
            active: activeMenu === '/admin/lease',
            'active-highlight': pendingLeaseCount > 0
          }" 
          @click="$router.push('/admin/lease')"
        >
          <el-icon><DocumentCopy /></el-icon>
          <span>长期租赁情况</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/announcement' }" @click="$router.push('/admin/announcement')">
          <el-icon><Document /></el-icon>
          <span>公告管理</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/message-board' }" @click="$router.push('/admin/message-board')">
          <el-icon><ChatLineRound /></el-icon>
          <span>留言板管理</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/statistics' }" @click="$router.push('/admin/statistics')">
          <el-icon><DataAnalysis /></el-icon>
          <span>统计分析</span>
        </div>
        <div 
          class="menu-item" 
          :class="{ 
            active: activeMenu === '/admin/call',
            'active-highlight': hasUnprocessedCall > 0
          }" 
          @click="$router.push('/admin/call')"
        >
          <el-icon><Phone /></el-icon>
          <span>呼叫记录</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/study-time' }" @click="$router.push('/admin/study-time')">
          <el-icon><DataAnalysis /></el-icon>
          <span>学习时长统计</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/points' }" @click="$router.push('/admin/points')">
          <el-icon><Star /></el-icon>
          <span>积分管理</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/price-config' }" @click="$router.push('/admin/price-config')">
          <el-icon><Money /></el-icon>
          <span>定价管理</span>
        </div>
        <div 
          class="menu-item" 
          :class="{ 
            active: activeMenu === '/admin/memo',
            'highlight-memo': hasUnprocessedMemoToday
          }" 
          @click="$router.push('/admin/memo')"
        >
          <el-icon><EditPen /></el-icon>
          <span>备忘录</span>
        </div>
        <div 
          class="menu-item" 
          :class="{ 
            active: activeMenu === '/admin/extra-charge',
            'active-highlight': hasUnpaidExtraCharge
          }" 
          @click="$router.push('/admin/extra-charge')"
        >
          <el-icon><Money /></el-icon>
          <span>额外收费管理</span>
        </div>
        <div 
          class="menu-item" 
          :class="{ active: activeMenu === '/admin/user-modify-log' }" 
          @click="$router.push('/admin/user-modify-log')"
        >
          <el-icon><Document /></el-icon>
          <span>修改日志</span>
        </div>
      </div>
      <div class="main-content">
        <router-view />
      </div>
    </div>

    <!-- 移动端抽屉菜单 -->
    <el-drawer
      v-model="mobileMenuVisible"
      direction="ltr"
      size="240px"
      :with-header="false"
    >
      <div class="drawer-menu">
        <div class="menu-item" :class="{ active: activeMenu === '/admin' }" @click="go('/admin')">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/user' }" @click="go('/admin/user')">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/studyroom' }" @click="go('/admin/studyroom')">
          <el-icon><OfficeBuilding /></el-icon>
          <span>自习室管理</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/seat' }" @click="go('/admin/seat')">
          <el-icon><Grid /></el-icon>
          <span>座位管理</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/reservation' }" @click="go('/admin/reservation')">
          <el-icon><Calendar /></el-icon>
          <span>预约情况</span>
        </div>
        <div
          class="menu-item"
          :class="{
            active: activeMenu === '/admin/lease',
            'active-highlight': pendingLeaseCount > 0
          }"
          @click="go('/admin/lease')"
        >
          <el-icon><DocumentCopy /></el-icon>
          <span>长期租赁情况</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/announcement' }" @click="go('/admin/announcement')">
          <el-icon><Document /></el-icon>
          <span>公告管理</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/message-board' }" @click="go('/admin/message-board')">
          <el-icon><ChatLineRound /></el-icon>
          <span>留言板管理</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/statistics' }" @click="go('/admin/statistics')">
          <el-icon><DataAnalysis /></el-icon>
          <span>统计分析</span>
        </div>
        <div
          class="menu-item"
          :class="{
            active: activeMenu === '/admin/call',
            'active-highlight': hasUnprocessedCall > 0
          }"
          @click="go('/admin/call')"
        >
          <el-icon><Phone /></el-icon>
          <span>呼叫记录</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/study-time' }" @click="go('/admin/study-time')">
          <el-icon><DataAnalysis /></el-icon>
          <span>学习时长统计</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/points' }" @click="go('/admin/points')">
          <el-icon><Star /></el-icon>
          <span>积分管理</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/admin/price-config' }" @click="go('/admin/price-config')">
          <el-icon><Money /></el-icon>
          <span>定价管理</span>
        </div>
        <div
          class="menu-item"
          :class="{
            active: activeMenu === '/admin/memo',
            'highlight-memo': hasUnprocessedMemoToday
          }"
          @click="go('/admin/memo')"
        >
          <el-icon><EditPen /></el-icon>
          <span>备忘录</span>
        </div>
        <div
          class="menu-item"
          :class="{
            active: activeMenu === '/admin/extra-charge',
            'active-highlight': hasUnpaidExtraCharge
          }"
          @click="go('/admin/extra-charge')"
        >
          <el-icon><Money /></el-icon>
          <span>额外收费管理</span>
        </div>
        <div
          class="menu-item"
          :class="{ active: activeMenu === '/admin/user-modify-log' }"
          @click="go('/admin/user-modify-log')"
        >
          <el-icon><Document /></el-icon>
          <span>修改日志</span>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Setting, HomeFilled, User, OfficeBuilding, Grid, Calendar, DocumentCopy, Document, DataAnalysis, ChatLineRound, Phone, Star, Money, EditPen, Menu } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'
import AdminNotificationBell from '@/components/AdminNotificationBell.vue'
import { getPendingLeaseCount } from '@/api/admin/lease'
import { checkTodayUnprocessed } from '@/api/memo'
import { countUnpaidExtraChargeOrders } from '@/api/extraCharge'
import { getPendingCallCount } from '@/api/adminCall'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)
const activeMenu = computed(() => route.path)

const pendingLeaseCount = ref(0)
let leasePollTimer = null
const hasUnprocessedMemoToday = ref(false)
let memoCheckTimer = null
const hasUnpaidExtraCharge = ref(false)
let extraChargeCheckTimer = null
const hasUnprocessedCall = ref(0)
let callCheckTimer = null

const isMobile = ref(false)
const mobileMenuVisible = ref(false)

const updateIsMobile = () => {
  isMobile.value = window.matchMedia('(max-width: 768px)').matches
  if (!isMobile.value) {
    mobileMenuVisible.value = false
  }
}

const go = (path) => {
  mobileMenuVisible.value = false
  router.push(path)
}

const checkUnprocessedMemo = async () => {
  try {
    const res = await checkTodayUnprocessed()
    if (res.code === 200) {
      hasUnprocessedMemoToday.value = res.data?.hasUnprocessed || false
    }
  } catch (error) {
    console.error('检查未处理备忘录失败:', error)
  }
}

const startMemoCheck = () => {
  checkUnprocessedMemo()
  // 每5分钟检查一次
  memoCheckTimer = setInterval(() => {
    checkUnprocessedMemo()
  }, 300000)
  
  // 监听备忘录更新事件
  window.addEventListener('memo-updated', checkUnprocessedMemo)
}

const stopMemoCheck = () => {
  if (memoCheckTimer) {
    clearInterval(memoCheckTimer)
    memoCheckTimer = null
  }
  window.removeEventListener('memo-updated', checkUnprocessedMemo)
}

const loadPendingLeaseCount = async () => {
  try {
    const res = await getPendingLeaseCount()
    if (res.code === 200) {
      pendingLeaseCount.value = res.data || 0
    }
  } catch (e) {
    // ignore
  }
}

const checkUnpaidExtraCharge = async () => {
  try {
    const res = await countUnpaidExtraChargeOrders()
    if (res.code === 200) {
      hasUnpaidExtraCharge.value = (res.data?.count || 0) > 0
    }
  } catch (error) {
    console.error('检查未支付订单失败:', error)
  }
}

const startExtraChargeCheck = () => {
  checkUnpaidExtraCharge()
  // 每5秒检查一次
  extraChargeCheckTimer = setInterval(() => {
    checkUnpaidExtraCharge()
  }, 5000)
  
  // 监听额外收费更新事件
  window.addEventListener('extra-charge-updated', checkUnpaidExtraCharge)
}

const stopExtraChargeCheck = () => {
  if (extraChargeCheckTimer) {
    clearInterval(extraChargeCheckTimer)
    extraChargeCheckTimer = null
  }
  window.removeEventListener('extra-charge-updated', checkUnpaidExtraCharge)
}

const checkUnprocessedCall = async () => {
  try {
    const res = await getPendingCallCount()
    if (res.code === 200) {
      hasUnprocessedCall.value = res.data?.count || 0
    }
  } catch (error) {
    console.error('检查未处理呼叫记录失败:', error)
  }
}

const startCallCheck = () => {
  checkUnprocessedCall()
  // 每5秒检查一次
  callCheckTimer = setInterval(() => {
    checkUnprocessedCall()
  }, 5000)
  
  // 监听呼叫记录更新事件
  window.addEventListener('call-updated', checkUnprocessedCall)
}

const stopCallCheck = () => {
  if (callCheckTimer) {
    clearInterval(callCheckTimer)
    callCheckTimer = null
  }
  window.removeEventListener('call-updated', checkUnprocessedCall)
}

const startLeasePolling = () => {
  leasePollTimer = setInterval(() => {
    loadPendingLeaseCount()
  }, 30000)
}

const stopLeasePolling = () => {
  if (leasePollTimer) {
    clearInterval(leasePollTimer)
    leasePollTimer = null
  }
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}

onMounted(() => {
  updateIsMobile()
  window.addEventListener('resize', updateIsMobile)
  loadPendingLeaseCount()
  startLeasePolling()
  startMemoCheck()
  startExtraChargeCheck()
  startCallCheck()
})

onUnmounted(() => {
  window.removeEventListener('resize', updateIsMobile)
  stopLeasePolling()
  stopMemoCheck()
  stopExtraChargeCheck()
  stopCallCheck()
})
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.header {
  height: 60px;
  background: #409EFF;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mobile-menu-btn {
  color: white;
  margin-right: 6px;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  color: white;
  gap: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  color: white;
  font-size: 14px;
}

.user-info .el-button {
  color: white;
}

.body-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: 180px;
  background: white;
  border-right: 1px solid #e4e7ed;
  padding: 10px 0;
  overflow-y: auto;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin: 2px 8px;
  color: #606266;
  cursor: pointer;
  border-radius: 4px;
  font-size: 14px;
}

.menu-item:hover {
  background: #f5f7fa;
}


.menu-item.active {
  background: #409EFF;
  color: white;
}

.menu-item.highlight-memo {
  background: #f56c6c;
  color: white;
  font-weight: bold;
  animation: pulse-memo 2s infinite;
}

.menu-item.highlight-memo:hover {
  background: #f56c6c;
  opacity: 0.9;
}

.menu-item.active-highlight {
  background: #e6a23c;
  color: white;
  font-weight: bold;
  animation: pulse-extra-charge 2s infinite;
}

.menu-item.active-highlight:hover {
  background: #e6a23c;
  opacity: 0.9;
}

.menu-item.active-highlight.active {
  background: #e6a23c;
  color: white;
}

@keyframes pulse-memo {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.7);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(245, 108, 108, 0);
  }
}

@keyframes pulse-extra-charge {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(230, 162, 60, 0.7);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(230, 162, 60, 0);
  }
}

.menu-item .el-icon {
  margin-right: 8px;
  font-size: 18px;
}

.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.drawer-menu {
  padding: 10px 0;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 10px;
    gap: 8px;
    max-width: 100%;
    margin: 0;
  }

  .logo {
    font-size: 16px;
    white-space: nowrap;
  }

  .user-info {
    gap: 8px;
    font-size: 12px;
    white-space: nowrap;
  }

  .user-info span {
    display: none;
  }

  .body-container {
    overflow: visible;
    width: 100%;
  }

  .main-content {
    padding: 12px;
    max-width: 100%;
    margin: 0;
  }
}
</style>
