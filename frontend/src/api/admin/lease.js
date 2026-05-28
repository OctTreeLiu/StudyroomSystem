import request from '@/utils/request'

/**
 * 获取所有长期租赁申请（管理员）
 */
export function getAllLeases() {
  return request({
    url: '/admin/lease/list',
    method: 'get'
  })
}

/**
 * 根据状态获取长期租赁申请（管理员）
 */
export function getLeasesByStatus(status) {
  return request({
    url: `/admin/lease/list/status/${status}`,
    method: 'get'
  })
}

/**
 * 审核长期租赁申请
 */
export function auditLease(id, data) {
  return request({
    url: `/admin/lease/audit/${id}`,
    method: 'post',
    data
  })
}

/**
 * 根据ID获取租赁详情（管理员）
 */
export function getLeaseById(id) {
  return request({
    url: `/admin/lease/${id}`,
    method: 'get'
  })
}

/**
 * 获取待审核租赁申请数量
 */
export function getPendingLeaseCount() {
  return request({
    url: '/admin/lease/pending/count',
    method: 'get'
  })
}

