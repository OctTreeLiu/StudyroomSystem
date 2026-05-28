import request from '@/utils/request'

/**
 * 与“小光智能体”对话
 * @param {Object} data
 * @param {{role: string, content: string}[]} data.messages
 */
export function chatWithGuangAi(data) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data
  })
}

