import request from '@/utils/request'

/**
 * 根据自习室ID获取座位列表
 */
export function getSeatsByRoomId(roomId) {
  return request({
    url: `/seat/list/room/${roomId}`,
    method: 'get'
  })
}

/**
 * 根据ID获取座位详情
 */
export function getSeatById(id) {
  return request({
    url: `/seat/${id}`,
    method: 'get'
  })
}

/**
 * 根据时间段和自习室ID获取可用座位
 */
export function getAvailableSeats(roomId, startTime, endTime) {
  return request({
    url: `/seat/available`,
    method: 'get',
    params: {
      roomId,
      startTime,
      endTime
    }
  })
}

/**
 * 获取座位使用情况时间轴（普通用户版本，不返回用户名）
 */
export function getSeatUsageTimeline(seatId, date) {
  return request({
    url: `/seat/${seatId}/timeline`,
    method: 'get',
    params: {
      date
    }
  })
}

