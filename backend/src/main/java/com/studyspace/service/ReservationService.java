package com.studyspace.service;

import com.studyspace.dto.ReservationDTO;
import com.studyspace.entity.LongTermLease;
import com.studyspace.entity.Reservation;
import com.studyspace.entity.Seat;
import com.studyspace.entity.StudyRoom;
import com.studyspace.entity.User;
import com.studyspace.mapper.LongTermLeaseMapper;
import com.studyspace.mapper.ReservationMapper;
import com.studyspace.mapper.StudyRoomMapper;
import com.studyspace.mapper.UserMapper;
import com.studyspace.vo.EntryTicketVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 预约服务类
 */
@Service
public class ReservationService {
    
    @Autowired
    private ReservationMapper reservationMapper;
    
    @Autowired
    private StudyRoomMapper studyRoomMapper;
    
    @Autowired
    private LongTermLeaseMapper longTermLeaseMapper;
    
    @Autowired
    private SeatService seatService;
    
    @Autowired
    private PointsService pointsService;
    
    @Autowired
    private MemberService memberService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PriceConfigService priceConfigService;

    @Autowired
    private AlipayService alipayService;
    
    /**
     * 使用积分兑换预约
     */
    @Transactional
    public Reservation createReservationWithPoints(Long userId, ReservationDTO reservationDTO) {
        // 2.3 检查用户在所选时间段内是否存在生效中的长期租赁订单
        // 优化逻辑：只在所选时间段内检查是否有生效的长期租赁，允许预约长期租赁结束之后的日期
        LongTermLease conflictingLease = longTermLeaseMapper.selectActiveLeaseByUserIdAndTimeRange(
            userId, 
            reservationDTO.getStartTime(), 
            reservationDTO.getEndTime()
        );
        if (conflictingLease != null) {
            // 构建提示信息，包含座位信息
            String roomName = conflictingLease.getRoomName() != null ? conflictingLease.getRoomName() : "未知区域";
            String seatNumber = conflictingLease.getSeatNumber() != null ? conflictingLease.getSeatNumber() : "未知座位";
            throw new RuntimeException(String.format("你已经长期租赁了 %s %s号座位", roomName, seatNumber));
        }
        
        // 2.4 检查用户在所选时间段内是否有未开始或进行中的预约订单（防止重复预约）
        String startTimeStr = reservationDTO.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endTimeStr = reservationDTO.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<Reservation> userConflictingReservations = reservationMapper.selectUserConflictingReservations(
            userId, startTimeStr, endTimeStr
        );
        if (!userConflictingReservations.isEmpty()) {
            Reservation conflictReservation = userConflictingReservations.get(0);
            String roomName = conflictReservation.getRoomName() != null ? conflictReservation.getRoomName() : "未知区域";
            String seatNumber = conflictReservation.getSeatNumber() != null ? conflictReservation.getSeatNumber() : "未知座位";
            String startTime = conflictReservation.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            String endTime = conflictReservation.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            
            // 判断预约状态
            LocalDateTime now = LocalDateTime.now();
            String statusText;
            if (conflictReservation.getStartTime().isAfter(now)) {
                statusText = "未开始";
            } else if (conflictReservation.getEndTime().isAfter(now)) {
                statusText = "进行中";
            } else {
                statusText = "已预约";
            }
            
            throw new RuntimeException(String.format("你已预约了 %s %s号座位（%s 至 %s，状态：%s），无法重复预约", 
                roomName, seatNumber, startTime, endTime, statusText));
        }
        
        // 计算预约时长（分钟）
        long totalMinutes = java.time.Duration.between(reservationDTO.getStartTime(), reservationDTO.getEndTime()).toMinutes();
        
        // 检查时长是否为正数
        if (totalMinutes <= 0) {
            throw new RuntimeException("预约时长必须大于0");
        }
        
        // 检查是否为2小时的整数倍（120分钟）
        if (totalMinutes % 120 != 0) {
            throw new RuntimeException("必须是两个小时的整数倍");
        }
        
        // 计算2小时的倍数
        int twoHourMultiples = (int) (totalMinutes / 120);
        
        // 计算所需积分：30积分 * 倍数
        int requiredPoints = twoHourMultiples * 30;
        
        // 检查用户积分是否足够
        Integer currentPoints = pointsService.getUserTotalPoints(userId);
        if (currentPoints < requiredPoints) {
            throw new RuntimeException(String.format("积分不足，当前积分：%d，需要积分：%d", currentPoints, requiredPoints));
        }
        
        // 先创建预约（金额设为0，因为使用积分支付）
        Reservation reservation = createReservation(userId, reservationDTO);
        reservation.setAmount(BigDecimal.ZERO);
        reservation.setPaymentStatus(1); // 已付款（积分支付）
        reservation.setPaymentTime(LocalDateTime.now());
        reservation.setStatus(1); // 已付款待使用
        reservationMapper.update(reservation);
        
        // 扣除积分并关联预约ID（传入2小时的倍数）
        pointsService.exchangeWithPoints(userId, twoHourMultiples, reservation.getId());
        
        // 更新座位状态为已被预约（从已锁定状态更新）
        Seat seat = seatService.getSeatById(reservation.getSeatId());
        if (seat != null && seat.getStatus() == 4) {
            seat.setStatus(1); // 已被预约
            seatService.updateSeat(seat);
        }
        
        return reservation;
    }
    
