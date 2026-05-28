<template>
  <div class="study-time-chart-container">
    <div class="chart-card">
      <!-- 顶部统计信息 -->
      <div class="chart-header">
        <div class="total-time">
          <span class="time-label">今日总时长</span>
          <span class="time-value">{{ formatTotalTime(totalMinutes) }}</span>
        </div>
        <div v-if="comparisonText" class="comparison">
          {{ comparisonText }}
        </div>
      </div>

      <!-- 时间范围切换 -->
      <div class="period-selector">
        <el-radio-group v-model="currentPeriod" @change="handlePeriodChange" size="small">
          <el-radio-button label="24h">24小时</el-radio-button>
          <el-radio-button label="7d">一周</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 图表容器 -->
      <div class="chart-wrapper">
        <div ref="chartContainer" class="chart-container"></div>
      </div>

      <!-- 底部说明 -->
      <div class="chart-footer">
        <span class="footer-text">使用时长包括所有学习记录，可能超过设定的学习目标。了解更多</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  // 24小时数据：数组，每个元素 { hour: 0-23, minutes: number }
  hoursData: {
    type: Array,
    default: () => []
  },
  // 7天数据：数组，每个元素 { date: string, minutes: number }
  daysData: {
    type: Array,
    default: () => []
  },
  // 总时长（分钟）
  totalMinutes: {
    type: Number,
    default: 0
  },
  // 对比文本（如：比昨天少6小时12分钟）
  comparisonText: {
    type: String,
    default: ''
  },
  // 是否为管理员模式
  isAdmin: {
    type: Boolean,
    default: false
  }
})

const chartContainer = ref(null)
let chartInstance = null
const currentPeriod = ref('24h')

// 格式化总时长
const formatTotalTime = (minutes) => {
  if (!minutes || minutes === 0) {
    return '0分钟'
  }
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0 && mins > 0) {
    return `${hours}小时${mins}分钟`
  } else if (hours > 0) {
    return `${hours}小时`
  } else {
    return `${mins}分钟`
  }
}

// 处理时间段切换
const handlePeriodChange = () => {
  nextTick(() => {
    renderChart()
  })
}

// 渲染图表
const renderChart = () => {
  if (!chartContainer.value) return

  // 销毁旧图表
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }

  // 创建新图表
  chartInstance = echarts.init(chartContainer.value)

  let option = {}

  if (currentPeriod.value === '24h') {
    // 24小时柱状图
    option = get24HoursOption()
  } else {
    // 7天柱状图
    option = get7DaysOption()
  }

  chartInstance.setOption(option, true)

  // 响应式调整
  window.addEventListener('resize', handleResize)
}

// 获取24小时图表配置
const get24HoursOption = () => {
  // 准备数据：初始化24小时，每个小时0分钟
  const hoursDataMap = new Map()
  for (let i = 0; i < 24; i++) {
    hoursDataMap.set(i, 0)
  }

  // 填充实际数据
  if (props.hoursData && props.hoursData.length > 0) {
    props.hoursData.forEach(item => {
      const hour = typeof item.hour === 'number' ? item.hour : parseInt(item.hour)
      const minutes = item.minutes || 0
      hoursDataMap.set(hour, minutes)
    })
  }

  // 转换为数组
  const hours = Array.from({ length: 24 }, (_, i) => i)
  const minutes = hours.map(h => hoursDataMap.get(h) || 0)

  // 计算最大分钟数，用于设置Y轴
  const maxMinutes = Math.max(...minutes, 60) // 至少60分钟，确保有网格线

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        const param = params[0]
        const hour = param.name
        const mins = param.value
        const hours = Math.floor(mins / 60)
        const remainingMins = mins % 60
        if (hours > 0 && remainingMins > 0) {
          return `${hour}<br/>学习时长: ${hours}小时${remainingMins}分钟`
        } else if (hours > 0) {
          return `${hour}<br/>学习时长: ${hours}小时`
        } else {
          return `${hour}<br/>学习时长: ${remainingMins}分钟`
        }
      }
    },
    grid: {
      left: '10%',
      right: '5%',
      bottom: '15%',
      top: '10%',
      containLabel: false
    },
    xAxis: {
      type: 'category',
      data: hours.map(h => {
        // 格式化小时为时间字符串
        const hourStr = `${String(h).padStart(2, '0')}:00`
        // 只显示关键时间点：00:00, 06:00, 12:00, 18:00, 24:00
        const keyHours = [0, 6, 12, 18, 23]
        return keyHours.includes(h) ? hourStr : ''
      }),
      boundaryGap: true,
      axisLabel: {
        interval: 0, // 显示所有标签，但空字符串不会显示
        fontSize: 12,
        color: '#666'
      },
      axisTick: {
        show: false
      },
      axisLine: {
        lineStyle: {
          color: '#e0e0e0'
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '分钟',
      nameTextStyle: {
        color: '#666',
        fontSize: 12
      },
      min: 0,
      max: maxMinutes,
      interval: maxMinutes >= 60 ? 30 : 15, // 如果最大值>=60，间隔30分钟，否则15分钟
      axisLabel: {
        formatter: '{value} min',
        fontSize: 12,
        color: '#666'
      },
      splitLine: {
        show: true,
        lineStyle: {
          type: 'dashed',
          color: '#e0e0e0'
        }
      },
      axisLine: {
        show: false
      }
    },
    series: [
      {
        name: '学习时长',
        type: 'bar',
        data: minutes,
        barWidth: '60%',
        itemStyle: {
          color: '#409EFF',
          borderRadius: [4, 4, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: '#66B1FF'
          }
        },
        label: {
          show: false
        }
      }
    ]
  }
}

