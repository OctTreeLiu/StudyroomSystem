import request from '@/utils/request'

/**
 * 获取当前用户的消息列表
 */
export function getMyNotifications() {
  return request({
    url: '/notification/list',
    method: 'get'
  })
}

/**
 * 获取当前用户的未读消息数量
 */
export function getUnreadCount() {
  return request({
    url: '/notification/unread-count',
    method: 'get'
  })
}

/**
 * 标记消息为已读
 */
export function markAsRead(id) {
  return request({
    url: `/notification/read/${id}`,
    method: 'put'
  })
}

/**
 * 标记所有消息为已读
 */
export function markAllAsRead() {
  return request({
    url: '/notification/read-all',
    method: 'put'
  })
}

