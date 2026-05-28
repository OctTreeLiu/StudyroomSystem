<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button @click="loadUsers">刷新</el-button>
        </div>
      </template>

      <!-- 搜索框和筛选 -->
      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入用户名或ID进行搜索"
          clearable
          @input="handleSearch"
          style="width: 300px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="filterMemberType" placeholder="筛选身份" style="width: 150px" @change="handleFilterChange" clearable>
          <el-option label="全部" value="" />
          <el-option label="普通用户" :value="0" />
          <el-option label="VIP" :value="1" />
          <el-option label="SVIP" :value="2" />
        </el-select>
      </div>

      <el-table v-loading="loading" :data="pagedUsers" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar
              :size="40"
              :src="row.avatarUrl"
              :icon="UserFilled"
            >
              <template #default>
                <span>{{ row.username?.charAt(0)?.toUpperCase() || '?' }}</span>
              </template>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="150">
          <template #default="{ row }">
            <span class="username-text">{{ row.username }}</span>
          </template>
        </el-table-column>
        <el-table-column label="身份" width="120" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="getMemberTagType(row.memberType)" 
              :class="getMemberTagClass(row.memberType)"
              size="small"
            >
              {{ getMemberTypeText(row.memberType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="会员剩余时长" width="150" align="center">
          <template #default="{ row }">
            <span v-if="row.memberType && row.memberType > 0 && row.memberExpireTime">
              {{ getRemainingDays(row.memberExpireTime) }}
            </span>
            <span v-else style="color: #909399;">0天</span>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : 'primary'">
              {{ row.role === 1 ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editUser(row)">编辑</el-button>
            <el-button type="success" size="small" @click="openSendNotificationDialog(row)">发送通知</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 内联编辑表单 -->
      <div v-if="editDialogVisible" class="edit-form-container">
        <div class="edit-form-header">
          <h3>编辑用户信息</h3>
          <el-button text @click="editDialogVisible = false">关闭</el-button>
        </div>
        <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="120px" class="edit-form">
          <el-form-item label="用户名">
            <el-input v-model="editForm.username" disabled />
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="editForm.realName" placeholder="请输入真实姓名" clearable />
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input
              v-model="editForm.phone"
              placeholder="请输入联系电话"
              maxlength="11"
              inputmode="numeric"
              clearable
              @input="(v) => (editForm.phone = normalizePhone(v))"
            />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <EmailInput v-model="editForm.email" placeholder="请输入邮箱账号" />
          </el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="editForm.gender">
              <el-radio label="男">男</el-radio>
              <el-radio label="女">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="年龄" prop="age">
            <el-input-number 
              v-model="editForm.age" 
              :min="1" 
              :max="150" 
              placeholder="请输入年龄"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="兴趣爱好" prop="hobby">
            <el-input 
              v-model="editForm.hobby" 
              type="textarea" 
              :rows="3"
              placeholder="请输入兴趣爱好"
              :maxlength="200"
              show-word-limit
              clearable 
            />
          </el-form-item>
          <el-form-item label="在读/毕业高校" prop="university">
            <el-input 
              v-model="editForm.university" 
              placeholder="请输入在读或毕业高校"
              :maxlength="100"
              show-word-limit
              clearable 
            />
          </el-form-item>
          <el-form-item label="个性签名" prop="signature">
            <el-input 
              v-model="editForm.signature" 
              type="textarea" 
              :rows="3"
              placeholder="请输入个性签名（不设置则默认为：是一条有梦想的咸鱼......）"
              :maxlength="200"
              show-word-limit
              clearable 
            />
          </el-form-item>
          <el-form-item label="身份类型" prop="memberType">
            <el-select v-model="editForm.memberType" placeholder="请选择身份类型" style="width: 100%">
              <el-option label="普通用户" :value="0" />
              <el-option label="VIP" :value="1" />
              <el-option label="SVIP" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="会员剩余时长（天）" prop="remainingDays">
            <el-input-number 
              v-model="editForm.remainingDays" 
              :min="0" 
              :max="3650"
              :disabled="!editForm.memberType || editForm.memberType === 0"
              placeholder="请输入剩余天数（普通用户为0）"
              style="width: 100%"
            />
            <div style="font-size: 12px; color: #909399; margin-top: 5px;">
              <span v-if="editForm.memberType && editForm.memberType > 0">
                当前到期时间：{{ editForm.memberExpireTime ? formatTime(editForm.memberExpireTime) : '未设置' }}
              </span>
              <span v-else>普通用户剩余时长为0</span>
            </div>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="editForm.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button @click="editDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="updateLoading" @click="handleUpdate">确定</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 发送通知对话框 -->
      <el-dialog
        v-model="sendNotificationDialogVisible"
        title="发送通知"
        width="500px"
        :close-on-click-modal="false"
      >
        <el-form :model="notificationForm" :rules="notificationRules" ref="notificationFormRef" label-width="100px">
          <el-form-item label="接收用户">
            <el-input v-model="notificationForm.username" disabled />
          </el-form-item>
          <el-form-item label="通知标题" prop="title">
            <el-input v-model="notificationForm.title" placeholder="请输入通知标题" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="通知内容" prop="content">
            <el-input
              v-model="notificationForm.content"
              type="textarea"
              :rows="5"
              placeholder="请输入通知内容"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="sendNotificationDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="sendNotificationLoading" @click="handleSendNotification">发送</el-button>
        </template>
      </el-dialog>

      <div class="pagination">
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :total="userList.length"
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
import { ElMessage } from 'element-plus'
import { UserFilled, Search } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getAllUsers, updateUser, sendNotificationToUser } from '@/api/admin/user'
import { validatePhone11 } from '@/utils/validators'
import EmailInput from '@/components/EmailInput.vue'

const loading = ref(false)
const pageSize = 10
const currentPage = ref(1)
const userList = ref([])
const allUsers = ref([]) // 存储所有用户数据
const searchKeyword = ref('') // 搜索关键词
const filterMemberType = ref('') // 身份筛选
const editDialogVisible = ref(false)
const updateLoading = ref(false)
const editFormRef = ref(null)
const currentUserId = ref(null)
const sendNotificationDialogVisible = ref(false)
const sendNotificationLoading = ref(false)
const notificationFormRef = ref(null)
const currentNotificationUser = ref(null)

const editForm = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  gender: '',
  age: null,
  hobby: '',
  university: '',
  signature: '',
  memberType: 0,
  remainingDays: 0,
  memberExpireTime: null,
  status: 1
})

const editRules = {
  phone: [{ validator: validatePhone11, trigger: 'blur' }],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  age: [
    { type: 'number', min: 1, max: 150, message: '年龄必须在1-150之间', trigger: 'blur' }
  ],
  hobby: [
    { max: 200, message: '兴趣爱好长度不能超过200个字符', trigger: 'blur' }
  ],
  university: [
    { max: 100, message: '高校名称长度不能超过100个字符', trigger: 'blur' }
  ],
  signature: [
    { max: 200, message: '个性签名长度不能超过200个字符', trigger: 'blur' }
  ]
}

const normalizePhone = (value) => {
  const v = value === null || value === undefined ? '' : String(value)
  return v.replace(/\D/g, '').slice(0, 11)
}

const notificationForm = reactive({
  username: '',
  title: '',
  content: ''
})

const notificationRules = {
  title: [
    { required: true, message: '请输入通知标题', trigger: 'blur' },
    { max: 100, message: '标题长度不能超过100个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入通知内容', trigger: 'blur' },
    { max: 500, message: '内容长度不能超过500个字符', trigger: 'blur' }
  ]
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const getRemainingDays = (expireTime) => {
  if (!expireTime) return '0天'
  // 使用日期计算，忽略时分秒，确保与编辑表单中的计算一致
  const now = dayjs().startOf('day')
  const expire = dayjs(expireTime).startOf('day')
  const days = expire.diff(now, 'day')
  if (days < 0) {
    return '已过期'
  } else if (days === 0) {
    return '今天到期'
  } else {
    return `${days}天`
  }
}

const getMemberTypeText = (memberType) => {
  if (memberType === 1) {
    return 'VIP'
  } else if (memberType === 2) {
    return 'SVIP'
  } else {
    return '普通用户'
  }
}

const getMemberTagType = (memberType) => {
  if (memberType === 1) {
    return 'warning'
  } else if (memberType === 2) {
    return 'danger'
  } else {
    return 'info'
  }
}

const getMemberTagClass = (memberType) => {
  if (memberType === 1) {
    return 'member-tag-vip'
  } else if (memberType === 2) {
    return 'member-tag-svip'
  } else {
    return ''
  }
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getAllUsers()
    if (res.code === 200) {
      allUsers.value = res.data || []
      // 应用搜索过滤
      applySearchFilter()
      currentPage.value = 1
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 搜索时重置到第一页
  currentPage.value = 1
  applySearchFilter()
}

const handleFilterChange = () => {
  // 筛选时重置到第一页
  currentPage.value = 1
  applySearchFilter()
}

const applySearchFilter = () => {
  let result = allUsers.value
  
  // 身份筛选
  if (filterMemberType.value !== '' && filterMemberType.value !== null && filterMemberType.value !== undefined) {
    result = result.filter(user => {
      const userMemberType = user.memberType !== null && user.memberType !== undefined ? user.memberType : 0
      return userMemberType === filterMemberType.value
    })
  }
  
  // 搜索关键词筛选
  if (searchKeyword.value && searchKeyword.value.trim() !== '') {
    const keyword = searchKeyword.value.toLowerCase().trim()
    result = result.filter(user => {
      // 搜索用户名（不区分大小写）
      const usernameMatch = user.username && user.username.toLowerCase().includes(keyword)
      // 搜索ID
      const idMatch = user.id && user.id.toString().includes(keyword)
      return usernameMatch || idMatch
    })
  }
  
  userList.value = result
}

const pagedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return userList.value.slice(start, start + pageSize)
})

const handlePageChange = (page) => {
  currentPage.value = page
}

const editUser = (user) => {
  currentUserId.value = user.id
  editForm.username = user.username || ''
  editForm.realName = user.realName || ''
  editForm.phone = user.phone || ''
  editForm.email = user.email || ''
  editForm.gender = user.gender || ''
  editForm.age = user.age || null
  editForm.hobby = user.hobby || ''
  editForm.university = user.university || ''
  editForm.signature = user.signature || ''
  editForm.memberType = user.memberType !== null && user.memberType !== undefined ? user.memberType : 0
  editForm.memberExpireTime = user.memberExpireTime || null
  
  // 计算剩余天数（使用日期计算，忽略时分秒，确保准确性）
  if (editForm.memberExpireTime && editForm.memberType && editForm.memberType > 0) {
    const now = dayjs().startOf('day') // 使用当天的开始时间
    const expire = dayjs(editForm.memberExpireTime).startOf('day') // 使用到期日期的开始时间
    const days = expire.diff(now, 'day')
    editForm.remainingDays = days > 0 ? days : 0
  } else {
    editForm.remainingDays = 0
  }
  
  editForm.status = user.status !== null && user.status !== undefined ? user.status : 1
  editDialogVisible.value = true
}

const handleUpdate = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      // 处理会员剩余时长：根据剩余天数计算到期时间
      let memberExpireTime = null
      if (editForm.memberType && editForm.memberType > 0) {
        if (editForm.remainingDays > 0) {
          // 从当前日期开始，加上剩余天数，设置为当天的23:59:59，确保完整的一天
          // 例如：设置30天，到期时间应该是 (今天 + 30天) 的 23:59:59
          memberExpireTime = dayjs()
            .add(editForm.remainingDays, 'day')
            .hour(23)
            .minute(59)
            .second(59)
            .millisecond(999)
            .toISOString()
        } else {
          // 如果剩余天数为0，设置为当前时间（表示已过期）
          memberExpireTime = dayjs().toISOString()
        }
      } else {
        // 普通用户，会员到期时间为null
        memberExpireTime = null
      }
      
      const updateData = {
        ...editForm,
        memberExpireTime: memberExpireTime
      }
      
      updateLoading.value = true
      try {
        const res = await updateUser(currentUserId.value, updateData)
        if (res.code === 200) {
          ElMessage.success('更新成功')
          editDialogVisible.value = false
          loadUsers()
        }
      } catch (error) {
        ElMessage.error(error.message || '更新失败')
      } finally {
        updateLoading.value = false
      }
    }
  })
}

