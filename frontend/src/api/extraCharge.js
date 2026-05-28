import request from '@/utils/request'

/**
 * 创建额外收费订单（管理员）
 */
export function createExtraCharge(data) {
  return request({
    url: '/admin/extra-charge/create',
    method: 'post',
    data
  })
}

/**
 * 获取所有额外收费订单列表（管理员，分页）
 */
export function getAllExtraChargeOrders(page = 1, pageSize = 10) {
  return request({
    url: '/admin/extra-charge/list',
    method: 'get',
    params: {
      page,
      pageSize
    }
  })
}

/**
 * 统计未支付订单数量（管理员）
 */
export function countUnpaidExtraChargeOrders() {
  return request({
    url: '/admin/extra-charge/count-unpaid',
    method: 'get'
  })
}

/**
 * 获取我的额外收费订单列表（用户，分页）
 */
export function getMyExtraChargeOrders(page = 1, pageSize = 10) {
  return request({
    url: '/extra-charge/list',
    method: 'get',
    params: {
      page,
      pageSize
    }
  })
}

/**
 * 创建支付订单（用户）
 */
export function createExtraChargePayment(id) {
  return request({
    url: `/extra-charge/pay/${id}`,
    method: 'post'
  })
}

/**
 * 主动取消额外收费订单（用户）
 */
export function cancelExtraChargeOrder(id) {
  return request({
    url: `/extra-charge/cancel/${id}`,
    method: 'post'
  })
}

/**
 * 统计用户未支付订单数量（用户）
 */
export function countMyUnpaidExtraChargeOrders() {
  return request({
    url: '/extra-charge/count-unpaid',
    method: 'get'
  })
}

