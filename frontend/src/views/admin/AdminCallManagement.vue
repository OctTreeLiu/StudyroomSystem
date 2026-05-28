<template>
  <div class="admin-call-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>呼叫记录管理</span>
          <div>
            <el-input
              v-model="searchKeyword"
              placeholder="按用户名或用户ID查找"
              style="width: 220px; margin-right: 10px"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Filter /></el-icon>
              </template>
            </el-input>
            <el-button @click="loadCalls">刷新</el-button>
            <el-button 
              type="primary" 
              :icon="Filter"
              @click="showFilter = !showFilter"
            >
              筛选
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div v-if="showFilter" class="filter-section">
        <el-radio-group v-model="filterStatus" @change="handleFilterChange">
          <el-radio :label="null">全部</el-radio>
          <el-radio :label="0">待处理</el-radio>
          <el-radio :label="1">已处理</el-radio>
        </el-radio-group>
      </div>

      <el-table v-loading="loading" :data="callList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="150" />
        <el-table-column prop="seatInfo" label="座位信息" width="200">
          <template #default="{ row }">
            {{ row.seatInfo || '无' }}
          </template>
        </el-table-column>
        <el-table-column prop="message" label="留言" min-width="200">
          <template #default="{ row }">
            {{ row.message || '无' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'">
              {{ row.status === 0 ? '待处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="呼叫时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="handleAdminName" label="处理人" width="120">
          <template #default="{ row }">
            {{ row.handleAdminName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="handleTime" label="处理时间" width="180">
          <template #default="{ row }">
            {{ row.handleTime ? formatTime(row.handleTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="primary"
              size="small"
              @click="handleCallRecord(row.id)"
            >
              标记已处理
            </el-button>
            <span v-else style="color: #909399">已处理</span>
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
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Filter } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getAllCalls, handleCall } from '@/api/adminCall'

const loading = ref(false)
const callList = ref([])
const showFilter = ref(false)
const filterStatus = ref(null)
const searchKeyword = ref('')
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})
let pollTimer = null

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const loadCalls = async () => {
  loading.value = true
  try {
    const res = await getAllCalls(
      filterStatus.value,
      pagination.value.page,
      pagination.value.pageSize,
      searchKeyword.value
    )
    if (res.code === 200 && res.data) {
      callList.value = res.data.list || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载呼叫记录失败')
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  pagination.value.page = 1
  loadCalls()
}

const handleSearch = () => {
  pagination.value.page = 1
  loadCalls()
}

const handlePageChange = (page) => {
  pagination.value.page = page
  loadCalls()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.page = 1
  loadCalls()
}

const handleCallRecord = async (id) => {
  try {
    const res = await handleCall(id)
    if (res.code === 200) {
      ElMessage.success('标记成功')
      loadCalls()
      // 触发呼叫记录更新事件，用于菜单高亮
      window.dispatchEvent(new Event('call-updated'))
    }
  } catch (error) {
    ElMessage.error(error.message || '处理失败')
  }
}

// 定时轮询呼叫记录（每5秒）
const startPolling = () => {
  pollTimer = setInterval(() => {
    loadCalls()
  }, 5000) // 5秒轮询一次，确保用户呼叫后能快速显示
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onMounted(() => {
  loadCalls()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.admin-call-management {
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

.filter-section {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.pagination-container {
  display: flex;
  justify-content: center;
}
</style>

