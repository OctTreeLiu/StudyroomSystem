package com.studyspace.mapper;

import com.studyspace.entity.Seat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 座位Mapper接口
 */
@Mapper
public interface SeatMapper {
    
    /**
     * 根据ID查询座位
     */
    Seat selectById(@Param("id") Long id);
    
    /**
     * 根据自习室ID查询所有座位
     */
    List<Seat> selectByRoomId(@Param("roomId") Long roomId);
    
    /**
     * 根据自习室ID和状态查询座位
     */
    List<Seat> selectByRoomIdAndStatus(@Param("roomId") Long roomId, @Param("status") Integer status);
    
    /**
     * 查询所有座位
     */
    List<Seat> selectAll();

    /**
     * 根据自习室ID和时间段查询可用座位（排除冲突的预约和长期租赁）
     */
    List<Seat> selectAvailableSeatsByTimeRange(
        @Param("roomId") Long roomId,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime
    );
    
    /**
     * 插入座位
     */
    int insert(Seat seat);
    
    /**
     * 更新座位
     */
    int update(Seat seat);
}

