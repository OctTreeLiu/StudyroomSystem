import request from '@/utils/request'

/**
 * 获取管理员联系方式（公开接口）
 */
export function getContactInfo() {
  return request({
    url: '/contact/info',
    method: 'get'
  })
}

/**
 * 获取管理员联系方式（管理员）
 */
export function getAdminContactInfo() {
  return request({
    url: '/admin/contact/info',
    method: 'get'
  })
}

/**
 * 更新管理员联系方式（管理员）
 */
export function updateAdminContact(data) {
  return request({
    url: '/admin/contact/update',
    method: 'put',
    data
  })
}

