<template>
  <div class="lease-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的长期租赁</span>
          <el-button type="primary" @click="$router.push('/user/lease/apply')">新建申请</el-button>
        </div>
      </template>

      <!-- 筛选区域 -->
      <div class="filter-section">
        <el-select 
          v-model="filterRoom" 
          placeholder="筛选大区" 
          style="width: 180px; margin-right: 10px" 
          @change="handleFilterChange" 
          clearable
        >
          <el-option label="全部" value="" />
          <el-option
            v-for="room in roomOptions"
            :key="room.roomId"
            :label="`${room.roomNumber} - ${room.roomName}`"
            :value="room.roomId"
          />
        </el-select>
        <el-select 
          v-model="filterSeat" 
          placeholder="筛选座位" 
          style="width: 180px; margin-right: 10px" 
          @change="handleFilterChange" 
          clearable
        >
          <el-option label="全部" value="" />
          <el-option
            v-for="seat in seatOptions"
            :key="seat.seatId"
            :label="seat.seatNumber + (seat.seatName ? ` - ${seat.seatName}` : '')"
            :value="seat.seatId"
          />
        </el-select>
        <el-select 
          v-model="filterStatus" 
          placeholder="筛选状态" 
          style="width: 150px; margin-right: 10px" 
          @change="handleFilterChange"
          clearable
        >
          <el-option label="全部" value="" />
          <el-option label="待审核" :value="0" />
          <el-option label="审核通过待付款" :value="1" />
          <el-option label="已付款生效" :value="2" />
          <el-option label="已拒绝" :value="3" />
          <el-option label="已取消" :value="4" />
          <el-option label="使用中" :value="5" />
          <el-option label="使用结束" :value="6" />
        </el-select>
        <el-button type="info" @click="handleResetFilter">重置筛选</el-button>
      </div>

      <el-table v-loading="loading" :data="pagedLeases" stripe style="width: 100%">
        <el-table-column prop="leaseNumber" label="租赁编号" width="180" />
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
        <el-table-column prop="startDate" label="开始日期" width="120">
          <template #default="{ row }">
            {{ row.startDate }}
          </template>
        </el-table-column>
        <el-table-column prop="endDate" label="结束日期" width="120">
          <template #default="{ row }">
            {{ row.endDate }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100" align="center">
          <template #default="{ row }">
            ¥{{ formatAmount(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="支付时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.paymentTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.paymentStatus === 1 ? 'success' : 'warning'">
              {{ row.paymentStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" align="center" fixed="right">
          <template #default="{ row }">
            <el-tooltip
              v-if="row.status === 1 && row.paymentStatus === 0"
              content="订单提交成功，请在2小时内完成支付，若超时未支付，系统将自动取消订单。（以付款截止时间为准）"
              placement="top"
            >
              <el-button
                type="success"
                size="small"
                @click="handlePay(row)"
              >
                支付
              </el-button>
            </el-tooltip>
            <el-button
              v-if="row.status === 1 && row.paymentStatus === 0"
              type="danger"
              size="small"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button type="primary" size="small" @click="viewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 详情对话框 -->
      <el-dialog v-model="detailVisible" title="租赁详情" width="600px">
        <div v-if="currentLease">
          <el-alert
            title="重要提示"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 12px"
          >
            <template #default>
              <div style="line-height: 1.7">
                <div>长期租赁订单一经支付即为锁定座位资源，在租赁周期内该座位将无法被其他用户预约。</div>
                <div>为保障自习室资源的合理使用，长期租赁订单支付成功后将 <b>不支持取消及退款</b>。</div>
                <div>请在确认使用需求后再进行购买，感谢您的理解与配合。</div>
              </div>
            </template>
          </el-alert>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="租赁编号">{{ currentLease.leaseNumber }}</el-descriptions-item>
            <el-descriptions-item label="申请时间">
              {{ formatTime(currentLease.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="大区">
              {{ currentLease.roomNumber }} - {{ currentLease.roomName }}
            </el-descriptions-item>
            <el-descriptions-item label="座位" v-if="currentLease.seatNumber">
              {{ currentLease.seatNumber }}<span v-if="currentLease.seatName"> - {{ currentLease.seatName }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="开始日期">{{ currentLease.startDate }}</el-descriptions-item>
            <el-descriptions-item label="结束日期">{{ currentLease.endDate }}</el-descriptions-item>
            <el-descriptions-item label="金额">¥{{ formatAmount(currentLease.amount) }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">
              {{ formatTime(currentLease.paymentTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="支付状态">
              <el-tag :type="currentLease.paymentStatus === 1 ? 'success' : 'warning'">
                {{ currentLease.paymentStatus === 1 ? '已支付' : '未支付' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(currentLease.status)">
                {{ getStatusText(currentLease.status) }}
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
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getMyLeases, createLeasePayment, cancelLease } from '@/api/lease'

const loading = ref(false)
const pageSize = 10
const currentPage = ref(1)
const leaseList = ref([])
const detailVisible = ref(false)
const currentLease = ref(null)
const route = useRoute()
const filterRoom = ref('')
const filterSeat = ref('')
const filterStatus = ref('')

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const formatAmount = (amount) => {
  if (amount == null || amount === undefined || amount === '') return '0.00'
  // 确保金额始终显示两位小数
  const num = typeof amount === 'string' ? parseFloat(amount) : Number(amount)
  if (isNaN(num) || num < 0) return '0.00'
  return num.toFixed(2)
}

const getStatusText = (status) => {
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
  return statusMap[status] || '未知'
}

const getStatusType = (status) => {
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
  return typeMap[status] || 'info'
}

const loadLeases = async () => {
  loading.value = true
  try {
    const res = await getMyLeases()
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

const handlePay = async (row) => {
  try {
    await ElMessageBox.confirm(
      `
        <div style="line-height: 1.7">
          <div style="margin-bottom: 8px">
            确认支付 <b>¥${formatAmount(row.amount)}</b> 吗？付款截止时间：${formatTime(row.paymentDeadline)}
          </div>
            <div style="margin-bottom: 8px; color: #e6a23c">
            订单提交成功，请在 <b>2小时内</b> 完成支付，若超时未支付，系统将自动取消订单。（以付款截止时间为准）
          </div>
          <div style="color: #606266">
            <div>长期租赁订单一经支付即为锁定座位资源，在租赁周期内该座位将无法被其他用户预约。</div>
            <div>为保障自习室资源的合理使用，长期租赁订单支付成功后将 <b>不支持取消及退款</b>。</div>
            <div>请在确认使用需求后再进行购买，感谢您的理解与配合。</div>
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
    
    loading.value = true
    const res = await createLeasePayment(row.id)
    loading.value = false

    if (res.code === 200 && res.data) {
      if (typeof res.data === 'string' && res.data.includes('<form')) {
        const div = document.createElement('div')
        div.innerHTML = res.data
        document.body.appendChild(div)

        const form = div.querySelector('form')
        if (form) {
          // 留在当前标签页支付，支付后网关回跳当前页面
          form.target = '_self'
          form.submit()
          document.body.removeChild(div)
          ElMessage.success('正在跳转到支付页面...')
        } else {
          document.body.removeChild(div)
          console.error('长期租赁支付表单HTML:', res.data)
          ElMessage.error('支付表单格式错误，请查看控制台')
        }
      } else {
        console.error('长期租赁支付表单数据:', res.data)
        ElMessage.error('支付表单生成失败，返回数据格式不正确')
      }
    } else {
      ElMessage.error(res.message || '创建支付订单失败')
    }
  } catch (error) {
    loading.value = false
    if (error !== 'cancel') {
      console.error('长期租赁支付错误:', error)
      ElMessage.error(error.message || '支付失败，请稍后重试')
    }
  }
}

const viewDetail = (lease) => {
  currentLease.value = lease
  detailVisible.value = true
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `
        <div style="line-height: 1.7">
          <div style="margin-bottom: 8px">
            确定要取消该长期租赁申请吗？
          </div>
          <div style="color: #e6a23c">
            取消后该申请将无法恢复，如需使用请重新提交申请。
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
    
    loading.value = true
    const res = await cancelLease(row.id)
    loading.value = false

    if (res.code === 200) {
      ElMessage.success('取消成功')
      // 重新加载列表
      await loadLeases()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (error) {
    loading.value = false
    if (error !== 'cancel') {
      console.error('取消长期租赁错误:', error)
      ElMessage.error(error.message || '取消失败，请稍后重试')
    }
  }
}

onMounted(() => {
  loadLeases()

  // 检查是否从支付回调返回
  if (route.query.paySuccess === 'true') {
    ElMessage.success('支付成功，正在刷新租赁状态...')
    // 清除URL参数并刷新列表
    window.history.replaceState({}, '', '/user/lease')
    setTimeout(() => {
      loadLeases()
    }, 1000)
  }
})

// 获取所有唯一的大区选项
const roomOptions = computed(() => {
  const roomMap = new Map()
  leaseList.value.forEach(lease => {
    if (lease.roomId && lease.roomName) {
      if (!roomMap.has(lease.roomId)) {
        roomMap.set(lease.roomId, {
          roomId: lease.roomId,
          roomName: lease.roomName,
          roomNumber: lease.roomNumber
        })
      }
    }
  })
  return Array.from(roomMap.values()).sort((a, b) => {
    // 按大区编号排序
    if (a.roomNumber && b.roomNumber) {
      return a.roomNumber.localeCompare(b.roomNumber)
    }
    return 0
  })
})

// 获取所有唯一的座位选项
const seatOptions = computed(() => {
  const seatMap = new Map()
  leaseList.value.forEach(lease => {
    if (lease.seatId && lease.seatNumber) {
      if (!seatMap.has(lease.seatId)) {
        seatMap.set(lease.seatId, {
          seatId: lease.seatId,
          seatNumber: lease.seatNumber,
          seatName: lease.seatName
        })
      }
    }
  })
  return Array.from(seatMap.values()).sort((a, b) => {
    // 按座位号排序
    if (a.seatNumber && b.seatNumber) {
      return a.seatNumber.localeCompare(b.seatNumber)
    }
    return 0
  })
})

// 筛选后的租赁列表
const filteredLeases = computed(() => {
  let result = leaseList.value
  
  // 大区筛选
  if (filterRoom.value !== '') {
    result = result.filter(l => l.roomId === filterRoom.value)
  }
  
  // 座位筛选
  if (filterSeat.value !== '') {
    result = result.filter(l => l.seatId === filterSeat.value)
  }
  
  // 状态筛选
  if (filterStatus.value !== '') {
    result = result.filter(l => l.status === filterStatus.value)
  }
  
  return result
})

const pagedLeases = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredLeases.value.slice(start, start + pageSize)
})

const handlePageChange = (page) => {
  currentPage.value = page
}

const handleFilterChange = () => {
  // 筛选时重置到第一页
  currentPage.value = 1
}

const handleResetFilter = () => {
  filterRoom.value = ''
  filterSeat.value = ''
  filterStatus.value = ''
  currentPage.value = 1
}
</script>

<style scoped>
.lease-list {
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

.filter-section {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
</style>

