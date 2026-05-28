package com.studyspace.service;

import com.studyspace.entity.Notification;
import com.studyspace.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息通知服务类
 */
@Service
public class NotificationService {
    
    @Autowired
    private NotificationMapper notificationMapper;
    
    /**
     * 创建消息通知
     */
    @Transactional
    public void createNotification(Notification notification) {
        if (notification.getCreateTime() == null) {
            notification.setCreateTime(LocalDateTime.now());
        }
        if (notification.getIsRead() == null) {
            notification.setIsRead(0);
        }
        notificationMapper.insert(notification);
    }
    
    /**
     * 为用户创建预约提醒消息
     */
    @Transactional
    public void createReservationReminder(Long userId, String username, String roomName, String seatNumber, LocalDateTime endTime) {
        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(1); // 预约提醒
            notification.setTitle("预约即将结束提醒");
            notification.setContent(String.format("您的预约（%s %s号座位）将在5分钟后结束。如需继续使用，请及时续约。如不续约，请检查好随身物品后离开。", roomName, seatNumber));
            notification.setIsRead(0);
            notification.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(notification);
            System.out.println("用户提醒消息创建成功: userId=" + userId + ", content=" + notification.getContent());
        } catch (Exception e) {
            System.err.println("创建用户提醒消息失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 为管理员创建预约结束提醒消息
     */
    @Transactional
    public void createAdminReservationReminder(String username, String roomName, String seatNumber) {
        try {
            Notification notification = new Notification();
            notification.setUserId(null); // NULL表示发送给所有管理员
            notification.setType(1); // 预约提醒
            notification.setTitle("座位使用即将结束提醒");
            notification.setContent(String.format("用户 %s 正在使用的 %s %s号位置即将在5分钟后结束。", username, roomName, seatNumber));
            notification.setIsRead(0);
            notification.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(notification);
            System.out.println("管理员提醒消息创建成功: content=" + notification.getContent());
        } catch (Exception e) {
            System.err.println("创建管理员提醒消息失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 根据用户ID查询消息列表
     */
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationMapper.selectByUserId(userId);
    }
    
    /**
     * 查询用户未读消息数量
     */
    public int getUnreadCountByUserId(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }
    
    /**
     * 查询所有管理员的消息
     */
    public List<Notification> getAdminNotifications() {
        return notificationMapper.selectAdminNotifications();
    }
    
    /**
     * 标记消息为已读
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationMapper.markAsRead(notificationId, LocalDateTime.now());
    }
    
    /**
     * 标记用户所有消息为已读
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsReadByUserId(userId, LocalDateTime.now());
    }
}

