<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <el-icon><Reading /></el-icon>
          <span>逐光自习室 - 登录</span>
        </div>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span>还没有账号？</span>
        <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
      </div>
      <div class="login-footer" style="margin-top: 10px">
        <el-link type="info" @click="$router.push('/')">返回首页</el-link>
        <span style="margin: 0 10px; color: #909399">|</span>
        <el-link type="info" @click="$router.push('/contact')">联系我们</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Reading } from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  // 防止重复提交
  if (loading.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        if (res.code === 200) {
          // 确保user对象存在且role字段正确
          if (!res.data.user) {
            throw new Error('登录返回的用户信息为空')
          }
          
          // 确保role字段存在，如果不存在则默认为0（普通用户）
          if (res.data.user.role === undefined || res.data.user.role === null) {
            res.data.user.role = 0
          }
          
          // 先保存用户信息，再保存token（确保userInfo先设置）
          userStore.setUserInfo(res.data.user)
          userStore.setToken(res.data.token)
          
          // 验证状态是否已正确设置
          if (!userStore.isLogin || !userStore.userInfo) {
            throw new Error('登录状态设置失败，请重试')
          }
          
          ElMessage.success('登录成功')
          
          // 等待一下确保状态更新完成，使用nextTick确保Vue响应式更新完成
          await new Promise(resolve => {
            setTimeout(() => {
              // 再次验证状态
              if (userStore.isLogin && userStore.userInfo) {
                resolve()
              } else {
                resolve() // 即使验证失败也继续，让路由守卫处理
              }
            }, 300)
          })
          
          // 根据角色跳转，使用replace避免返回登录页
          const targetPath = res.data.user.role === 1 ? '/admin' : '/user'
          
          // 使用nextTick确保DOM更新完成后再跳转
          await router.replace(targetPath).catch(err => {
            console.error('路由跳转失败:', err)
            // 如果replace失败，尝试使用push
            return router.push(targetPath)
          })
        }
      } catch (error) {
        console.error('登录错误:', error)
        ElMessage.error(error.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
}

.login-card {
  width: 420px;
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

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #606266;
}

.login-footer .el-link {
  margin-left: 5px;
  font-weight: 500;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}


/* 响应式设计 */
@media (max-width: 480px) {
  .login-card {
    width: 90%;
    margin: 20px;
  }
}
</style>

