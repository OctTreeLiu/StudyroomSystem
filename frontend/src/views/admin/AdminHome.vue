<template>
  <div class="admin-home">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>管理员控制台</span>
        </div>
      </template>
      <el-alert
        title="欢迎进入管理员后台"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      >
        <template #default>
          <p>您正在使用管理员账户登录系统，可以进行用户管理、公告管理、预约审核等操作。</p>
          <p>后续功能将在接下来的开发中逐步完善。</p>
        </template>
      </el-alert>
      
      <el-descriptions :column="2" border style="margin-top: 20px">
        <el-descriptions-item label="管理员账号">{{ userInfo?.username }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag type="danger">管理员</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 联系方式管理 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>联系方式管理</span>
          <el-button type="primary" @click="openEditDialog">编辑联系方式</el-button>
        </div>
      </template>
      
      <div v-loading="contactLoading" class="contact-display">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="联系电话">
            {{ contactInfo.phone || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱地址">
            {{ contactInfo.email || '未设置' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 编辑联系方式对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑联系方式" width="500px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
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
        <el-form-item label="邮箱地址" prop="email">
          <EmailInput v-model="editForm.email" placeholder="请输入邮箱账号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="updateLoading" @click="handleUpdate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useUserStore } from '@/stores'
import { getCurrentUserInfo } from '@/api/user'
import { getAdminContactInfo, updateAdminContact } from '@/api/contact'
import { ElMessage } from 'element-plus'
import { validatePhone11 } from '@/utils/validators'
import EmailInput from '@/components/EmailInput.vue'

const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)

const contactLoading = ref(false)
const contactInfo = ref({
  phone: '',
  email: ''
})

const editDialogVisible = ref(false)
const updateLoading = ref(false)
const editFormRef = ref(null)

const editForm = reactive({
  phone: '',
  email: ''
})

const editRules = {
  phone: [{ validator: validatePhone11, trigger: 'blur' }],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const normalizePhone = (value) => {
  const v = value === null || value === undefined ? '' : String(value)
  return v.replace(/\D/g, '').slice(0, 11)
}

const loadContactInfo = async () => {
  contactLoading.value = true
  try {
    const res = await getAdminContactInfo()
    if (res.code === 200) {
      contactInfo.value = res.data || {}
    }
  } catch (error) {
    ElMessage.error('获取联系方式失败')
  } finally {
    contactLoading.value = false
  }
}

const openEditDialog = () => {
  editForm.phone = contactInfo.value.phone || ''
  editForm.email = contactInfo.value.email || ''
  editDialogVisible.value = true
}

const handleUpdate = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      updateLoading.value = true
      try {
        const res = await updateAdminContact(editForm)
        if (res.code === 200) {
          ElMessage.success('更新成功')
          editDialogVisible.value = false
          await loadContactInfo()
        }
      } catch (error) {
        ElMessage.error(error.message || '更新失败')
      } finally {
        updateLoading.value = false
      }
    }
  })
}

onMounted(async () => {
  // 获取最新用户信息
  try {
    const res = await getCurrentUserInfo()
    if (res.code === 200) {
      userStore.setUserInfo(res.data)
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
  
  // 加载联系方式
  await loadContactInfo()
})
</script>

<style scoped>
.admin-home {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.contact-display {
  padding: 10px 0;
}
</style>

