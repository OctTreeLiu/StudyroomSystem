import request from '@/utils/request'

/**
 * 获取所有已发布的公告列表
 */
export function getAnnouncementList() {
  return request({
    url: '/announcement/list',
    method: 'get'
  })
}

/**
 * 分页获取已发布的公告列表（游客可见）
 */
export function getAnnouncementListWithPagination(page = 1, pageSize = 10) {
  return request({
    url: '/announcement/list/page',
    method: 'get',
    params: {
      page,
      pageSize
    }
  })
}

/**
 * 根据ID获取公告详情
 */
export function getAnnouncementById(id) {
  return request({
    url: `/announcement/${id}`,
    method: 'get'
  })
}

