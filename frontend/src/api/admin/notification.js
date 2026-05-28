import request from '@/utils/request'

/**
 * 获取管理员的消息列表
 */
export function getAdminNotifications() {
  return request({
    url: '/admin/notification/list',
    method: 'get'
  })
}

/**
 * 获取管理员的未读消息数量
 */
export function getAdminUnreadCount() {
  return request({
    url: '/admin/notification/unread-count',
    method: 'get'
  })
}

/**
 * 标记消息为已读（管理员）
 */
export function markAsRead(id) {
  return request({
    url: `/admin/notification/read/${id}`,
    method: 'put'
  })
}

/**
 * 标记所有消息为已读（管理员）
 */
export function markAllAsRead() {
  return request({
    url: '/admin/notification/read-all',
    method: 'put'
  })
}

