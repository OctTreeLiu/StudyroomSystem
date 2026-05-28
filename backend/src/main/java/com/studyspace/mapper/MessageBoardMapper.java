package com.studyspace.mapper;

import com.studyspace.entity.MessageBoard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 留言板Mapper接口
 */
@Mapper
public interface MessageBoardMapper {
    
    /**
     * 根据ID查询留言
     */
    MessageBoard selectById(@Param("id") Long id);
    
    /**
     * 查询所有留言列表（按时间倒序，最新在前）
     */
    List<MessageBoard> selectAll();
    
    /**
     * 分页查询所有留言列表（按时间倒序，最新在前）
     */
    List<MessageBoard> selectAllWithPagination(@Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 分页查询留言列表（支持按用户名搜索）
     */
    List<MessageBoard> selectAllWithPaginationAndUsername(@Param("offset") Integer offset, @Param("limit") Integer limit, @Param("username") String username);
    
    /**
     * 分页查询留言列表（支持按内容模糊搜索）
     */
    List<MessageBoard> selectAllWithPaginationAndContent(@Param("offset") Integer offset, @Param("limit") Integer limit, @Param("content") String content);
    
    /**
     * 分页查询留言列表（支持按用户名和内容搜索）
     */
    List<MessageBoard> selectAllWithPaginationAndSearch(@Param("offset") Integer offset, @Param("limit") Integer limit, @Param("username") String username, @Param("content") String content);
    
    /**
     * 查询留言总数
     */
    int countAll();
    
    /**
     * 查询留言总数（支持按用户名搜索）
     */
    int countAllByUsername(@Param("username") String username);
    
    /**
     * 查询留言总数（支持按内容模糊搜索）
     */
    int countAllByContent(@Param("content") String content);
    
    /**
     * 查询留言总数（支持按用户名和内容搜索）
     */
    int countAllBySearch(@Param("username") String username, @Param("content") String content);
    
    /**
     * 根据用户ID查询留言列表
     */
    List<MessageBoard> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 插入留言
     */
    int insert(MessageBoard messageBoard);
    
    /**
     * 删除留言
     */
    int deleteById(@Param("id") Long id);
}