// 获取7天图表配置
const get7DaysOption = () => {
  // 准备数据：获取最近7天
  const days = []
  const minutes = []

  // 创建最近7天的日期数组
  const today = new Date()
  const dateMap = new Map()

  // 初始化7天数据
  for (let i = 6; i >= 0; i--) {
    const date = new Date(today)
    date.setDate(date.getDate() - i)
    const dateStr = date.toISOString().split('T')[0]
    const dayLabel = `${date.getMonth() + 1}/${date.getDate()}`
    days.push(dayLabel)
    dateMap.set(dateStr, 0)
  }

  // 填充实际数据
  if (props.daysData && props.daysData.length > 0) {
    props.daysData.forEach(item => {
      const dateStr = item.date || item.day
      const mins = item.minutes || 0
      if (dateMap.has(dateStr)) {
        dateMap.set(dateStr, mins)
      }
    })
  }

  // 重新构建，确保days和minutes数组顺序一致
  const finalDays = []
  const finalMinutes = []
  for (let i = 6; i >= 0; i--) {
    const date = new Date(today)
    date.setDate(date.getDate() - i)
    const dateStr = date.toISOString().split('T')[0]
    const dayLabel = `${date.getMonth() + 1}/${date.getDate()}`
    finalDays.push(dayLabel)
    finalMinutes.push(dateMap.get(dateStr) || 0)
  }
  
  // 使用重新构建的数组
  days.length = 0
  minutes.length = 0
  days.push(...finalDays)
  minutes.push(...finalMinutes)

  // 计算最大分钟数
  const maxMinutes = Math.max(...minutes, 60)

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        const param = params[0]
        const date = param.name
        const mins = param.value
        const hours = Math.floor(mins / 60)
        const remainingMins = mins % 60
        if (hours > 0 && remainingMins > 0) {
          return `${date}<br/>学习时长: ${hours}小时${remainingMins}分钟`
        } else if (hours > 0) {
          return `${date}<br/>学习时长: ${hours}小时`
        } else {
          return `${date}<br/>学习时长: ${remainingMins}分钟`
        }
      }
    },
    grid: {
      left: '10%',
      right: '5%',
      bottom: '15%',
      top: '10%',
      containLabel: false
    },
    xAxis: {
      type: 'category',
      data: days,
      boundaryGap: true,
      axisLabel: {
        interval: 0,
        fontSize: 12,
        color: '#666'
      },
      axisTick: {
        show: false
      },
      axisLine: {
        lineStyle: {
          color: '#e0e0e0'
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '',
      nameTextStyle: {
        color: '#666',
        fontSize: 12
      },
      min: 0,
      max: maxMinutes,
      interval: maxMinutes >= 60 ? 30 : 15,
      axisLabel: {
        show: false // 隐藏纵轴标签，但保留网格线和tooltip
      },
      splitLine: {
        show: true,
        lineStyle: {
          type: 'dashed',
          color: '#e0e0e0'
        }
      },
      axisLine: {
        show: false
      }
    },
    series: [
      {
        name: '学习时长',
        type: 'bar',
        data: minutes,
        barWidth: '60%',
        itemStyle: {
          color: '#409EFF',
          borderRadius: [4, 4, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: '#66B1FF'
          }
        },
        label: {
          show: false
        }
      }
    ]
  }
}

// 处理窗口大小变化
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

// 监听数据变化
watch(() => [props.hoursData, props.daysData], () => {
  nextTick(() => {
    renderChart()
  })
}, { deep: true })

onMounted(() => {
  nextTick(() => {
    renderChart()
  })
})

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.study-time-chart-container {
  margin-top: 20px;
}

.chart-card {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-header {
  margin-bottom: 20px;
}

.total-time {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.time-label {
  font-size: 14px;
  color: #909399;
}

.time-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.comparison {
  font-size: 14px;
  color: #606266;
  margin-top: 8px;
}

.period-selector {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}

.chart-wrapper {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
}

.chart-container {
  width: 100%;
  height: 400px;
}

.chart-footer {
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}

.footer-text {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chart-card {
    padding: 16px;
  }

  .time-value {
    font-size: 24px;
  }

  .chart-container {
    height: 300px;
  }

  .period-selector {
    justify-content: center;
  }
}
</style>

