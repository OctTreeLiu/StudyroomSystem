package com.studyspace.mapper;

import com.studyspace.entity.MessageLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 留言点赞Mapper接口
 */
@Mapper
public interface MessageLikeMapper {
    
    /**
     * 根据留言ID和用户ID查询点赞记录
     */
    MessageLike selectByMessageIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);
    
    /**
     * 根据留言ID统计点赞数量
     */
    int countByMessageId(@Param("messageId") Long messageId);
    
    /**
     * 插入点赞
     */
    int insert(MessageLike like);
    
    /**
     * 删除点赞
     */
    int deleteByMessageIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);
    
    /**
     * 根据留言ID删除所有点赞
     */
    int deleteByMessageId(@Param("messageId") Long messageId);
}

