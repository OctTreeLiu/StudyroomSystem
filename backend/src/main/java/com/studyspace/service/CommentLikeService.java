package com.studyspace.service;

import com.studyspace.entity.CommentLike;
import com.studyspace.mapper.CommentLikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 评论点赞服务类
 */
@Service
public class CommentLikeService {
    
    @Autowired
    private CommentLikeMapper commentLikeMapper;
    
    /**
     * 检查用户是否已点赞
     */
    public boolean isLiked(Long commentId, Long userId) {
        CommentLike like = commentLikeMapper.selectByCommentIdAndUserId(commentId, userId);
        return like != null;
    }
    
    /**
     * 根据评论ID统计点赞数量
     */
    public int countLikesByCommentId(Long commentId) {
        return commentLikeMapper.countByCommentId(commentId);
    }
    
    /**
     * 点赞或取消点赞
     */
    @Transactional
    public boolean toggleLike(Long commentId, Long userId) {
        CommentLike existingLike = commentLikeMapper.selectByCommentIdAndUserId(commentId, userId);
        
        if (existingLike != null) {
            // 已点赞，取消点赞
            commentLikeMapper.deleteByCommentIdAndUserId(commentId, userId);
            return false;
        } else {
            // 未点赞，添加点赞
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            commentLikeMapper.insert(like);
            return true;
        }
    }
}

