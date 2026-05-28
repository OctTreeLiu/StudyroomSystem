package com.studyspace.mapper;

import com.studyspace.entity.UserModifyLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户修改日志Mapper接口
 */
@Mapper
public interface UserModifyLogMapper {
    
    /**
     * 插入日志
     */
    int insert(UserModifyLog log);
    
    /**
     * 查询最近的修改日志（分页），支持按被修改用户ID或用户名模糊查询
     */
    List<UserModifyLog> selectRecentLogs(@Param("offset") int offset,
                                         @Param("pageSize") int pageSize,
                                         @Param("keyword") String keyword);
    
    /**
     * 统计日志总数，支持按被修改用户ID或用户名模糊查询
     */
    long countAll(@Param("keyword") String keyword);
    
    /**
     * 根据ID查询日志
     */
    UserModifyLog selectById(@Param("id") Long id);
}

