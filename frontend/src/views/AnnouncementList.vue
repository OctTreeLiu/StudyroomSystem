<template>
  <div class="announcement-list-page">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <el-icon><Reading /></el-icon>
          <span class="logo-text">逐光自习室</span>
        </div>
        <div class="nav-buttons">
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
      <div class="announcement-container">
        <div class="page-header">
          <h1 class="page-title">
            <el-icon><Bell /></el-icon>
            <span>系统公告</span>
          </h1>
          <el-button @click="$router.push('/')">返回首页</el-button>
        </div>

        <el-card class="announcement-card-container">
          <div v-loading="loading">
            <el-empty v-if="!loading && announcementList.length === 0" description="暂无公告" />
            
            <template v-else>
              <div class="announcements-list">
                <el-card
                  v-for="announcement in announcementList"
                  :key="announcement.id"
                  class="announcement-item"
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
              
              <!-- 分页组件 -->
              <div v-if="pagination.total > 0" class="pagination-container">
                <el-pagination
                  v-model:current-page="pagination.page"
                  v-model:page-size="pagination.pageSize"
                  :total="pagination.total"
                  :page-sizes="[10, 20, 30, 50]"
                  layout="total, sizes, prev, pager, next, jumper"
                  @current-change="handlePageChange"
                  @size-change="handleSizeChange"
                />
              </div>
            </template>
          </div>
        </el-card>
      </div>
    </el-main>

    <!-- 底部 -->
    <el-footer class="footer">
      <div class="footer-content">
        <p>&copy; 2024 逐光自习室管理系统. All rights reserved.</p>
      </div>
    </el-footer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Reading, Bell } from '@element-plus/icons-vue'
import { getAnnouncementListWithPagination } from '@/api/announcement'
import { useUserStore } from '@/stores'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const isLogin = computed(() => userStore.isLogin)
const loading = ref(false)
const announcementList = ref([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const loadAnnouncements = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementListWithPagination(pagination.value.page, pagination.value.pageSize)
    if (res.code === 200 && res.data) {
      announcementList.value = res.data.list || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error(error.message || '加载公告失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  pagination.value.page = page
  loadAnnouncements()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.page = 1
  loadAnnouncements()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleGoToUserCenter = () => {
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
.announcement-list-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
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
  cursor: pointer;
}

.logo-text {
  margin-left: 8px;
}

.main-content {
  flex: 1;
  padding: 40px 20px;
  background: transparent;
}

.announcement-container {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.page-title .el-icon {
  font-size: 32px;
  color: #409EFF;
}

.announcement-card-container {
  border-radius: 8px;
}

.announcements-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 30px;
}

.announcement-item {
  border-left: 4px solid #409EFF;
  transition: all 0.3s;
}

.announcement-item:hover {
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

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  padding: 20px 0;
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
</style>

