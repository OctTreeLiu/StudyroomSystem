<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <el-icon><UserFilled /></el-icon>
          <span>逐光自习室 - 注册</span>
        </div>
      </template>
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名（2-20个字符）" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（6-16个字符）"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="registerForm.realName" placeholder="请输入真实姓名（选填）" clearable />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入联系电话（选填）"
            maxlength="11"
            inputmode="numeric"
            clearable
            @input="(v) => (registerForm.phone = normalizePhone(v))"
          />
        </el-form-item>
        <el-form-item label="邮箱">
          <EmailInput v-model="registerForm.email" placeholder="请输入邮箱账号（选填）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="handleRegister">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <span>已有账号？</span>
        <el-link type="primary" @click="$router.push('/login')">立即登录</el-link>
      </div>
      <div class="register-footer" style="margin-top: 10px">
        <el-link type="info" @click="$router.push('/')">返回首页</el-link>
        <span style="margin: 0 10px; color: #909399">|</span>
        <el-link type="info" @click="$router.push('/contact')">联系我们</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { register } from '@/api/auth'
import { useUserStore } from '@/stores'
import EmailInput from '@/components/EmailInput.vue'
import { validatePhone11 } from '@/utils/validators'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const normalizePhone = (value) => {
  const v = value === null || value === undefined ? '' : String(value)
  return v.replace(/\D/g, '').slice(0, 11)
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度必须在2-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码长度必须在6-16个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码长度必须在6-16个字符之间', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [{ validator: validatePhone11, trigger: 'blur' }]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await register(registerForm)
        if (res.code === 200) {
          // 保存token和用户信息
          userStore.setToken(res.data.token)
          userStore.setUserInfo(res.data.user)
          
          ElMessage.success('注册成功')
          
          // 根据角色跳转
          if (res.data.user.role === 1) {
            router.push('/admin')
          } else {
            router.push('/user')
          }
        }
      } catch (error) {
        ElMessage.error(error.message || '注册失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
  padding: 20px;
}

.register-card {
  width: 480px;
  max-width: 100%;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  padding: 10px 0;
}

.card-header .el-icon {
  font-size: 24px;
  margin-right: 8px;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  color: #606266;
}

.register-footer .el-link {
  margin-left: 5px;
  font-weight: 500;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}


/* 响应式设计 */
@media (max-width: 480px) {
  .register-card {
    width: 100%;
  }
}
</style>

