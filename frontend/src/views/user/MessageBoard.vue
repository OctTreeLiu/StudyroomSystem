<template>
  <div class="message-board">
    <el-card class="message-card">
      <template #header>
        <div class="card-header">
          <div>
            <h2>留言板</h2>
            <span class="subtitle">与大家分享你的想法和建议</span>
          </div>
          <div class="search-section">
            <el-input
              v-model="searchUsername"
              placeholder="搜索用户名"
              clearable
              style="width: 200px; margin-right: 10px"
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-input
              v-model="searchContent"
              placeholder="搜索留言内容"
              clearable
              style="width: 200px; margin-right: 10px"
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </template>

      <!-- 留言输入区域 -->
      <div class="input-section">
        <el-form :model="messageForm" :rules="messageRules" ref="messageFormRef">
          <el-form-item prop="content">
            <el-input
              v-model="messageForm.content"
              type="textarea"
              :rows="4"
              placeholder="请输入你的留言内容..."
              maxlength="1000"
              show-word-limit
              clearable
            />
          </el-form-item>
          <div class="form-actions">
            <el-checkbox v-model="messageForm.isAnonymous">匿名发布</el-checkbox>
            <el-button 
              type="primary" 
              :loading="submitting"
              @click="handleSubmit"
            >
              发布留言
            </el-button>
          </div>
        </el-form>
      </div>
    </el-card>

    <!-- 留言列表 -->
    <div class="message-list-container">
      <div v-loading="loading" class="message-list">
        <el-empty v-if="!loading && messageList.length === 0" description="暂无留言，快来发表第一条吧！" />
        
        <div v-else>
          <div class="message-items">
            <div
              v-for="message in messageList"
              :key="message.id"
              class="message-item"
            >
              <div class="message-header">
                <div class="user-info">
                  <el-avatar 
                    :size="40" 
                    :src="message.isAnonymous === 1 ? null : message.avatarUrl"
                    :icon="message.isAnonymous === 1 ? User : null"
                  >
                    <template #default>
                      <span v-if="message.isAnonymous === 1">?</span>
                      <span v-else>{{ message.username?.charAt(0)?.toUpperCase() || '?' }}</span>
                    </template>
                  </el-avatar>
                  <div class="user-details">
                    <span class="username">{{ message.username || '未知用户' }}</span>
                    <span class="time">{{ formatTime(message.createTime) }}</span>
                  </div>
                </div>
                <!-- 删除按钮（仅自己的留言） -->
                <el-button
                  v-if="userStore.isLogin && message.userId === userStore.userInfo?.id"
                  type="danger"
                  text
                  size="small"
                  :icon="Delete"
                  @click="handleDeleteMessage(message)"
                >
                  删除
                </el-button>
              </div>
              <div class="message-content">
                {{ message.content }}
              </div>
              
              <!-- 点赞和评论操作区 -->
              <div class="message-actions">
                <div class="action-item" @click="handleLike(message)">
                  <el-icon :class="{ 'liked': message.isLiked }">
                    <component :is="message.isLiked ? 'StarFilled' : 'Star'" />
                  </el-icon>
                  <span :class="{ 'liked': message.isLiked }">{{ message.likeCount || 0 }}</span>
                </div>
                <div class="action-item" @click="toggleComment(message.id)">
                  <el-icon><ChatLineRound /></el-icon>
                  <span>{{ message.commentCount || 0 }}</span>
                </div>
              </div>
              
              <!-- 评论区域 -->
              <div v-if="expandedComments[message.id]" class="comment-section">
                <!-- 评论列表 -->
                <div v-if="commentMap[message.id] && commentMap[message.id].length > 0" class="comment-list">
                  <div
                    v-for="comment in commentMap[message.id]"
                    :key="comment.id"
                    class="comment-item"
                    :class="{ 'comment-item-reply': comment.parentId }"
                  >
                    <div class="comment-header">
                      <el-avatar 
                        :size="30" 
                        :src="comment.isAnonymous === 1 ? null : comment.avatarUrl"
                        :icon="comment.isAnonymous === 1 ? User : null"
                      >
                        <template #default>
                          <span v-if="comment.isAnonymous === 1">?</span>
                          <span v-else>{{ comment.username?.charAt(0)?.toUpperCase() || '?' }}</span>
                        </template>
                      </el-avatar>
                      <div class="comment-user">
                        <span class="comment-username">
                          {{ comment.username || '未知用户' }}
                          <span v-if="comment.parentUsername" class="reply-to">
                            回复 @{{ comment.parentUsername }}
                          </span>
                        </span>
                        <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                      </div>
                    </div>
                    <div class="comment-content">{{ comment.content }}</div>
                    <div class="comment-actions">
                      <div class="comment-like" @click="handleCommentLike(comment)">
                        <el-icon :class="{ 'liked': comment.isLiked }">
                          <component :is="comment.isLiked ? 'StarFilled' : 'Star'" />
                        </el-icon>
                        <span :class="{ 'liked': comment.isLiked }">{{ comment.likeCount || 0 }}</span>
                      </div>
                      <el-button 
                        text
                        size="small"
                        @click="handleReply(message.id, comment.id, comment.username)"
                      >
                        回复
                      </el-button>
                    </div>
                    
                    <!-- 次级评论列表 -->
                    <div v-if="comment.children && comment.children.length > 0" class="sub-comment-list">
                      <div
                        v-for="subComment in comment.children"
                        :key="subComment.id"
                        class="comment-item sub-comment-item"
                      >
                        <div class="comment-header">
                          <el-avatar 
                            :size="24" 
                            :src="subComment.isAnonymous === 1 ? null : subComment.avatarUrl"
                            :icon="subComment.isAnonymous === 1 ? User : null"
                          >
                            <template #default>
                              <span v-if="subComment.isAnonymous === 1">?</span>
                              <span v-else>{{ subComment.username?.charAt(0)?.toUpperCase() || '?' }}</span>
                            </template>
                          </el-avatar>
                          <div class="comment-user">
                            <span class="comment-username">
                              {{ subComment.username || '未知用户' }}
                              <span v-if="subComment.parentUsername" class="reply-to">
                                回复 @{{ subComment.parentUsername }}
                              </span>
                            </span>
                            <span class="comment-time">{{ formatTime(subComment.createTime) }}</span>
                          </div>
                        </div>
                        <div class="comment-content">{{ subComment.content }}</div>
                        <div class="comment-actions">
                          <div class="comment-like" @click="handleCommentLike(subComment)">
                            <el-icon :class="{ 'liked': subComment.isLiked }">
                              <component :is="subComment.isLiked ? 'StarFilled' : 'Star'" />
                            </el-icon>
                            <span :class="{ 'liked': subComment.isLiked }">{{ subComment.likeCount || 0 }}</span>
                          </div>
                          <el-button 
                            text
                            size="small"
                            @click="handleReply(message.id, subComment.id, subComment.username)"
                          >
                            回复
                          </el-button>
                          <!-- 删除按钮（仅自己的评论） -->
                          <el-button
                            v-if="userStore.isLogin && subComment.userId === userStore.userInfo?.id"
                            text
                            size="small"
                            style="color: #f56c6c; margin-left: 10px;"
                            @click="handleDeleteComment(message.id, subComment.id)"
                          >
                            删除
                          </el-button>
                        </div>
                        
                        <!-- 次级评论的回复输入框 -->
                        <div v-if="replyingTo[subComment.id]" class="reply-input-section">
                          <el-input
                            v-model="replyInputs[subComment.id]"
                            type="textarea"
                            :rows="2"
                            :placeholder="`回复 @${replyingTo[subComment.id].username}：`"
                            maxlength="500"
                            show-word-limit
                            @keyup.ctrl.enter="handleReplySubmit(message.id, subComment.id)"
                          />
                          <div class="comment-input-actions">
                            <el-checkbox v-model="replyAnonymous[subComment.id]">匿名</el-checkbox>
                            <div>
                              <el-button 
                                size="small"
                                @click="cancelReply(subComment.id)"
                              >
                                取消
                              </el-button>
                              <el-button 
                                type="primary" 
                                size="small"
                                :loading="replySubmitting[subComment.id]"
                                @click="handleReplySubmit(message.id, subComment.id)"
                              >
                                发布
                              </el-button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                    
                    <!-- 一级评论的回复输入框 -->
                    <div v-if="replyingTo[comment.id]" class="reply-input-section">
                      <el-input
                        v-model="replyInputs[comment.id]"
                        type="textarea"
                        :rows="2"
                        :placeholder="`回复 @${replyingTo[comment.id].username}：`"
                        maxlength="500"
                        show-word-limit
                        @keyup.ctrl.enter="handleReplySubmit(message.id, comment.id)"
                      />
                      <div class="comment-input-actions">
                        <el-checkbox v-model="replyAnonymous[comment.id]">匿名</el-checkbox>
                        <div>
                          <el-button 
                            size="small"
                            @click="cancelReply(comment.id)"
                          >
                            取消
                          </el-button>
                          <el-button 
                            type="primary" 
                            size="small"
                            :loading="replySubmitting[comment.id]"
                            @click="handleReplySubmit(message.id, comment.id)"
                          >
                            发布
                          </el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                
                <!-- 一级评论输入框 -->
                <div class="comment-input-section">
                  <el-input
                    v-model="commentInputs[message.id]"
                    type="textarea"
                    :rows="2"
                    placeholder="写下你的评论..."
                    maxlength="500"
                    show-word-limit
                    @keyup.ctrl.enter="handleComment(message.id)"
                  />
                  <div class="comment-input-actions">
                    <el-checkbox v-model="commentAnonymous[message.id]">匿名</el-checkbox>
                    <el-button 
                      type="primary" 
                      size="small"
                      :loading="commentSubmitting[message.id]"
                      @click="handleComment(message.id)"
                    >
                      发布评论
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 分页组件 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.pageSize"
              :total="pagination.total"
              :page-sizes="[10, 20, 30, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Search, Star, StarFilled, ChatLineRound, Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import { getMessageList, createMessage, getCommentList, createComment, toggleLike, deleteMessage, deleteComment, toggleCommentLike } from '@/api/messageBoard'
