import request from '@/utils/request'

/**
 * 创建会员订单
 */
export function createMemberOrder(memberType) {
  return request({
    url: '/member/create',
    method: 'post',
    params: {
      memberType
    }
  })
}

/**
 * 创建会员支付订单（返回支付表单HTML）
 */
export function createMemberPayment(id) {
  return request({
    url: `/member/pay/${id}`,
    method: 'post',
    params: {
      frontBase: window.location.origin
    }
  })
}

/**
 * 获取我的会员订单列表
 */
export function getMyMemberOrders() {
  return request({
    url: '/member/my-orders',
    method: 'get'
  })
}

/**
 * 根据ID获取会员订单详情
 */
export function getMemberOrderById(id) {
  return request({
    url: `/member/order/${id}`,
    method: 'get'
  })
}

/**
 * 主动取消会员订单（用户）
 */
export function cancelMemberOrder(id) {
  return request({
    url: `/member/cancel/${id}`,
    method: 'post'
  })
}

