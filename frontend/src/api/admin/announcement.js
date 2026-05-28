import request from '@/utils/request'

/**
 * 获取所有公告列表（管理员）
 */
export function getAllAnnouncements() {
  return request({
    url: '/admin/announcement/list',
    method: 'get'
  })
}

/**
 * 发布公告
 */
export function createAnnouncement(data) {
  return request({
    url: '/admin/announcement/create',
    method: 'post',
    data
  })
}

/**
 * 更新公告
 */
export function updateAnnouncement(id, data) {
  return request({
    url: `/admin/announcement/update/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除公告
 */
export function deleteAnnouncement(id) {
  return request({
    url: `/admin/announcement/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 根据ID获取公告详情（管理员）
 */
export function getAnnouncementById(id) {
  return request({
    url: `/admin/announcement/${id}`,
    method: 'get'
  })
}

