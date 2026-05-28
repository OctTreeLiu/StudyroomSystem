<template>
  <div class="user-modify-log-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>修改日志</span>
          <div>
            <el-input
              v-model="searchKeyword"
              placeholder="按用户ID或用户名查询（被修改的用户）"
              style="width: 260px; margin-right: 10px"
              clearable
              @input="handleSearch"
            />
            <el-button @click="loadLogs">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="logList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="操作记录" min-width="300">
          <template #default="{ row }">
            <span>管理员<strong>{{ row.adminUsername }}</strong>对<strong>{{ row.userId }}</strong>号用户<strong>{{ row.userUsername }}</strong>的信息修改</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div v-if="!loading && pagination.total > 0" class="pagination-container" style="margin-top: 20px; padding: 20px 0;">
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
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="修改详情"
      width="900px"
      :close-on-click-modal="false"
    >
      <div v-if="currentLog" class="detail-container">
        <div class="detail-cards">
          <!-- 修改前 -->
          <el-card class="detail-card before-card">
            <template #header>
              <div class="card-title">修改前</div>
            </template>
            <el-descriptions :column="1" border v-if="beforeData">
              <el-descriptions-item label="会员类型">
                {{ beforeData.memberType || '普通用户' }}
              </el-descriptions-item>
              <el-descriptions-item label="会员到期时间">
                {{ beforeData.memberExpireTime ? formatTime(beforeData.memberExpireTime) : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="真实姓名">
                {{ beforeData.realName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="联系电话">
                {{ beforeData.phone || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="邮箱">
                {{ beforeData.email || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 修改后 -->
          <el-card class="detail-card after-card">
            <template #header>
              <div class="card-title">修改后</div>
            </template>
            <el-descriptions :column="1" border v-if="afterData">
              <el-descriptions-item label="会员类型">
                <span :class="getMemberTypeClass(afterData.memberType)">
                  {{ afterData.memberType || '普通用户' }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="会员到期时间">
                {{ afterData.memberExpireTime ? formatTime(afterData.memberExpireTime) : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="真实姓名">
                {{ afterData.realName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="联系电话">
                {{ afterData.phone || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="邮箱">
                {{ afterData.email || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserModifyLogs, getUserModifyLogDetail } from '@/api/admin/userModifyLog'
import dayjs from 'dayjs'

const loading = ref(false)
const logList = ref([])
const detailDialogVisible = ref(false)
const currentLog = ref(null)
const searchKeyword = ref('')

const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

const beforeData = computed(() => {
  if (!currentLog.value || !currentLog.value.modifyDetail) {
    return null
  }
  try {
    const detail = JSON.parse(currentLog.value.modifyDetail)
    return detail.before || null
  } catch (e) {
    console.error('解析修改详情失败:', e)
    return null
  }
})

const afterData = computed(() => {
  if (!currentLog.value || !currentLog.value.modifyDetail) {
    return null
  }
  try {
    const detail = JSON.parse(currentLog.value.modifyDetail)
    return detail.after || null
  } catch (e) {
    console.error('解析修改详情失败:', e)
    return null
  }
})

const loadLogs = async () => {
  loading.value = true
  try {
    const res = await getUserModifyLogs(
      pagination.value.page,
      pagination.value.pageSize,
      searchKeyword.value
    )
    if (res.code === 200 && res.data) {
      logList.value = res.data.list || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载日志列表失败')
  } finally {
    loading.value = false
  }
}

const showDetail = async (row) => {
  try {
    const res = await getUserModifyLogDetail(row.id)
    if (res.code === 200 && res.data) {
      currentLog.value = res.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error('获取日志详情失败')
    }
  } catch (error) {
    ElMessage.error('获取日志详情失败')
  }
}

const handlePageChange = (page) => {
  pagination.value.page = page
  loadLogs()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.page = 1
  loadLogs()
}

const handleSearch = () => {
  pagination.value.page = 1
  loadLogs()
}

const formatTime = (time) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const getMemberTypeClass = (memberType) => {
  if (memberType === 'VIP') {
    return 'member-type-vip'
  } else if (memberType === 'SVIP') {
    return 'member-type-svip'
  }
  return ''
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.user-modify-log-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  display: flex;
  justify-content: center;
}

.detail-container {
  padding: 10px 0;
}

.detail-cards {
  display: flex;
  gap: 20px;
}

.detail-card {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.before-card {
  border-left: 3px solid #909399;
}

.after-card {
  border-left: 3px solid #67c23a;
}

.member-type-vip {
  color: #e6a23c;
  font-weight: 600;
}

.member-type-svip {
  color: #f56c6c;
  font-weight: 600;
}
</style>

