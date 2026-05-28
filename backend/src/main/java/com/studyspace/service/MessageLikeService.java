package com.studyspace.service;

import com.studyspace.entity.MessageLike;
import com.studyspace.mapper.MessageLikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 留言点赞服务类
 */
@Service
public class MessageLikeService {
    
    @Autowired
    private MessageLikeMapper messageLikeMapper;
    
    /**
     * 检查用户是否已点赞
     */
    public boolean isLiked(Long messageId, Long userId) {
        MessageLike like = messageLikeMapper.selectByMessageIdAndUserId(messageId, userId);
        return like != null;
    }
    
    /**
     * 根据留言ID统计点赞数量
     */
    public int countLikesByMessageId(Long messageId) {
        return messageLikeMapper.countByMessageId(messageId);
    }
    
    /**
     * 点赞或取消点赞
     */
    @Transactional
    public boolean toggleLike(Long messageId, Long userId) {
        MessageLike existingLike = messageLikeMapper.selectByMessageIdAndUserId(messageId, userId);
        
        if (existingLike != null) {
            // 已点赞，取消点赞
            messageLikeMapper.deleteByMessageIdAndUserId(messageId, userId);
            return false;
        } else {
            // 未点赞，添加点赞
            MessageLike like = new MessageLike();
            like.setMessageId(messageId);
            like.setUserId(userId);
            messageLikeMapper.insert(like);
            return true;
        }
    }
}

