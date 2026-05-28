package com.studyspace.mapper;

import com.studyspace.entity.Points;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 积分流水记录Mapper接口
 */
@Mapper
public interface PointsMapper {
    
    /**
     * 插入积分流水记录
     */
    int insert(Points points);
    
    /**
     * 根据ID查询积分流水记录
     */
    Points selectById(@Param("id") Long id);
    
    /**
     * 根据用户ID分页查询积分流水记录
     */
    List<Points> selectByUserIdWithPagination(@Param("userId") Long userId, 
                                             @Param("offset") Integer offset, 
                                             @Param("limit") Integer limit);
    
    /**
     * 统计用户积分流水记录总数
     */
    int countByUserId(@Param("userId") Long userId);
    
    /**
     * 分页查询所有积分流水记录（管理员使用）
     */
    List<Points> selectAllWithPagination(@Param("offset") Integer offset, 
                                         @Param("limit") Integer limit,
                                         @Param("username") String username);
    
    /**
     * 统计所有积分流水记录总数（管理员使用）
     */
    int countAll(@Param("username") String username);
    
    /**
     * 检查用户今日是否已签到
     */
    Points selectTodaySignIn(@Param("userId") Long userId, @Param("today") String today);
    
    /**
     * 根据用户ID和类型查询积分流水记录
     */
    List<Points> selectByUserIdAndType(@Param("userId") Long userId, 
                                       @Param("type") String type);
    
    /**
     * 检查用户今日是否已获得长期租赁积分奖励
     */
    Points selectTodayLeaseReward(@Param("userId") Long userId, @Param("today") String today);
}

