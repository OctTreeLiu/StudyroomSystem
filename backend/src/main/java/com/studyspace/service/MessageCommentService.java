package com.studyspace.service;

import com.studyspace.common.PageResult;
import com.studyspace.entity.MessageComment;
import com.studyspace.mapper.CommentLikeMapper;
import com.studyspace.mapper.MessageCommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 留言评论服务类
 */
@Service
public class MessageCommentService {
    
    @Autowired
    private MessageCommentMapper messageCommentMapper;
    
    @Autowired
    private CommentLikeMapper commentLikeMapper;
    
    /**
     * 根据留言ID查询评论列表（返回层级结构）
     * @param messageId 留言ID
     * @param currentUserId 当前用户ID（可选，用于判断是否已点赞）
     */
    public List<MessageComment> getCommentsByMessageId(Long messageId, Long currentUserId) {
        List<MessageComment> allComments = messageCommentMapper.selectByMessageId(messageId);
        
        // 设置当前用户是否已点赞
        if (currentUserId != null) {
            for (MessageComment comment : allComments) {
                boolean isLiked = commentLikeMapper.selectByCommentIdAndUserId(comment.getId(), currentUserId) != null;
                comment.setIsLiked(isLiked);
                // 递归处理子评论
                if (comment.getChildren() != null) {
                    for (MessageComment child : comment.getChildren()) {
                        boolean childIsLiked = commentLikeMapper.selectByCommentIdAndUserId(child.getId(), currentUserId) != null;
                        child.setIsLiked(childIsLiked);
                    }
                }
            }
        }
        
        // 构建层级结构
        return buildCommentTree(allComments);
    }
    
    /**
     * 构建评论树结构
     */
    private List<MessageComment> buildCommentTree(List<MessageComment> allComments) {
        // 分离一级评论和次级评论
        List<MessageComment> topLevelComments = new ArrayList<>();
        Map<Long, List<MessageComment>> childrenMap = allComments.stream()
            .filter(comment -> comment.getParentId() != null)
            .collect(Collectors.groupingBy(MessageComment::getParentId));
        
        // 遍历所有评论，构建树结构
        for (MessageComment comment : allComments) {
            if (comment.getParentId() == null) {
                // 一级评论
                List<MessageComment> children = childrenMap.getOrDefault(comment.getId(), new ArrayList<>());
                comment.setChildren(children);
                topLevelComments.add(comment);
            }
        }
        
        return topLevelComments;
    }
    
    /**
     * 根据留言ID统计评论数量
     */
    public int countCommentsByMessageId(Long messageId) {
        return messageCommentMapper.countByMessageId(messageId);
    }
    
    /**
     * 创建评论（支持一级评论和次级评论）
     */
    @Transactional
    public MessageComment createComment(Long userId, Long messageId, Long parentId, String content, Boolean isAnonymous) {
        // 如果提供了 parentId，验证父评论是否存在且属于同一留言
        if (parentId != null) {
            MessageComment parentComment = messageCommentMapper.selectById(parentId);
            if (parentComment == null) {
                throw new RuntimeException("父评论不存在");
            }
            if (!parentComment.getMessageId().equals(messageId)) {
                throw new RuntimeException("父评论不属于该留言");
            }
            
            // 如果父评论本身也是次级评论（有parentId），则找到一级评论
            // 保持二级评论结构：所有次级评论的parentId都指向一级评论
            if (parentComment.getParentId() != null) {
                // 父评论是次级评论，找到它的一级评论
                parentId = parentComment.getParentId();
            }
            // 如果父评论是一级评论（parentId == null），则直接使用parentId
        }
        
        MessageComment comment = new MessageComment();
        comment.setUserId(userId);
        comment.setMessageId(messageId);
        comment.setParentId(parentId);
        comment.setContent(content);
        comment.setIsAnonymous(isAnonymous != null && isAnonymous ? 1 : 0);
        
        messageCommentMapper.insert(comment);
        
        // 返回包含用户名的完整信息
        return messageCommentMapper.selectById(comment.getId());
    }
    
    /**
     * 删除评论（级联删除次级评论由数据库外键处理）
     */
    @Transactional
    public void deleteComment(Long id) {
        MessageComment comment = messageCommentMapper.selectById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        
        messageCommentMapper.deleteById(id);
    }
    
    /**
     * 用户删除自己的评论（级联删除所有次级评论）
     */
    @Transactional
    public void deleteCommentByUser(Long id, Long userId) {
        MessageComment comment = messageCommentMapper.selectById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        
        // 验证是否为评论所有者
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的评论");
        }
        
        // 如果是一级评论，删除所有次级评论
        if (comment.getParentId() == null) {
            // 删除所有以该评论为父评论的次级评论
            messageCommentMapper.deleteByParentId(id);
        }
        
        // 删除评论本身
        messageCommentMapper.deleteById(id);
    }
    
    /**
     * 管理员：分页查询所有评论（包含层级信息）
     */
    public PageResult<MessageComment> getAllCommentsWithPagination(Integer page, Integer pageSize, String username) {
        int offset = (page - 1) * pageSize;
        List<MessageComment> comments = messageCommentMapper.selectAllWithPagination(offset, pageSize, username);
        int total = messageCommentMapper.countAll(username);
        
        PageResult<MessageComment> pageResult = new PageResult<>();
        pageResult.setList(comments);
        pageResult.setTotal((long) total);
        return pageResult;
    }
}

