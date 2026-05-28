<template>
  <div class="announcement-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统公告</span>
          <el-button type="primary" @click="loadAnnouncements">刷新</el-button>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="announcementList.length === 0" description="暂无公告" />
        <el-timeline v-else>
          <el-timeline-item
            v-for="announcement in pagedAnnouncements"
            :key="announcement.id"
            :timestamp="formatTime(announcement.createTime)"
            placement="top"
          >
            <el-card shadow="hover" class="announcement-card">
              <h3 class="announcement-title">{{ announcement.title }}</h3>
              <div class="announcement-content" v-html="formatContent(announcement.content)"></div>
              <div class="announcement-footer">
                <el-tag size="small" type="info">发布人：{{ announcement.publisherName || '系统管理员' }}</el-tag>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <div class="pagination" v-if="announcementList.length > 0">
          <el-pagination
            background
            layout="prev, pager, next, jumper"
            :total="announcementList.length"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getAnnouncementList } from '@/api/announcement'

const loading = ref(false)
const pageSize = 5
const currentPage = ref(1)
const announcementList = ref([])

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

const formatContent = (content) => {
  if (!content) return ''
  // 将换行符转换为<br>
  return content.replace(/\n/g, '<br>')
}

const loadAnnouncements = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementList()
    if (res.code === 200) {
      announcementList.value = res.data
      currentPage.value = 1
    }
  } catch (error) {
    ElMessage.error('加载公告列表失败')
  } finally {
    loading.value = false
  }
}

const pagedAnnouncements = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return announcementList.value.slice(start, start + pageSize)
})

const handlePageChange = (page) => {
  currentPage.value = page
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announcement-list {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-card {
  margin-bottom: 10px;
}

.announcement-title {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 18px;
  font-weight: bold;
}

.announcement-content {
  color: #606266;
  line-height: 1.8;
  margin-bottom: 15px;
  word-wrap: break-word;
}

.announcement-footer {
  display: flex;
  justify-content: flex-end;
}
</style>

