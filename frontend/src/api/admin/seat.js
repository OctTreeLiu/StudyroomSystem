import request from '@/utils/request'

/**
 * 获取所有座位（管理员）
 */
export function getAllSeats() {
  return request({
    url: '/admin/seat/list',
    method: 'get'
  })
}

/**
 * 根据自习室ID获取座位列表（管理员）
 */
export function getSeatsByRoomId(roomId) {
  return request({
    url: `/admin/seat/list/room/${roomId}`,
    method: 'get'
  })
}

/**
 * 更新座位状态（管理员）
 */
export function updateSeatStatus(id, status) {
  return request({
    url: `/admin/seat/update-status/${id}`,
    method: 'put',
    data: { status }
  })
}

/**
 * 根据ID获取座位详情（管理员）
 */
export function getSeatById(id) {
  return request({
    url: `/admin/seat/${id}`,
    method: 'get'
  })
}

/**
 * 获取座位使用情况时间轴（管理员）
 */
export function getSeatUsageTimeline(seatId, date) {
  return request({
    url: `/admin/seat/${seatId}/timeline`,
    method: 'get',
    params: {
      date
    }
  })
}

