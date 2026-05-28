import request from '@/utils/request'

/**
 * 获取我的使用记录
 */
export function getMyUsageRecords() {
  return request({
    url: '/usage/my',
    method: 'get'
  })
}

/**
 * 根据ID获取使用记录详情
 */
export function getUsageRecordById(id) {
  return request({
    url: `/usage/${id}`,
    method: 'get'
  })
}

