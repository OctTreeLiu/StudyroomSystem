package com.studyspace.service;


import com.studyspace.dto.LongTermLeaseDTO;
import com.studyspace.entity.LongTermLease;
import com.studyspace.entity.Reservation;
import com.studyspace.entity.Seat;
import com.studyspace.entity.StudyRoom;
import com.studyspace.mapper.LongTermLeaseMapper;
import com.studyspace.mapper.ReservationMapper;
import com.studyspace.mapper.StudyRoomMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * 长期租赁服务类
 */
@Service
public class LongTermLeaseService {
    
    @Autowired
    private LongTermLeaseMapper longTermLeaseMapper;
    
    @Autowired
    private StudyRoomMapper studyRoomMapper;
    
    @Autowired
    private SeatService seatService;
    
    @Autowired
    private ReservationMapper reservationMapper;
    
    @Autowired
    private PriceConfigService priceConfigService;

    @Autowired
    private AlipayService alipayService;
    
    @Autowired
    private UsageRecordService usageRecordService;
    
    /**
     * 创建长期租赁申请
     */
    @Transactional
    public LongTermLease createLeaseApplication(Long userId, LongTermLeaseDTO leaseDTO) {
        // 1. 检查自习室是否存在
        StudyRoom studyRoom = studyRoomMapper.selectById(leaseDTO.getRoomId());
        if (studyRoom == null) {
            throw new RuntimeException("自习室不存在");
        }
        
        // 2. 检查座位是否存在且属于该自习室
        Seat seat = seatService.getSeatById(leaseDTO.getSeatId());
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }
        if (!seat.getRoomId().equals(leaseDTO.getRoomId())) {
            throw new RuntimeException("座位不属于该自习室");
        }
        
        // 2.1 检查座位是否处于维护中
        if (seat.getStatus() != null && seat.getStatus() == 3) {
            throw new RuntimeException("该座位正在维护中，无法申请长期租赁");
        }
        
        // 2.2 检查座位是否已被锁定、已被预约或被长期租赁
        if (seat.getStatus() != null && seat.getStatus() != 0) {
            if (seat.getStatus() == 1) {
                throw new RuntimeException("该座位已被预约，无法申请长期租赁");
            } else if (seat.getStatus() == 2) {
                throw new RuntimeException("该座位已被长期租赁，无法申请长期租赁");
            } else if (seat.getStatus() == 4) {
                throw new RuntimeException("该座位已被锁定，请稍后再试");
            }
        }
        
        // 3. 检查时间是否有效
        if (leaseDTO.getStartTime().isAfter(leaseDTO.getEndTime())) {
            throw new RuntimeException("开始时间不能晚于结束时间");
        }
        
        if (leaseDTO.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("开始时间不能早于当前时间");
        }
        
        // 从时间中提取日期（用于存储和计算天数）
        LocalDate startDate = leaseDTO.getStartTime().toLocalDate();
        LocalDate endDate = leaseDTO.getEndTime().toLocalDate();
        
        // 4. 使用时间段检查该座位是否可用（检查预约和长期租赁冲突）
        java.util.List<Seat> availableSeats = seatService.getAvailableSeatsByTimeRange(
            leaseDTO.getRoomId(), 
            leaseDTO.getStartTime(), 
            leaseDTO.getEndTime()
        );
        boolean seatAvailable = availableSeats.stream()
            .anyMatch(s -> s.getId().equals(leaseDTO.getSeatId()));
        if (!seatAvailable) {
            throw new RuntimeException("该座位在所选时间段内不可用，可能已被预约或长期租赁");
        }
        
        // 5. 检查用户是否已有未审核或已通过的申请
        java.util.List<LongTermLease> userLeases = longTermLeaseMapper.selectByUserId(userId);
        boolean hasPending = userLeases.stream()
            .anyMatch(l -> l.getStatus() == 0 || l.getStatus() == 1 || l.getStatus() == 2);
        if (hasPending) {
            throw new RuntimeException("您已有待审核或已生效的长期租赁申请");
        }
        
