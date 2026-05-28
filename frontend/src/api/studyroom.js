import request from '@/utils/request'

/**
 * 获取所有自习室列表
 */
export function getStudyRoomList() {
  return request({
    url: '/studyroom/list',
    method: 'get'
  })
}

/**
 * 根据ID获取自习室详情
 */
export function getStudyRoomById(id) {
  return request({
    url: `/studyroom/${id}`,
    method: 'get'
  })
}

/**
 * 根据状态获取自习室列表
 */
export function getStudyRoomsByStatus(status) {
  return request({
    url: `/studyroom/list/status/${status}`,
    method: 'get'
  })
}

/**
 * 管理员新增自习室
 */
export function createAdminStudyRoom(data) {
  return request({
    url: '/admin/studyroom',
    method: 'post',
    data
  })
}

