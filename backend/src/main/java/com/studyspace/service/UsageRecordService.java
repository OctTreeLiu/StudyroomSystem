package com.studyspace.service;

import com.studyspace.entity.Reservation;
import com.studyspace.entity.UsageRecord;
import com.studyspace.mapper.ReservationMapper;
import com.studyspace.mapper.UsageRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



/**
 * 使用记录服务类
 */
@Service
public class UsageRecordService {
    
    @Autowired
    private UsageRecordMapper usageRecordMapper;
    
    @Autowired
    private ReservationMapper reservationMapper;
    
    /**
     * 根据用户ID查询使用记录
     * 同时检查并创建已完成的预约记录
     */
    public java.util.List<UsageRecord> getRecordsByUserId(Long userId) {
        // 查询该用户所有已支付且已完成的预约
        java.util.List<Reservation> completedReservations = reservationMapper.selectByUserId(userId);
        completedReservations.stream()
            .filter(r -> r.getPaymentStatus() == 1 && r.getStatus() == 3)
            .forEach(r -> {
                try {
                    createRecordForCompletedReservation(r.getId());
                } catch (Exception e) {
                    // 创建失败不影响查询，只记录日志
                    System.err.println("为预约创建使用记录失败: " + e.getMessage());
                }
            });
        
        return usageRecordMapper.selectByUserId(userId);
    }
    
    /**
     * 根据ID查询使用记录
     */
    public UsageRecord getRecordById(Long id) {
        return usageRecordMapper.selectById(id);
    }
    
    /**
     * 查询所有使用记录（管理员使用）
     */
    public java.util.List<UsageRecord> getAllRecords() {
        return usageRecordMapper.selectAll();
    }
    
    /**
     * 为已开始的预约创建使用记录
     * 这个方法应该被定时任务调用，或者在使用开始时调用
     */
    @Transactional
    public void createRecordForReservation(Long reservationId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            return;
        }
        
        // 检查是否已有记录
        java.util.List<UsageRecord> existingRecords = usageRecordMapper.selectByUserId(reservation.getUserId());
        boolean exists = existingRecords.stream()
            .anyMatch(r -> r.getReservationId() != null && r.getReservationId().equals(reservationId));
        
        if (exists) {
            return; // 已有记录，不重复创建
        }
        
        // 创建使用记录
        UsageRecord record = new UsageRecord();
        record.setUserId(reservation.getUserId());
        record.setRoomId(reservation.getRoomId());
        record.setSeatId(reservation.getSeatId());
        record.setReservationId(reservationId);
        record.setStartTime(reservation.getStartTime());
        record.setType(java.lang.Integer.valueOf(1)); // 预约使用
        record.setDuration(java.lang.Integer.valueOf(0)); // 初始时长为0，结束时更新
        