    /**
     * 创建预约
     */
    @Transactional
    public Reservation createReservation(Long userId, ReservationDTO reservationDTO) {
        // 1. 检查自习室是否存在
        StudyRoom studyRoom = studyRoomMapper.selectById(reservationDTO.getRoomId());
        if (studyRoom == null) {
            throw new RuntimeException("自习室不存在");
        }
        
        // 2. 检查座位是否存在且属于该自习室
        Seat seat = seatService.getSeatById(reservationDTO.getSeatId());
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }
        if (!seat.getRoomId().equals(reservationDTO.getRoomId())) {
            throw new RuntimeException("座位不属于该自习室");
        }
        
        // 2.1 检查座位是否处于维护中
        if (seat.getStatus() != null && seat.getStatus() == 3) {
            throw new RuntimeException("该座位正在维护中，无法预约");
        }
        
        // 2.2 检查座位是否已被锁定、已被预约或被长期租赁
        if (seat.getStatus() != null && seat.getStatus() != 0) {
            if (seat.getStatus() == 1) {
                throw new RuntimeException("该座位已被预约，无法预约");
            } else if (seat.getStatus() == 2) {
                throw new RuntimeException("该座位已被长期租赁，无法预约");
            } else if (seat.getStatus() == 4) {
                throw new RuntimeException("该座位已被锁定，请稍后再试");
            }
        }
        
        // 2.3 检查用户在所选时间段内是否存在生效中的长期租赁订单
        // 优化逻辑：只在所选时间段内检查是否有生效的长期租赁，允许预约长期租赁结束之后的日期
        LongTermLease conflictingLease = longTermLeaseMapper.selectActiveLeaseByUserIdAndTimeRange(
            userId, 
            reservationDTO.getStartTime(), 
            reservationDTO.getEndTime()
        );
        if (conflictingLease != null) {
            // 构建提示信息，包含座位信息
            String roomName = conflictingLease.getRoomName() != null ? conflictingLease.getRoomName() : "未知区域";
            String seatNumber = conflictingLease.getSeatNumber() != null ? conflictingLease.getSeatNumber() : "未知座位";
            throw new RuntimeException(String.format("你已经长期租赁了 %s %s号座位", roomName, seatNumber));
        }
        
