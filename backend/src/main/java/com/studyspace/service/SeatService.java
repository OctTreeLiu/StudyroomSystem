package com.studyspace.service;

import com.studyspace.entity.Reservation;
import com.studyspace.entity.LongTermLease;
import com.studyspace.entity.Seat;
import com.studyspace.mapper.LongTermLeaseMapper;
import com.studyspace.mapper.ReservationMapper;
import com.studyspace.mapper.SeatMapper;
import com.studyspace.vo.SeatTimeSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 座位服务类
 */
@Service
public class SeatService {
    
    @Autowired
    private SeatMapper seatMapper;
    
    @Autowired
    private ReservationMapper reservationMapper;
    
    @Autowired
    private LongTermLeaseMapper longTermLeaseMapper;
    
    /**
     * 根据ID查询座位
     */
    public Seat getSeatById(Long id) {
        return seatMapper.selectById(id);
    }
    
    /**
     * 新增座位（管理员创建自习室等场景）
     */
    public void insertSeat(Seat seat) {
        seatMapper.insert(seat);
    }

    /**
     * 根据自习室ID查询所有座位
     */
    public List<Seat> getSeatsByRoomId(Long roomId) {
        // 管理员端座位管理：确保长期租赁到期后座位能自动释放为“空闲”
        releaseExpiredLeaseSeats(roomId);
        return seatMapper.selectByRoomId(roomId);
    }

    private void releaseExpiredLeaseSeats(Long roomId) {
        List<Seat> leasedSeats = seatMapper.selectByRoomIdAndStatus(roomId, 2);
        if (leasedSeats == null || leasedSeats.isEmpty()) {
            return;
        }

        for (Seat seat : leasedSeats) {
            if (seat == null || seat.getId() == null) {
                continue;
            }
            boolean activeLease = longTermLeaseMapper.existsActiveLeaseBySeatId(seat.getId());
            if (!activeLease) {
                seat.setStatus(0);
                seatMapper.update(seat);
            }
        }
    }
    
    /**
     * 根据自习室ID和状态查询座位
     */
    public List<Seat> getSeatsByRoomIdAndStatus(Long roomId, Integer status) {
        return seatMapper.selectByRoomIdAndStatus(roomId, status);
    }
    
    /**
     * 查询所有座位
     */
    public List<Seat> getAllSeats() {
        return seatMapper.selectAll();
    }

    /**
     * 判断指定座位是否存在未支付的订单
     * 包含：未支付的预约 和 未支付的长期租赁申请
     */
    public boolean hasUnpaidOrders(Long seatId) {
        if (seatId == null) {
            return false;
        }
        // 未支付预约
        java.util.List<com.studyspace.entity.Reservation> unpaidReservations =
                reservationMapper.selectUnpaidReservationsBySeatId(seatId);
        if (unpaidReservations != null && !unpaidReservations.isEmpty()) {
            return true;
        }
        // 未支付长期租赁
        java.util.List<com.studyspace.entity.LongTermLease> unpaidLeases =
                longTermLeaseMapper.selectUnpaidLeasesBySeatId(seatId);
        return unpaidLeases != null && !unpaidLeases.isEmpty();
    }
    
    /**
     * 更新座位信息
     */
    public void updateSeat(Seat seat) {
        seatMapper.update(seat);
    }

    /**
     * 根据时间段和自习室ID查询可用座位
     * 排除：
     * 1. 维护中的座位（status = 3）
     * 2. 已被锁定、已被预约、被长期租赁的座位（status != 0）
     * 3. 时间段内有冲突的预约（状态为0、1、2的预约，不包括已取消和已完成的）
     * 4. 时间段内有长期租赁冲突的座位
     */
    public List<Seat> getAvailableSeatsByTimeRange(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        String startTimeStr = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endTimeStr = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("查询可用座位 - roomId: " + roomId + ", startTime: " + startTimeStr + ", endTime: " + endTimeStr);
        List<Seat> seats = seatMapper.selectAvailableSeatsByTimeRange(roomId, startTimeStr, endTimeStr);
        System.out.println("查询结果 - 找到 " + (seats != null ? seats.size() : 0) + " 个可用座位");
        return seats;
    }

    /**
     * 获取座位在某一天的使用情况时间轴
     * @param seatId 座位ID
     * @param date 日期
     * @return 时间段列表
     */
    public List<SeatTimeSlot> getSeatUsageTimeline(Long seatId, LocalDate date) {
        List<SeatTimeSlot> timeline = new ArrayList<>();
        
        // 检查座位是否存在
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null) {
            return timeline;
        }

        // 维护中的座位不允许查看时间轴
        if (seat.getStatus() != null && seat.getStatus() == 3) {
            throw new RuntimeException("该座位维护中，暂不可查看使用时间轴");
        }
        
        // 该天的开始和结束时间
        LocalDateTime dayStart = date.atStartOfDay(); // 00:00:00
        LocalDateTime dayEnd = date.atTime(23, 59, 59); // 23:59:59
        
        // 1. 查询该座位在该天的所有预约（已付款、使用中、未支付）
        List<Reservation> reservations = reservationMapper.selectReservationsBySeatIdAndDate(seatId, date);
        
        // 2. 查询该座位在该天的长期租赁
        List<LongTermLease> leases = longTermLeaseMapper.selectLeasesBySeatIdAndDate(seatId, date);
        
        // 3. 创建时间段列表
        List<TimeSlot> slots = new ArrayList<>();
        
