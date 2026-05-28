import request from '@/utils/request'

/**
 * 获取所有留言列表（管理员，分页）
 */
export function getMessageList(page = 1, pageSize = 10, username = '', content = '') {
  return request({
    url: '/admin/message-board/list',
    method: 'get',
    params: {
      page,
      pageSize,
      username,
      content
    }
  })
}

/**
 * 删除留言（管理员）
 */
export function deleteMessage(id) {
  return request({
    url: `/admin/message-board/delete/${id}`,
    method: 'post'
  })
}

/**
 * 获取所有评论列表（管理员，分页）
 */
export function getCommentList(page = 1, pageSize = 10, username = '') {
  return request({
    url: '/admin/message-comment/list',
    method: 'get',
    params: {
      page,
      pageSize,
      username
    }
  })
}

/**
 * 删除评论（管理员）
 */
export function deleteComment(id) {
  return request({
    url: `/admin/message-comment/delete/${id}`,
    method: 'post'
  })
}

