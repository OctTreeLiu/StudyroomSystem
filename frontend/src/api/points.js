import request from '@/utils/request'

/**
 * 获取用户当前积分信息
 */
export function getPointsInfo() {
  return request({
    url: '/points/info',
    method: 'get'
  })
}

/**
 * 分页查询用户积分流水记录
 */
export function getPointsHistory(page = 1, pageSize = 10) {
  return request({
    url: '/points/history',
    method: 'get',
    params: {
      page,
      pageSize
    }
  })
}

/**
 * 每日签到
 */
export function dailySignIn() {
  return request({
    url: '/points/sign-in',
    method: 'post'
  })
}

/**
 * 计算积分兑换所需积分
 */
export function calculateExchangePoints(hours) {
  return request({
    url: '/points/exchange/calculate',
    method: 'post',
    params: {
      hours
    }
  })
}