const openSendNotificationDialog = (user) => {
  currentNotificationUser.value = user
  notificationForm.username = user.username || ''
  notificationForm.title = ''
  notificationForm.content = ''
  sendNotificationDialogVisible.value = true
}

const handleSendNotification = async () => {
  if (!notificationFormRef.value) return
  
  await notificationFormRef.value.validate(async (valid) => {
    if (valid) {
      sendNotificationLoading.value = true
      try {
        const res = await sendNotificationToUser(
          currentNotificationUser.value.id,
          notificationForm.title,
          notificationForm.content
        )
        if (res.code === 200) {
          ElMessage.success('通知发送成功')
          sendNotificationDialogVisible.value = false
          notificationForm.title = ''
          notificationForm.content = ''
        }
      } catch (error) {
        ElMessage.error(error.message || '发送通知失败')
      } finally {
        sendNotificationLoading.value = false
      }
    }
  })
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management {
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

.search-section {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

:deep(.el-card) {
  border-radius: 4px;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

/* 用户名：全部设置为黑色 */
.username-text {
  color: #303133;
}

/* VIP身份标签：红色 */
.member-tag-vip {
  color: #f56c6c !important;
  border-color: #f56c6c !important;
  background-color: #fef0f0 !important;
}

/* SVIP身份标签：金色加粗 */
.member-tag-svip {
  color: #ffd700 !important;
  border-color: #ffd700 !important;
  background-color: #fffbf0 !important;
  font-weight: bold !important;
}

:deep(.el-radio-group) {
  display: flex;
  gap: 20px;
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

