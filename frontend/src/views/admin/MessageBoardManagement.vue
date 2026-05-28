<template>
  <div class="message-board-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>留言板管理</span>
          <div class="header-actions">
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
            <el-button @click="activeTab === 'messages' ? loadMessages() : loadAllComments()">刷新</el-button>
          </div>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="留言管理" name="messages">

          <div v-loading="loading">
            <el-empty v-if="!loading && messageList.length === 0 && pagination.total === 0" description="暂无留言" />
            
            <template v-else>
          <div v-if="messageList.length > 0" class="message-list">
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
                <el-button
                  type="danger"
                  size="small"
                  :icon="Delete"
                  @click="handleDelete(message)"
                >
                  删除
                </el-button>
              </div>
              <div class="message-content">
                {{ message.content }}
              </div>
              
              <!-- 统计信息 -->
              <div class="message-stats">
                <span class="stat-item">
                  <el-icon><Star /></el-icon>
                  <span>点赞 {{ message.likeCount || 0 }}</span>
                </span>
                <span class="stat-item">
                  <el-icon><ChatLineRound /></el-icon>
                  <span>评论 {{ message.commentCount || 0 }}</span>
                </span>
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
                          <span v-if="comment.parentId" class="reply-badge">次级评论</span>
                          <span v-if="comment.parentUsername" class="reply-to">
                            回复 @{{ comment.parentUsername }}
                          </span>
                        </span>
                        <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                      </div>
                      <el-button
                        type="danger"
                        text
                        size="small"
                        :icon="Delete"
                        @click="handleDeleteComment(message.id, comment.id)"
                      >
                        删除
                      </el-button>
                    </div>
                    <div class="comment-content">{{ comment.content }}</div>
                    
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
                          <el-button
                            type="danger"
                            text
                            size="small"
                            :icon="Delete"
                            @click="handleDeleteComment(message.id, subComment.id)"
                          >
                            删除
                          </el-button>
                        </div>
                        <div class="comment-content">{{ subComment.content }}</div>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-else class="no-comments">
                  <el-empty description="暂无评论" :image-size="60" />
                </div>
              </div>
              
              <!-- 展开/收起评论按钮 -->
              <div class="comment-toggle" @click="toggleComment(message.id)">
                <el-button text type="primary" size="small">
                  {{ expandedComments[message.id] ? '收起评论' : '查看评论' }}
                </el-button>
              </div>
            </div>
          </div>
        </template>
        

        <!-- 分页组件 - 始终显示（只要有总数） -->
        <div v-if="!loading && pagination.total > 0" class="pagination-container" style="margin-top: 20px; padding: 20px 0;">
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
        </el-tab-pane>
        
        <el-tab-pane label="评论管理" name="comments">
          <div v-loading="commentLoading">
            <el-empty v-if="!commentLoading && commentList.length === 0 && commentPagination.total === 0" description="暂无评论" />
            
            <template v-else>
              <div v-if="commentList.length > 0" class="comment-list">
                <div
                  v-for="comment in commentList"
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
                        <span v-if="comment.parentId" class="reply-badge">次级评论</span>
                        <span v-if="comment.parentUsername" class="reply-to">
                          回复 @{{ comment.parentUsername }}
                        </span>
                      </span>
                      <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                    </div>
                    <el-button
                      type="danger"
                      size="small"
                      :icon="Delete"
                      @click="handleDeleteComment(comment)"
                    >
                      删除
                    </el-button>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                </div>
              </div>
            </template>
            
            <!-- 分页组件 -->
            <div v-if="!commentLoading && commentPagination.total > 0" class="pagination-container" style="margin-top: 20px; padding: 20px 0;">
              <el-pagination
                v-model:current-page="commentPagination.page"
                v-model:page-size="commentPagination.pageSize"
                :total="commentPagination.total"
                :page-sizes="[10, 20, 30, 50]"
                layout="total, sizes, prev, pager, next, jumper"
                @current-change="handleCommentPageChange"
                @size-change="handleCommentSizeChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Delete, Search, Star, ChatLineRound } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getMessageList, deleteMessage, getCommentList, deleteComment } from '@/api/admin/messageBoard'
import { getCommentList as getUserCommentList } from '@/api/messageBoard'

