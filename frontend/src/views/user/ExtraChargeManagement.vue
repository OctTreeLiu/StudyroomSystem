<template>
  <div class="extra-charge-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>额外收费</span>
          <el-button @click="loadOrders">刷新</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="orderList" stripe style="width: 100%">
        <el-table-column prop="orderNumber" label="订单编号" width="200" />
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
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div v-if="row.paymentStatus === 0">
              <el-button
                type="primary"
                size="small"
                @click="handlePay(row)"
              >
                去支付
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleCancel(row)"
              >
                取消
              </el-button>
            </div>
            <span v-else style="color: #909399">
              {{ row.paymentStatus === 1 ? '已支付' : '已取消' }}
            </span>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyExtraChargeOrders, createExtraChargePayment, cancelExtraChargeOrder } from '@/api/extraCharge'
import dayjs from 'dayjs'

const route = useRoute()
const loading = ref(false)
const orderList = ref([])

const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getMyExtraChargeOrders(pagination.value.page, pagination.value.pageSize)
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

const handlePay = async (order) => {
  try {
    await ElMessageBox.confirm(
      `
        <div style="line-height: 1.7">
          <div style="margin-bottom: 8px">
            确认支付 <b>¥${order.amount}</b> 吗？
          </div>
          <div style="color: #e6a23c">
            额外收费订单已创建，请在 <b>24小时内</b> 完成支付，若超时未支付，系统将自动取消该订单。
          </div>
        </div>
      `,
      '确认支付',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }
    )
    
    const res = await createExtraChargePayment(order.id)
    
    if (res.code === 200 && res.data) {
      // 检查返回的数据是否是HTML表单
      if (typeof res.data === 'string' && res.data.includes('<form')) {
        // 创建表单并在当前标签页发起支付
        const div = document.createElement('div')
        div.innerHTML = res.data
        document.body.appendChild(div)
        
        // 提交表单
        const form = div.querySelector('form')
        if (form) {
          // 留在当前标签页，支付完成后由网关回跳携带 paySuccess 参数
          form.target = '_self'
          form.submit()
          document.body.removeChild(div)
          ElMessage.success('正在跳转到支付页面...')
        } else {
          document.body.removeChild(div)
          console.error('支付表单HTML:', res.data)
          ElMessage.error('支付表单格式错误，请查看控制台')
        }
      } else {
        console.error('支付表单数据:', res.data)
        ElMessage.error('支付表单生成失败，返回数据格式不正确')
      }
    } else {
      ElMessage.error(res.message || '创建支付订单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付错误:', error)
      ElMessage.error(error.message || '支付失败，请稍后重试')
    }
  }
}

const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm(
      `
        <div style="line-height: 1.7">
          <div style="margin-bottom: 8px">
            确定要取消该额外收费订单吗？
          </div>
          <div style="color: #e6a23c">
            取消后该订单将无法支付，如需缴费请联系管理员重新生成订单。
          </div>
        </div>
      `,
      '确认取消',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '我再想想',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }
    )

    const res = await cancelExtraChargeOrder(order.id)
    if (res.code === 200) {
      ElMessage.success(res.message || '取消成功')
      loadOrders()
      // 触发额外收费状态更新事件（刷新侧边栏红点）
      window.dispatchEvent(new Event('extra-charge-updated'))
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消额外收费订单失败:', error)
      ElMessage.error(error.message || '取消失败，请稍后重试')
    }
  }
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
  
  // 检查是否有支付成功参数
  if (route.query.paySuccess === 'true') {
    ElMessage.success('支付成功！')
    loadOrders()
    // 触发额外收费状态更新事件
    window.dispatchEvent(new Event('extra-charge-updated'))
  }
})

// 监听额外收费更新事件
window.addEventListener('extra-charge-updated', () => {
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

