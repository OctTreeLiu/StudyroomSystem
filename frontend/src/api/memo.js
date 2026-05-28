import request from '@/utils/request'

/**
 * 创建备忘录
 */
export function createMemo(data) {
  return request({
    url: '/memo/create',
    method: 'post',
    data
  })
}

/**
 * 更新备忘录
 */
export function updateMemo(id, data) {
  return request({
    url: `/memo/update/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除备忘录
 */
export function deleteMemo(id) {
  return request({
    url: `/memo/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 获取我的所有备忘录（分页）
 */
export function getMyMemos(page = 1, pageSize = 10, date = null, status = null) {
  return request({
    url: '/memo/list',
    method: 'get',
    params: {
      page,
      pageSize,
      date,
      status
    }
  })
}

/**
 * 根据日期获取备忘录
 */
export function getMemosByDate(date) {
  return request({
    url: '/memo/list-by-date',
    method: 'get',
    params: { date }
  })
}

/**
 * 检查今天是否有未处理的备忘录
 */
export function checkTodayUnprocessed() {
  return request({
    url: '/memo/check-today-unprocessed',
    method: 'get'
  })
}

