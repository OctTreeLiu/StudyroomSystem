import request from '@/utils/request'

/**
 * 提交长期租赁申请
 */
export function applyLease(data) {
  return request({
    url: '/lease/apply',
    method: 'post',
    data
  })
}

/**
 * 获取我的租赁列表
 */
export function getMyLeases() {
  return request({
    url: '/lease/my',
    method: 'get'
  })
}

/**
 * 根据ID获取租赁详情
 */
export function getLeaseById(id) {
  return request({
    url: `/lease/${id}`,
    method: 'get'
  })
}

/**
 * 创建长期租赁支付订单（返回支付表单HTML）
 */
export function createLeasePayment(id) {
  return request({
    url: `/lease/pay/${id}`,
    method: 'post',
    params: {
      frontBase: window.location.origin
    }
  })
}

/**
 * 取消或申请退款长期租赁
 */
export function cancelLease(id) {
  return request({
    url: `/lease/cancel/${id}`,
    method: 'post'
  })
}

/**
 * 检查用户是否有已生效的长期租赁订单（用于预约冲突检查）
 * 注意：此接口检查当前日期是否有长期租赁，建议使用 checkActiveLeaseByTime 接口
 */
export function checkActiveLease() {
  return request({
    url: '/lease/check-active',
    method: 'get'
  })
}

/**
 * 检查用户在指定时间段内是否有生效的长期租赁订单（用于预约冲突检查）
 * 优化逻辑：只在所选时间段内检查是否有生效的长期租赁，允许预约长期租赁结束之后的日期
 */
export function checkActiveLeaseByTime(startTime, endTime) {
  return request({
    url: '/lease/check-active-by-time',
    method: 'get',
    params: {
      startTime: startTime,
      endTime: endTime
    }
  })
}