        // 2.4 检查用户在所选时间段内是否有未开始或进行中的预约订单（防止重复预约）
        String startTimeStr = reservationDTO.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endTimeStr = reservationDTO.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<Reservation> userConflictingReservations = reservationMapper.selectUserConflictingReservations(
            userId, startTimeStr, endTimeStr
        );
        if (!userConflictingReservations.isEmpty()) {
            Reservation conflictReservation = userConflictingReservations.get(0);
            String roomName = conflictReservation.getRoomName() != null ? conflictReservation.getRoomName() : "未知区域";
            String seatNumber = conflictReservation.getSeatNumber() != null ? conflictReservation.getSeatNumber() : "未知座位";
            String startTime = conflictReservation.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            String endTime = conflictReservation.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            
            // 判断预约状态
            LocalDateTime now = LocalDateTime.now();
            String statusText;
            if (conflictReservation.getStartTime().isAfter(now)) {
                statusText = "未开始";
            } else if (conflictReservation.getEndTime().isAfter(now)) {
                statusText = "进行中";
            } else {
                statusText = "已预约";
            }
            
            throw new RuntimeException(String.format("你已预约了 %s %s号座位（%s 至 %s，状态：%s），无法重复预约", 
                roomName, seatNumber, startTime, endTime, statusText));
        }
        
        // 3. 检查时间是否有效
        if (reservationDTO.getStartTime().isAfter(reservationDTO.getEndTime())) {
            throw new RuntimeException("开始时间不能晚于结束时间");
        }
        
        if (reservationDTO.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("开始时间不能早于当前时间");
        }

        // 3.1 开始/结束时间必须是15分钟刻度
        validateQuarterHour(reservationDTO.getStartTime(), "开始时间必须是15分钟刻度，例如 08:00 / 08:15 / 08:30 / 08:45");
        validateQuarterHour(reservationDTO.getEndTime(), "结束时间必须是15分钟刻度，例如 08:00 / 08:15 / 08:30 / 08:45");
        
        // 4. 检查座位是否被长期租赁
        LongTermLease activeLease = longTermLeaseMapper.selectActiveBySeatId(reservationDTO.getSeatId());
        if (activeLease != null) {
            throw new RuntimeException("该座位已被长期租赁，无法预约");
        }
        
        // 5. 检查座位时间冲突（查询是否有其他用户已付款或使用中的预约）
        // 注意：用户自身的预约冲突已在步骤2.4中检查
        List<Reservation> conflictingReservations = reservationMapper.selectConflictingReservations(
            reservationDTO.getSeatId(), startTimeStr, endTimeStr
        );
        // 过滤掉当前用户的预约（因为已经在2.4中检查过了）
        conflictingReservations.removeIf(r -> r.getUserId().equals(userId));
        if (!conflictingReservations.isEmpty()) {
            throw new RuntimeException("该座位在该时间段已被其他用户预约，请选择其他时间或座位");
        }
        
        // 6. 创建预约记录
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setRoomId(reservationDTO.getRoomId());
        reservation.setSeatId(reservationDTO.getSeatId());
        reservation.setReservationNumber(generateReservationNumber());
        reservation.setStartTime(reservationDTO.getStartTime());
        reservation.setEndTime(reservationDTO.getEndTime());
        reservation.setStatus(0); // 待付款
        reservation.setPaymentStatus(0); // 未付款
        // 计算金额（从配置读取每小时价格，按分钟精确计算）
        long minutes = java.time.Duration.between(reservationDTO.getStartTime(), reservationDTO.getEndTime()).toMinutes();
        // 按分钟计算，保留2位小数
        double hours = minutes / 60.0;
        BigDecimal pricePerHour = priceConfigService.getReservationPricePerHour();
        BigDecimal baseAmount = BigDecimal.valueOf(hours).multiply(pricePerHour).setScale(2, java.math.RoundingMode.HALF_UP);
        
        // 应用会员折扣
        User user = userMapper.selectById(userId);
        BigDecimal discount = memberService.getMemberDiscount(user);
        BigDecimal amount = baseAmount.multiply(discount).setScale(2, java.math.RoundingMode.HALF_UP);
        reservation.setAmount(amount);
        
        reservationMapper.insert(reservation);
        
