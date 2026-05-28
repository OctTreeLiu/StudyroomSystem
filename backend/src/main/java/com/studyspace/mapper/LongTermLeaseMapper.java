package com.studyspace.mapper;

import com.studyspace.entity.LongTermLease;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 长期租赁Mapper接口
 */
@Mapper
public interface LongTermLeaseMapper {
    
    /**
     * 根据ID查询租赁
     */
    LongTermLease selectById(@Param("id") Long id);

    /**
     * 根据租赁编号查询租赁
     */
    LongTermLease selectByLeaseNumber(@Param("leaseNumber") String leaseNumber);
    
    /**
     * 根据用户ID查询租赁列表
     */
    List<LongTermLease> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询所有租赁（管理员使用）
     */
    List<LongTermLease> selectAll();
    
    /**
     * 根据状态查询租赁
     */
    List<LongTermLease> selectByStatus(@Param("status") Integer status);
    
    /**
     * 根据自习室ID查询有效的租赁（状态为2-已付款生效）
     */
    LongTermLease selectActiveByRoomId(@Param("roomId") Long roomId);
    
    /**
     * 根据座位ID查询有效的租赁（状态为2-已付款生效）
     */
    LongTermLease selectActiveBySeatId(@Param("seatId") Long seatId);
    
    /**
     * 查询用户当前有效的长期租赁（状态为2-已付款生效，且当前日期在租赁期间内）
     */
    LongTermLease selectCurrentActiveLeaseByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户在指定时间段内是否有生效的长期租赁订单
     * 用于预约冲突检查：如果用户在所选时间段内存在生效的长期租赁，则禁止预约
     * @param userId 用户ID
     * @param startTime 预约开始时间
     * @param endTime 预约结束时间
     * @return 如果有冲突的长期租赁，返回该租赁记录；否则返回null
     */
    LongTermLease selectActiveLeaseByUserIdAndTimeRange(
        @Param("userId") Long userId,
        @Param("startTime") java.time.LocalDateTime startTime,
        @Param("endTime") java.time.LocalDateTime endTime
    );

    /**
     * 统计待审核的长期租赁申请数量
     */
    Integer countPending();

    /**
     * 查询审核通过但未付款且超过截止时间的租赁
     */
    List<LongTermLease> selectExpiredUnpaidLeases();

    /**
     * 根据座位ID查询未支付的长期租赁申请
     */
    List<LongTermLease> selectUnpaidLeasesBySeatId(@Param("seatId") Long seatId);

    /**
     * 根据座位ID和时间段查询冲突的长期租赁
     */
    List<LongTermLease> selectConflictingLeasesByTimeRange(
        @Param("seatId") Long seatId,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime
    );

    /**
     * 根据座位ID和日期查询该天的长期租赁
     */
    List<LongTermLease> selectLeasesBySeatIdAndDate(
        @Param("seatId") Long seatId,
        @Param("date") java.time.LocalDate date
    );
    
    /**
     * 查询用户在指定日期范围内有效的长期租赁记录
     */
    List<LongTermLease> selectActiveLeasesByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") java.time.LocalDate startDate,
        @Param("endDate") java.time.LocalDate endDate
    );
    
    /**
     * 查询所有用户在指定日期范围内有效的长期租赁记录
     */
    List<LongTermLease> selectAllActiveLeasesByDateRange(
        @Param("startDate") java.time.LocalDate startDate,
        @Param("endDate") java.time.LocalDate endDate
    );
    
    /**
     * 检查指定座位是否存在当前仍在生效期内的长期租赁（按日期判断）
     */
    boolean existsActiveLeaseBySeatId(@Param("seatId") Long seatId);

    /**
     * 插入租赁
     */
    int insert(LongTermLease lease);
    
    /**
     * 更新租赁
     */
    int update(LongTermLease lease);
}

