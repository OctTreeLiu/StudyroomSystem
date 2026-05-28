<template>
  <div class="memo-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>备忘录</span>
          <el-button type="primary" @click="openCreateDialog">新建备忘录</el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :inline="true">
          <el-form-item label="日期筛选">
            <el-date-picker
              v-model="filterDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              clearable
              @change="handleFilterChange"
            />
          </el-form-item>
          <el-form-item label="状态筛选">
            <el-select v-model="filterStatus" placeholder="全部状态" clearable @change="handleFilterChange">
              <el-option label="未处理" :value="0" />
              <el-option label="已处理" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button @click="loadMemos">刷新</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 备忘录列表 -->
      <el-table v-loading="loading" :data="memoList" stripe style="width: 100%">
        <el-table-column prop="title" label="主题" min-width="200" />
        <el-table-column prop="memoDate" label="日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.memoDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="memoForm" :rules="rules" ref="memoFormRef" label-width="100px">
        <el-form-item label="主题" prop="title">
          <el-input v-model="memoForm.title" placeholder="请输入备忘录主题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="日期" prop="memoDate">
          <el-date-picker
            v-model="memoForm.memoDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="memoForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入备忘录内容"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="memoForm.status">
            <el-radio :label="0">未处理</el-radio>
            <el-radio :label="1">已处理</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { createMemo, updateMemo, deleteMemo, getMyMemos } from '@/api/memo'

const loading = ref(false)
const submitLoading = ref(false)
const memoList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新建备忘录')
const currentMemoId = ref(null)
const filterDate = ref(null)
const filterStatus = ref(null)
const memoFormRef = ref(null)
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

const memoForm = reactive({
  title: '',
  memoDate: '',
  content: '',
  status: 0
})

const rules = {
  title: [
    { required: true, message: '请输入主题', trigger: 'blur' },
    { max: 200, message: '主题长度不能超过200个字符', trigger: 'blur' }
  ],
  memoDate: [
    { required: true, message: '请选择日期', trigger: 'change' }
  ],
  content: [
    { max: 1000, message: '内容长度不能超过1000个字符', trigger: 'blur' }
  ]
}


const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD')
}

const loadMemos = async () => {
  loading.value = true
  try {
    const res = await getMyMemos(
      pagination.value.page,
      pagination.value.pageSize,
      filterDate.value,
      filterStatus.value
    )
    if (res.code === 200 && res.data) {
      memoList.value = res.data.list || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载备忘录列表失败')
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  pagination.value.page = 1 // 筛选时重置到第一页
  loadMemos()
}

const handlePageChange = (page) => {
  pagination.value.page = page
  loadMemos()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.page = 1
  loadMemos()
}

const openCreateDialog = () => {
  dialogTitle.value = '新建备忘录'
  currentMemoId.value = null
  memoForm.title = ''
  memoForm.memoDate = dayjs().format('YYYY-MM-DD')
  memoForm.content = ''
  memoForm.status = 0
  dialogVisible.value = true
}

const openEditDialog = (memo) => {
  dialogTitle.value = '编辑备忘录'
  currentMemoId.value = memo.id
  memoForm.title = memo.title || ''
  memoForm.memoDate = memo.memoDate || ''
  memoForm.content = memo.content || ''
  memoForm.status = memo.status !== null && memo.status !== undefined ? memo.status : 0
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!memoFormRef.value) return

  await memoFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (currentMemoId.value) {
          // 更新
          const res = await updateMemo(currentMemoId.value, memoForm)
          if (res.code === 200) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            loadMemos()
            // 触发侧边栏检查（通过事件或直接调用）
            window.dispatchEvent(new CustomEvent('memo-updated'))
          }
        } else {
          // 创建
          const res = await createMemo(memoForm)
          if (res.code === 200) {
            ElMessage.success('创建成功')
            dialogVisible.value = false
            loadMemos()
            // 触发侧边栏检查
            window.dispatchEvent(new CustomEvent('memo-updated'))
          }
        }
      } catch (error) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = async (memo) => {
  try {
    await ElMessageBox.confirm('确定要删除此备忘录吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deleteMemo(memo.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadMemos()
      // 触发侧边栏检查
      window.dispatchEvent(new CustomEvent('memo-updated'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadMemos()
})
</script>

<style scoped>
.memo-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.filter-section {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>

