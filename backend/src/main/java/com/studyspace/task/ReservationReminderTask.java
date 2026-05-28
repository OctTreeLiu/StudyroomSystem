package com.studyspace.task;

import com.studyspace.entity.Reservation;
import com.studyspace.mapper.ReservationMapper;
import com.studyspace.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 预约提醒定时任务
 * 检查即将在5分钟内结束的预约，并发送提醒消息
 */
@Component
public class ReservationReminderTask {
    
    @Autowired
    private ReservationMapper reservationMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    // 记录已发送提醒的预约ID，避免重复发送
    private final Set<Long> sentReminders = new HashSet<>();
    
    /**
     * 每分钟执行一次，检查即将结束的预约
     */
    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    public void checkReservationsEndingSoon() {
        try {
            LocalDateTime now = LocalDateTime.now();
            System.out.println("========== 预约提醒定时任务执行 ==========");
            System.out.println("当前时间: " + now);
            
            // 查询即将在5分钟内结束的预约
            List<Reservation> reservations = reservationMapper.selectReservationsEndingSoon();
            System.out.println("查询到 " + reservations.size() + " 个即将结束的预约");
            
            for (Reservation reservation : reservations) {
                System.out.println("检查预约 ID: " + reservation.getId() + 
                    ", 用户: " + reservation.getUsername() + 
                    ", 结束时间: " + reservation.getEndTime());
                
                // 检查是否已经发送过提醒（避免重复发送）
                if (sentReminders.contains(reservation.getId())) {
                    System.out.println("预约 " + reservation.getId() + " 已发送过提醒，跳过");
                    continue;
                }
                
                // 检查是否真的在5分钟内结束
                LocalDateTime endTime = reservation.getEndTime();
                if (endTime == null) {
                    System.out.println("预约 " + reservation.getId() + " 结束时间为空，跳过");
                    continue;
                }
                
                long minutesUntilEnd = java.time.Duration.between(now, endTime).toMinutes();
                System.out.println("预约 " + reservation.getId() + " 距离结束还有 " + minutesUntilEnd + " 分钟");
                
                // 放宽条件：在0-5分钟内结束的都发送提醒
                if (minutesUntilEnd >= 0 && minutesUntilEnd <= 5) {
                    System.out.println("为预约 " + reservation.getId() + " 发送提醒消息");
                    
                    try {
                        // 为用户发送提醒
                        notificationService.createReservationReminder(
                            reservation.getUserId(),
                            reservation.getUsername() != null ? reservation.getUsername() : "用户",
                            reservation.getRoomName() != null ? reservation.getRoomName() : "未知大区",
                            reservation.getSeatNumber() != null ? reservation.getSeatNumber() : "未知座位",
                            endTime
                        );
                        System.out.println("用户提醒消息已创建");
                        
                        // 为管理员发送提醒
                        notificationService.createAdminReservationReminder(
                            reservation.getUsername() != null ? reservation.getUsername() : "用户",
                            reservation.getRoomName() != null ? reservation.getRoomName() : "未知大区",
                            reservation.getSeatNumber() != null ? reservation.getSeatNumber() : "未知座位"
                        );
                        System.out.println("管理员提醒消息已创建");
                        
                        // 记录已发送提醒
                        sentReminders.add(reservation.getId());
                        System.out.println("预约 " + reservation.getId() + " 提醒发送成功");
                    } catch (Exception e) {
                        System.err.println("发送提醒消息失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("预约 " + reservation.getId() + " 不在提醒时间范围内（" + minutesUntilEnd + " 分钟）");
                }
            }
            
            // 清理已结束的预约ID（避免内存泄漏）
            sentReminders.removeIf(id -> {
                try {
                    Reservation r = reservationMapper.selectById(id);
                    return r == null || (r.getEndTime() != null && r.getEndTime().isBefore(now));
                } catch (Exception e) {
                    return true;
                }
            });
            
            System.out.println("========== 预约提醒定时任务完成 ==========");
            
        } catch (Exception e) {
            System.err.println("检查预约提醒失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

