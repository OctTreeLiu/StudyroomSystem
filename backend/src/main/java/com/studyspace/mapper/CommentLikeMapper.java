package com.studyspace.mapper;

import com.studyspace.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 评论点赞Mapper接口
 */
@Mapper
public interface CommentLikeMapper {
    
    /**
     * 根据评论ID和用户ID查询点赞记录
     */
    CommentLike selectByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);
    
    /**
     * 根据评论ID统计点赞数量
     */
    int countByCommentId(@Param("commentId") Long commentId);
    
    /**
     * 插入点赞
     */
    int insert(CommentLike like);
    
    /**
     * 删除点赞
     */
    int deleteByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);
    
    /**
     * 根据评论ID删除所有点赞
     */
    int deleteByCommentId(@Param("commentId") Long commentId);
}

