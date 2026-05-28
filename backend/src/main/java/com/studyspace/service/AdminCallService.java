package com.studyspace.service;

import com.studyspace.common.PageResult;
import com.studyspace.entity.AdminCall;
import com.studyspace.entity.LongTermLease;
import com.studyspace.entity.Notification;
import com.studyspace.entity.User;
import com.studyspace.mapper.AdminCallMapper;
import com.studyspace.mapper.LongTermLeaseMapper;
import com.studyspace.vo.EntryTicketVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员呼叫服务类
 */
@Service
public class AdminCallService {
    
    @Autowired
    private AdminCallMapper adminCallMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private LongTermLeaseMapper longTermLeaseMapper;
    
    /**
     * 创建呼叫记录并发送通知给管理员
     */
    @Transactional
    public AdminCall createCall(Long userId, String message) {
        // 获取用户信息
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 获取用户当前座位信息
        String seatInfo = getCurrentSeatInfo(userId);
        
        // 创建呼叫记录
        AdminCall adminCall = new AdminCall();
        adminCall.setUserId(userId);
        adminCall.setUsername(user.getUsername());
        adminCall.setPhone(user.getPhone());
        adminCall.setSeatInfo(seatInfo);
        adminCall.setMessage(message);
        adminCall.setStatus(0); // 待处理
        adminCall.setCreateTime(LocalDateTime.now());
        
        adminCallMapper.insert(adminCall);
        
        // 发送通知给所有管理员
        Notification notification = new Notification();
        notification.setUserId(null); // NULL表示发送给所有管理员
        notification.setType(2); // 系统通知
        notification.setTitle("用户呼叫管理员");
        
        StringBuilder content = new StringBuilder();
        content.append("用户 ").append(user.getUsername());
        if (seatInfo != null && !seatInfo.isEmpty()) {
            content.append("（").append(seatInfo).append("）");
        }
        content.append(" 请求帮助。");
        if (message != null && !message.trim().isEmpty()) {
            content.append("留言：").append(message);
        }
        content.append(" 联系电话：").append(user.getPhone() != null ? user.getPhone() : "未填写");
        
        notification.setContent(content.toString());
        notification.setIsRead(0);
        notification.setRelatedId(adminCall.getId());
        notificationService.createNotification(notification);
        
        return adminCall;
    }
    
    /**
     * 获取用户当前座位信息
     */
    private String getCurrentSeatInfo(Long userId) {
        try {
            // 优先查询当前有效的预约
            EntryTicketVO entryTicket = reservationService.getCurrentEntryTicket(userId);
            
            if (entryTicket != null) {
                if (entryTicket.getRoomName() != null && entryTicket.getSeatNumber() != null) {
                    return entryTicket.getRoomName() + " " + entryTicket.getSeatNumber() + "号座位";
                } else if (entryTicket.getSeatName() != null) {
                    return entryTicket.getSeatName();
                }
            }
            
            // 如果没有预约，查询当前有效的长期租赁
            LongTermLease lease = longTermLeaseMapper.selectCurrentActiveLeaseByUserId(userId);
            if (lease != null) {
                if (lease.getRoomName() != null && lease.getSeatNumber() != null) {
                    return lease.getRoomName() + " " + lease.getSeatNumber() + "号座位（长期租赁）";
                } else if (lease.getSeatName() != null) {
                    return lease.getSeatName() + "（长期租赁）";
                }
                return "长期租赁座位";
            }
        } catch (Exception e) {
            // 静默失败，不影响呼叫功能
            System.err.println("获取座位信息失败: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 查询所有呼叫记录
     */
    public List<AdminCall> getAllCalls() {
        return adminCallMapper.selectAll();
    }
    
    /**
     * 分页查询所有呼叫记录，支持按状态和用户名/用户ID模糊查询
     */
    public PageResult<AdminCall> getAllCallsWithPagination(Integer page,
                                                           Integer pageSize,
                                                           Integer status,
                                                           String keyword) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        int offset = (page - 1) * pageSize;
        List<AdminCall> list;
        long total;

        if (status != null) {
            list = adminCallMapper.selectByStatusWithPagination(status, offset, pageSize, keyword);
            total = adminCallMapper.countByStatus(status, keyword);
        } else {
            list = adminCallMapper.selectAllWithPagination(offset, pageSize, keyword);
            total = adminCallMapper.countAll(keyword);
        }
        
        return new PageResult<>(list, total, page, pageSize);
    }
    
    /**
     * 根据状态查询呼叫记录
     */
    public List<AdminCall> getCallsByStatus(Integer status) {
        return adminCallMapper.selectByStatus(status);
    }
    
    /**
     * 根据用户ID查询呼叫记录
     */
    public List<AdminCall> getCallsByUserId(Long userId) {
        return adminCallMapper.selectByUserId(userId);
    }
    
    /**
     * 处理呼叫（标记为已处理）
     */
    @Transactional
    public void handleCall(Long callId, Long adminId) {
        AdminCall adminCall = adminCallMapper.selectById(callId);
        if (adminCall == null) {
            throw new RuntimeException("呼叫记录不存在");
        }
        
        adminCall.setStatus(1); // 已处理
        adminCall.setHandleTime(LocalDateTime.now());
        adminCall.setHandleAdminId(adminId);
        
        adminCallMapper.update(adminCall);
    }
    
    /**
     * 统计待处理数量
     */
    public int countPending() {
        return adminCallMapper.countPending();
    }
}

