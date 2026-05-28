<template>
  <div class="call-admin-button" v-if="shouldShow">
    <el-button
      type="primary"
      :icon="Phone"
      circle
      size="large"
      class="call-btn"
      @click="showDialog = true"
    />
    
    <!-- 呼叫对话框 -->
    <el-dialog
      v-model="showDialog"
      title="呼叫管理员"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="callForm" label-width="80px">
        <el-form-item label="留言">
          <el-input
            v-model="callForm.message"
            type="textarea"
            :rows="4"
            placeholder="请输入您需要帮助的内容（选填）"
            :maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleCall">确定呼叫</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Phone } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'
import { callAdmin } from '@/api/adminCall'

const userStore = useUserStore()
const route = useRoute()

const isLogin = computed(() => userStore.isLogin)
// 在管理员页面不显示呼叫按钮
const shouldShow = computed(() => {
  return isLogin.value && !route.path.startsWith('/admin')
})

const showDialog = ref(false)
const loading = ref(false)
const callForm = ref({
  message: ''
})

const handleCall = async () => {
  loading.value = true
  try {
    const res = await callAdmin(callForm.value)
    if (res.code === 200) {
      ElMessage.success('呼叫成功，管理员将尽快联系您')
      showDialog.value = false
      callForm.value.message = ''
    }
  } catch (error) {
    ElMessage.error(error.message || '呼叫失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.call-admin-button {
  position: fixed;
  right: 30px;
  bottom: 30px;
  z-index: 1000;
}

.call-btn {
  width: 60px;
  height: 60px;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s;
}

.call-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .call-admin-button {
    right: 20px;
    bottom: 20px;
  }
  
  .call-btn {
    width: 50px;
    height: 50px;
    font-size: 20px;
  }
}
</style>