        usageRecordMapper.insert(record);
    }
    
    /**
     * 更新使用记录（结束时调用）
     */
    @Transactional
    public void updateRecordEndTime(Long reservationId, java.time.LocalDateTime endTime) {
        UsageRecord record = usageRecordMapper.selectByReservationId(reservationId);
        
        if (record != null) {
            record.setEndTime(endTime);
            if (record.getStartTime() != null) {
                long minutes = java.time.Duration.between(record.getStartTime(), endTime).toMinutes();
                record.setDuration(java.lang.Integer.valueOf((int) minutes));
            }
            usageRecordMapper.update(record);
        }
    }
    
    /**
     * 为已完成的预约创建使用记录
     * 当预约状态更新为"已完成"时调用
     */
    @Transactional
    public void createRecordForCompletedReservation(Long reservationId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            return;
        }
        
        // 只处理已支付且已完成的预约
        if (reservation.getPaymentStatus() != 1 || reservation.getStatus() != 3) {
            return;
        }
        
        // 检查是否已有记录
        UsageRecord existingRecord = usageRecordMapper.selectByReservationId(reservationId);
        if (existingRecord != null) {
            // 如果已有记录但未设置结束时间，更新它
            if (existingRecord.getEndTime() == null && reservation.getEndTime() != null) {
                existingRecord.setEndTime(reservation.getEndTime());
                if (existingRecord.getStartTime() != null) {
                    long minutes = java.time.Duration.between(existingRecord.getStartTime(), reservation.getEndTime()).toMinutes();
                    existingRecord.setDuration((int) minutes);
                }
                usageRecordMapper.update(existingRecord);
            }
            return; // 已有记录，不重复创建
        }
        
        // 创建完整的使用记录
        UsageRecord record = new UsageRecord();
        record.setUserId(reservation.getUserId());
        record.setRoomId(reservation.getRoomId());
        record.setSeatId(reservation.getSeatId());
        record.setReservationId(reservationId);
        record.setStartTime(reservation.getStartTime());
        record.setEndTime(reservation.getEndTime());
        record.setType(java.lang.Integer.valueOf(1)); // 预约使用
        
        // 计算使用时长（分钟）
        if (reservation.getStartTime() != null && reservation.getEndTime() != null) {
            long minutes = java.time.Duration.between(reservation.getStartTime(), reservation.getEndTime()).toMinutes();
            record.setDuration(java.lang.Integer.valueOf((int) minutes));
        }
        
        usageRecordMapper.insert(record);
    }
    
    /**
     * 为长期租赁创建使用记录（简化处理，每天创建一条）
     */
    @Transactional
    public void createRecordForLease(Long leaseId, Long userId, Long roomId, Long seatId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        UsageRecord record = new UsageRecord();
        record.setUserId(userId);
        record.setRoomId(roomId);
        record.setSeatId(seatId);
        record.setLeaseId(leaseId);
        record.setStartTime(startTime);
        record.setEndTime(endTime);
        record.setType(java.lang.Integer.valueOf(2)); // 长期租赁使用
        
        if (startTime != null && endTime != null) {
            long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
            record.setDuration(java.lang.Integer.valueOf((int) minutes));
        }
        
        usageRecordMapper.insert(record);
    }
    
    /**
     * 为长期租赁订单创建使用记录（长期租赁结束时调用）
     * @param lease 长期租赁订单
     */
    @Transactional
    public void createByLongTermLease(com.studyspace.entity.LongTermLease lease) {
        if (lease == null) {
            return;
        }
        
        // 只处理已付款生效的长期租赁（状态为2）
        if (lease.getPaymentStatus() == null || lease.getPaymentStatus() != 1 
            || lease.getStatus() == null || lease.getStatus() != 2) {
            return;
        }
        
        // 检查是否已有记录
        java.util.List<UsageRecord> existingRecords = usageRecordMapper.selectByUserId(lease.getUserId());
        boolean exists = existingRecords.stream()
            .anyMatch(r -> r.getLeaseId() != null && r.getLeaseId().equals(lease.getId()));
        
        if (exists) {
            return; // 已有记录，不重复创建
        }
        
        // 严禁将 LocalDate 与 LocalDateTime 混用：直接使用当前实体可用字段含义
        // 将租赁日期转换为使用记录时间：开始日期 00:00:00，结束日期 00:00:00（不做 23:59:59 人工扩展）
        java.time.LocalDateTime startTime = lease.getStartDate() != null
            ? lease.getStartDate().atStartOfDay()
            : null;
        java.time.LocalDateTime endTime = lease.getEndDate() != null
            ? lease.getEndDate().atStartOfDay()
            : null;

        // 创建使用记录
        UsageRecord record = new UsageRecord();
        record.setUserId(lease.getUserId());
        record.setRoomId(lease.getRoomId());
        record.setSeatId(lease.getSeatId());
        record.setLeaseId(lease.getId());
        record.setStartTime(startTime);
        record.setEndTime(endTime);
        record.setType(java.lang.Integer.valueOf(2)); // 长期租赁使用（2-长期租赁使用）
        record.setCreateTime(java.time.LocalDateTime.now());

        // 计算使用时长（分钟）
        if (startTime != null && endTime != null) {
            long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
            record.setDuration(java.lang.Integer.valueOf((int) minutes));
        }

        usageRecordMapper.insert(record);
    }
}

