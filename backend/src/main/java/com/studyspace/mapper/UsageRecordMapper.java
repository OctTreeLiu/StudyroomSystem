package com.studyspace.mapper;

import com.studyspace.entity.UsageRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 使用记录Mapper接口
 */
@Mapper
public interface UsageRecordMapper {
    
    /**
     * 根据ID查询记录
     */
    UsageRecord selectById(@Param("id") Long id);
    
    /**
     * 根据用户ID查询使用记录
     */
    List<UsageRecord> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询所有使用记录（管理员使用）
     */
    List<UsageRecord> selectAll();
    
    /**
     * 插入使用记录
     */
    int insert(UsageRecord record);
    
    /**
     * 更新使用记录
     */
    int update(UsageRecord record);
    
    /**
     * 根据预约ID查询使用记录
     */
    UsageRecord selectByReservationId(@Param("reservationId") Long reservationId);
    
    /**
     * 统计用户在指定时间范围内的总学习时长（分钟）
     */
    Integer sumDurationByUserIdAndTimeRange(@Param("userId") Long userId, 
                                            @Param("startTime") java.time.LocalDateTime startTime,
                                            @Param("endTime") java.time.LocalDateTime endTime);
    
    /**
     * 统计所有用户在指定时间范围内的总学习时长（分钟）
     */
    Integer sumDurationByTimeRange(@Param("startTime") java.time.LocalDateTime startTime,
                                    @Param("endTime") java.time.LocalDateTime endTime);
    
    /**
     * 获取用户在指定时间范围内的每日统计
     */
    List<java.util.Map<String, Object>> getDailyStatisticsByUserId(@Param("userId") Long userId,
                                                                    @Param("startTime") java.time.LocalDateTime startTime,
                                                                    @Param("endTime") java.time.LocalDateTime endTime);
    
    /**
     * 获取所有用户在指定时间范围内的每日统计
     */
    List<java.util.Map<String, Object>> getDailyStatistics(@Param("startTime") java.time.LocalDateTime startTime,
                                                             @Param("endTime") java.time.LocalDateTime endTime);
    
    /**
     * 获取所有用户在指定时间范围内的每周统计
     */
    List<java.util.Map<String, Object>> getWeeklyStatistics(@Param("startTime") java.time.LocalDateTime startTime,
                                                              @Param("endTime") java.time.LocalDateTime endTime);
    
    /**
     * 获取所有用户在指定时间范围内的每月统计
     */
    List<java.util.Map<String, Object>> getMonthlyStatistics(@Param("startTime") java.time.LocalDateTime startTime,
                                                               @Param("endTime") java.time.LocalDateTime endTime);
    
    /**
     * 获取学习时长排行榜（Top N用户）
     */
    List<java.util.Map<String, Object>> getTopUsersByStudyTime(@Param("startTime") java.time.LocalDateTime startTime,
                                                              @Param("endTime") java.time.LocalDateTime endTime,
                                                              @Param("limit") Integer limit);
    
    /**
     * 获取用户在指定时间范围内的每小时统计（过去24小时）
     */
    List<java.util.Map<String, Object>> getHourlyStatisticsByUserId(@Param("userId") Long userId,
                                                                     @Param("startTime") java.time.LocalDateTime startTime,
                                                                     @Param("endTime") java.time.LocalDateTime endTime);
    
    /**
     * 获取所有用户在指定时间范围内的每小时统计（过去24小时）
     */
    List<java.util.Map<String, Object>> getHourlyStatistics(@Param("startTime") java.time.LocalDateTime startTime,
                                                             @Param("endTime") java.time.LocalDateTime endTime);
    
    /**
     * 获取用户在指定时间范围内的使用记录详情（用于按日期拆分统计）
     */
    List<UsageRecord> selectUsageRecordsByUserIdAndTimeRange(@Param("userId") Long userId,
                                                              @Param("startTime") java.time.LocalDateTime startTime,
                                                              @Param("endTime") java.time.LocalDateTime endTime);
    
    /**
     * 获取所有用户在指定时间范围内的使用记录详情（用于按日期拆分统计）
     */
    List<UsageRecord> selectUsageRecordsByTimeRange(@Param("startTime") java.time.LocalDateTime startTime,
                                                     @Param("endTime") java.time.LocalDateTime endTime);
}

