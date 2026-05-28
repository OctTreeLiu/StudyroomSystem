import request from '@/utils/request'

/**
 * 获取修改日志列表（分页），支持按被修改用户ID或用户名模糊查询
 */
export function getUserModifyLogs(page = 1, pageSize = 10, keyword) {
  return request({
    url: '/admin/user-modify-log/list',
    method: 'get',
    params: {
      page,
      pageSize,
      keyword
    }
  })
}

/**
 * 获取修改日志详情
 */
export function getUserModifyLogDetail(id) {
  return request({
    url: `/admin/user-modify-log/${id}`,
    method: 'get'
  })
}