        // 6. 创建长期租赁申请
        LongTermLease lease = new LongTermLease();
        lease.setUserId(userId);
        lease.setRoomId(leaseDTO.getRoomId());
        lease.setSeatId(leaseDTO.getSeatId());
        lease.setLeaseNumber(generateLeaseNumber());
        lease.setStartDate(startDate);
        lease.setEndDate(endDate);
        // 业务逻辑调整：用户提交申请后管理员自动同意，直接进入“审核通过待付款”
        lease.setStatus(1); // 审核通过待付款
        lease.setPaymentStatus(0); // 未付款
        lease.setPaymentDeadline(LocalDateTime.now().plusHours(2));
        lease.setApplyReason(leaseDTO.getApplyReason());
        lease.setAuditRemark("允许");
        
        // 计算金额（按天计算，从配置读取每天价格）
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal pricePerDay = priceConfigService.getLongLeasePricePerDay();
        lease.setAmount(BigDecimal.valueOf(days).multiply(pricePerDay).setScale(2, RoundingMode.HALF_UP));
        
        longTermLeaseMapper.insert(lease);
        
        // 7. 锁定座位（设置为已锁定状态）
        seat.setStatus(4); // 已锁定（未支付状态）
        seatService.updateSeat(seat);
        
        return lease;
    }
    
    /**
     * 生成租赁编号
     */
    private String generateLeaseNumber() {
        return "LEASE" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * 根据用户ID查询租赁列表
     * 状态必须由后端统一根据当前时间计算（见 docs/长期租赁订单状态与座位状态自动同步.md）
     */
    public java.util.List<LongTermLease> getLeasesByUserId(Long userId) {
        java.util.List<LongTermLease> leases = longTermLeaseMapper.selectByUserId(userId);
        applyDynamicStatus(leases);
        return leases;
    }

    /**
     * 根据当前时间动态修正长期租赁订单状态：
     * now < startDate  => 2 已付款生效（待使用）
     * startDate <= now <= endDate => 5 使用中
     * now > endDate => 6 使用结束
     * 仅对已支付（paymentStatus=1）且处于 2/5/6 这类“已付款后的生命周期”进行修正。
     */
    private void applyDynamicStatus(java.util.List<LongTermLease> leases) {
        if (leases == null || leases.isEmpty()) {
            return;
        }
        java.time.LocalDate today = java.time.LocalDate.now();

        for (LongTermLease lease : leases) {
            if (lease == null) {
                continue;
            }
            if (lease.getPaymentStatus() == null || lease.getPaymentStatus() != 1) {
                continue;
            }
            if (lease.getStatus() == null) {
                continue;
            }
            // 仅处理“已付款后的状态”
            if (!(lease.getStatus() == 2 || lease.getStatus() == 5 || lease.getStatus() == 6)) {
                continue;
            }
            if (lease.getStartDate() == null || lease.getEndDate() == null) {
                continue;
            }

            int computed;
            if (today.isBefore(lease.getStartDate())) {
                computed = 2;
            } else if (today.isAfter(lease.getEndDate())) {
                computed = 6;
            } else {
                computed = 5;
            }

            if (lease.getStatus() != computed) {
                lease.setStatus(computed);
                longTermLeaseMapper.update(lease);
            }
        }
    }
    
    /**
     * 查询用户当前有效的长期租赁订单（状态为2-已付款生效，且当前日期在租赁期间内）
     */
    public LongTermLease getCurrentActiveLeaseByUserId(Long userId) {
        return longTermLeaseMapper.selectCurrentActiveLeaseByUserId(userId);
    }
    
    /**
     * 检查用户在指定时间段内是否有生效的长期租赁订单
     * 用于预约冲突检查：如果用户在所选时间段内存在生效的长期租赁，则禁止预约
     * @param userId 用户ID
     * @param startTime 预约开始时间
     * @param endTime 预约结束时间
     * @return 如果有冲突的长期租赁，返回该租赁记录；否则返回null
     */
    public LongTermLease getActiveLeaseByUserIdAndTimeRange(Long userId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        return longTermLeaseMapper.selectActiveLeaseByUserIdAndTimeRange(userId, startTime, endTime);
    }
    
    /**
     * 根据ID查询租赁
     */
    public LongTermLease getLeaseById(Long id) {
        return longTermLeaseMapper.selectById(id);
    }

    /**
     * 根据租赁编号查询租赁
     */
    public LongTermLease getLeaseByNumber(String leaseNumber) {
        return longTermLeaseMapper.selectByLeaseNumber(leaseNumber);
    }
    
    /**
     * 查询所有租赁（管理员使用）
     * 与用户端列表一致：返回前按当前日期同步 2/5/6（已付款待开始、使用中、使用结束）
     */
    public java.util.List<LongTermLease> getAllLeases() {
        java.util.List<LongTermLease> leases = longTermLeaseMapper.selectAll();
        applyDynamicStatus(leases);
        return leases;
    }
    
    /**
     * 根据状态查询租赁（管理员）
     * 先全量同步动态状态再筛选，避免库中仍为 2 但日期已进入租期/已结束时筛不出来或展示不准
     */
    public java.util.List<LongTermLease> getLeasesByStatus(java.lang.Integer status) {
        java.util.List<LongTermLease> all = longTermLeaseMapper.selectAll();
        applyDynamicStatus(all);
        if (status == null) {
            return all;
        }
        java.util.List<LongTermLease> out = new java.util.ArrayList<>();
        for (LongTermLease l : all) {
            if (status.equals(l.getStatus())) {
                out.add(l);
            }
        }
        return out;
    }

    /**
     * 统计待审核的租赁申请数量
     */
    public java.lang.Integer countPending() {
        java.lang.Integer count = longTermLeaseMapper.countPending();
        return count == null ? 0 : count;
    }
    
    /**
     * 支付长期租赁（用户支付）
     */
    @Transactional
    public void payLease(Long leaseId, Long userId, String tradeNo) {
        LongTermLease lease = longTermLeaseMapper.selectById(leaseId);
        if (lease == null) {
            throw new RuntimeException("租赁记录不存在");
        }
        
        // 检查是否为该用户的租赁
        if (!lease.getUserId().equals(userId)) {
            throw new RuntimeException("无权支付该租赁");
        }
        
        // 检查状态
        if (lease.getStatus() != 1) {
            throw new RuntimeException("该租赁申请未审核通过或已支付");
        }
        
        // 检查是否超过付款截止时间
        if (lease.getPaymentDeadline() != null && lease.getPaymentDeadline().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("已超过付款截止时间，无法支付");
        }
        
        // 更新支付状态
        lease.setPaymentStatus(java.lang.Integer.valueOf(1));
        lease.setPaymentTime(java.time.LocalDateTime.now());
        lease.setStatus(java.lang.Integer.valueOf(2)); // 已付款生效
        // 保存支付宝交易号（可能为null，例如后台手动处理等场景）
        lease.setTradeNo(tradeNo);
        longTermLeaseMapper.update(lease);
        
        // 更新座位状态为被长期租赁（从已锁定状态更新）
        Seat seat = seatService.getSeatById(lease.getSeatId());
        if (seat != null) {
            // 如果座位是已锁定状态，更新为被长期租赁
            if (seat.getStatus() == 4) {
                seat.setStatus(2); // 被长期租赁
                seatService.updateSeat(seat);
            } else if (seat.getStatus() == 0) {
                // 如果座位是空闲状态（理论上不应该发生，但为了安全起见）
                seat.setStatus(2); // 被长期租赁
                seatService.updateSeat(seat);
            }
        }
    }

    /**
     * 取消未支付的长期租赁订单（用户主动取消）
     */
    @Transactional
    public void cancelUnpaidLease(Long leaseId, Long userId) {
        LongTermLease lease = longTermLeaseMapper.selectById(leaseId);
        if (lease == null) {
            throw new RuntimeException("租赁记录不存在");
        }

        // 校验归属
        if (!lease.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该租赁");
        }

        // 仅允许对审核通过待付款且未支付的订单进行取消
        if (lease.getStatus() == null || lease.getStatus() != 1) {
            throw new RuntimeException("该租赁状态不允许取消");
        }
        
        if (lease.getPaymentStatus() == null || lease.getPaymentStatus() != 0) {
            throw new RuntimeException("该租赁已支付，无法取消");
        }

        // 更新状态为已取消
        lease.setStatus(4); // 已取消
        lease.setAuditRemark("用户主动取消未支付订单");
        longTermLeaseMapper.update(lease);

        // 释放座位锁定
        Seat seat = seatService.getSeatById(lease.getSeatId());
        if (seat != null && seat.getStatus() == 4) {
            // 检查是否有其他未支付的预约或长期租赁申请锁定该座位
            java.util.List<Reservation> unpaidReservations = reservationMapper.selectUnpaidReservationsBySeatId(lease.getSeatId());
            java.util.List<LongTermLease> unpaidLeases = longTermLeaseMapper.selectUnpaidLeasesBySeatId(lease.getSeatId());
            unpaidLeases.removeIf(l -> l.getId().equals(lease.getId())); // 排除当前取消的申请
            
            // 如果没有其他未支付的订单，释放座位
            if (unpaidReservations.isEmpty() && unpaidLeases.isEmpty()) {
                // 查询该座位的所有有效预约和长期租赁
                java.util.List<Reservation> activeReservations = reservationMapper.selectActiveReservationsBySeatId(lease.getSeatId());
                LongTermLease activeLease = longTermLeaseMapper.selectActiveBySeatId(lease.getSeatId());
                
                if (activeReservations.isEmpty() && activeLease == null) {
                    seat.setStatus(0); // 空闲
                } else if (!activeReservations.isEmpty()) {
                    seat.setStatus(1); // 已被预约
                } else if (activeLease != null) {
                    seat.setStatus(2); // 被长期租赁
                }
                seatService.updateSeat(seat);
            }
        }
    }

    /**
     * 取消已支付的长期租赁并退款（用户主动退款）
     */
    @Transactional
    public void cancelLease(Long leaseId, Long userId) {
        LongTermLease lease = longTermLeaseMapper.selectById(leaseId);
        if (lease == null) {
            throw new RuntimeException("租赁记录不存在");
        }

        // 校验归属
        if (!lease.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该租赁");
        }

        // 仅允许对已付款且生效中的订单申请退款
        if (lease.getPaymentStatus() == null || lease.getPaymentStatus() != 1 || lease.getStatus() == null || lease.getStatus() != 2) {
            throw new RuntimeException("该租赁未生效或未支付，无法退款");
        }

        // 已经退款过的不能重复退款
        if (lease.getRefundStatus() != null && lease.getRefundStatus() == 1) {
            throw new RuntimeException("该租赁已退款，请勿重复操作");
        }

        // 检查支付时间是否在可退款窗口内（2小时）
        if (lease.getPaymentTime() == null) {
            throw new RuntimeException("支付时间不存在，无法判断是否可退款");
        }
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long minutes = java.time.Duration.between(lease.getPaymentTime(), now).toMinutes();
        if (minutes > 120) {
            throw new RuntimeException("超过退款允许时间，退款失败");
        }

        // 优先使用 tradeNo，如果没有 tradeNo，则仅用 outTradeNo 走退款
        String tradeNo = lease.getTradeNo();
        boolean refundSuccess = alipayService.refund(
            lease.getLeaseNumber(),   // outTradeNo 一定存在
            StringUtils.isBlank(tradeNo) ? null : tradeNo, // 允许为 null
            lease.getAmount(),        // refundAmount
            "用户取消订单"             // refundReason
        );

        if (!refundSuccess) {
            throw new RuntimeException("退款处理失败，请稍后重试或联系客服");
        }

        // 更新退款信息与状态
        lease.setRefundAmount(lease.getAmount());
        lease.setRefundTime(now);
        lease.setRefundStatus(1); // 退款成功
        lease.setRefundReason("用户取消订单");
        lease.setStatus(5); // 已退款

        longTermLeaseMapper.update(lease);

        // 释放或调整座位状态
        Seat seat = seatService.getSeatById(lease.getSeatId());
        if (seat != null) {
            // 查询该座位的所有有效预约和长期租赁
            java.util.List<Reservation> activeReservations = reservationMapper.selectActiveReservationsBySeatId(lease.getSeatId());
            LongTermLease activeLease = longTermLeaseMapper.selectActiveBySeatId(lease.getSeatId());

            if (activeReservations.isEmpty() && activeLease == null) {
                seat.setStatus(0); // 空闲
            } else if (!activeReservations.isEmpty()) {
                seat.setStatus(1); // 已被预约
            } else if (activeLease != null) {
                seat.setStatus(2); // 被长期租赁
            }
            seatService.updateSeat(seat);
        }
    }
    
    /**
     * 审核长期租赁申请
     */
    @Transactional
    public void auditLease(Long leaseId, Boolean approved, String auditRemark) {
        LongTermLease lease = longTermLeaseMapper.selectById(leaseId);
        if (lease == null) {
            throw new RuntimeException("租赁记录不存在");
        }
        
        if (lease.getStatus() != 0) {
            throw new RuntimeException("该申请已审核，无法重复审核");
        }
        
        if (approved) {
            // 审核通过，状态改为待付款，设置付款截止时间（2小时后）
            lease.setStatus(1); // 审核通过待付款
            lease.setPaymentDeadline(java.time.LocalDateTime.now().plusHours(2));
            lease.setAuditRemark(auditRemark);
            
            // 检查该时间段内是否有冲突的长期租赁
            LongTermLease existingLease = longTermLeaseMapper.selectActiveBySeatId(lease.getSeatId());
            if (existingLease != null && !existingLease.getId().equals(lease.getId())) {
                // 检查日期是否重叠
                if (!(lease.getEndDate().isBefore(existingLease.getStartDate()) 
                    || lease.getStartDate().isAfter(existingLease.getEndDate()))) {
                    throw new RuntimeException("该时间段内该座位已被长期租赁，无法审核通过");
                }
            }
        } else {
            // 审核拒绝，释放座位锁定
            lease.setStatus(3); // 已拒绝
            lease.setAuditRemark(auditRemark);
            
            // 释放座位锁定
            Seat seat = seatService.getSeatById(lease.getSeatId());
            if (seat != null && seat.getStatus() == 4) {
                // 检查是否有其他未支付的预约或长期租赁申请锁定该座位
                java.util.List<Reservation> unpaidReservations = reservationMapper.selectUnpaidReservationsBySeatId(lease.getSeatId());
                java.util.List<LongTermLease> unpaidLeases = longTermLeaseMapper.selectUnpaidLeasesBySeatId(lease.getSeatId());
                unpaidLeases.removeIf(l -> l.getId().equals(lease.getId())); // 排除当前被拒绝的申请
                
                // 如果没有其他未支付的订单，释放座位
                if (unpaidReservations.isEmpty() && unpaidLeases.isEmpty()) {
                    // 查询该座位的所有有效预约和长期租赁
                    java.util.List<Reservation> activeReservations = reservationMapper.selectActiveReservationsBySeatId(lease.getSeatId());
                    LongTermLease activeLease = longTermLeaseMapper.selectActiveBySeatId(lease.getSeatId());
                    
                    if (activeReservations.isEmpty() && activeLease == null) {
                        seat.setStatus(0); // 空闲
                    } else if (!activeReservations.isEmpty()) {
                        seat.setStatus(1); // 已被预约
                    } else if (activeLease != null) {
                        seat.setStatus(2); // 被长期租赁
                    }
                    seatService.updateSeat(seat);
                }
            }
        }
        
        longTermLeaseMapper.update(lease);
    }

    /**
     * 取消审核通过但超时未付款的租赁（超过付款截止时间，默认 2 小时）
     * @return 本次取消的订单数量
     */
    @Transactional
    public int cancelExpiredUnpaidLeases() {
        java.util.List<LongTermLease> list = longTermLeaseMapper.selectExpiredUnpaidLeases();
        if (list == null || list.isEmpty()) {
            return 0;
        }
        for (LongTermLease lease : list) {
            lease.setStatus(4); // 已取消
            lease.setPaymentStatus(0);
            lease.setAuditRemark("超时未付款，系统自动取消");
            longTermLeaseMapper.update(lease);
            
            // 释放座位锁定
            Seat seat = seatService.getSeatById(lease.getSeatId());
            if (seat != null && seat.getStatus() == 4) {
                // 检查是否有其他未支付的预约或长期租赁申请锁定该座位
                java.util.List<Reservation> unpaidReservations = reservationMapper.selectUnpaidReservationsBySeatId(lease.getSeatId());
                java.util.List<LongTermLease> unpaidLeases = longTermLeaseMapper.selectUnpaidLeasesBySeatId(lease.getSeatId());
                unpaidLeases.removeIf(l -> l.getId().equals(lease.getId())); // 排除当前取消的申请
                
                // 如果没有其他未支付的订单，释放座位
                if (unpaidReservations.isEmpty() && unpaidLeases.isEmpty()) {
                    // 查询该座位的所有有效预约和长期租赁
                    java.util.List<Reservation> activeReservations = reservationMapper.selectActiveReservationsBySeatId(lease.getSeatId());
                    LongTermLease activeLease = longTermLeaseMapper.selectActiveBySeatId(lease.getSeatId());
                    
                    if (activeReservations.isEmpty() && activeLease == null) {
                        seat.setStatus(0); // 空闲
                    } else if (!activeReservations.isEmpty()) {
                        seat.setStatus(1); // 已被预约
                    } else if (activeLease != null) {
                        seat.setStatus(2); // 被长期租赁
                    }
                    seatService.updateSeat(seat);
                }
            }
        }
        return list.size();
    }
    
    /**
     * 获取指定日期范围内在使用中的长期租赁列表（用于座位图显示）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 长期租赁列表
     */
    public java.util.List<LongTermLease> getActiveLeasesByDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return longTermLeaseMapper.selectAllActiveLeasesByDateRange(startDate, endDate);
    }
    
    /**
     * 处理已过期的长期租赁订单，自动生成使用记录并更新状态
     * 当长期租赁订单到期结束（endDate < LocalDate.now()）时调用
     */
    @Transactional
    public void processExpiredLeases() {
        java.time.LocalDate now = java.time.LocalDate.now();
        // 查询所有已付款生效（状态为2）且已过期的租赁
        java.util.List<LongTermLease> allLeases = longTermLeaseMapper.selectAll();
        java.util.List<LongTermLease> expiredLeases = allLeases.stream()
            .filter(lease -> lease.getStatus() != null && lease.getStatus() == 2 
                && lease.getEndDate() != null && lease.getEndDate().isBefore(now))
            .collect(java.util.stream.Collectors.toList());
        
        for (LongTermLease lease : expiredLeases) {
            try {
                // 生成使用记录
                usageRecordService.createByLongTermLease(lease);
                
                // 更新租赁状态为已过期
                lease.setStatus(4); // 已过期
                longTermLeaseMapper.update(lease);
                
                // 释放座位状态
                Seat seat = seatService.getSeatById(lease.getSeatId());
                if (seat != null) {
                    // 查询该座位的所有有效预约和长期租赁
                    java.util.List<Reservation> activeReservations = reservationMapper.selectActiveReservationsBySeatId(lease.getSeatId());
                    LongTermLease activeLease = longTermLeaseMapper.selectActiveBySeatId(lease.getSeatId());
                    
                    if (activeReservations.isEmpty() && activeLease == null) {
                        seat.setStatus(0); // 空闲
                    } else if (!activeReservations.isEmpty()) {
                        seat.setStatus(1); // 已被预约
                    } else if (activeLease != null) {
                        seat.setStatus(2); // 被长期租赁
                    }
                    seatService.updateSeat(seat);
                }
            } catch (Exception e) {
                // 处理失败不影响其他租赁的处理，只记录日志
                System.err.println("处理过期租赁失败，租赁ID: " + lease.getId() + ", 错误: " + e.getMessage());
            }
        }
    }
}

