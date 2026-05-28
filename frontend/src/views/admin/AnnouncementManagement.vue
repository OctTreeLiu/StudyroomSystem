<template>
  <div class="announcement-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
          <el-button type="primary" @click="handleCreate">发布公告</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="pagedAnnouncements" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" width="300" />
        <el-table-column prop="publisherName" label="发布人" width="150" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '发布中' : '已删除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              v-if="row.status === 1" 
              type="danger" 
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 内联编辑表单 -->
      <div v-if="dialogVisible" class="edit-form-container">
        <div class="edit-form-header">
          <h3>{{ isEdit ? '编辑公告' : '发布公告' }}</h3>
          <el-button text @click="dialogVisible = false">关闭</el-button>
        </div>
        <el-form :model="announcementForm" :rules="rules" ref="announcementFormRef" label-width="100px" class="edit-form">
          <el-form-item label="公告标题" prop="title">
            <el-input 
              v-model="announcementForm.title" 
              placeholder="请输入公告标题"
              maxlength="200"
              show-word-limit
              clearable
            />
          </el-form-item>
          <el-form-item label="公告内容" prop="content">
            <el-input
              v-model="announcementForm.content"
              type="textarea"
              :rows="10"
              placeholder="请输入公告内容"
              show-word-limit
              maxlength="2000"
            />
          </el-form-item>
          <el-form-item>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="pagination">
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :total="announcementList.length"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { 
  getAllAnnouncements, 
  createAnnouncement, 
  updateAnnouncement, 
  deleteAnnouncement 
} from '@/api/admin/announcement'

const loading = ref(false)
const pageSize = 10
const currentPage = ref(1)
const announcementList = ref([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const announcementFormRef = ref(null)
const currentAnnouncementId = ref(null)

const announcementForm = reactive({
  title: '',
  content: ''
})

const rules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { max: 200, message: '标题长度不能超过200个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' }
  ]
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const loadAnnouncements = async () => {
  loading.value = true
  try {
    const res = await getAllAnnouncements()
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

const handleCreate = () => {
  isEdit.value = false
  currentAnnouncementId.value = null
  announcementForm.title = ''
  announcementForm.content = ''
  dialogVisible.value = true
}

const handleEdit = (announcement) => {
  isEdit.value = true
  currentAnnouncementId.value = announcement.id
  announcementForm.title = announcement.title || ''
  announcementForm.content = announcement.content || ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!announcementFormRef.value) return
  
  await announcementFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        let res
        if (isEdit.value) {
          res = await updateAnnouncement(currentAnnouncementId.value, announcementForm)
        } else {
          res = await createAnnouncement(announcementForm)
        }
        
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '发布成功')
          dialogVisible.value = false
          loadAnnouncements()
        }
      } catch (error) {
        ElMessage.error(error.message || (isEdit.value ? '更新失败' : '发布失败'))
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = async (announcement) => {
  try {
    await ElMessageBox.confirm(
      '确认删除该公告吗？',
      '确认删除',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await deleteAnnouncement(announcement.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadAnnouncements()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announcement-management {
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

:deep(.el-card) {
  border-radius: 4px;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

.edit-form-container {
  margin-top: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.edit-form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.edit-form-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}
</style>

