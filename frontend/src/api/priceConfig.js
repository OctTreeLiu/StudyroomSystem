import request from '@/utils/request'

/**
 * 获取预约每小时价格（用户端）
 */
export function getReservationPrice() {
  return request({
    url: '/price-config/reservation-price',
    method: 'get'
  })
}

/**
 * 获取长期租赁每天价格（用户端）
 */
export function getLeasePrice() {
  return request({
    url: '/price-config/lease-price',
    method: 'get'
  })
}

/**
 * 获取会员价格和积分信息（用户端）
 */
export function getMemberInfo() {
  return request({
    url: '/price-config/member-info',
    method: 'get'
  })
}