import { useUserStore } from '@/stores'

dayjs.extend(relativeTime)

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const messageFormRef = ref(null)
const messageList = ref([])
const searchUsername = ref('')
const searchContent = ref('')
const expandedComments = ref({}) // 展开的评论留言ID集合
const commentMap = ref({}) // 评论数据 { messageId: [comments] }
const commentInputs = ref({}) // 评论输入内容 { messageId: content }
const commentAnonymous = ref({}) // 评论是否匿名 { messageId: boolean }
const commentSubmitting = ref({}) // 评论提交状态 { messageId: boolean }
const replyingTo = ref({}) // 正在回复的评论 { commentId: { id, username } }
const replyInputs = ref({}) // 回复输入内容 { commentId: content }
const replyAnonymous = ref({}) // 回复是否匿名 { commentId: boolean }
const replySubmitting = ref({}) // 回复提交状态 { commentId: boolean }
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

const messageForm = reactive({
  content: '',
  isAnonymous: false
})

const messageRules = {
  content: [
    { required: true, message: '请输入留言内容', trigger: 'blur' },
    { min: 1, max: 1000, message: '留言内容长度在1到1000个字符之间', trigger: 'blur' }
  ]
}

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await getMessageList(pagination.value.page, pagination.value.pageSize, searchUsername.value, searchContent.value)
    if (res.code === 200 && res.data) {
      messageList.value = (res.data.list || []).map(msg => ({
        ...msg,
        commentCount: msg.commentCount || 0,
        likeCount: msg.likeCount || 0,
        isLiked: msg.isLiked || false
      }))
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error(error.message || '加载留言失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  loadMessages()
}

const handlePageChange = (page) => {
  pagination.value.page = page
  loadMessages()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.page = 1
  loadMessages()
}

const handleSubmit = async () => {
  if (!messageFormRef.value) return
  
  await messageFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const res = await createMessage({
          content: messageForm.content.trim(),
          isAnonymous: messageForm.isAnonymous
        })
        if (res.code === 200) {
          ElMessage.success('留言发布成功')
          // 清空表单
          messageForm.content = ''
          messageForm.isAnonymous = false
          // 重新加载留言列表（回到第一页）
          pagination.value.page = 1
          await loadMessages()
        }
      } catch (error) {
        ElMessage.error(error.message || '发布留言失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const formatTime = (time) => {
  if (!time) return ''
  const now = dayjs()
  const messageTime = dayjs(time)
  const diffMinutes = now.diff(messageTime, 'minute')
  
  if (diffMinutes < 1) {
    return '刚刚'
  } else if (diffMinutes < 60) {
    return `${diffMinutes}分钟前`
  } else if (diffMinutes < 1440) {
    return `${Math.floor(diffMinutes / 60)}小时前`
  } else if (diffMinutes < 10080) {
    return `${Math.floor(diffMinutes / 1440)}天前`
  } else {
    return messageTime.format('YYYY-MM-DD HH:mm')
  }
}

const toggleComment = async (messageId) => {
  if (expandedComments.value[messageId]) {
    // 收起评论
    expandedComments.value[messageId] = false
  } else {
    // 展开评论
    expandedComments.value[messageId] = true
    // 如果还没有加载评论，则加载
    if (!commentMap.value[messageId]) {
      await loadComments(messageId)
    }
  }
}

const loadComments = async (messageId) => {
  try {
    const res = await getCommentList(messageId)
    if (res.code === 200) {
      // 确保点赞数据正确初始化
      const comments = (res.data || []).map(comment => ({
        ...comment,
        likeCount: comment.likeCount || 0,
        isLiked: comment.isLiked || false,
        children: (comment.children || []).map(child => ({
          ...child,
          likeCount: child.likeCount || 0,
          isLiked: child.isLiked || false
        }))
      }))
      commentMap.value[messageId] = comments
    }
  } catch (error) {
    ElMessage.error('加载评论失败')
  }
}

const handleComment = async (messageId) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  
  const content = (commentInputs.value[messageId] || '').trim()
  if (!content) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  if (content.length > 500) {
    ElMessage.warning('评论内容不能超过500个字符')
    return
  }
  
  commentSubmitting.value[messageId] = true
  try {
    const res = await createComment({
      messageId,
      parentId: null, // 一级评论
      content,
      isAnonymous: commentAnonymous.value[messageId] || false
    })
    if (res.code === 200) {
      ElMessage.success('评论发布成功')
      // 清空输入框
      commentInputs.value[messageId] = ''
      commentAnonymous.value[messageId] = false
      // 重新加载评论
      await loadComments(messageId)
      // 更新留言的评论数
      const message = messageList.value.find(m => m.id === messageId)
      if (message) {
        message.commentCount = (message.commentCount || 0) + 1
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '发布评论失败')
  } finally {
    commentSubmitting.value[messageId] = false
  }
}

const handleReply = (messageId, parentCommentId, parentUsername) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  
  // 设置回复目标
  replyingTo.value[parentCommentId] = {
    id: parentCommentId,
    username: parentUsername
  }
  // 初始化输入框
  if (!replyInputs.value[parentCommentId]) {
    replyInputs.value[parentCommentId] = ''
  }
  if (replyAnonymous.value[parentCommentId] === undefined) {
    replyAnonymous.value[parentCommentId] = false
  }
}

const cancelReply = (commentId) => {
  delete replyingTo.value[commentId]
  delete replyInputs.value[commentId]
  delete replyAnonymous.value[commentId]
}

const handleReplySubmit = async (messageId, parentCommentId) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  
  const content = (replyInputs.value[parentCommentId] || '').trim()
  if (!content) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  if (content.length > 500) {
    ElMessage.warning('回复内容不能超过500个字符')
    return
  }
  
  // 获取被回复的用户信息
  const replyTarget = replyingTo.value[parentCommentId]
  const replyToUsername = replyTarget ? replyTarget.username : null
  
  replySubmitting.value[parentCommentId] = true
  try {
    const res = await createComment({
      messageId,
      parentId: parentCommentId, // 次级评论（后端会自动转换为一级评论ID）
      content,
      isAnonymous: replyAnonymous.value[parentCommentId] || false
    })
    if (res.code === 200) {
      ElMessage.success('回复发布成功')
      // 清空输入框和状态
      cancelReply(parentCommentId)
      // 重新加载评论
      await loadComments(messageId)
      // 如果回复的是次级评论，需要更新新评论的parentUsername显示
      if (replyToUsername && res.data) {
        // 找到新创建的评论并更新parentUsername
        const comments = commentMap.value[messageId]
        if (comments) {
          // 递归查找新创建的评论（通过时间戳或ID）
          const updateParentUsername = (commentList) => {
            for (const comment of commentList) {
              if (comment.id === res.data.id) {
                comment.parentUsername = replyToUsername
                return
              }
              if (comment.children) {
                updateParentUsername(comment.children)
              }
            }
          }
          updateParentUsername(comments)
        }
      }
      // 更新留言的评论数
      const message = messageList.value.find(m => m.id === messageId)
      if (message) {
        message.commentCount = (message.commentCount || 0) + 1
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '发布回复失败')
  } finally {
    replySubmitting.value[parentCommentId] = false
  }
}

const handleLike = async (message) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    const res = await toggleLike(message.id)
    if (res.code === 200 && res.data) {
      message.isLiked = res.data.isLiked
      message.likeCount = res.data.likeCount || 0
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDeleteMessage = async (message) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条留言吗？删除后将无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await deleteMessage(message.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      // 重新加载留言列表
      await loadMessages()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleDeleteComment = async (messageId, commentId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条评论吗？如果是一级评论，将同时删除所有次级评论。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await deleteComment(commentId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      // 重新加载评论
      await loadComments(messageId)
      // 更新留言的评论数
      const message = messageList.value.find(m => m.id === messageId)
      if (message) {
        // 重新计算评论数
        const comments = commentMap.value[messageId] || []
        let count = 0
        comments.forEach(comment => {
          count++ // 一级评论
          if (comment.children) {
            count += comment.children.length // 次级评论
          }
        })
        message.commentCount = count
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleCommentLike = async (comment) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    const res = await toggleCommentLike(comment.id)
    if (res.code === 200 && res.data) {
      comment.isLiked = res.data.isLiked
      comment.likeCount = res.data.likeCount || 0
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.message-board {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.message-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.search-section {
  display: flex;
  gap: 10px;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
  font-weight: 600;
}

.subtitle {
  font-size: 14px;
  color: #909399;
}

.input-section {
  margin-top: 20px;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.message-list-container {
  margin-top: 20px;
}

.message-list {
  min-height: 200px;
}

.message-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-item {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.message-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.message-header {
  margin-bottom: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}

.time {
  font-size: 12px;
  color: #909399;
}

.message-content {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

:deep(.el-card__header) {
  padding: 20px;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-textarea__inner) {
  font-size: 14px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.message-actions {
  display: flex;
  gap: 20px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  color: #909399;
  font-size: 14px;
  transition: color 0.3s;
}

.action-item:hover {
  color: #409eff;
}

.action-item .el-icon {
  font-size: 18px;
}

.action-item.liked,
.action-item .liked {
  color: #f56c6c;
}

.comment-section {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.comment-list {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  padding: 10px;
  background: #f9f9f9;
  border-radius: 6px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px; /* 减少头像和用户名的间距 */
  margin-bottom: 8px;
}

.comment-user {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0; /* 允许用户名区域收缩 */
}

.comment-username {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.comment-time {
  font-size: 11px;
  color: #909399;
}

.comment-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
  margin-left: 40px;
  margin-top: 5px;
}

.comment-item-reply {
  margin-left: 20px;
  border-left: 2px solid #e4e7ed;
  padding-left: 10px;
}

.comment-actions {
  margin-top: 8px;
  margin-left: 40px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.comment-like {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  color: #909399;
  font-size: 13px;
  transition: color 0.3s;
}

.comment-like:hover {
  color: #409eff;
}

.comment-like .el-icon {
  font-size: 16px;
}

.comment-like.liked,
.comment-like .liked {
  color: #f56c6c;
}

.reply-to {
  color: #409eff;
  font-size: 12px;
  margin-left: 5px;
}

.sub-comment-list {
  margin-top: 10px;
  margin-left: 20px;
  padding-left: 10px;
  border-left: 2px solid #f0f0f0;
}

.sub-comment-item {
  margin-top: 8px;
  padding: 8px;
  background: #fafafa;
  border-radius: 4px;
}

.sub-comment-item .comment-header {
  gap: 6px; /* 减少次级评论中头像和用户名的间距 */
  margin-bottom: 6px;
}

.sub-comment-item .comment-user {
  flex: 1;
  min-width: 0; /* 允许用户名区域收缩 */
}

.sub-comment-item .comment-content {
  margin-left: 30px; /* 次级评论内容左对齐，与头像对齐 */
  margin-top: 4px;
}

.sub-comment-item .comment-actions {
  margin-left: 30px; /* 次级评论操作按钮左对齐 */
  display: flex;
  align-items: center;
  gap: 15px;
}

.reply-input-section {
  margin-top: 10px;
  margin-left: 40px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
}

.comment-input-section {
  margin-top: 10px;
}

.comment-input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
</style>

