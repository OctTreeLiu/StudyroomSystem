<template>
  <div class="user-home">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <span>个人信息</span>
            <el-tag v-if="userInfo?.id" type="info" effect="dark" size="large" class="user-id-tag">
              ID: {{ userInfo.id }}
            </el-tag>
          </div>
          <div class="header-actions">
            <el-button @click="openPasswordDialog">修改密码</el-button>
            <el-button type="primary" @click="openEditDialog">修改信息</el-button>
          </div>
        </div>
      </template>
      <div v-if="userInfo" class="user-info-container">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="handleAvatarUpload"
            accept="image/*"
          >
            <div class="avatar-wrapper">
              <el-avatar
                :size="120"
                :src="userInfo.avatarUrl"
                :icon="UserFilled"
                class="avatar"
              >
                <template #default>
                  <span style="font-size: 48px">{{ userInfo.username?.charAt(0)?.toUpperCase() }}</span>
                </template>
              </el-avatar>
              <div class="avatar-overlay">
                <el-icon :size="30"><Camera /></el-icon>
                <span>点击上传</span>
              </div>
            </div>
          </el-upload>
          <p class="avatar-tip">点击头像上传新头像</p>
          <p class="avatar-size-tip">上传的头像大小不得超过1M</p>
        </div>

        <!-- 用户信息 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ userInfo.realName || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ userInfo.phone || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ userInfo.email || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ userInfo.gender || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ userInfo.age || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="兴趣爱好" :span="2">
            {{ userInfo.hobby || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="在读/毕业高校" :span="2">
            {{ userInfo.university || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="个性签名" :span="2">
            {{ userInfo.signature || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="userInfo.role === 1 ? 'danger' : 'primary'">
              {{ userInfo.role === 1 ? '管理员' : '普通用户' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="会员身份">
            <el-tag 
              :class="getMemberTagClass(userInfo.memberType, userInfo.memberExpireTime)"
              :type="getMemberTagType(userInfo.memberType)"
            >
              {{ getMemberTypeText(userInfo.memberType, userInfo.memberExpireTime) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ userInfo.createTime }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 内联编辑表单 -->
    <div v-if="editDialogVisible" class="edit-form-container">
      <div class="edit-form-header">
        <h3>修改个人信息</h3>
        <el-button text @click="editDialogVisible = false">关闭</el-button>
      </div>
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="120px" class="edit-form">
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
        <el-form-item>
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="updateLoading" @click="handleUpdate">确定</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="420px" :close-on-click-modal="false">
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="90px"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password autocomplete="current-password" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password autocomplete="new-password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled, Camera } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'
import { getCurrentUserInfo, updateUserInfo, uploadAvatar, changePassword } from '@/api/user'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { validatePhone11 } from '@/utils/validators'
import EmailInput from '@/components/EmailInput.vue'

const userStore = useUserStore()
const router = useRouter()

const userInfo = computed(() => userStore.userInfo)
const editDialogVisible = ref(false)
const updateLoading = ref(false)
const avatarUploading = ref(false)
const editFormRef = ref(null)

const editForm = reactive({
  realName: '',
  phone: '',
  email: '',
  gender: '',
  age: null,
  hobby: '',
  university: '',
  signature: ''
})

const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 16, message: '新密码长度必须在6-16位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { min: 6, max: 16, message: '新密码长度必须在6-16位之间', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的新密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

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

// 获取会员类型文本
const getMemberTypeText = (memberType, memberExpireTime) => {
  if (!memberType || memberType === 0) {
    return '普通用户'
  }
  
  // 检查会员是否有效（未过期）
  if (memberExpireTime) {
    const expireTime = dayjs(memberExpireTime)
    if (expireTime.isBefore(dayjs())) {
      return '普通用户（会员已过期）'
    }
  } else {
    return '普通用户'
  }
  
  if (memberType === 1) {
    return 'VIP会员'
  } else if (memberType === 2) {
    return 'SVIP会员'
  }
  
  return '普通用户'
}

// 获取会员标签类型
const getMemberTagType = (memberType) => {
  if (!memberType || memberType === 0) {
    return 'info'
  }
  if (memberType === 1) {
    return 'warning' // VIP 使用 warning 类型（黄色/橙色）
  } else if (memberType === 2) {
    return 'danger' // SVIP 使用 danger 类型（红色）
  }
  return 'info'
}

// 获取会员标签样式类
const getMemberTagClass = (memberType, memberExpireTime) => {
  if (!memberType || memberType === 0) {
    return ''
  }
  
  // 检查会员是否有效（未过期）
  if (memberExpireTime) {
    const expireTime = dayjs(memberExpireTime)
    if (expireTime.isBefore(dayjs())) {
      return '' // 会员已过期，不应用样式
    }
  } else {
    return '' // 没有到期时间，不应用样式
  }
  
  if (memberType === 1) {
    return 'member-tag-vip' // VIP：红色边框
  } else if (memberType === 2) {
    return 'member-tag-svip' // SVIP：金色加粗
  }
  
  return ''
}

const loadUserInfo = async () => {
  try {
    const res = await getCurrentUserInfo()
    if (res.code === 200) {
      userStore.setUserInfo(res.data)
      // 填充编辑表单
      editForm.realName = res.data.realName || ''
      editForm.phone = res.data.phone || ''
      editForm.email = res.data.email || ''
      editForm.gender = res.data.gender || ''
      editForm.age = res.data.age || null
      editForm.hobby = res.data.hobby || ''
      editForm.university = res.data.university || ''
      editForm.signature = res.data.signature || ''
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

// 打开编辑抽屉
const openEditDialog = () => {
  if (userInfo.value) {
    editForm.realName = userInfo.value.realName || ''
    editForm.phone = userInfo.value.phone || ''
    editForm.email = userInfo.value.email || ''
    editForm.gender = userInfo.value.gender || ''
    editForm.age = userInfo.value.age || null
    editForm.hobby = userInfo.value.hobby || ''
    editForm.university = userInfo.value.university || ''
    editForm.signature = userInfo.value.signature || ''
  }
  editDialogVisible.value = true
}

const openPasswordDialog = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    passwordLoading.value = true
    try {
      const res = await changePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      if (res.code === 200) {
        ElMessage.success('密码修改成功，请重新登录')
        passwordDialogVisible.value = false
        userStore.logout()
        router.push({ path: '/login', query: { redirect: '/user/info' } })
      }
    } catch (error) {
      ElMessage.error(error.message || '修改失败')
    } finally {
      passwordLoading.value = false
    }
  })
}

const handleUpdate = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      updateLoading.value = true
      try {
        const res = await updateUserInfo(editForm)
        if (res.code === 200) {
          ElMessage.success('更新成功')
          editDialogVisible.value = false
          // 重新加载用户信息
          await loadUserInfo()
        }
      } catch (error) {
        ElMessage.error(error.message || '更新失败')
      } finally {
        updateLoading.value = false
      }
    }
  })
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt1M = file.size / 1024 / 1024 < 1

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt1M) {
    ElMessage.error('头像大小不能超过 1MB!')
    return false
  }
  return true
}

const handleAvatarUpload = async (options) => {
  const file = options.file
  avatarUploading.value = true
  
  try {
    // 将文件转换为base64
    const reader = new FileReader()
    reader.onload = async (e) => {
      try {
        const base64 = e.target.result
        const res = await uploadAvatar(base64)
        if (res.code === 200) {
          ElMessage.success('头像上传成功')
          userStore.setUserInfo(res.data)
          await loadUserInfo()
        }
      } catch (error) {
        ElMessage.error(error.message || '头像上传失败')
      } finally {
        avatarUploading.value = false
      }
    }
    reader.onerror = () => {
      ElMessage.error('文件读取失败')
      avatarUploading.value = false
    }
    reader.readAsDataURL(file)
  } catch (error) {
    ElMessage.error('头像上传失败')
    avatarUploading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.user-home {
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-id-tag {
  font-size: 14px;
  font-weight: 600;
  padding: 6px 12px;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
  animation: pulse-id 2s infinite;
}

@keyframes pulse-id {
  0%, 100% {
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
  }
  50% {
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.6);
  }
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 4px;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar {
  border: 2px solid #409EFF;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(64, 158, 255, 0.8);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  font-size: 12px;
  gap: 5px;
}

.avatar-tip {
  margin-top: 10px;
  color: #606266;
  font-size: 14px;
  text-align: center;
}

.avatar-size-tip {
  margin-top: 5px;
  color: #909399;
  font-size: 12px;
  text-align: center;
}

:deep(.el-card) {
  border-radius: 4px;
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

/* VIP会员样式：红色边框 */
:deep(.member-tag-vip) {
  border: 2px solid #f56c6c !important;
  border-radius: 4px;
  box-sizing: border-box;
}

/* SVIP会员样式：金色加粗 */
:deep(.member-tag-svip) {
  border: 2px solid #ffd700 !important;
  border-radius: 4px;
  font-weight: bold !important;
  color: #d4af37 !important;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.15) 0%, rgba(255, 215, 0, 0.05) 100%) !important;
  box-sizing: border-box;
  text-shadow: 0 1px 2px rgba(212, 175, 55, 0.3);
}
</style>

