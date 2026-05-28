package com.studyspace.service;

import com.studyspace.entity.Seat;
import com.studyspace.mapper.LongTermLeaseMapper;
import com.studyspace.mapper.ReservationMapper;
import com.studyspace.mapper.SeatMapper;
import com.studyspace.mapper.StudyRoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计服务类
 */
@Service
public class StatisticsService {
    
    @Autowired
    private StudyRoomMapper studyRoomMapper;
    
    @Autowired
    private ReservationMapper reservationMapper;
    
    @Autowired
    private SeatMapper seatMapper;
    
    @Autowired
    private LongTermLeaseMapper longTermLeaseMapper;
    
    /**
     * 获取座位总体统计信息（五个大区座位之和的使用情况）
     */
    public Map<String, Object> getSeatStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 查询所有座位（五个大区的所有座位）
        List<Seat> allSeats = seatMapper.selectAll();
        int totalSeats = allSeats.size();
        
        // 查询所有当前日期在使用中的长期租赁（status=2 且当前日期在开始和结束日期之间）
        java.time.LocalDate today = java.time.LocalDate.now();
        List<com.studyspace.entity.LongTermLease> activeLeases = longTermLeaseMapper.selectAllActiveLeasesByDateRange(today, today);
        
        // 统计各状态的座位数量
        long freeSeats = 0;
        long reservedSeats = 0;
        long leasedSeats = activeLeases.size(); // 使用中的长期租赁座位数
        long maintenanceSeats = 0;
        
        for (Seat seat : allSeats) {
            // 检查该座位是否被当前使用中的长期租赁占用
            boolean isLeased = activeLeases.stream()
                .anyMatch(lease -> lease.getSeatId() != null && lease.getSeatId().equals(seat.getId()));
            
            if (seat.getStatus() != null) {
                switch (seat.getStatus()) {
                    case 0:
                        if (!isLeased) {
                            freeSeats++;
                        }
                        break;
                    case 1:
                        reservedSeats++;
                        break;
                    case 2:
                        // 如果座位状态是2（被长期租赁），但不在使用中的长期租赁列表中，说明是"已付款待使用"状态
                        // 这种情况下，座位应该算作空闲（因为还未开始使用）
                        if (!isLeased) {
                            freeSeats++;
                        }
                        break;
                    case 3:
                        maintenanceSeats++;
                        break;
                }
            } else {
                if (!isLeased) {
                    freeSeats++;
                }
            }
        }
        
        // 计算空闲率
        BigDecimal freeRate = totalSeats > 0 
            ? BigDecimal.valueOf(freeSeats * 100.0 / totalSeats)
                .setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        // 查询所有预约记录（统计预约次数）
        List<com.studyspace.entity.Reservation> allReservations = reservationMapper.selectAll();
        int totalReservations = allReservations.size();
        
        // 查询所有长期租赁订单
        List<com.studyspace.entity.LongTermLease> allLeases = longTermLeaseMapper.selectAll();
        
        // 统计已完成的预约（status == 3）
        long completedReservationCount = allReservations.stream()
            .filter(r -> r.getStatus() != null && r.getStatus() == 3)
            .count();
        
        // 统计"使用结束"的长期租赁订单（status == 6）
        long completedLeaseCount = allLeases.stream()
            .filter(l -> l.getStatus() != null && l.getStatus() == 6)
            .count();
        
        // 已完成次数 = 已完成的预约订单数 + "使用结束"的长期租赁订单数
        long completedReservations = completedReservationCount + completedLeaseCount;
        
        // 统计已取消的预约
        long cancelledReservations = allReservations.stream()
            .filter(r -> r.getStatus() != null && r.getStatus() == 4)
            .count();
        
        // 统计已支付的预约
        long paidReservations = allReservations.stream()
            .filter(r -> r.getPaymentStatus() != null && r.getPaymentStatus() == 1)
            .count();
        