        // 7. 锁定座位（设置为已锁定状态）
        seat.setStatus(4); // 已锁定（未支付状态）
        seatService.updateSeat(seat);
        
        return reservation;
    }
    
    /**
     * 生成预约编号
     */
    private String generateReservationNumber() {
        return "RES" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * 根据用户ID查询预约列表
     */
    public List<Reservation> getReservationsByUserId(Long userId) {
        List<Reservation> list = reservationMapper.selectByUserId(userId);
        list.forEach(r -> applyRuntimeStatus(r, false));
        return list;
    }
    
    /**
     * 根据ID查询预约
     */
    public Reservation getReservationById(Long id) {
        Reservation reservation = reservationMapper.selectById(id);
        applyRuntimeStatus(reservation, false);
        return reservation;
    }
    
    /**
     * 根据预约编号查询预约
     */
    public Reservation getReservationByNumber(String reservationNumber) {
        Reservation reservation = reservationMapper.selectByReservationNumber(reservationNumber);
        applyRuntimeStatus(reservation, false);
        return reservation;
    }

    /**
     * 管理员查询全部预约并同步运行时状态
     */
    public List<Reservation> getAllWithRuntimeStatus() {
        List<Reservation> list = reservationMapper.selectAll();
        list.forEach(r -> applyRuntimeStatus(r, true));
        return list;
    }
    
    /**
     * 按日期查询预约记录（开始时间或结束时间在指定日期内）
     */
    public List<Reservation> getAllByDateWithRuntimeStatus(String date) {
        List<Reservation> list = reservationMapper.selectAllByDate(date);
        list.forEach(r -> applyRuntimeStatus(r, true));
        return list;
    }
    
    /**
     * 取消预约
     */
    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        // 检查是否为该用户的预约
        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("无权取消该预约");
        }
        
        // 检查预约状态
        if (reservation.getStatus() == 3) {
            throw new RuntimeException("该预约已完成，无法取消");
        }
        
        if (reservation.getStatus() == 4) {
            throw new RuntimeException("该预约已取消");
        }
        
        // 检查是否已开始使用
        if (reservation.getStartTime().isBefore(LocalDateTime.now()) && reservation.getStatus() == 2) {
            throw new RuntimeException("该预约正在进行中，无法取消");
        }
        
        // 如果已支付，需要处理退款或积分订单特殊取消逻辑
        if (reservation.getPaymentStatus() != null && reservation.getPaymentStatus() == 1) {
            // 判断是否为积分兑换订单（金额为0）
            boolean isPointsOrder = reservation.getAmount() != null
                    && reservation.getAmount().compareTo(BigDecimal.ZERO) == 0;

            if (isPointsOrder) {
                // 积分兑换订单：不走退款逻辑，只允许在预约开始前取消
                LocalDateTime now = LocalDateTime.now();
                if (!reservation.getStartTime().isAfter(now)) {
                    throw new RuntimeException("已到预约开始时间，无法取消");
                }

                // 积分不退还，直接走后续的状态更新和座位释放逻辑
            } else {
            // 检查是否已退款
            if (reservation.getRefundStatus() != null && reservation.getRefundStatus() == 1) {
                throw new RuntimeException("该预约已退款，请勿重复操作");
            }

            // 校验退款时间窗口：支付时间之后的30分钟内可以退款
            if (reservation.getPaymentTime() != null) {
                long minutesSincePay = java.time.Duration
                        .between(reservation.getPaymentTime(), LocalDateTime.now())
                        .toMinutes();
                if (minutesSincePay > 30) {
                    throw new RuntimeException("超过退款允许时间，退款失败");
                }
            } else {
                throw new RuntimeException("支付时间不存在，无法判断是否可退款");
            }

            // 优先使用 tradeNo，如果没有 tradeNo，则仅用 outTradeNo 走退款
            String tradeNo = reservation.getTradeNo();
            boolean refundSuccess = alipayService.refund(
                reservation.getReservationNumber(), // outTradeNo 一定存在
                StringUtils.isBlank(tradeNo) ? null : tradeNo, // 允许为 null
                reservation.getAmount(),            // refundAmount
                "用户取消"                           // refundReason
            );

            if (!refundSuccess) {
                // 退款失败，抛出异常，让前端提示用户，避免订单取消但钱没退
                throw new RuntimeException("退款处理失败，请稍后重试或联系客服");
            }

            // 更新退款信息
            reservation.setRefundAmount(reservation.getAmount());
            reservation.setRefundTime(LocalDateTime.now());
            reservation.setRefundStatus(1); // 退款成功
            reservation.setRefundReason("用户取消");
            }
        }

        // 更新预约状态为已取消
        reservation.setStatus(4);
        reservationMapper.update(reservation);
        
        // 更新座位状态（释放锁定）
        Seat seat = seatService.getSeatById(reservation.getSeatId());
        if (seat != null) {
            // 如果座位是被锁定的状态（未支付），直接释放
            if (seat.getStatus() == 4) {
                // 检查是否有其他未支付的预约或长期租赁申请锁定该座位
                // 查询该座位的所有未支付预约
                List<Reservation> unpaidReservations = reservationMapper.selectUnpaidReservationsBySeatId(reservation.getSeatId());
                unpaidReservations.removeIf(r -> r.getId().equals(reservationId)); // 排除当前取消的预约
                
                // 查询该座位的所有未支付长期租赁申请
                List<LongTermLease> unpaidLeases = longTermLeaseMapper.selectUnpaidLeasesBySeatId(reservation.getSeatId());
                
                // 如果没有其他未支付的订单，释放座位
                if (unpaidReservations.isEmpty() && unpaidLeases.isEmpty()) {
                    // 查询该座位的所有有效预约（已付款待使用或使用中）
                    List<Reservation> activeReservations = reservationMapper.selectActiveReservationsBySeatId(reservation.getSeatId());
                    // 查询该座位的所有有效长期租赁
                    LongTermLease activeLease = longTermLeaseMapper.selectActiveBySeatId(reservation.getSeatId());
                    
                    if (activeReservations.isEmpty() && activeLease == null) {
                        seat.setStatus(0); // 空闲
                    } else if (!activeReservations.isEmpty()) {
                        seat.setStatus(1); // 已被预约
                    } else if (activeLease != null) {
                        seat.setStatus(2); // 被长期租赁
                    }
                    seatService.updateSeat(seat);
                }
            } else if (seat.getStatus() == 1) {
                // 如果座位是已被预约状态，检查是否还有其他有效预约
                List<Reservation> activeReservations = reservationMapper.selectActiveReservationsBySeatId(reservation.getSeatId());
                // 过滤掉已取消和已完成的预约
                boolean hasActiveReservation = activeReservations.stream()
                    .anyMatch(r -> r.getStatus() != 4 && r.getStatus() != 3 && !r.getId().equals(reservationId));
                if (!hasActiveReservation) {
                    // 检查是否有长期租赁
                    LongTermLease activeLease = longTermLeaseMapper.selectActiveBySeatId(reservation.getSeatId());
                    if (activeLease == null) {
                        seat.setStatus(0); // 空闲
                        seatService.updateSeat(seat);
                    }
                }
            }
        }
    }
    
    /**
     * 支付预约（模拟支付，实际应对接支付平台）
     */
    @Transactional
    public void payReservation(Long reservationId, Long userId, String tradeNo) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        // 检查是否为该用户的预约
        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("无权支付该预约");
        }
        
        // 检查预约状态
        if (reservation.getPaymentStatus() == 1) {
            throw new RuntimeException("该预约已支付");
        }
        
        if (reservation.getStatus() == 4) {
            throw new RuntimeException("该预约已取消，无法支付");
        }
        
        // 更新支付状态
        reservation.setPaymentStatus(1);
        reservation.setPaymentTime(LocalDateTime.now());
        reservation.setStatus(1); // 已付款待使用
        // 保存支付宝交易号（可能为null，例如积分兑换/其他非支付宝支付场景）
        reservation.setTradeNo(tradeNo);
        reservationMapper.update(reservation);
        
        // 更新座位状态为已被预约（从已锁定状态更新）
        Seat seat = seatService.getSeatById(reservation.getSeatId());
        if (seat != null) {
            // 如果座位是已锁定状态，更新为已被预约
            if (seat.getStatus() == 4) {
                seat.setStatus(1); // 已被预约
                seatService.updateSeat(seat);
            } else if (seat.getStatus() == 0) {
                // 如果座位是空闲状态（理论上不应该发生，但为了安全起见）
                seat.setStatus(1); // 已被预约
                seatService.updateSeat(seat);
            }
        }
    }

    /**
     * 开始/结束时间必须为整15分钟
     */
    private void validateQuarterHour(LocalDateTime time, String message) {
        if (time.getSecond() != 0 || time.getNano() != 0 || time.getMinute() % 15 != 0) {
            throw new RuntimeException(message);
        }
    }

    /**
     * 按当前时间动态计算状态：已付款且已到开始时间则视为使用中
     */
    private void applyRuntimeStatus(Reservation reservation, boolean persistChange) {
        if (reservation == null) {
            return;
        }
        // 仅对已支付且未取消的预约进行运行时状态同步
        if (reservation.getPaymentStatus() != null && reservation.getPaymentStatus() == 1
                && reservation.getStartTime() != null && reservation.getEndTime() != null
                && (reservation.getStatus() == null || reservation.getStatus() != 4)) {

            Integer originalStatus = reservation.getStatus();
            LocalDateTime now = LocalDateTime.now();
            int newStatus;
            if (now.isBefore(reservation.getStartTime())) {
                newStatus = 1; // 已支付待使用
            } else if (!now.isAfter(reservation.getEndTime())) {
                newStatus = 2; // 使用中
            } else {
                newStatus = 3; // 已完成
            }

            if (originalStatus == null || originalStatus != newStatus) {
                reservation.setStatus(newStatus);
                if (persistChange) {
                    reservationMapper.update(reservation);
                    
                    // 当状态从"已付款待使用"(1)变为"使用中"(2)时，自动赠送积分
                    if (originalStatus != null && originalStatus == 1 && newStatus == 2) {
                        try {
                            // 计算预约时长（小时）
                            long minutes = java.time.Duration.between(reservation.getStartTime(), reservation.getEndTime()).toMinutes();
                            int hours = (int) (minutes / 60);
                            
                            // 赠送积分（每2小时+1积分，取整）
                            pointsService.awardReservationPoints(reservation.getUserId(), reservation.getId(), hours);
                        } catch (Exception e) {
                            // 积分赠送失败不影响预约状态更新，仅记录错误
                            System.err.println("赠送预约积分失败: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取用户当前有效的入场凭证
     * 优先检查预约，如果没有再检查长期租赁
     * 预约条件：已支付且（在时间段内 或 距离开始时间≤15分钟）
     * 长期租赁条件：状态为2（已付款生效）且当前日期在租赁期间内
     */
    public EntryTicketVO getCurrentEntryTicket(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        
        // 获取用户信息（包含会员信息）
        User user = userMapper.selectById(userId);
        
        // 优先查询当前有效的预约
        Reservation reservation = reservationMapper.selectCurrentEntryTicket(userId);
        
        if (reservation != null) {
            // 应用运行时状态（确保状态是最新的）
            applyRuntimeStatus(reservation, false);
            
            // 构建入场凭证VO（预约类型）
            EntryTicketVO entryTicket = new EntryTicketVO();
            entryTicket.setCurrentTime(now);
            entryTicket.setUsername(reservation.getUsername());
            entryTicket.setSeatNumber(reservation.getSeatNumber());
            entryTicket.setSeatName(reservation.getSeatName());
            entryTicket.setRoomName(reservation.getRoomName());
            entryTicket.setRoomNumber(reservation.getRoomNumber());
            entryTicket.setStartTime(reservation.getStartTime());
            entryTicket.setEndTime(reservation.getEndTime());
            entryTicket.setReservationNumber(reservation.getReservationNumber());
            entryTicket.setType("reservation");
            
            // 设置会员信息
            if (user != null) {
                entryTicket.setMemberType(user.getMemberType());
                entryTicket.setMemberExpireTime(user.getMemberExpireTime());
            }
            
            // 判断凭证状态
            if (now.isBefore(reservation.getStartTime())) {
                // 距离开始时间≤15分钟，但还未开始
                entryTicket.setStatus("即将开始");
            } else if (now.isAfter(reservation.getEndTime())) {
                // 已结束（理论上不应该出现，因为查询条件已经过滤）
                entryTicket.setStatus("已结束");
            } else {
                // 在时间段内
                entryTicket.setStatus("可入场");
            }
            
            return entryTicket;
        }
        
        // 如果没有预约，查询当前有效的长期租赁
        LongTermLease lease = longTermLeaseMapper.selectCurrentActiveLeaseByUserId(userId);
        
        if (lease != null) {
            // 构建入场凭证VO（长期租赁类型）
            EntryTicketVO entryTicket = new EntryTicketVO();
            entryTicket.setCurrentTime(now);
            entryTicket.setUsername(lease.getUsername());
            entryTicket.setSeatNumber(lease.getSeatNumber());
            entryTicket.setSeatName(lease.getSeatName());
            entryTicket.setRoomName(lease.getRoomName());
            entryTicket.setRoomNumber(lease.getRoomNumber());
            // 长期租赁的开始和结束时间使用日期的开始和结束时间
            entryTicket.setStartTime(lease.getStartDate().atStartOfDay());
            entryTicket.setEndTime(lease.getEndDate().atTime(23, 59, 59));
            entryTicket.setLeaseNumber(lease.getLeaseNumber());
            entryTicket.setType("lease");
            entryTicket.setStatus("可入场");
            
            // 设置会员信息
            if (user != null) {
                entryTicket.setMemberType(user.getMemberType());
                entryTicket.setMemberExpireTime(user.getMemberExpireTime());
            }
            
            return entryTicket;
        }
        
        // 既没有预约也没有长期租赁
        return null;
    }
    
    /**
     * 取消超时未支付的预约（创建5分钟后仍未支付）
     */
    @Transactional
    public void cancelTimeoutUnpaidReservations() {
        List<Reservation> timeoutList = reservationMapper.selectUnpaidTimeoutReservations();
        if (timeoutList == null || timeoutList.isEmpty()) {
            return;
        }
        for (Reservation reservation : timeoutList) {
            reservation.setStatus(4); // 已取消
            reservation.setPaymentStatus(0);
            reservationMapper.update(reservation);
            
            // 释放座位锁定
            Seat seat = seatService.getSeatById(reservation.getSeatId());
            if (seat != null && seat.getStatus() == 4) {
                // 检查是否有其他未支付的预约或长期租赁申请锁定该座位
                List<Reservation> unpaidReservations = reservationMapper.selectUnpaidReservationsBySeatId(reservation.getSeatId());
                unpaidReservations.removeIf(r -> r.getId().equals(reservation.getId())); // 排除当前取消的预约
                
                List<LongTermLease> unpaidLeases = longTermLeaseMapper.selectUnpaidLeasesBySeatId(reservation.getSeatId());
                
                // 如果没有其他未支付的订单，释放座位
                if (unpaidReservations.isEmpty() && unpaidLeases.isEmpty()) {
                    // 查询该座位的所有有效预约和长期租赁
                    List<Reservation> activeReservations = reservationMapper.selectActiveReservationsBySeatId(reservation.getSeatId());
                    LongTermLease activeLease = longTermLeaseMapper.selectActiveBySeatId(reservation.getSeatId());
                    
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
    }
}

