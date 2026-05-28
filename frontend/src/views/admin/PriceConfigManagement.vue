<template>
  <div class="price-config-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>定价管理</span>
          <el-button type="primary" :loading="saving" @click="handleSaveAll">保存所有配置</el-button>
        </div>
      </template>

      <el-alert
        title="定价配置说明"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      >
        <template #default>
          <p>• 折扣填写格式为小数（如 0.8 表示 8 折，0.9 表示 9 折）</p>
          <p>• 折扣值必须在 0-1 之间，最多保留两位小数</p>
          <p>• 价格和积分值必须大于等于 0</p>
          <p>• 修改配置后需要点击"保存所有配置"按钮才能生效</p>
        </template>
      </el-alert>

      <el-table v-loading="loading" :data="configList" stripe style="width: 100%">
        <el-table-column prop="description" label="说明" min-width="250" />
        <el-table-column label="配置值" min-width="250">
          <template #default="{ row }">
            <el-input
              v-model="row.configValue"
              :placeholder="getPlaceholder(row.configKey)"
              clearable
              @blur="validateValue(row)"
            >
              <template #append v-if="isDiscount(row.configKey)">
                <span style="color: #909399; font-size: 12px">
                  {{ formatDiscount(row.configValue) }}
                </span>
              </template>
            </el-input>
          </template>
        </el-table-column>
        <el-table-column label="单位" width="100" align="center">
          <template #default="{ row }">
            <span v-if="isPrice(row.configKey)" style="color: #909399">元</span>
            <span v-else-if="isDiscount(row.configKey)" style="color: #909399">折</span>
            <span v-else-if="isPoints(row.configKey)" style="color: #909399">积分</span>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllPriceConfigs, batchUpdatePriceConfigs } from '@/api/admin/priceConfig'

const loading = ref(false)
const saving = ref(false)
const configList = ref([])

// 配置说明映射
const configDescriptions = {
  RESERVATION_PRICE_PER_HOUR: '预约每小时价格',
  LONG_LEASE_PRICE_PER_DAY: '长期租赁一天价格',
  VIP_PRICE: 'VIP 价格',
  SVIP_PRICE: 'SVIP 价格',
  VIP_DISCOUNT: 'VIP 折扣',
  SVIP_DISCOUNT: 'SVIP 折扣',
  VIP_POINTS: 'VIP 所送积分',
  SVIP_POINTS: 'SVIP 所送积分'
}

// 加载配置列表
const loadConfigs = async () => {
  loading.value = true
  try {
    const res = await getAllPriceConfigs()
    if (res.code === 200) {
      // 确保所有配置都有描述
      configList.value = res.data.configs.map(config => ({
        ...config,
        description: config.description || configDescriptions[config.configKey] || config.configKey
      }))
    } else {
      ElMessage.error(res.message || '获取配置失败')
    }
  } catch (error) {
    console.error('获取配置失败:', error)
    ElMessage.error('获取配置失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 判断是否为价格配置
const isPrice = (configKey) => {
  return configKey && configKey.includes('PRICE')
}

// 判断是否为折扣配置
const isDiscount = (configKey) => {
  return configKey && configKey.includes('DISCOUNT')
}

// 判断是否为积分配置
const isPoints = (configKey) => {
  return configKey && configKey.includes('POINTS')
}

// 获取占位符
const getPlaceholder = (configKey) => {
  if (isDiscount(configKey)) {
    return '请输入0-1之间的小数，如0.8表示8折'
  } else if (isPrice(configKey) || isPoints(configKey)) {
    return '请输入数值'
  }
  return '请输入配置值'
}

// 格式化折扣显示
const formatDiscount = (value) => {
  if (!value) return ''
  try {
    const num = parseFloat(value)
    if (isNaN(num)) return ''
    const percent = (num * 10).toFixed(1)
    return `${percent}折`
  } catch {
    return ''
  }
}

// 验证配置值
const validateValue = (row) => {
  const { configKey, configValue } = row
  
  if (!configValue || configValue.trim() === '') {
    ElMessage.warning(`${configDescriptions[configKey] || configKey} 不能为空`)
    return false
  }
  
  // 验证折扣
  if (isDiscount(configKey)) {
    try {
      const num = parseFloat(configValue.trim())
      if (isNaN(num)) {
        ElMessage.warning('折扣值必须是有效的数字')
        return false
      }
      if (num < 0 || num > 1) {
        ElMessage.warning('折扣值必须在0-1之间')
        return false
      }
      // 检查小数位数
      const parts = configValue.trim().split('.')
      if (parts.length > 1 && parts[1].length > 2) {
        ElMessage.warning('折扣值最多保留两位小数')
        return false
      }
    } catch {
      ElMessage.warning('折扣值格式不正确')
      return false
    }
  }
  
  // 验证价格
  if (isPrice(configKey)) {
    try {
      const num = parseFloat(configValue.trim())
      if (isNaN(num)) {
        ElMessage.warning('价格必须是有效的数字')
        return false
      }
      if (num <= 0) {
        ElMessage.warning('价格必须大于0')
        return false
      }
    } catch {
      ElMessage.warning('价格格式不正确')
      return false
    }
  }
  
  // 验证积分
  if (isPoints(configKey)) {
    try {
      const num = parseInt(configValue.trim())
      if (isNaN(num)) {
        ElMessage.warning('积分必须是有效的整数')
        return false
      }
      if (num < 0) {
        ElMessage.warning('积分不能为负数')
        return false
      }
    } catch {
      ElMessage.warning('积分格式不正确')
      return false
    }
  }
  
  return true
}

// 保存所有配置
const handleSaveAll = async () => {
  // 验证所有配置
  let hasError = false
  for (const config of configList.value) {
    if (!validateValue(config)) {
      hasError = true
      break
    }
  }
  
  if (hasError) {
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要保存所有配置吗？保存后立即生效。', '确认保存', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  
  saving.value = true
  try {
    // 准备更新数据（只包含需要更新的字段）
    const updateData = configList.value.map(config => ({
      configKey: config.configKey,
      configValue: config.configValue.trim(),
      description: config.description
    }))
    
    const res = await batchUpdatePriceConfigs(updateData)
    if (res.code === 200) {
      ElMessage.success('配置保存成功')
      // 重新加载配置
      await loadConfigs()
    } else {
      ElMessage.error(res.message || '保存配置失败')
    }
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存配置失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped>
.price-config-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

