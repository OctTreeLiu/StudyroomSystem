package com.studyspace.service;

import com.studyspace.entity.LongTermLease;
import com.studyspace.entity.Reservation;
import com.studyspace.entity.Seat;
import com.studyspace.entity.StudyRoom;
import com.studyspace.mapper.LongTermLeaseMapper;
import com.studyspace.mapper.ReservationMapper;
import com.studyspace.mapper.StudyRoomMapper;
import com.studyspace.service.SeatService;
import com.studyspace.service.UsageRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 自习室服务类
 */
@Service
public class StudyRoomService {
    
    @Autowired
    private StudyRoomMapper studyRoomMapper;
    
    @Autowired
    private ReservationMapper reservationMapper;
    
    @Autowired
    private LongTermLeaseMapper longTermLeaseMapper;
    
    @Autowired
    private UsageRecordService usageRecordService;
    
    @Autowired
    private SeatService seatService;
    
    /**
     * 查询所有自习室（自动检查并更新已结束的预约状态，并更新大区状态为"有座/无座"）
     */
    public List<StudyRoom> getAllStudyRooms() {
        // 先返回数据，状态更新异步执行，不阻塞查询
        List<StudyRoom> rooms = studyRoomMapper.selectAll();
        
        // 异步更新状态（在新线程中执行，不阻塞主查询）
        new Thread(() -> {
            try {
                updateExpiredReservations();
                updateSeatStatuses();
                updateRoomAvailabilityStatus();
            } catch (Exception e) {
                // 状态更新失败不影响查询，只记录日志
                System.err.println("更新自习室状态失败: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
        
        return rooms;
    }
    
    /**
     * 更新大区状态：0-有座（有可用座位），1-无座（无可用座位）
     */
    @Transactional
    public void updateRoomAvailabilityStatus() {
        try {
            List<StudyRoom> allRooms = studyRoomMapper.selectAll();
            
            for (StudyRoom room : allRooms) {
                try {
                    // 查询该大区的空闲座位（只查询空闲座位，优化性能）
                    List<Seat> availableSeats = seatService.getSeatsByRoomIdAndStatus(room.getId(), 0);
                    
                    // 更新大区状态：0-有座，1-无座
                    int newStatus = (availableSeats != null && !availableSeats.isEmpty()) ? 0 : 1;
                    if (room.getStatus() == null || !room.getStatus().equals(newStatus)) {
                        room.setStatus(newStatus);
                        studyRoomMapper.update(room);
                    }
                } catch (Exception e) {
                    // 单个大区更新失败不影响其他大区
                    System.err.println("更新大区 " + room.getId() + " 状态失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // 整体更新失败不影响主查询
            System.err.println("更新大区状态失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 检查并更新已结束的预约状态，并更新座位状态
     */
    @Transactional
    public void updateExpiredReservations() {
        try {
            // 查询所有已结束但未标记为已完成的预约（SQL中使用NOW()判断）
            List<Reservation> expiredReservations = reservationMapper.selectExpiredReservations();
            if (expiredReservations == null || expiredReservations.isEmpty()) {
                return; // 没有过期的预约，直接返回
            }
            
            // 限制每次最多处理50个过期预约，避免一次性处理太多
            List<Reservation> toProcess = expiredReservations.stream()
                .limit(50)
                .collect(java.util.stream.Collectors.toList());
            
            for (Reservation reservation : toProcess) {
                try {
                    // 更新预约状态为已完成
                    reservation.setStatus(3); // 已完成
                    reservationMapper.update(reservation);
                    
                    // 更新座位状态
                    if (reservation.getSeatId() != null) {
                        Seat seat = seatService.getSeatById(reservation.getSeatId());
                        if (seat != null && seat.getStatus() != null && seat.getStatus() == 1) {
                            // 检查该座位是否还有其他进行中的预约
                            List<Reservation> activeReservations = reservationMapper.selectActiveReservationsBySeatId(reservation.getSeatId());
                            boolean hasActiveReservation = activeReservations != null && activeReservations.stream()
                                .anyMatch(r -> r != null && r.getStatus() != null && r.getStatus() != 4 && r.getStatus() != 3 && !r.getId().equals(reservation.getId()));
                            if (!hasActiveReservation) {
                                // 检查是否有长期租赁
                                LongTermLease activeLease = longTermLeaseMapper.selectActiveBySeatId(reservation.getSeatId());
                                if (activeLease == null) {
                                    seat.setStatus(0); // 空闲
                                    seatService.updateSeat(seat);
                                } else {
                                    seat.setStatus(2); // 被长期租赁
                                    seatService.updateSeat(seat);
                                }
                            }
                        }
                    }
                    
                    // 为已完成的预约创建使用记录（如果还没有）
                    try {
                        usageRecordService.createRecordForCompletedReservation(reservation.getId());
                    } catch (Exception e) {
                        // 创建使用记录失败不影响状态更新，只记录日志
                        System.err.println("为预约创建使用记录失败: " + e.getMessage());
                    }
                } catch (Exception e) {
                    // 单个预约更新失败不影响其他预约，只记录日志
                    System.err.println("更新预约 " + reservation.getId() + " 状态失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // 整体更新失败不影响主查询，只记录日志
            System.err.println("更新过期预约状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查并更新所有座位的状态
     * 确保状态为"已被预约"的座位确实有进行中的预约
     */
    @Transactional
    public void updateSeatStatuses() {
        try {
            // 查询所有座位，处理状态为"已被预约"和"空闲"的座位
            List<Seat> allSeats = seatService.getAllSeats();
            List<Seat> seatsToCheck = allSeats.stream()
                .filter(s -> s.getStatus() != null && (s.getStatus() == 0 || s.getStatus() == 1))
                .limit(100) // 限制每次最多处理100个座位，避免一次性处理太多
                .collect(java.util.stream.Collectors.toList());
            
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            
            for (Seat seat : seatsToCheck) {
                try {
                    // 检查是否有有效的长期租赁
                    LongTermLease activeLease = longTermLeaseMapper.selectActiveBySeatId(seat.getId());
                    if (activeLease != null) {
                        // 如果有有效的长期租赁，状态应该为"被长期租赁"（status = 2）
                        if (seat.getStatus() != 2) {
                            seat.setStatus(2);
                            seatService.updateSeat(seat);
                        }
                        continue;
                    }
                    
                    // 检查是否有进行中的预约（当前时间在预约时间段内）
                    List<Reservation> activeReservations = reservationMapper.selectActiveReservationsBySeatId(seat.getId());
                    
                    // 过滤出真正进行中的预约（当前时间在预约时间段内）
                    boolean hasActiveReservation = activeReservations.stream()
                        .anyMatch(reservation -> 
                            (now.isAfter(reservation.getStartTime().minusSeconds(1)) || now.isEqual(reservation.getStartTime())) && 
                            now.isBefore(reservation.getEndTime())
                        );
                    
                    // 根据当前时间是否在预约时间段内来更新座位状态
                    if (hasActiveReservation) {
                        // 如果有进行中的预约，确保状态为"已预约"
                        if (seat.getStatus() != 1) {
                            seat.setStatus(1); // 已被预约
                            seatService.updateSeat(seat);
                        }
                    } else {
                        // 如果没有进行中的预约，将座位状态更新为空闲
                        if (seat.getStatus() != 0) {
                            seat.setStatus(0); // 空闲
                            seatService.updateSeat(seat);
                        }
                    }
                } catch (Exception e) {
                    // 单个座位更新失败不影响其他座位
                    System.err.println("更新座位 " + seat.getId() + " 状态失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // 整体更新失败不影响主查询
            System.err.println("更新座位状态失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 根据ID查询自习室（自动检查并更新已结束的预约状态）
     */
    public StudyRoom getStudyRoomById(Long id) {
        // 先返回数据，状态更新异步执行
        StudyRoom room = studyRoomMapper.selectById(id);
        
        // 异步更新状态
        new Thread(() -> {
            try {
                updateExpiredReservations();
                updateSeatStatuses();
                updateRoomAvailabilityStatus();
            } catch (Exception e) {
                System.err.println("更新自习室状态失败: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
        
        return room;
    }
    
    /**
     * 根据状态查询自习室（自动检查并更新已结束的预约状态）
     */
    public List<StudyRoom> getStudyRoomsByStatus(Integer status) {
        // 先返回数据，状态更新异步执行
        List<StudyRoom> rooms = studyRoomMapper.selectByStatus(status);
        
        // 异步更新状态
        new Thread(() -> {
            try {
                updateExpiredReservations();
                updateSeatStatuses();
                updateRoomAvailabilityStatus();
            } catch (Exception e) {
                System.err.println("更新自习室状态失败: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
        
        return rooms;
    }
    
    /**
     * 更新自习室信息
     */
    public void updateStudyRoom(StudyRoom studyRoom) {
        studyRoomMapper.update(studyRoom);
    }

    private static final int MAX_ROOM_CAPACITY = 500;

    /**
     * 管理员新增自习室：写入基本信息并按容量生成空闲座位，大区状态为有座。
     */
    @Transactional(rollbackFor = Exception.class)
    public StudyRoom createStudyRoom(StudyRoom room) {
        if (room == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        String roomNumber = room.getRoomNumber() == null ? "" : room.getRoomNumber().trim();
        String roomName = room.getRoomName() == null ? "" : room.getRoomName().trim();
        if (roomNumber.isEmpty()) {
            throw new IllegalArgumentException("自习室编号不能为空");
        }
        if (roomName.isEmpty()) {
            throw new IllegalArgumentException("自习室名称不能为空");
        }
        if (room.getCapacity() == null || room.getCapacity() < 1) {
            throw new IllegalArgumentException("容量至少为 1");
        }
        if (room.getCapacity() > MAX_ROOM_CAPACITY) {
            throw new IllegalArgumentException("容量不能超过 " + MAX_ROOM_CAPACITY);
        }
        if (studyRoomMapper.selectByRoomNumber(roomNumber) != null) {
            throw new IllegalArgumentException("自习室编号已存在");
        }
        room.setRoomNumber(roomNumber);
        room.setRoomName(roomName);
        if (room.getLocation() != null) {
            room.setLocation(room.getLocation().trim());
            if (room.getLocation().isEmpty()) {
                room.setLocation(null);
            }
        }
        if (room.getDescription() != null) {
            room.setDescription(room.getDescription().trim());
            if (room.getDescription().isEmpty()) {
                room.setDescription(null);
            }
        }
        room.setStatus(0);
        studyRoomMapper.insert(room);
        Long roomId = room.getId();
        if (roomId == null) {
            throw new IllegalStateException("创建自习室失败，未生成主键");
        }
        int cap = room.getCapacity();
        int numWidth = Math.max(2, String.valueOf(cap).length());
        String numFmt = "%0" + numWidth + "d";
        for (int i = 1; i <= cap; i++) {
            Seat seat = new Seat();
            seat.setRoomId(roomId);
            seat.setSeatNumber("S" + String.format(numFmt, i));
            seat.setSeatName(roomName + "-" + String.format(numFmt, i) + "号座位");
            seat.setStatus(0);
            seatService.insertSeat(seat);
        }
        return studyRoomMapper.selectById(roomId);
    }
}

