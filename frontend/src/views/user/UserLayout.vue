<template>
  <div class="user-layout">
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
          <el-icon><Reading /></el-icon>
          <span>逐光自习室 - 用户中心</span>
        </div>
        <div class="user-info">
          <el-button text @click="$router.push('/contact')" style="color: white;">联系我们</el-button>
          <NotificationBell />
          <el-avatar
            :size="36"
            :src="userInfo?.avatarUrl"
            :icon="User"
            @click="$router.push('/user/info')"
          >
            <template #default>
              <span>{{ userInfo?.username?.charAt(0)?.toUpperCase() }}</span>
            </template>
          </el-avatar>
          <span>欢迎，{{ userInfo?.username }}</span>
          <el-button text @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </div>
    <div class="body-container">
      <div v-if="!isMobile" class="sidebar">
        <div class="menu-item" :class="{ active: activeMenu === '/user' }" @click="$router.push('/user')">
          <el-icon><Ticket /></el-icon>
          <span>入场凭证</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/info' }" @click="$router.push('/user/info')">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/studyroom' }" @click="$router.push('/user/studyroom')">
          <el-icon><OfficeBuilding /></el-icon>
          <span>自习室列表</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/announcement' }" @click="$router.push('/user/announcement')">
          <el-icon><Document /></el-icon>
          <span>系统公告</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/reservation' }" @click="$router.push('/user/reservation')">
          <el-icon><Calendar /></el-icon>
          <span>我的预约</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/lease' }" @click="$router.push('/user/lease')">
          <el-icon><DocumentCopy /></el-icon>
          <span>长期租赁</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/usage' }" @click="$router.push('/user/usage')">
          <el-icon><Clock /></el-icon>
          <span>使用记录</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/message-board' }" @click="$router.push('/user/message-board')">
          <el-icon><ChatLineRound /></el-icon>
          <span>留言板</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/study-time' }" @click="$router.push('/user/study-time')">
          <el-icon><DataAnalysis /></el-icon>
          <span>学习时长统计</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/study-ranking' }" @click="$router.push('/user/study-ranking')">
          <el-icon><Medal /></el-icon>
          <span>学习红人榜</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/points' || activeMenu === '/user/points/history' || activeMenu === '/user/points/exchange' }" @click="$router.push('/user/points')">
          <el-icon><Star /></el-icon>
          <span>积分中心</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/member' }" @click="$router.push('/user/member')">
          <el-icon><Trophy /></el-icon>
          <span>会员中心</span>
        </div>
        <div 
          class="menu-item" 
          :class="{ 
            active: activeMenu === '/user/memo',
            'highlight-memo': hasUnprocessedMemoToday
          }" 
          @click="$router.push('/user/memo')"
        >
          <el-icon><EditPen /></el-icon>
          <span>备忘录</span>
        </div>
        <div 
          class="menu-item" 
          :class="{ 
            active: activeMenu === '/user/extra-charge',
            'active-highlight': hasUnpaidExtraCharge
          }" 
          @click="$router.push('/user/extra-charge')"
        >
          <el-icon><Money /></el-icon>
          <span>额外收费</span>
        </div>
        <div
          class="menu-item"
          :class="{ active: activeMenu === '/user/ai-agent' }"
          @click="$router.push('/user/ai-agent')"
        >
          <el-icon><ChatLineRound /></el-icon>
          <span>小光智能体</span>
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
        <div class="menu-item" :class="{ active: activeMenu === '/user' }" @click="go('/user')">
          <el-icon><Ticket /></el-icon>
          <span>入场凭证</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/info' }" @click="go('/user/info')">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/studyroom' }" @click="go('/user/studyroom')">
          <el-icon><OfficeBuilding /></el-icon>
          <span>自习室列表</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/announcement' }" @click="go('/user/announcement')">
          <el-icon><Document /></el-icon>
          <span>系统公告</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/reservation' }" @click="go('/user/reservation')">
          <el-icon><Calendar /></el-icon>
          <span>我的预约</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/lease' }" @click="go('/user/lease')">
          <el-icon><DocumentCopy /></el-icon>
          <span>长期租赁</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/usage' }" @click="go('/user/usage')">
          <el-icon><Clock /></el-icon>
          <span>使用记录</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/message-board' }" @click="go('/user/message-board')">
          <el-icon><ChatLineRound /></el-icon>
          <span>留言板</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/study-time' }" @click="go('/user/study-time')">
          <el-icon><DataAnalysis /></el-icon>
          <span>学习时长统计</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/study-ranking' }" @click="go('/user/study-ranking')">
          <el-icon><Medal /></el-icon>
          <span>学习红人榜</span>
        </div>
        <div
          class="menu-item"
          :class="{ active: activeMenu === '/user/points' || activeMenu === '/user/points/history' || activeMenu === '/user/points/exchange' }"
          @click="go('/user/points')"
        >
          <el-icon><Star /></el-icon>
          <span>积分中心</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/member' }" @click="go('/user/member')">
          <el-icon><Trophy /></el-icon>
          <span>会员中心</span>
        </div>
        <div
          class="menu-item"
          :class="{
            active: activeMenu === '/user/memo',
            'highlight-memo': hasUnprocessedMemoToday
          }"
          @click="go('/user/memo')"
        >
          <el-icon><EditPen /></el-icon>
          <span>备忘录</span>
        </div>
        <div
          class="menu-item"
          :class="{
            active: activeMenu === '/user/extra-charge',
            'active-highlight': hasUnpaidExtraCharge
          }"
          @click="go('/user/extra-charge')"
        >
          <el-icon><Money /></el-icon>
          <span>额外收费</span>
        </div>
        <div class="menu-item" :class="{ active: activeMenu === '/user/ai-agent' }" @click="go('/user/ai-agent')">
          <el-icon><ChatLineRound /></el-icon>
          <span>小光智能体</span>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Reading, User, OfficeBuilding, Document, Calendar, DocumentCopy, Clock, Ticket, ChatLineRound, DataAnalysis, Star, Trophy, Medal, EditPen, Money, Menu } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'
import NotificationBell from '@/components/NotificationBell.vue'
import { checkTodayUnprocessed } from '@/api/memo'
import { countMyUnpaidExtraChargeOrders } from '@/api/extraCharge'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)
const activeMenu = computed(() => route.path)
const hasUnprocessedMemoToday = ref(false)
let memoCheckTimer = null
const hasUnpaidExtraCharge = ref(false)
let extraChargeCheckTimer = null

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

const checkUnpaidExtraCharge = async () => {
  try {
    const res = await countMyUnpaidExtraChargeOrders()
    if (res.code === 200) {
      hasUnpaidExtraCharge.value = (res.data?.count || 0) > 0
    }
  } catch (error) {
    console.error('检查未支付订单失败:', error)
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

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}

onMounted(() => {
  updateIsMobile()
  window.addEventListener('resize', updateIsMobile)
  startMemoCheck()
  startExtraChargeCheck()
})

onUnmounted(() => {
  window.removeEventListener('resize', updateIsMobile)
  stopMemoCheck()
  stopExtraChargeCheck()
})
</script>

<style scoped>
.user-layout {
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
  max-width: 1200px;
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
