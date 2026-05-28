import request from '@/utils/request'

/**
 * 获取留言列表（分页）
 */
export function getMessageList(page = 1, pageSize = 10, username = '', content = '') {
  return request({
    url: '/message-board/list',
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
 * 发布留言
 */
export function createMessage(data) {
  return request({
    url: '/message-board/create',
    method: 'post',
    data
  })
}

/**
 * 根据ID获取留言详情
 */
export function getMessageById(id) {
  return request({
    url: `/message-board/${id}`,
    method: 'get'
  })
}

/**
 * 获取评论列表
 */
export function getCommentList(messageId) {
  return request({
    url: `/message-comment/list/${messageId}`,
    method: 'get'
  })
}

/**
 * 创建评论（支持一级评论和次级评论）
 */
export function createComment(data) {
  return request({
    url: '/message-comment/create',
    method: 'post',
    data
  })
}

/**
 * 删除评论
 */
export function deleteComment(id) {
  return request({
    url: `/message-comment/delete/${id}`,
    method: 'post'
  })
}

/**
 * 点赞或取消点赞
 */
export function toggleLike(messageId) {
  return request({
    url: `/message-like/toggle/${messageId}`,
    method: 'post'
  })
}

/**
 * 检查是否已点赞
 */
export function checkLike(messageId) {
  return request({
    url: `/message-like/check/${messageId}`,
    method: 'get'
  })
}

/**
 * 用户删除自己的留言
 */
export function deleteMessage(id) {
  return request({
    url: `/message-board/delete/${id}`,
    method: 'post'
  })
}

/**
 * 评论点赞或取消点赞
 */
export function toggleCommentLike(commentId) {
  return request({
    url: `/comment-like/toggle/${commentId}`,
    method: 'post'
  })
}

/**
 * 检查评论是否已点赞
 */
export function checkCommentLike(commentId) {
  return request({
    url: `/comment-like/check/${commentId}`,
    method: 'get'
  })
}

