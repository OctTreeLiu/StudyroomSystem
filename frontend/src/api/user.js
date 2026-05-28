import request from '@/utils/request'

/**
 * 获取当前用户信息
 */
export function getCurrentUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

/**
 * 更新用户信息
 */
export function updateUserInfo(data) {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}

/**
 * 上传用户头像
 */
export function uploadAvatar(avatarUrl) {
  return request({
    url: '/user/avatar',
    method: 'put',
    data: { avatarUrl }
  })
}

/**
 * 修改密码
 */
export function changePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}

