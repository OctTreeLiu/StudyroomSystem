import request from '@/utils/request'

/**
 * 获取所有用户列表（管理员）
 */
export function getAllUsers() {
  return request({
    url: '/admin/user/list',
    method: 'get'
  })
}

/**
 * 根据ID获取用户详情（管理员）
 */
export function getUserById(id) {
  return request({
    url: `/admin/user/${id}`,
    method: 'get'
  })
}

/**
 * 更新用户信息（管理员）
 */
export function updateUser(id, data) {
  return request({
    url: `/admin/user/update/${id}`,
    method: 'put',
    data
  })
}

/**
 * 向用户发送通知（管理员）
 */
export function sendNotificationToUser(userId, title, content) {
  return request({
    url: '/admin/notification/send-to-user',
    method: 'post',
    data: {
      userId,
      title,
      content
    }
  })
}

