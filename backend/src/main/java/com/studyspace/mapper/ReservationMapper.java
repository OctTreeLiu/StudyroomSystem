package com.studyspace.mapper;

import com.studyspace.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预约Mapper接口
 */
@Mapper
public interface ReservationMapper {
    
    /**
     * 根据ID查询预约
     */
    Reservation selectById(@Param("id") Long id);
    
    /**
     * 根据预约编号查询预约
     */
    Reservation selectByReservationNumber(@Param("reservationNumber") String reservationNumber);
    
    /**
     * 根据用户ID查询预约列表
     */
    List<Reservation> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询所有预约（管理员使用）
     */
    List<Reservation> selectAll();
    
    /**
     * 按日期查询预约记录（开始时间或结束时间在指定日期内）
     */
    List<Reservation> selectAllByDate(@Param("date") String date);
    
    /**
     * 根据座位ID和时间段查询冲突的预约
     */
    List<Reservation> selectConflictingReservations(
        @Param("seatId") Long seatId,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime
    );
    
    /**
     * 根据座位ID查询进行中的预约
     */
    List<Reservation> selectActiveReservationsBySeatId(@Param("seatId") Long seatId);

    /**
     * 查询某个自习室的进行中预约（已付款待使用或使用中，且未结束）
     */
    List<Reservation> selectActiveReservationsByRoomId(@Param("roomId") Long roomId);

    /**
     * 查询所有已结束但未标记为已完成的预约
     */
    List<Reservation> selectExpiredReservations();

    /**
     * 查询即将在5分钟内结束的预约（状态为使用中或已付款待使用）
     */
    List<Reservation> selectReservationsEndingSoon();

    /**
     * 查询超时未支付的预约（创建5分钟后仍未支付）
     */
    List<Reservation> selectUnpaidTimeoutReservations();

    /**
     * 根据座位ID查询未支付的预约
     */
    List<Reservation> selectUnpaidReservationsBySeatId(@Param("seatId") Long seatId);

    /**
     * 根据座位ID和时间段查询冲突的预约（已付款或使用中）
     */
    List<Reservation> selectConflictingReservationsByTimeRange(
        @Param("seatId") Long seatId,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime
    );

    /**
     * 根据座位ID和时间段查询冲突的未支付预约（锁定状态）
     */
    List<Reservation> selectUnpaidConflictingReservationsByTimeRange(
        @Param("seatId") Long seatId,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime
    );

    /**
     * 根据座位ID和日期查询该天的所有预约
     */
    List<Reservation> selectReservationsBySeatIdAndDate(
        @Param("seatId") Long seatId,
        @Param("date") java.time.LocalDate date
    );

    /**
     * 查询用户当前有效的入场凭证（在时间段内或距离开始时间≤15分钟）
     */
    Reservation selectCurrentEntryTicket(@Param("userId") Long userId);

    /**
     * 查询用户在指定日期范围内有效的预约记录（已付款且未取消）
     * 用于使用时长统计
     */
    List<Reservation> selectValidReservationsByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") java.time.LocalDate startDate,
        @Param("endDate") java.time.LocalDate endDate
    );

    /**
     * 查询所有用户在指定日期范围内有效的预约记录（已付款且未取消）
     * 用于使用时长统计
     */
    List<Reservation> selectAllValidReservationsByDateRange(
        @Param("startDate") java.time.LocalDate startDate,
        @Param("endDate") java.time.LocalDate endDate
    );
    
    /**
     * 查询用户在指定时间段内是否有未开始或进行中的预约订单（用于防止重复预约）
     * 条件：已支付且未取消，且时间段有重叠
     */
    List<Reservation> selectUserConflictingReservations(
        @Param("userId") Long userId,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime
    );

    /**
     * 插入预约
     */
    int insert(Reservation reservation);

    /**
     * 更新预约
     */
    int update(Reservation reservation);
}

