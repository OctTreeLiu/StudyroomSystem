<template>
  <div class="contact-container">
    <el-card class="contact-card">
      <template #header>
        <div class="card-header">
          <el-icon><Phone /></el-icon>
          <span>联系我们</span>
        </div>
      </template>
      
      <div v-loading="loading" class="contact-content">
        <div class="contact-info">
          <div class="contact-item">
            <el-icon class="contact-icon"><Phone /></el-icon>
            <div class="contact-detail">
              <div class="contact-label">联系电话</div>
              <div class="contact-value">{{ contactInfo.phone || '暂无' }}</div>
            </div>
          </div>
          
          <div class="contact-item">
            <el-icon class="contact-icon"><Message /></el-icon>
            <div class="contact-detail">
              <div class="contact-label">邮箱地址</div>
              <div class="contact-value">{{ contactInfo.email || '暂无' }}</div>
            </div>
          </div>
        </div>
        
        <div class="contact-tips">
          <el-alert
            title="温馨提示"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>如有任何问题或需要帮助，请通过以上方式联系我们。</p>
              <p>我们会在收到您的消息后尽快回复。</p>
            </template>
          </el-alert>
        </div>
      </div>
      
      <div class="contact-footer">
        <el-button @click="handleBack">返回</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Phone, Message } from '@element-plus/icons-vue'
import { getContactInfo } from '@/api/contact'

const router = useRouter()

const loading = ref(false)
const contactInfo = ref({
  phone: '',
  email: ''
})

const loadContactInfo = async () => {
  loading.value = true
  try {
    const res = await getContactInfo()
    if (res.code === 200) {
      contactInfo.value = res.data || {}
    }
  } catch (error) {
    ElMessage.error('获取联系方式失败')
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  // 如果有历史记录，返回上一页；否则返回首页
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

onMounted(() => {
  loadContactInfo()
})
</script>

<style scoped>
.contact-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
  padding: 20px;
}

.contact-card {
  width: 600px;
  max-width: 100%;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  gap: 10px;
}

.card-header .el-icon {
  font-size: 28px;
  color: #409EFF;
}

.contact-content {
  padding: 20px 0;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 30px;
  margin-bottom: 30px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
  border-left: 4px solid #409EFF;
}

.contact-icon {
  font-size: 32px;
  color: #409EFF;
  flex-shrink: 0;
}

.contact-detail {
  flex: 1;
}

.contact-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.contact-value {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  word-break: break-all;
}

.contact-tips {
  margin-top: 30px;
}

.contact-footer {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

/* 响应式设计 */
@media (max-width: 600px) {
  .contact-card {
    width: 100%;
  }
  
  .contact-item {
    flex-direction: column;
    text-align: center;
  }
}
</style>