        // 计算营业额：所有已完成的预约订单（status == 3）的金额总和
        BigDecimal reservationRevenue = allReservations.stream()
            .filter(r -> r.getStatus() != null && r.getStatus() == 3 && r.getAmount() != null)
            .map(r -> r.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 计算长期租赁营业额：所有"使用结束"的长期租赁订单（status == 6）的金额总和
        BigDecimal leaseRevenue = allLeases.stream()
            .filter(l -> l.getStatus() != null && l.getStatus() == 6 && l.getAmount() != null)
            .map(l -> l.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 总营业额 = 已完成的预约营业额 + "使用结束"的长期租赁营业额
        BigDecimal totalRevenue = reservationRevenue.add(leaseRevenue);
        
        statistics.put("totalSeats", totalSeats);
        statistics.put("freeSeats", freeSeats);
        statistics.put("reservedSeats", reservedSeats);
        statistics.put("leasedSeats", leasedSeats);
        statistics.put("maintenanceSeats", maintenanceSeats);
        statistics.put("freeRate", freeRate);
        statistics.put("totalReservations", totalReservations);
        statistics.put("completedReservations", completedReservations);
        statistics.put("cancelledReservations", cancelledReservations);
        statistics.put("paidReservations", paidReservations);
        statistics.put("totalRevenue", totalRevenue);
        
        return statistics;
    }
    
    /**
     * 获取自习室统计信息（保留此方法以兼容旧代码）
     */
    @Deprecated
    public Map<String, Object> getStudyRoomStatistics() {
        return getSeatStatistics();
    }
    
    /**
     * 获取每个自习室的使用情况统计
     */
    public List<Map<String, Object>> getRoomUsageStatistics() {
        List<com.studyspace.entity.StudyRoom> allRooms = studyRoomMapper.selectAll();
        List<com.studyspace.entity.Reservation> allReservations = reservationMapper.selectAll();
        
        return allRooms.stream().map(room -> {
            Map<String, Object> roomStats = new HashMap<>();
            roomStats.put("roomId", room.getId());
            roomStats.put("roomNumber", room.getRoomNumber());
            roomStats.put("roomName", room.getRoomName());
            roomStats.put("status", room.getStatus());
            
            // 统计该自习室的预约次数
            long reservationCount = allReservations.stream()
                .filter(r -> r.getRoomId().equals(room.getId()))
                .count();
            
            // 统计该自习室已完成的预约
            long completedCount = allReservations.stream()
                .filter(r -> r.getRoomId().equals(room.getId()) && r.getStatus() == 3)
                .count();
            
            roomStats.put("reservationCount", reservationCount);
            roomStats.put("completedCount", completedCount);
            
            return roomStats;
        }).collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 获取五个大区的统计信息
     */
    public List<Map<String, Object>> getAreaStatistics() {
        // 查询所有自习室（五个大区）
        List<com.studyspace.entity.StudyRoom> allRooms = studyRoomMapper.selectAll();
        // 查询所有座位
        List<Seat> allSeats = seatMapper.selectAll();
        // 查询所有预约记录
        List<com.studyspace.entity.Reservation> allReservations = reservationMapper.selectAll();
        
        // 查询所有当前日期在使用中的长期租赁（status=2 且当前日期在开始和结束日期之间）
        java.time.LocalDate today = java.time.LocalDate.now();
        List<com.studyspace.entity.LongTermLease> activeLeases = longTermLeaseMapper.selectAllActiveLeasesByDateRange(today, today);
        
        // 按大区统计
        return allRooms.stream().map(room -> {
            Map<String, Object> areaStats = new HashMap<>();
            areaStats.put("roomId", room.getId());
            areaStats.put("roomNumber", room.getRoomNumber());
            areaStats.put("roomName", room.getRoomName());
            
            // 获取该大区的所有座位
            List<Seat> roomSeats = allSeats.stream()
                .filter(s -> s.getRoomId().equals(room.getId()))
                .collect(Collectors.toList());
            
            int totalSeats = roomSeats.size();
            int freeSeats = 0;
            int reservedSeats = 0;
            int leasedSeats = 0;
            int maintenanceSeats = 0;
            
            // 统计该大区当前使用中的长期租赁座位数（只统计使用中的，不包括已付款待使用）
            leasedSeats = (int) activeLeases.stream()
                .filter(lease -> lease.getRoomId().equals(room.getId()))
                .count();
            
            // 统计座位状态（排除长期租赁的座位，因为长期租赁已单独统计）
            for (Seat seat : roomSeats) {
                // 检查该座位是否被当前使用中的长期租赁占用
                boolean isLeased = activeLeases.stream()
                    .anyMatch(lease -> lease.getSeatId() != null && lease.getSeatId().equals(seat.getId()));
                
                if (seat.getStatus() != null) {
                    switch (seat.getStatus()) {
                        case 0:
                            if (!isLeased) {
                                freeSeats++;
                            }
                            break;
                        case 1:
                            reservedSeats++;
                            break;
                        case 2:
                            // 如果座位状态是2（被长期租赁），但不在使用中的长期租赁列表中，说明是"已付款待使用"状态
                            // 这种情况下，座位应该算作空闲（因为还未开始使用）
                            if (!isLeased) {
                                freeSeats++;
                            }
                            break;
                        case 3:
                            maintenanceSeats++;
                            break;
                    }
                } else {
                    if (!isLeased) {
                        freeSeats++;
                    }
                }
            }
            
            // 统计该大区的预约次数
            long reservationCount = allReservations.stream()
                .filter(r -> r.getRoomId().equals(room.getId()))
                .count();
            
            // 查询所有长期租赁订单
            List<com.studyspace.entity.LongTermLease> allLeases = longTermLeaseMapper.selectAll();
            
            // 统计该大区已完成的预约（status == 3）
            long completedReservationCount = allReservations.stream()
                .filter(r -> r.getRoomId().equals(room.getId()) && r.getStatus() != null && r.getStatus() == 3)
                .count();
            
            // 统计该大区"使用结束"的长期租赁订单（status == 6）
            long completedLeaseCount = allLeases.stream()
                .filter(l -> l.getRoomId().equals(room.getId()) && l.getStatus() != null && l.getStatus() == 6)
                .count();
            
            // 已完成次数 = 已完成的预约订单数 + "使用结束"的长期租赁订单数
            long completedCount = completedReservationCount + completedLeaseCount;
            
            // 统计该大区已支付的预约
            long paidCount = allReservations.stream()
                .filter(r -> r.getRoomId().equals(room.getId()) && r.getPaymentStatus() == 1)
                .count();
            
            // 计算该大区的预约营业额：该大区所有已完成的预约订单（status == 3）的金额总和
            BigDecimal reservationRevenue = allReservations.stream()
                .filter(r -> r.getRoomId().equals(room.getId()) 
                    && r.getStatus() != null && r.getStatus() == 3
                    && r.getAmount() != null)
                .map(r -> r.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 计算该大区的长期租赁营业额：该大区所有"使用结束"的长期租赁订单（status == 6）的金额总和
            BigDecimal leaseRevenue = allLeases.stream()
                .filter(l -> l.getRoomId().equals(room.getId())
                    && l.getStatus() != null && l.getStatus() == 6
                    && l.getAmount() != null)
                .map(l -> l.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 该大区总营业额 = 已完成的预约营业额 + "使用结束"的长期租赁营业额
            BigDecimal areaRevenue = reservationRevenue.add(leaseRevenue);
            
            // 计算使用率（已预约+使用中的长期租赁的座位数 / 总座位数）
            BigDecimal usageRate = totalSeats > 0
                ? BigDecimal.valueOf((reservedSeats + leasedSeats) * 100.0 / totalSeats)
                    .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            
            areaStats.put("totalSeats", totalSeats);
            areaStats.put("freeSeats", freeSeats);
            areaStats.put("reservedSeats", reservedSeats);
            areaStats.put("leasedSeats", leasedSeats);
            areaStats.put("maintenanceSeats", maintenanceSeats);
            areaStats.put("reservationCount", reservationCount);
            areaStats.put("completedCount", completedCount);
            areaStats.put("paidCount", paidCount);
            areaStats.put("usageRate", usageRate);
            areaStats.put("revenue", areaRevenue);
            
            return areaStats;
        }).collect(Collectors.toList());
    }

    /**
     * 根据时间段获取座位统计信息
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    public Map<String, Object> getSeatStatisticsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 查询所有座位
        List<Seat> allSeats = seatMapper.selectAll();
        // 总座位数包括所有座位（包括维护中的）
        int totalSeats = allSeats.size();
        
        // 格式化时间字符串
        String startTimeStr = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endTimeStr = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // 统计空闲座位和占用座位（不包括维护中的座位）
        int freeSeats = 0;
        int occupiedSeats = 0;
        int maintenanceSeats = 0;
        
        for (Seat seat : allSeats) {
            // 维护中的座位单独统计，不参与空闲/占用统计
            if (seat.getStatus() != null && seat.getStatus() == 3) {
                maintenanceSeats++;
                continue;
            }
            
            // 检查该座位在时间段内是否有冲突
            boolean hasConflict = false;
            
            // 1. 检查是否有已付款或使用中的预约冲突
            List<com.studyspace.entity.Reservation> conflictingReservations = reservationMapper.selectConflictingReservationsByTimeRange(
                seat.getId(), startTimeStr, endTimeStr
            );
            if (!conflictingReservations.isEmpty()) {
                hasConflict = true;
            }
            
            // 2. 检查是否有未支付预约（锁定状态）冲突
            List<com.studyspace.entity.Reservation> unpaidReservations = reservationMapper.selectUnpaidConflictingReservationsByTimeRange(
                seat.getId(), startTimeStr, endTimeStr
            );
            if (!unpaidReservations.isEmpty()) {
                hasConflict = true;
            }
            
            // 3. 检查是否有长期租赁冲突
            List<com.studyspace.entity.LongTermLease> conflictingLeases = longTermLeaseMapper.selectConflictingLeasesByTimeRange(
                seat.getId(), startTimeStr, endTimeStr
            );
            if (!conflictingLeases.isEmpty()) {
                hasConflict = true;
            }
            
            // 4. 检查座位当前状态（如果座位本身已被锁定、已预约或被长期租赁）
            if (seat.getStatus() != null && seat.getStatus() != 0) {
                hasConflict = true;
            }
            
            if (hasConflict) {
                occupiedSeats++;
            } else {
                freeSeats++;
            }
        }
        
        // 计算空闲率和使用率（基于非维护座位数）
        int activeSeats = totalSeats - maintenanceSeats;
        BigDecimal freeRate = activeSeats > 0 
            ? BigDecimal.valueOf(freeSeats * 100.0 / activeSeats)
                .setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        BigDecimal usageRate = activeSeats > 0 
            ? BigDecimal.valueOf(occupiedSeats * 100.0 / activeSeats)
                .setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        // 计算时间段内的营业额：支付时间在时间段内的预约订单和长期租赁订单的金额总和
        List<com.studyspace.entity.Reservation> allReservations = reservationMapper.selectAll();
        BigDecimal reservationRevenue = allReservations.stream()
            .filter(r -> r.getPaymentStatus() != null && r.getPaymentStatus() == 1 
                && r.getAmount() != null 
                && r.getPaymentTime() != null
                && !r.getPaymentTime().isBefore(startTime)
                && !r.getPaymentTime().isAfter(endTime))
            .map(r -> r.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<com.studyspace.entity.LongTermLease> allLeases = longTermLeaseMapper.selectAll();
        BigDecimal leaseRevenue = allLeases.stream()
            .filter(l -> l.getPaymentStatus() != null && l.getPaymentStatus() == 1
                && l.getAmount() != null
                && l.getPaymentTime() != null
                && !l.getPaymentTime().isBefore(startTime)
                && !l.getPaymentTime().isAfter(endTime))
            .map(l -> l.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalRevenue = reservationRevenue.add(leaseRevenue);
        
        statistics.put("totalSeats", totalSeats);
        statistics.put("freeSeats", freeSeats);
        statistics.put("occupiedSeats", occupiedSeats);
        statistics.put("maintenanceSeats", maintenanceSeats);
        statistics.put("freeRate", freeRate.doubleValue());
        statistics.put("usageRate", usageRate.doubleValue());
        statistics.put("totalRevenue", totalRevenue);
        statistics.put("startTime", startTimeStr);
        statistics.put("endTime", endTimeStr);
        
        return statistics;
    }

    /**
     * 根据时间段获取五个大区的统计信息
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 各区的统计信息
     */
    public List<Map<String, Object>> getAreaStatisticsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        List<com.studyspace.entity.StudyRoom> allRooms = studyRoomMapper.selectAll();
        List<Seat> allSeats = seatMapper.selectAll();
        List<com.studyspace.entity.Reservation> allReservations = reservationMapper.selectAll();
        List<com.studyspace.entity.LongTermLease> allLeases = longTermLeaseMapper.selectAll();
        
        // 格式化时间字符串
        String startTimeStr = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endTimeStr = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        return allRooms.stream().map(room -> {
            Map<String, Object> areaStats = new HashMap<>();
            areaStats.put("roomId", room.getId());
            areaStats.put("roomNumber", room.getRoomNumber());
            areaStats.put("roomName", room.getRoomName());
            
            // 获取该大区的所有座位
            List<Seat> roomSeats = allSeats.stream()
                .filter(s -> s.getRoomId().equals(room.getId()))
                .collect(Collectors.toList());
            
            int totalSeats = roomSeats.size();
            int freeSeats = 0;
            int occupiedSeats = 0;
            
            // 统计每个座位在时间段内的使用情况
            for (Seat seat : roomSeats) {
                // 排除维护中的座位
                if (seat.getStatus() != null && seat.getStatus() == 3) {
                    continue;
                }
                
                boolean hasConflict = false;
                
                // 检查预约冲突
                List<com.studyspace.entity.Reservation> conflictingReservations = reservationMapper.selectConflictingReservationsByTimeRange(
                    seat.getId(), startTimeStr, endTimeStr
                );
                if (!conflictingReservations.isEmpty()) {
                    hasConflict = true;
                }
                
                // 检查未支付预约冲突
                List<com.studyspace.entity.Reservation> unpaidReservations = reservationMapper.selectUnpaidConflictingReservationsByTimeRange(
                    seat.getId(), startTimeStr, endTimeStr
                );
                if (!unpaidReservations.isEmpty()) {
                    hasConflict = true;
                }
                
                // 检查长期租赁冲突
                List<com.studyspace.entity.LongTermLease> conflictingLeases = longTermLeaseMapper.selectConflictingLeasesByTimeRange(
                    seat.getId(), startTimeStr, endTimeStr
                );
                if (!conflictingLeases.isEmpty()) {
                    hasConflict = true;
                }
                
                // 检查座位当前状态
                if (seat.getStatus() != null && seat.getStatus() != 0) {
                    hasConflict = true;
                }
                
                if (hasConflict) {
                    occupiedSeats++;
                } else {
                    freeSeats++;
                }
            }
            
            // 计算使用率和空闲率（基于非维护座位数）
            int activeSeats = totalSeats;
            int maintenanceCount = 0;
            for (Seat seat : roomSeats) {
                if (seat.getStatus() != null && seat.getStatus() == 3) {
                    maintenanceCount++;
                }
            }
            activeSeats = totalSeats - maintenanceCount;
            
            BigDecimal usageRate = activeSeats > 0
                ? BigDecimal.valueOf(occupiedSeats * 100.0 / activeSeats)
                    .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            
            BigDecimal freeRate = activeSeats > 0
                ? BigDecimal.valueOf(freeSeats * 100.0 / activeSeats)
                    .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            
            // 计算该大区在时间段内的营业额：支付时间在时间段内的预约订单和长期租赁订单的金额总和
            BigDecimal reservationRevenue = allReservations.stream()
                .filter(r -> r.getRoomId().equals(room.getId())
                    && r.getPaymentStatus() != null && r.getPaymentStatus() == 1
                    && r.getAmount() != null
                    && r.getPaymentTime() != null
                    && !r.getPaymentTime().isBefore(startTime)
                    && !r.getPaymentTime().isAfter(endTime))
                .map(r -> r.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal leaseRevenue = allLeases.stream()
                .filter(l -> l.getRoomId().equals(room.getId())
                    && l.getPaymentStatus() != null && l.getPaymentStatus() == 1
                    && l.getAmount() != null
                    && l.getPaymentTime() != null
                    && !l.getPaymentTime().isBefore(startTime)
                    && !l.getPaymentTime().isAfter(endTime))
                .map(l -> l.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal areaRevenue = reservationRevenue.add(leaseRevenue);
            
            areaStats.put("totalSeats", totalSeats);
            areaStats.put("freeSeats", freeSeats);
            areaStats.put("occupiedSeats", occupiedSeats);
            areaStats.put("maintenanceSeats", maintenanceCount);
            areaStats.put("usageRate", usageRate.doubleValue());
            areaStats.put("freeRate", freeRate.doubleValue());
            areaStats.put("revenue", areaRevenue);
            
            return areaStats;
        }).collect(Collectors.toList());
    }
}

