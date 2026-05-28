<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <el-icon><Reading /></el-icon>
          <span class="logo-text">逐光自习室</span>
        </div>
        <div class="nav-buttons">
          <el-button text @click="$router.push('/contact')">联系我们</el-button>
          <template v-if="isLogin">
            <el-button type="primary" @click="handleGoToUserCenter">进入用户中心</el-button>
            <el-button @click="handleLogout">退出登录</el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')">登录</el-button>
            <el-button @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>

    <!-- 主要内容区域 -->
    <el-main class="main-content">
      <!-- 欢迎横幅 -->
      <section class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">欢迎来到逐光自习室</h1>
          <p class="hero-subtitle">为您提供安静、舒适、高效的学习环境</p>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="$router.push('/register')">
              立即注册
            </el-button>
            <el-button size="large" @click="$router.push('/login')">
              已有账号，立即登录
            </el-button>
          </div>
        </div>
      </section>

      <!-- 系统公告（游客可见） -->
      <section class="announcements-section" v-if="announcementList.length > 0">
        <div class="announcements-container">
          <h2 class="announcements-title">
            <el-icon><Bell /></el-icon>
            <span>系统公告</span>
          </h2>
          <div class="announcements-list">
            <el-card
              v-for="announcement in announcementList"
              :key="announcement.id"
              class="announcement-card"
              shadow="hover"
            >
              <div class="announcement-header">
                <h3 class="announcement-title">{{ announcement.title }}</h3>
                <span class="announcement-time">{{ formatTime(announcement.createTime) }}</span>
              </div>
              <div class="announcement-content">{{ announcement.content }}</div>
              <div class="announcement-footer" v-if="announcement.publisherName">
                <el-tag size="small" type="info">发布人：{{ announcement.publisherName }}</el-tag>
              </div>
            </el-card>
          </div>
          <div class="announcements-more">
            <el-button type="primary" size="large" @click="$router.push('/announcements')">
              查看更多公告
            </el-button>
          </div>
        </div>
      </section>

      <!-- 功能特色 -->
      <section class="features-section">
        <h2 class="section-title">系统功能特色</h2>
        <el-row :gutter="30">
          <el-col :xs="24" :sm="12" :md="8" v-for="feature in features" :key="feature.title">
            <el-card class="feature-card" shadow="hover">
              <div class="feature-icon">
                <el-icon :size="48" :color="feature.color">
                  <component :is="iconComponents[feature.icon]" />
                </el-icon>
              </div>
              <h3 class="feature-title">{{ feature.title }}</h3>
              <p class="feature-desc">{{ feature.description }}</p>
            </el-card>
          </el-col>
        </el-row>
      </section>

      <!-- 使用说明 -->
      <section class="instructions-section">
        <h2 class="section-title">使用说明</h2>
        <el-timeline>
          <el-timeline-item
            v-for="(step, index) in instructions"
            :key="index"
            :timestamp="step.title"
            placement="top"
          >
            <el-card>
              <p>{{ step.content }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </section>

      <!-- 关于我们 -->
      <section class="about-section">
        <h2 class="section-title">关于逐光自习室</h2>
        <div class="about-content">
          <p>
            逐光自习室致力于为广大学子提供优质的学习环境。我们拥有多个功能完善的自习室，
            配备舒适的座椅、充足的光线、稳定的网络和安静的环境，让您能够全身心地投入到学习中。
          </p>
          <p>
            通过本系统，您可以方便地查看自习室状态、预约座位、管理个人信息，享受便捷的
            自习室服务体验。
          </p>
        </div>
      </section>
    </el-main>

    <!-- 底部 -->
    <el-footer class="footer">
      <div class="footer-content">
        <p>&copy; 2024 逐光自习室管理系统. All rights reserved.</p>
        <p style="margin-top: 10px;">
          <el-link type="info" @click="$router.push('/contact')" style="color: rgba(255, 255, 255, 0.8);">
            联系我们
          </el-link>
        </p>
      </div>
    </el-footer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Reading,
  User,
  OfficeBuilding,
  Calendar,
  Document,
  Clock,
  Money,
  Notebook,
  Bell
} from '@element-plus/icons-vue'
import { getAnnouncementList } from '@/api/announcement'
import { getCurrentUserInfo } from '@/api/user'
import { useUserStore } from '@/stores'
import dayjs from 'dayjs'

const iconComponents = {
  User,
  OfficeBuilding,
  Calendar,
  Document,
  Notebook,
  Clock
}