        // 添加预约时间段
        for (Reservation reservation : reservations) {
            // 只处理在当天范围内的时间段
            LocalDateTime start = reservation.getStartTime().isBefore(dayStart) ? dayStart : reservation.getStartTime();
            LocalDateTime end = reservation.getEndTime().isAfter(dayEnd) ? dayEnd : reservation.getEndTime();
            
            if (start.isBefore(dayEnd) && end.isAfter(dayStart)) {
                String statusType;
                String statusText;
                if (reservation.getPaymentStatus() == 1) {
                    // 已付款（含历史已完成订单，需在时间轴中展示）
                    statusType = "reserved";
                    statusText = (reservation.getStatus() != null && reservation.getStatus() == 3)
                            ? "已完成"
                            : "已预约";
                } else {
                    // 未付款
                    statusType = "locked";
                    statusText = "已锁定";
                }
                
                slots.add(new TimeSlot(start, end, statusType, statusText, reservation.getId(), null, reservation.getUsername()));
            }
        }
        
        // 添加长期租赁时间段
        for (LongTermLease lease : leases) {
            if (lease.getStatus() == 2) { // 已付款生效
                LocalDateTime start = lease.getStartDate().atStartOfDay();
                LocalDateTime end = lease.getEndDate().atTime(23, 59, 59);
                
                // 只取当天的部分
                start = start.isBefore(dayStart) ? dayStart : start;
                end = end.isAfter(dayEnd) ? dayEnd : end;
                
                if (start.isBefore(dayEnd) && end.isAfter(dayStart)) {
                    slots.add(new TimeSlot(start, end, "leased", "长期租赁", null, lease.getId(), lease.getUsername()));
                }
            }
        }
        
        // 4. 按开始时间排序
        slots.sort(Comparator.comparing(TimeSlot::getStartTime));
        
        // 5. 生成时间轴（合并重叠，填充空闲时间段）
        LocalTime currentTime = LocalTime.MIN; // 00:00
        
        for (TimeSlot slot : slots) {
            LocalTime slotStart = slot.getStartTime().toLocalTime();
            LocalTime slotEnd = slot.getEndTime().toLocalTime();
            
            // 如果当前时间小于时间段开始时间，添加空闲时间段
            if (currentTime.isBefore(slotStart)) {
                SeatTimeSlot freeSlot = new SeatTimeSlot();
                freeSlot.setStartTime(currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                freeSlot.setEndTime(slotStart.format(DateTimeFormatter.ofPattern("HH:mm")));
                freeSlot.setStatusType("free");
                freeSlot.setStatusText("空闲");
                timeline.add(freeSlot);
            }
            
            // 添加当前时间段
            SeatTimeSlot timeSlot = new SeatTimeSlot();
            timeSlot.setStartTime(slotStart.format(DateTimeFormatter.ofPattern("HH:mm")));
            timeSlot.setEndTime(slotEnd.format(DateTimeFormatter.ofPattern("HH:mm")));
            timeSlot.setStatusType(slot.getStatusType());
            timeSlot.setStatusText(slot.getStatusText());
            timeSlot.setReservationId(slot.getReservationId());
            timeSlot.setLeaseId(slot.getLeaseId());
            timeSlot.setUsername(slot.getUsername());
            timeline.add(timeSlot);
            
            currentTime = slotEnd;
        }
        
        // 6. 如果最后还有剩余时间，添加空闲时间段
        if (currentTime.isBefore(LocalTime.MAX)) {
            SeatTimeSlot freeSlot = new SeatTimeSlot();
            freeSlot.setStartTime(currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            freeSlot.setEndTime("24:00");
            freeSlot.setStatusType("free");
            freeSlot.setStatusText("空闲");
            timeline.add(freeSlot);
        }
        
        // 7. 如果没有任何时间段，说明全天空闲
        if (timeline.isEmpty()) {
            SeatTimeSlot freeSlot = new SeatTimeSlot();
            freeSlot.setStartTime("00:00");
            freeSlot.setEndTime("24:00");
            freeSlot.setStatusType("free");
            freeSlot.setStatusText("空闲");
            timeline.add(freeSlot);
        }
        
        return timeline;
    }
    
    /**
     * 获取座位在某一天的使用情况时间轴（普通用户版本，不返回用户名）
     * @param seatId 座位ID
     * @param date 日期
     * @return 时间段列表（不包含用户名信息）
     */
    public List<SeatTimeSlot> getSeatUsageTimelineForUser(Long seatId, LocalDate date) {
        List<SeatTimeSlot> timeline = getSeatUsageTimeline(seatId, date);
        // 移除用户名信息
        for (SeatTimeSlot slot : timeline) {
            slot.setUsername(null);
        }
        return timeline;
    }
    
    /**
     * 内部类：时间段
     */
    private static class TimeSlot {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String statusType;
        private String statusText;
        private Long reservationId;
        private Long leaseId;
        private String username;
        
        public TimeSlot(LocalDateTime startTime, LocalDateTime endTime, String statusType, 
                       String statusText, Long reservationId, Long leaseId, String username) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.statusType = statusType;
            this.statusText = statusText;
            this.reservationId = reservationId;
            this.leaseId = leaseId;
            this.username = username;
        }
        
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public String getStatusType() { return statusType; }
        public String getStatusText() { return statusText; }
        public Long getReservationId() { return reservationId; }
        public Long getLeaseId() { return leaseId; }
        public String getUsername() { return username; }
    }
}

