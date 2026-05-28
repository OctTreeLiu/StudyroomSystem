package com.studyspace.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 入场凭证视图对象
 */
@Data
public class EntryTicketVO {
    
    /**
     * 当前系统时间
     */
    private LocalDateTime currentTime;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 座位编号
     */
    private String seatNumber;
    
    /**
     * 座位名称
     */
    private String seatName;
    
    /**
     * 自习室名称
     */
    private String roomName;
    
    /**
     * 自习室编号
     */
    private String roomNumber;
    
    /**
     * 预约开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 预约结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 凭证状态（如：可入场）
     */
    private String status;
    
    /**
     * 预约编号
     */
    private String reservationNumber;
    
    /**
     * 长期租赁编号
     */
    private String leaseNumber;
    
    /**
     * 凭证类型：reservation-预约，lease-长期租赁
     */
    private String type;
    
    /**
     * 会员类型：0-普通用户，1-VIP，2-SVIP
     */
    private Integer memberType;
    
    /**
     * 会员到期时间
     */
    private LocalDateTime memberExpireTime;
}

