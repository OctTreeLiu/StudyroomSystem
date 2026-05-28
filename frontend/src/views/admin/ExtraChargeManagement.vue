<template>
  <div class="extra-charge-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>额外收费管理</span>
          <el-button type="primary" @click="openCreateDialog">发起收费申请</el-button>
        </div>
      </template>

      <!-- 提示信息 -->
      <el-alert
        title="用户付款后，按时处理事务并做好备忘录。"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <el-table v-loading="loading" :data="orderList" stripe style="width: 100%">
        <el-table-column prop="orderNumber" label="订单编号" width="200" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column prop="amount" label="收费金额" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: 600">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="收费内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="paymentStatus" label="支付状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.paymentStatus === 1 ? 'success' : row.paymentStatus === 2 ? 'info' : 'warning'">
              {{ row.paymentStatus === 1 ? '已支付' : row.paymentStatus === 2 ? '已取消' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="付款时间" width="180">
          <template #default="{ row }">
            <span v-if="row.paymentTime" :class="{ 'highlight-payment-time': row.paymentStatus === 1 }">
              {{ formatTime(row.paymentTime) }}
            </span>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
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

    <!-- 发起收费申请对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="发起收费申请"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="chargeForm" :rules="chargeRules" ref="chargeFormRef" label-width="100px">
        <el-form-item label="选择用户" prop="userId">
          <el-select
            v-model="chargeForm.userId"
            placeholder="请选择用户"
            filterable
            remote
            :remote-method="searchUsers"
            :loading="userSearchLoading"
            style="width: 100%"
            @change="handleUserChange"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="`${user.username} (ID: ${user.id})`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input
            v-model="chargeForm.username"
            placeholder="选择用户后自动填充"
            disabled
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="收费金额" prop="amount">
          <el-input-number
            v-model="chargeForm.amount"
            :min="0.01"
            :precision="2"
            :step="0.01"
            placeholder="请输入收费金额"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="收费内容" prop="content">
          <el-input
            v-model="chargeForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入收费内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllExtraChargeOrders, createExtraCharge } from '@/api/extraCharge'
import { getAllUsers } from '@/api/admin/user'
import dayjs from 'dayjs'

const loading = ref(false)
const orderList = ref([])
const createDialogVisible = ref(false)
const submitting = ref(false)
const chargeFormRef = ref(null)
const userSearchLoading = ref(false)
const userOptions = ref([])

const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

const chargeForm = ref({
  userId: null,
  username: '',
  amount: null,
  content: ''
})

const chargeRules = {
  userId: [
    { required: true, message: '请选择用户', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '请输入收费金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '收费金额必须大于0', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入收费内容', trigger: 'blur' },
    { min: 1, max: 500, message: '收费内容长度在1到500个字符', trigger: 'blur' }
  ]
}

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getAllExtraChargeOrders(pagination.value.page, pagination.value.pageSize)
    if (res.code === 200 && res.data) {
      orderList.value = res.data.list || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = async () => {
  chargeForm.value = {
    userId: null,
    username: '',
    amount: null,
    content: ''
  }
  userOptions.value = []
  createDialogVisible.value = true
  // 预加载用户列表，以便在选择用户后能正确显示用户名
  await searchUsers('')
}

const handleUserChange = (userId) => {
  if (userId) {
    const selectedUser = userOptions.value.find(user => user.id === userId)
    if (selectedUser) {
      chargeForm.value.username = selectedUser.username
    } else {
      // 如果在下拉列表中找不到，可能是从其他来源选择的，尝试从所有用户中查找
      searchUsers('').then(() => {
        const user = userOptions.value.find(u => u.id === userId)
        if (user) {
          chargeForm.value.username = user.username
        }
      })
    }
  } else {
    chargeForm.value.username = ''
  }
}

const searchUsers = async (query) => {
  userSearchLoading.value = true
  try {
    const res = await getAllUsers()
    if (res.code === 200 && res.data) {
      if (!query || query.trim() === '') {
        // 如果没有查询条件，显示所有用户（用于查找已选择的用户）
        userOptions.value = res.data
      } else {
        const keyword = query.toLowerCase()
        userOptions.value = res.data
          .filter(user => 
            user.username.toLowerCase().includes(keyword) ||
            user.id.toString().includes(keyword)
          )
          .slice(0, 20) // 限制显示数量
      }
      
      // 如果已经选择了用户，更新用户名显示
      if (chargeForm.value.userId) {
        const selectedUser = userOptions.value.find(user => user.id === chargeForm.value.userId)
        if (selectedUser) {
          chargeForm.value.username = selectedUser.username
        }
      }
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    userSearchLoading.value = false
  }
}

const handleCreate = async () => {
  if (!chargeFormRef.value) return
  
  await chargeFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const res = await createExtraCharge({
          userId: chargeForm.value.userId,
          amount: chargeForm.value.amount,
          content: chargeForm.value.content
        })
        if (res.code === 200) {
          ElMessage.success('收费申请创建成功')
          createDialogVisible.value = false
          loadOrders()
          // 触发额外收费状态更新事件
          window.dispatchEvent(new Event('extra-charge-updated'))
        }
      } catch (error) {
        ElMessage.error(error.message || '创建收费申请失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handlePageChange = (page) => {
  pagination.value.page = page
  loadOrders()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.page = 1
  loadOrders()
}

const formatTime = (time) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.extra-charge-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.highlight-payment-time {
  color: #67c23a;
  font-weight: 600;
}

.pagination-container {
  display: flex;
  justify-content: center;
}
</style>

