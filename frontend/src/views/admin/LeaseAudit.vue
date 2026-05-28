<template>
  <div class="lease-audit">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>长期租赁情况</span>
          <div class="header-controls">
            <el-input
              v-model="searchKeyword"
              placeholder="请输入用户名或用户ID"
              clearable
              @input="handleSearch"
              @clear="handleSearch"
              class="search-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select v-model="filterRoom" placeholder="筛选大区" style="width: 150px" @change="handleFilterChange" clearable>
              <el-option label="全部" value="" />
              <el-option
                v-for="room in roomOptions"
                :key="room.roomNumber"
                :label="room.roomName"
                :value="room.roomNumber"
              />
            </el-select>
            <el-select v-model="filterStatus" placeholder="筛选状态" style="width: 150px" @change="handleFilterChange" clearable>
              <el-option label="全部" value="" />
              <el-option label="待审核" :value="0" />
              <el-option label="审核通过待付款" :value="1" />
              <el-option label="已付款生效" :value="2" />
              <el-option label="已拒绝" :value="3" />
              <el-option label="已取消" :value="4" />
              <el-option label="使用中" :value="5" />
              <el-option label="使用结束" :value="6" />
            </el-select>
            <el-button type="primary" style="margin-left: 10px" @click="loadLeases">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="pagedLeases" stripe style="width: 100%">
        <el-table-column prop="leaseNumber" label="租赁编号" width="180" />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="roomName" label="大区" width="200">
          <template #default="{ row }">
            {{ row.roomNumber }} - {{ row.roomName }}
          </template>
        </el-table-column>
        <el-table-column prop="seatNumber" label="座位" width="150">
          <template #default="{ row }">
            <span v-if="row.seatNumber">
              {{ row.seatNumber }}<span v-if="row.seatName"> - {{ row.seatName }}</span>
            </span>
            <span v-else style="color: #909399">未指定</span>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="amount" label="金额" width="100" align="center">
          <template #default="{ row }">
            ¥{{ row.amount }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="支付时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.paymentTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)">
              {{ getStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 0" 
              type="success" 
              size="small"
              @click="auditLeaseClick(row, true)"
            >
              通过
            </el-button>
            <el-button 
              v-if="row.status === 0" 
              type="danger" 
              size="small"
              @click="auditLeaseClick(row, false)"
            >
              拒绝
            </el-button>
            <el-button 
              v-else
              type="primary" 
              size="small"
              @click="viewDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 审核对话框 -->
      <el-dialog v-model="auditDialogVisible" :title="auditForm.approved ? '审核通过' : '审核拒绝'" width="500px">
        <el-form :model="auditForm" ref="auditFormRef" label-width="100px">
          <el-form-item label="审核备注">
            <el-input
              v-model="auditForm.auditRemark"
              type="textarea"
              :rows="4"
              placeholder="请输入审核备注（选填）"
              maxlength="500"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="auditLoading" @click="handleAudit">确定</el-button>
        </template>
      </el-dialog>

      <!-- 详情对话框 -->
      <el-dialog v-model="detailVisible" title="租赁详情" width="600px">
        <div v-if="currentLease">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="租赁编号">{{ currentLease.leaseNumber }}</el-descriptions-item>
            <el-descriptions-item label="用户">{{ currentLease.username }}</el-descriptions-item>
            <el-descriptions-item label="大区">
              {{ currentLease.roomNumber }} - {{ currentLease.roomName }}
            </el-descriptions-item>
            <el-descriptions-item label="座位" v-if="currentLease.seatNumber">
              {{ currentLease.seatNumber }}<span v-if="currentLease.seatName"> - {{ currentLease.seatName }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="金额">¥{{ currentLease.amount }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">
              {{ formatTime(currentLease.paymentTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="开始日期">{{ currentLease.startDate }}</el-descriptions-item>
            <el-descriptions-item label="结束日期">{{ currentLease.endDate }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(currentLease)">
                {{ getStatusText(currentLease) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="申请理由" :span="2">
              {{ currentLease.applyReason }}
            </el-descriptions-item>
            <el-descriptions-item v-if="currentLease.auditRemark" label="审核备注" :span="2">
              {{ currentLease.auditRemark }}
            </el-descriptions-item>
            <el-descriptions-item v-if="currentLease.paymentDeadline" label="付款截止时间" :span="2">
              {{ formatTime(currentLease.paymentDeadline) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-dialog>

      <div class="pagination">
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :total="filteredLeases.length"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getAllLeases, auditLease as auditLeaseAPI } from '@/api/admin/lease'

const loading = ref(false)
const pageSize = 10
const currentPage = ref(1)
const leaseList = ref([])
const filterStatus = ref('')
const filterRoom = ref('')
const searchKeyword = ref('')
const auditDialogVisible = ref(false)
const auditLoading = ref(false)
const auditFormRef = ref(null)
const detailVisible = ref(false)
const currentLease = ref(null)
const currentLeaseId = ref(null)

const auditForm = reactive({
  approved: true,
  auditRemark: ''
})

// 获取所有唯一的大区选项
const roomOptions = computed(() => {
  const roomMap = new Map()
  leaseList.value.forEach(lease => {
    if (lease.roomNumber && lease.roomName) {
      if (!roomMap.has(lease.roomNumber)) {
        roomMap.set(lease.roomNumber, {
          roomNumber: lease.roomNumber,
          roomName: lease.roomName
        })
      }
    }
  })
  return Array.from(roomMap.values()).sort((a, b) => {
    // 按大区编号排序
    return a.roomNumber.localeCompare(b.roomNumber)
  })
})

const filteredLeases = computed(() => {
  let result = leaseList.value

  // 大区筛选
  if (filterRoom.value !== '') {
    result = result.filter(l => l.roomNumber === filterRoom.value)
  }

  // 状态筛选（与用户端保持一致，直接使用后端返回的数值状态）
  if (filterStatus.value !== '') {
    result = result.filter(l => l.status === filterStatus.value)
  }

  // 用户名和用户ID模糊查询
  if (searchKeyword.value && searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim()
    result = result.filter(l => {
      // 用户名模糊匹配（不区分大小写）
      const username = (l.username || '').toLowerCase()
      const usernameMatch = username.includes(keyword.toLowerCase())
      
      // 用户ID精确或模糊匹配（支持数字ID）
      const userId = l.userId ? String(l.userId) : ''
      const userIdMatch = userId.includes(keyword)
      
      return usernameMatch || userIdMatch
    })
  }

  return result
})

const pagedLeases = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredLeases.value.slice(start, start + pageSize)
})

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

// 状态文本与用户端长期租赁列表保持一致
const getStatusText = (lease) => {
  const statusMap = {
    0: '待审核',
    1: '审核通过待付款',
    2: '已付款生效',
    3: '已拒绝',
    4: '已取消',
    5: '使用中',
    6: '使用结束',
    7: '已退款'
  }
  return statusMap[lease.status] || '未知'
}

// 状态标签样式与用户端保持一致
const getStatusType = (lease) => {
  const typeMap = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'danger',
    4: 'info',
    5: 'success',
    6: 'info',
    7: 'info'
  }
  return typeMap[lease.status] || 'info'
}

const loadLeases = async () => {
  loading.value = true
  try {
    const res = await getAllLeases()
    if (res.code === 200) {
      leaseList.value = res.data
      currentPage.value = 1
    }
  } catch (error) {
    ElMessage.error('加载租赁列表失败')
  } finally {
    loading.value = false
  }
}

const auditLeaseClick = (lease, approved) => {
  currentLeaseId.value = lease.id
  auditForm.approved = approved
  auditForm.auditRemark = ''
  auditDialogVisible.value = true
}

const handleAudit = async () => {
  auditLoading.value = true
  try {
    const res = await auditLeaseAPI(currentLeaseId.value, auditForm)
    if (res.code === 200) {
      ElMessage.success(res.message || '审核成功')
      auditDialogVisible.value = false
      loadLeases()
    }
  } catch (error) {
    ElMessage.error(error.message || '审核失败')
  } finally {
    auditLoading.value = false
  }
}

const viewDetail = (lease) => {
  currentLease.value = lease
  detailVisible.value = true
}

onMounted(() => {
  loadLeases()
})

const handlePageChange = (page) => {
  currentPage.value = page
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleFilterChange = () => {
  currentPage.value = 1
}
</script>

<style scoped>
.lease-audit {
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

.header-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-input {
  width: 220px;
}

:deep(.el-card) {
  border-radius: 4px;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>

