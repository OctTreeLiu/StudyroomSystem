import request from '@/utils/request'

/**
 * 获取自习室统计信息
 */
export function getStudyRoomStatistics() {
  return request({
    url: '/admin/statistics/studyroom',
    method: 'get'
  })
}

/**
 * 获取每个自习室的使用情况统计
 */
export function getRoomUsageStatistics() {
  return request({
    url: '/admin/statistics/room-usage',
    method: 'get'
  })
}

/**
 * 获取五个大区的统计信息
 */
export function getAreaStatistics() {
  return request({
    url: '/admin/statistics/area',
    method: 'get'
  })
}

/**
 * 根据时间段获取座位统计信息
 */
export function getSeatStatisticsByTimeRange(startTime, endTime) {
  return request({
    url: '/admin/statistics/seat/time-range',
    method: 'get',
    params: {
      startTime,
      endTime
    }
  })
}

/**
 * 根据时间段获取五个大区的统计信息
 */
export function getAreaStatisticsByTimeRange(startTime, endTime) {
  return request({
    url: '/admin/statistics/area/time-range',
    method: 'get',
    params: {
      startTime,
      endTime
    }
  })
}

