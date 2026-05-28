import request from '@/utils/request'

/**
 * 获取所有预约记录（管理员）
 * @param {string} date - 可选，按日期查询（格式：YYYY-MM-DD）
 */
export function getAllReservations(date) {
  return request({
    url: '/admin/reservation/list',
    method: 'get',
    params: date ? { date } : {}
  })
}

/**
 * 根据ID获取预约详情（管理员）
 */
export function getReservationById(id) {
  return request({
    url: `/admin/reservation/${id}`,
    method: 'get'
  })
}

