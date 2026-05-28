<template>
  <div class="member-center">
    <!-- 当前会员状态卡片 -->
    <el-card class="member-status-card">
      <template #header>
        <div class="card-header">
          <h2>会员中心</h2>
        </div>
      </template>

      <div class="member-info">
        <div class="member-badge">
          <el-tag :type="getMemberTagType(memberInfo.memberType)" size="large" effect="dark">
            {{ getMemberTypeText(memberInfo.memberType) }}
          </el-tag>
        </div>
        <div class="member-details" v-if="isMember">
          <div class="detail-item">
            <span class="label">到期时间：</span>
            <span class="value">{{ formatDateTime(memberInfo.memberExpireTime) }}</span>
          </div>
          <div class="detail-item" v-if="daysRemaining >= 0">
            <span class="label">剩余天数：</span>
            <span class="value highlight">{{ daysRemaining }} 天</span>
          </div>
          <div class="detail-item" v-else>
            <span class="label">状态：</span>
            <span class="value expired">已过期</span>
          </div>
        </div>
        <div class="member-details" v-else>
          <p class="no-member-tip">您还不是会员，购买会员享受更多优惠！</p>
        </div>
      </div>
    </el-card>

    <!-- 会员权益说明 -->
    <el-card class="benefits-card">
      <template #header>
        <div class="card-header">会员权益</div>
      </template>
      <div class="benefits-content">
        <div class="benefit-item vip-benefit">
          <div class="benefit-header">
            <el-tag type="warning" size="large">VIP会员</el-tag>
            <span class="benefit-price">¥{{ memberConfig.vip.price }}/月</span>
          </div>
          <ul class="benefit-list">
            <li>每月赠送 {{ calculateHoursFromPoints(memberConfig.vip.points) }} 小时免费学习时长（{{ memberConfig.vip.points }} 积分）</li>
            <li>预约座位享受 {{ memberConfig.vip.discountText }}优惠</li>
          </ul>
        </div>
        <div class="benefit-item svip-benefit">
          <div class="benefit-header">
            <el-tag type="danger" size="large">SVIP会员</el-tag>
            <span class="benefit-price">¥{{ memberConfig.svip.price }}/月</span>
          </div>
          <ul class="benefit-list">
            <li>每月赠送 {{ calculateHoursFromPoints(memberConfig.svip.points) }} 小时免费学习时长（{{ memberConfig.svip.points }} 积分）</li>
            <li>预约座位享受 {{ memberConfig.svip.discountText }}优惠</li>
            <li>可使用自习室提供的自助饮品服务</li>
          </ul>
        </div>
      </div>
    </el-card>

    <!-- 会员升级服务 -->
    <el-card v-if="memberInfo.memberType === 1 && isMember" class="upgrade-card">
      <template #header>
        <div class="card-header">
          <span>会员升级服务</span>
        </div>
      </template>
      <div class="upgrade-content">
        <div class="upgrade-info">
          <div class="upgrade-icon">
            <el-icon :size="48" color="#f56c6c"><ArrowUp /></el-icon>
          </div>
          <div class="upgrade-details">
            <h3 class="upgrade-title">VIP 升级为 SVIP</h3>
            <div class="upgrade-benefits">
              <div class="upgrade-item">
                <el-icon color="#67c23a"><Check /></el-icon>
                <span>享受更高级别的会员权益</span>
              </div>
              <div class="upgrade-item">
                <el-icon color="#67c23a"><Check /></el-icon>
                <span>升级费用：<strong style="color: #f56c6c">¥1.15/天</strong></span>
              </div>
              <div class="upgrade-item">
                <el-icon color="#67c23a"><Check /></el-icon>
                <span>升级赠送：<strong style="color: #67c23a">3 积分</strong></span>
              </div>
            </div>
            <div class="upgrade-notice">
              <el-alert
                type="info"
                :closable="false"
                show-icon
              >
                <template #title>
                  <span>如需升级服务，请联系管理员处理</span>
                </template>
              </el-alert>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 购买会员 -->
    <el-card class="purchase-card">
      <template #header>
        <div class="card-header">购买会员</div>
      </template>
      <div class="purchase-content">
        <div class="member-options">
          <div 
            class="member-option" 
            :class="{ active: selectedMemberType === 1 }"
            @click="selectedMemberType = 1"
          >
            <div class="option-header">
              <el-tag type="warning" size="large">VIP</el-tag>
              <span class="option-price">¥{{ memberConfig.vip.price }}</span>
            </div>
            <div class="option-benefits">
              <div class="benefit-text">✓ {{ memberConfig.vip.points }}积分（{{ calculateHoursFromPoints(memberConfig.vip.points) }}小时）</div>
              <div class="benefit-text">✓ 预约{{ memberConfig.vip.discountText }}</div>
            </div>
          </div>
          <div 
            class="member-option" 
            :class="{ active: selectedMemberType === 2 }"
            @click="selectedMemberType = 2"
          >
            <div class="option-header">
              <el-tag type="danger" size="large">SVIP</el-tag>
              <span class="option-price">¥{{ memberConfig.svip.price }}</span>
            </div>
            <div class="option-benefits">
              <div class="benefit-text">✓ {{ memberConfig.svip.points }}积分（{{ calculateHoursFromPoints(memberConfig.svip.points) }}小时）</div>
              <div class="benefit-text">✓ 预约{{ memberConfig.svip.discountText }}</div>
              <div class="benefit-text">✓ 自助饮品服务</div>
            </div>
          </div>
        </div>
        <div class="purchase-action">
          <el-button 
            type="primary" 
            size="large" 
            :disabled="!selectedMemberType"
            :loading="purchasing"
            @click="handlePurchase"
            style="width: 200px"
          >
            立即购买
          </el-button>
          <p class="purchase-tip">会员周期：购买当日起 30 个自然日</p>
        </div>
      </div>
    </el-card>

    <!-- 会员订单历史 -->
    <el-card class="orders-card">
      <template #header>
        <div class="card-header">
          <span>会员订单历史</span>
          <el-button @click="loadOrders">刷新</el-button>
        </div>
      </template>
      <el-table v-loading="ordersLoading" :data="pagedOrders" stripe style="width: 100%">
        <el-table-column prop="orderNumber" label="订单编号" width="200" />
        <el-table-column label="会员类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.memberType === 1 ? 'warning' : 'danger'">
              {{ row.memberType === 1 ? 'VIP' : 'SVIP' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="订单金额" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: 600">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusType(row)">
              {{ getOrderStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pointsAwarded" label="赠送积分" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #67c23a; font-weight: 600">+{{ row.pointsAwarded }}</span>
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="200">
          <template #default="{ row }">
            {{ formatDate(row.startDate) }} 至 {{ formatDate(row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <div v-if="(row.status === 0 || row.status === null) && row.paymentStatus === 0">
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
                @click="handleCancelOrder(row)"
              >
                取消
              </el-button>
            </div>
            <span v-else-if="row.status === 2" style="color: #909399">已取消</span>
            <span v-else style="color: #909399">已支付</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination" v-if="orderList.length > 0">
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :total="orderList.length"
          :page-size="orderPageSize"
          :current-page="orderCurrentPage"
          @current-change="handleOrderPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowUp, Check } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores'
import { getCurrentUserInfo } from '@/api/user'
import { createMemberOrder, createMemberPayment, getMyMemberOrders, cancelMemberOrder } from '@/api/member'
import { getMemberInfo } from '@/api/priceConfig'

const route = useRoute()
const userStore = useUserStore()

const memberInfo = ref({
  memberType: 0,
  memberExpireTime: null
})
const selectedMemberType = ref(null)
const purchasing = ref(false)
const ordersLoading = ref(false)
const orderList = ref([])
const orderPageSize = 10
const orderCurrentPage = ref(1)
const memberConfig = ref({
  vip: {
    price: 32.88,
    points: 150,
    discount: 0.9,
    discountText: '9折'
  },
  svip: {
    price: 65.88,
    points: 300,
    discount: 0.8,
    discountText: '8折'
  }
})

const isMember = computed(() => {
  if (!memberInfo.value.memberType || memberInfo.value.memberType === 0) {
    return false
  }
  if (!memberInfo.value.memberExpireTime) {
    return false
  }
  return dayjs(memberInfo.value.memberExpireTime).isAfter(dayjs())
})

const daysRemaining = computed(() => {
  if (!memberInfo.value.memberExpireTime) {
    return -1
  }
  const expireTime = dayjs(memberInfo.value.memberExpireTime)
  const now = dayjs()
  return expireTime.diff(now, 'day')
})

const getMemberTypeText = (memberType) => {
  if (memberType === 1) {
    return 'VIP会员'
  } else if (memberType === 2) {
    return 'SVIP会员'
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

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD')
}

const loadMemberInfo = async () => {
  try {
    const res = await getCurrentUserInfo()
    if (res.code === 200 && res.data) {
      userStore.setUserInfo(res.data)
      memberInfo.value = {
        memberType: res.data.memberType || 0,
        memberExpireTime: res.data.memberExpireTime
      }
    }
  } catch (error) {
    ElMessage.error('加载会员信息失败')
  }
}

const loadOrders = async () => {
  ordersLoading.value = true
  try {
    const res = await getMyMemberOrders()
    if (res.code === 200 && res.data) {
      orderList.value = res.data
      orderCurrentPage.value = 1
    }
  } catch (error) {
    ElMessage.error('加载订单列表失败')
  } finally {
    ordersLoading.value = false
  }
}

// 分页后的订单列表
const pagedOrders = computed(() => {
  const start = (orderCurrentPage.value - 1) * orderPageSize
  return orderList.value.slice(start, start + orderPageSize)
})

const handleOrderPageChange = (page) => {
  orderCurrentPage.value = page
}

const handlePurchase = async () => {
  if (!selectedMemberType.value) {
    ElMessage.warning('请选择会员类型')
    return
  }

  purchasing.value = true
  try {
    const res = await createMemberOrder(selectedMemberType.value)
    if (res.code === 200 && res.data) {
      ElMessage.success('订单创建成功，正在跳转支付...')
      // 跳转到支付
      await handlePay(res.data)
    }
  } catch (error) {
    ElMessage.error(error.message || '创建订单失败')
  } finally {
    purchasing.value = false
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
            会员订单已创建，请在 <b>5分钟内</b> 完成支付，若超时未支付，系统将自动取消该订单。
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
    
    const res = await createMemberPayment(order.id)
    
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

const handleCancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm(
      `
        <div style="line-height: 1.7">
          <div style="margin-bottom: 8px">
            确定要取消该会员订单吗？
          </div>
          <div style="color: #e6a23c">
            取消后该订单将无法支付，如需购买请重新下单。
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

    const res = await cancelMemberOrder(order.id)
    if (res.code === 200) {
      ElMessage.success(res.message || '取消成功')
      loadOrders()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消会员订单失败:', error)
      ElMessage.error(error.message || '取消失败，请稍后重试')
    }
  }
}

// 根据积分计算可兑换的小时数（30积分=2小时）
const calculateHoursFromPoints = (points) => {
  if (!points || points <= 0) {
    return 0
  }
  // 30积分 = 2小时，所以 积分 / 15 = 小时数
  return Math.floor(points / 15)
}

// 获取订单状态文本
const getOrderStatusText = (row) => {
  if (row.status === 2) {
    return '已取消'
  } else if (row.status === 1 || row.paymentStatus === 1) {
    return '已支付'
  } else {
    return '待支付'
  }
}

// 获取订单状态标签类型
const getOrderStatusType = (row) => {
  if (row.status === 2) {
    return 'info' // 已取消 - 灰色
  } else if (row.status === 1 || row.paymentStatus === 1) {
    return 'success' // 已支付 - 绿色
  } else {
    return 'warning' // 待支付 - 黄色
  }
}

// 加载会员配置信息
const loadMemberConfig = async () => {
  try {
    const res = await getMemberInfo()
    if (res.code === 200 && res.data) {
      if (res.data.vip) {
        memberConfig.value.vip = {
          price: parseFloat(res.data.vip.price) || 32.88,
          points: parseInt(res.data.vip.points) || 150,
          discount: parseFloat(res.data.vip.discount) || 0.9,
          discountText: res.data.vip.discountText || '9折'
        }
      }
      if (res.data.svip) {
        memberConfig.value.svip = {
          price: parseFloat(res.data.svip.price) || 65.88,
          points: parseInt(res.data.svip.points) || 300,
          discount: parseFloat(res.data.svip.discount) || 0.8,
          discountText: res.data.svip.discountText || '8折'
        }
      }
    }
  } catch (error) {
    console.error('加载会员配置失败:', error)
    // 失败时使用默认值
  }
}

onMounted(() => {
  loadMemberConfig() // 加载会员配置
  loadMemberInfo()
  loadOrders()
  
  // 检查是否有支付成功参数
  if (route.query.paySuccess === 'true') {
    ElMessage.success('支付成功！')
    loadMemberInfo()
    loadOrders()
  }
})
</script>

<style scoped>
.member-center {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.member-status-card {
  margin-bottom: 20px;
}

.member-info {
  text-align: center;
  padding: 30px 20px;
}

.member-badge {
  margin-bottom: 20px;
}

.member-details {
  margin-top: 20px;
}

.detail-item {
  margin: 10px 0;
  font-size: 16px;
}

.detail-item .label {
  color: #606266;
  margin-right: 10px;
}

.detail-item .value {
  color: #303133;
  font-weight: 600;
}

.detail-item .value.highlight {
  color: #409eff;
  font-size: 18px;
}

.detail-item .value.expired {
  color: #f56c6c;
}

.no-member-tip {
  color: #909399;
  font-size: 16px;
  margin: 20px 0;
}

.benefits-card {
  margin-bottom: 20px;
}

.benefits-content {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.benefit-item {
  flex: 1;
  min-width: 300px;
  padding: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  background: #fafafa;
}

.vip-benefit {
  border-color: #e6a23c;
}

.svip-benefit {
  border-color: #f56c6c;
}

.benefit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.benefit-price {
  font-size: 20px;
  font-weight: 600;
  color: #f56c6c;
}

.benefit-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.benefit-list li {
  padding: 8px 0;
  color: #606266;
  font-size: 14px;
}

.purchase-card {
  margin-bottom: 20px;
}

.purchase-content {
  padding: 20px 0;
}

.member-options {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.member-option {
  flex: 1;
  min-width: 250px;
  padding: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.member-option:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

.member-option.active {
  border-color: #409eff;
  background: #ecf5ff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.3);
}

.option-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.option-price {
  font-size: 24px;
  font-weight: 600;
  color: #f56c6c;
}

.option-benefits {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.benefit-text {
  color: #606266;
  font-size: 14px;
}

.purchase-action {
  text-align: center;
}

.purchase-tip {
  margin-top: 15px;
  color: #909399;
  font-size: 13px;
}

.orders-card {
  margin-bottom: 20px;
}

.upgrade-card {
  margin-bottom: 20px;
  border: 2px solid #f56c6c;
  background: linear-gradient(135deg, #fff5f5 0%, #ffffff 100%);
}

.upgrade-content {
  padding: 20px 0;
}

.upgrade-info {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.upgrade-icon {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f56c6c 0%, #ff8c8c 100%);
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}

.upgrade-details {
  flex: 1;
}

.upgrade-title {
  margin: 0 0 15px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.upgrade-benefits {
  margin-bottom: 20px;
}

.upgrade-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  font-size: 15px;
  color: #606266;
}

.upgrade-item:last-child {
  margin-bottom: 0;
}

.upgrade-notice {
  margin-top: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

