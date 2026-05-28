import request from '@/utils/request'

/**
 * 获取我的学习时长统计
 */
export function getMyStudyTimeStatistics() {
  return request({
    url: '/study-time/statistics',
    method: 'get'
  })
}

/**
 * 获取所有用户的学习时长统计（管理员）
 */
export function getAllUsersStudyTimeStatistics() {
  return request({
    url: '/admin/study-time/statistics',
    method: 'get'
  })
}

/**
 * 获取所有用户的学习时长统计（按时间段：daily/weekly/monthly）
 */
export function getAllUsersStudyTimeStatisticsByPeriod(period) {
  return request({
    url: `/admin/study-time/statistics/${period}`,
    method: 'get'
  })
}

/**
 * 获取学习红人榜（管理员）
 */
export function getStudyTimeRanking(period) {
  return request({
    url: `/admin/study-time/ranking/${period}`,
    method: 'get'
  })
}

/**
 * 获取我的24小时详细学习时长统计（按小时）
 * 如果接口不存在（404），返回空数据，不显示错误提示
 */
export function getMy24HoursStatistics() {
  return request({
    url: '/study-time/statistics/24hours',
    method: 'get'
  }).catch(error => {
    // 如果是404错误（接口未实现），静默处理，返回空数据
    if (error.response && error.response.status === 404) {
      return Promise.resolve({ code: 200, data: [] })
    }
    // 其他错误继续抛出
    return Promise.reject(error)
  })
}

/**
 * 获取我的7天详细学习时长统计（按天）
 * 如果接口不存在（404），返回空数据，不显示错误提示
 */
export function getMy7DaysStatistics() {
  return request({
    url: '/study-time/statistics/7days',
    method: 'get'
  }).catch(error => {
    // 如果是404错误（接口未实现），静默处理，返回空数据
    if (error.response && error.response.status === 404) {
      return Promise.resolve({ code: 200, data: [] })
    }
    // 其他错误继续抛出
    return Promise.reject(error)
  })
}

/**
 * 获取学习红人榜（普通用户）
 */
export function getMyStudyTimeRanking(period) {
  return request({
    url: `/study-time/ranking/${period}`,
    method: 'get'
  })
}

/**
 * 获取所有用户的24小时详细学习时长统计（按小时，管理员）
 * 如果接口不存在（404），返回空数据，不显示错误提示
 */
export function getAllUsers24HoursStatistics() {
  return request({
    url: '/admin/study-time/statistics/24hours',
    method: 'get'
  }).catch(error => {
    // 如果是404错误（接口未实现），静默处理，返回空数据
    if (error.response && error.response.status === 404) {
      return Promise.resolve({ code: 200, data: [] })
    }
    // 其他错误继续抛出
    return Promise.reject(error)
  })
}

/**
 * 获取所有用户的7天详细学习时长统计（按天，管理员）
 * 如果接口不存在（404），返回空数据，不显示错误提示
 */
export function getAllUsers7DaysStatistics() {
  return request({
    url: '/admin/study-time/statistics/7days',
    method: 'get'
  }).catch(error => {
    // 如果是404错误（接口未实现），静默处理，返回空数据
    if (error.response && error.response.status === 404) {
      return Promise.resolve({ code: 200, data: [] })
    }
    // 其他错误继续抛出
    return Promise.reject(error)
  })
}

/**
 * 获取指定用户的学习时长统计（管理员）
 */
export function getUserStatisticsByAdmin(userId) {
  return request({
    url: `/admin/study-time/statistics/user/${userId}`,
    method: 'get'
  })
}

/**
 * 获取指定用户过去24小时的每小时统计（管理员）
 */
export function getUser24HoursStatisticsByAdmin(userId) {
  return request({
    url: `/admin/study-time/statistics/user/${userId}/24hours`,
    method: 'get'
  }).catch(error => {
    if (error.response && error.response.status === 404) {
      return Promise.resolve({ code: 200, data: [] })
    }
    return Promise.reject(error)
  })
}

/**
 * 获取指定用户过去7天的每日统计（管理员）
 */
export function getUser7DaysStatisticsByAdmin(userId) {
  return request({
    url: `/admin/study-time/statistics/user/${userId}/7days`,
    method: 'get'
  }).catch(error => {
    if (error.response && error.response.status === 404) {
      return Promise.resolve({ code: 200, data: [] })
    }
    return Promise.reject(error)
  })
}

