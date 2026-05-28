package com.studyspace.mapper;

import com.studyspace.entity.MessageComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 留言评论Mapper接口
 */
@Mapper
public interface MessageCommentMapper {
    
    /**
     * 根据ID查询评论
     */
    MessageComment selectById(@Param("id") Long id);
    
    /**
     * 根据留言ID查询评论列表（按时间正序）
     * 返回一级评论和次级评论的层级结构
     */
    List<MessageComment> selectByMessageId(@Param("messageId") Long messageId);
    
    /**
     * 根据父评论ID查询次级评论列表（按时间正序）
     */
    List<MessageComment> selectByParentId(@Param("parentId") Long parentId);
    
    /**
     * 根据留言ID统计评论数量
     */
    int countByMessageId(@Param("messageId") Long messageId);
    
    /**
     * 插入评论
     */
    int insert(MessageComment comment);
    
    /**
     * 删除评论
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据留言ID删除所有评论
     */
    int deleteByMessageId(@Param("messageId") Long messageId);
    
    /**
     * 根据父评论ID删除所有次级评论
     */
    int deleteByParentId(@Param("parentId") Long parentId);
    
    /**
     * 查询所有评论（管理员用，包含层级信息）
     */
    List<MessageComment> selectAllWithPagination(@Param("offset") Integer offset, 
                                                 @Param("pageSize") Integer pageSize,
                                                 @Param("username") String username);
    
    /**
     * 统计评论总数（管理员用）
     */
    int countAll(@Param("username") String username);
}

