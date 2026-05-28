package com.studyspace.mapper;

import com.studyspace.entity.AdminCall;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理员呼叫记录Mapper接口
 */
@Mapper
public interface AdminCallMapper {
    
    /**
     * 插入呼叫记录
     */
    int insert(AdminCall adminCall);
    
    /**
     * 根据ID查询
     */
    AdminCall selectById(@Param("id") Long id);
    
    /**
     * 查询所有呼叫记录（管理员使用）
     */
    List<AdminCall> selectAll();
    
    /**
     * 分页查询所有呼叫记录（管理员使用），支持按用户名或用户ID模糊查询
     */
    List<AdminCall> selectAllWithPagination(@Param("offset") Integer offset,
                                            @Param("limit") Integer limit,
                                            @Param("keyword") String keyword);
    
    /**
     * 根据状态查询呼叫记录
     */
    List<AdminCall> selectByStatus(@Param("status") Integer status);
    
    /**
     * 根据状态分页查询呼叫记录，支持按用户名或用户ID模糊查询
     */
    List<AdminCall> selectByStatusWithPagination(@Param("status") Integer status,
                                                 @Param("offset") Integer offset,
                                                 @Param("limit") Integer limit,
                                                 @Param("keyword") String keyword);
    
    /**
     * 统计所有呼叫记录总数，支持按用户名或用户ID模糊查询
     */
    int countAll(@Param("keyword") String keyword);
    
    /**
     * 根据状态统计呼叫记录总数，支持按用户名或用户ID模糊查询
     */
    int countByStatus(@Param("status") Integer status, @Param("keyword") String keyword);
    
    /**
     * 根据用户ID查询呼叫记录
     */
    List<AdminCall> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 更新呼叫记录
     */
    int update(AdminCall adminCall);
    
    /**
     * 统计待处理数量
     */
    int countPending();
}