const activeTab = ref('messages')
const loading = ref(false)
const commentLoading = ref(false)
const messageList = ref([])
const commentList = ref([])
const searchUsername = ref('')
const searchContent = ref('')
const expandedComments = ref({}) // 展开的评论留言ID集合
const commentMap = ref({}) // 评论数据 { messageId: [comments] }
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})
const commentPagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await getMessageList(pagination.value.page, pagination.value.pageSize, searchUsername.value, searchContent.value)
    if (res.code === 200 && res.data) {
      messageList.value = res.data.list || []
      pagination.value.total = res.data.total || 0
      // 调试信息
      console.log('分页数据:', {
        list: messageList.value.length,
        total: pagination.value.total,
        page: pagination.value.page,
        pageSize: pagination.value.pageSize
      })
    } else {
      console.error('API返回错误:', res)
    }
  } catch (error) {
    console.error('加载留言失败:', error)
    ElMessage.error(error.message || '加载留言失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (activeTab.value === 'messages') {
    pagination.value.page = 1
    loadMessages()
  } else {
    commentPagination.value.page = 1
    loadAllComments()
  }
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

const handleDelete = async (message) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除这条留言吗？`,
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
      // 如果当前页没有数据了，回到上一页
      if (messageList.value.length === 1 && pagination.value.page > 1) {
        pagination.value.page--
      }
      await loadMessages()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
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
    const res = await getUserCommentList(messageId)
    if (res.code === 200) {
      commentMap.value[messageId] = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载评论失败')
  }
}

const loadAllComments = async () => {
  commentLoading.value = true
  try {
    const res = await getCommentList(commentPagination.value.page, commentPagination.value.pageSize, searchUsername.value)
    if (res.code === 200 && res.data) {
      commentList.value = res.data.list || []
      commentPagination.value.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error(error.message || '加载评论失败')
  } finally {
    commentLoading.value = false
  }
}

const handleTabChange = (tabName) => {
  if (tabName === 'comments') {
    // 切换到评论标签页时，如果还没有加载过数据，则加载
    if (commentList.value.length === 0 && commentPagination.value.total === 0) {
      loadAllComments()
    }
  } else if (tabName === 'messages') {
    // 切换到留言标签页时，如果数据为空，则加载
    if (messageList.value.length === 0 && pagination.value.total === 0) {
      loadMessages()
    }
  }
}

const handleDeleteComment = async (messageIdOrComment, commentId) => {
  try {
    // 判断是从留言管理标签页调用（两个参数）还是从评论管理标签页调用（一个参数）
    let actualCommentId
    let actualMessageId = null
    
    if (commentId !== undefined) {
      // 从留言管理标签页调用：messageIdOrComment 是 messageId，commentId 是 commentId
      actualMessageId = messageIdOrComment
      actualCommentId = commentId
    } else {
      // 从评论管理标签页调用：messageIdOrComment 是 comment 对象
      const comment = messageIdOrComment
      actualCommentId = comment.id
    }
    
    await ElMessageBox.confirm(
      `确定要删除这条评论吗？如果是一级评论，将同时删除所有次级评论。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await deleteComment(actualCommentId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      
      // 如果是从留言管理标签页调用的，重新加载该留言的评论
      if (actualMessageId !== null) {
        await loadComments(actualMessageId)
        // 更新留言的评论数
        const message = messageList.value.find(m => m.id === actualMessageId)
        if (message) {
          // 重新计算评论数
          const comments = commentMap.value[actualMessageId] || []
          let count = 0
          comments.forEach(comment => {
            count++ // 一级评论
            if (comment.children) {
              count += comment.children.length // 次级评论
            }
          })
          message.commentCount = count
        }
      } else {
        // 如果是从评论管理标签页调用的，重新加载评论列表
        if (commentList.value.length === 1 && commentPagination.value.page > 1) {
          commentPagination.value.page--
        }
        await loadAllComments()
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleCommentPageChange = (page) => {
  commentPagination.value.page = page
  loadAllComments()
}

const handleCommentSizeChange = (size) => {
  commentPagination.value.pageSize = size
  commentPagination.value.page = 1
  loadAllComments()
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.message-board-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  flex-wrap: wrap;
  gap: 15px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-item {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e4e7ed;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.message-stats {
  display: flex;
  gap: 20px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e4e7ed;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #909399;
  font-size: 13px;
}

.stat-item .el-icon {
  font-size: 16px;
}

.comment-section {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e4e7ed;
}

.comment-list {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  padding: 10px;
  background: #f5f7fa;
  border-radius: 6px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 6px;
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
}

/* 二级评论的内容不需要那么大的左边距 */
.sub-comment-item .comment-content {
  margin-left: 30px;
}

.no-comments {
  padding: 20px 0;
}

.comment-toggle {
  margin-top: 10px;
  text-align: center;
}

.reply-badge {
  display: inline-block;
  padding: 2px 6px;
  margin-left: 8px;
  background: #409eff;
  color: white;
  border-radius: 3px;
  font-size: 11px;
}

.comment-item-reply {
  border-left: 3px solid #409eff;
  padding-left: 10px;
  margin-left: 10px;
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

/* 优化二级评论的整体布局，使头像和用户名更紧凑 */
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

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px; /* 减少头像和用户名的间距 */
  margin-bottom: 8px;
  justify-content: space-between;
}

.comment-user {
  flex: 1;
  min-width: 0; /* 允许用户名区域收缩 */
}
</style>

