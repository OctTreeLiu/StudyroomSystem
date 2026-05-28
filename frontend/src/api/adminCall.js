import request from '@/utils/request'

/**
 * 呼叫管理员
 */
export function callAdmin(data) {
  return request({
    url: '/admin-call/call',
    method: 'post',
    data
  })
}

/**
 * 获取我的呼叫记录
 */
export function getMyCalls() {
  return request({
    url: '/admin-call/my-calls',
    method: 'get'
  })
}

/**
 * 获取所有呼叫记录（管理员）- 支持分页与模糊查询（用户名 / 用户ID）
 */
export function getAllCalls(status, page = 1, pageSize = 10, keyword) {
  return request({
    url: '/admin/call/list',
    method: 'get',
    params: { 
      status,
      page,
      pageSize,
      keyword
    }
  })
}

/**
 * 处理呼叫（管理员）
 */
export function handleCall(id) {
  return request({
    url: `/admin/call/handle/${id}`,
    method: 'put'
  })
}

/**
 * 获取待处理数量（管理员）
 */
export function getPendingCallCount() {
  return request({
    url: '/admin/call/pending-count',
    method: 'get'
  })
}

