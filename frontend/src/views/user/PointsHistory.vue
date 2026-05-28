<template>
  <div class="points-history">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>积分流水记录</h2>
          <el-button @click="loadHistory">刷新</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="historyList" stripe style="width: 100%">
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分变动" width="120" align="center">
          <template #default="{ row }">
            <span :class="row.points > 0 ? 'points-increase' : 'points-decrease'">
              {{ row.points > 0 ? '+' : '' }}{{ row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" />
      </el-table>

      <!-- 分页组件 -->
      <div v-if="!loading && pagination.total > 0" class="pagination-container">
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
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getPointsHistory } from '@/api/points'

const loading = ref(false)
const historyList = ref([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const getTypeText = (type) => {
  const typeMap = {
    '签到': '签到',
    '预约奖励': '预约奖励',
    '积分兑换': '积分兑换',
    '会员赠送': '会员赠送',
    '长期租赁奖励': '长期租赁奖励',
    '管理员调整': '管理员调整'
  }
  return typeMap[type] || type
}

const getTypeTagType = (type) => {
  const typeMap = {
    '签到': 'success',
    '预约奖励': 'primary',
    '积分兑换': 'warning',
    '会员赠送': 'info',
    '长期租赁奖励': 'info',
    '管理员调整': 'danger'
  }
  return typeMap[type] || ''
}

const loadHistory = async () => {
  loading.value = true
  try {
    const res = await getPointsHistory(pagination.value.page, pagination.value.pageSize)
    if (res.code === 200 && res.data) {
      historyList.value = res.data.list || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载积分流水失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  pagination.value.page = page
  loadHistory()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.page = 1
  loadHistory()
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.points-history {
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

.points-increase {
  color: #67c23a;
  font-weight: 600;
}

.points-decrease {
  color: #f56c6c;
  font-weight: 600;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

