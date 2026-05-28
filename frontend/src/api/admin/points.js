import request from '@/utils/request'

/**
 * 分页查询所有用户积分流水记录（管理员）
 */
export function getAllPointsHistory(page = 1, pageSize = 10, username = '') {
  return request({
    url: '/admin/points/history',
    method: 'get',
    params: {
      page,
      pageSize,
      username
    }
  })
}

/**
 * 管理员调整用户积分
 */
export function adjustPoints(userId, points, description = '') {
  return request({
    url: '/admin/points/adjust',
    method: 'post',
    params: {
      userId,
      points,
      description
    }
  })
}

