import request from '@/utils/request'

/**
 * 获取所有价格配置（管理员）
 */
export function getAllPriceConfigs() {
  return request({
    url: '/admin/price-config/list',
    method: 'get'
  })
}

/**
 * 更新单个价格配置（管理员）
 */
export function updatePriceConfig(data) {
  return request({
    url: '/admin/price-config/update',
    method: 'put',
    data
  })
}

/**
 * 批量更新价格配置（管理员）
 */
export function batchUpdatePriceConfigs(data) {
  return request({
    url: '/admin/price-config/batch-update',
    method: 'put',
    data
  })
}

