import request from '@/utils/request'

/**
 * 创建预约
 */
export function createReservation(data) {
  return request({
    url: '/reservation/create',
    method: 'post',
    data
  })
}

/**
 * 使用积分兑换预约
 */
export function createReservationWithPoints(data) {
  return request({
    url: '/reservation/create-with-points',
    method: 'post',
    data
  })
}

/**
 * 获取我的预约列表
 */
export function getMyReservations() {
  return request({
    url: '/reservation/my',
    method: 'get'
  })
}

/**
 * 根据ID获取预约详情
 */
export function getReservationById(id) {
  return request({
    url: `/reservation/${id}`,
    method: 'get'
  })
}

/**
 * 取消预约
 */
export function cancelReservation(id) {
  return request({
    url: `/reservation/cancel/${id}`,
    method: 'post'
  })
}

/**
 * 创建支付订单（返回支付表单HTML）
 */
export function createPayment(id) {
  return request({
    url: `/reservation/pay/${id}`,
    method: 'post',
    params: {
      frontBase: window.location.origin
    }
  })
}

/**
 * 获取当前用户的入场凭证
 */
export function getEntryTicket() {
  return request({
    url: '/reservation/entry-ticket',
    method: 'get'
  })
}

