<template>
  <div class="admin-points-management">
    <!-- 用户列表卡片 -->
    <el-card style="margin-bottom: 20px">
      <template #header>
        <div class="card-header">
          <span>用户列表（快速调整积分）</span>
          <div>
            <el-input
              v-model="userSearchKeyword"
              placeholder="搜索用户名"
              clearable
              style="width: 250px; margin-right: 10px"
              @clear="filterUserList"
              @keyup.enter="filterUserList"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="filterUserList">搜索</el-button>
            <el-button @click="loadUserList">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="userListLoading" :data="pagedUserList" stripe style="width: 100%">
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="150" />
        <el-table-column label="当前积分" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #409eff; font-weight: 600; font-size: 16px">
              {{ row.totalPoints || 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="showAdjustDialogFromUser(row)"
            >
              调整积分
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 用户列表分页组件 -->
      <div v-if="!userListLoading && filteredUserList.length > 0" class="pagination-container">
        <el-pagination
          v-model:current-page="userPagination.page"
          v-model:page-size="userPagination.pageSize"
          :total="filteredUserList.length"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handleUserPageChange"
          @size-change="handleUserSizeChange"
        />
      </div>
    </el-card>

    <!-- 积分流水管理卡片 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>积分流水管理</span>
          <div>
            <el-input
              v-model="searchUsername"
              placeholder="搜索用户名"
              clearable
              style="width: 200px; margin-right: 10px"
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="loadHistory">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="historyList" stripe style="width: 100%">
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分变动" width="120" align="center">
          <template #default="{ row }">
            <span :class="row.points > 0 ? 'points-increase' : 'points-decrease'">
              {{ row.points > 0 ? '+' : '' }}{{ row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="showAdjustDialog(row)"
            >
              调整积分
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div v-if="!loading && pagination.total > 0" class="pagination-container">
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

    <!-- 调整积分对话框 -->
    <el-dialog v-model="adjustDialogVisible" title="调整积分" width="500px">
      <el-form :model="adjustForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="adjustForm.username" disabled />
        </el-form-item>
        <el-form-item label="调整积分" required>
          <el-input-number
            v-model="adjustForm.points"
            :min="-10000"
            :max="10000"
            :precision="0"
            style="width: 100%"
          />
          <div style="color: #909399; font-size: 12px; margin-top: 5px">
            正数表示增加积分，负数表示扣除积分
          </div>
        </el-form-item>
        <el-form-item label="调整说明">
          <el-input
            v-model="adjustForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入调整说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="adjusting" @click="handleAdjust">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getAllPointsHistory, adjustPoints } from '@/api/admin/points'
import { getAllUsers } from '@/api/admin/user'

const loading = ref(false)
const historyList = ref([])
const searchUsername = ref('')
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})
const adjustDialogVisible = ref(false)
const adjusting = ref(false)
const adjustForm = ref({
  userId: null,
  username: '',
  points: 0,
  description: ''
})

// 用户列表相关
const userListLoading = ref(false)
const userList = ref([])
const userSearchKeyword = ref('')
const userPagination = ref({
  page: 1,
  pageSize: 10
})

// 过滤后的用户列表（搜索过滤）
const filteredUserList = computed(() => {
  if (!userSearchKeyword.value) {
    return userList.value
  }
  const keyword = userSearchKeyword.value.toLowerCase()
  return userList.value.filter(user => {
    return (
      (user.username && user.username.toLowerCase().includes(keyword)) ||
      (user.realName && user.realName.toLowerCase().includes(keyword)) ||
      (user.phone && user.phone.includes(keyword))
    )
  })
})

// 分页后的用户列表
const pagedUserList = computed(() => {
  const start = (userPagination.value.page - 1) * userPagination.value.pageSize
  const end = start + userPagination.value.pageSize
  return filteredUserList.value.slice(start, end)
})

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const getTypeText = (type) => {
  const typeMap = {
    '签到': '签到',
    '预约奖励': '预约奖励',
    '积分兑换': '积分兑换',
    '会员赠送': '会员赠送',
    '长期租赁奖励': '长期租赁奖励',
    '管理员调整': '管理员调整'
  }
  return typeMap[type] || type
}

const getTypeTagType = (type) => {
  const typeMap = {
    '签到': 'success',
    '预约奖励': 'primary',
    '积分兑换': 'warning',
    '会员赠送': 'info',
    '长期租赁奖励': 'info',
    '管理员调整': 'danger'
  }
  return typeMap[type] || ''
}

const loadHistory = async () => {
  loading.value = true
  try {
    const res = await getAllPointsHistory(pagination.value.page, pagination.value.pageSize, searchUsername.value)
    if (res.code === 200 && res.data) {
      historyList.value = res.data.list || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载积分流水失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  loadHistory()
}

const handlePageChange = (page) => {
  pagination.value.page = page
  loadHistory()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.page = 1
  loadHistory()
}

const loadUserList = async () => {
  userListLoading.value = true
  try {
    const res = await getAllUsers()
    if (res.code === 200 && res.data) {
      // 只显示普通用户（role === 0），不显示管理员
      userList.value = res.data.filter(user => user.role === 0)
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    userListLoading.value = false
  }
}

const filterUserList = () => {
  // 搜索时重置到第一页
  userPagination.value.page = 1
}

const handleUserPageChange = (page) => {
  userPagination.value.page = page
}

const handleUserSizeChange = (size) => {
  userPagination.value.pageSize = size
  userPagination.value.page = 1
}

const showAdjustDialog = (row) => {
  adjustForm.value = {
    userId: row.userId,
    username: row.username || '未知用户',
    points: 0,
    description: ''
  }
  adjustDialogVisible.value = true
}

const showAdjustDialogFromUser = (user) => {
  adjustForm.value = {
    userId: user.id,
    username: user.username || '未知用户',
    points: 0,
    description: ''
  }
  adjustDialogVisible.value = true
}

const handleAdjust = async () => {
  if (adjustForm.value.points === 0) {
    ElMessage.warning('调整积分不能为0')
    return
  }

  adjusting.value = true
  try {
    const res = await adjustPoints(
      adjustForm.value.userId,
      adjustForm.value.points,
      adjustForm.value.description || ''
    )
    if (res.code === 200) {
      ElMessage.success(res.message || '调整积分成功')
      adjustDialogVisible.value = false
      await loadHistory()
      await loadUserList() // 刷新用户列表以更新积分
    }
  } catch (error) {
    ElMessage.error(error.message || '调整积分失败')
  } finally {
    adjusting.value = false
  }
}

onMounted(() => {
  loadHistory()
  loadUserList()
})
</script>

<style scoped>
.admin-points-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  flex-wrap: wrap;
  gap: 15px;
}

.points-increase {
  color: #67c23a;
  font-weight: 600;
}

.points-decrease {
  color: #f56c6c;
  font-weight: 600;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