const features = ref([
  {
    icon: 'User',
    title: '用户管理',
    description: '注册登录、个人信息管理，方便快捷',
    color: '#409EFF'
  },
  {
    icon: 'OfficeBuilding',
    title: '自习室查看',
    description: '实时查看自习室状态，了解座位情况',
    color: '#67C23A'
  },
  {
    icon: 'Calendar',
    title: '在线预约',
    description: '一键预约空闲自习室，支持在线支付',
    color: '#E6A23C'
  },
  {
    icon: 'Document',
    title: '长期租赁',
    description: '申请长期租赁，享受专属学习空间',
    color: '#F56C6C'
  },
  {
    icon: 'Notebook',
    title: '使用记录',
    description: '查看历史使用记录，追踪学习轨迹',
    color: '#909399'
  },
  {
    icon: 'Clock',
    title: '系统公告',
    description: '及时获取最新公告和通知信息',
    color: '#606266'
  }
])

const instructions = ref([
  {
    title: '第一步：注册账号',
    content: '如果您是新用户，请先完成注册，填写基本信息创建账号。'
  },
  {
    title: '第二步：查看自习室',
    content: '登录后，您可以查看所有自习室的当前状态，包括空闲、已预约或被长期租赁的情况。'
  },
  {
    title: '第三步：预约使用',
    content: '选择空闲的自习室，选择使用时间段，完成在线支付即可预约成功。'
  },
  {
    title: '第四步：按时使用',
    content: '在预约的时间段内到达自习室，使用您预约的座位进行学习。'
  },
  {
    title: '第五步：查看记录',
    content: '系统会自动记录您的使用情况，您可以随时查看历史使用记录。'
  }
])

const router = useRouter()
const userStore = useUserStore()

const isLogin = computed(() => userStore.isLogin)
const announcementList = ref([])

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const loadAnnouncements = async () => {
  try {
    const res = await getAnnouncementList()
    if (res.code === 200 && res.data) {
      // 只显示最新的3条公告
      announcementList.value = res.data.slice(0, 3)
    }
  } catch (error) {
    // 静默失败，不影响首页显示
    console.error('加载公告失败:', error)
  }
}

const handleGoToUserCenter = async () => {
  // 如果有token但userInfo为空，先获取用户信息
  if (userStore.isLogin && !userStore.userInfo) {
    try {
      const res = await getCurrentUserInfo()
      if (res.code === 200 && res.data) {
        userStore.setUserInfo(res.data)
      }
    } catch (error) {
      ElMessage.error('获取用户信息失败，请重新登录')
      userStore.logout()
      router.push('/login')
      return
    }
  }
  
  // 根据角色跳转，如果没有userInfo则让路由守卫处理
  if (userStore.userInfo?.role === 1) {
    router.push('/admin')
  } else {
    router.push('/user')
  }
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #409EFF;
}

.header {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.logo-icon {
  margin-right: 10px;
}

.logo-text {
  margin-left: 8px;
}

.main-content {
  flex: 1;
  padding: 0;
  background: transparent;
}

.hero-section {
  padding: 80px 20px;
  text-align: center;
  color: white;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.hero-subtitle {
  font-size: 24px;
  margin-bottom: 40px;
  opacity: 0.9;
}

.hero-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
  flex-wrap: wrap;
}

.announcements-section {
  padding: 60px 20px;
  background: white;
  margin-top: 0;
  border-radius: 20px 20px 0 0;
}

.announcements-container {
  max-width: 1200px;
  margin: 0 auto;
}

.announcements-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 40px;
  color: #303133;
}

.announcements-title .el-icon {
  font-size: 36px;
  color: #409EFF;
}

.announcements-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.announcement-card {
  border-left: 4px solid #409EFF;
  transition: all 0.3s;
}

.announcement-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 10px;
}

.announcement-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  flex: 1;
  min-width: 200px;
}

.announcement-time {
  font-size: 14px;
  color: #909399;
  white-space: nowrap;
}

.announcement-content {
  color: #606266;
  line-height: 1.8;
  margin-bottom: 15px;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.announcement-footer {
  display: flex;
  justify-content: flex-end;
}

.announcements-more {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.features-section,
.instructions-section,
.about-section {
  padding: 60px 20px;
  background: white;
  margin-top: 40px;
}

.features-section:first-of-type {
  margin-top: 0;
  border-radius: 20px 20px 0 0;
}

.section-title {
  text-align: center;
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 50px;
  color: #303133;
}

.feature-card {
  text-align: center;
  margin-bottom: 20px;
}

.feature-card:hover {
}

.feature-icon {
  margin-bottom: 20px;
}

.feature-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #303133;
}

.feature-desc {
  color: #606266;
  line-height: 1.6;
}

.instructions-section {
  background: #f5f7fa;
}

.about-content {
  max-width: 900px;
  margin: 0 auto;
  text-align: center;
}

.about-content p {
  font-size: 16px;
  line-height: 2;
  color: #606266;
  margin-bottom: 20px;
}

.footer {
  background: rgba(48, 49, 51, 0.9);
  color: white;
  text-align: center;
  padding: 30px 0;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }

  .hero-subtitle {
    font-size: 18px;
  }

  .section-title {
    font-size: 28px;
  }
}
</style>

